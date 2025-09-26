package net.branchlock.layout.removal;

public class MissingMemberError extends Error {
  public static final MissingMemberError ERROR = new MissingMemberError();

  public MissingMemberError() {
    super("The called class or class member was removed. Please contact the developer of this application to exclude the referenced member.");
  }
}
