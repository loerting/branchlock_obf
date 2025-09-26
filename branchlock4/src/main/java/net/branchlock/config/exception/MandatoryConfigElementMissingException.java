package net.branchlock.config.exception;

public class MandatoryConfigElementMissingException extends ConfigException {
  public MandatoryConfigElementMissingException(String key) {
    super("Mandatory config element missing: " + key);
  }
}
