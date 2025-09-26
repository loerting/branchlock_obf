package net.branchlock.task.driver.implementations;

import net.branchlock.task.driver.IDriver;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public interface SingleDriver<T> extends IDriver<T> {
  @Override
  default boolean drive(Stream<T> stream) {
    AtomicBoolean result = new AtomicBoolean(true);
    stream.forEach(c -> {
      if (!driveEach(c))
        result.set(false);
    });
    return result.get();
  }

  boolean driveEach(T c);
}
