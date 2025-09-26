package net.branchlock.task.implementation.salting.drivers;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.ASMLimits;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.SingleClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.salting.MethodSalt;
import net.branchlock.task.implementation.salting.Salting;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.Random;

public class TrapInserterDriver implements SingleClassDriver, Opcodes {
  private static final Random R = Branchlock.R;
  private final Salting salting;

  public TrapInserterDriver(Salting salting) {
    this.salting = salting;
  }

  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    return salting.defaultClassExclusionHandlers();
  }

  @Override
  public String identifier() {
    return "trap-inserter";
  }

  @Override
  public boolean driveEach(BClass c) {
    c.methods.stream()
      .filter(bm -> bm.matchesAccess(ACC_STATIC, ACC_ABSTRACT | ACC_NATIVE)
        && !bm.isStaticInitializer() && bm.getSalt() != null
        && bm.getInstructionCount() > 40 && bm.getInstructionCount() < ASMLimits.MAX_METHOD_SIZE * 0.8)
      .max(Comparator.comparingInt(BMethod::getInstructionCount)).ifPresent(big -> {
        MethodSalt fixedDescVal = big.getSalt();
        // load the variable, insert an if statement that checks if the last bit is set in a way that the if is never taken.
        // inside the if, place an infinite loop. This can trap emulators that guess the fixed desc value.

        InsnList trap = new InsnList();
        LabelNode afterTrap = new LabelNode();
        LabelNode startTrap = new LabelNode();

        boolean lastBitSet = (fixedDescVal.getValue() & 1) == 1;

        trap.add(fixedDescVal.makeLoad());
        trap.add(new InsnNode(ICONST_1));
        trap.add(new InsnNode(IAND));
        trap.add(new JumpInsnNode(lastBitSet ? IFNE : IFEQ, afterTrap));
        trap.add(startTrap);
        if (R.nextBoolean()) // just to place something between the label and the goto to make it less detectable.
          trap.add(new IincInsnNode(fixedDescVal.getSlot(), 1));
        trap.add(new JumpInsnNode(GOTO, startTrap));
        trap.add(afterTrap);

        JumpInsnNode gotoInsn = big.getCode().findByOpcode(GOTO);
        if (gotoInsn != null && R.nextBoolean())
            big.instructions.insertBefore(gotoInsn, trap);
        else
            big.instructions.insert(trap);

        big.maxStack = Math.max(big.maxStack, 2);
      });

    return true;
  }
}
