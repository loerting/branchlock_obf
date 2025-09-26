package net.branchlock.task.implementation.references.calls;

import net.branchlock.commons.asm.Reference;
import org.objectweb.asm.Type;

import java.util.Objects;

public class InvokerMethod {

  public final Type invokerDescriptor;
  public final boolean isStatic;
  public final Reference.RefType refType;

  /**
   * Not used in equals and hashCode.
   * Used to make it easier to generate the invoker method. (Would be possible without it, but would be more complicated.)
   * Not relevant for the actual invoker method.
   */
  public final Reference sampleReference;

  public InvokerMethod(Type invokerDescriptor, boolean isStatic, Reference sampleReference) {
    this.invokerDescriptor = invokerDescriptor;
    this.isStatic = isStatic;
    this.sampleReference = sampleReference;
    this.refType = sampleReference.getType();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InvokerMethod that = (InvokerMethod) o;
    return isStatic == that.isStatic && Objects.equals(invokerDescriptor, that.invokerDescriptor) && refType == that.refType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(invokerDescriptor, isStatic, refType);
  }
}
