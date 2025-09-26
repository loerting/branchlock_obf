package net.branchlock;


import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindNameFactories {
  public static void main(String[] args) {
    Stream<Character> allChars = Stream.iterate((char)0, c -> (char) (c + 1)).limit(Character.MAX_VALUE)
      .filter(Character::isJavaIdentifierStart);
    allChars.collect(Collectors.groupingBy(Character.UnicodeBlock::of)).forEach((block, chars) -> {
      System.out.println(block + ": " + chars.stream().map(String::valueOf).collect(Collectors.joining()));
    });
  }

}
