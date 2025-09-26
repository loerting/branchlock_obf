package net.branchlock.config;

import net.branchlock.config.exception.ConfigElementNotOfExpectedTypeException;
import net.branchlock.config.exception.MandatoryConfigElementMissingException;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public interface Config {
  /**
   * Returns the value of the given key. If the key is not present, the default value is returned.
   *
   * @param key The key of the value to return.
   * @param o   The default value to return if the key is not present.
   */
  <T> T getOrDefaultValue(String key, T o) throws ConfigElementNotOfExpectedTypeException;

  /**
   * Returns the value of the given key. If the key is not present, an exception is thrown.
   *
   * @param key The key of the value to return.
   * @throws MandatoryConfigElementMissingException if the key is not present.
   */
  <T> T getMandatoryValue(String key) throws MandatoryConfigElementMissingException, ConfigElementNotOfExpectedTypeException;

  /**
   * Checks if the config has a value for the given key. The value of the key can be anything.
   */
  boolean has(String key);

  /**
   * Returns the value of the given key. If the key is not present, null is returned.
   *
   * @param key The key of the value to return.
   *            The value of the key must be a collection / array.
   * @throws ConfigElementNotOfExpectedTypeException if the value of the key is not a collection / array.
   */
  <T> Collection<T> getMultiValue(String key) throws ConfigElementNotOfExpectedTypeException;

  /**
   * Returns the value of the given key. If the key is not present, null is returned.
   *
   * @param key The key of the value to return.
   *            The value of the key must be a map.
   * @throws ConfigElementNotOfExpectedTypeException if the value of the key is not a map.
   */
  <T> Map<T, Config> getMapValue(String key, Class<T> mapKeyType) throws ConfigElementNotOfExpectedTypeException;

  /**
   * @return The old value or null if there was no value.
   */
  <T> T setValue(String key, T value);

  /**
   * If the key does not exist, the supplier is called to create a new value.
   * It is saved in the config for the rest of the program.
   */
  <T> T getOrSetDefaultValue(String key, Supplier<T> o);

  /**
   * Returns the value of the given key as a new Config.
   *
   * @throws MandatoryConfigElementMissingException if the key is not present.
   */
  Config getSubConfig(String key);
}
