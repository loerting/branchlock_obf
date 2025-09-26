package net.branchlock.task.driver.implementations;

import net.branchlock.structure.BResource;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.streamsupply.implementations.ResourceStreamSupplier;

public interface ResourceDriver extends IDriver<BResource> {
  default ResourceStreamSupplier streamSupplier() {
    return new ResourceStreamSupplier();
  }
}
