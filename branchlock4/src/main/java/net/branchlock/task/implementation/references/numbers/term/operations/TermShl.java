package net.branchlock.task.implementation.references.numbers.term.operations;

import net.branchlock.commons.os.Bits;
import net.branchlock.task.implementation.references.numbers.term.BiTerm;
import net.branchlock.task.implementation.references.numbers.term.NumTerm;
import net.branchlock.task.implementation.references.numbers.term.Term;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

public class TermShl extends BiTerm {

  public TermShl(Term term1, Term term2) {
    super(term1, term2);
  }

  public static TermShl forNumber(Number n) {
    if (n instanceof Long) {
      long number = n.longValue();
      if (number == 0) {
        long random = R.nextInt(Byte.MAX_VALUE) & -2; // cut last bit
        int possibleShift = Bits.shiftCountForZero(random).a;
        possibleShift += R.nextInt(512) * 64;
        return new TermShl(new NumTerm(random), new NumTerm(possibleShift));
      }
      int possibleShift = Bits.shiftRange((long) n).a;
      int shift = possibleShift > 1 ? 1 + R.nextInt(possibleShift - 1) : 0; // avoid zero
      long l1 = number >>> shift;
      shift += R.nextInt(512) * 64;
      return new TermShl(new NumTerm(l1), new NumTerm(shift));
    } else {
      int number = n.intValue();
      if (number == 0) {
        int random = R.nextInt(Byte.MAX_VALUE) & -2; // cut last bit
        int possibleShift = Bits.shiftCountForZero(random).a;
        possibleShift += R.nextInt(512) * 32;
        return new TermShl(new NumTerm(random), new NumTerm(possibleShift));
      }
      int possibleShift = Bits.shiftRange((int) n).a;
      int shift = possibleShift > 1 ? 1 + R.nextInt(possibleShift - 1) : 0; // avoid zero
      int i1 = number >>> shift;
      shift += R.nextInt(512) * 32;
      return new TermShl(new NumTerm(i1), new NumTerm(shift));
    }
  }

  @Override
  public InsnList getTerm(boolean enhanced) {
    InsnList il = new InsnList();
    boolean left = R.nextBoolean();
    InsnList termFst = term1.getTerm(enhanced && left);
    InsnList termSnd = term2.getTerm(enhanced && !left);
    il.add(termFst);
    il.add(termSnd);
    il.add(new InsnNode(term1.isWide() ? LSHL : ISHL));
    return il;
  }

  @Override
  public Number calculate() {
    if (this.isWide())
      return term1.calculate().longValue() << term2.calculate().intValue();
    else
      return term1.calculate().intValue() << term2.calculate().intValue();
  }
}
