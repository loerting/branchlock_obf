package net.branchlock.task.implementation.references.strings;

import net.branchlock.Branchlock;
import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.implementation.references.drivers.FieldInlinerDriver;
import net.branchlock.task.implementation.references.strings.driver.CompatibilityStringDriver;
import net.branchlock.task.implementation.references.strings.driver.ConcatFactoryLifterDriver;
import net.branchlock.task.metadata.TaskMetadata;

import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.List;

@TaskMetadata(name = "String encryption", priority = TaskMetadata.Level.SEVENTH, performanceCost = TaskMetadata.PerformanceCost.MINIMAL, ids = "strings")
public class Strings extends Task {

  private final int minLength = innerConfig.getOrDefaultValue("min_length", 0);
  private int maxLength = innerConfig.getOrDefaultValue("max_length", Integer.MAX_VALUE);

  public Strings(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  private static boolean isValidURL(String string) {
    try {
      URL ignored = new URI(string).toURL();
      return true;
    } catch (Exception ignored) {
    }
    return false;
  }

  private static boolean hasHighRatioOfSpecialCharacters(String string) {
    int specialCharacters = 0;
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if (c < 32 || c > 126) {
        specialCharacters++;
      }
    }
    return specialCharacters > string.length() * 0.25;
  }

  private static boolean isBase64(String string) {
    if (string.length() % 4 != 0) return false;
    try {
      String res = new String(Base64.getDecoder().decode(string));
      return !hasHighRatioOfSpecialCharacters(res);
    } catch (Exception ignored) {
    }
    return false;
  }

  public static boolean possiblyMalicious(String string) {
    string = string.trim();
    if (string.length() <= 3) return false;
    if (string.length() >= 256) return true;

    if (isValidURL(string)) return true;

    if (!string.contains(" ") && string.endsWith("=")) return true;
    if (string.length() >= 8 && isBase64(string)) return true;

    if (hasHighRatioOfSpecialCharacters(string)) return true;

    // check for possibly dangerous strings
    string = string.toLowerCase();
    if (string.endsWith(".class")) return true;
    if (string.contains("http") || string.contains("https")) return true;
    if (string.contains(".exe") || string.contains(".dll") || string.contains(".jar")) return true;
    if (string.contains(".bat") || string.contains(".sh") || string.contains(".cmd")) return true;
    if (string.contains("hkey_") || string.contains("hklm") || string.contains("hkcu")) return true;
    return string.contains("@echo") || string.contains("powershell");
  }

  @Override
  public void preExecute() {
    if (maxLength <= 0) maxLength = Integer.MAX_VALUE;
    if (maxLength <= minLength) {
      Branchlock.LOGGER.warning("Max length is less than min length, ignoring max length.");
      maxLength = Integer.MAX_VALUE;
    }
    if (settingsManager.isDemo()) {
      Branchlock.LOGGER.warning("Demo mode does not allow obfuscation of sensitive data like URLs, IP addresses or similar.");
      Branchlock.LOGGER.warning("This is to prevent ToS abuse, like using it to obfuscate malware.");
    }
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    return List.of(new FieldInlinerDriver(this, f -> (f.value instanceof String)),
      new ConcatFactoryLifterDriver(this),
      new CompatibilityStringDriver(this)
    );
  }

  public boolean canEncrypt(String string) {
    boolean lengthCheck = string.length() >= minLength && string.length() <= maxLength;
    if (!lengthCheck) return false;

    if (settingsManager.isDemo()) {
      return !possiblyMalicious(string);
    }
    return true;
  }
}
