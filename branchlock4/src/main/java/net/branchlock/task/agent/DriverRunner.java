package net.branchlock.task.agent;

import net.branchlock.Branchlock;
import net.branchlock.structure.provider.IDataStreamProvider;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;

import java.util.Collection;
import java.util.stream.Stream;

public class DriverRunner {
  private final IDataStreamProvider dataStreamProvider;

  public DriverRunner(IDataStreamProvider dataStreamProvider) {
    this.dataStreamProvider = dataStreamProvider;
  }

  public <T> boolean runDriver(IDriver<T> driver) {
    Collection<? extends IPassThrough<T>> iExclusionHandlers = driver.passThroughs();
    Stream<T> stream = driver.streamSupplier().fromDataStreamProvider(dataStreamProvider);
    for (IPassThrough<T> iPassThrough : iExclusionHandlers) {
      stream = iPassThrough.passThrough(stream);
    }
    boolean success = false;
    try {
      long ms = System.currentTimeMillis();
      driver.preDrive();
      success = driver.drive(stream);
      driver.postDrive();
      Branchlock.LOGGER.info("Driver \"{}\" completed in {}ms.", driver.identifier(), System.currentTimeMillis() - ms);
    } catch (Exception e) {
      Branchlock.LOGGER.error("Driver \"{}\" threw an exception:", e, driver.identifier());
    }

    if (!success) {
      Branchlock.LOGGER.error("Driver \"{}\" did not complete successfully.", driver.identifier());
      return false;
    }
    return true;
  }
}
