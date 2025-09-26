package net.branchlock.task.implementation.references.drivers;

import net.branchlock.commons.asm.Reference;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.task.Task;
import net.branchlock.task.driver.implementations.SingleClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import org.objectweb.asm.Opcodes;

import java.util.Collection;

public class IllegalAccessFixDriver implements SingleClassDriver, Opcodes {
  private final Task task;

  public IllegalAccessFixDriver(Task task) {
    this.task = task;
  }

  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    return task.noExclusionHandlers();
  }

  @Override
  public String identifier() {
    return "access-updater";
  }

  @Override
  public boolean driveEach(BClass bc) {
    for (BMethod method : bc.methods) {
      method.getCode().iterateOverReferences(ref -> {
        BClass refHolder = task.dataProvider.getClasses().get(ref.owner);
        if (refHolder == null || !refHolder.isLocal()) return;
        refHolder.setAccess(refHolder.ensureLegalAccess(bc, refHolder.access)); // allow access to reference holder class
          switch (ref.type) {
              case METHOD_INVOKE -> {
                  BMethod bMethod = refHolder.resolveMethod(ref.name, ref.desc);
                  if (bMethod == null) return;
                  IEquivalenceClass<BMethod> equivalenceClass = bMethod.requireEquivalenceClass();
                  for (BMethod member : equivalenceClass.getMembers()) {
                      if (member.isLocal()) {
                        BClass owner = member.getOwner();
                        owner.setAccess(owner.ensureLegalAccess(bc, owner.access)); // allow access to resolved method owner
                        member.setAccess(owner.ensureLegalAccess(bc, member.access)); // allow access to member
                      }
                  }
              }
              case FIELD_GET, FIELD_SET -> {
                  BField field = refHolder.resolveField(ref.name, ref.desc);
                  if (field != null && field.isLocal()) {
                    BClass owner = field.getOwner();
                    owner.setAccess(owner.ensureLegalAccess(bc, owner.access)); // allow access to resolved field owner
                    field.setAccess(owner.ensureLegalAccess(bc, field.access)); // allow access to member
                  }
              }
              case CLASS_TYPE -> { /* nothing needs to be done, reference holder access is updated already above */ }
          }
      });
    }
    return true;
  }
}
