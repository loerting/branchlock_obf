package net.branchlock.stacktrace;


import net.branchlock.Branchlock;
import net.branchlock.config.Config;
import net.branchlock.cryptography.DESedeECB;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackTraceDecryption {
  private static final Pattern STACKTRACE_PATTERN = Pattern.compile("\\((.{6,})\\)"); //"\\((.+:\\b\\d+\\b)\\)"
  private static final Set<String> JAVA_SOURCE_ENDINGS = Set.of(".java", ".kt", ".groovy", ".scala", ".kts");

  private static String decryptStacktrace(String key, String stacktrace) {
    Branchlock.LOGGER.info("Trying to decrypt stacktrace.");
    DESedeECB des = new DESedeECB(key);

    Matcher m = STACKTRACE_PATTERN.matcher(stacktrace);
    StringBuilder sb = new StringBuilder(stacktrace.length());

    int errors = 0;
    int successes = 0;

    while (m.find()) {
      String text = m.group(1);
      try {
        String[] split = text.split(":");
        String firstSeg = split[0].trim();
        if (firstSeg.equals("Unknown Source") || firstSeg.equals("Native Method") || JAVA_SOURCE_ENDINGS.stream().anyMatch(firstSeg::endsWith))
          continue;

        // check if firstSeg is base64
        if (!firstSeg.matches("[A-Za-z0-9+/=]+"))
          continue;

        String decrypted = des.decrypt(firstSeg);
        int line = split.length > 1 ? des.decryptLine(Integer.parseInt(split[1])) : -1;
        String decoded = line <= 0 ? decrypted : decrypted + ":" + line;
        m.appendReplacement(sb, Matcher.quoteReplacement("(" + decoded + ")"));
        successes++;
      } catch (IllegalBlockSizeException ibse) {
        // probably false positive
        errors++;
      } catch (BadPaddingException bpe) {
        m.appendReplacement(sb, Matcher.quoteReplacement("(Decryption failed)"));
        errors++;
      } catch (Exception e) {
        Branchlock.LOGGER.error("Error while decrypting: {}", e.toString());
        m.appendReplacement(sb, Matcher.quoteReplacement("(Decryption failure: [" + e + "])"));
        errors++;
      }
    }
    m.appendTail(sb);
    if (errors > 0) {
      if (successes == 0) {
        Branchlock.LOGGER.info("Failed to decrypt anything. Please check your decryption key.");
      } else {
        Branchlock.LOGGER.error("{} line(s) failed to decrypt.", errors);
      }
    }
    Branchlock.LOGGER.info("{} line(s) successfully decrypted.", successes);
    String s = sb.toString();
    if (s.trim().isEmpty()) {
      s += "Failed to decrypt anything. Please check your decryption key.";
    }
    return s;
  }

  public static int decryptStacktraceConfig(Config config) {
    String key = config.getOrDefaultValue("key", "");
    if (key == null || key.trim().isEmpty()) {
      Branchlock.LOGGER.error("Stacktrace decryption key is empty or null.");
      return 1;
    }
    File input = new File((String) config.getMandatoryValue("input"));
    if (!input.exists() || !input.isFile()) {
      Branchlock.LOGGER.error("Stacktrace input file not found.");
      return 1;
    }
    File output = new File((String) config.getMandatoryValue("output"));
    Branchlock.LOGGER.info("Decrypting stacktrace and writing to output...");
    try {
      String decrypted = StackTraceDecryption.decryptStacktrace(key, new String(Files.readAllBytes(input.toPath())));
      Files.write(output.toPath(), decrypted.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      return 0;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
