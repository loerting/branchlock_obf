package net.branchlock;

import net.branchlock.inputprovider.BranchlockRunType;

public class BranchlockStacktraceRunner extends BranchlockFileRunner {

  public BranchlockStacktraceRunner(String configFilePath) {
    super(configFilePath);
  }

  public static void main(String[] args) {
    System.exit(new Branchlock(new BranchlockStacktraceRunner(args[0])).startBranchlock());
  }

  @Override
  public BranchlockRunType getRunType() {
    return BranchlockRunType.STACKTRACE_DECRYPTION;
  }

}

