package net.branchlock.task.implementation.references.numbers.term;

public abstract class BiTerm extends Term {
  public Term term1;
  public Term term2;

  protected BiTerm(Term term1, Term term2) {
    this.term1 = term1;
    this.term2 = term2;
  }

  @Override
  public boolean isWide() {
    return term1.calculate() instanceof Long || term2.calculate() instanceof Long;
  }

  public void obfuscateRecursive(int depth) {
    if (term1 instanceof NumTerm) {
      term1 = ((NumTerm) term1).obfuscate();
    }
    if (term2 instanceof NumTerm) {
      term2 = ((NumTerm) term2).obfuscate();
    }
    depth--;
    if (depth >= 0) {
      if (term1 instanceof BiTerm) {
        ((BiTerm) term1).obfuscateRecursive(depth);
      }
      if (term2 instanceof BiTerm) {
        ((BiTerm) term2).obfuscateRecursive(depth);
      }
    }
  }
}
