package net.branchlock.task.naming;

import net.branchlock.Branchlock;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.naming.nameiterator.INameIterator;
import net.branchlock.task.naming.nameiterator.implementation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A factory for creating method / class / field names.
 */
public class NameFactory {
  private final DataProvider dataProvider;
  private INameIterator nameIterator;

  public NameFactory(DataProvider dataProvider) {
    this.dataProvider = dataProvider;
    initializeNameIterator(dataProvider.branchlock.settingsManager);
  }

  private void initializeNameIterator(SettingsManager settingsManager) {
    switch (settingsManager.getNameIterator()) {
      default:
        Branchlock.LOGGER.warning("Unknown name iterator: " + settingsManager.getNameIterator());
      case "alphabetic":
      case "alphanumeric":
        nameIterator = new AlphabeticNameIterator();
        break;
      case "arabic":
        nameIterator = new ArabicNameIterator();
        break;
      case "ls-and-is":
        nameIterator = new LsAndIsNameIterator();
        break;
      case "chinese":
        nameIterator = new ChineseNameIterator();
        break;
      case "non-printable":
        nameIterator = new NonPrintableNameIterator();
        break;
      case "keywords":
        nameIterator = new KeywordNameIterator();
        break;
      case "ideographic":
        nameIterator = new IdeographicNameIterator();
        break;
      case "rtl":
        nameIterator = new RTLAlphabeticNameIterator();
        break;
    }
  }


  /**
   * Returns a new name for the given method.
   * The name is guaranteed to be unique in the given class.
   *
   * @param method The method to get a new name for.
   * @return A new name for the given method.
   */
  public String getUniqueNewMethodName(BMethod method) {
    return getUniqueNewMethodName(method, Collections.emptyMap());
  }

  /**
   * Returns a new name for the given method.
   * The name is guaranteed to be unique in the given class and also in the method's equivalence class.
   *
   * @param method                The method to get a new name for.
   * @param classToNewMethodNames A map of classes with their new method names.
   *                              To ensure uniqueness while generating mappings for multiple methods.
   * @return A new name for the given method.
   */
  public String getUniqueNewMethodName(BMethod method, Map<BClass, Set<String>> classToNewMethodNames) {
    return getUniqueNewMethodName(method.requireEquivalenceClass(), classToNewMethodNames, true);
  }

  public String getUniqueNewMethodName(IEquivalenceClass<BMethod> equivalenceClass, Map<BClass, Set<String>> classToNewMethodNames, boolean checkForShadowing) {
    if(classToNewMethodNames.isEmpty())
      nameIterator.reset();
    Set<BMethod> equalMethods = equivalenceClass.getMembers();

    Outer:
    while (true) {
      String newName = nameIterator.next();
      for (BMethod equalMethod : equalMethods) {
        boolean classAlreadyHasMethod = equalMethod.getOwner().resolveMethod(newName, equalMethod.getDescriptor()) != null;
        if (classAlreadyHasMethod) continue Outer;

        // TODO this implementation is actually not 100% correct, resolveMethod instead of containsSignature should be used. but this takes much longer, and the chance of a collision is really low.

        Predicate<BClass> predicate = bc -> classToNewMethodNames.getOrDefault(bc, Set.of()).contains(newName) || bc.methods.containsSignature(newName, equalMethod.getDescriptor());
        boolean newNameCollision = equalMethod.getOwner().matchesOrParent(predicate);
        if (newNameCollision) continue Outer;
        if(checkForShadowing) {
          boolean newNameCollisionTop = equalMethod.getOwner().matchesOrChild(predicate);
          if (newNameCollisionTop) continue Outer;
        }
      }
      return newName;
    }
  }


  public String getUniqueFieldName(BField bField, Map<BClass, Set<String>> newFieldNamesInClass, boolean checkForShadowing) {
    if (newFieldNamesInClass.isEmpty())
      nameIterator.reset();

    BClass owner = bField.getOwner();
    while (true) {
      String newName = nameIterator.next();
      // TODO same here, resolveField instead of containsSignature should be used. but this takes much longer, and the chance of a collision is really low.
      Predicate<BClass> predicate = bc -> newFieldNamesInClass.getOrDefault(bc, Set.of()).contains(newName) || bc.fields.containsSignature(newName, bField.getDescriptor());
      if (owner.matchesOrParent(predicate)) continue;
      if(checkForShadowing) {
        if (owner.matchesOrChild(predicate)) continue;
      }
      if (owner.resolveField(newName, bField.getDescriptor()) != null) continue;
      return newName;
    }
  }


  /**
   * @return a method name for a virtual method that can be used in the given class without causing a name collision.
   */
  public String getUniqueMethodName(BClass owner, String methodDescriptor) {
    nameIterator.reset();
    while (true) {
      String newName = nameIterator.next();
      if (owner.resolveMethod(newName, methodDescriptor) == null) {
        return newName;
      }
    }
  }

  public String getUniqueFieldName(BClass owner, String fieldDescriptor) {
    nameIterator.reset();
    while (true) {
      String newName = nameIterator.next();
      if (owner.resolveField(newName, fieldDescriptor) == null) {
        return newName;
      }
    }
  }

  public String getUniqueClassNameInPackage() {
    // only reset here because the chance of a valid name is really high if only one class needs a new name.
    nameIterator.reset();
    String packagePrefix = dataProvider.randomClass().getPackage();
    String newName;
    do {
      newName = packagePrefix + nameIterator.next();
    } while (dataProvider.getClasses().containsKey(newName));
    return newName;
  }

  public String getUniqueClassName(Set<String> newClassNames, boolean nullByteTrick, boolean createPackages) {
    if(createPackages) nameIterator.reset();

    int prefixDepth = 0;
    StringBuilder prefix = new StringBuilder();


    String newName;
    do {
      String next = nameIterator.next();
      newName = prefix + (nullByteTrick ? "\0" : "") + next;

      if (createPackages && Branchlock.R.nextFloat() < Math.pow(0.5, prefixDepth + 1)) {
        // if the name is colliding, to create more confusion, move the new class into a package that has the same name as an already existing class.
        prefix.append(next).append("/");
        prefixDepth++;
      }
    } while (newClassNames.contains(newName) || dataProvider.getClasses().containsKey(newName));
    return newName;
  }

  public String getNameIteratorNext() {
    return nameIterator.next();
  }

  public void resetNameIterator() {
    nameIterator.reset();
  }
}
