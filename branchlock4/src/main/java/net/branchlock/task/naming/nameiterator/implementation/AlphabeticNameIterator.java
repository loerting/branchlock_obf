package net.branchlock.task.naming.nameiterator.implementation;

/**
 * Goes over all lowercase, uppercase letters and numbers.
 */
public final class AlphabeticNameIterator extends AlphabetIterator {
  public AlphabeticNameIterator() {
    super("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
  }
}
