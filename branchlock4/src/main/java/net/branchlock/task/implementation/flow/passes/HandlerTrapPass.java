package net.branchlock.task.implementation.flow.passes;

import net.branchlock.commons.asm.Instructions;
import net.branchlock.structure.BMethod;
import net.branchlock.task.Task;
import net.branchlock.task.implementation.flow.FlowPassDriver;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class HandlerTrapPass extends FlowPassDriver {
  public HandlerTrapPass(Task task, float coveragePct) {
    super(task, coveragePct);
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    stream.forEach(bm -> {
        InsnList insnList = bm.instructions;
      List<TryCatchBlockNode> toAdd = new ArrayList<>();
      for (TryCatchBlockNode tcbn : bm.tryCatchBlocks) {
        if (R.nextFloat() >= coveragePct)
          continue;
        LabelNode tcbStart = new LabelNode();
        insnList.insertBefore(tcbn.handler, tcbStart);
        insnList.insertBefore(tcbn.handler, new JumpInsnNode(GOTO, tcbn.handler));
        toAdd.add(new TryCatchBlockNode(tcbStart, tcbn.handler, tcbn.handler, tcbn.type));

        InsnList handlerLoop = new InsnList();
        handlerLoop.add(new InsnNode(DUP));
        handlerLoop.add(new JumpInsnNode(IFNULL, tcbStart));
        insnList.insertBefore(Instructions.getRealNext(tcbn.handler), handlerLoop);
      }
      bm.tryCatchBlocks.addAll(toAdd);
    });
    return true;
  }

  @Override
  public String identifier() {
    return "handler-trap";
  }

  @Override
  protected boolean isFitting(BMethod t) {
    if (t.tryCatchBlocks == null) return false;
    return !t.tryCatchBlocks.isEmpty();
  }
}
