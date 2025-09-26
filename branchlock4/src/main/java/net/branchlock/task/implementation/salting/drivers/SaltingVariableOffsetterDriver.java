package net.branchlock.task.implementation.salting.drivers;

import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.SingleMethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.salting.Salting;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Collection;
import java.util.List;

public class SaltingVariableOffsetterDriver implements SingleMethodDriver, Opcodes {
  private static final List<Integer> TWO_WORD_VAR_INSTR = List.of(LLOAD, LSTORE, DLOAD, DSTORE);
  private final Salting salting;
  private final SaltingSignatureCollectorDriver saltingSignatureCollectorDriver;

  public SaltingVariableOffsetterDriver(Salting salting, SaltingSignatureCollectorDriver saltingSignatureCollectorDriver) {
    this.salting = salting;
    this.saltingSignatureCollectorDriver = saltingSignatureCollectorDriver;
  }

  public static int getSaltSlot(String newDescriptor, boolean isStatic) {
    int var = isStatic ? 0 : 1; // skip "this"
    for (Type argumentType : Type.getArgumentTypes(newDescriptor)) var += argumentType.getSize();

    // the variable is the last parameter, which is an int type.
    return var - Type.INT_TYPE.getSize();
  }

  @Override
  public boolean driveEach(BMethod bm) {
    String newDescriptor = saltingSignatureCollectorDriver.newDescriptors.get(bm);
    if (newDescriptor == null) return true;


    int saltSlot = getSaltSlot(newDescriptor, bm.isStatic());
    bm.getCode().streamInstructions()
      .filter(i -> i.getType() == AbstractInsnNode.VAR_INSN)
      .map(i -> (VarInsnNode) i)
      .filter(i -> i.var >= saltSlot)
      .forEach(i -> i.var += Type.INT_TYPE.getSize());
    bm.getCode().streamInstructions()
      .filter(i -> i.getType() == AbstractInsnNode.IINC_INSN)
      .map(i -> (IincInsnNode) i)
      .filter(i -> i.var >= saltSlot)
      .forEach(i -> i.var += Type.INT_TYPE.getSize());

    if (bm.getCode().streamInstructions()
      .anyMatch(i -> i.getType() == AbstractInsnNode.VAR_INSN
        && TWO_WORD_VAR_INSTR.contains(i.getOpcode()) && ((VarInsnNode) i).var == saltSlot - 1)) {
      // does not cover following scenario, which is legal in bytecode but not in Java:
      // static void example(int arg0)
      // iload 0
      // pop
      // dconst_0
      // dstore 0
      // ...
      // saltSlot cannot be in local #1, because the arg0 was overwritten by dstore 0, and is now size 2
      throw new IllegalStateException("Rare local var case observed which is not supported yet. (Exclude member " + bm + ")");
    }


    bm.maxLocals += Type.INT_TYPE.getSize();
    bm.localVariables = null;

    return true;
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return List.of(t -> t.filter(BMethod::isLocal));
  }

  @Override
  public String identifier() {
    return "variable-offsetter";

  }
}
