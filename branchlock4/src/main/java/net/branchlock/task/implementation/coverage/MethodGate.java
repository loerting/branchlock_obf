package net.branchlock.task.implementation.coverage;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.commons.asm.Reference;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

public class MethodGate implements Opcodes {
  private static final Random R = Branchlock.R;

  public final String originalDesc;
  public final BClass gateOwner;
  public final BMethod gateMethod;

  public final Map<Reference, Integer> methodIDs = new HashMap<>();

  // TODO make this more generic by merging by generified desc, but still keep size and efficiency

  public MethodGate(String originalDesc, BClass gateOwner, BMethod gateMethod) {
    this.originalDesc = originalDesc;
    this.gateOwner = gateOwner;
    this.gateMethod = gateMethod;
  }

  public int addMethod(Reference min) {
    if (methodIDs.containsKey(min)) {
      return methodIDs.get(min);
    }
    int id;
    do {
      id = R.nextInt(methodIDs.size() * 2 + 4);
    } while (methodIDs.containsValue(id));
    methodIDs.put(min, id);
    return id;
  }


  public void generateSwitchMethod() {
    if (methodIDs.isEmpty())
      throw new IllegalStateException();
    Type[] argTypes = Type.getArgumentTypes(originalDesc);
    InsnList il = new InsnList();
    int nextFreeVarIdx = 0;
    for (Type t : argTypes) {
      il.add(new VarInsnNode(t.getOpcode(ILOAD), nextFreeVarIdx));
      nextFreeVarIdx += t.getSize();
    }
    if (methodIDs.size() == 1) {
      Reference ref = methodIDs.keySet().iterator().next();
      il.add(new MethodInsnNode(INVOKESTATIC, ref.owner, ref.name, ref.desc));
      il.add(new InsnNode(Type.getReturnType(ref.desc).getOpcode(IRETURN)));
    } else if (methodIDs.size() == 2) {
      // generate if-else
      List<Reference> refs = new ArrayList<>(methodIDs.keySet());
      il.add(new VarInsnNode(ILOAD, nextFreeVarIdx)); // load id
      il.add(Instructions.intPush(methodIDs.get(refs.get(0)))); // push id of method 1
      LabelNode second = new LabelNode();
      il.add(new JumpInsnNode(IF_ICMPNE, second)); // if id != id of method 1, jump to second
      il.add(refs.get(0).createStaticInstruction());
      il.add(new InsnNode(Type.getReturnType(refs.get(0).desc).getOpcode(IRETURN)));
      il.add(second);
      il.add(refs.get(1).createStaticInstruction());
      il.add(new InsnNode(Type.getReturnType(refs.get(1).desc).getOpcode(IRETURN)));
    } else {
      il.add(new VarInsnNode(ILOAD, nextFreeVarIdx)); // switch int
      nextFreeVarIdx += Type.INT_TYPE.getSize(); // make sure maxLocals is correct afterwards

      List<Integer> indexes = new ArrayList<>();
      List<LabelNode> labels = new ArrayList<>();

      Set<InsnList> switchCases = new HashSet<>();
      methodIDs.forEach((ref, idx) -> {
        indexes.add(idx);
        LabelNode sCaseStart = new LabelNode();
        labels.add(sCaseStart);

        InsnList sCase = new InsnList();

        sCase.add(sCaseStart);
        sCase.add(new MethodInsnNode(INVOKESTATIC, ref.owner, ref.name, ref.desc));
        sCase.add(new InsnNode(Type.getReturnType(ref.desc).getOpcode(IRETURN)));

        switchCases.add(sCase);
      });

      il.add(Instructions.orderSwitch(new LookupSwitchInsnNode(labels.get(R.nextInt(labels.size())),
        indexes.stream().mapToInt(i -> i).toArray(), labels.toArray(new LabelNode[0]))));

      for (InsnList switchCase : switchCases) {
        il.add(switchCase);
      }
    }
    gateMethod.instructions = il;
    gateMethod.maxLocals = gateMethod.maxStack = nextFreeVarIdx;

    gateOwner.addMethod(gateMethod);
  }
}
