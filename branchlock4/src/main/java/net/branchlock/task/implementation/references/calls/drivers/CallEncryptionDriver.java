package net.branchlock.task.implementation.references.calls.drivers;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.*;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMember;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.task.driver.implementations.SingleClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.references.calls.InvokerMethod;
import net.branchlock.task.implementation.references.calls.References;
import net.branchlock.task.implementation.references.calls.Reflection;
import net.branchlock.task.implementation.references.calls.container.EncryptedMember;
import net.branchlock.task.implementation.references.calls.container.EncryptedMemberContainer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class CallEncryptionDriver implements SingleClassDriver, Opcodes {
  public static final Type OBJECT_TYPE = Type.getType(Object.class);
  private static final Random R = Branchlock.R;
  public final List<InvokerMethod> invokerMethods = new ArrayList<>();
  private final References references;
  private final boolean excludeCallsToRuntime;
  private final boolean excludeFields;
  public int callerHashKey;

  public CallEncryptionDriver(References references, boolean excludeCallsToRuntime, boolean excludeFields) {
    this.references = references;
    this.excludeCallsToRuntime = excludeCallsToRuntime;
    this.excludeFields = excludeFields;
  }

  private static void insertGatewayReturnHandling(Reference reference, InsnList il) {

    switch (reference.type) {
      case METHOD_INVOKE: {
        Type returnType = Type.getReturnType(reference.desc);
        if (Instructions.isObject(returnType) && returnType != OBJECT_TYPE)
          il.add(new TypeInsnNode(CHECKCAST, returnType.getInternalName()));
        else if (returnType == Type.VOID_TYPE)
          il.add(new InsnNode(POP));
        else
          il.add(Boxing.unbox(returnType));
        break;
      }
      case FIELD_GET: {
        Type returnType = Type.getType(reference.desc);
        if (Instructions.isObject(returnType) && returnType != OBJECT_TYPE) {
          il.add(new TypeInsnNode(CHECKCAST, returnType.getInternalName()));
        }
        // we do not need to unbox primitives, as the function will return the accurate type
        break;
      }
    }
  }

  @Override
  public void preDrive() {
    Branchlock.LOGGER.info("Excluding final fields from being encrypted to ensure compatibility with Java 12+.");
    callerHashKey = R.nextInt();
  }

  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    return references.defaultClassExclusionHandlers();
  }

  @Override
  public boolean driveEach(BClass c) {
    // ensure CONSTANT_Class support.
    c.raiseMinAPI(MajorVersion.JAVA_5);

    EncryptedMemberContainer emc = new EncryptedMemberContainer();

    c.methods.forEach(currentMethod -> {
      for (AbstractInsnNode ain : currentMethod.instructions) {
        int opcode = ain.getOpcode();
        switch (ain.getType()) {
          case AbstractInsnNode.FIELD_INSN: {
            if (excludeFields) continue;
            FieldInsnNode fin = (FieldInsnNode) ain;
            if (fin.owner.startsWith("[")) continue;
            if (fin.owner.startsWith("com/sun/jna") && fin.name.equals("activeField")) continue;
            if (currentMethod.isConstructor() && opcode == PUTFIELD) continue;

            BClass bClass = references.dataProvider.resolveBClass(fin.owner, currentMethod);
            if (bClass == null) continue;
            if (bClass.hasAnnotation("CallerSensitive")) continue;
            if (excludeCallsToRuntime && !bClass.isLocal()) continue;

            BField fn = bClass.resolveField(fin.name, fin.desc);
            if (fn == null) continue;
            if (fn.hasAnnotation("CallerSensitive")) continue;
            if (fn.hasAccess(ACC_FINAL)) continue;

            // check if accessible from any random outside class
            if (!fn.isLocal() && isNotAccessibleFromAnywhere(fn)) continue;

            boolean staticReference = opcode == GETSTATIC || opcode == PUTSTATIC;
            encryptReference(emc, currentMethod, ain, Reference.of(fin), staticReference);
            break;
          }
          case AbstractInsnNode.METHOD_INSN: {
            MethodInsnNode min = (MethodInsnNode) ain;

            // method.invoke will never invoke like INVOKESPECIAL, it will always select the uppermost method in the instance, even when casted
            // we could analyze if the object reference is the same class as the owner, because then it would work
            // bm.owner.node().name and min.owner equals check is not enough, as the object reference must not be "this" all the time
            if (opcode == INVOKESPECIAL) continue;

            // we do not want uninitialized references
            if (min.name.equals("<init>")) continue;

            // java seems to have a bug with findVirtual for methods like Array[].clone() java.lang.invoke
            // .WrongMethodTypeException: MethodHandle(Array)Object should be of type
            // (Array[])Object, even with the class being an array.
            if (min.owner.startsWith("[") || min.name.startsWith("<")) continue;

            if (min.owner.startsWith("java/") && min.name.equals("valueOf")) continue;
            // JVM-BUG: multiple hashCode invocations cause the JVM to crash.
            if (min.owner.equals("java/lang/Object") && min.name.equals("hashCode")) continue;
            // we don't want signature polymorphic methods or getCallerClass calls.
            if (Reflection.EXCLUDED_CLASSES.contains(min.owner)) continue;

            BClass bClass = references.dataProvider.resolveBClass(min.owner, currentMethod);
            if (bClass == null) continue;
            if (bClass.hasAnnotation("CallerSensitive")) continue;
            if (excludeCallsToRuntime && !bClass.isLocal()) continue;

            BMethod bme = bClass.resolveMethod(min.name, min.desc);
            if (bme == null) continue;
            IEquivalenceClass<BMethod> equivalenceClass = bme.requireEquivalenceClass();
            // reflection lookup will resolve the deepest method, on which the access check will be performed.
            // it is therefore necessary to check for all eq methods if they are not local that they are accessible from any random outside class
            if (equivalenceClass.getMembers().stream().anyMatch(m ->
              m.hasAnnotation("CallerSensitive") || (!m.isLocal() && isNotAccessibleFromAnywhere(m)))) continue;

            boolean staticReference = opcode == INVOKESTATIC;
            encryptReference(emc, currentMethod, ain, Reference.of(min), staticReference);
          }
        }
      }
    });

    if (emc.isEmpty()) return true;

    c.methods.forEach(this::optimizeReferenceFieldLoad);

    addReferenceFieldWithInitialization(c, emc);

    return true;
  }

  private boolean isNotAccessibleFromAnywhere(BMember member) {
    if(member.hasOwner()) {
      if(isNotAccessibleFromAnywhere(member.getOwner())) return true;
    }

    return (member.getAccess() & ACC_PUBLIC) != ACC_PUBLIC;
  }

  private void optimizeReferenceFieldLoad(BMethod bm) {
    if (bm.getCode().countPredicate(this::isRefFieldGet) < 3) return;

    AbstractInsnNode arrayGet = null;
    for (AbstractInsnNode ain : bm.instructions.toArray()) {
      if (isRefFieldGet(ain)) {
        arrayGet = ain;
        bm.instructions.set(ain, new VarInsnNode(ALOAD, bm.maxLocals));
      }
    }
    InsnList il = new InsnList();
    il.add(arrayGet);
    il.add(new VarInsnNode(ASTORE, bm.maxLocals));
    bm.instructions.insert(il);
    bm.maxLocals++;
  }

  private boolean isRefFieldGet(AbstractInsnNode ain) {
    return ain.getOpcode() == GETSTATIC && ((FieldInsnNode) ain).name.equals(Reflection.REFERENCE_FIELD_NAME);
  }

  private void addReferenceFieldWithInitialization(BClass c, EncryptedMemberContainer emc) {
    BField refArray = new BField(c, (c.isInterface() ? ACC_PUBLIC : ACC_PRIVATE) | ACC_STATIC | ACC_FINAL,
      Reflection.REFERENCE_FIELD_NAME, Reflection.REFERENCE_FIELD_DESC, null, null);
    c.addField(refArray);

    InsnList il = new InsnList();
    il.add(Instructions.intPush(emc.size()));
    il.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));

    for (EncryptedMember encryptedMember : emc.members()) {
      InsnList arrayStore = new InsnList();

      arrayStore.add(new InsnNode(DUP));
      arrayStore.add(Instructions.intPush(emc.getIndex(encryptedMember)));
      arrayStore.add(Instructions.longPush(encryptedMember.hash));
      arrayStore.add(Boxing.box(Type.LONG_TYPE)); // the invoker itself will load the reference
      arrayStore.add(new InsnNode(AASTORE));

      il.add(arrayStore);
    }
    il.add(refArray.createPut());

    BMethod staticInitializer = c.getOrMakeStaticInitializer();
    staticInitializer.instructions.insert(il);

    references.nameTransformer.transformFieldsInsideClass(c, refArray);
  }

  private void encryptReference(EncryptedMemberContainer emc, BMethod bm, AbstractInsnNode ain, Reference reference, boolean staticReference) {
    int referenceHash = Reflection.hashReference(reference);
    int callerHash = bm.getName().hashCode() ^ callerHashKey;
    long encryptionCode = Reflection.calculateEncryptionCode(staticReference, reference.owner, referenceHash, callerHash);
    EncryptedMember member = new EncryptedMember(encryptionCode, reference, staticReference);
    // check for hash collision.
    if (!emc.addMember(member)) return;

    MethodInsnNode caller = generateGatewayReference(reference, staticReference);

    InsnList il = new InsnList();

    insertGatewayCallerArguments(bm, il, member, caller, staticReference, emc);
    il.add(caller);
    insertGatewayReturnHandling(reference, il);

    bm.instructions.insert(ain, il);
    bm.instructions.remove(ain);

  }

  private MethodInsnNode generateGatewayReference(Reference ref, boolean isStatic) {
    boolean isMethod = ref.type.isMethod();
    List<Type> invokerArguments = new ArrayList<>();
    if (!isStatic)
      invokerArguments.add(OBJECT_TYPE); // add reference as argument for non static methods

    boolean returner = ref.isReturner();
    Type[] argumentTypes = isMethod ? Type.getArgumentTypes(ref.desc) : (returner ? new Type[0] : new Type[]{Type.getType(ref.desc)});

    for (Type t : argumentTypes) {
      if (Instructions.isObject(t)) {
        invokerArguments.add(OBJECT_TYPE);
      } else {
        invokerArguments.add(t);
      }
    }
    if (isStatic)
      invokerArguments.add(OBJECT_TYPE); // add java/lang/Class reference as argument for static methods

    invokerArguments.add(Type.getType(Object[].class)); // array
    invokerArguments.add(Type.INT_TYPE); // array index

    Type returnType = switch (ref.type) {
        case METHOD_INVOKE -> OBJECT_TYPE;
        case FIELD_SET -> Type.VOID_TYPE;
        case FIELD_GET -> ref.desc.length() == 1 ? Type.getType(ref.desc) : OBJECT_TYPE; // length == 1 means primitive
        default -> throw new AssertionError();
    };

      Type methodType = Type.getMethodType(returnType, invokerArguments.toArray(new Type[0]));

    InvokerMethod invokerMethod = new InvokerMethod(methodType, isStatic, ref);

    int index = invokerMethods.indexOf(invokerMethod);
    if (index == -1) {
      invokerMethods.add(invokerMethod);
      index = invokerMethods.size() - 1;
    }

    return new MethodInsnNode(INVOKESTATIC, Reflection.GATEWAY_CLASS_NAME, "invoker$" + index, methodType.getDescriptor(), false);
  }

  private void insertGatewayCallerArguments(BMethod bm, InsnList il, EncryptedMember member, MethodInsnNode caller, boolean isStatic, EncryptedMemberContainer encryptedMemberContainer) {
    if (isStatic)
      il.add(new LdcInsnNode(Type.getObjectType(member.ref.owner)));

    il.add(new FieldInsnNode(GETSTATIC, bm.getOwner().getName(), Reflection.REFERENCE_FIELD_NAME, Reflection.REFERENCE_FIELD_DESC));
    int callerInt = encryptedMemberContainer.getIndex(member) ^ Reflection.getXORCodeBetweenGatewayCall(caller.name);

    if (bm.getSalt() != null) {
      il.add(bm.getSalt().loadEncryptedInt(callerInt));
    } else {
      il.add(Instructions.intPush(callerInt));
    }
  }

  @Override
  public String identifier() {
    return "call-replacer";
  }
}
