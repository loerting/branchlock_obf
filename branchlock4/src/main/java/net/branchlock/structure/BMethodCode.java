package net.branchlock.structure;

import net.branchlock.commons.asm.Instructions;
import net.branchlock.commons.asm.Reference;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A decorator for a methods instruction list.
 */
public class BMethodCode implements Opcodes {
  private final BMethod bMethod;

  public BMethodCode(BMethod bMethod) {
    this.bMethod = bMethod;
  }

  public InsnList getInstructions() {
    return bMethod.instructions;
  }

  public int countType(int insnType) {
    InsnList instructions = getInstructions();
    int count = 0;
    for (int i = 0; i < instructions.size(); i++) {
      if (instructions.get(i).getType() == insnType) {
        count++;
      }
    }
    return count;
  }

  public int countOpcodes(int... opcodes) {
    InsnList instructions = getInstructions();
    int count = 0;
    for (int i = 0; i < instructions.size(); i++) {
      for (int opcode : opcodes) {
        if (instructions.get(i).getOpcode() == opcode) {
          count++;
        }
      }
    }
    return count;
  }

  public void removeIf(Predicate<AbstractInsnNode> predicate) {
    InsnList instructions = getInstructions();
    for (AbstractInsnNode ain : instructions.toArray()) {
      if (predicate.test(ain)) {
        instructions.remove(ain);
      }
    }
  }

  public void cutReturn() {
    InsnList il = getInstructions();
    LabelNode end = new LabelNode();
    for (AbstractInsnNode ain : il.toArray()) {
      int opcode = ain.getOpcode();
      if (opcode == RETURN || opcode == ARETURN || opcode == IRETURN || opcode == LRETURN || opcode == FRETURN || opcode == DRETURN) {
        if (Instructions.getRealNext(ain) != null) {
          il.set(ain, new JumpInsnNode(GOTO, end));
        } else {
          // we are at the end of the method
          il.remove(ain);
        }
      }
    }
    il.add(end);
  }

  public Stream<AbstractInsnNode> streamInstructions() {
    return StreamSupport.stream(getInstructions().spliterator(), false);
  }

  @SuppressWarnings("unchecked")
  public <T extends AbstractInsnNode> Stream<T> streamOpcodes(int... opcodes) {
    return (Stream<T>) streamInstructions().filter(ain -> {
      for (int opcode : opcodes) {
        if (ain.getOpcode() == opcode) {
          return true;
        }
      }
      return false;
    });
  }


  public Stream<Object> streamLdcValues() {
    return streamInstructions().filter(ain -> ain.getType() == AbstractInsnNode.LDC_INSN).map(ain -> ((LdcInsnNode) ain).cst);
  }

  /**
   * Replaces specific field loads by instructions or constants.
   *
   * @param fieldName the name of the field to replace
   * @param value     the value to replace the field with. Can be an {@link InsnList}, an {@link AbstractInsnNode}, a {@link Number} or a {@link String}.
   */
  public void replacePlaceholder(String fieldName, Object value) {
    InsnList il = getInstructions();
    for (AbstractInsnNode ain : il.toArray()) {
      if (ain.getType() == AbstractInsnNode.FIELD_INSN) {
        FieldInsnNode fieldIn = (FieldInsnNode) ain;
        if (fieldIn.name.equals(fieldName)) {
          if (value instanceof InsnList) {
            il.insertBefore(fieldIn, (InsnList) value);
            il.remove(fieldIn);
          } else if (value instanceof AbstractInsnNode) {
            il.set(fieldIn, (AbstractInsnNode) value);
          } else if (value instanceof Number) {
            il.set(fieldIn, Instructions.numberPush((Number) value));
          } else if (value instanceof String) {
            String stringValue = (String) value;
            il.insertBefore(fieldIn, Instructions.safeStringPush(stringValue));
            il.remove(fieldIn);
          }
        }
      }
    }
  }

  public <T> T findByOpcode(int opcode) {
    for (AbstractInsnNode ain : getInstructions()) {
      if (ain.getOpcode() == opcode) {
        return (T) ain;
      }
    }
    return null;
  }

  public int countPredicate(Predicate<AbstractInsnNode> ain) {
    int count = 0;
    for (AbstractInsnNode node : getInstructions()) {
      if (ain.test(node)) {
        count++;
      }
    }
    return count;
  }

  public void removeDebugInformation() {
    MethodNode node = bMethod;
    node.localVariables = null;
    node.signature = null;
    node.exceptions = null;
  }

  /**
   * Iterates over all field method and invokedynamic references (except array methods).
   *
   * @param consumer the consumer to accept the references
   */
  public void iterateOverReferences(Consumer<Reference> consumer) {
    for (AbstractInsnNode ain : getInstructions()) {
      switch (ain.getType()) {
        case AbstractInsnNode.FIELD_INSN -> {
          FieldInsnNode fin = (FieldInsnNode) ain;
          // we do not need memberLinkFactory.findMember here, no field has multiple containers
          consumer.accept(Reference.of(fin));
        }
        case AbstractInsnNode.METHOD_INSN -> {
          MethodInsnNode min = (MethodInsnNode) ain;
          if (min.owner.startsWith("[")) {
            // array methods are ignored
            break;
          }
          consumer.accept(Reference.of(min));
        }
        case AbstractInsnNode.INVOKE_DYNAMIC_INSN -> {
          InvokeDynamicInsnNode idin = (InvokeDynamicInsnNode) ain;
          if (idin.bsmArgs != null) {
            for (Object bsmArg : idin.bsmArgs) {
              if (bsmArg instanceof Handle) {
                Handle handle = (Handle) bsmArg;
                consumer.accept(Reference.of(handle));
              } else if(bsmArg instanceof Type) {
                acceptType(consumer, (Type) bsmArg);
              }
            }
          }
          if (idin.bsm != null) {
            consumer.accept(Reference.of(idin.bsm));
          }
        }
        case AbstractInsnNode.LDC_INSN -> {
          Object cst = ((LdcInsnNode) ain).cst;
          if(cst instanceof Type) {
            acceptType(consumer, (Type) cst);
          } else if(cst instanceof Handle) {
            consumer.accept(Reference.of((Handle) cst));
          }
        }
      }
    }
  }

  private static void acceptType(Consumer<Reference> consumer, Type cst) {
      if (cst.getSort() == Type.OBJECT || cst.getSort() == Type.ARRAY) {
      consumer.accept(Reference.of(cst));
    } else if(cst.getSort() == Type.METHOD) {
      consumer.accept(Reference.of(cst.getReturnType()));
      for (Type argumentType : cst.getArgumentTypes()) {
        consumer.accept(Reference.of(argumentType));
      }
    }
  }
}
