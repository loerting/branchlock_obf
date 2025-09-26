package net.branchlock.commons.asm;

public enum MajorVersion {
  JAVA_5(49), JAVA_6(50), JAVA_7(51), JAVA_8(52),
  JAVA_9(53), JAVA_10(54), JAVA_11(55), JAVA_12(56),
  JAVA_13(57), JAVA_14(58), JAVA_15(59), JAVA_16(60),
  JAVA_17(61), JAVA_18(62), JAVA_19(63), JAVA_20(64),
  JAVA_21(65), JAVA_22(66), JAVA_23(67), JAVA_24(68);

  private final int code;

  MajorVersion(int code) {
    this.code = code;
  }

  public static MajorVersion fromCode(int code) {
    for (MajorVersion version : values()) {
      if (version.getCode() == code) {
        return version;
      }
    }
    return null;
  }

  public static String codeToVersion(int code) {
    MajorVersion majorVersion = fromCode(code);
    if (majorVersion == null) {
      return "Unknown";
    }
    return majorVersion.name().replace("_", " ") + " (code " + majorVersion.getCode() + ")";
  }

  public int getCode() {
    return code;
  }
}
