package net.branchlock.task.driver.passthrough;

import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.structure.provider.DataProvider;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Finds all methods that are invoked dynamically by a lambda expression and filters them out.
 */
public class LambdaExclusionPassThrough implements IPassThrough<BMethod> {

  private final Set<BMethod> affectedMethods = new HashSet<>();
  private final DataProvider dataProvider;

  public LambdaExclusionPassThrough(DataProvider dataProvider) {
    this.dataProvider = dataProvider;
    dataProvider.streamInputMethods().forEach(bMethod -> {
        for (AbstractInsnNode ain : bMethod.instructions) {
        if (ain instanceof InvokeDynamicInsnNode) {
          InvokeDynamicInsnNode idyn = (InvokeDynamicInsnNode) ain;
          if (idyn.bsm != null) {
            Handle h = idyn.bsm;
            resolveAndAdd(bMethod, h.getOwner(), h.getName(), h.getDesc());
          }

          for (int i = 0; i < idyn.bsmArgs.length; i++) {
            Object o = idyn.bsmArgs[i];
            if (o instanceof Handle) {
              Handle h = (Handle) o;
              resolveAndAdd(bMethod, h.getOwner(), h.getName(), h.getDesc());
            }
          }
          if (isMetafactory(idyn.bsm) && idyn.bsmArgs.length >= 3 && idyn.bsmArgs[0] instanceof Type &&
            idyn.bsmArgs[2] instanceof Type) {
            String owner = Type.getReturnType(idyn.desc).getInternalName();
            String name = idyn.name;
            String desc = ((Type) idyn.bsmArgs[0]).getDescriptor();

            resolveAndAdd(bMethod, owner, name, desc);
          }
        }
      }
    });
  }

  public static boolean isMetafactory(Handle bsm) {
    if (bsm == null)
      return false;
    return bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory") && bsm.getName().equals("metafactory") &&
      bsm.getDesc()
        .equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;" +
          "Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;" +
          "Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;");
  }

  private void resolveAndAdd(BMethod accessFrom, String owner, String name, String desc) {
    BClass bClass = dataProvider.resolveBClass(owner, accessFrom);
    if (bClass != null) {
      BMethod bMethod = bClass.resolveMethod(name, desc);
      if (bMethod == null) return;
      IEquivalenceClass<BMethod> equivalenceClass = bMethod.requireEquivalenceClass();
      affectedMethods.addAll(equivalenceClass.getMembers());
    }
  }


  @Override
  public Stream<BMethod> passThrough(Stream<BMethod> t) {
    return t.filter(bm -> !affectedMethods.contains(bm));
  }
}
