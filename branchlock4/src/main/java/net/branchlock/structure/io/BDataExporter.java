package net.branchlock.structure.io;

import net.branchlock.Branchlock;
import net.branchlock.commons.string.StringUtils;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BResource;
import net.branchlock.structure.provider.DataProvider;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

public class BDataExporter {
  private final DataProvider dataProvider;
  private final Branchlock branchlock;
  private final JarFile openJarFile;

  public BDataExporter(DataProvider dataProvider, Branchlock branchlock, JarFile openJarFile) {
    this.dataProvider = dataProvider;
    this.branchlock = branchlock;
    this.openJarFile = openJarFile;
    if (openJarFile == null)
      throw new IllegalArgumentException("openJarFile cannot be null");
  }

  public void export(File output, boolean crashDecompilerTools) {
    // create the directories of the output file if they don't exist
    File parentFile = output.getParentFile();
    if (parentFile != null && !parentFile.exists()) {
      if (!parentFile.mkdirs()) {
        throw new UnsupportedOperationException("Could not create directories for output file: " + output.getAbsolutePath());
      }
    }

    try (JarOutputStream out = new JarOutputStream(new FileOutputStream(output))) {
      if (branchlock.settingsManager.isDemo()) {
        out.setComment("Obfuscated using a demo version of the Branchlock obfuscator for java - https://branchlock.net");
      } else if (!branchlock.settingsManager.isNoWatermark()) {
        out.setComment(openJarFile.getComment() != null ? openJarFile.getComment() : "Obfuscated using the Branchlock obfuscator for java - https://branchlock.net");
      }

      boolean noCompress = branchlock.settingsManager.isNoCompression();
      out.setLevel(noCompress ? Deflater.NO_COMPRESSION : Deflater.BEST_COMPRESSION);
      if (noCompress)
        Branchlock.LOGGER.info("Disabling jar compression.");

      saveResources(out);
      saveClasses(crashDecompilerTools, out);

    } catch (IOException e) {
      Branchlock.LOGGER.error("Failed to save archive.", e);
    } finally {
      try {
        openJarFile.close();
      } catch (IOException e) {
        Branchlock.LOGGER.error("Failed to close jar file.", e);
      }
    }
  }


  private void saveClasses(boolean crashDecompilerTools, JarOutputStream out) {
    Map<String, byte[]> writtenClasses = new OutputConverter(dataProvider).toBytecode();
    for (Map.Entry<String, BClass> classEntry : dataProvider.getClasses().entrySet()) {
      String newName = classEntry.getKey();
      BClass bc = classEntry.getValue();

      byte[] bytes = writtenClasses.get(newName);

      if (bytes == null) {
        Branchlock.LOGGER.error("Failed to find class entry {}", newName);
        bytes = new byte[0];
      }

      String extension = crashDecompilerTools ? ".class/" : ".class";

      String oldPath = bc.getJarPath();
      String newPath = getNewPathForClass(oldPath, bc.getOriginalName(), newName + extension);
      try {
        JarEntry oldEntry = oldPath != null ? openJarFile.getJarEntry(oldPath) : null;
        JarEntry entry = cloneOldEntryWithNewData(oldEntry, newPath, bytes);
        out.putNextEntry(entry);
        out.write(bytes);
        out.closeEntry();
      } catch (Exception e) {
        Branchlock.LOGGER.error("Failed at class entry {}", e, newPath);
      }
    }
  }

  private static String getNewPathForClass(String oldPath, String originalName, String newNamePath) {
    if (oldPath != null && originalName != null) {
      int lastDot = oldPath.lastIndexOf('.');
      String oldPathNoExtension = lastDot > 0 ? oldPath.substring(0, lastDot) : oldPath;

      if (!oldPathNoExtension.equals(originalName)) {
        // class name did not equal path name, so it was probably in a folder or a mismatching name
        if (oldPathNoExtension.endsWith(originalName)) {
          // classes like in spring applications. e.g. BOOT-INF/classes/com/example/MyClass.class are in folders
          String folder = oldPathNoExtension.substring(0, oldPathNoExtension.length() - originalName.length() - 1);

          return folder + "/" + newNamePath;
        } else {
          // if we have a full mismatch, just use the old path instead
          return oldPath;
        }
      }
    }
    return newNamePath;
  }

  private void saveResources(JarOutputStream out) {
    boolean removeEmptyDirectories = branchlock.settingsManager.isRemoveEmptyDirs();
    Set<String> writtenFiles = new HashSet<>();
    AtomicInteger javaSourceFiles = new AtomicInteger();

    Map<String, BResource> toExport = new HashMap<>();

    toExport.putAll(dataProvider.getResources());
    toExport.putAll(dataProvider.getUnloadableResources()); // also add those files back into the jar, which should remain the same.
    toExport.forEach((origName, bRes) -> {
      String name = bRes.name;
      if (name.endsWith(".java") || name.endsWith(".kt") || name.endsWith(".scala") || name.endsWith(".groovy")) {
        javaSourceFiles.incrementAndGet();
      }
      if (!writtenFiles.add(name)) {
        Branchlock.LOGGER.warning("Skipping duplicate jar entry {} ", name);
        return;
      }

      JarEntry oldEntry = openJarFile.getJarEntry(origName); // could be null (new file)

      if (removeEmptyDirectories && oldEntry != null && oldEntry.isDirectory()) return;
      try {
        byte[] bytes = oldEntry == null ? new byte[0] : IOUtils.toByteArray(openJarFile.getInputStream(oldEntry));
        JarEntry newEntry = cloneOldEntryWithNewData(oldEntry, name, bytes);

        out.putNextEntry(newEntry);
        out.write(bRes.replacement != null ? bRes.replacement : bytes);
        out.closeEntry();
      } catch (Exception e) {
        Branchlock.LOGGER.error("Failed at resource jar entry {} ", e, name);
      }
    });

    if (javaSourceFiles.get() > 0) {
      Branchlock.LOGGER.warning("{} Java / JVM language source file(s) found in the jar file, you could possibly leak source code.",
        javaSourceFiles.get());
    }
  }

  private static JarEntry cloneOldEntryWithNewData(JarEntry oldEntry, String newName, byte[] bytes) {
    JarEntry newEntry = new JarEntry(newName);
    if(oldEntry == null) return newEntry;

    newEntry.setTime(oldEntry.getTime());
    newEntry.setComment(oldEntry.getComment());
    newEntry.setExtra(oldEntry.getExtra());
    if(oldEntry.getCreationTime() != null)
      newEntry.setCreationTime(oldEntry.getCreationTime());
    if(oldEntry.getLastAccessTime() != null)
      newEntry.setLastAccessTime(oldEntry.getLastAccessTime());
    if(oldEntry.getLastModifiedTime() != null)
      newEntry.setLastModifiedTime(oldEntry.getLastModifiedTime());

    if(oldEntry.getMethod() == JarEntry.STORED) {
      newEntry.setMethod(JarEntry.STORED);
      newEntry.setSize(bytes.length);
      newEntry.setCompressedSize(bytes.length);

      CRC32 crc = new CRC32();
      crc.update(bytes);
      newEntry.setCrc(crc.getValue());
    } else {
      newEntry.setMethod(JarEntry.DEFLATED);
    }
    return newEntry;
  }

  public void logSizeChange(File inputFile, File output) {
    String sizeChange = StringUtils.toNumInUnits(output.length() - inputFile.length()).trim();
    if (!sizeChange.startsWith("-"))
      sizeChange = "+" + sizeChange;

    String percentage = String.valueOf(Math.round((output.length() / (double) inputFile.length() * 100) - 100));
    if (!percentage.startsWith("-"))
      percentage = "+" + percentage;
    Branchlock.LOGGER.info("File size changed by {} or {}%. ({} -> {})", sizeChange, percentage, StringUtils.toNumInUnits(inputFile.length()),
      StringUtils.toNumInUnits(output.length()));
  }

}
