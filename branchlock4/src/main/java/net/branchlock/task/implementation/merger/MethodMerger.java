package net.branchlock.task.implementation.merger;

import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.implementation.references.drivers.IllegalAccessFixDriver;
import net.branchlock.task.metadata.TaskMetadata;

import java.util.List;

@TaskMetadata(name = "Method merger", priority = TaskMetadata.Level.SECOND,
  performanceCost = TaskMetadata.PerformanceCost.MINIMAL, ids = "method-merger", demoApplicable = false)
public class MethodMerger extends Task {

  public MethodMerger(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    MergeMethodContainerCreator containerCreator = new MergeMethodContainerCreator(this);
    MethodMergerUpdater updater = new MethodMergerUpdater(this, containerCreator);
    return List.of(
      containerCreator,
      updater,
      new IllegalAccessFixDriver(this)
    );
  }
}
