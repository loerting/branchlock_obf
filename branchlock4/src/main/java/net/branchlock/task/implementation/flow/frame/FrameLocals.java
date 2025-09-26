package net.branchlock.task.implementation.flow.frame;

import org.objectweb.asm.tree.analysis.BasicValue;

import java.util.Arrays;

public class FrameLocals {
  private final BasicValue[] locals;
  private final int hash;

  public FrameLocals(BasicValue[] locals) {
    this.locals = locals;
    this.hash = Arrays.hashCode(locals);
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof FrameLocals)) {
      return false;
    }
    FrameLocals other = (FrameLocals) obj;
    return Arrays.equals(locals, other.locals);
  }

  @Override
  public String toString() {
    return Arrays.toString(locals);
  }

  public boolean containsTop() {
    for (BasicValue local : locals) {
      if (local == BasicValue.UNINITIALIZED_VALUE)
        return true;
    }
    return false;
  }
}