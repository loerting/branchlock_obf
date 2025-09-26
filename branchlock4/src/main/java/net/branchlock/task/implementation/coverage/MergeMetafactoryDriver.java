package net.branchlock.task.implementation.coverage;

import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.MethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.references.strings.driver.ConcatFactoryLifterDriver;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.stream.Stream;

public class MergeMetafactoryDriver implements MethodDriver, Opcodes {
  private final RuntimeCoverage runtimeCoverage;
  private BMethod metafactoryProxy;

  public MergeMetafactoryDriver(RuntimeCoverage runtimeCoverage) {
    this.runtimeCoverage = runtimeCoverage;
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    stream.flatMap(BMethod::streamInstr)
      .filter(insn -> insn.getType() == AbstractInsnNode.INVOKE_DYNAMIC_INSN)
      .map(insn -> (InvokeDynamicInsnNode) insn)
      .filter(dyn -> !ConcatFactoryLifterDriver.isConcatFactory(dyn)) // make sure string encryption can still lift them
      .forEach(this::introduceMetafactoryProxy);
    return true;
  }

  private void introduceMetafactoryProxy(InvokeDynamicInsnNode idin) {
    if (metafactoryProxy == null) {
      initMetafactoryProxy(idin);
    }

    idin.bsm = new Handle(idin.bsm.getTag(), metafactoryProxy.getOwner().getName(), metafactoryProxy.getName(),
      metafactoryProxy.getDescriptor(), idin.bsm.isInterface());
  }

  private void initMetafactoryProxy(InvokeDynamicInsnNode idin) {
    String desc = idin.bsm.getDesc();
    BClass nonInitializing = runtimeCoverage.dataUtilities.getPreparedNSEClass();
    String proxyName = runtimeCoverage.nameFactory.getUniqueMethodName(nonInitializing, desc);
    BMethod proxy = new BMethod(nonInitializing, ACC_PUBLIC | ACC_STATIC, proxyName, desc, null, null);
    int varIdx = 0;
    Type[] args = Type.getArgumentTypes(desc);
    for (Type arg : args) {
      proxy.instructions.add(new VarInsnNode(arg.getOpcode(ILOAD), varIdx));
      varIdx += arg.getSize();
    }
    proxy.maxLocals = proxy.maxStack = varIdx;
    proxy.instructions.add(new MethodInsnNode(INVOKESTATIC, idin.bsm.getOwner(), idin.bsm.getName(), desc));
    proxy.instructions.add(new InsnNode(Type.getReturnType(desc).getOpcode(IRETURN)));

    metafactoryProxy = proxy;
    nonInitializing.addMethod(metafactoryProxy);
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return runtimeCoverage.defaultMemberExclusionHandlersPlus(b -> b.filter(bm -> {
      BClass bClass = bm.getOwner();
      return bClass.version >= 52;
    }));
  }

  @Override
  public String identifier() {
    return "metafactory-merger";
  }
}
