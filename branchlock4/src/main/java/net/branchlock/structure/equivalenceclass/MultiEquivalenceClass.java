package net.branchlock.structure.equivalenceclass;

import net.branchlock.structure.BMethod;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that represents a method equivalence class.
 * Methods that override or are overridden by each other are in the same equivalence class.
 * To prevent performance issues, the equivalence class will not automatically remove methods that are destroyed, renamed, removed etc.
 * <p>
 * Note that only local equivalence classes require to be complete at all times.
 */
public class MultiEquivalenceClass implements IEquivalenceClass<BMethod> {
  /**
   * The set of methods in this equivalence class.
   */
  private final Set<BMethod> methods;

  private EquivalenceClassState state = EquivalenceClassState.VALID;


  /**
   * Creates a new method equivalence class.
   */
  public MultiEquivalenceClass() {
    this.methods = ConcurrentHashMap.newKeySet();
  }

  /**
   * Adds a method to this equivalence class.
   *
   * @param method The method to add.
   */
  @Override
  public void addMember(BMethod method) {
    if (methods.contains(method))
      return;
    ensureValidInstance();
    if (!methods.isEmpty() && !getIdentifier().equals(method.getIdentifier())) {
      throw new IllegalArgumentException("Cannot add a method that is not equivalent to the other methods in this equivalence class: got "
        + method.getIdentifier() + ", expected " + getIdentifier());
    }
    this.methods.add(method);
  }

  /**
   * Get the set of methods in this equivalence class.
   * Note that only local equivalence classes require to be complete at all times.
   */
  @Override
  public Set<BMethod> getMembers() {
    ensureValidInstance();
    return Collections.unmodifiableSet(methods);
  }

  private void ensureValidInstance() {
    String text = methods.isEmpty() ? "<empty>" : methods.iterator().next().toString();
    if (state == EquivalenceClassState.MERGED)
      throw new IllegalStateException("method equivalence class was merged into another one. This instance of " + text + " is not valid anymore.");
    if (state == EquivalenceClassState.INVALIDATED)
      throw new IllegalStateException("method equivalence class was invalidated. This instance of " + text + " is not valid anymore.");
    if (state != EquivalenceClassState.VALID)
      throw new IllegalStateException("method equivalence class is in an unknown state: " + state);
  }

  @Override
  public String getIdentifier() {
    BMethod next = getAnyMember();
    if (next == null)
      return null;
    return next.getIdentifier();
  }

  @Override
  public BMethod getAnyMember() {
    ensureValidInstance();
    if (methods.isEmpty())
      return null;
    return methods.iterator().next();
  }

  /**
   * Merge this equivalence class with another.
   *
   * @param other The other equivalence class. It cannot be used anymore after this call and all of its instances have to be replaced.
   */
  @Override
  public void merge(IEquivalenceClass<BMethod> other) {
    ensureValidInstance();
    if (!(other instanceof MultiEquivalenceClass))
      throw new IllegalArgumentException("Cannot merge a PolyEquivalenceClass with a " + other.getClass().getSimpleName() + ".");
    MultiEquivalenceClass otherClass = (MultiEquivalenceClass) other;
    if (!otherClass.isValidInstance())
      throw new IllegalArgumentException("Cannot merge an equivalence class that is not valid anymore. ");
    if (otherClass == this)
      throw new IllegalArgumentException("Cannot merge an equivalence class with itself.");
    String ownIdentifier = getIdentifier();
    if (!methods.isEmpty() && !otherClass.methods.isEmpty() && !ownIdentifier.equals(otherClass.getIdentifier()))
      throw new IllegalArgumentException("Cannot merge two equivalence classes that are not equivalent.");
    methods.addAll(otherClass.methods);
    otherClass.state = EquivalenceClassState.MERGED;
  }


  @Override
  public void removeMember(BMethod bm) {
    ensureValidInstance();
    methods.remove(bm);
  }

  @Override
  public boolean isValidInstance() {
    return state == EquivalenceClassState.VALID;
  }

  @Override
  public String toString() {
    return "VEC{" + getIdentifier() + ", " + methods.size() + " methods, " + state + "}";
  }

  @Override
  public boolean isLocal() {
    return methods.stream().allMatch(BMethod::isLocal);
  }

  @Override
  public boolean isSignatureChangeable() {
    return methods.stream().allMatch(BMethod::isSignatureChangeable);
  }

  @Override
  public boolean isNameChangeable() {
    return methods.stream().allMatch(BMethod::isNameChangeable);
  }

  public void invalidate() {
    state = EquivalenceClassState.INVALIDATED;
  }

  @Override
  public EquivalenceClassState getState() {
    return state;
  }

  /*
   * DO NOT IMPLEMENT hashCode() and equals()!
   * Equivalence classes are used as keys in a HashMap.
   */
}
