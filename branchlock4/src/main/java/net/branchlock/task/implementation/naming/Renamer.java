package net.branchlock.task.implementation.naming;

import net.branchlock.Branchlock;
import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.data.ReflectionDetector;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.implementation.naming.drivers.*;
import net.branchlock.task.implementation.references.drivers.IllegalAccessFixDriver;
import net.branchlock.task.implementation.references.drivers.TargetVersionSetDriver;
import net.branchlock.task.metadata.TaskMetadata;

import java.util.LinkedList;
import java.util.List;

@TaskMetadata(name = "Member renamer", priority = TaskMetadata.Level.FIFTH, ids = "rename")
public class Renamer extends Task {

  public boolean disableReflectionDetection = innerConfig.getOrDefaultValue("disable_reflection_detection", false);
  public boolean nullByteTrick = innerConfig.getOrDefaultValue("null_byte_trick", false);
  public boolean createPackages = innerConfig.getOrDefaultValue("create_packages", true);
  public boolean disableResourceUpdate = innerConfig.getOrDefaultValue("disable_resource_update", false);


  public Renamer(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public void preExecute() {
    Branchlock.LOGGER.info("Use a @ForceRename annotation to force a member to be renamed.");
    Branchlock.LOGGER.info("Note that the debug information still contains class names and line numbers.");
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    ReflectionDetector reflectionDetector = new ReflectionDetector(dataProvider);
    RemappableClassCollectorDriver remappableClassCollectorDriver = new RemappableClassCollectorDriver(this, reflectionDetector);
    RemappableMethodCollectorDriver remappableMethodCollectorDriver = new RemappableMethodCollectorDriver(this, reflectionDetector);
    RemappableFieldCollectorDriver remappableFieldCollectorDriver = new RemappableFieldCollectorDriver(this, reflectionDetector);
    List<IDriver<?>> drivers = new LinkedList<>();

    if (!innerConfig.getOrDefaultValue("keep_class_names", false)) {
      drivers.add(remappableClassCollectorDriver);
    }
    if (!innerConfig.getOrDefaultValue("keep_method_names", false)) {
      drivers.add(remappableMethodCollectorDriver);
    }
    if (!innerConfig.getOrDefaultValue("keep_field_names", false)) {
      drivers.add(remappableFieldCollectorDriver);
    }
    if (!innerConfig.getOrDefaultValue("keep_local_var_names", false)) {
      drivers.add(new LocalVariableRenamerDriver(this));
    }

    MemberRemapperDriver memberRemapperDriver = new MemberRemapperDriver(this, remappableClassCollectorDriver, remappableMethodCollectorDriver, remappableFieldCollectorDriver);
    drivers.add(memberRemapperDriver);
    drivers.add(new IllegalAccessFixDriver(this));
    drivers.add(new TargetVersionSetDriver(this));
    drivers.add(new EnclosingClassFixDriver(this));
    if (disableResourceUpdate) {
        Branchlock.LOGGER.info("Resource updating disabled.");
    } else {
      drivers.add(new ResourceUpdaterDriver(this));
    }
    return drivers;
  }
}
