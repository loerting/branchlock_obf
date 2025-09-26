package net.branchlock.task.naming.nameiterator.implementation;

import net.branchlock.Branchlock;
import net.branchlock.task.naming.nameiterator.INameIterator;

import java.io.StringWriter;
import java.util.Random;
import java.util.stream.IntStream;

public class AlphabetIterator implements INameIterator {
  private final String alphabet;
  private final int alphabetLength;
  private int index = 0;
  private int length = 1;

  public AlphabetIterator(String alphabet) {
    this.alphabet = shuffle(alphabet);
    this.alphabetLength = alphabet.length();
    if (alphabetLength < 3)
      throw new IllegalArgumentException("Alphabet must be at least 3 characters long.");
  }

  public AlphabetIterator(char[] alphabet) {
    this(new String(alphabet));
  }

  public AlphabetIterator(IntStream charStream) {
    StringWriter sw = new StringWriter();
    charStream.forEach(sw::write);
    String result = shuffle(sw.toString());
    this.alphabet = result.substring(0, Math.min(result.length(), 512));
    this.alphabetLength = alphabet.length();
  }

  private static String shuffle(String string) {
    Random random = Branchlock.R;
    char[] chars = string.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      int randomIndex = random.nextInt(chars.length);
      char temp = chars[i];
      chars[i] = chars[randomIndex];
      chars[randomIndex] = temp;
    }
    return new String(chars);
  }

  /**
   * Generates the next possible name. Always starts with the first letter of the alphabet.
   * Goes through all possible combinations of the alphabet.
   */
  @Override
  public String next() {
    StringBuilder sb = new StringBuilder();
    int currentIndex = index;
    for (int i = 0; i < length; i++) {
      int currentLetterIndex = currentIndex % alphabetLength;
      sb.append(alphabet.charAt(currentLetterIndex));
      currentIndex = currentIndex / alphabetLength;
    }
    index++;
    if (index == Math.pow(alphabetLength, length)) {
      index = 0;
      length++;
    }
    return sb.toString();
  }

  @Override
  public void reset() {
    index = 0;
    length = 1;
  }
}
