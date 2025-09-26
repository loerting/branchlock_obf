package net.branchlock.testing.exclusion;


import net.branchlock.task.driver.passthrough.ConfigExclusionPassThrough;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the exclusion token to regex conversion.
 */
public class ExclusionTokenTest {


  @Test
  public void testNormalMatches() {
    assertTrue(matches("foo/bar/Class", "foo/bar/Class"));
    assertFalse(matches("foo/bar/Class", "foo/bar/Class$Inner"));

    assertTrue(matches("foo/bar/Class$Inner", "foo/bar/Class$Inner"));

    assertTrue(matches("foo/*/Class", "foo/bar/Class"));
    assertTrue(matches("foo/*/Class", "foo/baz/Class"));
    assertFalse(matches("foo/*/Class", "foo/bar/Class$Inner"));
    assertFalse(matches("foo/*/Class", "foo/bar/bar/Class"));
    assertFalse(matches("foo/*/Class", "foo/Class"));

    assertTrue(matches("foo/**/Class", "foo/bar/Class"));
    assertTrue(matches("foo/**/Class", "foo/bar/baz/Class"));
    assertFalse(matches("foo/**/Class", "foo/bar/Class$Inner"));
    assertFalse(matches("foo/**/Class", "foo/Class"));

    assertTrue(matches("foo/**", "foo/bar/Class"));
    assertTrue(matches("foo/**", "foo/bar/baz/Class"));

    assertTrue(matches("foo/bar/Class*", "foo/bar/Class"));
    assertTrue(matches("foo/bar/Class*", "foo/bar/Class2"));
    assertTrue(matches("foo/bar/Class*", "foo/bar/Class$Inner"));
    assertFalse(matches("foo/bar/Class*", "foo/bar/Claz"));

    assertTrue(matches("foo/bar/Class**", "foo/bar/Class"));
    assertTrue(matches("foo/bar/Class**", "foo/bar/Class2"));
    assertTrue(matches("foo/bar/Class**", "foo/bar/Class$Inner"));
    assertTrue(matches("foo/bar/Class**", "foo/bar/Class$Inner2"));
    assertFalse(matches("foo/bar/Class**", "foo/bar/Claz"));

    assertFalse(matches("foo/bar/Class#method*", "foo/bar/Class"));
  }

  private boolean matches(String token, String... classes) {
    String regex = ConfigExclusionPassThrough.toRegex(token);
    return Arrays.stream(classes).allMatch(c -> c.matches(regex));
  }

}
