package net.branchlock.task.implementation.salting;

import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.implementation.salting.drivers.*;
import net.branchlock.task.metadata.TaskMetadata;

import java.util.List;

@TaskMetadata(name = "Salting", priority = TaskMetadata.Level.THIRD, demoApplicable = false,
  performanceCost = TaskMetadata.PerformanceCost.MINIMAL, ids = {"salting", "generify"})
public class Salting extends Task {
  public Salting(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    SaltingSignatureCollectorDriver saltingSignatureCollectorDriver = new SaltingSignatureCollectorDriver(this);
    SaltingVariableOffsetterDriver saltingVariableOffsetterDriver = new SaltingVariableOffsetterDriver(this, saltingSignatureCollectorDriver);
    SaltingReferenceUpdaterDriver saltingReferenceUpdaterDriver = new SaltingReferenceUpdaterDriver(this, saltingSignatureCollectorDriver);
    SaltingSignatureUpdaterDriver saltingSignatureUpdaterDriver = new SaltingSignatureUpdaterDriver(this, saltingSignatureCollectorDriver);

    // update references before updating signatures. we cannot associate the references if the signatures are updated first.

    return List.of(saltingSignatureCollectorDriver, saltingVariableOffsetterDriver, saltingReferenceUpdaterDriver,
      saltingSignatureUpdaterDriver, new TrapInserterDriver(this)); // new SaltingDebugDriver(this)
  }
}
