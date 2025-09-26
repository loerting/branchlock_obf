package net.branchlock.task.implementation.salting.drivers;

import net.branchlock.commons.asm.Instructions;
import net.branchlock.commons.generics.Debugging;
import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.SingleMethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.salting.MethodSalt;
import net.branchlock.task.implementation.salting.Salting;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collection;

@Debugging
public class SaltingDebugDriver implements SingleMethodDriver, Opcodes {

  private final Salting salting;

  public SaltingDebugDriver(Salting salting) {
    this.salting = salting;
    if (!salting.dataProvider.branchlock.settingsManager.isDebugMode())
      throw new IllegalStateException("SaltingDebugDriver should only be used in debug mode!");
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return salting.defaultMemberExclusionHandlers();
  }

  @Override
  public boolean driveEach(BMethod bm) {
    MethodSalt salt = bm.getSalt();
    if (salt == null || bm.getInstructionCount() == 0) return true;

    InsnList dbg = new InsnList();

    LabelNode success = new LabelNode();

    dbg.add(salt.makeLoad());
    dbg.add(Instructions.intPush(salt.getValue()));
    dbg.add(new JumpInsnNode(IF_ICMPEQ, success));

    dbg.add(new TypeInsnNode(NEW, "java/lang/RuntimeException"));
    dbg.add(new InsnNode(DUP));
    dbg.add(new LdcInsnNode("Wrong salt in " + bm));
    dbg.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false));
    dbg.add(new InsnNode(ATHROW));

    dbg.add(success);

      bm.instructions.insert(dbg);
    return true;
  }
}
