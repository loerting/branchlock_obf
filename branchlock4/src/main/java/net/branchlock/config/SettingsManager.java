package net.branchlock.config;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.MajorVersion;
import net.branchlock.structure.provider.DataProvider;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class SettingsManager {
  private final Config config;
  private final Config generalConfig;

  public SettingsManager(Config config) {
    this.config = config;
    this.generalConfig = config.getSubConfig("general");
  }

  public File getInputFile() {
    String input = config.getMandatoryValue("input");
    File inputFile = new File(input);
    if (!inputFile.exists()) {
      throw new IllegalArgumentException("Input file does not exist: " + input);
    }
    if (!inputFile.isFile()) {
      throw new IllegalArgumentException("Input is not a file: " + input);
    }
    try {
      if (inputFile.equals(new File(SettingsManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()))) {
        throw new IllegalArgumentException("Cannot obfuscate itself.");
      }
    } catch (URISyntaxException e) {
      Branchlock.LOGGER.error("Failed to get jar location.", e);
    }
    return inputFile;
  }

  public File getOutputFile() {
    String output = config.getMandatoryValue("output");
    File outputFile = new File(output);
    if (outputFile.exists()) {
      if (!outputFile.isFile()) {
        throw new IllegalArgumentException("Output is not a file: " + output);
      }
    }
    return outputFile;
  }

  public Collection<String> getLibraries() {
    return config.getMultiValue("libraries");
  }

  public boolean isDemo() {
    return config.getOrDefaultValue("demo", false);
  }

  public boolean isAndroid() {
    return config.getOrDefaultValue("android", false);
  }

  public boolean isNoWatermark() {
    return generalConfig.getOrDefaultValue("no_watermark", false);
  }

  public boolean isNoCompression() {
    return generalConfig.getOrDefaultValue("no_compress", false);
  }

  public boolean isRemoveEmptyDirs() {
    return generalConfig.getOrDefaultValue("remove_empty_directories", false);
  }

  public int getTargetVersion() {
    return Math.toIntExact(generalConfig.getOrDefaultValue("target_version", MajorVersion.JAVA_5.getCode()));
  }

  public Config getGeneralConfig() {
    return generalConfig;
  }

  public boolean isDebugMode() {
    // if the implementation version is null, we are in an IDE.
    return config.getOrDefaultValue("debug_mode", false) || getClass().getPackage().getImplementationVersion() == null;
  }

  public String getNameIterator() {
    return generalConfig.getOrDefaultValue("name_iterator", "alphanumeric");
  }

  public long determineDefaultTargetVersion(DataProvider dp) {
    Branchlock.LOGGER.info("Guessing target version using median (\"target_version\" not set) ...");
    int[] versions = dp.streamInputClasses().mapToInt(bc -> bc.version).sorted().toArray();

    if (versions.length == 0) {
      return MajorVersion.JAVA_8.getCode();
    }
    // get the median of the sorted array
    int version = versions[Math.max(0, versions.length / 2 - 1)];
    if (version < MajorVersion.JAVA_5.getCode()) {
      Branchlock.LOGGER.info("Raised target version to Java 5.");
      version = MajorVersion.JAVA_5.getCode();
    }
    return version;
  }

  public boolean isDisableClassLimit() {
    return !isDemo() && generalConfig.getOrDefaultValue("disable_class_limit", false);
  }

  public byte[] getRandomSeed() {
    if (!generalConfig.has("random_seed"))
      return null;
    String randomSeed = generalConfig.<String>getMandatoryValue("random_seed");
    if (randomSeed.isEmpty())
      return null;

    return randomSeed.getBytes(StandardCharsets.UTF_8);
  }

  public boolean isLoadInnerJars() {
    return generalConfig.getOrDefaultValue("load_inner_jars", false);
  }
}
