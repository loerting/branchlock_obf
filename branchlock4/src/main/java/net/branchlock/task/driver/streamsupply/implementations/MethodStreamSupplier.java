package net.branchlock.task.driver.streamsupply.implementations;

import net.branchlock.structure.BMethod;
import net.branchlock.structure.provider.IDataStreamProvider;
import net.branchlock.task.driver.streamsupply.IDriverStreamSupplier;

import java.util.stream.Stream;

public class MethodStreamSupplier implements IDriverStreamSupplier<BMethod> {
  @Override
  public Stream<BMethod> fromDataStreamProvider(IDataStreamProvider dataStreamProvider) {
    return dataStreamProvider.streamInputMethods();
  }
}
