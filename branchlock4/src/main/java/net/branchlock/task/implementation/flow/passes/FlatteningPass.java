package net.branchlock.task.implementation.flow.passes;

import net.branchlock.commons.asm.Instructions;
import net.branchlock.commons.asm.interpreter.TypeInterpreter;
import net.branchlock.commons.java.Pair;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.task.Task;
import net.branchlock.task.implementation.flow.FlowPassDriver;
import net.branchlock.task.implementation.flow.frame.FrameLocals;
import net.branchlock.task.implementation.salting.MethodSalt;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Control flow flattening.
 * <p>
 * Requirements: More than 5 jumps in a method. The method shouldn't be performance dependent.
 * <p>
 * The goal: Disrupt control flow graphs.
 * <p>
 * How it works:
 * Introduce a jump index variable.
 * Introduce the "switcher" part. It will be responsible for performing all outgoing jumps.
 * There will be a TABLESWITCH instruction that reads the jump index value that selects the label for the GOTO instruction.
 * All goto instructions will be replaced with a jump to the switcher part. Before the jump, the jump index variable will be set to the index of the label.
 * Conditional jumps can also be replaced with the switcher part. Just replace the destination label with the switcher part.
 * <p>
 * Be careful: Jumps that have something on the stack cannot be replaced.
 * <p>
 * Problem: The switcher will not work if for different labels there are different frame locals.
 * Solution: Create multiple switchers for different frames.
 * <p>
 */
public class FlatteningPass extends FlowPassDriver {

  private static final int MIN_JUMPS_PER_SIGNATURE = 4;
  private final BClass object = task.dataProvider.resolveRuntimeBClass("java/lang/Object");

  public FlatteningPass(Task task, float coveragePct) {
    super(task, coveragePct);
  }

  private static LabelNode addSwitcher(BMethod m, int jumpIndexVarIndex, List<LabelNode> labels) {
    LabelNode switcherLabel = new LabelNode();
    InsnList switcher = new InsnList();
    switcher.add(switcherLabel);
    switcher.add(new VarInsnNode(ILOAD, jumpIndexVarIndex));
    switcher.add(new TableSwitchInsnNode(0, labels.size() - 1, labels.get(R.nextInt(labels.size())), labels.toArray(new LabelNode[0])));
    m.getCode().getInstructions().add(switcher);

    return switcherLabel;
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    stream.forEach(bm -> {
      Map<FrameLocals, List<JumpInsnNode>> jumpsByLocals = scanForReplaceableJumps(bm);
      if (jumpsByLocals == null || jumpsByLocals.isEmpty()) {
        // no jumps to replace
        return;
      }

      int jumpIndexVarIndex = introduceJumpIndexVariable(bm);

      // This is unnecessary. We do not need to initialize the jump index variable.
      // addJumpIndexSetBefore(m, null, jumpIndexVarIndex, -1, fixedDescVal);

      // for each different frame locals, create a switcher
      for (Map.Entry<FrameLocals, List<JumpInsnNode>> jumpsByLocal : jumpsByLocals.entrySet()) {
        FrameLocals locals = jumpsByLocal.getKey();
        List<JumpInsnNode> jumps = jumpsByLocal.getValue();
        if (jumps.size() < MIN_JUMPS_PER_SIGNATURE) {
          // not enough jumps to replace
          continue;
        }
        List<LabelNode> labels = jumps.stream().map(j -> j.label).distinct().collect(Collectors.toList());

        LabelNode switcher = addSwitcher(bm, jumpIndexVarIndex, labels);
        // replace jumps with switcher
        for (JumpInsnNode jump : jumps) {
          addJumpIndexSetBefore(bm, jump, jumpIndexVarIndex, labels.indexOf(jump.label));
          jump.label = switcher;
        }
      }
    });
    return true;
  }

  private Map<FrameLocals, List<JumpInsnNode>> scanForReplaceableJumps(BMethod bm) {
    List<Pair<AbstractInsnNode, Frame<BasicValue>>> frames = bm.getInstructionsWithFrames(new TypeInterpreter(task.dataProvider, bm));
    if (frames == null)
      return null;

    Map<FrameLocals, List<JumpInsnNode>> labels = new HashMap<>();
    // add each JumpInsnNode where stack is empty afterwards
    for (Pair<AbstractInsnNode, Frame<BasicValue>> pair : frames) {
      AbstractInsnNode insn = pair.a;
      Frame<BasicValue> frame = pair.b;
      if (insn instanceof JumpInsnNode) {
        if (frame == null || frame.getStackSize() != 0)
          continue;

        FrameLocals destinationLocals = getFrameLocals(frame);

        if (destinationLocals.containsTop()) {
          // I sadly can't pull this off, as the analyzer is not accurate enough. I do not know why.
          // Instead of top values (UNINITIALIZED), there should be an initialized value, but it seems like it cannot find the common parent sometimes.
          // TODO try to fix this, but it's really hard
          continue;
        }
        int opcode = insn.getOpcode();
        if (opcode == GOTO || (opcode >= IFEQ && opcode <= IFLE) || (opcode >= IF_ICMPEQ && opcode <= IF_ACMPNE)) {
          labels.computeIfAbsent(destinationLocals, s -> new ArrayList<>()).add((JumpInsnNode) insn);
        }
      }
    }
    return labels;
  }

  /**
   * Introduce a jump index variable. This requires m.maxLocals to be correctly set.
   *
   * @return
   */
  private int introduceJumpIndexVariable(BMethod bm) {
    MethodNode m = bm;
    int varIdx = m.maxLocals;
    m.maxLocals++;
    return varIdx;
  }

  private void addJumpIndexSetBefore(BMethod bm, AbstractInsnNode ain, int jumpIndexVarIndex, int value) {
    MethodSalt salt = bm.getSalt();
    InsnList il = new InsnList();
    if (salt != null && R.nextFloat() < 0.1f) {
      il.add(salt.loadEncryptedInt(value));
    } else {
      il.add(Instructions.intPush(value));
    }
    il.add(new VarInsnNode(ISTORE, jumpIndexVarIndex));

    InsnList instructions = bm.getCode().getInstructions();
    if (ain == null) {
      instructions.insert(il);
    } else {
      instructions.insertBefore(ain, il);
    }
  }

  private FrameLocals getFrameLocals(Frame<BasicValue> frame) {
    int localCount = frame.getLocals();
    BasicValue[] local = new BasicValue[localCount];
    for (int i = 0; i < localCount; i++) {
      local[i] = frame.getLocal(i);
    }
    return new FrameLocals(local);
  }

  @Override
  public String identifier() {
    return "flow-flattening";
  }

  @Override
  protected boolean isFitting(BMethod t) {
    boolean objectMethod = t.hasMultiEquivalenceClass() && t.requireEquivalenceClass().getMembers().stream().anyMatch(bm -> bm.getOwner() == object);
    return !objectMethod && t.getCode().countType(AbstractInsnNode.JUMP_INSN) > MIN_JUMPS_PER_SIGNATURE;
  }
}
