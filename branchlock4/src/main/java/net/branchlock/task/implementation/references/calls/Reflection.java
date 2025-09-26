package net.branchlock.task.implementation.references.calls;

import net.branchlock.commons.asm.Reference;
import net.branchlock.layout.references.ReflectionLookup;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Reflection {
  public static final String GATEWAY_CLASS_NAME = ReflectionLookup.class.getName().replace('.', '/');
  public static final List<String> EXCLUDED_CLASSES =
    Arrays.asList("java/lang/invoke/MethodHandle", "java/lang/invoke/MethodType", "java/lang/ClassLoader",
      "jdk/internal/reflect/Reflection", "sun/reflect/Reflection");
  public static final String REFERENCE_FIELD_NAME = "$BL_DYNAMIC_REFERENCES";
  public static final String REFERENCE_FIELD_DESC = "[Ljava/lang/Object;";

  public static int hashReference(Reference ref) {
    // don't add owners, they don't matter, they are just the starting point of reverse search in JVM invokes
    List<String> params = new ArrayList<>();
    params.add(ref.name);
    if (ref.type.isMethod()) {
      params.add(toClassName(Type.getReturnType(ref.desc).getDescriptor()));
      Arrays.stream(Type.getArgumentTypes(ref.desc))
        .map(t -> toClassName(t.getDescriptor()))
        .forEach(params::add);
    } else {
      params.add(toClassName(Type.getType(ref.desc).getDescriptor()));
    }
    return getHash(params.toArray(new String[0]));
  }

  public static long calculateEncryptionCode(boolean isStatic, String owner, int memberHash, long callerHash) {
    int classHash = toClassName(Type.getObjectType(owner).getDescriptor()).hashCode();
    long code = (((long) memberHash) << 32) | (((long) classHash) & 0xffffffffL);

    // TODO saltPool was used in callerHash in BL3. add when saltPool is implemented.
    callerHash = (callerHash << 32) | (callerHash & 0xffffffffL);

    code &= 0xfffffffffffffffeL;
    code |= isStatic ? 1L : 0L; // sacrifice the last bit for the static flag

    code ^= callerHash;
    return code;
  }

  private static int getHash(String[] params) {
    int result = 17;
    for (String c : params) {
      result = 31 * result + c.hashCode();
    }
    return result;
  }

  public static int getXORCodeBetweenGatewayCall(String name) {
    return Math.abs(name.hashCode() ^ 0xCAFEBABE) % 128;
  }

  public static String toClassName(String descriptor) {
    Type t = Type.getType(descriptor);
    if (t.getSort() == Type.ARRAY) {
      // why java, why?!
      return descriptor.replace('/', '.');
    }
    return t.getClassName();
  }
}
