package net.branchlock.commons.java;

public interface UncheckedFunction<T, U> {
  U apply(T t) throws Exception;
}
