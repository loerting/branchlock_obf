package net.branchlock.structure.provider;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Conversion;
import net.branchlock.commons.asm.DependencyCycleDetector;
import net.branchlock.commons.io.ManifestRewriter;
import net.branchlock.structure.*;
import net.branchlock.structure.equivalenceclass.EquivalentMethodDefinitionFinder;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.structure.io.BDataExporter;
import net.branchlock.structure.io.BDataReader;
import net.branchlock.structure.io.MissingReferenceHandler;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class DataProvider implements IDataProvider, IDataStreamProvider {
  public final Branchlock branchlock;
  /**
   * When a class is added, its subclasses have to be populated.
   */
  private final Map<String, BClass> classes;
  private final Map<String, BClass> libs;
  private final Map<String, BClass> cachedRuntimeClasses = new ConcurrentHashMap<>();
  /**
   * Adding a resource will not add it to the output jar!
   */
  private final Map<String, BResource> resources;
  /**
   * Classes that failed to load, which will be kept as they are.
   */
  private final Map<String, BResource> unloadable;
  private final MissingReferenceHandler missingReferenceHandler;
  public JarFile openJarFile;
  private File inputFile;
  private boolean interactiveDemo;


  public DataProvider(Branchlock branchlock) {
    this.branchlock = branchlock;
    this.classes = new HashMap<>();
    this.libs = new HashMap<>();
    this.resources = new HashMap<>();
    this.unloadable = new HashMap<>();
    this.missingReferenceHandler = new MissingReferenceHandler(this);
  }

  public BClass getClassOrLib(String name) {
    BClass bClass = classes.get(name);
    if (bClass != null)
      return bClass;
    return libs.get(name);
  }

  public Stream<BClass> streamInputClasses() {
    return classes.values().stream();
  }

  public Stream<BMethod> streamInputMethods() {
    return classes.values().stream().flatMap(bClass -> bClass.methods.stream());
  }


  public Stream<BField> streamInputFields() {
    return classes.values().stream().flatMap(BClass::streamFields);
  }


  public Stream<Map.Entry<String, BClass>> streamClasspathEntries() {
    return Stream.concat(classes.entrySet().stream(), libs.entrySet().stream());
  }

  public Stream<BClass> streamClasspath() {
    return Stream.concat(classes.values().stream(), libs.values().stream());
  }

  public Stream<IEquivalenceClass<BMethod>> streamInputMethodEquivalenceClasses() {
    return streamInputMethods()
      .map(BMethod::requireEquivalenceClass)
      .distinct();
  }

  public Stream<BResource> streamResources() {
    return resources.values().stream();
  }

  public boolean containsClassOrLib(String name) {
    return classes.containsKey(name) || libs.containsKey(name);
  }

  /**
   * Loads classes and resources from a jar file.
   */
  public void loadInputJar(File jarFile, boolean classesOnly) throws IOException {
    if (openJarFile != null || !classes.isEmpty())
      throw new IllegalStateException("Already loaded!");

    BDataReader bDataReader = new BDataReader(this, branchlock, true);

    openJarFile = bDataReader.readFile(jarFile, classesOnly);
    inputFile = jarFile;

    classes.putAll(bDataReader.getClasses());
    resources.putAll(bDataReader.getResources());
    unloadable.putAll(bDataReader.getUnloadableResources());


    Branchlock.LOGGER.info("Loaded {} classes and {} resources.", classes.size(), resources.size());
    if (!unloadable.isEmpty()) {
      Branchlock.LOGGER.warning("{} class(es) failed to load and will be kept as they are:", unloadable.size());
      unloadable.keySet().stream().limit(10).forEach(Branchlock.LOGGER::warning);
      if (unloadable.size() > 10)
        Branchlock.LOGGER.warning("...");
    }
  }

  public void detectDependencyCycles() {
    // Dependency cycles are not supported, as they will cause StackOverflowErrors.
    // TODO test with real world examples
    Branchlock.LOGGER.info("Detecting dependency cycles...");
    List<DependencyCycleDetector.DependencyCycle> detectedCycles = new DependencyCycleDetector(this).detect();
    if (!detectedCycles.isEmpty()) {
      Branchlock.LOGGER.error("Dependency cycle(s) detected! Unable to run, as this will cause StackOverflowErrors.");
      Branchlock.LOGGER.error("Please remove the class(es) that are causing the cycle.");
      for (DependencyCycleDetector.DependencyCycle cycle : detectedCycles) {
        Branchlock.LOGGER.error("Cycle: " + cycle);
      }
      throw new IllegalStateException("Dependency cycles detected!");
    }
  }


  /**
   * Reads all .class files from the jar and adds them to the libs container.
   */
  public void loadLibraryJar(File jarFile) throws IOException {
    if (jarFile == null)
      throw new IllegalArgumentException("Null file.");
    if (jarFile.isDirectory()) {
      loadLibraryDir(jarFile);
      return;
    }
    if (!jarFile.exists())
      throw new IllegalArgumentException("Jar file does not exist: " + jarFile.getAbsolutePath());
    BDataReader bDataReader = new BDataReader(this, branchlock, false);
    bDataReader.readFile(jarFile, true);
    libs.putAll(bDataReader.getClasses());

    // We don't want to add the resources or not loadable library classes to the output jar.
  }
  public void loadLibraryJarInputStream(InputStream is) throws IOException {
    BDataReader bDataReader = new BDataReader(this, branchlock, false);
    bDataReader.readInputStream(is, true);
    libs.putAll(bDataReader.getClasses());
  }

  /**
   * Scan the directory recursively and load all .jar files.
   *
   * @param dir
   */
  private void loadLibraryDir(File dir) {
    if (!dir.isDirectory())
      throw new IllegalArgumentException("Not a directory: " + dir.getAbsolutePath());
    File[] files = dir.listFiles();
    if (files == null)
      return;
    for (File file : files) {
      if (file.isDirectory()) {
        loadLibraryDir(file);
      } else if (file.getName().endsWith(".jar")) {
        try {
          loadLibraryJar(file);
        } catch (IOException e) {
          Branchlock.LOGGER.error("Failed to load library jar: " + file.getAbsolutePath(), e);
        }
      }
    }
  }

  public int saveAsJar(File output, boolean crashDecompilerTools) {
    if (!branchlock.settingsManager.isAndroid())
      new ManifestRewriter(this).rewriteManifest();

    if (branchlock.settingsManager.isDebugMode())
      sanityCheck();

    BDataExporter bDataExporter = new BDataExporter(this, branchlock, openJarFile);
    bDataExporter.export(output, crashDecompilerTools);

    bDataExporter.logSizeChange(inputFile, output);
    Branchlock.LOGGER.info("Saved {} classes and {} resources.{}", classes.size(), resources.size(), System.lineSeparator());
    missingReferenceHandler.logMissingReferences();
    return 0; // TODO: return exit code (1 = error)
  }

  public void sanityCheck() {
    Branchlock.LOGGER.debug("Sanity check (debug)...");
    classes.forEach((name, bClass) -> {
      if (!bClass.getName().equals(name))
        throw new IllegalStateException("Class map name mismatch: " + name + " != " + bClass.getName());
      if (bClass.invalidated)
        throw new IllegalStateException("Destroyed class in class list found: " + name);
      bClass.directSubClasses.stream().filter(bClass2 -> bClass2.invalidated).findAny().ifPresent(bClass2 -> {
        throw new IllegalStateException("Destroyed sub class found in class " + name + ": " + bClass2.getName());
      });
      bClass.methods.forEach(bm -> {
        if (bm.requireEquivalenceClass() == null)
          throw new IllegalStateException("Equivalence class is null for method " + bm);
        if (!bm.requireEquivalenceClass().isValidInstance()) {
          throw new IllegalStateException("Equivalence class is not valid anymore for method " + bm);
        }
        Set<BMethod> members = bm.requireEquivalenceClass().getMembers();

        if(bm.hasMultiEquivalenceClass()) {
          if(bm.isStatic()) {
            throw new IllegalStateException("Static method " + bm + " has multiple equivalence classes!");
          }
          if(bm.requireEquivalenceClass().isLocal()) {
            EquivalentMethodDefinitionFinder emdf = new EquivalentMethodDefinitionFinder(bm);
            emdf.findEquivalentDefinitions();
            if (!members.equals(emdf.getResults())) {
              throw new IllegalStateException("Local equivalence class does not match newly inferred equivalent method definitions: " + members + " != " + emdf.getResults() + " for " + bm);
            }
          }
        }

        members.stream().filter(bm2 -> bm2.getOwner().invalidated).findAny().ifPresent(bMethod -> {
          throw new IllegalStateException("Destroyed method owner found in equivalence class: " + bMethod);
        });
        // members in the same equivalence class should have the same salt value
        Integer saltValue = null;
        for (BMethod member : bm.requireEquivalenceClass().getMembers()) {
          if (member.getSalt() == null)
            continue;
          if (saltValue == null)
            saltValue = member.getSalt().getValue();
          else if (saltValue != member.getSalt().getValue())
            throw new IllegalStateException("Salt value mismatch in equivalence class " + bm.requireEquivalenceClass());
        }
      });
    });
  }

  public byte[] getResource(BResource bRes) {
    return getResource(bRes.name);
  }

  private byte[] getResource(String name) {
    try {
      return IOUtils.toByteArray(openJarFile.getInputStream(openJarFile.getJarEntry(name)));
    } catch (IOException e) {
      Branchlock.LOGGER.error("Failed to get resource {}", e, name);
      return null;
    }
  }

  public JarEntry getJarEntry(BResource bRes) {
    return openJarFile.getJarEntry(bRes.name);
  }

  @Override
  public Map<String, BClass> getClasses() {
    return Collections.unmodifiableMap(classes);
  }

  @Override
  public Map<String, BClass> getLibs() {
    return Collections.unmodifiableMap(libs);
  }

  @Override
  public Map<String, BResource> getUnloadableResources() {
    return Collections.unmodifiableMap(unloadable);
  }

  @Override
  public Map<String, BResource> getResources() {
    return Collections.unmodifiableMap(resources);
  }

  /**
   * @param className    The name of the class to get. Packages are separated by slashes.
   * @param accessedFrom
   * @return the class with the given name, or null if it doesn't exist.
   */
  @Override
  public BClass resolveBClass(String className, BMember accessedFrom) {
    if (className == null)
      return null;
    if (className.startsWith("[") || className.endsWith(";"))
      throw new IllegalArgumentException("Wrong format for class resolving: " + className);
    if ("java/lang/Object".equals(className))
      return resolveRuntimeBClass(className); // speed up the most common case
    BClass bClass = classes.get(className);
    if (bClass != null)
      return bClass;
    bClass = libs.get(className);
    if (bClass != null)
      return bClass;
    BClass runtimeClass = resolveRuntimeBClass(className);
    // ignore missing references if the input file isn't fully loaded yet.
    if ((accessedFrom == null || accessedFrom.isLocal()) && runtimeClass == null && inputFile != null) {
      missingReferenceHandler.observeMissingReference(className, accessedFrom);
    }
    return runtimeClass;
  }

  /**
   * Resolves a class from the classpath.
   * This method is synchronized because we do not want two classes initializing at the same time.
   *
   * @param className
   * @return
   */
  public synchronized BClass resolveRuntimeBClass(String className) {
    if (className == null)
      return null;
    if (cachedRuntimeClasses.containsKey(className)) // has to be outside of the lock
      return cachedRuntimeClasses.get(className);
    BClass bClass = Conversion.loadClasspathNode(this, className);
    if (bClass == null) {
      return null;
    }
    cachedRuntimeClasses.put(className, bClass);
    populateSubClassesForParents(bClass);
    // equivalence classes have to be updated here, else the runtime class methods would not pop up in input methods equivalence classes.
    bClass.updateMethodEquivalenceClasses();
    return bClass;
  }

  public void prepareAllClasses() {
    Branchlock.LOGGER.info("Populating class hierarchy and resetting equivalence classes...");

    populateSubClasses();

    getClasses().values().forEach(BClass::resetMethodEquivalenceClasses);
    getLibs().values().forEach(BClass::resetMethodEquivalenceClasses);

    // Equivalence classes are currently being updated dynamically, so this is not needed:
    // getClasses().values().forEach(BClass::updateMethodEquivalenceClasses);
    // getLibs().values().forEach(BClass::updateMethodEquivalenceClasses);
  }

  /**
   * Populates the subclasses for all classes, including library or runtime classes.
   * If at any point later a class is added to the project, this method must be called again.
   */
  public void populateSubClasses() {
    // clear() to avoid old classes directSubClasses
    for (BClass bClass : classes.values()) {
      bClass.directSubClasses.clear();
    }
    for (BClass bClass : libs.values()) {
      bClass.directSubClasses.clear();
    }

    for (BClass bClass : classes.values()) {
      populateSubClassesForParents(bClass);
    }
    for (BClass bClass : libs.values()) {
      populateSubClassesForParents(bClass);
    }
    // we don't need to populate subclasses for cached runtime classes, as they are populated when they are loaded.
  }

  public void populateSubClassesForParents(BClass bClass) {
    Set<BClass> directParentClasses = bClass.getDirectParentClasses();
    for (BClass parentClass : directParentClasses) {
      parentClass.directSubClasses.add(bClass);
    }
  }

  /**
   * Adds a class to the output jar.
   */
  public void addClass(BClass bClass) {
    if (classes.containsKey(bClass.name)) {
      throw new IllegalArgumentException("Class already exists: " + bClass.name);
    }
    classes.put(bClass.name, bClass);
    bClass.setLocal(true);
    populateSubClassesForParents(bClass);
  }

  /**
   * Adds multiple classes to the output jar.
   */
  public void addClasses(Collection<BClass> bClasses) {
    for (BClass bClass : bClasses) {
      addClass(bClass);
    }
  }

  public BClass findClassByOriginalName(String oldName) {
    for (BClass bClass : classes.values()) {
      if (oldName.equals(bClass.getOriginalName()))
        return bClass;
    }
    return null;
  }

  public boolean isLocal(String className) {
    return classes.containsKey(className);
  }

  public boolean isRuntimeClass(String className) {
    if (classes.containsKey(className))
      return false;
    if (libs.containsKey(className))
      return false;

    return cachedRuntimeClasses.containsKey(className) || resolveRuntimeBClass(className) != null;
  }

  public boolean isInteractiveDemo() {
    return interactiveDemo;
  }

  public void setInteractiveDemo(boolean interactiveDemo) {
    this.interactiveDemo = interactiveDemo;
  }

  public BClass removeClass(String name) {
    BClass bClass = classes.remove(name);
    if (bClass != null) {
      bClass.methods.forEach(BMethod::resetEquivalenceClass);
      bClass.getDirectParentClasses().forEach(parent -> parent.directSubClasses.remove(bClass));

      bClass.reset();
      bClass.setLocal(false);
      bClass.invalidate();
    }
    return bClass;
  }

  public void updateClasses(Map<BClass, BClass> newNodes) {
      newNodes.keySet().forEach(oldNode -> {
          classes.remove(oldNode.getName());
          oldNode.reset();
      });

      newNodes.forEach((oldNode, newNode) -> {
          oldNode.updateClass(newNode);
          classes.put(oldNode.getName(), oldNode);
          newNode.reset();
          newNode.invalidate();
      });

    prepareAllClasses();
  }

  public void updateClass(BClass oldNode, BClass newNode) {
    Set<BClass> copyOfDirectSubClasses = new HashSet<>(oldNode.directSubClasses);

    classes.remove(oldNode.getName());
    oldNode.reset();
    oldNode.updateClass(newNode);
    classes.put(oldNode.getName(), oldNode);

    // update class again
    oldNode.directSubClasses.clear();
    oldNode.directSubClasses.addAll(copyOfDirectSubClasses);
    oldNode.updateMethodEquivalenceClasses();
  }

  public BClass randomClass() {
    return classes.values().iterator().next();
  }
}
