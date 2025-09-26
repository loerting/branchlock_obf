package net.branchlock.task.naming.nameiterator.implementation;

import net.branchlock.Branchlock;
import net.branchlock.task.naming.nameiterator.INameIterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeywordNameIterator implements INameIterator {
  private final List<String> javaKeywords = new ArrayList<>(List.of(
    // ReservedKeyword
    "abstract", "continue", "for", "new", "switch",
    "assert", "default", "if", "package", "synchronized",
    "boolean", "do", "goto", "private", "this",
    "break", "double", "implements", "protected", "throw",
    "byte", "else", "import", "public", "throws",
    "case", "enum", "instanceof", "return", "transient",
    "catch", "extends", "int", "short", "try",
    "char", "final", "interface", "static", "void",
    "class", "finally", "long", "strictfp", "volatile",
    "const", "float", "native", "super", "while",
    //"_",
    // ContextualKeyword
    "exports", "opens", "requires", "uses",
    "module", "permits", "sealed", "var",
    "non-sealed", "provides", "to", "with",
    "open", "record", "transitive", "yield",
    // Other
    "null", "true", "false"
  ));
  private int index = 0;
  private final AlphabeticNameIterator alphabeticNameIterator = new AlphabeticNameIterator();
  private String suffix = "";

  public KeywordNameIterator() {
    Collections.shuffle(javaKeywords, Branchlock.R);
  }

  @Override
  public String next() {
    if (index >= javaKeywords.size()) {
      index = 0;
      suffix = alphabeticNameIterator.next();
    }
    return javaKeywords.get(index++) + suffix;
  }

  @Override
  public void reset() {
    index = 0;
    suffix = "";
    alphabeticNameIterator.reset();
  }
}
