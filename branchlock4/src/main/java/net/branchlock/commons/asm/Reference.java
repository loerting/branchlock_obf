package net.branchlock.commons.asm;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Objects;

/**
 * Class representing a reference to a method or field.
 * I
 */
public class Reference implements Opcodes {
  public String owner;
  public String name;
  public String desc;
  public RefType type;

  private Reference(String owner, String name, String desc, RefType type) {
    this.owner = owner;
    this.name = name;
    this.desc = desc;
    if (desc != null && desc.startsWith("(") != type.isMethod()) {
      throw new IllegalArgumentException("desc and type mismatch");
    }
    this.type = type;
  }

  public static Reference of(MethodInsnNode min) {
    return new Reference(min.owner, min.name, min.desc, RefType.METHOD_INVOKE);
  }

  public static Reference of(FieldInsnNode fin) {
    RefType type = fin.getOpcode() == PUTFIELD || fin.getOpcode() == PUTSTATIC ? RefType.FIELD_SET : RefType.FIELD_GET;
    return new Reference(fin.owner, fin.name, fin.desc, type);
  }

  public static Reference of(String owner, String name, String desc, RefType type) {
    return new Reference(owner, name, desc, type);
  }

  public static Reference of(Handle handle) {
    RefType type = switch (handle.getTag()) {
      case H_GETFIELD, H_GETSTATIC -> RefType.FIELD_GET;
      case H_PUTFIELD, H_PUTSTATIC -> RefType.FIELD_SET;
      case H_INVOKEVIRTUAL, H_INVOKESTATIC, H_INVOKESPECIAL, H_NEWINVOKESPECIAL, H_INVOKEINTERFACE -> RefType.METHOD_INVOKE;
      default -> throw new IllegalArgumentException("Unknown handle tag: " + handle.getTag());
    };
    return new Reference(handle.getOwner(), handle.getName(), handle.getDesc(), type);
  }

  public static Reference of(Type cst) {
    return new Reference(cst.getInternalName(), null, null, RefType.CLASS_TYPE);
  }

  public RefType getType() {
    return type;
  }

  public AbstractInsnNode createStaticInstruction() {
    if (type.isMethod()) {
      return new MethodInsnNode(INVOKESTATIC, owner, name, desc, false);
    } else {
      return new FieldInsnNode(type == RefType.FIELD_GET ? GETSTATIC : PUTSTATIC, owner, name, desc);
    }
  }

  /**
   * Creates a new instruction node for this reference. Interface methods are not supported.
   */
  public AbstractInsnNode createInstruction() {
    if (type.isMethod()) {
      return new MethodInsnNode(name.equals("<init>") ? INVOKESPECIAL : INVOKEVIRTUAL, owner, name, desc, false);
    } else {
      return new FieldInsnNode(type == RefType.FIELD_GET ? GETFIELD : PUTFIELD, owner, name, desc);
    }
  }

  public Reference copyWithNewType(RefType newType) {
    return new Reference(owner, name, desc, newType);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Reference reference = (Reference) o;
    return Objects.equals(owner, reference.owner) && Objects.equals(name, reference.name) && Objects.equals(desc, reference.desc) && type == reference.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner, name, desc, type);
  }

  public boolean equalsItem(Reference ref) {
    return Objects.equals(owner, ref.owner) && Objects.equals(name, ref.name) && Objects.equals(desc, ref.desc);
  }

  public boolean isReturner() {
    if (type == RefType.FIELD_SET)
      return false;
    if (type == RefType.CLASS_TYPE)
      return true; // ldc instruction which returns the class type
    return !desc.endsWith("V");
  }

  @Override
  public String toString() {
    return "Reference{" +
      "owner='" + owner + '\'' +
      ", name='" + name + '\'' +
      ", desc='" + desc + '\'' +
      ", type=" + type +
      '}';
  }

  public enum RefType {
    METHOD_INVOKE, FIELD_GET, FIELD_SET, CLASS_TYPE;

    public boolean isMethod() {
      return this == METHOD_INVOKE;
    }

    public boolean isField() {
      return this == FIELD_GET || this == FIELD_SET;
    }
  }
}
