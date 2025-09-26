package net.branchlock.task.naming.nameiterator.implementation;

import net.branchlock.Branchlock;
import net.branchlock.task.naming.nameiterator.INameIterator;

/**
 * An iterator that generates names with a mix of right-to-left and left-to-right alphabets.
 */
public final class RTLAlphabeticNameIterator implements INameIterator {
  private AlphabetIterator rtlIterator = new AlphabetIterator("\u0620\u063d\u063e\u063f");
  private AlphabetIterator alphabeticIterator = new AlphabetIterator("abcdefghijklmnopqrstuvwxyz");

  private int index = 0;

  @Override
  public String next() {
    String next = alphabeticIterator.next();

    if (next.length() > 1 && index % 2 == 0) {
      String rtlChar1 = rtlIterator.next();
      String rtlChar2 = rtlIterator.next();

      int position = Branchlock.R.nextInt(next.length() - 1) + 1;
      next = next.substring(0, position) + rtlChar2 + next.substring(position);

      if(Branchlock.R.nextBoolean()) {
        next = rtlChar1 + next;
      } else {
        next = next + rtlChar1;
      }

      if (index % 4 == 0) rtlIterator.reset();
    }

    index++;
    return next;
  }

  @Override
  public void reset() {
    index = 0;
    rtlIterator.reset();
    alphabeticIterator.reset();
  }
}
