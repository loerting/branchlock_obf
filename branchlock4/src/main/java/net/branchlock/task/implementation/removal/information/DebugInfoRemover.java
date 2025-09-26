package net.branchlock.task.implementation.removal.information;

import net.branchlock.commons.asm.Instructions;
import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.implementations.SingleClassDriver;
import net.branchlock.task.driver.implementations.SingleFieldDriver;
import net.branchlock.task.driver.implementations.SingleMethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.metadata.TaskMetadata;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.*;
import java.util.stream.Collectors;

@TaskMetadata(name = "Debug information remover", priority = TaskMetadata.Level.FIRST, ids = {"debug-info-remover", "debug-remover"})
public class DebugInfoRemover extends Task {

  // TODO maybe remove module-info?
  private static final List<String> KOTLIN_METADATA = Arrays.asList("Lkotlin/coroutines/jvm/internal/DebugMetadata;", "Lkotlin/Metadata;");
  private static final String KOTLIN_SDE = "Lkotlin/jvm/internal/SourceDebugExtension;";
  private final boolean keepStacktrace = innerConfig.getOrDefaultValue("keep_stacktrace_info", false);
  private final boolean removeAnnotations = innerConfig.getOrDefaultValue("remove_annotations", false);
  private final boolean keepLocalVars = innerConfig.getOrDefaultValue("keep_local_vars", false);
  private final boolean keepSignatures = innerConfig.getOrDefaultValue("keep_signatures", false);
  private final boolean removeKotlinMetadata = innerConfig.getOrDefaultValue("remove_kotlin", false);

  private static final Set<String> REMOVABLE_ANNOTATIONS = Set.of("Ljava/lang/Deprecated;");
  public DebugInfoRemover(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public void preExecute() {
    if (removeKotlinMetadata) {
      LOGGER.info("Removing Kotlin metadata. Don't do this if you want to use Kotlin reflection or are obfuscating a library.");
    }
  }

  @Override
  public List<IDriver<?>> getDrivers() {
      List<IDriver<?>> iDrivers = new ArrayList<>();
      iDrivers.add(new ClassDebugInfoRemover());
      iDrivers.add(new MethodDebugInfoRemover());
      iDrivers.add(new FieldDebugInfoRemover());
      if(removeKotlinMetadata) {
        iDrivers.add(new KotlinIntrinsicsAdapter(this));
      }
      return iDrivers;
  }

  private static void handleSourceDebugExtension(List<AnnotationNode> visibleAnnotations) {
    visibleAnnotations.stream().filter(anno -> KOTLIN_SDE.equals(anno.desc)).forEach(anno -> {
      // replace with a minimal source map of the java bytecode spec which contains no information.
      anno.values = new ArrayList<>();
      anno.values.add("value");
      anno.values.add(new ArrayList<>(List.of("SMAP\n\nKotlin\n*S Kotlin\n*F\n+ 1 \n\n*L\n1#1,1:1\n*E")));
    });
  }

  private class ClassDebugInfoRemover implements SingleClassDriver {

    @Override
    public boolean driveEach(BClass c) {
        if (!keepStacktrace)
            c.sourceFile = null;
        c.sourceDebug = null;
        c.innerClasses = new ArrayList<>();
        c.outerClass = null;
        c.outerMethod = null;
        c.outerMethodDesc = null;
        c.nestHostClass = null;
        c.nestMembers = null;
        if (!keepSignatures)
            c.signature = null;
        if (removeAnnotations) {
            c.invisibleTypeAnnotations = null;
            c.visibleTypeAnnotations = null;
            c.invisibleAnnotations = null;
            c.visibleAnnotations = null;
        } else {
            if (c.invisibleAnnotations != null)
                c.invisibleAnnotations.removeIf(anno -> REMOVABLE_ANNOTATIONS.contains(anno.desc));
            if (c.visibleAnnotations != null)
                c.visibleAnnotations.removeIf(anno -> REMOVABLE_ANNOTATIONS.contains(anno.desc));
        }

        if (c.visibleAnnotations != null && removeKotlinMetadata) {
            c.visibleAnnotations.removeIf(anno -> KOTLIN_METADATA.contains(anno.desc));
            handleSourceDebugExtension(c.visibleAnnotations);
        }
        if (c.invisibleAnnotations != null && removeKotlinMetadata) {
            c.invisibleAnnotations.removeIf(anno -> KOTLIN_METADATA.contains(anno.desc));
            handleSourceDebugExtension(c.invisibleAnnotations);
        }

        BMethod staticInit = c.methods.get("<clinit>", "()V");
        if (staticInit != null) {
            AbstractInsnNode realNext = Instructions.getRealNext(staticInit.instructions.getFirst());
            if (realNext == null || realNext.getOpcode() == RETURN) {
                c.methods.remove(staticInit);
            }
        }

        return true;
    }

    @Override
    public Collection<IPassThrough<BClass>> passThroughs() {
        return defaultClassExclusionHandlers();
    }

  }


  private class MethodDebugInfoRemover implements SingleMethodDriver {
    @Override
    public boolean driveEach(BMethod m) {
        if (!keepLocalVars) {
            m.localVariables = null;
            m.invisibleLocalVariableAnnotations = null;
            m.visibleLocalVariableAnnotations = null;
        }

        if (m.tryCatchBlocks != null)
            m.tryCatchBlocks.stream().filter(tcb -> "java/lang/Throwable".equals(tcb.type)).forEach(tcb -> tcb.type = null);
        m.exceptions = null;

        if (!keepSignatures)
            m.signature = null;

        if (removeAnnotations) {
            m.invisibleTypeAnnotations = null;
            m.visibleTypeAnnotations = null;
            m.invisibleAnnotations = null;
            m.visibleAnnotations = null;
            m.invisibleParameterAnnotations = null;
            m.visibleParameterAnnotations = null;
        } else {
            if (m.invisibleAnnotations != null)
                m.invisibleAnnotations.removeIf(anno -> REMOVABLE_ANNOTATIONS.contains(anno.desc));
            if (m.visibleAnnotations != null)
                m.visibleAnnotations.removeIf(anno -> REMOVABLE_ANNOTATIONS.contains(anno.desc));
        }

        if (!keepStacktrace) {
            // remove line numbers
            m.getCode().removeIf(insn -> insn.getType() == AbstractInsnNode.LINE);
        }
        return true;
    }

    @Override
    public Collection<IPassThrough<BMethod>> passThroughs() {
        return defaultMemberExclusionHandlers();
    }

  }

  private class FieldDebugInfoRemover implements SingleFieldDriver {
    @Override
    public boolean driveEach(BField f) {
        if (!keepSignatures)
            f.signature = null;
        if (removeAnnotations) {
            f.invisibleTypeAnnotations = null;
            f.visibleTypeAnnotations = null;
            f.invisibleAnnotations = null;
            f.visibleAnnotations = null;
        } else {
            if (f.invisibleAnnotations != null)
                f.invisibleAnnotations.removeIf(anno -> REMOVABLE_ANNOTATIONS.contains(anno.desc));
            if (f.visibleAnnotations != null)
                f.visibleAnnotations.removeIf(anno -> REMOVABLE_ANNOTATIONS.contains(anno.desc));
        }
        if (!f.isStatic()) {
            // JVM spec 4.7.2: If a field_info structure representing a non-static field has a ConstantValue attribute, then that attribute must silently be ignored.
            f.value = null;
        }
        return true;
    }

    @Override
    public Collection<IPassThrough<BField>> passThroughs() {
        return defaultMemberExclusionHandlers();
    }
  }
}
