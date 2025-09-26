package net.branchlock.commons.debug;

import net.branchlock.Branchlock;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

public class Debugging implements Opcodes {
  public static void asmify(ClassNode cn) {
    TraceClassVisitor traceClassVisitor =
      new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out));
    cn.accept(traceClassVisitor);
  }

  public static void checkClass(ClassNode cn) {
    CheckClassAdapter cca = new CheckClassAdapter(new ClassVisitor(Opcodes.ASM9) {
    });
    cn.accept(cca);
    Branchlock.LOGGER.info("Finished checking class.");
  }

  public static InsnList generateSysout(String toString) {
    InsnList dbg = new InsnList();
    dbg.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
    dbg.add(new LdcInsnNode(toString));
    dbg.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V"));
    return dbg;
  }
}
