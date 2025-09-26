package net.branchlock.commons.asm.interpreter;

import net.branchlock.commons.asm.Instructions;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMember;
import net.branchlock.structure.provider.IDataProvider;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

import java.util.List;

public class TypeInterpreter extends Interpreter<BasicValue> implements Opcodes {

  public static final Type NULL_TYPE = Type.getObjectType("null");
  private final IDataProvider dataProvider;
  private final BMember memberToBeAnalyzed;

  public TypeInterpreter(IDataProvider dataProvider, BMember memberToBeAnalyzed) {
    super(ASM8);
    this.dataProvider = dataProvider;
    this.memberToBeAnalyzed = memberToBeAnalyzed;
  }

  @Override
  public BasicValue newValue(final Type type) {
    if (type == null) {
      return BasicValue.UNINITIALIZED_VALUE;
    }
    switch (type.getSort()) {
      case Type.VOID:
        return null;
      case Type.BOOLEAN:
      case Type.CHAR:
      case Type.BYTE:
      case Type.SHORT:
      case Type.INT:
        return BasicValue.INT_VALUE;
      case Type.FLOAT:
        return BasicValue.FLOAT_VALUE;
      case Type.LONG:
        return BasicValue.LONG_VALUE;
      case Type.DOUBLE:
        return BasicValue.DOUBLE_VALUE;
      case Type.ARRAY:
      case Type.OBJECT:
        return new BasicValue(type);
      default:
        throw new AssertionError();
    }
  }

  @Override
  public BasicValue newOperation(final AbstractInsnNode insn) throws AnalyzerException {
    switch (insn.getOpcode()) {
      case ACONST_NULL:
        return newValue(NULL_TYPE);
      case ICONST_M1:
      case ICONST_0:
      case ICONST_1:
      case ICONST_2:
      case ICONST_3:
      case ICONST_4:
      case ICONST_5:
      case BIPUSH:
      case SIPUSH:
        return BasicValue.INT_VALUE;
      case LCONST_0:
      case LCONST_1:
        return BasicValue.LONG_VALUE;
      case FCONST_0:
      case FCONST_1:
      case FCONST_2:
        return BasicValue.FLOAT_VALUE;
      case DCONST_0:
      case DCONST_1:
        return BasicValue.DOUBLE_VALUE;
      case LDC:
        Object value = ((LdcInsnNode) insn).cst;
        if (value instanceof Integer) {
          return BasicValue.INT_VALUE;
        } else if (value instanceof Float) {
          return BasicValue.FLOAT_VALUE;
        } else if (value instanceof Long) {
          return BasicValue.LONG_VALUE;
        } else if (value instanceof Double) {
          return BasicValue.DOUBLE_VALUE;
        } else if (value instanceof String) {
          return newValue(Type.getObjectType("java/lang/String"));
        } else if (value instanceof Type) {
          int sort = ((Type) value).getSort();
          if (sort == Type.OBJECT || sort == Type.ARRAY) {
            return newValue(Type.getObjectType("java/lang/Class"));
          } else if (sort == Type.METHOD) {
            return newValue(Type.getObjectType("java/lang/invoke/MethodType"));
          } else {
            throw new AnalyzerException(insn, "Illegal LDC value " + value);
          }
        } else if (value instanceof Handle) {
          return newValue(Type.getObjectType("java/lang/invoke/MethodHandle"));
        } else if (value instanceof ConstantDynamic) {
          return newValue(Type.getType(((ConstantDynamic) value).getDescriptor()));
        } else {
          throw new AnalyzerException(insn, "Illegal LDC value " + value);
        }
      case JSR:
        return BasicValue.RETURNADDRESS_VALUE;
      case GETSTATIC:
        return newValue(Type.getType(((FieldInsnNode) insn).desc));
      case NEW:
        return newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
      default:
        throw new AssertionError();
    }
  }

  @Override
  public BasicValue copyOperation(final AbstractInsnNode insn, final BasicValue value) {
    return value;
  }

  @Override
  public BasicValue unaryOperation(final AbstractInsnNode insn, final BasicValue value) throws AnalyzerException {
    switch (insn.getOpcode()) {
      case INEG:
      case IINC:
      case L2I:
      case F2I:
      case D2I:
      case I2B:
      case I2C:
      case I2S:
        return BasicValue.INT_VALUE;
      case FNEG:
      case I2F:
      case L2F:
      case D2F:
        return BasicValue.FLOAT_VALUE;
      case LNEG:
      case I2L:
      case F2L:
      case D2L:
        return BasicValue.LONG_VALUE;
      case DNEG:
      case I2D:
      case L2D:
      case F2D:
        return BasicValue.DOUBLE_VALUE;
      case IFEQ:
      case IFNE:
      case IFLT:
      case IFGE:
      case IFGT:
      case IFLE:
      case TABLESWITCH:
      case LOOKUPSWITCH:
      case IRETURN:
      case LRETURN:
      case FRETURN:
      case DRETURN:
      case ARETURN:
      case PUTSTATIC:
        return null;
      case GETFIELD:
        return newValue(Type.getType(((FieldInsnNode) insn).desc));
      case NEWARRAY:
        switch (((IntInsnNode) insn).operand) {
          case T_BOOLEAN:
            return newValue(Type.getType("[Z"));
          case T_CHAR:
            return newValue(Type.getType("[C"));
          case T_BYTE:
            return newValue(Type.getType("[B"));
          case T_SHORT:
            return newValue(Type.getType("[S"));
          case T_INT:
            return newValue(Type.getType("[I"));
          case T_FLOAT:
            return newValue(Type.getType("[F"));
          case T_DOUBLE:
            return newValue(Type.getType("[D"));
          case T_LONG:
            return newValue(Type.getType("[J"));
          default:
            break;
        }
        throw new AnalyzerException(insn, "Invalid array type");
      case ANEWARRAY:
        return newValue(Type.getType("[" + Type.getObjectType(((TypeInsnNode) insn).desc)));
      case ARRAYLENGTH:
        return BasicValue.INT_VALUE;
      case ATHROW:
        return null;
      case CHECKCAST:
        return newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
      case INSTANCEOF:
        return BasicValue.INT_VALUE;
      case MONITORENTER:
      case MONITOREXIT:
      case IFNULL:
      case IFNONNULL:
        return null;
      default:
        throw new AssertionError();
    }
  }

  @Override
  public BasicValue binaryOperation(final AbstractInsnNode insn, final BasicValue value1, final BasicValue value2) {
    switch (insn.getOpcode()) {
      case IALOAD:
      case BALOAD:
      case CALOAD:
      case SALOAD:
      case IADD:
      case ISUB:
      case IMUL:
      case IDIV:
      case IREM:
      case ISHL:
      case ISHR:
      case IUSHR:
      case IAND:
      case IOR:
      case IXOR:
        return BasicValue.INT_VALUE;
      case FALOAD:
      case FADD:
      case FSUB:
      case FMUL:
      case FDIV:
      case FREM:
        return BasicValue.FLOAT_VALUE;
      case LALOAD:
      case LADD:
      case LSUB:
      case LMUL:
      case LDIV:
      case LREM:
      case LSHL:
      case LSHR:
      case LUSHR:
      case LAND:
      case LOR:
      case LXOR:
        return BasicValue.LONG_VALUE;
      case DALOAD:
      case DADD:
      case DSUB:
      case DMUL:
      case DDIV:
      case DREM:
        return BasicValue.DOUBLE_VALUE;
      case AALOAD:
        return BasicValue.REFERENCE_VALUE;
      case LCMP:
      case FCMPL:
      case FCMPG:
      case DCMPL:
      case DCMPG:
        return BasicValue.INT_VALUE;
      case IF_ICMPEQ:
      case IF_ICMPNE:
      case IF_ICMPLT:
      case IF_ICMPGE:
      case IF_ICMPGT:
      case IF_ICMPLE:
      case IF_ACMPEQ:
      case IF_ACMPNE:
      case PUTFIELD:
        return null;
      default:
        throw new AssertionError();
    }
  }

  @Override
  public BasicValue ternaryOperation(final AbstractInsnNode insn, final BasicValue value1, final BasicValue value2,
                                     final BasicValue value3) {
    return null;
  }

  @Override
  public BasicValue naryOperation(final AbstractInsnNode insn, final List<? extends BasicValue> values) {
    int opcode = insn.getOpcode();
    if (opcode == MULTIANEWARRAY) {
      return newValue(Type.getType(((MultiANewArrayInsnNode) insn).desc));
    } else if (opcode == INVOKEDYNAMIC) {
      return newValue(Type.getReturnType(((InvokeDynamicInsnNode) insn).desc));
    } else {
      /*
      if (opcode == INVOKESPECIAL && ((MethodInsnNode) insn).name.equals("<init>")) {
        Convert value from TOP to OBJECT here
      }
      30. Oct 2022: BasicInterpreter does not do this, so we don't do it either
      */
      return newValue(Type.getReturnType(((MethodInsnNode) insn).desc));
    }
  }

  @Override
  public void returnOperation(final AbstractInsnNode insn, final BasicValue value, final BasicValue expected) {
    // Nothing to do.
  }

  @Override
  public BasicValue merge(final BasicValue value1, final BasicValue value2) {
    if (value1 == BasicValue.UNINITIALIZED_VALUE)
      return value1;
    if (value2 == BasicValue.UNINITIALIZED_VALUE)
      return value2;
    if (value1.equals(value2))
      return value1;
    if (value1.getType() == NULL_TYPE && Instructions.isObject(value2.getType()))
      return value2;
    if (value2.getType() == NULL_TYPE && Instructions.isObject(value1.getType()))
      return value1;


    if (isAssignableFrom(value1, value2)) {
      return value2;
    }
    if (isAssignableFrom(value2, value1)) {
      return value1;
    }

    return BasicValue.UNINITIALIZED_VALUE;
  }

  /*
    Limitation: Cannot detect uninitialized objects. returns true even if one object is NOT initialized (invokespecial <init>)!
 */
  public boolean isAssignableFrom(BasicValue v1, BasicValue v2) {
    if (v1 == BasicValue.UNINITIALIZED_VALUE || v2 == BasicValue.UNINITIALIZED_VALUE)
      return false; // you can't assign from uninitialized to uninitialized
    if (v1 == v2)
      return true;
    if (v2 == BasicValue.REFERENCE_VALUE)
      return false; // both values have to be BasicValue.REFERENCE_VALUE in order to be assignable
    return isAssignableFrom(v1.getType(), v2.getType());
  }

  /**
   * @return true if t1 can be assigned to t2 in frames
   */
  public boolean isAssignableFrom(Type t1, Type t2) {
    if (t1 == t2)
      return true;
    if (t1 == null || t2 == null)
      return false;
    if (t1.equals(t2))
      return true;
    if (Instructions.isObject(t1) && t2.getInternalName().equals("java/lang/Object"))
      return true;
    if (t1.getSort() == Type.OBJECT && t2.getSort() == Type.OBJECT) {
      // check assertion to parents
      String className1 = t1.getClassName().replace('.', '/');
      String className2 = t2.getClassName().replace('.', '/');
      if ("null".equals(className1) || "null".equals(className2))
        return false;
      BClass bClass = dataProvider.resolveBClass(className1, memberToBeAnalyzed);
      return bClass != null && bClass.isAssertableTo(className2);
    }

    return false;
  }

  public boolean canJumpFrom(Frame<BasicValue> from, Frame<BasicValue> to) {
    if (from == null || to == null)
      return false;
    if (from.getStackSize() != to.getStackSize())
      return false;
    // local size must be the same if the frames are from the same method
    for (int i = 0; i < from.getStackSize(); i++) {
      if (!isAssignableFrom(from.getStack(i), to.getStack(i)))
        return false;
    }
    for (int i = 0; i < from.getLocals(); i++) {
      if (!isAssignableFrom(from.getLocal(i), to.getLocal(i))) {
        return false;
      }
    }
    return true;
  }
}
