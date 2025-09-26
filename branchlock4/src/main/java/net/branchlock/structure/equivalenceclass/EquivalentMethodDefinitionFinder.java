package net.branchlock.structure.equivalenceclass;

import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import org.objectweb.asm.Opcodes;

import java.util.*;

import static net.branchlock.structure.equivalenceclass.EquivalentMethodDefinitionFinder.ScanState.*;

public class EquivalentMethodDefinitionFinder implements Opcodes {

  private final BMethod method;

  private final Set<BMethod> equivalentMethods = new HashSet<>();

  public EquivalentMethodDefinitionFinder(BMethod method) {
    this.method = Objects.requireNonNull(method);
    if (method.getOwner().invalidated)
      throw new IllegalArgumentException("Cannot find equivalent methods for invalidated method: " + method);
  }

  /**
   * JVM §5.4.5 Method overriding
   */
  private static boolean canOverride(BMethod equiMethod) {
    return !equiMethod.hasAccess(ACC_STATIC) && !equiMethod.hasAccess(ACC_PRIVATE);
  }

  public void findEquivalentDefinitions() {
    if (!equivalentMethods.isEmpty())
      throw new IllegalStateException("Already found equivalent methods for: " + method);
    if (!canOverride(method)) {
      equivalentMethods.add(method);
      return;
    }
    if (findEquivalentDefinitions(method.getOwner(), new HashMap<>()) != SEEN)
      throw new IllegalStateException("Method not found in its own class: " + method);
  }

  /**
   * Find all the containers a method is contained in. This only considers overridden methods.
   */
  public ScanState findEquivalentDefinitions(BClass current, Map<BClass, ScanState> seen) {
    if (seen.containsKey(current)) // already run through
      return seen.get(current); // return cached result

    seen.put(current, UNKNOWN); // for now mark it as visited, but with unknown result

    boolean methodOverriddenCurrent = false;

    // covariant return types (which lead to a different descriptor) are implemented using bridge methods.
    // we can check for equivalence by checking for same name and same desc.
    BMethod equiMethod = current.methods.get(method.getName(), method.getDescriptor());
    // WARNING: an abstract method can override another abstract method, do not cut

    if (equiMethod != null) {
      // Note: we cannot shortcut subclass equivalence scan because a method is final, because of this case:
      // itf I { void m(); }
      // class A { final void m() {} } <--- equivalence scan for this class alone would be wrong
      // class B extends A implements I {}
      if (canOverride(equiMethod)) {
        equivalentMethods.add(equiMethod);
        methodOverriddenCurrent = true;
      }
      // do not stop if canOverride is false, it causes problems.
    }

    boolean methodFoundLower = false;

    // do not cut bottom search if link.local, we need to go deeper to ensure everything is linked with containers
    // you will get rare AbstractMethodErrors!
    for (BClass bottom : current.getDirectParentClasses()) {
      ScanState result = this.findEquivalentDefinitions(bottom, seen);
      if (result == SEEN || result == UNKNOWN) {
        methodFoundLower = true;
        // do not break here, we need to check all bottoms
      }
    }

    boolean methodSeenAboveBelow = methodOverriddenCurrent || methodFoundLower; // mark as seen if we found the method in a bottom class
    seen.put(current, methodSeenAboveBelow ? SEEN : NEVER_SEEN);

    // if the method is not found in any bottom class and also not in this class, we do not need to go higher from here
    // because it is not overriding anything
    if (methodSeenAboveBelow) {
      for (BClass top : current.directSubClasses) {
        this.findEquivalentDefinitions(top, seen);
      }
    }

    return methodSeenAboveBelow ? SEEN : NEVER_SEEN;
  }

  public Set<BMethod> getResults() {
    return equivalentMethods;
  }

  public enum ScanState {
    UNKNOWN,
    SEEN,
    NEVER_SEEN
  }
}
