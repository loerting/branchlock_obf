package net.branchlock.layout.references;


import net.branchlock.commons.generics.Placeholder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

@SuppressWarnings("ALL")
public class ReflectionLookup {

  @Placeholder
  private static String MODIFIERS_FIELD_NAME;
  // don't make them final, the compiler inlines them
  @Placeholder
  private static int LOOKUP_CLASS_HASH;

  @Placeholder
  private static int RANDOM_XOR;


  private static Field modifiersField;

  static {
    try {
      modifiersField = Field.class.getDeclaredField(MODIFIERS_FIELD_NAME);
      modifiersField.setAccessible(true);
    } catch (Exception e) { // prior to newer java versions it was NoSuchFieldException, but with stricter policy we also have to avoid other exceptions.
    }
  }

  public static Method findMethod(long hash, Class<?> clazz) {
    hash ^= getCallerHash();
    int classHash = ((int) (hash)) & 0b11111111111111111111111111111110;
    int memberHash = (int) (hash >> 32);

    clazz = findParentClassRecursive(clazz, classHash);
    if (clazz == null) {
      throw new ClassCastException();
    }

    // we have to differentiate between static methods and instance methods.
    // Check JVM §15.12.4.4 on how Reflection invokes methods.
    // If the method is static, we have to find the first method with the given name and descriptor.
    // If the method is not static, we have to find the lowest method with the given name and descriptor and let Reflection invoke do the correct resolution.

    Method found;
    if((hash & 1L) != 0) {
      found = findFirstMethodRecursive(clazz, clazz, memberHash, true);
    } else {
      found = findLowestMethodRecursive(clazz, null, clazz, memberHash, false);
    }
    if (found != null) {
      try {
        // don't check for public modifier, as somehow even some fields with public modifiers need setAccessible
        found.setAccessible(true);
      } catch (Exception e) {
      }
    }
    return found;
  }

  public static Field findField(long hash, Class<?> clazz) throws Throwable {
    hash ^= getCallerHash();
    int classHash = ((int) (hash)) & 0xfffffffe;
    int memberHash = (int) (hash >> 32);
    clazz = findParentClassRecursive(clazz, classHash);
    if (clazz == null) {
      throw new ClassCastException();
    }
    Field found = findFirstFieldRecursive(clazz, clazz, memberHash, (hash & 1L) != 0);
    if (found != null) {
      try {
        // don't check for public modifier, as somehow even some fields with public modifiers need setAccessible
        found.setAccessible(true);

        if (modifiersField != null) {
          int modifiers = found.getModifiers();
          if ((modifiers & 16) != 0) {
            // field is final, remove modifier
            modifiersField.setInt(found, modifiers & ~16);
          }
        }
      } catch (Exception e) {
      }
    }
    return found;
  }

  private static long getCallerHash() {
    StackTraceElement[] trace = new Throwable().getStackTrace();
    int index = 0;
    while (trace[index++].getClassName().hashCode() == LOOKUP_CLASS_HASH) {
    }
    long l = trace[--index].getMethodName().hashCode() ^ RANDOM_XOR;
    return (l << 32) | (l & 0xffffffffL);
  }


  /**
   * This lookup is used for finding static methods.
   * @return
   */
  private static Method findFirstMethodRecursive(Class<?> ownerAttribute, Class<?> c, int hash,
                                                  boolean methodShouldBeStatic) {
    for (final Method m : c.getDeclaredMethods()) {
      if (!checkAccess(ownerAttribute, c, methodShouldBeStatic, m)) continue;
      if (getHashMethod(m.getName(), m.getReturnType().getName(), m.getParameterTypes()) == hash) {
        return m;
      }
    }
    // ordering is: superclass is prioritized over interface methods as in JVM §5.4.3.3.
    if (c.getSuperclass() != null) {
      final Method j = findFirstMethodRecursive(ownerAttribute, c.getSuperclass(), hash, methodShouldBeStatic);
      if (j != null) {
        return j;
      }
    }
    for (final Class<?> i : c.getInterfaces()) {
      final Method j = findFirstMethodRecursive(ownerAttribute, i, hash, methodShouldBeStatic);
      if (j != null) {
        return j;
      }
    }
    return null;
  }

  /**
   * This lookup is used for finding instance methods.
   * @return
   */
  private static Method findLowestMethodRecursive(Class<?> ownerAttribute, Method found, Class<?> c, int hash,
                                                  boolean methodShouldBeStatic) {
    for (final Method m : c.getDeclaredMethods()) {
      // we do not want to resolve private methods from top classes that cant access them. java will choose another
      // resolution mode if the resolution is impossible by access (it wont select the top overriding one, but
      // rather the private method)

      if (!checkAccess(ownerAttribute, c, methodShouldBeStatic, m)) continue;
      if (getHashMethod(m.getName(), m.getReturnType().getName(), m.getParameterTypes()) == hash) {
        found = m;
      }
    }
    // ordering is: superclass is prioritized over interface methods as in JVM §5.4.3.3.
    // note that we have to reverse the cases as the result is not returned instantly.
    for (final Class<?> i : c.getInterfaces()) {
      final Method j = findLowestMethodRecursive(ownerAttribute, found, i, hash, methodShouldBeStatic);
      if (j != null) {
        found = j;
      }
    }
    if (c.getSuperclass() != null) {
      final Method j = findLowestMethodRecursive(ownerAttribute, found, c.getSuperclass(), hash, methodShouldBeStatic);
      if (j != null) {
        found = j;
      }
    }
    return found;
  }

  private static boolean checkAccess(Class<?> ownerAttribute, Class<?> c, boolean methodShouldBeStatic, Method m) {
    int modifiers = m.getModifiers();
    if (((modifiers & 0x8) != 0x0) != methodShouldBeStatic)
      return false;
    if ((modifiers & 0x1) == 0x0) { //if not public
      if ((modifiers & 0x2) != 0x0) { // if private
        if (!ownerAttribute.equals(c))
          return false;
      } else if ((modifiers & 0x4) != 0x0) { // if protected
        if (!(Objects.equals(ownerAttribute.getPackage(), c.getPackage()) || c.isAssignableFrom(ownerAttribute)))
          return false;
      } else { // package access
        if (!Objects.equals(ownerAttribute.getPackage(), c.getPackage()))
          return false;
      }
    }
    return true;
  }

  private static Class<?> findParentClassRecursive(Class<?> c, int hash) {
    if ((c.getName().hashCode() & 0b11111111111111111111111111111110) == hash) {
      return c;
    }
    Class<?> clz;
    for (Class<?> i : c.getInterfaces()) {
      clz = findParentClassRecursive(i, hash);
      if (clz != null)
        return clz;
    }
    if (c.getSuperclass() != null) {
      clz = findParentClassRecursive(c.getSuperclass(), hash);
      if (clz != null)
        return clz;
    }
    return null;
  }

  /*
    Just don't add class as a hash parameter
   */
  private static Field findFirstFieldRecursive(Class<?> ownerAttribute, Class<?> c, int hash,
                                               boolean fieldShouldBeStatic) {
    for (Field f : c.getDeclaredFields()) {
      int modifiers = f.getModifiers();

      if (((modifiers & 0x8) != 0x0) != fieldShouldBeStatic)
        continue;
      if ((modifiers & 0x1) == 0x0) { //if not public
        if ((modifiers & 0x2) != 0x0) { // if private
          if (!ownerAttribute.equals(c))
            continue;
        } else if ((modifiers & 0x4) != 0x0) { // if protected
          if (!(Objects.equals(ownerAttribute.getPackage(), c.getPackage()) || c.isAssignableFrom(ownerAttribute)))
            continue;
        } else { // package access
          if (!Objects.equals(ownerAttribute.getPackage(), c.getPackage()))
            continue;
        }
      }
      if (getHashField(f.getName(), f.getType().getName()) == hash) {
        return f;
      }
    }
    // order as in JVM §5.4.3.2.
    Field f;
    for (Class<?> i : c.getInterfaces()) {
      f = findFirstFieldRecursive(ownerAttribute, i, hash, fieldShouldBeStatic);
      if (f != null)
        return f;
    }
    if (c.getSuperclass() != null) {
      f = findFirstFieldRecursive(ownerAttribute, c.getSuperclass(), hash, fieldShouldBeStatic);
      if (f != null)
        return f;
    }
    return null;
  }


  public static int getHashField(Object name, Object typeName) {
    int result = 17;
    result = 31 * result + name.hashCode();
    result = 31 * result + typeName.hashCode();
    return result;
  }

  public static int getHashMethod(Object name, Object clazzName, Object[] params) {
    int result = 17;
    result = 31 * result + name.hashCode();
    result = 31 * result + clazzName.hashCode();
    for (Object c : params)
      result = 31 * result + ((Class<?>) c).getName().hashCode();
    return result;
  }
}
