package net.branchlock.structure.io;

import net.branchlock.structure.BMember;

import java.util.HashSet;
import java.util.Set;

public class MissingReference {
  private final String className;
  private final Set<BMember> referencedFrom;

  public MissingReference(String className) {
    this.className = className;
    this.referencedFrom = new HashSet<>();
  }

  public String getClassName() {
    return className;
  }

  public Set<BMember> getReferencedFrom() {
    return referencedFrom;
  }
}
