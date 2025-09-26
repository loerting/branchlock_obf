package net.branchlock.task.driver.implementations;

import net.branchlock.structure.BClass;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.streamsupply.implementations.ClasspathStreamSupplier;

public interface ClasspathDriver extends IDriver<BClass> {
  default ClasspathStreamSupplier streamSupplier() {
    return new ClasspathStreamSupplier();
  }
}
