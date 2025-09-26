package net.branchlock.task.implementation.references.numbers.term.operations;

import net.branchlock.task.implementation.references.numbers.term.BiTerm;
import net.branchlock.task.implementation.references.numbers.term.NumTerm;
import net.branchlock.task.implementation.references.numbers.term.Term;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

public class TermXor extends BiTerm {


  public TermXor(Term term1, Term term2) {
    super(term1, term2);
  }

  public static TermXor forNumber(Number n) {
    if (n instanceof Long) {
      long l1 = adaptedRandomInt(n);
      long l2 = n.longValue() ^ l1;
      return new TermXor(new NumTerm(l1), new NumTerm(l2));
    } else {
      int i1 = adaptedRandomInt(n);
      int i2 = n.intValue() ^ i1;
      return new TermXor(new NumTerm(i1), new NumTerm(i2));
    }
  }

  @Override
  public InsnList getTerm(boolean enhanced) {
    InsnList il = new InsnList();
    boolean left = R.nextBoolean();
    InsnList termFst = term1.getTerm(enhanced && left);
    InsnList termSnd = term2.getTerm(enhanced && !left);
    if (R.nextBoolean()) {
      il.add(termFst);
      il.add(termSnd);
    } else {
      il.add(termSnd);
      il.add(termFst);
    }
    il.add(new InsnNode(this.isWide() ? LXOR : IXOR));
    return il;
  }

  @Override
  public Number calculate() {
    if (this.isWide())
      return term1.calculate().longValue() ^ term2.calculate().longValue();
    else
      return term1.calculate().intValue() ^ term2.calculate().intValue();
  }
}
