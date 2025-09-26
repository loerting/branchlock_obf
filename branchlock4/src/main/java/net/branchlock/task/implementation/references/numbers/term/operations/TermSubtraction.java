package net.branchlock.task.implementation.references.numbers.term.operations;

import net.branchlock.task.implementation.references.numbers.term.BiTerm;
import net.branchlock.task.implementation.references.numbers.term.NumTerm;
import net.branchlock.task.implementation.references.numbers.term.Term;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

public class TermSubtraction extends BiTerm {


  public TermSubtraction(Term term1, Term term2) {
    super(term1, term2);
  }

  public static TermSubtraction forNumber(Number n) {
    if (n instanceof Long) {
      long l1 = adaptedRandomInt(n);
      long l2 = n.longValue() + l1;
      return new TermSubtraction(new NumTerm(l2), new NumTerm(l1));
    } else {
      int i1 = adaptedRandomInt(n);
      int i2 = n.intValue() + i1;
      return new TermSubtraction(new NumTerm(i2), new NumTerm(i1));
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
    il.add(new InsnNode(this.isWide() ? LSUB : ISUB));
    return il;
  }

  @Override
  public Number calculate() {
    if (this.isWide())
      return term1.calculate().longValue() - term2.calculate().longValue();
    else
      return term1.calculate().intValue() - term2.calculate().intValue();
  }
}
