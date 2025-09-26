package net.branchlock.task.implementation.salting.drivers;

import net.branchlock.commons.asm.ASMLimits;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.task.driver.implementations.SingleMethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.references.numbers.Numbers;
import net.branchlock.task.implementation.salting.Salting;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Collection;

/**
 * Finds
 */
public class SaltingReferenceUpdaterDriver implements SingleMethodDriver, Opcodes {

  private final Salting salting;
  private final SaltingSignatureCollectorDriver saltingSignatureCollectorDriver;

  public SaltingReferenceUpdaterDriver(Salting salting, SaltingSignatureCollectorDriver saltingSignatureCollectorDriver) {
    this.salting = salting;
    this.saltingSignatureCollectorDriver = saltingSignatureCollectorDriver;
  }


  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return salting.noExclusionHandlers();
  }

  @Override
  public String identifier() {
    return "reference-updater";
  }

  @Override
  public boolean driveEach(BMethod bm) {
      for (AbstractInsnNode ain : bm.instructions) {
      if (ain.getType() == AbstractInsnNode.METHOD_INSN) {
        MethodInsnNode min = (MethodInsnNode) ain;
        if (min.owner.startsWith("[")) continue; // array methods
        BClass bClass = salting.dataProvider.resolveBClass(min.owner, bm);
        if (bClass == null) continue;
        BMethod bMethod = bClass.resolveMethod(min.name, min.desc);
        if (bMethod == null) continue;
        IEquivalenceClass<BMethod> equivalenceClass = bMethod.requireEquivalenceClass();
        String newDescriptor = getNewDescriptor(equivalenceClass);
        if (newDescriptor == null) continue;

        min.desc = newDescriptor;

        int saltValue = getSaltValue(equivalenceClass);
        if (bm.getSalt() != null) {
          // use current salt for next salt
          InsnList pushSalt = bm.getSalt().loadEncryptedInt(saltValue);
            bm.instructions.insertBefore(min, pushSalt);
        } else {
          if (bm.isStaticInitializer() && bm.getInstructionCount() < ASMLimits.MAX_METHOD_SIZE / 4) {
              bm.instructions.insertBefore(min, Numbers.generateCalculation(saltValue, Numbers.NumbersStrength.STRONG));
          } else {
              bm.instructions.insertBefore(min, Numbers.generateCalculation(saltValue, Numbers.NumbersStrength.WEAK));
          }
        }
      }
    }
    return true;
  }

  private String getNewDescriptor(IEquivalenceClass<BMethod> equivalenceClass) {
    for (BMethod member : equivalenceClass.getMembers()) {
      String newDesc = saltingSignatureCollectorDriver.newDescriptors.get(member);
      if (newDesc != null) {
        return newDesc;
      }
    }
    return null;
  }

  private int getSaltValue(IEquivalenceClass<BMethod> equivalenceClass) {
    for (BMethod member : equivalenceClass.getMembers()) {
      if (member.getSalt() != null) {
        return member.getSalt().getValue();
      }
    }
    throw new IllegalStateException("No salt found for equivalence class " + equivalenceClass);
  }
}
