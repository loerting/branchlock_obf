package net.branchlock.inputprovider;


public interface BranchlockInputProvider {
  BranchlockRunType getRunType();

  ConfigProvider getConfigProvider();
}
