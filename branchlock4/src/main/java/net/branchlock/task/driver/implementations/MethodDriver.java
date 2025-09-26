package net.branchlock.task.driver.implementations;

import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.streamsupply.implementations.MethodStreamSupplier;

public interface MethodDriver extends IDriver<BMethod> {
  default MethodStreamSupplier streamSupplier() {
    return new MethodStreamSupplier();
  }
}
