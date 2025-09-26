package net.branchlock.structure;

import net.branchlock.commons.asm.Access;
import net.branchlock.commons.asm.Annotations;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.Objects;

public class BField extends FieldNode implements BMember, Opcodes {

  private final BClass owner;

  public BField(BClass owner, int access, String name, String descriptor, String signature, Object value) {
    super(Opcodes.ASM9, access, name, descriptor, signature, value);
    this.owner = Objects.requireNonNull(owner);
  }

  @Override
  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    String oldIdentifier = getIdentifier();
    this.name = name;
    owner.fields.observeIdentifierChanged(this, oldIdentifier);
  }

  @Override
  public String getDescriptor() {
    return this.desc;
  }

  public void setDescriptor(String desc) {
    String oldIdentifier = getIdentifier();
    this.desc = desc;
    owner.fields.observeIdentifierChanged(this, oldIdentifier);
  }

  @Override
  public boolean isLocal() {
    return owner.isLocal();
  }

  public boolean isStatic() {
    return (this.access & ACC_STATIC) != 0;
  }

  @Override
  public boolean hasOwner() {
    return true;
  }

  @Override
  public BClass getOwner() {
    return owner;
  }

  @Override
  public boolean hasAnnotation(String annotation) {
    return Annotations.has(annotation, this.visibleAnnotations, this.invisibleAnnotations);
  }

  @Override
  public int getAccess() {
    return this.access;
  }

  @Override
  public void setAccess(int access) {
    if(!isLocal()) throw new IllegalStateException("Cannot change access of non-local field");
    this.access = access;
  }

  public boolean hasAccess(int access) {
    return (this.access & access) == access;
  }

  public String getIdentifier() {
    return this.name + this.desc;
  }

  @Override
  public String getOriginalName() {
    // TODO implement original name tracking
    return this.name;
  }

  public FieldInsnNode createPut() {
    return new FieldInsnNode(isStatic() ? PUTSTATIC : PUTFIELD, owner.name, this.name, this.desc);
  }

  public FieldInsnNode createGet() {
    return new FieldInsnNode(isStatic() ? GETSTATIC : GETFIELD, owner.name, this.name, this.desc);
  }

  public boolean isNameChangeable() {
    return isLocal() &&
      !hasAnnotation("RetainSignature") &&
      !"serialVersionUID".equals(this.name) &&
      !Access.isVolatile(this.access);
  }

  @Override
  public String toString() {
    return owner.name + "." + getIdentifier();
  }

  public boolean isAccessibleFrom(BClass bc) {
    if(!getOwner().isAccessibleFrom(bc)) return false;
    if(hasAccess(ACC_PUBLIC)) return true;
    if(hasAccess(ACC_PRIVATE)) return bc == getOwner();
    boolean samePackage = bc.getPackage().equals(getOwner().getPackage());
    if(hasAccess(ACC_PROTECTED)) return samePackage || bc.isAssertableTo(getOwner().getName());
    return samePackage;
  }
}
