package net.branchlock.task.implementation.flow;

import net.branchlock.Branchlock;
import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.implementation.flow.passes.*;
import net.branchlock.task.metadata.TaskMetadata;

import java.util.Arrays;
import java.util.List;

@TaskMetadata(name = "Flow obfuscation", priority = TaskMetadata.Level.FOURTH,
  performanceCost = TaskMetadata.PerformanceCost.NOTICEABLE, ids = "flow", demoApplicable = false)
public class ControlFlow extends Task {

  public static final List<String> IGNORED_METHODS = Arrays.asList("equals", "hashCode", "valueOf");
  public static final float MAX_COVERAGE = 1.0f;
  public float coveragePct = innerConfig.getOrDefaultValue("coverage", 0.5f);

  public ControlFlow(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
    if (innerConfig.getOrDefaultValue("heavy", false)) {
      Branchlock.LOGGER.warning("Please don't use \"heavy\": true anymore, use \"coverage\": 0.8 instead.");
      coveragePct = Math.max(coveragePct, 0.8f);
    }
  }

  public static List<IDriver<?>> getFlowDrivers(float coveragePercent, Task task) {
    // TODO add jumps to TCBs pass and fix its performance issues
    return List.of(
      new FlatteningPass(task, coveragePercent),
      new RedundantEdgesPass(task, coveragePercent),
      new TryCatchTrapPass(task, coveragePercent),
      new BadJumpTargetPass(task, coveragePercent),
      new HandlerTrapPass(task, coveragePercent)
    );
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    return getFlowDrivers(coveragePct, this);
  }

  /*
    Observations:
    Do not use exceptions thrown by the JVM as jumps. This is very bad for performance.
      - replacing goto with monitor enter/exit exception did not work well (is also not supported on dex)
      - converting jumps to TCBs did not work well
   */
}
