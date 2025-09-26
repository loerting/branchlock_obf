package net.branchlock.task.implementation.references.calls;

import net.branchlock.Branchlock;
import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.implementation.references.calls.drivers.CallEncryptionDriver;
import net.branchlock.task.implementation.references.calls.drivers.DecryptionClassGeneratorDriver;
import net.branchlock.task.metadata.TaskMetadata;

import java.util.List;

@TaskMetadata(name = "Reference encryption", priority = TaskMetadata.Level.SIXTH,
  performanceCost = TaskMetadata.PerformanceCost.NOTICEABLE, demoApplicable = false, ids = "references")
public class References extends Task {
  private final boolean excludeCallsToRuntime = innerConfig.getOrDefaultValue("exclude_runtime", false) || innerConfig.getOrDefaultValue("only_local", false);
  private final boolean excludeFields = innerConfig.getOrDefaultValue("ignore_fields", false) || innerConfig.getOrDefaultValue("exclude_fields", false);

  public References(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public void preExecute() {
    if (innerConfig.getOrDefaultValue("force_encrypt_unresolved", false)) {
      Branchlock.LOGGER.warning("force_encrypt_unresolved is not supported anymore. Ignoring.");
    }
    if (excludeCallsToRuntime) {
      Branchlock.LOGGER.info("Only calls to local members will be encrypted.");
    }
    if (excludeFields) {
      Branchlock.LOGGER.info("Fields will not be encrypted.");
    }
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    CallEncryptionDriver callEncryptionDriver = new CallEncryptionDriver(this, excludeCallsToRuntime, excludeFields);
    DecryptionClassGeneratorDriver decryptionClassGeneratorDriver = new DecryptionClassGeneratorDriver(this, callEncryptionDriver);
    return List.of(callEncryptionDriver, decryptionClassGeneratorDriver);
  }
}
