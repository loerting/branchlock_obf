package net.branchlock.commons.os;

import net.branchlock.commons.java.Pair;

import java.util.ArrayList;

public class Bits {

  public static final char[] VALID_IDENTIFIER_CHARS;

  static {
    VALID_IDENTIFIER_CHARS = new char[Short.MAX_VALUE + 1];
    int i = 0;

    for (int x = 5121; x < 44032 + 11172; x++) {
      if (x == 5121 + 620) {
        x = 19968;
      } else if (x == 19968 + 20976) {
        x = 44032;
      }
      Bits.VALID_IDENTIFIER_CHARS[i++] = (char) x;
    }

    /*
     for (char c : Bits.validIdentifierChars) {
     if (!Character.isJavaIdentifierPart(c))
     throw new RuntimeException("Should actually be an identifier: " + (int) c);
     }
     */
  }

  public static Pair<Integer, Integer> shiftRange(long number) {
    int first = 0;
    while ((number >>> first) << first == number && first < Long.SIZE)
      first++;

    int second = 0;
    while ((number << second) >>> second == number && second < Long.SIZE)
      second++;
    return new Pair<>(first, second);
  }

  public static Pair<Integer, Integer> shiftRange(int number) {
    int first = 0;
    while ((number >>> first) << first == number && first < Integer.SIZE)
      first++;

    int second = 0;
    while ((number << second) >>> second == number && second < Integer.SIZE)
      second++;
    return new Pair<>(first, second);
  }

  public static Pair<Integer, Integer> shiftCountForZero(long number) {
    int first = 0;
    while ((number << first) != 0 && first < Long.SIZE)
      first++;

    int second = 0;
    while ((number >>> second) != 0 && second < Long.SIZE)
      second++;
    return new Pair<>(first, second);
  }


  public static Pair<Integer, Integer> shiftCountForZero(int number) {
    int first = 0;
    while ((number << first) != 0 && first < Integer.SIZE)
      first++;

    int second = 0;
    while ((number >>> second) != 0 && second < Integer.SIZE)
      second++;
    return new Pair<>(first, second);
  }

  public static String encryptIntsToIdentifierString(int... ints) {
    StringBuilder sb = new StringBuilder();
    for (int integer : ints) {
      for (int i = 0; i < 32; i += 16) {
        char bite = (char) ((integer >> i) & 0xffffL);
        if (bite <= Short.MAX_VALUE) {
          sb.append(VALID_IDENTIFIER_CHARS[bite]);
        } else {
          sb.append('\0');
          sb.append(VALID_IDENTIFIER_CHARS[bite - Short.MAX_VALUE - 1]);
        }
      }
    }
    return sb.toString();
  }

  public static ArrayList<Integer> decryptIntsFromIdentifierString(String str) {
    ArrayList<Integer> integers = new ArrayList<>();

    int firstPart = 0;
    boolean hasFirstBit = false;
    boolean nextIsUpper = false;
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (c == '\0') {
        nextIsUpper = true;
        continue;
      }

      int index = 0;
      for (int j = 0; j < VALID_IDENTIFIER_CHARS.length; j++) {
        if (c == VALID_IDENTIFIER_CHARS[j]) {
          index = j;
          break;
        }
      }
      if (nextIsUpper) {
        index += Short.MAX_VALUE + 1;
        nextIsUpper = false;
      }
      if (!hasFirstBit) {
        hasFirstBit = true;
        firstPart = index;
      } else {
        integers.add(firstPart | (index << 16));
        hasFirstBit = false;
      }
    }
    return integers;
  }


  public static int realMod(int a, int b) {
    return (a % b + b) % b;
  }

  public static Pair<Integer, Integer> getOrAndDifference(int from, int to) {
    // iterate over bits
    int or = 0;
    int and = 0;
    for (int i = 0; i < Integer.SIZE; i++) {
      int bit = 1 << i;
      int fromBit = from & bit;
      int toBit = to & bit;

      if (fromBit == 0 && toBit != 0) {
        or |= bit;
      } else if (fromBit != 0 && toBit == 0) {
        and |= bit;
      }
    }
    and = ~and;

    // remove unnecessary one bits from and mask
    and &= (Integer.highestOneBit(from | to) << 1) - 1;

    return new Pair<>(or, and);
  }

}
