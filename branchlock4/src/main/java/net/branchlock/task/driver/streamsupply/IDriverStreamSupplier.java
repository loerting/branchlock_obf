package net.branchlock.task.driver.streamsupply;

import net.branchlock.structure.provider.IDataStreamProvider;

import java.util.stream.Stream;

public interface IDriverStreamSupplier<T> {
  Stream<T> fromDataStreamProvider(IDataStreamProvider dataStreamProvider);
}
