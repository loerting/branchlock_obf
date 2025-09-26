package net.branchlock.task.implementation.monitoring.debugchecker;

import net.branchlock.Branchlock;
import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.metadata.TaskMetadata;

import java.util.ArrayList;
import java.util.List;

@TaskMetadata(name = "Debug detection", priority = TaskMetadata.Level.FOURTH, ids = {"anti-debug", "debug-checker", "debug-detection"})
public class DebugChecker extends Task {
  private static final List<String> FORBIDDEN_ARGS = new ArrayList<>();
  private final boolean checkNoverifyArgument = innerConfig.getOrDefaultValue("check_noverify", false);

  public DebugChecker(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public void preExecute() {
    FORBIDDEN_ARGS.addAll(List.of("-Xdebug", "-verbose", "-XX:+TraceClassLoading", "-Xbootclasspath", "-verbose:class", "-verbose:jni", "-verbose:gc"));
    if (checkNoverifyArgument) {
      FORBIDDEN_ARGS.add("-Xverify:none");
      Branchlock.LOGGER.info("Added -Xverify:none (-noverify) to the list of forbidden arguments.");
    }
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    if (settingsManager.isAndroid())
      return List.of(new AndroidDebugCheckerDriver(this));
    else
      return List.of(new DesktopDebugCheckerDriver(this, FORBIDDEN_ARGS));
  }
}
