package net.branchlock.task.naming.nameiterator;

public interface INameIterator {
  String next();

  default void reset() {
    throw new UnsupportedOperationException();
  }
}
