package net.branchlock.annotations.properties;


/**
 * This annotation indicates that a method depends on runtime speed (i.e. a small code change could have a big impact on performance).
 * This method will be obfuscated in a way that does not affect performance. Be aware that this means less protection on this method.
 * This is useful for methods that are called very often, e.g. in loops.
 * You do not have to mark equals() and hashCode() methods as performance sensitive, they are already marked as such.
 */
@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
public @interface PerformanceSensitive {
}