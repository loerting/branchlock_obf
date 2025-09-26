package net.branchlock.commons.asm;

import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

public class Frames {


  public static boolean matchesStack(Frame<BasicValue> f1, Frame<BasicValue> f2) {
    if (f1 == null || f2 == null)
      return false;
    if (f1.getStackSize() != f2.getStackSize())
      return false;
    for (int i = 0; i < f1.getStackSize(); i++) {
      if (!f1.getStack(i).equals(f2.getStack(i)))
        return false;
    }
    return true;
  }

  public static boolean matchesLocal(Frame<BasicValue> f1, Frame<BasicValue> f2) {
    if (f1 == null || f2 == null)
      return false;
    if (f1.getLocals() != f2.getLocals())
      return false;
    for (int i = 0; i < f1.getLocals(); i++) {
      if (!f1.getLocal(i).equals(f2.getLocal(i))) {
        return false;
      }
    }
    return true;
  }

  public static boolean framesEqual(Frame<BasicValue> f1, Frame<BasicValue> f2) {
    return matchesStack(f1, f2) && matchesLocal(f1, f2);
  }
}
