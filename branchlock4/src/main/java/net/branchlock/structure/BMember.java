package net.branchlock.structure;

public interface BMember {
  String getName();

  String getDescriptor();

  /**
   * @return true if the member is present in the input jar file (not in the library inputs).
   */
  boolean isLocal();

  boolean hasOwner();

  BMember getOwner();

  boolean hasAnnotation(String annotation);

  int getAccess();

  void setAccess(int access);

  String getIdentifier();

  String getOriginalName();
}
