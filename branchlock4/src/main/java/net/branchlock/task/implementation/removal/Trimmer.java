package net.branchlock.task.implementation.removal;

import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.metadata.TaskMetadata;

import java.util.List;

@TaskMetadata(name = "Trimmer", priority = TaskMetadata.Level.FIRST, ids = "trimmer")
public class Trimmer extends Task {
  public final boolean onlyAnnotatedEntryPoints = innerConfig.getOrDefaultValue("entry_points_require_annotation", false);
  public boolean disableReflectionDetection = innerConfig.getOrDefaultValue("disable_reflection_detection", false);
  public boolean keepUnusedClasses = innerConfig.getOrDefaultValue("keep_unused_classes", false);
  public boolean keepUnusedFields = innerConfig.getOrDefaultValue("keep_unused_fields", false);
  public boolean errorReplacement = innerConfig.getOrDefaultValue("error_replacement", false);

  public Trimmer(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    CalledMemberCollector calledMemberCollector = new CalledMemberCollector(this);
    UnusedMemberRemover unusedMemberRemover = new UnusedMemberRemover(this, calledMemberCollector);
    return List.of(
            calledMemberCollector,
      unusedMemberRemover
    );
  }
}
