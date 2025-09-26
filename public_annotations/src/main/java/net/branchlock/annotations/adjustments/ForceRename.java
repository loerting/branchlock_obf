package net.branchlock.annotations.adjustments;

/**
 * Forcefully rename a member with Renamer.
 * This ignores automatic reflection detection, exclusion / inclusion and other annotations.
 */
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.METHOD})
public @interface ForceRename {
}
