package net.branchlock.task.implementation.flow;

import net.branchlock.Branchlock;
import net.branchlock.structure.BMethod;
import net.branchlock.task.Task;
import net.branchlock.task.driver.implementations.MethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import org.objectweb.asm.Opcodes;

import java.util.Collection;
import java.util.Random;

public abstract class FlowPassDriver implements MethodDriver, Opcodes {
  protected static final Random R = Branchlock.R;
  public final Task task;
  protected final float coveragePct;

  public FlowPassDriver(Task task, float coveragePct) {
    this.task = task;
    this.coveragePct = coveragePct;
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return task.defaultMemberExclusionHandlersPlus(t -> t.filter(bm -> isSuitable(bm) && isFitting(bm)));
  }

  private boolean isSuitable(BMethod bm) {
    if (bm.hasAnnotation("PerformanceSensitive"))
      return false;
    if (ControlFlow.IGNORED_METHODS.contains(bm.getName()))
      return false;
    return bm.getInstructionCount() < Short.MAX_VALUE * 1.5;
  }

  protected abstract boolean isFitting(BMethod t);
}
