package net.branchlock.task.implementation.naming.drivers;

import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.SingleMethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.naming.Renamer;
import org.objectweb.asm.tree.LocalVariableNode;

import java.util.Collection;
import java.util.List;

public class LocalVariableRenamerDriver implements SingleMethodDriver {

  private final Renamer renamer;

  public LocalVariableRenamerDriver(Renamer renamer) {
    this.renamer = renamer;
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return renamer.defaultMemberExclusionHandlers();
  }

  @Override
  public String identifier() {
    return "local-variable-renamer";
  }

  @Override
  public boolean driveEach(BMethod c) {
    renamer.nameFactory.resetNameIterator();
    List<LocalVariableNode> localVariables = c.localVariables;
    if (localVariables != null) {
      localVariables.forEach(lv -> {
        if (lv.name != null)
          lv.name = renamer.nameFactory.getNameIteratorNext();
      });
    }
    return true;
  }
}
