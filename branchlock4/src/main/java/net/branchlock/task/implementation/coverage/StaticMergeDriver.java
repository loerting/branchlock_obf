package net.branchlock.task.implementation.coverage;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.commons.asm.Reference;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.MethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

public class StaticMergeDriver implements MethodDriver, Opcodes {
  /**
   * Maps any method descriptor to the collective switch method (gate).
   */
  public final Map<String, MethodGate> gates = new HashMap<>();
  private final RuntimeCoverage runtimeCoverage;
  private final Random R = Branchlock.R;

  public StaticMergeDriver(RuntimeCoverage runtimeCoverage) {
    this.runtimeCoverage = runtimeCoverage;
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    Branchlock.LOGGER.info("Merging static methods.");
    stream.forEach(bm -> {
      MethodNode m = bm;
      for (AbstractInsnNode ain : m.instructions.toArray()) {
        if (ain.getOpcode() == INVOKESTATIC) {
          MethodInsnNode min = (MethodInsnNode) ain;
          if (min.itf || !runtimeCoverage.dataProvider.isRuntimeClass(min.owner))
            continue;
          String desc = min.desc;
          MethodGate mg = gates.computeIfAbsent(desc, d -> {
            BClass nonInit = runtimeCoverage.dataUtilities.getPreparedNSEClass();
            String[] splitDesc = desc.split("\\)");
            char rIntType = "ISC".charAt(R.nextInt(3));
            BMethod mn = new BMethod(nonInit, ACC_PUBLIC | ACC_STATIC, "$m_gate_" + gates.size(),
              splitDesc[0] + rIntType + ")" + splitDesc[1], null, null);
            return new MethodGate(d, nonInit, mn);
          });
          int id = mg.addMethod(Reference.of(min));
          if (bm.getSalt() != null) {
            m.instructions.insertBefore(min, bm.getSalt().loadEncryptedInt(id));
          } else {
            m.instructions.insertBefore(min, Instructions.intPush(id));
          }

          // don't replace min fields here, as min is used by MethodGate and needs the original fields.
          m.instructions.set(min, new MethodInsnNode(INVOKESTATIC, mg.gateOwner.name, mg.gateMethod.name, mg.gateMethod.desc));
        }
      }
    });

    gates.forEach((desc, gate) -> gate.generateSwitchMethod());
    return true;
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return runtimeCoverage.defaultMemberExclusionHandlers();
  }

  @Override
  public String identifier() {
    return "static-method-merger";
  }
}
