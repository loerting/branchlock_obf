package net.branchlock.commons.asm;

public class ASMLimits {

  public static final int MAX_METHOD_SIZE = Short.MAX_VALUE - 1;
  public static final int MAX_UTF8_BYTES_SIZE = Short.MAX_VALUE * 2 - 1;

  public static boolean isUTF8TooLarge(String s) {
    int length = s.length();
    if (length > MAX_UTF8_BYTES_SIZE)
      return true;
    int byteLength = 0;
    for (int i = 0; i < length; ++i) {
      char charValue = s.charAt(i);
      if (charValue >= 0x0001 && charValue <= 0x007F) {
        byteLength++;
      } else if (charValue <= 0x07FF) {
        byteLength += 2;
      } else {
        byteLength += 3;
      }
    }
    return byteLength > MAX_UTF8_BYTES_SIZE;
  }
}
