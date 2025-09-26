package net.branchlock.task.implementation.removal.information;

import net.branchlock.Branchlock;
import net.branchlock.commons.java.Pair;
import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.SingleMethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class KotlinIntrinsicsAdapter implements SingleMethodDriver, Opcodes {
  private static final String KOTLIN_INTRINSICS_CLASS = "kotlin/jvm/internal/Intrinsics";
  private static final Set<String> CONTAINS_DEBUG_INFO = Set.of("checkNotNull", "checkExpressionValueIsNotNull",
    "checkNotNullExpressionValue", "checkReturnedValueIsNotNull", "checkFieldIsNotNull", "checkParameterIsNotNull", "checkNotNullParameter");
  private final DebugInfoRemover debugInfoRemover;
  private final AtomicInteger counter = new AtomicInteger(0);

  public KotlinIntrinsicsAdapter(DebugInfoRemover debugInfoRemover) {
    this.debugInfoRemover = debugInfoRemover;
  }

  @Override
  public boolean driveEach(BMethod m) {
    List<Pair<MethodInsnNode, Frame<SourceValue>>> methodCalls = m.getNodesWithFrames(new SourceInterpreter(), MethodInsnNode.class);
    for (Pair<MethodInsnNode, Frame<SourceValue>> pair : methodCalls) {
      MethodInsnNode methodInsnNode = pair.a;
      Frame<SourceValue> b = pair.b;
      if(b == null) continue;
      if (KOTLIN_INTRINSICS_CLASS.equals(methodInsnNode.owner) && (CONTAINS_DEBUG_INFO.contains(methodInsnNode.name))) {
          // all descriptor entries that are String are debug information. Find the origin and if its a LDC replace it with "?BL".
        Type[] argumentTypes = Type.getArgumentTypes(methodInsnNode.desc);
        for (int i = 0; i < argumentTypes.length; i++) {
            if (!argumentTypes[i].equals(Type.getType(String.class))) continue;
            Set<AbstractInsnNode> insns = b.getStack(b.getStackSize() - argumentTypes.length + i).insns;
            if(insns.size() != 1) {
                continue;
            }
            AbstractInsnNode ain = insns.iterator().next();
            if (ain.getOpcode() != LDC) continue;
            LdcInsnNode ldcInsnNode = (LdcInsnNode) ain;
            if (ldcInsnNode.cst instanceof String) {
              counter.incrementAndGet();
              ldcInsnNode.cst = "<BL?>";
            }
        }
      }
    }
    return true;
  }

  @Override
  public void postDrive() {
    Branchlock.LOGGER.info("Replaced {} Kotlin intrinsics strings.", counter.get());
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return debugInfoRemover.defaultMemberExclusionHandlers();
  }
}
