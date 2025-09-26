package net.branchlock.task.driver.streamsupply.implementations;

import net.branchlock.structure.BClass;
import net.branchlock.structure.provider.IDataStreamProvider;
import net.branchlock.task.driver.streamsupply.IDriverStreamSupplier;

import java.util.stream.Stream;

public class ClassStreamSupplier implements IDriverStreamSupplier<BClass> {
  @Override
  public Stream<BClass> fromDataStreamProvider(IDataStreamProvider dataStreamProvider) {
    return dataStreamProvider.streamInputClasses();
  }
}
