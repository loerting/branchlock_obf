package net.branchlock.task.factory;

import net.branchlock.Branchlock;
import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.implementation.coverage.RuntimeCoverage;
import net.branchlock.task.implementation.encryption.StacktraceEncryption;
import net.branchlock.task.implementation.encryption.scrambler.Scrambler;
import net.branchlock.task.implementation.flow.ControlFlow;
import net.branchlock.task.implementation.generic.BranchlockAnnotationRemover;
import net.branchlock.task.implementation.generic.CrashTools;
import net.branchlock.task.implementation.generic.UnifyVisibility;
import net.branchlock.task.implementation.generic.ShuffleMembers;
import net.branchlock.task.implementation.merger.MethodMerger;
import net.branchlock.task.implementation.monitoring.RootChecker;
import net.branchlock.task.implementation.monitoring.debugchecker.DebugChecker;
import net.branchlock.task.implementation.naming.Renamer;
import net.branchlock.task.implementation.references.calls.References;
import net.branchlock.task.implementation.references.numbers.Numbers;
import net.branchlock.task.implementation.references.strings.Strings;
import net.branchlock.task.implementation.removal.information.DebugInfoRemover;
import net.branchlock.task.implementation.removal.Trimmer;
import net.branchlock.task.implementation.salting.Salting;
import net.branchlock.task.implementation.watermark.DemoWatermark;
import net.branchlock.task.implementation.watermark.Watermark;
import net.branchlock.task.metadata.TaskMetadata;

import java.util.*;

/**
 * This factory is used to create tasks.
 */
public class TaskFactory {
  private static final Set<Class<? extends Task>> TASKS = new HashSet<>() {{
    add(DemoWatermark.class);
    add(Watermark.class);
    add(StacktraceEncryption.class);
    add(RuntimeCoverage.class);
    add(ControlFlow.class);
    add(Scrambler.class);
    add(DebugInfoRemover.class);
    add(ShuffleMembers.class);
    add(UnifyVisibility.class);
    add(RootChecker.class);
    add(DebugChecker.class);
    add(Numbers.class);
    add(Strings.class);
    add(Salting.class);
    add(References.class);
    add(Renamer.class);
    add(Trimmer.class);
    add(CrashTools.class);
    add(MethodMerger.class);
    add(BranchlockAnnotationRemover.class);
  }};

  private final Config config;
  private final SettingsManager settingsManager;
  private final DataProvider dataProvider;

  public TaskFactory(Config config, SettingsManager settingsManager, DataProvider dataProvider) {
    this.config = config;
    this.settingsManager = settingsManager;
    this.dataProvider = dataProvider;
  }

  public List<Task> createTasksFromConfig() {
    if (!config.has("tasks"))
      return Collections.emptyList();

    Map<String, Config> taskConfigs = getTaskConfigs();
    taskConfigs.values().removeIf(tConfig -> !tConfig.getOrDefaultValue("enabled", true));

    List<Task> tasks = new ArrayList<>();

    for (String taskId : taskConfigs.keySet()) {
      Class<? extends Task> taskClass = getTaskClassFromId(taskId);
      if (taskClass == null) {
        Branchlock.LOGGER.error("Task with id \"{}\" is not known.", taskId);
        continue;
      }
      TaskMetadata annotation = (TaskMetadata) taskClass.getAnnotations()[0];

      /*
       11.02.2025 remove demo task limitation

      if (!annotation.demoApplicable() && settingsManager.isDemo()) {
        Branchlock.LOGGER.error("Skipping task \"" + annotation.name() + "\" because it is not applicable in demo mode.");
        continue;
      }

       */
      if (!annotation.androidCompatible() && settingsManager.isAndroid()) {
        Branchlock.LOGGER.error("Skipping task \"" + annotation.name() + "\" because it is not compatible with Android.");
        continue;
      }
      if (!annotation.desktopCompatible() && !settingsManager.isAndroid()) {
        Branchlock.LOGGER.error("Skipping task \"" + annotation.name() + "\" because it is not desktop compatible.");
        continue;
      }

      tasks.add(createTask(taskClass, taskConfigs.get(taskId)));
    }
    return tasks;
  }

  public Class<? extends Task> getTaskClassFromId(String id) {
    for (Class<? extends Task> taskClass : TASKS) {
      TaskMetadata annotation = (TaskMetadata) taskClass.getAnnotations()[0];
      if (Arrays.asList(annotation.ids()).contains(id)) return taskClass;
    }
    return null;
  }


  private Map<String, Config> getTaskConfigs() {
    return config.getMapValue("tasks", String.class);
  }

  private Task createTask(Class<? extends Task> taskClass, Config taskConfig) {
    try {
      return taskClass.getConstructor(SettingsManager.class, Config.class, DataProvider.class).newInstance(settingsManager, taskConfig, dataProvider);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Task createTaskFromId(String id) {
    Map<String, Config> taskConfigs = getTaskConfigs();

    Class<? extends Task> taskClass = getTaskClassFromId(id);
    if (taskClass == null) throw new IllegalArgumentException("Task with id " + id + " is not known.");
    return createTask(taskClass, taskConfigs.get(id));
  }
}
