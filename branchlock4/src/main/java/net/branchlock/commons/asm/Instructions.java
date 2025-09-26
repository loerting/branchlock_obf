package net.branchlock.commons.asm;

import net.branchlock.commons.string.StringUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.*;
import java.util.stream.Collectors;

public class Instructions implements Opcodes {

  public static final int MAX_STR_LEN = Short.MAX_VALUE - 2;
  private static final List<Integer> OTHER_SIDE_EFFECT_OPS =
    Arrays.asList(AALOAD, AASTORE, ANEWARRAY, ARRAYLENGTH, ATHROW, BALOAD, BASTORE, CALOAD, CASTORE, CHECKCAST,
      DALOAD, DASTORE, FALOAD, FASTORE, IALOAD, IASTORE, IDIV, IREM, LALOAD, LASTORE, LDIV, LREM,
      MONITORENTER, MONITOREXIT, MULTIANEWARRAY, NEW, NEWARRAY, SALOAD, SASTORE);

  public static InsnList copy(InsnList insnList) {
    InsnList copy = new InsnList();
    Map<LabelNode, LabelNode> labels = cloneLabels(insnList);
    for (AbstractInsnNode ain : insnList) {
      copy.add(ain.clone(labels));
    }
    return copy;
  }

  public static Map<LabelNode, LabelNode> cloneLabels(InsnList insns) {
    HashMap<LabelNode, LabelNode> labelMap = new HashMap<>();
    for (AbstractInsnNode insn = insns.getFirst(); insn != null; insn = insn.getNext()) {
      if (insn.getType() == AbstractInsnNode.LABEL) {
        labelMap.put((LabelNode) insn, new LabelNode());
      }
    }
    return labelMap;
  }

  public static boolean isComputable(AbstractInsnNode ain) {
    switch (ain.getType()) {
      case AbstractInsnNode.METHOD_INSN:
      case AbstractInsnNode.FIELD_INSN:
      case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
      case AbstractInsnNode.VAR_INSN:
      case AbstractInsnNode.JUMP_INSN:
        return false;
      default:
        return !isCodeEnd(ain);
    }
  }

  public static boolean isCodeEnd(AbstractInsnNode ain) {
    switch (ain.getOpcode()) {
      case ATHROW:
      case RETURN:
      case ARETURN:
      case DRETURN:
      case FRETURN:
      case IRETURN:
      case LRETURN:
        return true;
      default:
        return false;
    }
  }

  public static AbstractInsnNode getRealNext(AbstractInsnNode ain) {
    if (ain == null)
      return null;
    do {
      // skip labels, frames and line numbers
      ain = ain.getNext();
    } while (ain != null && (ain.getOpcode() == -1 || ain.getOpcode() == NOP));
    return ain;
  }

  public static AbstractInsnNode getRealPrevious(AbstractInsnNode ain) {
    if (ain == null)
      return null;
    do {
      // skip labels, frames and line numbers
      ain = ain.getPrevious();
    } while (ain != null && (ain.getOpcode() == -1 || ain.getOpcode() == NOP));
    return ain;
  }

  public static boolean isIntegerPush(AbstractInsnNode ain) {
    int op = ain.getOpcode();

    switch (op) {
      case BIPUSH:
      case SIPUSH:
      case ICONST_M1:
      case ICONST_0:
      case ICONST_1:
      case ICONST_2:
      case ICONST_3:
      case ICONST_4:
      case ICONST_5:
        return true;
    }
    if (ain.getType() == AbstractInsnNode.LDC_INSN) {
      return ((LdcInsnNode) ain).cst instanceof Integer;
    }
    return false;
  }

  public static boolean isWholeNumber(AbstractInsnNode ain) {
    int op = ain.getOpcode();

    switch (op) {
      case BIPUSH:
      case SIPUSH:
      case LCONST_0:
      case LCONST_1:
      case ICONST_M1:
      case ICONST_0:
      case ICONST_1:
      case ICONST_2:
      case ICONST_3:
      case ICONST_4:
      case ICONST_5:
        return true;
    }
    if (ain.getType() == AbstractInsnNode.LDC_INSN) {
      Object cst = ((LdcInsnNode) ain).cst;
      return cst instanceof Integer || cst instanceof Long;
    }
    return false;
  }

  public static boolean isNumber(AbstractInsnNode ain) {
    if (isWholeNumber(ain))
      return true;
    switch (ain.getOpcode()) {
      case DCONST_0:
      case DCONST_1:
      case FCONST_0:
      case FCONST_1:
      case FCONST_2:
        return true;
      default:
        return ain instanceof LdcInsnNode && ((LdcInsnNode) ain).cst instanceof Number;
    }
  }

  public static Number getNumberVal(AbstractInsnNode node) {
    if (node.getType() == AbstractInsnNode.LDC_INSN) {
      return (Number) ((LdcInsnNode) node).cst;
    }
    switch (node.getOpcode()) {
      case DCONST_0:
        return 0d;
      case DCONST_1:
        return 1d;
      case FCONST_0:
        return 0f;
      case FCONST_1:
        return 1f;
      case FCONST_2:
        return 2f;
      default:
        return getWholeNumberValue(node);
    }
  }

  public static Number getWholeNumberValue(AbstractInsnNode node) {
    if (node.getOpcode() >= ICONST_M1 && node.getOpcode() <= ICONST_5) {
      return node.getOpcode() - ICONST_0; // simple but effective
    }
    if (node.getOpcode() >= LCONST_0 && node.getOpcode() <= LCONST_1) {
      return (long) (node.getOpcode() - LCONST_0);
    }
    if (node.getOpcode() == SIPUSH || node.getOpcode() == BIPUSH) {
      return ((IntInsnNode) node).operand;
    }
    if (node.getType() == AbstractInsnNode.LDC_INSN) {
      return (Number) ((LdcInsnNode) node).cst;
    }
    throw new IllegalArgumentException("not a whole number: " + node.getClass().getName());
  }

  public static int getIntValue(AbstractInsnNode node) {
    if (node.getOpcode() >= ICONST_M1 && node.getOpcode() <= ICONST_5) {
      return node.getOpcode() - ICONST_0; // simple but effective
    }
    if (node.getOpcode() == SIPUSH || node.getOpcode() == BIPUSH) {
      return ((IntInsnNode) node).operand;
    }
    if (node.getType() == AbstractInsnNode.LDC_INSN) {
      return (Integer) ((LdcInsnNode) node).cst;
    }
    throw new IllegalArgumentException("not an int: " + node.getClass().getName());
  }

  public static long getLongValue(AbstractInsnNode node) {
    if (node.getOpcode() >= LCONST_0 && node.getOpcode() <= LCONST_1) {
      return (node.getOpcode() - LCONST_0);
    }
    if (node.getType() == AbstractInsnNode.LDC_INSN) {
      return (Long) ((LdcInsnNode) node).cst;
    }
    throw new IllegalArgumentException("not a long: " + node.getClass().getName());
  }

  public static AbstractInsnNode makeNullPush(Type type) {
    switch (type.getSort()) {
      case Type.OBJECT:
      case Type.ARRAY:
        return new InsnNode(ACONST_NULL);
      case Type.VOID:
        return new InsnNode(NOP);
      case Type.DOUBLE:
        return new InsnNode(DCONST_0);
      case Type.FLOAT:
        return new InsnNode(FCONST_0);
      case Type.LONG:
        return new InsnNode(LCONST_0);
      default:
        return new InsnNode(ICONST_0);
    }
  }

  public static AbstractInsnNode intPush(int intValue) {
    if (intValue >= -1 && intValue <= 5) {
      return new InsnNode(ICONST_0 + intValue);
    }
    if (intValue >= -128 && intValue <= 127) {
      return new IntInsnNode(BIPUSH, intValue);
    }
    if (intValue >= -32768 && intValue <= 32767) {
      return new IntInsnNode(SIPUSH, intValue);
    }
    return new LdcInsnNode(intValue);
  }

  public static AbstractInsnNode longPush(long longValue) {
    if (longValue == 0)
      return new InsnNode(LCONST_0);
    if (longValue == 1)
      return new InsnNode(LCONST_1);
    return new LdcInsnNode(longValue);
  }

  public static AbstractInsnNode doublePush(double doubleValue) {
    if (doubleValue == 0)
      return new InsnNode(DCONST_0);
    if (doubleValue == 1)
      return new InsnNode(DCONST_1);
    return new LdcInsnNode(doubleValue);
  }

  public static AbstractInsnNode floatPush(float floatValue) {
    if (floatValue == 0)
      return new InsnNode(FCONST_0);
    if (floatValue == 1)
      return new InsnNode(FCONST_1);
    if (floatValue == 2)
      return new InsnNode(FCONST_2);
    return new LdcInsnNode(floatValue);
  }

  public static InsnList singleton(AbstractInsnNode ain) {
    InsnList list = new InsnList();
    list.add(ain);
    return list;
  }

  public static boolean isObject(Type type) {
    int sort = type.getSort();
    return sort == Type.OBJECT || sort == Type.ARRAY;
  }

  /**
   * Use BMethod's utils instead.
   */
  @Deprecated
  public static boolean contains(MethodNode m, int... opcode) {
    for (AbstractInsnNode ain : m.instructions.toArray()) {
      for (int value : opcode) {
        if (value == ain.getOpcode())
          return true;
      }
    }
    return false;
  }

  /**
   * @param value must be an Integer Long Float or Double
   */
  public static AbstractInsnNode numberPush(Number value) {
    if (value instanceof Integer)
      return intPush(value.intValue());
    if (value instanceof Long)
      return longPush(value.longValue());
    if (value instanceof Float)
      return floatPush(value.floatValue());
    if (value instanceof Double)
      return doublePush(value.doubleValue());
    throw new IllegalArgumentException("not a handled number");
  }

  /**
   * Use BMethod's utils instead.
   */
  @Deprecated
  public static void replacePlaceholderCode(InsnList il, String fieldName, AbstractInsnNode replacement) {
    for (AbstractInsnNode ain : il.toArray()) {
      if (ain.getType() == AbstractInsnNode.FIELD_INSN) {
        FieldInsnNode fieldIn = (FieldInsnNode) ain;
        if (fieldIn.name.equals(fieldName)) {
          il.set(fieldIn, replacement);
        }
      }
    }
  }

  /**
   * Use BMethod's utils instead.
   */
  @Deprecated
  public static void replacePlaceholderCode(InsnList il, String fieldName, InsnList replacement) {
    for (AbstractInsnNode ain : il.toArray()) {
      if (ain.getType() == AbstractInsnNode.FIELD_INSN) {
        FieldInsnNode fieldIn = (FieldInsnNode) ain;
        if (fieldIn.name.equals(fieldName)) {
          il.insertBefore(fieldIn, replacement);
          il.remove(fieldIn);
        }
      }
    }
  }

  /**
   * Use BMethod's utils instead.
   */
  @Deprecated
  public static AbstractInsnNode find(InsnList il, int op) {
    for (AbstractInsnNode ain : il.toArray()) {
      if (ain.getOpcode() == op) {
        return ain;
      }
    }
    return null;
  }

  /**
   * Use BMethod's utils instead.
   */
  @Deprecated
  public static void cutReturn(InsnList il) {
    LabelNode end = new LabelNode();
    for (AbstractInsnNode ain : il.toArray()) {
      int opcode = ain.getOpcode();
      if (opcode == RETURN || opcode == ARETURN || opcode == IRETURN || opcode == LRETURN || opcode == FRETURN || opcode == DRETURN) {
        il.set(ain, new JumpInsnNode(GOTO, end));
      }
    }
    il.add(end);
  }

  /**
   * Use BMethod's utils instead.
   */
  @Deprecated
  public static void removeLines(InsnList il) {
    for (AbstractInsnNode ain : il.toArray()) {
      if (ain.getType() == AbstractInsnNode.LINE) {
        il.remove(ain);
      }
    }
  }

  public static LookupSwitchInsnNode orderSwitch(LookupSwitchInsnNode lsin) {
    HashMap<Integer, LabelNode> cases = new HashMap<>();
    for (int i = 0; i < lsin.keys.size(); i++) {
      Integer key = lsin.keys.get(i);
      LabelNode ln = lsin.labels.get(i);
      cases.put(key, ln);
    }
    lsin.keys = cases.keySet().stream().sorted(Integer::compareTo).collect(Collectors.toList());
    lsin.labels = cases.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).map(Map.Entry::getValue)
      .collect(Collectors.toList());
    return lsin;
  }

  public static boolean hasSideEffect(AbstractInsnNode ain) {
    int opcode = ain.getOpcode();
    if (opcode == -1)
      return false;

    return ain.getType() == AbstractInsnNode.METHOD_INSN ||
      ain.getType() == AbstractInsnNode.FIELD_INSN ||
      ain.getType() == AbstractInsnNode.INVOKE_DYNAMIC_INSN ||
      OTHER_SIDE_EFFECT_OPS.contains(opcode);
  }

  public static InsnList safeStringPush(String value) {
    if (ASMLimits.isUTF8TooLarge(value)) {
      int split = 1;
      int length = value.length();
      List<String> strings;
      do {
        split++;
        strings = StringUtils.splitEqually(value, length / split);
      } while (strings.stream().anyMatch(ASMLimits::isUTF8TooLarge));

      InsnList strMaker = new InsnList();
      boolean firstAdded = false;
      for (String splitString : strings) {
        strMaker.add(new LdcInsnNode(splitString));
        if (firstAdded) {
          strMaker.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;"));
        } else {
          firstAdded = true;
        }
      }
      return strMaker;
    }
    InsnList strMaker = new InsnList();
    strMaker.add(new LdcInsnNode(value));
    return strMaker;
  }

  public static InsnList generateConsoleDebug(String toString) {
    InsnList dbg = new InsnList();
    dbg.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
    dbg.add(new LdcInsnNode(toString));
    dbg.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V"));
    return dbg;
  }
}
