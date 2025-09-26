package net.branchlock.task.implementation.merger;

import net.branchlock.Branchlock;
import net.branchlock.commons.java.Pair;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMethod;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MergeMethodBucket implements Opcodes {
  public final List<BMethod> containedMethods = new ArrayList<>();
  public BMethod caller = null;

  public boolean isFull() {
    // if the method gets too big, the JVM will complain.
    // also use up only a small bit of the max size as we want to perform other tasks on the class as well
    return getSize() >= Short.MAX_VALUE * 0.35f;
  }

  public int getSize() {
    return containedMethods.stream().mapToInt(BMethod::getInstructionCount).sum();
  }


  public void createMethod(MethodMerger methodMerger, Map<BMethod, Pair<BMethod, Integer>> methodMap, Type genSigPlusId, BClass container) {
    if (caller != null) throw new IllegalStateException("Method already created");
    Collections.shuffle(containedMethods, Branchlock.R);
    BMethod mn = new BMethod(container, ACC_PUBLIC | ACC_STATIC, "bucket$" + container.methods.size(), genSigPlusId.getDescriptor(), null, null);
    InsnList il = new InsnList();

    int lastVarIdx = 0;
    Type[] argumentTypes = genSigPlusId.getArgumentTypes();
    for (int i = 0; i < argumentTypes.length - 1; i++) {
      lastVarIdx += argumentTypes[i].getSize();
    }

    mn.maxLocals = lastVarIdx + 1;

    il.add(new VarInsnNode(ILOAD, lastVarIdx));

    List<LabelNode> labels = new ArrayList<>();
    for (int i = 0; i < containedMethods.size(); i++) {
      labels.add(new LabelNode());
    }

    int splitPoint = containedMethods.size() / 2;

    LabelNode defaultLabel = new LabelNode();
    il.add(new TableSwitchInsnNode(0, splitPoint - 1, defaultLabel, labels.stream().limit(splitPoint).toArray(LabelNode[]::new)));

    for (int i = 0; i < containedMethods.size(); i++) {
      BMethod bm = containedMethods.get(i);


      il.add(labels.get(i));
      il.add(generateCasts(Type.getMethodType(bm.getDescriptor()), genSigPlusId));

      BClass owner = bm.getOwner();
      if (!owner.isInterface() && !owner.isAbstract() && owner.hasStaticInitSideEffect()) {
        // simulate class initialization by referencing the class in any way.
        String fieldName = methodMerger.nameFactory.getUniqueFieldName(owner, "I");
        BField bf = new BField(owner, ACC_PUBLIC | ACC_STATIC, fieldName, "I", null, null);
        owner.addField(bf);

        il.add(bf.createGet());
        final int[] nonExecutingJumps = {IFGT, IFLT, IFNE};
        il.add(new JumpInsnNode(nonExecutingJumps[Branchlock.R.nextInt(nonExecutingJumps.length)], labels.get(Branchlock.R.nextInt(labels.size()))));
        // we could also use the NEW instruction and POP, but this is too obvious
      }

        for (AbstractInsnNode ain : bm.instructions) {
        if (ain.getOpcode() == INVOKESPECIAL) {
          MethodInsnNode min = (MethodInsnNode) ain;
          if (!"<init>".equals(min.name)) {
            // The invokespecial instruction is used to invoke instance initialization methods as well as private methods and methods of a superclass of the current class.
            // Replace with an invokevirtual as we change the parent class
            il.add(new MethodInsnNode(INVOKEVIRTUAL, min.owner, min.name, min.desc, min.itf));
            continue;
          }
        } else if (ain.getType() == AbstractInsnNode.VAR_INSN) {
          VarInsnNode vin = (VarInsnNode) ain;
          if (vin.var >= lastVarIdx) {
            // offset other variable indices by one as we added a variable
            il.add(new VarInsnNode(vin.getOpcode(), vin.var + Type.INT_TYPE.getSize()));
            continue;
          }
        } else if (ain.getType() == AbstractInsnNode.IINC_INSN) {
          IincInsnNode iin = (IincInsnNode) ain;
          if (iin.var >= lastVarIdx) {
            // offset other variable indices by one as we added a variable
            il.add(new IincInsnNode(iin.var + Type.INT_TYPE.getSize(), iin.incr));
            continue;
          }
        }
        il.add(ain);
      }

      mn.tryCatchBlocks.addAll(bm.tryCatchBlocks);

      mn.maxLocals = Math.max(mn.maxLocals, bm.maxLocals + Type.INT_TYPE.getSize());
      mn.maxStack = Math.max(mn.maxStack, bm.maxStack);
    }

    il.add(defaultLabel);
    il.add(new VarInsnNode(ILOAD, lastVarIdx));
    int funMerge = Branchlock.R.nextInt(1 + splitPoint / 2); // this makes decompilation much worse
    il.add(new TableSwitchInsnNode(splitPoint - funMerge, containedMethods.size() - 1, labels.get(Branchlock.R.nextInt(splitPoint)), labels.stream().skip(splitPoint - funMerge).toArray(LabelNode[]::new)));

    mn.instructions = il;

    container.addMethod(mn);
    caller = mn;

    for (int i = 0; i < containedMethods.size(); i++) {
      BMethod bm = containedMethods.get(i);
      methodMap.put(bm, new Pair<>(mn, i));
    }
  }

  /**
   * As the general desc uses java/lang/Object instead of the specific type, we need to cast the variables to the correct type.
   */
  private InsnList generateCasts(Type descriptor, Type genSigPlusId) {
    Type[] argumentTypes = descriptor.getArgumentTypes();
    Type[] genSigPlusIdTypes = genSigPlusId.getArgumentTypes();
    InsnList il = new InsnList();
    int varIndex = 0;
    for (int i = 0; i < argumentTypes.length; i++) {
      Type argType = argumentTypes[i];
      Type genSigPlusIdType = genSigPlusIdTypes[i];
      if (!argType.equals(genSigPlusIdType)) {
        if (argType.getSort() != Type.OBJECT && argType.getSort() != Type.ARRAY) throw new IllegalStateException("Cannot cast primitive type " + argType + " to " + genSigPlusIdType);
        if (!argType.equals(Type.getType(Object.class))) { // we don't need to cast to Object
          il.add(new VarInsnNode(ALOAD, varIndex));
          il.add(new TypeInsnNode(CHECKCAST, argType.getInternalName()));
          il.add(new VarInsnNode(ASTORE, varIndex));
        }
      }
      varIndex += argType.getSize();
    }
    return il;
  }
}
