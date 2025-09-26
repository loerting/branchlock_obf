package net.branchlock.annotations.properties;


/**
 * This annotation indicates that a method is sensitive to the caller. Calls to this method will not be encrypted.
 */
@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
public @interface CallerSensitive {
}