package net.branchlock.task.driver.implementations;

import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.streamsupply.implementations.MethodStreamSupplier;

public interface SingleMethodDriver extends SingleDriver<BMethod> {
  default MethodStreamSupplier streamSupplier() {
    return new MethodStreamSupplier();
  }
}
