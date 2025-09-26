package net.branchlock.task.implementation.references.numbers.drivers;

import net.branchlock.Branchlock;
import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.MethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.references.numbers.Numbers;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Converts all interesting floating point numbers to integers.
 */
public class FloatingPointConverterDriver implements MethodDriver, Opcodes {
  private final Numbers numbers;

  public FloatingPointConverterDriver(Numbers numbers) {
    this.numbers = numbers;
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    Branchlock.LOGGER.info("Lifting floating point numbers...");
    stream.forEach(bm -> {
      for (AbstractInsnNode ain : bm.instructions.toArray())
        if (ain.getOpcode() == LDC) {
          LdcInsnNode ldcInsnNode = (LdcInsnNode) ain;
          if (ldcInsnNode.cst instanceof Double) {
            ldcInsnNode.cst = Double.doubleToLongBits((Double) ldcInsnNode.cst);
            bm.instructions.insert(ain, new MethodInsnNode(INVOKESTATIC, "java/lang/Double", "longBitsToDouble", "(J)D"));
          } else if (ldcInsnNode.cst instanceof Float) {
            ldcInsnNode.cst = Float.floatToIntBits((Float) ldcInsnNode.cst);
            bm.instructions.insert(ain, new MethodInsnNode(INVOKESTATIC, "java/lang/Float", "intBitsToFloat", "(I)F"));
          }
        }
    });
    return true;
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return numbers.defaultMemberExclusionHandlers();
  }
}
