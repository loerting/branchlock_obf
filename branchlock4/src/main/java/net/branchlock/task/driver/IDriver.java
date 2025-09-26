package net.branchlock.task.driver;

import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.driver.streamsupply.IDriverStreamSupplier;

import java.util.Collection;
import java.util.stream.Stream;

public interface IDriver<T> {

  /**
   * All Objects of T are passed to this method, after they have been filtered by the exclusion handlers.
   */
  boolean drive(Stream<T> stream);

  /**
   * This method is called before the driver is run.
   */
  default void preDrive() {
  }

  /**
   * This method is called after the driver is run.
   */
  default void postDrive() {
  }

  /**
   * All Objects of T are first passed through the exclusion handlers. If the object is excluded, it
   * will not be passed to the drive method.
   */
  Collection<IPassThrough<T>> passThroughs();

  /**
   * The stream supplier is used to get the stream of T objects.
   */
  IDriverStreamSupplier<T> streamSupplier();

  /**
   * The name of the driver.
   */
  default String identifier() {
    String simpleName = getClass().getSimpleName().toLowerCase();
    if (simpleName.isBlank()) return "anonymous-ic-driver";

    return simpleName;
  }

}
