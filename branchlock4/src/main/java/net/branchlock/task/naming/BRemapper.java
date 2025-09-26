package net.branchlock.task.naming;

import net.branchlock.commons.java.MultiMap;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMember;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.structure.provider.DataProvider;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;

import java.util.*;


/**
 * Remaps BMembers to their new names.
 * Note that this renames the whole equivalence class for each method.
 * It also requires all other classes to not be remapped. Divide into two steps: first remap all classes, then update underlying nodes.
 */
public class BRemapper extends Remapper {
  /**
   * Maps members to their new names.
   */
  private Map<BMember, String> oldToNewMap;
  private final DataProvider dp;
  private final NameFactory nameFactory;
  private final List<? extends BClass> classNames;
  private final List<? extends BMember> members;
  private final boolean nullByteTrick;
  private final boolean createPackages;

  public BRemapper(DataProvider dp, NameFactory nameFactory, List<? extends BClass> classNames, List<? extends BMember> members, boolean nullByteTrick, boolean createPackages) {
    this.dp = dp;
    this.nameFactory = nameFactory;
    this.classNames = classNames;
    this.members = members;
    this.nullByteTrick = nullByteTrick;
    this.createPackages = createPackages;
  }

  public void computeOldToNewMap() {
    oldToNewMap = new HashMap<>();
    nameFactory.resetNameIterator();

    // class names
    Set<String> newClassNames = new HashSet<>();
    for (BClass bClass : classNames) {
      String newName = nameFactory.getUniqueClassName(newClassNames, nullByteTrick, createPackages);
      oldToNewMap.put(bClass, newName);
      newClassNames.add(newName);
    }

    // member names
    Map<BClass, Set<String>> newMethodNamesInClass = new HashMap<>();
    Map<BClass, Set<String>> newFieldNamesInClass = new HashMap<>();
    MultiMap<BClass, BMember> ownerToMembers = new MultiMap<>();
    for (BMember member : members) ownerToMembers.put((BClass) member.getOwner(), member);
    Set<BClass> ownersOfMappedMembers = new HashSet<>();
    Set<IEquivalenceClass<BMethod>> mappedMethodEquivalenceClasses = new HashSet<>();
    for (BClass owner : ownerToMembers.keySet()) {
      nameFactory.resetNameIterator(); // here is the best place to reset the name iterator: low chance of collision and high chance of reusing names
      createMappingsInsideOwnerClass(owner, newMethodNamesInClass, newFieldNamesInClass, ownerToMembers, ownersOfMappedMembers, mappedMethodEquivalenceClasses);
    }
  }

  private void createMappingsInsideOwnerClass(BClass owner, Map<BClass, Set<String>> newMethodNamesInClass, Map<BClass, Set<String>> newFieldNamesInClass, MultiMap<BClass, BMember> ownerToMembers, Set<BClass> ownersOfMappedMembers, Set<IEquivalenceClass<BMethod>> mappedMethodEquivalenceClasses) {
    if(ownersOfMappedMembers.contains(owner)) {
      return;
    }
    ownersOfMappedMembers.add(owner);

    // perform recursively: parent class members should be prioritized, so we can disable shadowing check for more performance
    for (BClass directParentClass : owner.getDirectParentClasses()) {
      createMappingsInsideOwnerClass(directParentClass, newMethodNamesInClass, newFieldNamesInClass, ownerToMembers, ownersOfMappedMembers, mappedMethodEquivalenceClasses);
    }

    // create mappings for members inside owner
    Collection<BMember> bMembers = ownerToMembers.get(owner);
    if (bMembers == null) return;
    for (BMember member : bMembers) {
      if (member instanceof BMethod) {
        BMethod bMethod = (BMethod) member;
        IEquivalenceClass<BMethod> equivalenceClass = bMethod.requireEquivalenceClass();
        if (mappedMethodEquivalenceClasses.contains(equivalenceClass)) continue;
        mappedMethodEquivalenceClasses.add(equivalenceClass);

        String newName = nameFactory.getUniqueNewMethodName(equivalenceClass, newMethodNamesInClass, false); // shadowing check disabled
        equivalenceClass.getMembers().forEach(bm -> {
          oldToNewMap.put(bm, newName);
          newMethodNamesInClass.computeIfAbsent(bm.getOwner(), k -> new HashSet<>()).add(newName);
        });
      } else if (member instanceof BField) {
        BField bField = (BField) member;
        String newName = nameFactory.getUniqueFieldName(bField, newFieldNamesInClass, false); // shadowing check disabled
        oldToNewMap.put(bField, newName);
        newFieldNamesInClass.computeIfAbsent(bField.getOwner(), k -> new HashSet<>()).add(newName);
      }
    }
  }

  @Override
  public String map(String internalName) {
    BClass bClass = dp.getClassOrLib(internalName);
    return bClass == null ? internalName : oldToNewMap.getOrDefault(bClass, internalName);
  }

  @Override
  public String mapMethodName(String owner, String name, String descriptor) {
    if (name.startsWith("<")) return name;
    BClass classOrLib = dp.getClassOrLib(owner);
    if (classOrLib == null) return name;
    BMethod bMethod = classOrLib.resolveMethod(name, descriptor);
    return bMethod == null ? name : oldToNewMap.getOrDefault(bMethod, name);
  }

  @Override
  public String mapFieldName(String owner, String name, String descriptor) {
    BClass classOrLib = dp.getClassOrLib(owner);
    if (classOrLib == null) return name;
    BField bField = classOrLib.resolveField(name, descriptor);
    return bField == null ? name : oldToNewMap.getOrDefault(bField, name);
  }

  @Override
  public String mapSignature(String signature, boolean typeSignature) {
    try {
      return super.mapSignature(signature, typeSignature);
    } catch (Exception e) {
      // ignore illegal signature, could've been created by Scrambler
      return signature;
    }
  }


  @Override
  public String mapAnnotationAttributeName(String descriptor, String name) {
    String owner = Type.getType(descriptor).getInternalName();
    BClass bClass = dp.getClassOrLib(owner);
    if (bClass == null) return name;
    return bClass.methods.stream()
      .filter(bm -> bm.getName().equals(name))
      .map(oldToNewMap::get)
      .filter(Objects::nonNull)
      .findFirst()
      .orElse(name);
  }
}
