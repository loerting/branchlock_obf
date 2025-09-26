package net.branchlock.structure;

import net.branchlock.commons.asm.*;
import net.branchlock.commons.generics.Placeholder;
import net.branchlock.commons.java.UnusableList;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.structure.provider.IDataProvider;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BClass extends ClassNode implements BMember, Opcodes {
  /**
   * The methods of this class.
   */
  public final BMemberContainer<BMethod> methods;
  /**
   * The methods of this class.
   */
  public final BMemberContainer<BField> fields;

  /**
   * The currently known direct subclasses (classes that extend or implement this class) of this class.
   * This is populated by {@link DataProvider#populateSubClasses()}.
   * It may not be complete.
   */
  public final Set<BClass> directSubClasses = ConcurrentHashMap.newKeySet();
  protected final IDataProvider dataProvider;
  private String originalName;
  /**
   * Specifies whether this class is in the input jar.
   */
  private boolean localClass;
  public boolean invalidated;

  /**
   * The original file path inside the jar file this class has been read from. Can be null.
   */
  private String jarPath;

  public BClass(IDataProvider dataProvider) {
    super(Opcodes.ASM9);
    this.dataProvider = Objects.requireNonNull(dataProvider);
    this.methods = new BMemberContainer<>();
    this.fields = new BMemberContainer<>();

    // make sure they are unmodifiable to prevent wrong usage.
    super.methods = new UnusableList<>("use BClass methods instead");
    super.fields = new UnusableList<>("use BClass fields instead");
  }

  public String getPackage() {
    String owner = this.name;
    if (owner.indexOf('/') == -1)
      return "";
    return owner.substring(0, owner.lastIndexOf('/') + 1);
  }

  public void raiseMinAPI(MajorVersion ver) {
    this.version = Math.max(ver.getCode(), this.version);
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String getDescriptor() {
    throw new UnsupportedOperationException();
  }

  public String getRuntimeName() {
    return this.name.replace('/', '.');
  }

  public Set<BClass> getDirectParentClasses() {
    Set<BClass> parents = new HashSet<>();
    BClass parent = getSuperClass();
    if (parent != null) parents.add(parent);
    parents.addAll(getInterfaces());
    return parents;
  }

  public BClass getSuperClass() {
    return dataProvider.resolveBClass(this.superName, this);
  }

  public Set<BClass> getInterfaces() {
    Set<BClass> interfaces = new HashSet<>();
    for (String iface : this.interfaces) {
      BClass ifaceClass = dataProvider.resolveBClass(iface, this);
      if (ifaceClass != null) {
        interfaces.add(ifaceClass);
      }
    }
    return interfaces;
  }


  public boolean isLocal() {
    return localClass;
  }

  public void setLocal(boolean local) {
    this.localClass = local;
  }

  @Override
  public boolean hasOwner() {
    return false;
  }

  @Override
  public BMember getOwner() {
    throw new UnsupportedOperationException();
  }

  public String getOriginalName() {
    if (originalName == null) {
      return this.name;
    }
    return originalName;
  }

  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  /**
   * @return true if an instance of this class could be stored in a variable of type {@code superClass}
   */
  public boolean isAssertableTo(String superClass) {
    if ("java/lang/Object".equals(superClass)) {
      return true;
    }
    if (superClass.equals(this.name) || (this.interfaces.contains(superClass))) {
      return true;
    }
    return getDirectParentClasses().stream().anyMatch(bClass -> bClass.isAssertableTo(superClass));
  }

  public boolean isAssertableToAny(Set<String> superClasses) {
    if (superClasses.contains("java/lang/Object")) {
      return true;
    }
    if (superClasses.contains(this.name) || this.interfaces.stream().anyMatch(superClasses::contains)) {
      return true;
    }
    return getDirectParentClasses().stream().anyMatch(bClass -> bClass.isAssertableToAny(superClasses));
  }

  /**
   * Tests whether the predicate is true for this class and all super classes of this class.
   */
  public boolean matchesOrParent(Predicate<BClass> predicate) {
    return predicate.test(this) || getDirectParentClasses().stream().anyMatch(bClass -> bClass.matchesOrParent(predicate));
  }

  public boolean matchesOrChild(Predicate<BClass> predicate) {
    return predicate.test(this) || directSubClasses.stream().anyMatch(c -> c.matchesOrChild(predicate));
  }

  public void addMethod(BMethod method) {
    if (method.getOwner() != this)
      throw new IllegalArgumentException("Method does not belong to this class");
    methods.add(method);
    method.updateEquivalenceClass();
  }

  public void addField(BField field) {
    if (field.getOwner() != this)
      throw new IllegalArgumentException("Field does not belong to this class");
    this.fields.add(field);
  }

  public boolean isInterface() {
    return Access.isInterface(this.access);
  }

  public boolean isEnum() {
    return Access.isEnum(this.access);
  }

  public boolean isAnnotation() {
    return Access.isAnnotation(this.access) || isAssertableTo("java/lang/annotation/Annotation");
  }

  public int ensureLegalAccess(BClass from, int access) {
    if (Access.isPublic(access))
      return access;
    boolean samePackage = getPackage().equals(from.getPackage());

    boolean privIllegal = Access.isPrivate(access) && !from.getName().equals(getName());
    boolean protIllegal = Access.isProtected(access) && !samePackage; /* TODO find out why this does not work: && !from.isAssertableTo(owner.getName()); */
    boolean packPrivIllegal = !Access.isPrivate(access) && !Access.isProtected(access) && !samePackage;

    if (privIllegal || protIllegal || packPrivIllegal) {
      return samePackage ? Access.makeProtected(access) : Access.makePublic(access);
    }
    return access;
  }

  public boolean hasAccess(int access) {
    return (this.access & access) == access;
  }
  @Override
  public String toString() {
    if (invalidated) {
      return "[InvalidatedBClass]";
    }
    return this.name;
  }

  public Stream<BField> streamFields() {
    return this.fields.stream();
  }

  public boolean hasAnnotation(String annotation) {
    return Annotations.has(annotation, this.visibleAnnotations, this.invisibleAnnotations);
  }

  @Override
  public int getAccess() {
    return this.access;
  }

  @Override
  public void setAccess(int access) {
    if (!isLocal()) throw new IllegalStateException("Cannot set access on non-local class");
    this.access = access;
  }

  @Override
  public String getIdentifier() {
    return this.name;
  }

  /**
   * Method resolution specified by JVM §5.4.3.3.
   * It is fundamentally the same for static and virtual methods.
   * TODO: this is 99% correct but a small case for interface is wrong.
   *
   * @param name The name of the method
   * @param desc The descriptor of the method
   * @return The equivalence class of this method.
   */
  public BMethod resolveMethod(String name, String desc) {
    BMethod found = methods.get(name, desc);
    if (found != null) return found;
    BClass superClass = getSuperClass();
    if (superClass != null) {
      BMethod superMethod = superClass.resolveMethod(name, desc);
      if (superMethod != null) return superMethod;
    }
    for (BClass itfClass : getInterfaces()) {
      BMethod parentMethod = itfClass.resolveMethod(name, desc);
      if (parentMethod != null) return parentMethod;
    }
    return null;
  }


  /**
   * Field resolution specified by JVM §5.4.3.2.
   * It is fundamentally the same for static and virtual fields.
   * Field resolution is different from method resolution. It first checks the interfaces, then the super class.
   *
   * @param name The name of the field
   * @param desc The descriptor of the field
   * @return The field, or null if it could not be found.
   */
  public BField resolveField(String name, String desc) {
    BField found = fields.get(name, desc);
    if (found != null) return found;

    for (BClass itfClass : getInterfaces()) {
      BField parentMethod = itfClass.resolveField(name, desc);
      if (parentMethod != null) return parentMethod;
    }

    BClass superClass = getSuperClass();
    if (superClass != null) {
      BField superMethod = superClass.resolveField(name, desc);
      if (superMethod != null) return superMethod;
    }
    return null;
  }

  public void removePlaceholderFields() {
    this.fields.removeIf(fieldNode -> Annotations.has(Placeholder.class.getName().replace('.', '/'), fieldNode.visibleAnnotations, fieldNode.invisibleAnnotations));
  }

  public boolean isNameChangeable() {
    return isLocal()
      && !hasAnnotation("RetainSignature")
      && !hasAnnotation("org/springframework/boot/autoconfigure/SpringBootApplication")
      && !methods.containsSignature("main", "([Ljava/lang/String;)V")
      && !isAssertableToAny(Set.of("java/applet/Applet", "net/minecraft/launchwrapper/ITweaker", "java/net/URLStreamHandler",
      "android/app/Activity", "android/app/Service", "android/content/BroadcastReceiver", "android/content/ContentProvider", "android/appwidget/AppWidgetProvider"))
      && !this.name.startsWith("net/branchlock/annotations/")
      && methods.stream().noneMatch(m -> m.hasAccess(ACC_NATIVE));
  }

  public boolean isAbstract() {
    return Access.isAbstract(this.access);
  }

  public boolean hasStaticInitSideEffect() {
    // TODO cache this method.

    if ("java/lang/Object".equals(getName())) return false; // java/lang/Object has no static initialization.
    // 02-12-2023: removed return true for interfaces, because it is not correct.

    BMethod staticInit = methods.get("<clinit>", "()V");
    if (staticInit != null) {
      if (StreamSupport.stream(staticInit.instructions.spliterator(), false).anyMatch(Instructions::hasSideEffect)) return true;
    }

    return getDirectParentClasses().stream().anyMatch(parent -> parent.isLocal() && parent.hasStaticInitSideEffect());
  }


  public BMethod getOrMakeStaticInitializer() {
    BMethod staticInitializer = methods.get("<clinit>", "()V");
    if (staticInitializer == null) {
      staticInitializer = new BMethod(this, Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
      staticInitializer.instructions.add(new InsnNode(Opcodes.RETURN));
      addMethod(staticInitializer);
    }
    return staticInitializer;
  }

  public void updateMethodEquivalenceClasses() {
    methods.forEach(BMethod::updateEquivalenceClass);
  }

  public void resetMethodEquivalenceClasses() {
    methods.forEach(BMethod::resetEquivalenceClass);
  }

  public boolean isAccessibleFrom(BClass bc) {
    if (bc == null) return false;
    if (bc == this) return true;
    if (hasAccess(ACC_PUBLIC)) return true;
    if (hasAccess(ACC_PRIVATE)) return outerClass != null && outerClass.equals(bc.getName());
    if (hasAccess(ACC_PROTECTED)) return bc.isAssertableTo(getName()) || bc.getPackage().equals(getPackage());
    return false;
  }

  public void setJarPath(String jarPath) {
    this.jarPath = jarPath;
  }

  public String getJarPath() {
    return jarPath;
  }

  // ##############################  ASM API methods  ##############################
  // #########################  Update on new ASM version  #########################

  /**
   * Resets this ClassNode to its initial state.
   */
  public void reset() {
    // make sure directSubClasses are updated
    this.directSubClasses.clear();
    // make sure the methods do not remain in the equivalence classes.
    methods.forEach(BMethod::resetEquivalenceClass);
    this.methods.clear();
    this.fields.clear();
    this.invalidated = false;
    // do not reset originalName, as is it intended to be used after reset.

    this.version = -1;
    this.access = -1;
    this.name = null;
    this.signature = null;
    this.superName = null;
    this.interfaces = new ArrayList<>();
    this.sourceFile = null;
    this.sourceDebug = null;
    this.module = null;
    this.outerClass = null;
    this.outerMethod = null;
    this.outerMethodDesc = null;
    this.visibleAnnotations = null;
    this.invisibleAnnotations = null;
    this.visibleTypeAnnotations = null;
    this.invisibleTypeAnnotations = null;
    this.attrs = null;
    this.innerClasses = new ArrayList<>();
    this.nestHostClass = null;
    this.nestMembers = null;
    this.permittedSubclasses = null;
    this.recordComponents = null;

    // TODO add new fields of ClassNode here on ASM update.
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    if (invalidated)
      throw new IllegalStateException("Class has been invalidated and can no longer be used.");
    if (this.name != null)
      throw new IllegalStateException("Class has already been visited (or reset has not been called).");
    super.visit(version, access, name, signature, superName, interfaces);
    if (originalName == null)
      this.originalName = name;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
    BMethod method = new BMethod(this, access, name, descriptor, signature, exceptions);
    // silently add method without updating equivalence class.
    this.methods.add(method);
    return method;
  }

  @Override
  public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
    BField field = new BField(this, access, name, descriptor, signature, value);
    // silently add field.
    this.fields.add(field);
    return field;
  }

  @Override
  public void accept(ClassVisitor classVisitor) {
    if (invalidated) {
      throw new IllegalStateException(name + " class has been invalidated and can no longer be used.");
    }
    // initialize both unused lists to make sure accept method works correctly.
    super.methods = Collections.unmodifiableList(methods.getCollection());
    super.fields = Collections.unmodifiableList(fields.getCollection());
    super.accept(classVisitor);
  }

  public void invalidate() {
    this.invalidated = true;
  }

  /**
   * Updates the into class without the need of creating a new BClass instance.
   * Requires {@link #reset()} to be called on all classes before.
   */
  public void updateClass(BClass newClass) {
    if (!isLocal() || !newClass.isLocal())
      throw new IllegalArgumentException("Both classes must be local.");

    newClass.accept(this);
  }
}
