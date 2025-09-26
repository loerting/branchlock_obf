package net.branchlock.task.driver.implementations;

import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.driver.streamsupply.IDriverStreamSupplier;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * A driver where only the DataProvider is supplied once.
 * Used for tasks that do not iterate over a collection of members.
 */
public interface IndividualDriver extends IDriver<Void> {

  @Override
  default Collection<IPassThrough<Void>> passThroughs() {
    return List.of();
  }

  @Override
  default IDriverStreamSupplier<Void> streamSupplier() {
    return t -> Stream.of((Void) null);
  }
}
