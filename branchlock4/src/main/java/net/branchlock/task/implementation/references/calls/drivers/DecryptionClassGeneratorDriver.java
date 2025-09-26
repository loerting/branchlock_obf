package net.branchlock.task.implementation.references.calls.drivers;

import net.branchlock.commons.asm.Boxing;
import net.branchlock.commons.asm.Conversion;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.commons.asm.Reference;
import net.branchlock.layout.references.ReflectionLookup;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.BMethodCode;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.driver.implementations.IndividualDriver;
import net.branchlock.task.implementation.flow.ControlFlow;
import net.branchlock.task.implementation.flow.passes.TryCatchTrapPass;
import net.branchlock.task.implementation.references.calls.InvokerMethod;
import net.branchlock.task.implementation.references.calls.References;
import net.branchlock.task.implementation.references.calls.Reflection;
import net.branchlock.task.implementation.references.numbers.Numbers;
import net.branchlock.task.implementation.references.strings.utils.StringsToChars;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DecryptionClassGeneratorDriver implements IndividualDriver, Opcodes {
  public static final Type OBJECT_TYPE = Type.getType(Object.class);
  private final References references;
  private final CallEncryptionDriver callEncryptionDriver;
  /**
   * Maps the number of arguments to the method caller method.
   */
  private final Map<Integer, BMethod> methodReflectionCallers = new HashMap<>();
  private BClass reflectionLookup;
  private BMethod findMethod;
  private BMethod findField;

  public DecryptionClassGeneratorDriver(References references, CallEncryptionDriver callEncryptionDriver) {
    this.references = references;
    this.callEncryptionDriver = callEncryptionDriver;
  }

  @Override
  public boolean drive(Stream<Void> stream) {
    DataProvider dataProvider = references.dataProvider;
    reflectionLookup = Conversion.loadProgramClass(dataProvider, ReflectionLookup.class);
    reflectionLookup.version = references.settingsManager.getTargetVersion();
    reflectionLookup.sourceFile = null;
    reflectionLookup.setOriginalName("net/branchlock/generated/ReferenceDecryptionClass");

    dataProvider.addClass(reflectionLookup);

    findMethod = reflectionLookup.methods.get("findMethod", "(JLjava/lang/Class;)Ljava/lang/reflect/Method;");
    findField = reflectionLookup.methods.get("findField", "(JLjava/lang/Class;)Ljava/lang/reflect/Field;");
    if (findMethod == null || findField == null)
      throw new RuntimeException("ReflectionLookup class is not loaded correctly.");

    reflectionLookup.removePlaceholderFields();

    TryCatchTrapPass tryCatchTrapPass = new TryCatchTrapPass(references, ControlFlow.MAX_COVERAGE);
    reflectionLookup.methods.forEach(bm -> {
      BMethodCode code = bm.getCode();
      code.removeIf(insn -> insn.getType() == AbstractInsnNode.LINE);
      code.removeDebugInformation();
      code.replacePlaceholder("RANDOM_XOR", callEncryptionDriver.callerHashKey);

      LdcInsnNode ldc = new LdcInsnNode(references.settingsManager.isAndroid() ? "accessFlags" : "modifiers");
      code.replacePlaceholder("MODIFIERS_FIELD_NAME", StringsToChars.toChars(ldc, bm.maxLocals));
      bm.maxLocals += 1;

      if (bm.getName().startsWith("getHash")) return;

      for (AbstractInsnNode ain : bm.instructions.toArray()) {
        if (Instructions.isIntegerPush(ain)) {
          int integer = Instructions.getIntValue(ain);
          bm.instructions.insertBefore(ain, Numbers.generateCalculation(integer, Numbers.NumbersStrength.WEAK));
          bm.instructions.remove(ain);
        }
      }


      tryCatchTrapPass.drive(Stream.of(bm));
    });

    placeInvokerAndCallerMethods();
    reflectionLookup.methods.shuffle();

    references.nameTransformer.transformMembers(List.of(reflectionLookup), List.of());

    // now, name won't change anymore.
    BMethod getCallerHash = reflectionLookup.methods.get("getCallerHash", "()J");
    if (getCallerHash == null) throw new RuntimeException("ReflectionLookup class is not loaded correctly.");
    getCallerHash.getCode().replacePlaceholder("LOOKUP_CLASS_HASH", reflectionLookup.getName().replace('/', '.').hashCode());

    reflectionLookup = references.nameTransformer.transformFieldsInsideClass(reflectionLookup, reflectionLookup.fields.toArray(new BField[0]));
    references.nameTransformer.transformMethodsOnly(reflectionLookup.methods.stream().filter(bm ->
      !bm.isConstructor() && bm.isSignatureChangeable()).collect(Collectors.toList()));
    return true;
  }

  private void placeInvokerAndCallerMethods() {
    List<InvokerMethod> invokerMethods = callEncryptionDriver.invokerMethods;
    for (int idx = 0; idx < invokerMethods.size(); idx++) {
      InvokerMethod im = invokerMethods.get(idx);
      Reference sampleRef = im.sampleReference;

      BMethod invoker = new BMethod(reflectionLookup, ACC_PUBLIC | ACC_STATIC, "invoker$" + idx, im.invokerDescriptor.getDescriptor(), null, null);
      InsnList il = invoker.instructions;

      int argCount = Arrays.stream(im.invokerDescriptor.getArgumentTypes()).mapToInt(Type::getSize).sum();
      il.add(new VarInsnNode(ALOAD, argCount - 2));
      il.add(Instructions.intPush(Reflection.getXORCodeBetweenGatewayCall("invoker$" + idx)));
      il.add(new VarInsnNode(ILOAD, argCount - 1)); // load array index
      il.add(new InsnNode(IXOR));
      // make sure when the index is load later, the index is unencrypted
      il.add(new InsnNode(DUP));
      il.add(new VarInsnNode(ISTORE, argCount - 1));

      il.add(new InsnNode(AALOAD));

      // exception to check if long is stored and then convert to method / field
      LabelNode longCheckStart = new LabelNode();
      LabelNode longCheckEnd = new LabelNode();
      LabelNode longCheckHandler = new LabelNode();
      il.add(longCheckStart);

      boolean isMethod = im.refType.isMethod();
      il.add(new TypeInsnNode(CHECKCAST, Type.getType(isMethod ? Method.class : Field.class).getInternalName()));


      if (!isMethod) {
        // Throw NoSuchFieldException if the field is null.
        // the null check for methods is located in the method reflection callers.
        il.add(new InsnNode(DUP));
        generateMemberNullCheck(il, false);
      }
      if (im.isStatic)
        il.add(new InsnNode(ACONST_NULL));
      else
        il.add(new VarInsnNode(ALOAD, 0));
      il.add(longCheckEnd);

      int var = im.isStatic ? 0 : 1;
      if (isMethod) {
        Type[] argumentTypes = Type.getArgumentTypes(sampleRef.desc);
        // box all arguments
        for (Type arg : argumentTypes) {
          il.add(new VarInsnNode(arg.getOpcode(ILOAD), var));
          if (!Instructions.isObject(arg)) {
            il.add(Boxing.box(arg));
          }
          var += arg.getSize();
        }
        // call the invoke method
        il.add(getMethodReflectionCaller(argumentTypes.length).makeInvoker());
        il.add(new InsnNode(ARETURN));
      } else {
        // field get / set
        boolean isSet = im.refType == Reference.RefType.FIELD_SET;
        Type fieldType = Type.getType(sampleRef.desc);

        if (isSet)
          il.add(new VarInsnNode(fieldType.getOpcode(ILOAD), var));

        il.add(generateFieldFunction(sampleRef.isReturner(), fieldType).createInstruction());
        il.add(new InsnNode(isSet ? RETURN : fieldType.getOpcode(IRETURN)));
      }

      // resolve the reference if we got a long as argument and store it into the array

      il.add(longCheckHandler);
      il.add(new InsnNode(POP)); // pop the exception

      il.add(new VarInsnNode(ALOAD, argCount - 2));
      il.add(new InsnNode(DUP));
      il.add(new VarInsnNode(ILOAD, argCount - 1)); // load array index
      il.add(new InsnNode(DUP_X1));
      il.add(new InsnNode(AALOAD));
      il.add(Boxing.unbox(Type.LONG_TYPE));
      if (im.isStatic) {
        il.add(new VarInsnNode(ALOAD, argCount - 3)); // load class
        il.add(new TypeInsnNode(CHECKCAST, "java/lang/Class"));
      } else {
        il.add(new VarInsnNode(ALOAD, 0)); // load object ref
        il.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;"));
      }
      il.add((isMethod ? findMethod : findField).makeInvoker());
      il.add(new InsnNode(DUP_X2));
      il.add(new InsnNode(AASTORE));
      il.add(new JumpInsnNode(GOTO, longCheckStart));
      invoker.tryCatchBlocks.add(new TryCatchBlockNode(longCheckStart, longCheckEnd, longCheckHandler, null));

      invoker.maxStack = 5;
      invoker.maxLocals = var + 2;

      reflectionLookup.addMethod(invoker);
    }
  }

  private void generateMemberNullCheck(InsnList il, boolean method) {
    LabelNode nullCheckAfter = new LabelNode();
    il.add(new JumpInsnNode(IFNONNULL, nullCheckAfter));
    String unresolvedException = Type.getType(method ? NoSuchMethodError.class : NoSuchFieldError.class).getInternalName();
    il.add(new TypeInsnNode(NEW, unresolvedException));
    il.add(new InsnNode(DUP));
    il.add(new MethodInsnNode(INVOKESPECIAL, unresolvedException, "<init>", "()V"));
    il.add(new InsnNode(ATHROW));
    il.add(nullCheckAfter);
  }

  private Reference generateFieldFunction(boolean get, Type fieldType) {
    String name = get ? "get" : "set";
    boolean object = Instructions.isObject(fieldType);
    if (!object) {
      String className = fieldType.getClassName();
      name += className.substring(0, 1).toUpperCase();
      name += className.substring(1);
    }
    Type genericType = object ? OBJECT_TYPE : fieldType;
    String desc;
    if (get)
      desc = Type.getMethodType(genericType, OBJECT_TYPE).getDescriptor();
    else
      desc = Type.getMethodType(Type.VOID_TYPE, OBJECT_TYPE, genericType).getDescriptor();
    return Reference.of("java/lang/reflect/Field", name, desc, Reference.RefType.METHOD_INVOKE);
  }

  private BMethod getMethodReflectionCaller(int argumentCount) {
    methodReflectionCallers.computeIfAbsent(argumentCount, this::generateMethodReflectionCaller);
    return methodReflectionCallers.get(argumentCount);
  }

  private BMethod generateMethodReflectionCaller(int argumentCount) {
    Type[] args = new Type[argumentCount + 2]; // plus object reference
    args[0] = Type.getType(Method.class);
    Arrays.fill(args, 1, args.length, OBJECT_TYPE);
    Type methodType = Type.getMethodType(OBJECT_TYPE, args);
    BMethod refCaller = new BMethod(reflectionLookup, ACC_PUBLIC | ACC_STATIC, "methodCaller$" + argumentCount, methodType.getDescriptor(), null, null);

    // InvocationTargetException range
    LabelNode start = new LabelNode();
    LabelNode end = new LabelNode();
    LabelNode handler = new LabelNode();
    LabelNode otherHandler = new LabelNode();

    InsnList il = refCaller.instructions;
    il.add(new VarInsnNode(ALOAD, 0)); // Method
    il.add(new VarInsnNode(ALOAD, 1)); // Object reference
    // store the stuff into an Object[]
    il.add(Instructions.intPush(argumentCount));
    il.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
    il.add(start);
    for (int i = 2; i < argumentCount + 2; i++) {
      il.add(new InsnNode(DUP));
      il.add(Instructions.intPush(i - 2));
      il.add(new VarInsnNode(ALOAD, i));
      il.add(new InsnNode(AASTORE));
    }
    il.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"));
    il.add(end);
    il.add(new InsnNode(ARETURN));
    il.add(handler);
    il.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Throwable", "getCause", "()Ljava/lang/Throwable;"));
    il.add(new InsnNode(ATHROW));
    il.add(otherHandler);
    il.add(new VarInsnNode(ALOAD, 0)); // load method
    generateMemberNullCheck(il, true); // check if method is null, if yes, throw NoSuchMethodError, if not, throw the existing throwable
    il.add(new InsnNode(ATHROW));
    refCaller.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, Type.getType(InvocationTargetException.class).getInternalName()));
    refCaller.tryCatchBlocks.add(new TryCatchBlockNode(start, end, otherHandler, null));

    refCaller.maxStack = 6;
    refCaller.maxLocals = args.length + 1;

    reflectionLookup.addMethod(refCaller);
    return refCaller;
  }

  @Override
  public String identifier() {
    return "decryption-class-generator";
  }
}
