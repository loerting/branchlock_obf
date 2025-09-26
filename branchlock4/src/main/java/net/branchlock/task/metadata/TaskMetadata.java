package net.branchlock.task.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TaskMetadata {
  String name() default "Unknown task";

  Level priority() default Level.SIXTH;

  boolean androidCompatible() default true;

  boolean desktopCompatible() default true;

  PerformanceCost performanceCost() default PerformanceCost.CLOSE_TO_ZERO;


  /**
   * Can the task be used with the demo version?
   *
   * Deprecated, as demo version allows all tasks to work as from 11.02.25
   */
  @Deprecated()
  boolean demoApplicable() default true;

  String[] ids() default {};

  enum Level {
    FIRST, SECOND, THIRD, FOURTH, FIFTH, SIXTH, SEVENTH, EIGHTH, LAST
  }

  enum PerformanceCost {
    CLOSE_TO_ZERO(1.01), MINIMAL(1.05), NOTICEABLE(1.2);

    public final double multiplier;

    PerformanceCost(double multiplier) {
      this.multiplier = multiplier;
    }
  }
}
