package net.branchlock.task.implementation.encryption.scrambler;

import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.metadata.TaskMetadata;

import java.util.List;

@TaskMetadata(name = "Logic scrambler", priority = TaskMetadata.Level.FIFTH, performanceCost = TaskMetadata.PerformanceCost.MINIMAL, ids = "scramble")
public class Scrambler extends Task {

  public Scrambler(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    return List.of(new SyntheticClassesDriver(this), new MethodScrambleDriver(this));
  }

}
