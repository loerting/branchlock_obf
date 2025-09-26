package net.branchlock;

import net.branchlock.inputprovider.BranchlockRunType;

public class BranchlockInteractiveDemoRunner extends BranchlockFileRunner {

  public BranchlockInteractiveDemoRunner(String configFilePath) {
    super(configFilePath);
  }

  public static void main(String[] args) {
    System.exit(new Branchlock(new BranchlockInteractiveDemoRunner(args[0])).startBranchlock());
  }

  @Override
  public BranchlockRunType getRunType() {
    return BranchlockRunType.INTERACTIVE_DEMO;
  }

}

