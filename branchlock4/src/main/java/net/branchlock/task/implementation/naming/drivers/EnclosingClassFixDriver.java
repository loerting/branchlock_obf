package net.branchlock.task.implementation.naming.drivers;

import net.branchlock.structure.BClass;
import net.branchlock.task.Task;
import net.branchlock.task.driver.implementations.SingleClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;

import java.util.Collection;

/**
 * The implementation of Class#getSimpleBinaryName() makes use of
 * getName().substring(enclosingClass.getName().length()).
 * If the enclosing class name is longer than the class name, it will result in an exception.
 * We can fix this by removing the enclosing class attribute from the class node.
 */
public class EnclosingClassFixDriver implements SingleClassDriver {
  private final Task task;

  public EnclosingClassFixDriver(Task task) {
    this.task = task;
  }

  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    return task.defaultClassExclusionHandlers();
  }

  @Override
  public String identifier() {
    return "enclosing-class-remover";
  }

  @Override
  public boolean driveEach(BClass bc) {
    bc.outerClass = null;
    return true;
  }
}
