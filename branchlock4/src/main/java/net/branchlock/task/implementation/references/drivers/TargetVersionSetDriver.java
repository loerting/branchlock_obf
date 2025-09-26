package net.branchlock.task.implementation.references.drivers;

import net.branchlock.structure.BClass;
import net.branchlock.task.Task;
import net.branchlock.task.driver.implementations.SingleClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;

import java.util.Collection;

public class TargetVersionSetDriver implements SingleClassDriver {
  private final Task task;

  public TargetVersionSetDriver(Task task) {
    this.task = task;
  }

  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    return task.defaultClassExclusionHandlers();
  }

  @Override
  public String identifier() {
    return "target-version-set";
  }

  @Override
  public boolean driveEach(BClass bc) {
    bc.version = Math.max(bc.version, task.settingsManager.getTargetVersion());
    return true;
  }
}
