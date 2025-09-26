package net.branchlock.config;

import mjson.Json;

public final class EmptyConfig extends JsonConfig {
  public EmptyConfig() {
    super(Json.object());
  }
}
