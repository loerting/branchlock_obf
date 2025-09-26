package net.branchlock.task.implementation.coverage;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Access;
import net.branchlock.commons.asm.interpreter.TypeInterpreter;
import net.branchlock.commons.java.Pair;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.MethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.*;
import java.util.stream.Stream;

public class CoverInstantiationDriver implements MethodDriver, Opcodes {
  public final Map<String, BClass> coverClasses = new HashMap<>();
  private final RuntimeCoverage runtimeCoverage;
  private final Random R = Branchlock.R;

  public CoverInstantiationDriver(RuntimeCoverage runtimeCoverage) {
    this.runtimeCoverage = runtimeCoverage;
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    stream.forEach(bm -> {
      introduceCoverClasses(bm);
      List<Pair<AbstractInsnNode, Frame<BasicValue>>> insnWithFrames = bm.getInstructionsWithFrames(new TypeInterpreter(runtimeCoverage.dataProvider, bm));
      if (insnWithFrames == null)
        return;
      updateInstructions(insnWithFrames);
    });
    return true;
  }

  private void updateInstructions(List<Pair<AbstractInsnNode, Frame<BasicValue>>> insnWithFrames) {
    for (Pair<AbstractInsnNode, Frame<BasicValue>> pair : insnWithFrames) {
      AbstractInsnNode ain = pair.a;
      Frame<BasicValue> frame = pair.b;

      if (ain.getType() != AbstractInsnNode.METHOD_INSN || frame == null)
        continue;
      MethodInsnNode min = (MethodInsnNode) ain;
      if (!coverClasses.containsKey(min.owner))
        continue;
      BClass coverClass = coverClasses.get(min.owner);
      String methodName = min.name;
      boolean isObjectInit = min.getOpcode() == INVOKESPECIAL && "<init>".equals(methodName);
      if (min.getOpcode() == INVOKEVIRTUAL || isObjectInit) {

        Type[] argTypes = Type.getArgumentTypes(min.desc);
        BasicValue objectReference = frame.getStack(frame.getStackSize() - 1 - argTypes.length);
        if (objectReference.getType() != null && objectReference.getType().getInternalName().equals(coverClass.getName())) {
          // match, we have to update the <init> reference to the new class
          if (coverClass.methods.get(methodName, min.desc) == null) {
            // add super constructor
            BMethod init = new BMethod(coverClass, ACC_PUBLIC, methodName, min.desc, null, null);
            init.instructions.add(new VarInsnNode(ALOAD, 0));
            int var = 1;
            for (Type t : argTypes) {
              init.instructions.add(new VarInsnNode(t.getOpcode(ILOAD), var));
              var += t.getSize();
            }
            init.maxLocals = init.maxStack = var;

            init.instructions.add(new MethodInsnNode(INVOKESPECIAL, min.owner, methodName, min.desc));
            init.instructions.add(new InsnNode(Type.getReturnType(min.desc).getOpcode(IRETURN)));

            coverClass.addMethod(init);
          }
          min.owner = coverClass.getName();
        }
      }
    }
  }

  private void introduceCoverClasses(BMethod bm) {
    for (AbstractInsnNode ain : bm.instructions.toArray()) {
      if (ain.getOpcode() == NEW) {
        TypeInsnNode tin = (TypeInsnNode) ain;
        if (!runtimeCoverage.dataProvider.isRuntimeClass(tin.desc)) {
          continue;
        }
        BClass rtClass = runtimeCoverage.dataProvider.resolveRuntimeBClass(tin.desc);

        if (!Access.isFinal(rtClass.access)) {
          if (coverClasses.containsKey(tin.desc)) {
            tin.desc = coverClasses.get(tin.desc).getName();
          } else {
            tin.desc = makeCoverClassFor(rtClass);
          }
        }
      }
    }
  }

  private String makeCoverClassFor(BClass rtClass) {
    BClass proxy = new BClass(runtimeCoverage.dataProvider);
    proxy.access = ACC_PUBLIC;
    proxy.version = runtimeCoverage.settingsManager.getTargetVersion();
    proxy.name = getUnusedAdaptedClassName();
    proxy.superName = rtClass.getName();
    runtimeCoverage.dataProvider.addClass(proxy);

    coverClasses.put(rtClass.getName(), proxy);
    return proxy.getName();

  }

  protected final String getUnusedAdaptedClassName() {
    StringBuilder name = new StringBuilder(runtimeCoverage.getRandomLocalClassName());
    do {
      name.append("$");
      name.append(R.nextInt(10));
    } while (runtimeCoverage.dataProvider.getClasses().containsKey(name.toString()));
    return name.toString();
  }


  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return runtimeCoverage.defaultMemberExclusionHandlers();
  }

  @Override
  public String identifier() {
    return "instantiation-coverage";
  }
}
