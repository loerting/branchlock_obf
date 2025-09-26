package net.branchlock.annotations.retention;

/**
 * This annotation ensures that the member is not removed. The code, signature and name can still be changed.
 * It can be used on classes, methods and fields.
 */
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.METHOD})
public @interface RetainPresence {
}