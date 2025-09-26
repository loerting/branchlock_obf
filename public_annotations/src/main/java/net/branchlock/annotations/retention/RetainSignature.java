package net.branchlock.annotations.retention;


/**
 * This annotation is used to ensure that the signature (name and descriptor) of a member is not changed.
 * In case of a method, it also affects all overwritten and overriding methods. Overloaded methods are not affected.
 */
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.METHOD})
public @interface RetainSignature {
}