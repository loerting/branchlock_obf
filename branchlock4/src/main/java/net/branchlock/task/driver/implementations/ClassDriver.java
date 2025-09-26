package net.branchlock.task.driver.implementations;

import net.branchlock.structure.BClass;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.streamsupply.implementations.ClassStreamSupplier;

public interface ClassDriver extends IDriver<BClass> {
  default ClassStreamSupplier streamSupplier() {
    return new ClassStreamSupplier();
  }
}
