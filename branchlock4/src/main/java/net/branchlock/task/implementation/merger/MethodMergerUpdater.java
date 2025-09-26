package net.branchlock.task.implementation.merger;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.commons.java.Pair;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.MethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodMergerUpdater implements MethodDriver, Opcodes {
  private final MethodMerger methodMerger;
  private final MergeMethodContainerCreator containerCreator;
  private BClass container;
  private Map<BMethod, Pair<BMethod, Integer>> methodMap;

  public MethodMergerUpdater(MethodMerger methodMerger, MergeMethodContainerCreator containerCreator) {
    this.methodMerger = methodMerger;
    this.containerCreator = containerCreator;
  }

  private static void prepareForCaller(BMethod bm, AbstractInsnNode min, String desc, int callerIdx) {
    if (bm.getSalt() != null) {
        bm.instructions.insertBefore(min, bm.getSalt().loadEncryptedInt(callerIdx));
    } else {
        bm.instructions.insertBefore(min, Instructions.intPush(callerIdx));
    }

    Type returnType = Type.getReturnType(desc);
    if (returnType.getSort() == Type.OBJECT || returnType.getSort() == Type.ARRAY) {
      if (!returnType.equals(Type.getType(Object.class))) {
          bm.instructions.insert(min, new TypeInsnNode(CHECKCAST, returnType.getInternalName()));
      }
    }
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    stream.forEach(bm -> {
      if (methodMap.containsKey(bm)) return; // these will get removed later
        for (AbstractInsnNode ain : bm.instructions) {
        if (ain.getOpcode() == INVOKESTATIC) {
          MethodInsnNode min = (MethodInsnNode) ain;
          BClass bClass = methodMerger.dataProvider.getClasses().get(min.owner);
          if (bClass == null) continue;
          BMethod bMethod = bClass.resolveMethod(min.name, min.desc);
          if (bMethod == null || !bMethod.hasAccess(ACC_STATIC)) return;
          if (!methodMap.containsKey(bMethod)) continue;
          Pair<BMethod, Integer> pair = methodMap.get(bMethod);

          prepareForCaller(bm, min, min.desc, pair.b);
            bm.instructions.set(min, new MethodInsnNode(INVOKESTATIC, container.getName(), pair.a.getName(), pair.a.getDescriptor(), false));
        }
        // all invokedynamic methods should be excluded.
      }
    });
    return true;
  }

  @Override
  public void preDrive() {
    Branchlock.LOGGER.info("Most static methods will lose their SourceFile attribute.");
    container = methodMerger.dataUtilities.createNewNoSideEffectClass();
    container.setOriginalName("net/branchlock/generated/MethodMergerContainerClass");
    methodMap = new HashMap<>(); // new methods with index
    containerCreator.containers.values().forEach(mergeMethodContainer -> mergeMethodContainer.createMethods(methodMerger, methodMap, container));
    methodMerger.dataProvider.addClass(container);
  }

  @Override
  public void postDrive() {
    containerCreator.containers.values().forEach(MergeMethodContainer::removeMethodsFromInput);
    Set<BMethod> toRemap = methodMap.values().stream().map(p -> p.a).collect(Collectors.toSet());
    methodMerger.nameTransformer.transformMethodsOnly(toRemap);
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return List.of();
  }

  @Override
  public String identifier() {
    return "method-merger-updater";
  }
}
