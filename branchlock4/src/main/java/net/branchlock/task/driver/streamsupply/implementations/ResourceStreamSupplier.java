package net.branchlock.task.driver.streamsupply.implementations;

import net.branchlock.structure.BResource;
import net.branchlock.structure.provider.IDataStreamProvider;
import net.branchlock.task.driver.streamsupply.IDriverStreamSupplier;

import java.util.stream.Stream;

public class ResourceStreamSupplier implements IDriverStreamSupplier<BResource> {
  @Override
  public Stream<BResource> fromDataStreamProvider(IDataStreamProvider dataStreamProvider) {
    return dataStreamProvider.streamResources();
  }
}
