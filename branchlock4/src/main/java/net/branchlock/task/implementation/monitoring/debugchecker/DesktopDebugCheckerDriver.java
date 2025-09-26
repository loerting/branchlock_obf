package net.branchlock.task.implementation.monitoring.debugchecker;

import net.branchlock.Branchlock;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.SingleClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.references.numbers.Numbers;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DesktopDebugCheckerDriver implements SingleClassDriver, Opcodes {
  private final DebugChecker debugChecker;
  private final List<String> forbiddenArgs;
  private final AtomicInteger counter = new AtomicInteger(0);

  public DesktopDebugCheckerDriver(DebugChecker debugChecker, List<String> forbiddenArgs) {
    this.debugChecker = debugChecker;
    this.forbiddenArgs = forbiddenArgs;
  }

  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    return debugChecker.defaultClassExclusionHandlersPlus(
      t -> t.filter(bc -> bc.methods.stream().anyMatch(bm -> !bm.isStaticInitializer() && bm.isPotentialEntryPoint())));
  }

  @Override
  public void postDrive() {
    Branchlock.LOGGER.info("Inserted {} debug checks at entry points.", counter.get());
  }

  @Override
  public String identifier() {
    return "desktop-debug-check";
  }

  @Override
  public boolean driveEach(BClass c) {
    counter.incrementAndGet();
    BMethod staticInitializer = c.getOrMakeStaticInitializer();

    InsnList il = new InsnList();

    LabelNode start = new LabelNode();
    LabelNode infiniteLoop = new LabelNode();
    il.add(start);
    il.add(new MethodInsnNode(INVOKESTATIC, "java/lang/management/ManagementFactory", "getRuntimeMXBean",
      "()Ljava/lang/management/RuntimeMXBean;"));
    il.add(new MethodInsnNode(INVOKEINTERFACE, "java/lang/management/RuntimeMXBean", "getInputArguments",
      "()Ljava/util/List;"));
    il.add(new InsnNode(ICONST_0));
    LabelNode loop = new LabelNode();
    il.add(loop);
    il.add(new InsnNode(DUP_X1));
    il.add(new InsnNode(SWAP));
    il.add(new InsnNode(DUP_X1));
    il.add(new InsnNode(SWAP));
    il.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;"));
    il.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I"));

    for (String arg : forbiddenArgs) {
      il.add(new InsnNode(DUP));
      il.add(Numbers.generateCalculation(arg.hashCode(), Numbers.NumbersStrength.STRONG));
      il.add(new JumpInsnNode(IF_ICMPEQ, infiniteLoop));
    }
    il.add(new InsnNode(POP));
    il.add(new InsnNode(SWAP));
    il.add(new InsnNode(ICONST_1));
    il.add(new InsnNode(IADD));
    il.add(new JumpInsnNode(GOTO, loop));
    il.add(infiniteLoop);
    il.add(new JumpInsnNode(GOTO, infiniteLoop));
    LabelNode end = new LabelNode();
    il.add(end);
    il.add(new InsnNode(POP));

      staticInitializer.instructions.insert(il);
    staticInitializer.tryCatchBlocks.add(new TryCatchBlockNode(start, end, end, "java/lang/IndexOutOfBoundsException"));

    staticInitializer.maxStack = Math.max(staticInitializer.maxStack, 5);
    return true;
  }
}
