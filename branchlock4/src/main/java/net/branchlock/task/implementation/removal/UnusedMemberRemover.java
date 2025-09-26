package net.branchlock.task.implementation.removal;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Access;
import net.branchlock.commons.asm.Conversion;
import net.branchlock.layout.removal.MissingMemberError;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMethod;
import net.branchlock.task.data.ReflectionDetector;
import net.branchlock.task.driver.implementations.SingleClassDriver;
import net.branchlock.task.driver.passthrough.ClassReflectionDetectionPassThrough;
import net.branchlock.task.driver.passthrough.IPassThrough;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class UnusedMemberRemover implements SingleClassDriver, Opcodes {
  private final Trimmer trimmer;
  private final CalledMemberCollector calledMemberCollector;
  private final AtomicInteger classCounter = new AtomicInteger(0);
  private final AtomicInteger memberCounter = new AtomicInteger(0);


  private BField trimmedMethodErrorField;


  public UnusedMemberRemover(Trimmer trimmer, CalledMemberCollector calledMemberCollector) {
    this.trimmer = trimmer;
    this.calledMemberCollector = calledMemberCollector;

  }

  @Override
  public void preDrive() {
    if (trimmer.errorReplacement) {
      BClass trimmedMethodError = Conversion.loadProgramClass(trimmer.dataProvider, MissingMemberError.class);
      trimmedMethodError.version = trimmer.settingsManager.getTargetVersion();
      trimmer.dataProvider.addClass(trimmedMethodError);
      String descriptor = Type.getType(MissingMemberError.class).getDescriptor();
      trimmedMethodError = trimmer.nameTransformer.transformFieldsInsideClass(trimmedMethodError, trimmedMethodError.resolveField("ERROR", descriptor));
      trimmedMethodErrorField = trimmedMethodError.streamFields().findAny().orElseThrow();
    }
  }

  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    if (trimmer.disableReflectionDetection)
      return trimmer.defaultClassExclusionHandlers();
    return trimmer.defaultClassExclusionHandlersPlus(new ClassReflectionDetectionPassThrough(trimmer.dataProvider, ReflectionDetector.ReflectionUsage.NAME_USED, ReflectionDetector.ReflectionUsage.METHOD_USED, ReflectionDetector.ReflectionUsage.INSTANTATION));
  }

  @Override
  public boolean driveEach(BClass c) {
    if (!trimmer.keepUnusedClasses && !c.hasAnnotation("RetainPresence") && !calledMemberCollector.usedClasses.contains(c)) {
      if (c.directSubClasses.stream().noneMatch(calledMemberCollector.usedClasses::contains)) {
        String oldName = c.getName();
        String superName = c.superName;

        BClass old = trimmer.dataProvider.removeClass(c.getName());
        classCounter.incrementAndGet();
        if (old == null) {
          Branchlock.LOGGER.error("Could not remove class " + c.getName() + " from data provider");
        }

        if (trimmer.errorReplacement) {
          // do not reference old node here, it is destroyed
          BClass emptyCopy = new BClass(trimmer.dataProvider);
          emptyCopy.name = oldName;
          emptyCopy.superName = superName;
          emptyCopy.access = ACC_PUBLIC;
          emptyCopy.version = trimmer.settingsManager.getTargetVersion();

          BMethod bMethod = emptyCopy.getOrMakeStaticInitializer();
          InsnList instructions = bMethod.instructions;
          instructions.clear();
          instructions.add(trimmedMethodErrorField.createGet());
          instructions.add(new InsnNode(ATHROW));

          trimmer.dataProvider.addClass(emptyCopy);
        }
      }
      return true;
    }
    Set<BMethod> toRemoveMethods = new HashSet<>();
    for (BMethod method : c.methods) {
      if (Access.isNative(method.access)) continue;
      if (c.hasAnnotation("RetainPresence")) continue;
      if (!calledMemberCollector.calledMembers.contains(method)) {
        toRemoveMethods.add(method);
      }
    }
    if (trimmer.errorReplacement) {
      for (BMethod method : toRemoveMethods) {
        if (method.instructions.size() <= 1) continue;
        method.instructions.clear();
        method.instructions.add(trimmedMethodErrorField.createGet());
        method.instructions.add(new InsnNode(ATHROW));
        method.tryCatchBlocks = new ArrayList<>();
        method.localVariables = null;
        method.maxStack = 1;
        method.maxLocals = 1;
      }
    } else {
      toRemoveMethods.forEach(BMethod::resetEquivalenceClass);
      c.methods.removeAll(toRemoveMethods);
    }

    Set<BField> toRemoveFields = new HashSet<>();
    if (!trimmer.keepUnusedFields) {
      c.streamFields().forEach(bf -> {
        if (!c.hasAnnotation("RetainPresence") && !calledMemberCollector.calledMembers.contains(bf)) {
          toRemoveFields.add(bf);
        }
      });
      for (BField toRemoveField : toRemoveFields) {
        c.fields.remove(toRemoveField);
      }
    }

    memberCounter.set(memberCounter.get() + toRemoveMethods.size() + toRemoveFields.size());
    return true;
  }

  @Override
  public void postDrive() {
    if (trimmer.errorReplacement)
      Branchlock.LOGGER.info("Removed content of unused classes and members and added an error message.");
    else
      Branchlock.LOGGER.info("Removed {} classes and {} unused members.", classCounter.get(), memberCounter.get());

    // do not remove this as interface class method which bind methods together in equivalence classes can get removed.
    trimmer.dataProvider.prepareAllClasses();
  }

  @Override
  public String identifier() {
    return "unused-member-remover";
  }
}
