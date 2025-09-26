package net.branchlock.task.implementation.salting.drivers;

import net.branchlock.Branchlock;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.task.data.ReflectionDetector;
import net.branchlock.task.driver.implementations.SingleMethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.driver.passthrough.LambdaExclusionPassThrough;
import net.branchlock.task.implementation.salting.MethodSalt;
import net.branchlock.task.implementation.salting.Salting;
import org.objectweb.asm.Type;

import java.util.*;

/**
 * This driver finds out which equivalence class can be remapped to which new signature. It also assigns the salt value to the equivalence classes.
 */
public class SaltingSignatureCollectorDriver implements SingleMethodDriver {
  private static final Random R = Branchlock.R;
  private static final Type[] INT_TYPES = new Type[]{Type.INT_TYPE, Type.SHORT_TYPE, Type.BYTE_TYPE, Type.CHAR_TYPE};

  /**
   * Maps the method to the new descriptor.
   * Note we cannot use equivalence classes as keys as they are changed / merged / invalidated during the process.
   */
  public final Map<BMethod, String> newDescriptors = new HashMap<>();
  private final Set<IEquivalenceClass<BMethod>> visitedEquivalenceClasses = new HashSet<>();
  private final Salting salting;
  private final ReflectionDetector reflectionDetector;

  public SaltingSignatureCollectorDriver(Salting salting) {
    this.salting = salting;
    this.reflectionDetector = new ReflectionDetector(salting.dataProvider);
  }

  private static List<Type[]> getVariations(Type[] arguments) {
    List<Type[]> variations = new ArrayList<>();
    for (Type intType : INT_TYPES) {
      Type[] variation = new Type[arguments.length + 1];
      System.arraycopy(arguments, 0, variation, 0, arguments.length);
      variation[arguments.length] = intType;

      // prefer int types that are the same as the last real argument
      if (arguments.length > 0 && intType == arguments[arguments.length - 1])
        variations.add(0, variation);
      else
        variations.add(variation);
    }
    return variations;
  }

  @Override
  public void postDrive() {
    for (IEquivalenceClass<BMethod> visitedEquivalenceClass : visitedEquivalenceClasses) {
      if (!visitedEquivalenceClass.isValidInstance())
        throw new IllegalStateException("At least one equivalence class has changed during this driver.");
    }
    Branchlock.LOGGER.info("Found {} new signatures.", newDescriptors.size());
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return salting.defaultMemberExclusionHandlersPlus(str -> str.filter(bm -> !reflectionDetector.isAffected(bm)), new LambdaExclusionPassThrough(salting.dataProvider));
  }

  @Override
  public boolean driveEach(BMethod c) {
    IEquivalenceClass<BMethod> equivalenceClass = c.requireEquivalenceClass();

    if (visitedEquivalenceClasses.contains(equivalenceClass)) return true;
    visitedEquivalenceClasses.add(equivalenceClass);

    if (!equivalenceClass.isSignatureChangeable()) return true;
    if (equivalenceClass.getMembers().stream().anyMatch(reflectionDetector::isAffected)) return true;
    findWorkingSignature(equivalenceClass);

    return true;
  }

  private void findWorkingSignature(IEquivalenceClass<BMethod> equivalenceClass) {
    BMethod anyMethod = equivalenceClass.getAnyMember();
    String name = anyMethod.getName();
    Type[] argumentTypes = Type.getArgumentTypes(anyMethod.getDescriptor());

    List<Type[]> variations = getVariations(argumentTypes);
    for (Type[] variation : variations) {
      String newDesc = Type.getMethodDescriptor(Type.getReturnType(anyMethod.getDescriptor()), variation);
      // check if we are not overriding any method by changing the signature.
      if (validVariation(name, newDesc, equivalenceClass)) {
        int saltSlot = SaltingVariableOffsetterDriver.getSaltSlot(newDesc, anyMethod.isStatic());
        MethodSalt salt = new MethodSalt(R.nextInt(Short.MAX_VALUE), saltSlot); // narrowing conversion
        equivalenceClass.getMembers().forEach(bm -> {
          newDescriptors.put(bm, newDesc);
          bm.setSalt(salt);
        });
        return;
      }
    }
  }

  private boolean validVariation(String name, String newDesc, IEquivalenceClass<BMethod> equivalenceClass) {
    return equivalenceClass.getMembers().stream().map(BMethod::getOwner).distinct().allMatch(c -> c.resolveMethod(name, newDesc) == null);
  }

  @Override
  public String identifier() {
    return "new-signature-finder";
  }
}
