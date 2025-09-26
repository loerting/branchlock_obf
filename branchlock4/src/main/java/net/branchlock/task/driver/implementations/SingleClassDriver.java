package net.branchlock.task.driver.implementations;

import net.branchlock.structure.BClass;
import net.branchlock.task.driver.streamsupply.implementations.ClassStreamSupplier;

public interface SingleClassDriver extends SingleDriver<BClass> {
  default ClassStreamSupplier streamSupplier() {
    return new ClassStreamSupplier();
  }
}
