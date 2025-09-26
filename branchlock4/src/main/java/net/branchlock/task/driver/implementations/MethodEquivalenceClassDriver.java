package net.branchlock.task.driver.implementations;

import net.branchlock.structure.BMethod;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.streamsupply.implementations.MethodEquivalenceClassStreamSupplier;

public interface MethodEquivalenceClassDriver extends IDriver<IEquivalenceClass<BMethod>> {
  default MethodEquivalenceClassStreamSupplier streamSupplier() {
    return new MethodEquivalenceClassStreamSupplier();
  }
}
