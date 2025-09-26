package net.branchlock.task.implementation.references.numbers.drivers;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.MethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.references.numbers.Numbers;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SwitchOperandExtractorDriver implements MethodDriver {
  private static final Random R = Branchlock.R;
  private final Numbers numbers;

  public SwitchOperandExtractorDriver(Numbers numbers) {
    this.numbers = numbers;
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    Branchlock.LOGGER.info("Extracting switch operands...");
    stream.forEach(bm -> {
        for (AbstractInsnNode ain : bm.instructions.toArray()) {
        int op = ain.getOpcode();
        if (op == Opcodes.TABLESWITCH) {
          TableSwitchInsnNode tsin = (TableSwitchInsnNode) ain;

          // convert to lookup switch
          LookupSwitchInsnNode lsin =
            new LookupSwitchInsnNode(tsin.dflt, IntStream.rangeClosed(tsin.min, tsin.max).toArray(),
              tsin.labels.toArray(new LabelNode[0]));

            bm.instructions.set(tsin, lsin);
          makeSwitchXOR(bm, lsin);
        } else if (op == Opcodes.LOOKUPSWITCH) {
          makeSwitchXOR(bm, (LookupSwitchInsnNode) ain);
        }
      }
    });
    return true;
  }

  private void makeSwitchXOR(BMethod bm, LookupSwitchInsnNode lsin) {
    int xor = R.nextInt();
      bm.instructions.insertBefore(lsin, Instructions.intPush(xor));
      bm.instructions.insertBefore(lsin, new InsnNode(Opcodes.IXOR));
    lsin.keys.replaceAll(i -> i ^ xor);
    Instructions.orderSwitch(lsin);
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return numbers.defaultMemberExclusionHandlers();
  }
}
