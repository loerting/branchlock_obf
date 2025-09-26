package net.branchlock.task.driver.passthrough;

import java.util.stream.Stream;

public interface IPassThrough<T> {

  /**
   * Note that the stream object could be changed.
   */
  Stream<T> passThrough(Stream<T> t);
}
