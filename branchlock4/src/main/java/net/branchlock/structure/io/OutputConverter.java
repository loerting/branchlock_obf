package net.branchlock.structure.io;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Access;
import net.branchlock.commons.asm.Conversion;
import net.branchlock.commons.asm.MajorVersion;
import net.branchlock.structure.BClass;
import net.branchlock.structure.provider.DataProvider;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodTooLargeException;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OutputConverter implements Opcodes {

  private final Map<String, BClass> classes;
  private final DataProvider dataProvider;

  public OutputConverter(DataProvider dataProvider) {
    this.classes = dataProvider.getClasses();
    this.dataProvider = dataProvider;
  }

  public Map<String, byte[]> toBytecode() {
    final Map<String, byte[]> writtenClasses = new ConcurrentHashMap<>();
    try (ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
      for (Map.Entry<String, BClass> entry : classes.entrySet()) {
        service.execute(() -> {
          BClass c = entry.getValue();
          if (writtenClasses.containsKey(c.name)) {
            Branchlock.LOGGER.warning("Skipping duplicate class file: {} ", c.name);
            return;
          }
          try {
            writtenClasses.put(c.name, classToBytecode(c));
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        });
      }
      service.shutdown();
      boolean awaited = service.awaitTermination(5, TimeUnit.MINUTES);
      if (!awaited) {
        Branchlock.LOGGER.warning("Failed to convert all classes to bytecode in time. Some classes may be missing.");
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return writtenClasses;
  }


  private byte[] classToBytecode(BClass c) throws InterruptedException {
    try {
      removeExistingFrames(c);
      return toBytecodeLibs(c);
    } catch (MethodTooLargeException e) {
      String originalName = classes.containsKey(e.getClassName()) ? classes.get(e.getClassName()).getOriginalName() : null;
      String className = e.getClassName() + (originalName != null && !e.getClassName().equals(originalName) ? " (" + originalName + ")" : "");
      Branchlock.LOGGER.error("Method {} is too large to write in class {}. Please exclude this class.", e.getMethodName(), className);

    } catch (OutOfMemoryError oom) {
      Branchlock.LOGGER.error("Out of memory during conversion. Please don't overdo obfuscation.");
      System.exit(-1);
      return new byte[0];
    } catch (Throwable e) {
      boolean containsJsr = e instanceof IllegalArgumentException && e.getMessage() != null && e.getMessage().contains("JSR/RET");
      if (containsJsr)
        Branchlock.LOGGER.warning("Class {} contains JSR/RET but is version {}.", c.name, c.version);
      else
        Branchlock.LOGGER.error("Exception at class conversion, {}", e, c.name);

      // try to write without frames
      Branchlock.LOGGER.warning("Trying to write class {} without frame generation.", c.name);
      try {
        return Conversion.toBytecode(c, true);
      } catch (OutOfMemoryError oom1) {
        Branchlock.LOGGER.error("Out of memory during conversion. Please don't overdo obfuscation.");
        System.exit(-1);
        return new byte[0];
      } catch (Throwable t) {
        Branchlock.LOGGER.error("Conversion failed fatally.");
      }
    }

    Branchlock.LOGGER.warning("Writing class {} without method code.", c.name);

    // try to write without method code
    removeMethodCode(c);
    try {
      return Conversion.toBytecode(c, true);
    } catch (Throwable ignored) {
    }
    Branchlock.LOGGER.error("Writing without method code failed, writing empty class.");

    // write empty class
    try {
      ClassNode cn = new ClassNode();
      cn.name = c.name;
      cn.version = c.version;
      cn.superName = c.superName;
      cn.interfaces = c.interfaces;
      cn.access = c.access;
      return Conversion.toBytecode0(cn);
    } catch (Throwable neverHappens) {
      Branchlock.LOGGER.error("Even empty class writing failed, returning empty byte array.");
      return new byte[0];
    }
  }

  private static void removeMethodCode(BClass c) {
    c.methods.forEach(m -> {
      if (!Access.isAbstract(m.access) && !Access.isNative(m.access)) {
        if (m.tryCatchBlocks != null)
          m.tryCatchBlocks.clear();
        if (m.localVariables != null)
          m.localVariables.clear();
        m.instructions.clear();
        m.instructions.add(new TypeInsnNode(NEW, "java/lang/UnknownError"));
        m.instructions.add(new InsnNode(DUP));
        m.instructions.add(new LdcInsnNode("Obfuscation export failed in this class, please contact the owner."));
        m.instructions.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/UnknownError", "<init>", "(Ljava/lang/String;)V"));
        m.instructions.add(new InsnNode(ATHROW));
      }
    });
  }

  private byte[] toBytecodeLibs(BClass cn) {
    ClassWriter cw = new FrameClassWriter(dataProvider, cn.version <= MajorVersion.JAVA_5.getCode() ? ClassWriter.COMPUTE_MAXS : ClassWriter.COMPUTE_FRAMES);
    cn.accept(cw);
    return cw.toByteArray();
  }


  /**
   * This somehow fixes an ASM bug
   */
  private void removeExistingFrames(BClass c) {
    for (MethodNode method : c.methods) {
      for (AbstractInsnNode ain : method.instructions.toArray()) {
        if (ain.getType() == AbstractInsnNode.FRAME)
          method.instructions.remove(ain);
      }
    }
  }
}
