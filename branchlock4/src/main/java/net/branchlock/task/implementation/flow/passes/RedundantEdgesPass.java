package net.branchlock.task.implementation.flow.passes;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Access;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.commons.asm.interpreter.TypeInterpreter;
import net.branchlock.commons.java.Pair;
import net.branchlock.structure.BMethod;
import net.branchlock.task.Task;
import net.branchlock.task.implementation.flow.FlowPassDriver;
import net.branchlock.task.implementation.salting.MethodSalt;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RedundantEdgesPass extends FlowPassDriver {
  private static final List<Integer> EDGE_TYPES = Arrays.asList(AbstractInsnNode.JUMP_INSN, AbstractInsnNode.LOOKUPSWITCH_INSN, AbstractInsnNode.TABLESWITCH_INSN);

  public RedundantEdgesPass(Task task, float coveragePct) {
    super(task, coveragePct);
  }

  @Override
  protected boolean isFitting(BMethod t) {
    return !t.isConstructor() && t.getInstructionCount() > 10;
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    stream.forEach(bm -> {
      MethodSalt fixedDescVal = bm.getSalt();
      TypeInterpreter interpreter = new TypeInterpreter(task.dataProvider, bm);
      List<Pair<AbstractInsnNode, Frame<BasicValue>>> instructions = bm.getInstructionsWithFrames(interpreter);
      if (instructions == null) {
        return;
      }

      boolean isStatic = Access.isStatic(bm.access);

      List<Pair<LabelNode, Frame<BasicValue>>> labels = instructions.stream()
        .filter(p -> p.a instanceof LabelNode)
        .map(p -> new Pair<>((LabelNode) p.a, p.b))
        .collect(Collectors.toList());

      int booleanTypeLoad = findBooleanVarSlot(bm, labels);
      boolean useVarGetter = booleanTypeLoad < 0 && fixedDescVal == null;
      int minDelay = 0;

      Set<LabelNode> usedTargetsForFakes = new HashSet<>();
      for (Pair<AbstractInsnNode, Frame<BasicValue>> pair : instructions) {
        if (minDelay > 0) {
          // make sure the ifs are not placed too close to each other
          minDelay--;
          continue;
        }

        AbstractInsnNode edgeNode = pair.a;
        Frame<BasicValue> frame = pair.b;
        if (frame == null || (!EDGE_TYPES.contains(edgeNode.getType()) && !Instructions.isCodeEnd(edgeNode) && frame.getStackSize() > 0))
          continue;

       LabelNode jumpableLabel = labels.stream()
          .skip(Branchlock.R.nextInt(labels.size() + 1))
          .filter(nodeFramePair -> !usedTargetsForFakes.contains(nodeFramePair.a) && isAwayFrom(bm, nodeFramePair.a, edgeNode) && interpreter.canJumpFrom(frame, nodeFramePair.b))
          .map(e -> e.a)
          .findFirst().orElse(null);

        if (jumpableLabel != null) {
          InsnList fakeEdge = new InsnList();
          if (useVarGetter) {
            // no boolean to abuse found, use a fake getter
            fakeEdge.add(new VarInsnNode(ALOAD, bm.maxLocals));
            // check code below, why there is this switch
            fakeEdge.add(new JumpInsnNode(isStatic ? IFNONNULL : IFNULL, jumpableLabel));
          } else if (fixedDescVal != null) {
            fakeEdge.add(fixedDescVal.makeLoad());
            fakeEdge.add(new JumpInsnNode(fixedDescVal.getValue() >= 0 ? IFLT : IFGT, jumpableLabel));
          } else {
            fakeEdge.add(new VarInsnNode(ILOAD, booleanTypeLoad));
            fakeEdge.add(new JumpInsnNode(IFLT, jumpableLabel));
          }
            bm.instructions.insertBefore(edgeNode, fakeEdge);

          usedTargetsForFakes.add(jumpableLabel);
          minDelay = 3;

          if (usedTargetsForFakes.size() > 3 + 5 * coveragePct)
            break; // too many jumps already, break out
        }
      }

      // initialize the var getter
      if (useVarGetter && !usedTargetsForFakes.isEmpty()) {
        // make sure the local is set at start
        InsnList start = new InsnList();
        if (isStatic)
          start.add(new InsnNode(ACONST_NULL));
        else
          start.add(new VarInsnNode(ALOAD, 0)); // load this

        start.add(new VarInsnNode(ASTORE, bm.maxLocals));
          bm.instructions.insert(start);

        // a new slot was used, increase maxLocals
        bm.maxLocals += 1; // size of null object == 1
      }

    });
    return true;
  }

  private int findBooleanVarSlot(BMethod bm, List<Pair<LabelNode, Frame<BasicValue>>> frames) {
    int varSlot = Access.isStatic(bm.access) ? 0 : 1;
    Type[] argumentTypes = bm.getArgs();
    for (Type argumentType : argumentTypes) {
      if (argumentType == Type.BOOLEAN_TYPE) {
        int finalVarSlot = varSlot;
        if (frames.stream().allMatch(f -> f.b == null || f.b.getLocal(finalVarSlot).getType() == Type.INT_TYPE)) // boolean == int in frames
          return varSlot; // slot type never changes, no var ranges
      }
      varSlot += argumentType.getSize();
    }
    return -1;
  }

  private boolean isAwayFrom(BMethod m, LabelNode key, AbstractInsnNode edgeNode) {
      return key != edgeNode && edgeNode.getNext() != key && edgeNode.getPrevious() != key && Math.abs(m.instructions.indexOf(edgeNode) - m.instructions.indexOf(key)) > m.getInstructionCount() / 10;
  }

  @Override
  public String identifier() {
    return "add-redundant-edges";
  }
}
