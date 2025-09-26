package net.branchlock.task.implementation.references.numbers.term;

import net.branchlock.Branchlock;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;

import java.util.Random;

public abstract class Term implements Opcodes {
  protected final static Random R = Branchlock.R;

  protected static int adaptedRandomInt(Number n) {
    if (n.intValue() == n.shortValue()) {
      return (short) R.nextInt();
    }
    return R.nextInt();
  }

  public abstract InsnList getTerm(boolean enhanced);

  public abstract Number calculate();

  public abstract boolean isWide();

}
