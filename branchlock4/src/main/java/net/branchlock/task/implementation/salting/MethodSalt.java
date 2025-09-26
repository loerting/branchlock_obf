package net.branchlock.task.implementation.salting;

import net.branchlock.commons.asm.Instructions;
import net.branchlock.commons.java.Pair;
import net.branchlock.commons.os.Bits;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Integer salt for a method node.
 * Is passed on through as many methods as possible, computing a unique value for each method.
 * This salt is not hardcoded in the bytecode, but passed on as a parameter, therefore it is hard to find out for an attacker.
 * It can be used to strengthen the obfuscation.
 */
public class MethodSalt implements Opcodes {
  private final int salt;
  private final int variableSlot;

  public MethodSalt(int salt, int variableSlot) {
    this.salt = salt;
    this.variableSlot = variableSlot;
  }

  public VarInsnNode makeLoad() {
    return new VarInsnNode(ILOAD, variableSlot);
  }

  public InsnList loadEncryptedInt(int intValue) {
    InsnList il = new InsnList();
    il.add(makeLoad());

    Pair<Integer, Integer> diff = Bits.getOrAndDifference(salt, intValue);
    // at least 4 bits are different
    if (Integer.bitCount(diff.a) >= 4) {
      il.add(Instructions.intPush(diff.a));
      il.add(new InsnNode(IOR));

      if ((salt | diff.a) != ((salt | diff.a) & diff.b)) {
        il.add(Instructions.intPush(diff.b));
        il.add(new InsnNode(IAND));
      }
      return il;
    }

    il.add(Instructions.intPush(intValue ^ salt));
    il.add(new InsnNode(IXOR));
    return il;
  }

  public int getValue() {
    return salt;
  }

  @Override
  public String toString() {
    return "MethodSalt{" +
      "salt=" + salt +
      ", variableSlot=" + variableSlot +
      '}';
  }

  public int getSlot() {
    return variableSlot;
  }
}
