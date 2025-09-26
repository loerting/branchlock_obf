package net.branchlock;

import mjson.Json;
import net.branchlock.config.JsonConfig;
import net.branchlock.inputprovider.BranchlockInputProvider;
import net.branchlock.inputprovider.BranchlockRunType;
import net.branchlock.inputprovider.ConfigProvider;

import java.io.File;
import java.net.MalformedURLException;

public class BranchlockFileRunner implements BranchlockInputProvider {
  private final String configFilePath;

  public BranchlockFileRunner(String configFilePath) {
    this.configFilePath = configFilePath;
  }

  public static void main(String[] args) {
    System.exit(new Branchlock(new BranchlockFileRunner(args[0])).startBranchlock());
  }

  @Override
  public BranchlockRunType getRunType() {
    return BranchlockRunType.TRANSFORMATION;
  }

  @Override
  public ConfigProvider getConfigProvider() {
    return () -> {
      try {
        return new JsonConfig(Json.read(new File(configFilePath).toURI().toURL()));
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    };
  }
}

