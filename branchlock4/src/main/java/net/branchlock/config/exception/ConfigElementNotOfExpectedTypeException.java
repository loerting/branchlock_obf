package net.branchlock.config.exception;

public class ConfigElementNotOfExpectedTypeException extends ConfigException {
  public ConfigElementNotOfExpectedTypeException(String key, Class<?> expectedType, Class<?> gotType) {
    super("Config element \"" + key + "\" is not of expected type \"" + expectedType + "\" but of type \"" + gotType + "\"");
  }

  public ConfigElementNotOfExpectedTypeException(String key, Class<?> expectedType, Class<?> gotType, Object value) {
    super("Config element \"" + key + "\" is not of expected type  \"" + expectedType + " \" but of type  \"" + gotType + "\" with value \"" + value + "\"");
  }
}
