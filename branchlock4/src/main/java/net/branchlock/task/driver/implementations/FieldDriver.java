package net.branchlock.task.driver.implementations;

import net.branchlock.structure.BField;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.streamsupply.implementations.FieldStreamSupplier;

public interface FieldDriver extends IDriver<BField> {
  default FieldStreamSupplier streamSupplier() {
    return new FieldStreamSupplier();
  }
}
