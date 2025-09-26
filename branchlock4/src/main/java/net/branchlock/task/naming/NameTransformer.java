package net.branchlock.task.naming;

import net.branchlock.Branchlock;
import net.branchlock.inputprovider.BranchlockRunType;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMember;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.provider.DataProvider;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.FieldNode;

import java.util.*;

/**
 * A class that transforms names of methods, fields, and classes.
 * This is intended for smaller purposes only.
 * Note: after transforming any class, all underlying ASM nodes are invalidated.
 */
public class NameTransformer {
  private final NameFactory nameFactory;
  private final DataProvider dataProvider;

  public NameTransformer(NameFactory nameFactory, DataProvider dataProvider) {
    this.nameFactory = nameFactory;
    this.dataProvider = dataProvider;
  }

  /**
   * Gives all methods a new name.
   * It also checks for collisions in their relative classes.
   * This is an expensive operation, call it on all methods to be renamed at once.
   * This does not rename the whole equivalence classes, only the given methods.
   */
  public void transformMethodsOnly(Collection<BMethod> methods) {
    Map<BMethod, String> methodToNewName = new HashMap<>();
    Map<BClass, Set<String>> classToNewMethodNames = new HashMap<>();

    for (BMethod method : methods) {
      if (method.isConstructor() || !method.isNameChangeable())
        throw new IllegalArgumentException("Cannot rename method with unchangeable name: " + method);
      String newName = nameFactory.getUniqueNewMethodName(method, classToNewMethodNames);
      methodToNewName.put(method, newName);
      classToNewMethodNames.computeIfAbsent(method.getOwner(), k -> new HashSet<>()).add(newName);
    }

    Map<String, String> oldNameToNewName = new HashMap<>();
    for (Map.Entry<BMethod, String> entry : methodToNewName.entrySet()) {
      BMethod method = entry.getKey();
      String newName = entry.getValue();
      oldNameToNewName.put(method.getOwner().getName() + "." + method.getName() + method.getDescriptor(), newName);
    }

    remapLocalClasses(new SimpleRemapper(oldNameToNewName));
  }

  /**
   * Transforms the given fields, but only inside the given class. References to the fields from outside classes will not be updated.
   */
  public BClass transformFieldsInsideClass(BClass bClass, BField... fields) {
    if (Arrays.stream(fields).anyMatch(field -> !bClass.fields.contains(field)))
      throw new IllegalArgumentException("All fields must belong to the given class");
    Map<String, String> fieldToNewName = new HashMap<>();
    for (FieldNode field : fields) {
      String newName = nameFactory.getUniqueFieldName(bClass, field.desc);
      fieldToNewName.put(bClass.getName() + "." + field.name, newName);
    }
    BClass copy = new BClass(dataProvider);
    copy.setLocal(true);
    bClass.accept(new BetterClassRemapper(copy, new SimpleRemapper(fieldToNewName)));

    dataProvider.updateClass(bClass, copy);

    return bClass;
  }

  private void remapLocalClasses(Remapper remapper) {
    Map<String, BClass> classes = dataProvider.getClasses();
    Map<BClass, BClass> newNodes = new HashMap<>();
    for (BClass bClass : classes.values()) {
      BClass newNode = new BClass(dataProvider);
      newNode.setLocal(true);
      bClass.accept(new BetterClassRemapper(newNode, remapper));

      newNodes.put(bClass, newNode);
    }

    dataProvider.updateClasses(newNodes);
  }

  public void transformMembers(List<? extends BClass> classNames, List<? extends BMember> members, boolean nullByteTrick, boolean createPackages) {
    BRemapper remapper = new BRemapper(dataProvider, nameFactory, classNames, members, nullByteTrick, createPackages);
    Branchlock.LOGGER.info("Generating new names for members...");
    remapper.computeOldToNewMap();
    Branchlock.LOGGER.info("Remapping classes...");
    remapLocalClasses(remapper);
  }
  public void transformMembers(List<? extends BClass> classNames, List<? extends BMember> members) {
    transformMembers(classNames, members, false, false);
  }
}
