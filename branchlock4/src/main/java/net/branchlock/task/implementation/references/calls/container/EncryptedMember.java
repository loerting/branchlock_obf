package net.branchlock.task.implementation.references.calls.container;


import net.branchlock.commons.asm.Reference;

import java.util.Objects;

public class EncryptedMember {
  public long hash;
  public Reference ref;
  public boolean isStatic;

  public EncryptedMember(long hash, Reference ref, boolean isStatic) {
    this.hash = hash;
    this.ref = ref;
    this.isStatic = isStatic;
  }

  /**
   * If the ref is set or get doesn't matter in equals. Using Reference#equalsItem.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EncryptedMember that = (EncryptedMember) o;
    return hash == that.hash && isStatic == that.isStatic && ref.equals(that.ref);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hash, ref, isStatic);
  }

  public boolean equalsNoType(EncryptedMember that) {
    return hash == that.hash && isStatic == that.isStatic && ref.equalsItem(that.ref);
  }
}
