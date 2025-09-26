package net.branchlock.annotations.properties;


/**
 * This annotation indicates that a method is an entry point (e.g. API).
 * Useful if an entry point is not called directly, but dynamically, e.g. through reflection.
 * This ensures that the Trimmer task will not remove the method, including all methods called by it.
 */
@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
public @interface EntryPoint {
}