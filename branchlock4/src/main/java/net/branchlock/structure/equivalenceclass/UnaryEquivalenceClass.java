package net.branchlock.structure.equivalenceclass;

import net.branchlock.structure.BMethod;

import java.util.Set;

public class UnaryEquivalenceClass implements IEquivalenceClass<BMethod> {

  private final BMethod method;
  private EquivalenceClassState state = EquivalenceClassState.VALID;

  public UnaryEquivalenceClass(BMethod method) {
    this.method = method;
  }

  @Override
  public void addMember(BMethod method) {
    throw new UnsupportedOperationException("Cannot add a method to a unary equivalence class.");
  }

  @Override
  public Set<BMethod> getMembers() {
    ensureValidInstance();
    return Set.of(method);
  }

  private void ensureValidInstance() {
    String text = method.toString();
    if (state == EquivalenceClassState.MERGED)
      throw new IllegalStateException("method equivalence class was merged into another one. This instance of " + text + " is not valid anymore.");
    if (state == EquivalenceClassState.INVALIDATED)
      throw new IllegalStateException("method equivalence class was invalidated. This instance of " + text + " is not valid anymore.");
    if (state != EquivalenceClassState.VALID)
      throw new IllegalStateException("method equivalence class is in an unknown state: " + state);
  }

  @Override
  public String getIdentifier() {
    return method.getIdentifier();
  }

  @Override
  public BMethod getAnyMember() {
    ensureValidInstance();
    return method;
  }

  @Override
  public void merge(IEquivalenceClass<BMethod> other) {
    throw new UnsupportedOperationException("Cannot merge a unary equivalence class with another equivalence class.");
  }

  @Override
  public void removeMember(BMethod bm) {
    throw new UnsupportedOperationException("Cannot remove a method from a unary equivalence class.");
  }

  @Override
  public boolean isValidInstance() {
    return state == EquivalenceClassState.VALID;
  }

  @Override
  public boolean isLocal() {
    return method.isLocal();
  }

  @Override
  public boolean isSignatureChangeable() {
    return method.isSignatureChangeable();
  }

  @Override
  public boolean isNameChangeable() {
    return method.isNameChangeable();
  }

  @Override
  public void invalidate() {
    state = EquivalenceClassState.INVALIDATED;
  }

  @Override
  public EquivalenceClassState getState() {
    return state;
  }

  @Override
  public String toString() {
    return "SEC{" + getIdentifier() + ", " + method + ", " + state + "}";
  }

  /*
   * DO NOT IMPLEMENT hashCode() and equals()!
   * Equivalence classes are used as keys in a HashMap.
   */
}
