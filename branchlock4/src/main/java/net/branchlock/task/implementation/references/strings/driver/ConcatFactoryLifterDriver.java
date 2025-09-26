package net.branchlock.task.implementation.references.strings.driver;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.MajorVersion;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.ClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.references.strings.Strings;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This replaces invokedynamic calls to StringConcatFactory.makeConcatWithConstants with a simple string concatenation.
 * <p>
 * We replace the invokedynamic by a new static method inside the class that is being invoked.
 * <p>
 * TODO requires testing.
 */
public class ConcatFactoryLifterDriver implements ClassDriver, Opcodes {

  private static final Type STRING_TYPE = Type.getType(String.class);
  private final Strings task;
  private final AtomicInteger lifted = new AtomicInteger(0);
  private final Map<ConcatKey, BMethod> concatMap = new ConcurrentHashMap<>();

  public ConcatFactoryLifterDriver(Strings task) {
    this.task = task;
  }

  public static boolean isConcatFactory(InvokeDynamicInsnNode insnNode) {
    return insnNode.bsm != null &&
      insnNode.bsm.getOwner().equals("java/lang/invoke/StringConcatFactory") &&
      insnNode.bsm.getName().equals("makeConcatWithConstants") &&
      insnNode.bsm.getDesc().equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;");
  }

  @Override
  public boolean drive(Stream<BClass> stream) {
    stream.forEach(bc -> new ArrayList<>(bc.methods).forEach(bm -> {
      Stream<InvokeDynamicInsnNode> dynamicInsnNodeStream = bm.getCode().streamOpcodes(INVOKEDYNAMIC);
      List<InvokeDynamicInsnNode> concats = dynamicInsnNodeStream.filter(ConcatFactoryLifterDriver::isConcatFactory).collect(Collectors.toList());
      for (InvokeDynamicInsnNode idyn : concats) {
        replaceConcatFactory(idyn, bc, bm);
      }
    }));

    return true;
  }

  private void replaceConcatFactory(InvokeDynamicInsnNode idyn, BClass bc, BMethod bm) {
    if (idyn.bsmArgs == null) return;
    if (idyn.bsmArgs.length < 1 || !(idyn.bsmArgs[0] instanceof String)) return;
    if (!"makeConcatWithConstants".equals(idyn.name)) return;
    String toReplace = (String) idyn.bsmArgs[0];

    ConcatKey key = ConcatKey.of(bc, toReplace, idyn.desc);

    BMethod concatSimulator;
    if (concatMap.containsKey(key)) {
      // avoid duplicate methods doing the same thing
      concatSimulator = concatMap.get(key);
    } else {
      concatSimulator = createConcatSimulator(bc, toReplace, idyn);
      bc.addMethod(concatSimulator);

      concatMap.put(key, concatSimulator);
    }

      bm.instructions.set(idyn, concatSimulator.makeInvoker());
    lifted.incrementAndGet();
  }

  private BMethod createConcatSimulator(BClass bc, String toReplace, InvokeDynamicInsnNode idyn) {
    String desc = idyn.desc;
    Type[] argumentTypes = Type.getArgumentTypes(desc);

    BMethod mn = new BMethod(bc, ACC_PRIVATE | ACC_STATIC, task.nameFactory.getUniqueMethodName(bc, desc), desc, null, null);
    InsnList il = mn.instructions;

    il.add(new TypeInsnNode(NEW, "java/lang/StringBuilder"));
    il.add(new InsnNode(DUP));
    il.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false));

    int varTableIndex = 0;
    int argumentTypeIndex = 0;
    int bootstrapArgIndex = 1;

    StringBuilder sb = new StringBuilder();

    for (char c : toReplace.toCharArray()) {
      switch (c) {
        case '\u0001' -> {
          if (sb.length() > 0) {
            il.add(new LdcInsnNode(sb.toString()));
            il.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
            sb = new StringBuilder();
          }

          // placeholder for argument
          Type t = argumentTypes[argumentTypeIndex++];
          Type appendDescType = getGeneralType(t);
          il.add(new VarInsnNode(t.getOpcode(ILOAD), varTableIndex));
          String descriptor = Type.getMethodDescriptor(Type.getObjectType("java/lang/StringBuilder"), appendDescType);
          il.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", descriptor, false));
          varTableIndex += t.getSize();
        }
        case '\u0002' -> {
          if (bootstrapArgIndex >= idyn.bsmArgs.length) {
            throw new IllegalStateException("Not enough bootstrap arguments for invokedynamic: " + idyn);
          }
          Object bootstrapArg = idyn.bsmArgs[bootstrapArgIndex++];
          sb.append(bootstrapArg.toString());
        }
        default -> sb.append(c);
      }
    }

    if (sb.length() > 0) {
      il.add(new LdcInsnNode(sb.toString()));
      il.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
    }

    il.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
    il.add(new InsnNode(ARETURN));

    return mn;
  }

  private Type getGeneralType(Type t) {
    if (STRING_TYPE.equals(t))
      return STRING_TYPE; // strings can be appended directly
    if (t.getSort() == Type.OBJECT)
      return Type.getType(Object.class); // objects via Object descriptor
    if (t.getSort() == Type.METHOD || t.getSort() == Type.VOID)
      throw new UnsupportedOperationException();

    // primitives and primitive arrays
    return t;
  }

  @Override
  public void postDrive() {
    if (lifted.get() > 0) {
      Branchlock.LOGGER.info("Prepared {} dynamic concat factory methods for encryption.", lifted.get());
    }
  }

  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    return task.defaultClassExclusionHandlersPlus(t -> t.filter(c -> {
      return c.version >= MajorVersion.JAVA_9.getCode();
    }));
  }

  @Override
  public String identifier() {
    return "concat-factory-lifter";
  }

  static class ConcatKey {
    public final BClass owner;
    public final String stringPlaceholder;
    public final String desc;

    private ConcatKey(BClass owner, String stringPlaceholder, String desc) {
      this.owner = owner;
      this.stringPlaceholder = stringPlaceholder;
      this.desc = desc;
    }

    public static ConcatKey of(BClass owner, String stringPlaceholder, String desc) {
      return new ConcatKey(owner, stringPlaceholder, desc);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ConcatKey concatKey = (ConcatKey) o;
      return Objects.equals(owner, concatKey.owner) && Objects.equals(stringPlaceholder, concatKey.stringPlaceholder) && Objects.equals(desc, concatKey.desc);
    }

    @Override
    public int hashCode() {
      return Objects.hash(owner, stringPlaceholder, desc);
    }
  }
}
