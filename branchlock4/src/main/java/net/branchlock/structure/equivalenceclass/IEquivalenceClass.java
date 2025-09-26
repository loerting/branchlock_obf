package net.branchlock.structure.equivalenceclass;

import java.util.Set;

public interface IEquivalenceClass<T> {
  void addMember(T method);

  Set<T> getMembers();

  String getIdentifier();

  T getAnyMember();

  void merge(IEquivalenceClass<T> other);

  void removeMember(T bm);

  boolean isValidInstance();

  boolean isLocal();

  boolean isSignatureChangeable();

  void invalidate();

  EquivalenceClassState getState();

  boolean isNameChangeable();
}
