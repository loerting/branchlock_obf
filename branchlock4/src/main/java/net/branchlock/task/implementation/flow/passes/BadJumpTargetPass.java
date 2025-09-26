package net.branchlock.task.implementation.flow.passes;

import net.branchlock.commons.asm.Instructions;
import net.branchlock.structure.BMethod;
import net.branchlock.task.Task;
import net.branchlock.task.implementation.flow.FlowPassDriver;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.stream.Stream;

public class BadJumpTargetPass extends FlowPassDriver {
  public BadJumpTargetPass(Task task, float coveragePct) {
    super(task, coveragePct);
  }

  public static int invertJumpOpcode(int jumpOp) {
    switch (jumpOp) {
      case IFEQ:
        return IFNE;
      case IFNE:
        return IFEQ;
      case IFLT:
        return IFGE;
      case IFGE:
        return IFLT;
      case IFGT:
        return IFLE;
      case IFLE:
        return IFGT;
      case IFNULL:
        return IFNONNULL;
      case IFNONNULL:
        return IFNULL;
      default:
        throw new IllegalArgumentException("Unimplemented jump inverse for " + jumpOp);
    }
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    stream.forEach(bm -> {
        InsnList insnList = bm.instructions;

      bm.streamInstr().forEach(ain -> {
        int opcode = ain.getOpcode();
        // only int jumps work. (when implementing for objects, change last instruction insert)
        if (!(opcode >= IFEQ && opcode <= IFLE) || R.nextFloat() >= coveragePct * 0.75f)
          return;

        JumpInsnNode jump = (JumpInsnNode) ain;
        LabelNode newTarget = new LabelNode();
        insnList.insert(jump, newTarget);
        insnList.insert(newTarget, new InsnNode(POP));

        insnList.insertBefore(jump, new InsnNode(DUP));
        if (R.nextDouble() < 0.1) {
          insnList.insertBefore(jump, new InsnNode(SWAP));
        }
        insnList.insertBefore(jump, new JumpInsnNode(invertJumpOpcode(opcode), newTarget));
        insnList.insert(jump, Instructions.intPush(R.nextInt(7) - 1)); // findDummyIntPush is unnecessary for runtime here.
      });
    });
    return true;
  }

  @Override
  public String identifier() {
    return "bad-jump-target";
  }

  @Override
  protected boolean isFitting(BMethod t) {
    return true;
  }
}
