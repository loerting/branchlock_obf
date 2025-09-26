package net.branchlock.task.implementation.references.numbers.term;

import net.branchlock.commons.asm.Instructions;
import net.branchlock.task.implementation.references.numbers.term.operations.*;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.nio.charset.StandardCharsets;

public class NumTerm extends Term {
  private static final String EASTER_EGG = "What is the answer to the question that cannot be answered without revealing the answer itself? Tell us at contact@branchlock.net";
  private final Number number;

  public NumTerm(Number number) {
    this.number = number;
    if (!(number instanceof Integer || number instanceof Long))
      throw new IllegalArgumentException();
  }

  public static String generateEvilString() {
    StringBuilder sb = new StringBuilder();
    int length = 2 + R.nextInt(3);
    for (int i = 0; i < length; i++) {
      sb.append(generateEvilUnicode());
    }
    return sb.toString();
  }

  public static String generateEvilUnicode() {
    if (R.nextFloat() < 0.1) return "\u2060";
    if (R.nextFloat() < 0.1) return "\uFFFE";

    // https://en.wikipedia.org/wiki/Tags_(Unicode_block)
    byte[] bytes = new byte[4];
    bytes[0] = (byte) (0xF3);
    bytes[1] = (byte) (0xA0);
    // 0x80 to 0x81
    bytes[2] = (byte) (0x80 + R.nextInt(2));
    // 0xA0 to 0xBF
    bytes[3] = (byte) (0xA0 + R.nextInt(0x40));
    return new String(bytes, StandardCharsets.UTF_8).replace("\uFFFD", "");
  }

  @Override
  public InsnList getTerm(boolean enhanced) {
    boolean wide = isWide();
    if (enhanced) {
      if (!wide && R.nextBoolean()) {
        return createDeterministicFunctionXor();
      }
      return createParseWithString(wide);
    }
    if (wide && number.longValue() == number.intValue()) {
      InsnList il = new InsnList();
      il.add(Instructions.intPush(number.intValue()));
      il.add(new InsnNode(I2L));
      return il;
    }
    if (!wide && number.intValue() == (char)number.intValue() && R.nextFloat() < 0.1f) {
      InsnList il = new InsnList();
      il.add(Instructions.intPush(number.intValue()));
      il.add(new InsnNode(I2C));
      return il;
    }
    return Instructions.singleton(Instructions.numberPush(number));
  }

  private InsnList createDeterministicFunctionXor() {
    // converts number to e.g.:
    // "random string".hashCode() ^ 123

    InsnList il = new InsnList();

    int randomValue = generateRandomDeterministicFunction(il);

    il.add(Instructions.intPush(randomValue ^ number.intValue()));
    il.add(new InsnNode(IXOR));
    return il;
  }

  private static int generateRandomDeterministicFunction(InsnList il) {
    int randomValue = switch (R.nextInt(2)) {
      case 0 -> {
        String randomString;
        if (R.nextFloat() < 0.1f) {
          randomString = generateEvilString();
        } else {
          int randIndex = R.nextInt(EASTER_EGG.length());
          randomString = String.format("%03d", randIndex) + EASTER_EGG.charAt(randIndex);
        }

        il.add(new LdcInsnNode(randomString));
        il.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I"));

        yield randomString.hashCode();
      }
      case 1 -> {
        float randomFloat = R.nextFloat();
        il.add(new LdcInsnNode(randomFloat));
        il.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Float", "floatToIntBits", "(F)I"));
        yield Float.floatToIntBits(randomFloat);
      }
      default -> throw new AssertionError();
    };

    if (R.nextBoolean()) {
      // narrow down to maybe save some calculations and introduce more randomness
      int narrowingConversion = R.nextInt(3);
      il.add(new InsnNode(switch (narrowingConversion) {
        case 0 -> I2B;
        case 1 -> I2S;
        case 2 -> I2C;
        default -> throw new AssertionError();
      }));
      randomValue = switch (narrowingConversion) {
        case 0 -> (byte) randomValue;
        case 1 -> (short) randomValue;
        case 2 -> (char) randomValue;
        default -> throw new AssertionError();
      };
    }
    return randomValue;
  }

  private InsnList createParseWithString(boolean wide) {
    int radix = 20 + R.nextInt(16);
    InsnList il = new InsnList();
    il.add(new LdcInsnNode(Long.toString(number.longValue(), radix)));
    il.add(Instructions.intPush(radix));
    il.add(new MethodInsnNode(INVOKESTATIC, (wide ? "java/lang/Long" : "java/lang/Integer"),
      wide ? "parseLong" : "parseInt", "(Ljava/lang/String;I)" + (wide ? "J" : "I")));
    return il;
  }

  @Override
  public Number calculate() {
    return number;
  }

  @Override
  public boolean isWide() {
    return number instanceof Long;
  }

  public BiTerm obfuscate() {
    if (number.longValue() >= Byte.MIN_VALUE && number.longValue() <= Byte.MAX_VALUE) {
      // small terms are not really secured with additions, subtractions or xors.
      switch (R.nextInt(2)) {
        case 0:
          return TermUShr.forNumber(number);
        case 1:
          return TermShl.forNumber(number);
      }
    } else {
      switch (R.nextInt(5)) {
        case 0:
          return TermAddition.forNumber(number);
        case 1:
          return TermSubtraction.forNumber(number);
        case 2:
          return TermUShr.forNumber(number);
        case 3:
          return TermShl.forNumber(number);
        case 4:
          return TermXor.forNumber(number);
      }
    }
    throw new AssertionError();
  }
}
