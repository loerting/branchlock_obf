package net.branchlock.commons.string;

import net.branchlock.Branchlock;

import java.nio.charset.StandardCharsets;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

public class StringUtils {
  public static boolean isValidUTF8(String s) {
    return s.hashCode() == new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8).hashCode();
  }

  public static List<String> splitEqually(String text, int size) {
    List<String> ret = new ArrayList<>((text.length() + size - 1) / size);

    for (int start = 0; start < text.length(); start += size) {
      ret.add(text.substring(start, Math.min(text.length(), start + size)));
    }
    return ret;
  }

  public static String toNumInUnits(long bytes) {
    if (-1000 < bytes && bytes < 1000) {
      return bytes + " B";
    }
    CharacterIterator ci = new StringCharacterIterator("kMGTPE");
    while (bytes <= -999_950 || bytes >= 999_950) {
      bytes /= 1000;
      ci.next();
    }
    return String.format("%.2f %cB", bytes / 1000.0, ci.current());
  }

  public static String generateAlphanumericString(int length) {
    final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(ALPHABET.charAt((int) (Branchlock.R.nextDouble() * ALPHABET.length())));
    }
    return sb.toString();
  }

  public static String swap(String string, int a, int b) {
    char[] chars = string.toCharArray();
    char temp = chars[a];
    chars[a] = chars[b];
    chars[b] = temp;
    return new String(chars);
  }
}
