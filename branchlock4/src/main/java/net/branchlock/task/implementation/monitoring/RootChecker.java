package net.branchlock.task.implementation.monitoring;

import net.branchlock.commons.asm.Access;
import net.branchlock.commons.asm.Conversion;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.layout.android.ChecksAndroid;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.structure.provider.IDataStreamProvider;
import net.branchlock.task.Task;
import net.branchlock.task.agent.DriverRunner;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.implementations.ClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.flow.ControlFlow;
import net.branchlock.task.implementation.references.numbers.Numbers;
import net.branchlock.task.implementation.references.strings.utils.StringsToChars;
import net.branchlock.task.metadata.TaskMetadata;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@TaskMetadata(name = "Root detection", priority = TaskMetadata.Level.FIFTH, ids = {"anti-root", "root-checker"}, desktopCompatible = false)
public class RootChecker extends Task {
  private final AtomicInteger injectionCount = new AtomicInteger(0);
  private BMethod rootCheckerMethod;

  public RootChecker(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public void preExecute() {
    rootCheckerMethod = makeAndAddRootChecker();

    rootCheckerMethod.getCode().removeIf(insn -> insn.getType() == AbstractInsnNode.LINE);

      for (AbstractInsnNode ain : rootCheckerMethod.instructions.toArray()) {
      if (ain instanceof LdcInsnNode) {
        LdcInsnNode ldc = (LdcInsnNode) ain;
        if (ldc.cst instanceof String) {
            rootCheckerMethod.instructions.insertBefore(ain, StringsToChars.toChars(ldc, rootCheckerMethod.maxLocals));
            rootCheckerMethod.instructions.remove(ain);
        }
      }
      if (Instructions.isIntegerPush(ain)) {
        int integer = Instructions.getIntValue(ain);
          rootCheckerMethod.instructions.insertBefore(ain, Numbers.generateCalculation(integer, Numbers.NumbersStrength.STRONG));
          rootCheckerMethod.instructions.remove(ain);
      }
    }


    rootCheckerMethod.maxLocals++;

    DriverRunner driverRunner = new DriverRunner(new IDataStreamProvider() {
      @Override
      public Stream<BMethod> streamInputMethods() {
        return Stream.of(rootCheckerMethod);
      }
    });
    ControlFlow.getFlowDrivers(1f, this).forEach(driverRunner::runDriver);
  }

  private BMethod makeAndAddRootChecker() {
    BClass androidMethods = Conversion.loadProgramClass(dataProvider, ChecksAndroid.class);
    BMethod rootCheckMethodNode = androidMethods.methods.find("checkRoot", null);
    rootCheckMethodNode.localVariables = null;

    BClass nonInitializing = dataUtilities.getPreparedNSEClass();
    rootCheckMethodNode.moveTo(nonInitializing);
    return rootCheckMethodNode;
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    return List.of(new ClassDriver() {
      @Override
      public boolean drive(Stream<BClass> stream) {
        stream.forEach(bc -> {
          BMethod onCreate = bc.methods.get("onCreate", "(Landroid/os/Bundle;)V");
          if (onCreate != null && !Access.isStatic(onCreate.access)) {
            InsnList list = new InsnList();
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(rootCheckerMethod.makeInvoker());
            onCreate.instructions.insert(list);
            injectionCount.incrementAndGet();
          }
        });
        return true;
      }

      @Override
      public Collection<IPassThrough<BClass>> passThroughs() {
        return defaultClassExclusionHandlersPlus(t -> t.filter(bc -> bc.isAssertableTo("android/app/Activity") || bc.getOriginalName().endsWith("Activity")));
      }
    });
  }

  @Override
  public void postExecute() {
    nameTransformer.transformMethodsOnly(Collections.singleton(rootCheckerMethod));
    if (injectionCount.get() == 0) {
      LOGGER.error("No root checks have been inserted because no Activity with an onCreate(Bundle) method was found. Check the unknown classes.");
    } else {
      LOGGER.info("Injected {} root checks into onCreate(Bundle) methods.", injectionCount.get());
    }
  }
}
