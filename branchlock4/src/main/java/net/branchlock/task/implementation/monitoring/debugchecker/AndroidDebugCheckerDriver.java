package net.branchlock.task.implementation.monitoring.debugchecker;

import net.branchlock.commons.asm.Conversion;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.layout.android.ChecksAndroid;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.provider.IDataStreamProvider;
import net.branchlock.task.agent.DriverRunner;
import net.branchlock.task.driver.implementations.SingleClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.flow.ControlFlow;
import net.branchlock.task.implementation.references.numbers.Numbers;
import net.branchlock.task.implementation.references.strings.utils.StringsToChars;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public class AndroidDebugCheckerDriver implements SingleClassDriver {
  private final DebugChecker debugChecker;
  private final BMethod debugCheckMethod;

  public AndroidDebugCheckerDriver(DebugChecker debugChecker) {
    this.debugChecker = debugChecker;
    this.debugCheckMethod = Objects.requireNonNull(getDebugCheckMethod());
  }

  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    return debugChecker.defaultClassExclusionHandlersPlus(t -> t.filter(bc -> bc.isAssertableTo("android/app/Activity")
      || bc.getOriginalName().endsWith("Activity")));
  }

  @Override
  public boolean driveEach(BClass c) {
    BMethod staticInitializer = c.getOrMakeStaticInitializer();
    BMethod method = debugCheckMethod.duplicateMethod();
    for (AbstractInsnNode ain : method.instructions.toArray()) {
      if (ain instanceof LdcInsnNode) {
        LdcInsnNode ldc = (LdcInsnNode) ain;
        if (ldc.cst instanceof String) {
          method.instructions.insertBefore(ain, StringsToChars.toChars(ldc, method.maxLocals));
          method.instructions.remove(ain);
        }
      }
      if (Instructions.isIntegerPush(ain)) {
        int integer = Instructions.getIntValue(ain);
        method.instructions.insertBefore(ain, Numbers.generateCalculation(integer, Numbers.NumbersStrength.STRONG));
        method.instructions.remove(ain);
      }
    }

    method.maxLocals++;

    DriverRunner driverRunner = new DriverRunner(new IDataStreamProvider() {
      @Override
      public Stream<BMethod> streamInputMethods() {
        return Stream.of(method);
      }
    });
    ControlFlow.getFlowDrivers(1f, debugChecker).forEach(driverRunner::runDriver);
    staticInitializer.injectMethod(method);
    return true;
  }
  private BMethod getDebugCheckMethod() {
    BClass androidMethods = Conversion.loadProgramClass(debugChecker.dataProvider, ChecksAndroid.class);
    return androidMethods.methods.find("checkDebug", null);
  }

}
