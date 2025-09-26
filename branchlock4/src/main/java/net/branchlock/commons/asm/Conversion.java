package net.branchlock.commons.asm;

import net.branchlock.Branchlock;
import net.branchlock.commons.io.ByteReader;
import net.branchlock.structure.BClass;
import net.branchlock.structure.provider.IDataProvider;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;

public class Conversion {

  public static final int EXPLOIT_CLASS = 3 << 24;

  public static byte[] toBytecode(ClassNode cn, boolean useMaxs) {
    try {
      ClassWriter cw = new ClassWriter(useMaxs ? ClassWriter.COMPUTE_MAXS : ClassWriter.COMPUTE_FRAMES);
      cn.accept(cw);
      return cw.toByteArray();
    } catch (Exception e) {
      return toBytecode0(cn);
    }
  }

  public static byte[] toBytecode0(ClassNode cn) {
    ClassWriter cw = new ClassWriter(0);
    cn.accept(cw);
    return cw.toByteArray();
  }


  public static BClass toNode(IDataProvider dp, final byte[] bytez) {
    BClass cn = new BClass(dp);
    ClassReader cr;
    try {
      cr = new ClassReader(bytez);
    } catch (Exception e) {
      Branchlock.LOGGER.error("Failed to init reader for class {}. error: {}", tryToReadClassName(bytez), e.getMessage());
      return cn;
    }

    try {
      cr.accept(cn, ClassReader.EXPAND_FRAMES);
    } catch (Exception e) {
      try {
        cr.accept(cn, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
      } catch (Exception e2) {
        Branchlock.LOGGER.error("Failed to read class {}, error: {}", tryToReadClassName(bytez), e2.getMessage());
      }
    }
    return cn;
  }


  private static String tryToReadClassName(byte[] bytes) {
    try {
      ByteReader br = new ByteReader(bytes);
      int cpCount = br.readUnsignedShort(8);

      int pointer = 10;

      int[] classLinkToUtf8Container = new int[cpCount];
      String[] utf8Container = new String[cpCount];
      int cpIdx = 1;
      while (cpIdx < cpCount) {
        int cpInfoSize;
        int currentType = bytes[pointer];
        switch (currentType) {
          case Symbol.CONSTANT_FIELDREF_TAG:
          case Symbol.CONSTANT_METHODREF_TAG:
          case Symbol.CONSTANT_INTERFACE_METHODREF_TAG:
          case Symbol.CONSTANT_INTEGER_TAG:
          case Symbol.CONSTANT_FLOAT_TAG:
          case Symbol.CONSTANT_NAME_AND_TYPE_TAG:
          case Symbol.CONSTANT_INVOKE_DYNAMIC_TAG:
          case Symbol.CONSTANT_DYNAMIC_TAG:
            cpInfoSize = 5;
            break;
          case Symbol.CONSTANT_LONG_TAG:
          case Symbol.CONSTANT_DOUBLE_TAG:
            cpInfoSize = 9;
            cpIdx++; // skip
            break;
          case Symbol.CONSTANT_UTF8_TAG:
            int len = br.readUnsignedShort(pointer + 1);
            cpInfoSize = 3 + len;
            utf8Container[cpIdx] = new String(bytes, pointer + 3, len);
            break;
          case Symbol.CONSTANT_METHOD_HANDLE_TAG:
            cpInfoSize = 4;
            break;
          case Symbol.CONSTANT_CLASS_TAG:
            classLinkToUtf8Container[cpIdx] = br.readUnsignedShort(pointer + 1);
            // no break
          case Symbol.CONSTANT_STRING_TAG:
          case Symbol.CONSTANT_METHOD_TYPE_TAG:
          case Symbol.CONSTANT_PACKAGE_TAG:
          case Symbol.CONSTANT_MODULE_TAG:
            cpInfoSize = 3;
            break;
          default:
            // ignore failure and continue to check each next byte..
            pointer++; // increase pointer and try again
            continue;
        }
        pointer += cpInfoSize;
        cpIdx++;
      }

      pointer += 2; // skip access flags
      int nameIndex = br.readUnsignedShort(pointer);
      if (nameIndex < cpCount) {
        return utf8Container[classLinkToUtf8Container[nameIndex]];
      }
    } catch (Exception e) {
    }
    return "[unknown name]";
  }

  public static boolean isInClasspath(String name) {
    try {
      Class.forName(name.replace('/', '.'), false, ClassLoader.getSystemClassLoader());
      return true;
    } catch (Throwable t) {
      return false;
    }
  }

  /**
   * Reads a class from the local jar. Used for loading layout classes.
   * This does not cache the class, so it should only be used once.
   * <p>
   * Be careful when using this method and adding the ClassNode to the project.
   * The version could exceed the target version.
   */
  public static BClass loadProgramClass(IDataProvider dp, Class<?> c) {
    try {
      byte[] bytes = loadFromClasspath(c.getName().replace('.', '/'), true);
      if (bytes == null)
        throw new IllegalStateException("Failed to load layout class " + c.getName());
      return toNode(dp, bytes);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load layout class " + c.getName(), e);
    }
  }

  public static BClass loadClasspathNode(IDataProvider dp, Class<?> c) {
    return loadClasspathNode(dp, c.getName().replace('.', '/'));
  }

  public static BClass loadClasspathNode(IDataProvider dp, String name) {
    try {
      byte[] bytes = loadFromClasspath(name, false);
      if (bytes == null)
        return null;
      return toNode(dp, bytes);
    } catch (IOException e) {
      Branchlock.LOGGER.error("Failed to load classpath node {}", name);
    }
    return null;
  }

  public static byte[] loadFromClasspath(String type, boolean permitLocalClasses) throws IOException {
    if (type == null) return null;
    try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(type + ".class")) {
      if (is == null) return null;

      if (!permitLocalClasses) {
        // make sure the class is a runtime class and not a program class.
        try {
          Class<?> c = Class.forName(type.replace('/', '.'), false, ClassLoader.getSystemClassLoader());
          if (c.getClassLoader() != null) {
            // this is a program class, not a runtime class
            return null;
          }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
          // ignore and return the array.
        }
      }
      return IOUtils.toByteArray(is);
    }
  }
}
