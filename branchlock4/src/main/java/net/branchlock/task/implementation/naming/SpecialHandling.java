package net.branchlock.task.implementation.naming;

public enum SpecialHandling {
  KEEP_PACKAGE, KEEP_NAME, KEEP_NAME_AND_MEMBERS; // the more right will override

  public static SpecialHandling lowest(boolean pckgExclusion, boolean nameExclusion, boolean fullExclusion) {
    if (fullExclusion)
      return KEEP_NAME_AND_MEMBERS;
    if (nameExclusion)
      return KEEP_NAME;
    if (pckgExclusion)
      return KEEP_PACKAGE;
    return null;
  }

  public static SpecialHandling min(SpecialHandling sh1, SpecialHandling sh2) {
    return sh1.ordinal() > sh2.ordinal() ? sh1 : sh2;
  }

}
