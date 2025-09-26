package net.branchlock.task.data;

import net.branchlock.commons.asm.Instructions;
import net.branchlock.commons.java.MultiMap;
import net.branchlock.commons.java.Pair;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMember;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.provider.DataProvider;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import java.util.*;

public class ReflectionDetector implements Opcodes {
  /*
TODO: implement a setting to do more specific exclusion with REFLECTION_NAME_ACCESS and REFLECTION_PACKAGE_ACCESS.
 */
  private static final List<String> REFLECTION_NAME_ACCESS = Arrays.asList("getName", "getTypeName", "getCanonicalName", "getSimpleName");
  private static final List<String> REFLECTION_PACKAGE_ACCESS = Arrays.asList("getPackageName", "getPackage", "getResource");
  private static final List<String> REFLECTION_METHOD_ACCESS = Arrays.asList("getDeclaredMethod",
    "getMethod", "getDeclaredMethods", "getMethods", "getEnumConstants");
  private static final List<String> REFLECTION_FIELD_ACCESS = Arrays.asList("getDeclaredField", "getField", "getDeclaredFields", "getFields");
  private static final List<String> REFLECTION_CONSTRUCTOR_ACCESS = Arrays.asList("getDeclaredConstructor", "getConstructor",
    "getDeclaredConstructors", "getConstructors", "newInstance", "getEnumConstants");
  private final DataProvider dataProvider;
  private MultiMap<BClass, ReflectionUsage> affectedClasses = null;
  public ReflectionDetector(DataProvider dataProvider) {
    this.dataProvider = dataProvider;
  }

  public MultiMap<BClass, ReflectionUsage> findAffectedClasses() {
    if (affectedClasses != null) return affectedClasses;
    affectedClasses = new MultiMap<>();

    dataProvider.streamInputClasses().parallel().forEach(bClass -> {
      if (bClass.isAssertableTo("java/net/URLStreamHandler")) {
        affectedClasses.put(bClass, ReflectionUsage.NAME_USED);
        affectedClasses.put(bClass, ReflectionUsage.INSTANTATION);
      }
    });

    dataProvider.streamInputMethods().parallel().forEach(bMethod -> {
      List<Pair<AbstractInsnNode, Frame<SourceValue>>> instructionsWithFrames = bMethod.getInstructionsWithFrames(new SourceInterpreter());
      detectReflection(bMethod.getOwner(), instructionsWithFrames);
    });
    affectedClasses.keySet().removeIf(bClass -> bClass.hasAnnotation("ForceInclude") || bClass.hasAnnotation("ForceRename"));
    return affectedClasses;
  }

  private void detectReflection(BClass methodOwner, List<Pair<AbstractInsnNode, Frame<SourceValue>>> instructionsWithFrames) {
    for (Pair<AbstractInsnNode, Frame<SourceValue>> pair : instructionsWithFrames) {
      AbstractInsnNode ain = pair.a;
      Frame<SourceValue> frame = pair.b;
      if (ain.getType() == AbstractInsnNode.METHOD_INSN) {
        MethodInsnNode min = (MethodInsnNode) ain;
        if (min.owner.equals("java/lang/Class") || min.owner.equals("java/lang/reflect/Constructor")) {
          handlePossibleReflectionCall(methodOwner, min, frame);
        }
      } else if (ain.getOpcode() == LDC) {
        // ldc string that is a class -> no name change
        Object cst = ((LdcInsnNode) ain).cst;
        if (cst instanceof String) {
          String className = String.valueOf(cst).replace('.', '/');
          if (className.length() > 4) {
            BClass affectedClass = dataProvider.getClassOrLib(className);
            if (affectedClass != null)
              affectedClasses.put(affectedClass, ReflectionUsage.NAME_USED);
          }
        }
      }
    }
  }

  private void handlePossibleReflectionCall(BClass methodOwner, MethodInsnNode ain, Frame<SourceValue> frame) {
    boolean methodAccess = REFLECTION_METHOD_ACCESS.contains(ain.name);
    boolean fieldAccess = REFLECTION_FIELD_ACCESS.contains(ain.name);
    boolean nameAccess = REFLECTION_NAME_ACCESS.contains(ain.name) || REFLECTION_PACKAGE_ACCESS.contains(ain.name);
    boolean constructorAccess = REFLECTION_CONSTRUCTOR_ACCESS.contains(ain.name);

    if (methodAccess || fieldAccess || nameAccess || constructorAccess) {
      SourceValue source = frame.getStack(frame.getStackSize() - 1 - Type.getArgumentTypes(ain.desc).length);

      switch (ain.name) {
        case "getResource" -> {
          AbstractInsnNode possibleLDC = frame.getStack(frame.getStackSize() - 1).insns.iterator().next();
          if (possibleLDC.getOpcode() == LDC && ((LdcInsnNode) possibleLDC).cst.toString().startsWith("/"))
            return;
        }
        case "newInstance" -> {
          AbstractInsnNode next = Instructions.getRealNext(ain);
          if (next.getOpcode() == CHECKCAST) {
            TypeInsnNode cast = (TypeInsnNode) next;
            BClass bClass = dataProvider.getClassOrLib(cast.desc);
            if (bClass != null) {
              affectedClasses.put(bClass, ReflectionUsage.INSTANTATION);
              bClass.directSubClasses.forEach(subClass -> affectedClasses.put(subClass, ReflectionUsage.INSTANTATION));
            }
          }
        }
      }

      if (source.getSize() != 1) return;

      ReflectionUsage characteristic = null;
      if (nameAccess) {
        characteristic = ReflectionUsage.NAME_USED;
      } else if (methodAccess) {
        characteristic = ReflectionUsage.METHOD_USED;
      } else if (fieldAccess) {
        characteristic = ReflectionUsage.FIELD_USED;
      } else if (constructorAccess) {
        characteristic = ReflectionUsage.INSTANTATION;
      }

      // try to identify the class that is being accessed

      AbstractInsnNode sourceNode = source.insns.iterator().next();
      if (sourceNode.getOpcode() == LDC) {
        Object cst = ((LdcInsnNode) sourceNode).cst;
        if (cst instanceof Type) {
          BClass bClass = dataProvider.getClassOrLib(((Type) cst).getInternalName());
          if (bClass != null) {
            affectedClasses.put(bClass, characteristic);
          }
        }
      } else if (sourceNode.getType() == AbstractInsnNode.METHOD_INSN) {
        MethodInsnNode clazzGetter = (MethodInsnNode) sourceNode;
        if (clazzGetter.name.equals("getClass") && clazzGetter.owner.equals("java/lang/Object")) {
          AbstractInsnNode previous = Instructions.getRealPrevious(clazzGetter);
          if (previous != null && previous.getOpcode() == ALOAD && ((VarInsnNode) previous).var == 0) {
            // mark the owner of the method
            affectedClasses.put(methodOwner, characteristic);
          }
        }
      }
    }
  }

  public boolean isAffected(BClass bc, ReflectionUsage... characteristic) {
    Collection<ReflectionUsage> reflectionUsages = findAffectedClasses().get(bc);
    if (reflectionUsages == null) return false;
    for (ReflectionUsage c : characteristic) {
      if(reflectionUsages.contains(c)) return true;
    }
    return false;
  }

  public boolean isAffected(BMethod bm) {
    Collection<ReflectionUsage> reflectionUsages = findAffectedClasses().get(bm.getOwner());
    if (reflectionUsages == null) return false;
    if (bm.isConstructor()) {
      return reflectionUsages.contains(ReflectionUsage.INSTANTATION);
    }
    return reflectionUsages.contains(ReflectionUsage.METHOD_USED);
  }

  public boolean isAffected(BField bf) {
    Collection<ReflectionUsage> reflectionUsages = findAffectedClasses().get(bf.getOwner());
    if (reflectionUsages == null) return false;
    return reflectionUsages.contains(ReflectionUsage.FIELD_USED);
  }

  public <T extends BMember> boolean isClassMemberAffected(T bMember) {
    if (bMember instanceof BMethod) {
      return isAffected((BMethod) bMember);
    } else if (bMember instanceof BField) {
      return isAffected((BField) bMember);
    } else {
      throw new IllegalArgumentException("Unsupported member type: " + bMember.getClass());
    }
  }


  public enum ReflectionUsage {
    NAME_USED, INSTANTATION, METHOD_USED, FIELD_USED
  }
}
