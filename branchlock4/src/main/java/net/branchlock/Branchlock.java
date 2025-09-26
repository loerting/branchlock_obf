package net.branchlock;

import net.branchlock.commons.asm.Conversion;
import net.branchlock.commons.asm.MajorVersion;
import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.decompiler.CFREmulator;
import net.branchlock.inputprovider.BranchlockInputProvider;
import net.branchlock.inputprovider.BranchlockRunType;
import net.branchlock.inputprovider.ConfigProvider;
import net.branchlock.layout.demo.InteractiveDemo;
import net.branchlock.logging.LogWrapper;
import net.branchlock.stacktrace.StackTraceDecryption;
import net.branchlock.structure.BClass;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.agent.TaskAgent;
import net.branchlock.task.driver.passthrough.ConfigClassExclusionPassThrough;
import net.branchlock.task.factory.TaskFactory;
import net.branchlock.task.implementation.generic.CrashTools;
import net.branchlock.task.metadata.TaskMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class Branchlock {
  public static final LogWrapper LOGGER = new LogWrapper();
  public static SecureRandom R = null;
  public final SettingsManager settingsManager;
  private final BranchlockInputProvider inputProvider;
  private final Config config;

  public Branchlock(BranchlockInputProvider inputProvider) {
    this.inputProvider = inputProvider;
    ConfigProvider configProvider = inputProvider.getConfigProvider();
    config = configProvider.loadConfig();
    settingsManager = new SettingsManager(config);
  }

  public int startBranchlock() {
    BranchlockRunType runType = inputProvider.getRunType();
    LOGGER.info("Branchlock {}", getClass().getPackage().getImplementationVersion());
    LOGGER.info("Copyright (c) {} branchlock.net. All rights reserved.{}", Calendar.getInstance().get(Calendar.YEAR), System.lineSeparator());
    LOGGER.info("Using JVM {} and run type {}.", System.getProperty("java.version"), runType.name());

    byte[] randomSeed = settingsManager.getRandomSeed();
    if (randomSeed == null) {
      Branchlock.LOGGER.info("No random seed provided. Using system implementation.");
      R = new SecureRandom();
    } else {
      Branchlock.LOGGER.info("A random seed was provided.");
      try {
        R = SecureRandom.getInstance("SHA1PRNG");
        R.setSeed(randomSeed);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
    }

    if (settingsManager.isDebugMode()) {
      LOGGER.warning("Debug mode enabled, performing debug checks.");
      LOGGER.enableDebugMode();
    }

    switch (runType) {
      case STACKTRACE_DECRYPTION:
        return StackTraceDecryption.decryptStacktraceConfig(config);
      case INTERACTIVE_DEMO:
      case TRANSFORMATION:
        return startObfuscationRoutine(runType);
    }
    return 0;
  }

  private int startObfuscationRoutine(BranchlockRunType runType) {
    DataProvider dataProvider = new DataProvider(this);
    LOGGER.beginSection("Loading classes");
    switch (runType) {
      case INTERACTIVE_DEMO:
        LOGGER.info("Loading interactive demo classes...");
        dataProvider.addClass(Conversion.loadProgramClass(dataProvider, InteractiveDemo.class));

        dataProvider.setInteractiveDemo(true);
        config.setValue("demo", false);
        break;
      case TRANSFORMATION:
        try {
          File inputFile = settingsManager.getInputFile();
          LOGGER.info("Loading classes from provided input file.");
          dataProvider.loadInputJar(inputFile, false);
        } catch (IOException e) {
          LOGGER.error("Could not load input jar", e);
          return 1;
        }
        long includedClassesGeneral = new ConfigClassExclusionPassThrough(settingsManager.getGeneralConfig(), dataProvider)
          .passThrough(dataProvider.streamInputClasses()).count();

        if (includedClassesGeneral == 0) {
          LOGGER.info("No classes included generally.");
        }

        if (settingsManager.isDemo() && includedClassesGeneral > 1500) {
          LOGGER.error("The demo version only supports a maximum of 1500 included classes.");
          return 2;
        }

        if (includedClassesGeneral > 1500) {
          if (settingsManager.isDisableClassLimit()) {
            LOGGER.info("Class limit reached but ignored. Continuing.");
          } else {
            LOGGER.error("Class limit reached: More than 1500 classes are included generally. ({} classes)", includedClassesGeneral);
            LOGGER.error("This is not recommended as it can lead to various problems. Please refer to our documentation.");
            LOGGER.error("If you still want to proceed, you can disable the class limit in general settings.");
            return 2;
          }
        }
        break;
    }
    if (config.has("libraries")) {
      LOGGER.beginSection("Loading libraries");
      int libNum = 0;
      for (String library : settingsManager.getLibraries()) {
        try {
          LOGGER.info("Loading library file " + libNum + "...");
          dataProvider.loadLibraryJar(new File(library));
          libNum++;
        } catch (IOException e) {
          LOGGER.error("Failed to load library jar: {}", e, library);
          return 1;
        }
      }
      LOGGER.info("Loaded {} library class(es).{}", dataProvider.getLibs().size(), System.lineSeparator());
    }

    if (settingsManager.isAndroid()) {
      BClass bClass = dataProvider.resolveBClass("android/os/Build", null);
      if (bClass == null) {
        LOGGER.error("Android mode requires the android.jar to be loaded as a library.");
        return 1;
      }
      LOGGER.info("Android runtime classes have been found.");
    }

    dataProvider.detectDependencyCycles();
    dataProvider.prepareAllClasses();

    int targetVersion = Math.toIntExact(settingsManager.getGeneralConfig()
      .getOrSetDefaultValue("target_version", () -> settingsManager.determineDefaultTargetVersion(dataProvider)));
    if (targetVersion < MajorVersion.JAVA_5.getCode()) {
      LOGGER.error("Target version must be at least Java 5. (Current: {})", targetVersion);
      return 1;
    }
    LOGGER.info("Target version: {}", MajorVersion.codeToVersion(targetVersion));

    TaskFactory taskFactory = new TaskFactory(config, settingsManager, dataProvider);
    List<Task> tasksToRun = taskFactory.createTasksFromConfig();

    if (settingsManager.isDebugMode())
      dataProvider.sanityCheck();
    // watermarking
    if (runType == BranchlockRunType.TRANSFORMATION) {
      if (settingsManager.isDemo())
        tasksToRun.add(taskFactory.createTaskFromId("demo_watermark"));
      else if (!settingsManager.isNoWatermark())
        tasksToRun.add(taskFactory.createTaskFromId("watermark"));
      tasksToRun.add(taskFactory.createTaskFromId("bl-anno-remover"));
    }

    LOGGER.beginSection("Running tasks");
    LOGGER.info("Running tasks...{}", System.lineSeparator());

    // ensure that tasks are run in the correct order
    tasksToRun.sort(Comparator.comparingInt(t -> t.getMetadata().priority().ordinal()));
    for (Task task : tasksToRun) {
      TaskMetadata metadata = task.getMetadata();
      LOGGER.beginSection("Running task \"" + metadata.name() + "\"");
      LOGGER.info("Starting task \"" + metadata.name() + "\".");
      TaskAgent taskAgent = new TaskAgent(task, dataProvider);
      taskAgent.execute();
      LOGGER.info("Finished task.{}", System.lineSeparator());

      if (settingsManager.isDebugMode())
        dataProvider.sanityCheck();
    }

    File outputFile = settingsManager.getOutputFile();

    LOGGER.beginSection("Saving output");
    LOGGER.info("Saving output to provided output file...");

    switch (runType) {
      case INTERACTIVE_DEMO:
        LOGGER.info("Decompiling interactive demo...");
        String demoClassName = InteractiveDemo.class.getName().replace('.', '/');
        BClass demoClassObfed = dataProvider.findClassByOriginalName(demoClassName);
        try {
          Files.write(outputFile.toPath(), new CFREmulator(demoClassObfed).decompile());
        } catch (Exception e) {
          LOGGER.error("Could not write decompiled demo", e);
          return 1;
        }
        return 0;
      case TRANSFORMATION:
        return dataProvider.saveAsJar(outputFile, isCrasherPresentAndFolderTrick(tasksToRun));
      default:
        throw new AssertionError("Invalid run type");
    }
  }

  private boolean isCrasherPresentAndFolderTrick(List<Task> tasksToRun) {
    CrashTools crashTools = tasksToRun.stream()
      .filter(task -> task instanceof CrashTools)
      .map(task -> (CrashTools) task)
      .findFirst()
      .orElse(null);
    return crashTools != null && crashTools.isFolderTrick();
  }


}
