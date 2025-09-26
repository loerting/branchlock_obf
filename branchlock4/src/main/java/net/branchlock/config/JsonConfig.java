package net.branchlock.config;

import mjson.Json;
import net.branchlock.config.exception.ConfigElementNotOfExpectedTypeException;
import net.branchlock.config.exception.MandatoryConfigElementMissingException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class JsonConfig implements Config {
  private final Json json;

  public JsonConfig(Json json) {
    this.json = json;
  }

  private static <T> T tryToCast(String key, Object from, Class<?> type) {
    if (from instanceof Long && type == Integer.class) {
      return (T) Integer.valueOf(((Number) from).intValue());
    }
    if (from instanceof Double && type == Float.class) {
      return (T) Float.valueOf(((Number) from).floatValue());
    }
    if (from instanceof String && type == Integer.class) {
      // try fallback.
      try {
        return (T) ((Integer) Integer.parseInt((String) from));
      } catch (NumberFormatException e) {
        // ignore and continue.
      }
    }
    if (type.isInstance(from)) {
      return (T) from;
    } else {
      if (type == String.class) {
        return (T) from.toString();
      } else {
        throw new ConfigElementNotOfExpectedTypeException(key, type, from.getClass(), from);
      }
    }
  }

  @Override
  public <T> T getOrDefaultValue(String key, T o) throws ConfigElementNotOfExpectedTypeException {
    if (json.has(key)) {
      Object value = json.at(key).getValue();
      return value == null ? o : tryToCast(key, value, o);
    } else {
      return o;
    }
  }

  @Override
  public <T> T getMandatoryValue(String key) throws MandatoryConfigElementMissingException, ConfigElementNotOfExpectedTypeException {
    if (json.has(key)) {
      Object value = json.at(key).getValue();
      return value == null ? null : tryToCast(key, value, value.getClass());
    } else {
      throw new MandatoryConfigElementMissingException(key);
    }
  }

  @Override
  public boolean has(String key) {
    return json.has(key);
  }

  @Override
  public <T> Collection<T> getMultiValue(String key) throws ConfigElementNotOfExpectedTypeException {
    if (json.has(key)) {

      Json at = json.at(key);
      if (!at.isArray()) {
        throw new ConfigElementNotOfExpectedTypeException(key, List.class, at.getValue().getClass());
      }
      List<Object> value = at.asList();
      return value == null ? null : (Collection<T>) value;
    } else {
      return null;
    }
  }

  @Override
  public <T> Map<T, Config> getMapValue(String key, Class<T> mapKeyType) throws ConfigElementNotOfExpectedTypeException {
    if (json.has(key)) {
      Json at = json.at(key);
      if (!at.isObject()) {
        throw new ConfigElementNotOfExpectedTypeException(key, Map.class, at.getValue().getClass());
      }
      Map<String, Json> value = at.asJsonMap();
      // convert the map to a map of configs
      Map<T, Config> result = new HashMap<>();
      for (Map.Entry<String, Json> entry : value.entrySet()) {
        result.put(tryToCast(key, entry.getKey(), mapKeyType), new JsonConfig(entry.getValue()));
      }
      return result;
    } else {
      return null;
    }
  }

  @Override
  public <T> T setValue(String key, T value) {
    Json old = json.at(key);
    json.set(key, value);
    return (T) (old != null ? old.getValue() : null);
  }

  @Override
  public <T> T getOrSetDefaultValue(String key, Supplier<T> o) {
    if (json.has(key)) {
      return getMandatoryValue(key);
    } else {
      T value = o.get();
      json.set(key, value);
      return value;
    }
  }

  @Override
  public Config getSubConfig(String key) {
    if (json.has(key)) {
      return new JsonConfig(json.at(key));
    } else {
      throw new MandatoryConfigElementMissingException(key);
    }
  }

  /**
   * Try to cast the value to the type of the default value, or throw ConfigElementNotOfExpectedTypeException.
   */
  private <T> T tryToCast(String key, Object from, T desiredTypeObject) {
    if (desiredTypeObject == null) return null;
    Class<?> type = desiredTypeObject.getClass();
    return tryToCast(key, from, type);
  }
}
