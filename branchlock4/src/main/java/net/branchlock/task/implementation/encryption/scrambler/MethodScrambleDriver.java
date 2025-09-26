package net.branchlock.task.implementation.encryption.scrambler;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.MethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class MethodScrambleDriver implements MethodDriver, Opcodes {
  private static final List<Integer> SHIFTABLE_OPS = List.of(IMUL, IDIV, LMUL, LDIV);
  private static final List<Integer> SHIFTS = List.of(ISHL, ISHR, LSHL, LSHR);
  private static final Random R = Branchlock.R;

  private final Scrambler scrambler;

  public MethodScrambleDriver(Scrambler scrambler) {
    this.scrambler = scrambler;
  }

  private static void turnOpcodeIntoBitshift(InsnList list, AbstractInsnNode ain) {
    int op = ain.getOpcode();
    boolean div = op == IDIV || op == LDIV;
    boolean wide = op == LMUL || op == LDIV;
    int shiftOp = SHIFTS.get(SHIFTABLE_OPS.indexOf(op));
    AbstractInsnNode previous = ain.getPrevious();
    long C = wide ? Instructions.getLongValue(previous) : Instructions.getIntValue(previous);
    if (C > 0) {
      double literalShift = Math.log10(C) / Math.log10(2);
      int shift = (int) Math.floor(literalShift);
      long remaining = C - (1L << shift);
      shift += (int) (R.nextGaussian() * 5) * (wide ? 64 : 32); // make it harder to understand by looking.
      if (remaining == 0) {
        list.set(previous, Instructions.intPush(shift));
        list.set(ain, new InsnNode(shiftOp));
      } else if (!div) {
        // TODO: division here

        // split multiplication -> one side is multiple of 2 and then translate to shift

        // 32 * 7
        // (32 * 4) + (32 * 3)
        // (32 << 2) + (32 * 3)

        InsnList il = new InsnList();
        il.add(new InsnNode(wide ? DUP2 : DUP));
        il.add(Instructions.intPush(shift));
        il.add(new InsnNode(shiftOp));
        if (wide) {
          il.add(new InsnNode(DUP2_X2));
          il.add(new InsnNode(POP2));
        } else {
          il.add(new InsnNode(SWAP));
        }
        if (remaining != 1) {
          il.add(wide ? Instructions.longPush(remaining) : Instructions.intPush((int) remaining));
          il.add(new InsnNode(wide ? LMUL : IMUL));
        }
        il.add(new InsnNode(wide ? LADD : IADD));

        list.insert(ain, il);

        list.remove(previous);
        list.remove(ain);
      }
    }
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    stream.forEach(bc -> {
      InsnList list = bc.instructions;
      bc.streamInstr().forEach(ain -> {

        int op = ain.getOpcode();
        if (op == I2C) {
          list.insertBefore(ain, Instructions.intPush(0xffff));
          list.set(ain, new InsnNode(IAND));
        } else if (op == INEG && R.nextFloat() < 0.75f) {
          // transform -(x) to ~(x)+1 caused issues on some JVM implementations.
        } else if (op == IINC && R.nextFloat() < 0.4f) {
          // increase the complexity of for loops
          IincInsnNode iinc = (IincInsnNode) ain;
          int random = R.nextInt(128) - 64;
          if (R.nextBoolean()) {
            list.insertBefore(ain, new IincInsnNode(iinc.var, random));
          } else {
            list.insert(ain, new IincInsnNode(iinc.var, random));
          }
          iinc.incr -= random;
        } else if (SHIFTABLE_OPS.contains(op) && Instructions.isWholeNumber(ain.getPrevious())) {
          turnOpcodeIntoBitshift(list, ain);
        }
      });
    });
    return true;
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return scrambler.defaultMemberExclusionHandlers();
  }

  @Override
  public String identifier() {
    return "method-scrambling";
  }
}
