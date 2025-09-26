package net.branchlock.inputprovider;

import net.branchlock.config.Config;
import net.branchlock.config.EmptyConfig;

public class DefaultConfigProvider implements ConfigProvider {
  @Override
  public Config loadConfig() {
    return new EmptyConfig();
  }
}
