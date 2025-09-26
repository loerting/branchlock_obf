package net.branchlock.task.driver.streamsupply.implementations;

import net.branchlock.structure.BField;
import net.branchlock.structure.provider.IDataStreamProvider;
import net.branchlock.task.driver.streamsupply.IDriverStreamSupplier;

import java.util.stream.Stream;

public class FieldStreamSupplier implements IDriverStreamSupplier<BField> {
  @Override
  public Stream<BField> fromDataStreamProvider(IDataStreamProvider dataStreamProvider) {
    return dataStreamProvider.streamInputFields();
  }
}
