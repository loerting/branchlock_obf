package net.branchlock.structure;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.*;
import net.branchlock.commons.java.Pair;
import net.branchlock.structure.equivalenceclass.EquivalentMethodDefinitionFinder;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.structure.equivalenceclass.UnaryEquivalenceClass;
import net.branchlock.structure.equivalenceclass.MultiEquivalenceClass;
import net.branchlock.task.implementation.salting.MethodSalt;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A decorator for ASM's {@link org.objectweb.asm.tree.MethodNode}.
 */
public class BMethod extends MethodNode implements BMember, Opcodes {
  /**
   * Locks used in the {@link #updateEquivalenceClass()} method.
   */
  private static final Map<String, Object> METHOD_SIGNATURE_EQC_LOCKS = new ConcurrentHashMap<>();
  private static final String MAIN_SIGNATURE = "main([Ljava/lang/String;)V";
  private static final String STATIC_INITIALIZER_SIGNATURE = "<clinit>()V";
  private static final String CREATE_UI_SIGNATURE = "createUI(Ljavax/swing/JComponent;)Ljavax/swing/plaf/ComponentUI;";
  private static final String JAVA_APPLET_INIT_SIGNATURE = "init()V";
  private static final String SERIALIZABLE_READ_OBJECT_SIGNATURE = "readObject(Ljava/io/ObjectInputStream;)V";
  private static final String SERIALIZABLE_WRITE_OBJECT_SIGNATURE = "writeObject(Ljava/io/ObjectOutputStream;)V";

  protected BClass owner;
  /**
   * The equivalence class of this method. Using this field you can get all methods that are equivalent to this method.
   */
  private IEquivalenceClass<BMethod> equivalenceClass;
  /*
    Unique salt value for this method. Can be used to harden the obfuscation. Can be null.
   */
  private MethodSalt salt;

  public BMethod(BClass owner, int access, String name, String descriptor, String signature, String[] exceptions) {
    super(Opcodes.ASM9, access, name, descriptor, signature, exceptions);
    this.owner = Objects.requireNonNull(owner);
  }

    public BMethod duplicateMethod() {
      BMethod copy = new BMethod(getOwner(), this.access, this.name, this.desc, this.signature,
        this.exceptions == null ? null : this.exceptions.toArray(new String[0]));
      accept(copy);
      return copy;
    }

    public boolean isStaticInitializer() {
    return STATIC_INITIALIZER_SIGNATURE.equals(getIdentifier());
  }

  public boolean isConstructor() {
    return "<init>".equals(this.name);
  }

  public boolean hasAnnotation(String annotation) {
    return Annotations.has(annotation, this.visibleAnnotations, this.invisibleAnnotations);
  }

  public MethodInsnNode makeInvoker() {
    int op;
    if (Access.isStatic(this.access)) {
      op = INVOKESTATIC;
    } else if (Access.isInterface(this.access) || Access.isAbstract(this.access)) {
      op = INVOKEINTERFACE;
    } else if (this.name.startsWith("<")) {
      op = INVOKESPECIAL;
    } else {
      op = INVOKEVIRTUAL;
    }
    return new MethodInsnNode(op, owner.name, this.name, this.desc);
  }

  public Type[] getArgs() {
    return Type.getArgumentTypes(this.desc);
  }

  public Type returnType() {
    return Type.getReturnType(this.desc);
  }

  public String getIdentifier() {
    return this.name + this.desc;
  }

  @Override
  public String getOriginalName() {
    // TODO implement original name handling
    return this.name;
  }

  @Override
  public String toString() {
    return owner.getOriginalName() + "#" + this.name + this.desc;
  }

  public boolean hasAccess(int access) {
    return (this.access & access) == access;
  }

  /**
   * @return true if this is a method that is used at runtime only.
   */
  public boolean isRuntimeEnumMethod() {
    return Access.isEnum(owner.access) && (this.name.equals("values") || this.name.equals("valueOf"));
  }

  public boolean hasAnyAccess(int access) {
    return (this.access & access) != 0;
  }

  public boolean matchesAccess(int hasAccess, int hasNotAccess) {
    return hasAccess(hasAccess) && !hasAnyAccess(hasNotAccess);
  }

  public void removeAccess(int access) {
    this.access = Access.removeAccess(this.access, access);
  }

  public int getInstructionCount() {
    return this.instructions.size();
  }

  public boolean isMain() {
    return MAIN_SIGNATURE.equals(getIdentifier()) && hasAccess(ACC_PUBLIC | ACC_STATIC);
  }

  public String getName() {
    return this.name;
  }

  public void setName(String newName) {
    if (!isLocal())
      throw new IllegalStateException("Cannot change signature of non-local method");
    String oldIdentifier = getIdentifier();
    this.name = newName;
    this.owner.methods.observeIdentifierChanged(this, oldIdentifier);
  }

  @Override
  public String getDescriptor() {
    return this.desc;
  }

  public void setDescriptor(String newDescriptor) {
    if (!isLocal())
      throw new IllegalStateException("Cannot change signature of non-local method");
    String oldIdentifier = getIdentifier();
    this.desc = newDescriptor;
    this.owner.methods.observeIdentifierChanged(this, oldIdentifier);
  }

  public IEquivalenceClass<BMethod> requireEquivalenceClass() {
    if(!validEquivalenceClass()) {
      updateEquivalenceClass();
    }
    return equivalenceClass;
  }

  public boolean validEquivalenceClass() {
    return equivalenceClass != null && equivalenceClass.isValidInstance();
  }

  /**
   * Update the equivalence class of this method.
   * All methods that are equivalent (overridden or overriding) to this method will be updated as well.
   * <p>
   * This method is thread-safe.
   */
  public void updateEquivalenceClass() {
    // lock on method signature to prevent concurrent modification of the equivalence class.
    String identifier = this.getIdentifier();
    METHOD_SIGNATURE_EQC_LOCKS.putIfAbsent(identifier, new Object());
    synchronized (METHOD_SIGNATURE_EQC_LOCKS.get(identifier)) {

      if (Access.isStatic(this.access) || isConstructor() || isStaticInitializer() || isRuntimeEnumMethod()) {
        if (equivalenceClass != null)
          equivalenceClass.invalidate();
        equivalenceClass = new UnaryEquivalenceClass(this);
        return;
      }

      // first, reset the equivalence class of this method.
      resetEquivalenceClass();

      // get all methods that override or are overridden by this method.
      EquivalentMethodDefinitionFinder methodDefinitionFinder = new EquivalentMethodDefinitionFinder(this);
      methodDefinitionFinder.findEquivalentDefinitions();
      Set<BMethod> bMethods = methodDefinitionFinder.getResults();
      if (!bMethods.contains(this)) throw new IllegalStateException("Method not found in class hierarchy (wrong owner?)");

      // get all their existing equivalence classes and merge them.
      List<MultiEquivalenceClass> existingEquivalenceClasses = bMethods.stream()
        .filter(BMethod::validEquivalenceClass)
        .map(BMethod::requireEquivalenceClass)
        .filter(equivalenceClass -> equivalenceClass instanceof MultiEquivalenceClass)
        .map(equivalenceClass -> (MultiEquivalenceClass) equivalenceClass)
        .distinct()
        .collect(Collectors.toList());
      // using an existing equivalence class if possible to prevent unnecessary creation of new equivalence classes.
      MultiEquivalenceClass newGeneralEquivalenceClass = existingEquivalenceClasses.isEmpty() ? new MultiEquivalenceClass() : existingEquivalenceClasses.remove(0);
      newGeneralEquivalenceClass.addMember(this);

      for (MultiEquivalenceClass existingEquivalenceClass : existingEquivalenceClasses) {
        // replace all instances of the merged equivalence classes with the new one.
        existingEquivalenceClass.getMembers().forEach(bm2 -> bm2.equivalenceClass = newGeneralEquivalenceClass);
        newGeneralEquivalenceClass.merge(existingEquivalenceClass);
      }

      // update the equivalence class of this method and of all methods that are equivalent to this method.
      equivalenceClass = newGeneralEquivalenceClass;
      bMethods.forEach(bm2 -> {
          newGeneralEquivalenceClass.addMember(bm2);
          bm2.equivalenceClass = newGeneralEquivalenceClass;
      });
    }
  }

  public void resetEquivalenceClass() {
    String identifier = this.getIdentifier();
    METHOD_SIGNATURE_EQC_LOCKS.putIfAbsent(identifier, new Object());
    synchronized (METHOD_SIGNATURE_EQC_LOCKS.get(identifier)) {
      if (equivalenceClass != null) {
          if (equivalenceClass instanceof UnaryEquivalenceClass) {
            equivalenceClass.invalidate();
          } else {
            equivalenceClass.removeMember(this);
          }

          // set the equivalence class to null.
        equivalenceClass = null;
      }
    }
  }

  /**
   * Perform a stack map frame analysis on this method, and return the result.
   * Each instruction will have a frame associated with it (that can be null).
   * If the analysis fails, null will be returned.
   */
  public <V extends Value> List<Pair<AbstractInsnNode, Frame<V>>> getInstructionsWithFrames(Interpreter<V> interpreter) {
    Analyzer<V> analyzer = new Analyzer<>(interpreter);
    try {
      analyzer.analyzeAndComputeMaxs(owner.getName(), this);
    } catch (AnalyzerException e) {
      Branchlock.LOGGER.error("Failed frame analysis in {}, {}", e, owner.getOriginalName(), this.name + this.desc);
      return null;
    }

    List<Pair<AbstractInsnNode, Frame<V>>> result = new ArrayList<>();
    Frame<V>[] frames = analyzer.getFrames();
    for (int i = 0; i < this.instructions.size(); i++) {
      result.add(Pair.of(this.instructions.get(i), frames[i]));
    }
    return result;
  }

  /**
   * Get instructions of type {@code nodeType} with their frames.
   */
  public <T extends AbstractInsnNode, V extends Value> List<Pair<T, Frame<V>>> getNodesWithFrames(Interpreter<V> interpreter, Class<T> nodeType) {
    List<Pair<AbstractInsnNode, Frame<V>>> instructionsWithFrames = getInstructionsWithFrames(interpreter);
    if (instructionsWithFrames == null) return null;
    List<Pair<T, Frame<V>>> result = new ArrayList<>();
    for (Pair<AbstractInsnNode, Frame<V>> pair : instructionsWithFrames) {
      if (nodeType.isInstance(pair.a)) {
        result.add(Pair.of(nodeType.cast(pair.a), pair.b));
      }
    }
    return result;
  }

  public MethodSalt getSalt() {
    return salt;
  }

  public void setSalt(MethodSalt salt) {
    if (this.salt != null) {
      throw new IllegalStateException("Salt already set");
    }
    this.salt = salt;
  }

  public BMethodCode getCode() {
    return new BMethodCode(this);
  }

  /**
   * Stream all instructions of this method. The instructions can be removed or added during the stream,
   * as it streams a copy of the instruction list.
   */
  public Stream<AbstractInsnNode> streamInstr() {
    return Arrays.stream(this.instructions.toArray());
  }

  public Reference toReference() {
    return Reference.of(owner.getName(), this.name, this.desc, Reference.RefType.METHOD_INVOKE);
  }

  /**
   * Injects the code of the given method into this method.
   * The code will be injected before the actual method body.
   * <p>
   * The method will be removed from the owner class, if it is actually inside the class.
   *
   * @param proxyMethod the method to inject, which will be unusable after this call.
   */
  public void injectMethod(BMethod proxyMethod) {
    if (proxyMethod.returnType() != Type.VOID_TYPE) {
      throw new IllegalArgumentException("Injected method must have a void return type");
    }
    if (proxyMethod.getArgs().length != 0) {
      throw new IllegalArgumentException("Injected method must have no arguments");
    }

    BMethodCode code = proxyMethod.getCode();
    code.cutReturn();
    code.removeIf(insn -> insn.getType() == AbstractInsnNode.LINE);

    // add the instructions to the method
    this.instructions.insert(code.getInstructions());

    if (this.tryCatchBlocks == null) this.tryCatchBlocks = new ArrayList<>();
    this.tryCatchBlocks.addAll(proxyMethod.tryCatchBlocks);

    this.localVariables = null;
    this.maxLocals = Math.max(this.maxLocals, proxyMethod.maxLocals);
    this.maxStack = Math.max(this.maxStack, proxyMethod.maxStack);

    // remove the method from the class if it is actually inside the class.
    proxyMethod.owner.methods.remove(proxyMethod);

    proxyMethod.resetEquivalenceClass();
  }

  public LocalVariableNode getLocalVariable(String localVarName) {
    if (this.localVariables == null) throw new IllegalStateException("Local variables not present");
    for (LocalVariableNode localVariableNode : this.localVariables) {
      if (localVariableNode.name.equals(localVarName)) return localVariableNode;
    }
    throw new IllegalArgumentException("Local variable not found: " + localVarName);
  }

  public boolean isLocal() {
    return owner.isLocal();
  }

  @Override
  public boolean hasOwner() {
    return true;
  }

  public boolean isSignatureChangeable() {
    if (!isLocal()) return false;
    if (Access.isNative(this.access)) return false;
    if (isRuntimeEnumMethod()) return false;
    if (isStaticInitializer()) return false;
    if (hasAnnotation("KeepDesc") || hasAnnotation("RetainSignature")) return false;
    if (hasAnnotation("com/google/common/eventbus/Subscribe") || hasAnnotation("org/bukkit/event/EventHandler")) return false;
    String signature = getIdentifier();
    if (isPotentialEntryPoint()) return false;

    // these methods are used in annotation declarations
    if (hasAccess(ACC_ABSTRACT) && owner.isAnnotation()) return false;

    // look and feel constructors are always called using reflection
    if (isConstructor() && getOwner().isAssertableTo("javax/swing/LookAndFeel")) return false;

    return !("valueOf(Ljava/lang/String;)L" + owner.getName() + ";").equals(signature);
  }

  public boolean isJavaAppletInit() {
    return JAVA_APPLET_INIT_SIGNATURE.equals(getIdentifier()) && owner.isAssertableTo("java/applet/Applet");
  }

  public boolean isSerializationMethod() {
    String id = getIdentifier();
    return SERIALIZABLE_READ_OBJECT_SIGNATURE.equals(id) || SERIALIZABLE_WRITE_OBJECT_SIGNATURE.equals(id);
  }

  public boolean isNameChangeable() {
    return !this.name.startsWith("<") && isSignatureChangeable()
      && !hasAnnotation("KeepName") && !hasAnnotation("RetainSignature")
      && !this.name.startsWith("net/branchlock/annotations/");
  }

  @Override
  public BClass getOwner() {
    return owner;
  }


  public boolean hasMultiEquivalenceClass() {
    return equivalenceClass instanceof MultiEquivalenceClass;
  }

  /**
   * Changes the signature of this method. Does not update any references to this method.
   *
   * @param name
   * @param newDescriptor
   */
  public void changeSignature(String name, String newDescriptor) {
    if (!isLocal())
      throw new IllegalStateException("Cannot change signature of non-local method");
    if (name.equals(this.name) && newDescriptor.equals(this.desc))
      throw new IllegalArgumentException("New signature is the same as the old one");
    if (!isSignatureChangeable()) {
      throw new IllegalStateException("Signature of method " + this + " is not changeable");
    }
    if (owner.methods.containsSignature(name, newDescriptor)) {
      throw new IllegalStateException("Owner of " + this + " already contains a method with the signature " + name + newDescriptor);
    }

    String oldIdentifier = getIdentifier();
    this.name = name;
    this.desc = newDescriptor;
    this.owner.methods.observeIdentifierChanged(this, oldIdentifier);
    updateEquivalenceClass();
  }

  public boolean isStatic() {
    return Access.isStatic(this.access);
  }

  public boolean isPotentialEntryPoint() {
    if (isStaticInitializer()) return true;
    if (isMain()) return true;
    if (CREATE_UI_SIGNATURE.equals(getIdentifier())) return true;
    if (isSerializationMethod()) return true;
    if (hasAnnotation("EntryPoint")) return true;
    return isJavaAppletInit();
  }

  /**
   * Replaces all Object / Array types with java/lang/Object
   */
  public Type getGeneralSignature() {
    Type[] argumentTypes = Type.getArgumentTypes(this.desc);
    Type[] newArgumentTypes = new Type[argumentTypes.length];
    for (int i = 0; i < argumentTypes.length; i++) {
      newArgumentTypes[i] = Instructions.isObject(argumentTypes[i]) ? Type.getType(Object.class) : argumentTypes[i];
    }
    Type returnType = Type.getReturnType(this.desc);
    Type newReturnType = Instructions.isObject(returnType) ? Type.getType(Object.class) : returnType;
    return Type.getMethodType(newReturnType, newArgumentTypes);
  }

  @Override
  public int getAccess() {
    return this.access;
  }

  @Override
  public void setAccess(int access) {
    if(!isLocal()) throw new IllegalStateException("Cannot change access of non-local method");
    this.access = access;
  }

  public void moveTo(BClass newOwner) {
    if (newOwner.methods.containsSignature(this.name, this.desc)) {
      throw new IllegalStateException("New owner already contains a method with the signature " + this.name + this.desc);
    }
    this.owner.methods.remove(this);
    this.owner = newOwner;
    this.owner.addMethod(this);
    updateEquivalenceClass();
  }

  public boolean isAccessibleFrom(BClass bc) {
    if(!getOwner().isAccessibleFrom(bc)) return false;
    if(hasAccess(ACC_PUBLIC)) return true;
    if(hasAccess(ACC_PRIVATE)) return bc == getOwner();
    boolean samePackage = bc.getPackage().equals(getOwner().getPackage());
    if(hasAccess(ACC_PROTECTED)) return samePackage || bc.isAssertableTo(getOwner().getName());
    return samePackage;
  }
}
