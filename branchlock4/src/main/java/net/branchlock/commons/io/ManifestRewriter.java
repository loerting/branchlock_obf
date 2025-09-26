package net.branchlock.commons.io;

import net.branchlock.Branchlock;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BResource;
import net.branchlock.structure.provider.DataProvider;

import java.nio.charset.StandardCharsets;

public class ManifestRewriter {
  public static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
  private final DataProvider dataProvider;

  public ManifestRewriter(DataProvider dataProvider) {
    this.dataProvider = dataProvider;
  }

  private static String findNewName(DataProvider ic, String line) {
    String oldName = line.trim().replace(".", "/");
    BClass bClass = ic.findClassByOriginalName(oldName);

    if (bClass != null) {
      return bClass.name.replace("/", ".");
    } else {
      return oldName;
    }
  }

  /**
   * Updates all classes in the manifest file to their new names.
   */
  public void rewriteManifest() {
    BResource manifest = dataProvider.getResources().get(MANIFEST_PATH);
    if (manifest == null) {
      Branchlock.LOGGER.info("No manifest found, not updating attributes.");
      return;
    }
    try {
      byte[] bytes = dataProvider.getResource(manifest);
      manifest.replacement = rewriteManifest(new String(bytes, StandardCharsets.UTF_8));
    } catch (Exception e) {
      Branchlock.LOGGER.error("Failed to read or modify manifest file \"{}\".", e, MANIFEST_PATH);
    }
  }

  private byte[] rewriteManifest(String manifestContent) {
    StringBuilder sb = new StringBuilder();
    // support CRLF and LF
    String[] split = manifestContent.split("\r?\n");
    for (String line : split) {
      if (line.startsWith("Main-Class: ")) {
        String newName = findNewName(dataProvider, line.substring(12));
        sb.append("Main-Class: ").append(newName);
      } else if (line.startsWith("Entry-Point: ")) {
        String[] classes = line.substring(13).split(" ");
        for (int i = 0; i < classes.length; i++) {
          String clazz = classes[i];
          String newName = findNewName(dataProvider, clazz);
          classes[i] = newName;
        }
        sb.append("Entry-Point: ").append(String.join(" ", classes));
      } else {
        sb.append(line);
      }
      sb.append("\n");
    }
    sb.append("\n"); // double newline is intended.

    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }
}
