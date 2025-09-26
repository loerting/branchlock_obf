package net.branchlock.task.driver.streamsupply.implementations;

import net.branchlock.structure.BMethod;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.structure.provider.IDataStreamProvider;
import net.branchlock.task.driver.streamsupply.IDriverStreamSupplier;

import java.util.stream.Stream;

/**
 * Supplies a stream of {@link IEquivalenceClass}es of {@link BMethod}s.
 * <p>
 * Use this class with caution. The equivalence classes are not guaranteed to be valid and may change at any time.
 */
public class MethodEquivalenceClassStreamSupplier implements IDriverStreamSupplier<IEquivalenceClass<BMethod>> {
  @Override
  public Stream<IEquivalenceClass<BMethod>> fromDataStreamProvider(IDataStreamProvider dataStreamProvider) {
    return dataStreamProvider.streamInputMethodEquivalenceClasses();
  }
}
