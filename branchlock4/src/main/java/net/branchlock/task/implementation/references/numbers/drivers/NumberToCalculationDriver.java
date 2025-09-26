package net.branchlock.task.implementation.references.numbers.drivers;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.ASMLimits;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.MethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.references.numbers.Numbers;
import net.branchlock.task.implementation.salting.MethodSalt;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public class NumberToCalculationDriver implements MethodDriver, Opcodes {
  private static final Set<String> PERFORMANCE_DEP_SIGNATURES = Set.of("hashCode()I", "equals(Ljava/lang/Object;)Z", "toString()Ljava/lang/String;");
  private final Numbers numbers;

  public NumberToCalculationDriver(Numbers numbers) {
    this.numbers = numbers;
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    Branchlock.LOGGER.info("Converting numbers to calculations...");
    stream.forEach(bm -> {
        for (AbstractInsnNode ain : bm.instructions.toArray()) {
        if (bm.getInstructionCount() > ASMLimits.MAX_METHOD_SIZE * 3 / 4) {
          Branchlock.LOGGER.warning("Method got too big, skipping: {}", bm);
          break;
        }
        if (Instructions.isWholeNumber(ain)) {
          replaceNumberWithCalculation(bm, ain);
        }
      }
    });
    return true;
  }

  private void replaceNumberWithCalculation(BMethod bm, AbstractInsnNode ain) {
    Number n = Instructions.getWholeNumberValue(ain);
    // small members tend to be used more often, don't obfuscate too much
    if (n.equals(0) || n.equals(1))
      return;

    InsnList il = new InsnList();
    MethodSalt salt = bm.getSalt();
    if (n instanceof Integer && salt != null) {
      il.add(salt.makeLoad());
      il.add(Numbers.generateCalculation(salt.getValue() ^ n.intValue(), Numbers.NumbersStrength.WEAK));
      il.add(new InsnNode(IXOR));
    } else {
      Numbers.NumbersStrength strength = n.shortValue() == n.longValue() ? Numbers.NumbersStrength.MEDIUM : Numbers.NumbersStrength.STRONG;
      il.add(Numbers.generateCalculation(n, PERFORMANCE_DEP_SIGNATURES.contains(bm.getIdentifier()) ? Numbers.NumbersStrength.WEAK : strength));
    }

      bm.instructions.insertBefore(ain, il);
      bm.instructions.remove(ain);
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return numbers.defaultMemberExclusionHandlers();
  }
}
