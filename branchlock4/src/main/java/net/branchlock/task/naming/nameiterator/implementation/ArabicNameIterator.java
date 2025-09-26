package net.branchlock.task.naming.nameiterator.implementation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ArabicNameIterator extends AlphabetIterator {
  public ArabicNameIterator() {
    super(IntStream.range(0, Character.MAX_VALUE).filter(Character::isJavaIdentifierStart)
      .filter(c -> Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ARABIC));
  }

  private static final List<Byte> RTL_DIRECTIONALITY = List.of(
   // Character.DIRECTIONALITY_RIGHT_TO_LEFT,
    Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
    //Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING,
     //Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE
  );

  public static void main(String[] args) {
    Map<Character.UnicodeBlock, List<Character>> blocks = IntStream.range(0, Character.MAX_VALUE)
      .filter(Character::isJavaIdentifierStart)
      .filter( c -> RTL_DIRECTIONALITY.contains(Character.getDirectionality(c)))
      .mapToObj(i -> (char) i)
      .collect(Collectors.groupingBy(Character.UnicodeBlock::of));

    blocks.forEach((key, value) -> {
        System.out.println(key);
      StringBuilder builder = new StringBuilder();
      for (char c : value) {
        builder.append(c).append("\n").append("\\u").append(String.format("%04x", (int) c)).append("\n");
      }
      System.out.println(builder);
    });
  }
}
