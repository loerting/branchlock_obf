package net.branchlock.structure.io;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Conversion;
import net.branchlock.commons.io.ClassStreamConverter;
import net.branchlock.commons.java.UncheckedFunction;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMember;
import net.branchlock.structure.BResource;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.structure.provider.IDataProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;

public class BDataReader implements IDataProvider {

  private final DataProvider dataProvider;
  private final Branchlock branchlock;
  private final boolean classifyAsLocal;
  private final Map<String, BClass> classes = new ConcurrentHashMap<>();
  private final Map<String, BResource> resources = new ConcurrentHashMap<>();
  private final Map<String, BResource> unloadableResources = new ConcurrentHashMap<>();
  private final Set<String> duplicateClassNames = ConcurrentHashMap.newKeySet();
  private final AtomicBoolean signatureWarningShown = new AtomicBoolean(false);
  private final boolean loadInnerJars;

  public BDataReader(DataProvider dataProvider, Branchlock branchlock, boolean classifyAsLocal) {
    this.dataProvider = dataProvider;
    this.branchlock = branchlock;
    this.classifyAsLocal = classifyAsLocal;
    this.loadInnerJars = this.branchlock.settingsManager.isLoadInnerJars();
  }

  public JarFile readFile(File jarFile, boolean classesOnly) throws IOException {
    JarFile openedJarFile = getJarFile(jarFile);
    ensureNoLayering(openedJarFile);
    Stream<JarEntry> entries = openedJarFile.stream().parallel();
    entries.forEach(z -> readEntry(z, openedJarFile::getInputStream, classesOnly));
    logErrors();
    return openedJarFile;
  }

  public void readInputStream(InputStream inputStream, boolean classesOnly) throws IOException {
    JarInputStream jarInputStream = new JarInputStream(inputStream);
    JarEntry entry;
    while ((entry = jarInputStream.getNextJarEntry()) != null) {
      readEntry(entry, e -> jarInputStream, classesOnly);
    }
    logErrors();
  }

  private JarFile getJarFile(File jarFile) throws IOException {
    if (jarFile == null)
      throw new IllegalArgumentException("The file provided is null.");
    if (jarFile.isDirectory())
      throw new IllegalArgumentException("Jar file is a directory: " + jarFile.getAbsolutePath());
    return new JarFile(jarFile, false);
  }

  private void logErrors() {
    if (!duplicateClassNames.isEmpty()) {
      Branchlock.LOGGER.error("{} duplicate class definitions found. Only one version will be stored back.", duplicateClassNames.size());
      for (String duplicateClassName : duplicateClassNames) {
        Branchlock.LOGGER.error("Duplicate class definition of {}", duplicateClassName);
      }
    }
  }

  private void ensureNoLayering(JarFile openedJarFile) {
    if (!classifyAsLocal) return;
    if (openedJarFile.getComment() != null && openedJarFile.getComment().contains("Branchlock")) {
      Branchlock.LOGGER.error("Already existing Branchlock obfuscation detected.");
      Branchlock.LOGGER.error("Layering does not improve protection, it only makes your program slower.");
      System.exit(-9);
    }
  }

  private void readEntry(JarEntry en, UncheckedFunction<JarEntry, InputStream> streamProvider, boolean classesOnly) {
    String entryName = en.getName();
    checkIfSignatureFile(entryName);
    try {
      InputStream entryInputStream = streamProvider.apply(en);
      if (loadInnerJars && entryName.endsWith(".jar")) {
        loadInnerJarAsLibrary(entryName, entryInputStream);

        // we still have to keep the jar file in the outer jar file.
        resources.put(entryName, new BResource(entryName));
        return;
      }
      byte[] bytes = ClassStreamConverter.toBytesOnlyClass(entryInputStream);
      if (bytes != null && !entryName.endsWith(".jnilib") && !entryName.endsWith("module-info.class") && !entryName.endsWith(".bin")) {
        final BClass cn = Conversion.toNode(dataProvider, bytes);
        boolean loadingSuccess = cn.name != null && (cn.superName != null || cn.name.equals("java/lang/Object"));
        if (loadingSuccess) {
          cn.setJarPath(entryName);
          cn.setLocal(classifyAsLocal);
          if (classes.containsKey(cn.name) && classifyAsLocal) {
            duplicateClassNames.add(cn.name);
            if (entryName.startsWith(cn.name) && entryName.endsWith(".class")) {
              // prefer the class version that is in the same package as the jar file and that is actually a class file.
              classes.put(cn.name, cn);
            }
            return;
          }
          classes.put(cn.name, cn);
        } else if (!classesOnly) {
          // these will be put back into the jar file just like they were before.
          unloadableResources.put(entryName, new BResource(entryName));
        }
      } else if (!classesOnly) {
        resources.put(entryName, new BResource(entryName));
      }
    } catch (Exception e) {
      Branchlock.LOGGER.error("Failed to load archive, entry {}", e, entryName);
    }
  }

  private void loadInnerJarAsLibrary(String jarName, InputStream is) {
    Branchlock.LOGGER.info("Loading inner jar file as library: {}", jarName);
    try {
      synchronized (classes) {
        dataProvider.loadLibraryJarInputStream(is);
      }
    } catch (IOException e) {
      Branchlock.LOGGER.error("Failed to load inner jar file as library: {}", e, jarName);
    }
  }

  private void checkIfSignatureFile(String entryName) {
    if (entryName.startsWith("META-INF/") && (entryName.endsWith(".SF") || entryName.endsWith(".DSA") || entryName.endsWith(".RSA"))) {
      if (!signatureWarningShown.get()) {
        Branchlock.LOGGER.warning("Signature file detected ({}), please only transform unsigned archives.", entryName);
        signatureWarningShown.set(true);
      }
    }
  }

  public Map<String, BClass> getClasses() {
    return classes;
  }

  @Override
  public Map<String, BClass> getLibs() {
    throw new UnsupportedOperationException("libraries are also returned using getClasses()");
  }

  public Map<String, BResource> getResources() {
    return resources;
  }

  public Map<String, BResource> getUnloadableResources() {
    return unloadableResources;
  }

  @Override
  public BClass resolveBClass(String className, BMember accessedFrom) {
    return classes.get(className);
  }


}
