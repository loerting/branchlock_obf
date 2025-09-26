package net.branchlock.task.driver.passthrough;

import net.branchlock.Branchlock;
import net.branchlock.config.Config;
import net.branchlock.config.exception.ConfigException;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMember;
import net.branchlock.structure.provider.DataProvider;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Excludes everything that is specified by the user in the configuration.
 * First, all general exclusion settings are applied to the stream, then the
 * task-specific exclusion settings are applied.
 */
public abstract class ConfigExclusionPassThrough<T> implements IPassThrough<T> {
  protected final Config config;
  protected final DataProvider dataProvider;

  protected ConfigExclusionPassThrough(Config config, DataProvider dataProvider) {
    this.config = Objects.requireNonNull(config);
    this.dataProvider = Objects.requireNonNull(dataProvider);
  }

  public static String toRegex(String token) {
    // Replace all occurrences of "$" with "\$".
    String regex = token.replace("$", "\\$");

    // * should not be preceded or succeeded by another *. (single star case)
    regex = regex.replaceAll("(?<!\\*)\\*(?!\\*)", "[^/]*");


    // replace all occurrences of "**" with ".*" if it is not preceded by a "]", which could be introduced by the previous case.
    regex = regex.replaceAll("(?<!])\\*\\*", ".*");


    // Add regular expression anchors to ensure a full match
    regex = "^" + regex + "$";
    return regex;
  }

  /**
   * Detects evil regular expressions and throws an exception if one is found.
   */
  private static void detectEvilRegex(String token) {
    char[] evilRegexChars = {'(', ')', '[', ']', '{', '}', '|', '\\', '^', '+', '?'}; // $ and * are allowed
    for (char evilRegexChar : evilRegexChars) {
      if (token.contains(String.valueOf(evilRegexChar))) {
        throw new ConfigException("The token \"" + token + "\" contains the regex character \"" + evilRegexChar + "\". This is not allowed.");
      }
    }
  }

  protected static <T> void warnIncludedNotInExcluded(Set<T> excluded, Set<T> included) {
    // check if there are any methods that are both excluded and included
    long count = included.stream().filter(m -> !excluded.contains(m)).count();
    if (count > 0) {
      Branchlock.LOGGER.warning("There are {} members that are included but not excluded. Possible error in config file.", count);
    }
  }

  protected Collection<String> getExcludedTokens() {
    Collection<String> exclude = config.getMultiValue("exclude");
    if (exclude == null)
      return Set.of();

    return exclude.stream()
      .map(String::trim)
      .map(s -> s.replace(".", "/"))
      .filter(s -> !s.isEmpty())
      .collect(Collectors.toList());
  }

  protected Collection<String> getIncludedTokens() {
    Collection<String> include = config.getMultiValue("include");
    if (include == null)
      return Set.of();
    return include.stream()
      .map(String::trim)
      .map(s -> s.replace(".", "/"))
      .filter(s -> !s.isEmpty())
      .collect(Collectors.toList());
  }

  protected boolean isMemberToken(String token) {
    return token.contains("#");
  }

  protected boolean matchesAnyToken(Collection<String> tokens, BMember bMember) {
    BMember owner = (bMember.hasOwner() ? bMember.getOwner() : bMember);
    String ownerName = owner.getOriginalName();

    String memberName = bMember.getOriginalName(); // TODO implement original name for BMember
    String childElement = ownerName + "#" + memberName;
    for (String token : tokens) {
      token = replaceMemberAbbreviations(token.trim());

      boolean matchAllSubClasses = false;
      if (token.startsWith("?")) {
        // Excluding classes that extend a certain class
        token = token.substring(1);
        matchAllSubClasses = true;
      }

      String annotation;
      if (token.contains("@")) {
        annotation = token.substring(token.indexOf("@") + 1);
        token = token.substring(0, token.indexOf("@"));

        // invalid annotation token
        if (annotation.contains("*") || annotation.contains("#")) return false;
      } else {
        annotation = null;
      }

      detectEvilRegex(token);
      String regex = toRegex(token);

      if (isMemberToken(token)) {
        if (bMember instanceof BClass)
          throw new IllegalArgumentException("Class matched with member token.");

        if (matchAllSubClasses) {
          if (!(owner instanceof BClass)) throw new IllegalArgumentException("Member token with ? must be a class token.");
          // member and subclass combination

          String[] splitToken = token.split("#");
          String tokenClassNameRegex = toRegex(splitToken[0]);
          String tokenMemberNameRegex = toRegex(splitToken[1]);

          if (((BClass) owner).matchesOrParent(bc -> bc.getOriginalName().matches(tokenClassNameRegex)
            && bMember.getOriginalName().matches(tokenMemberNameRegex) && checkAnnotation(bMember, annotation))) {
            return true;
          }
        } else {
          if (childElement.matches(regex) && checkAnnotation(bMember, annotation))
            return true;
        }
      } else {
        // this is a class token.
        if (matchAllSubClasses) {
          if (!(owner instanceof BClass)) throw new IllegalArgumentException("Class token with ? must be a class token.");
          if (((BClass) owner).matchesOrParent(bc -> bc.getOriginalName().matches(regex) && checkAnnotation(bc, annotation))) {
            return true;
          }
        } else {
          if (ownerName.matches(regex) && checkAnnotation(owner, annotation))
            return true;
        }
      }
    }
    return false;
  }

  private String replaceMemberAbbreviations(String token) {
    if (!isMemberToken(token)) return token;

    String[] splitRegex = token.split("#");
    String memberName = switch (splitRegex[1]) {
      case "init", "ctor", "constructor", "const" -> "<init>";
      case "clinit", "classinit", "classinitializer", "static", "staticinit", "staticinitializer", "initializer" -> "<clinit>";
      case "" -> throw new ConfigException("Empty member name in token.");
      default -> splitRegex[1];
    };
    return splitRegex[0] + "#" + memberName;
  }

  private boolean checkAnnotation(BMember bMember, String annotation) {
    if (annotation == null) {
      return true;
    }
    return bMember.hasAnnotation(annotation);
  }
}
