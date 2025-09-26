package net.branchlock.task.implementation.flow.passes;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Access;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.commons.asm.Reference;
import net.branchlock.commons.asm.interpreter.TypeInterpreter;
import net.branchlock.commons.java.Pair;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMethod;
import net.branchlock.task.Task;
import net.branchlock.task.implementation.flow.FlowPassDriver;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TryCatchTrapPass extends FlowPassDriver {
  private static final List<String> UNUSUAL_THROWABLES =
    Arrays.asList("java/lang/CloneNotSupportedException", "java/lang/ReflectiveOperationException",
      "java/lang/EnumConstantNotPresentException", "java/lang/IllegalMonitorStateException",
      "java/lang/InstantiationException",
      "java/lang/NoSuchMethodException",
      "java/lang/reflect/InvocationTargetException",
      "java/lang/reflect/GenericSignatureFormatError",
      "java/lang/ClassFormatError", "java/lang/ThreadDeath");
  private List<BClass> localThrowableList;
  private BMethod selfReturnProxy;
  private Reference dummyThrowableFieldSet;

  public TryCatchTrapPass(Task task, float coveragePct) {
    super(task, coveragePct);
    collectLocalExceptions();
  }

  private static void updateConstructors(List<BMethod> constructors) {
    for (BMethod bm : constructors) {
        for (AbstractInsnNode insn : bm.instructions) {
        AbstractInsnNode realPrevious = Instructions.getRealPrevious(insn);
        if (insn.getOpcode() == INVOKESPECIAL && realPrevious != null && realPrevious.getOpcode() == ALOAD) {
          MethodInsnNode min = (MethodInsnNode) insn;
          if (min.name.equals("<init>") && min.owner.equals("java/lang/Object") && ((VarInsnNode) realPrevious).var == 0) {
            min.owner = "java/lang/Throwable";
          }
        }
      }
    }
  }

  @Override
  public String identifier() {
    return "reorder-flow-and-trap-tcb";
  }

  @Override
  protected boolean isFitting(BMethod t) {
    return t.getInstructionCount() >= 10 && !t.isConstructor();
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    stream.forEach(bm -> {
      bm.localVariables = null; // make sure there are no label conflicts
      List<Pair<LabelNode, Frame<BasicValue>>> labels = bm.getNodesWithFrames(new TypeInterpreter(task.dataProvider, bm), LabelNode.class);

      if (labels == null || labels.isEmpty())
        return;

      Iterator<Pair<LabelNode, Frame<BasicValue>>> iterator = labels.iterator();
      int splitIndex;
      while (true) {
        if (!iterator.hasNext())
          return;
          splitIndex = bm.instructions.indexOf(iterator.next().a);
        if (splitIndex == 0 || splitIndex >= bm.getInstructionCount() - 1)
          continue;
        break;
      }

      InsnList lowerPart = new InsnList();
      bm.streamInstr().skip(splitIndex).forEach(i -> {
          bm.instructions.remove(i);
        lowerPart.add(i);
      });


      LabelNode firstPartStart = new LabelNode();
      LabelNode secondPartStart = new LabelNode();

      InsnList newStart = new InsnList();
      // jump to beginning of first part, that is now the lower part of the code
      newStart.add(new JumpInsnNode(GOTO, firstPartStart));

      newStart.add(secondPartStart); // beginning of second code part
      //newStart.addMember(new InsnNode(NOP)) we don't want try catch blocks with same start and end, #removeRedundantIllegalTCBs

      lowerPart.insert(newStart);
      lowerPart.add(firstPartStart);


      LabelNode firstPartEnd = new LabelNode();
      if (bm.tryCatchBlocks != null) {
        if (!bm.tryCatchBlocks.isEmpty()) {
          for (TryCatchBlockNode tcbn : new ArrayList<>(bm.tryCatchBlocks)) {
              if (bm.instructions.contains(tcbn.start) && lowerPart.contains(tcbn.end)) {
              // try catch block reaches over both, split the try block in two, keep handler
              bm.tryCatchBlocks.add(new TryCatchBlockNode(tcbn.start, firstPartEnd, tcbn.handler, tcbn.type)); // lower tcb
              tcbn.start = secondPartStart; // upper tcb
            }
          }
          // remove redundant / illegal try catch blocks
          bm.tryCatchBlocks.removeIf(tcb -> Instructions.getRealNext(tcb.start) == Instructions.getRealNext(tcb.end));
        }
      }

        bm.instructions.insert(lowerPart);
        bm.instructions.add(firstPartEnd);
        bm.instructions.add(new JumpInsnNode(GOTO, secondPartStart)); // first part finished, goto second

      trapTCBs(bm, secondPartStart, firstPartStart, firstPartEnd);
    });
    return true;
  }

  private void trapTCBs(BMethod bm, LabelNode secondPartStart, LabelNode firstPartStart, LabelNode firstPartEnd) {
    int size = bm.getInstructionCount();
      InsnList instr = bm.instructions;
    for (int i = 0; i < getTrapTCBCount(size); i++) {

      int from = randFromTo(instr.indexOf(secondPartStart), instr.indexOf(firstPartStart));
      int to = randFromTo(instr.indexOf(firstPartStart), instr.indexOf(firstPartEnd));

      AbstractInsnNode nodeStart = instr.get(from);
      AbstractInsnNode nodeEnd = instr.get(to);
      if (nodeStart == nodeEnd || instr.indexOf(Instructions.getRealNext(nodeStart)) >= to)
        return;

      LabelNode tcbStart = new LabelNode();
      LabelNode tcbEnd = new LabelNode();
      LabelNode tcbHandler = new LabelNode();

      instr.insertBefore(nodeStart, tcbStart);
      instr.insert(nodeEnd, tcbEnd);

      instr.add(tcbHandler);

      if (R.nextBoolean() && dummyThrowableFieldSet != null) {
        instr.add(getThrowableReturnProxy());
        instr.add(new InsnNode(ATHROW));
      } else {
        instr.add(new InsnNode(DUP));
        instr.add(getThrowableRandomFieldSet(false));
        instr.add(new InsnNode(ATHROW));
      }

      String exception;
      if (localThrowableList.size() < 3 || R.nextFloat() < 0.025f) {
        exception = getRandomException();
      } else {
        BClass bClass = localThrowableList.get(R.nextInt(localThrowableList.size()));
        exception = bClass.name;
      }

      if (bm.exceptions != null) {
        if (bm.exceptions.contains(exception)) {
          // the exception is more likely to get thrown in this method. choose an independent exception instead.
          // this is not optimal yet, because m.exceptions is only debug information.
          // would have to write a full scanner.
          exception = getRandomException();
        }
      }

      bm.tryCatchBlocks.add(new TryCatchBlockNode(tcbStart, tcbEnd, tcbHandler, exception));
    }
  }

  private int getTrapTCBCount(int size) {
    // in average about 1 to 3 try catch blocks per method
    return (int) Math.ceil(Math.pow(size, 0.5) * (0.1f + 0.1f * coveragePct));
  }

  private AbstractInsnNode getThrowableReturnProxy() {
    if (selfReturnProxy == null || R.nextFloat() < 0.01) {
      if (dummyThrowableFieldSet == null)
        throw new IllegalStateException("getThrowableRandomFieldSet has to be invoked first");

      String desc = "(Ljava/lang/Throwable;)Ljava/lang/Throwable;";
      BClass nonInitializing = task.dataUtilities.getPreparedNSEClass();
      String proxyName = task.nameFactory.getUniqueMethodName(nonInitializing, desc);
      BMethod proxy = new BMethod(nonInitializing, ACC_PUBLIC | ACC_STATIC, proxyName, desc, null, null);
      proxy.maxLocals = 1;
      proxy.maxStack = 1;

      LabelNode jump = new LabelNode();

      Reference fieldGet = dummyThrowableFieldSet.copyWithNewType(Reference.RefType.FIELD_GET);
      proxy.instructions.add(fieldGet.createStaticInstruction());
      proxy.instructions.add(new JumpInsnNode(R.nextBoolean() ? IFNULL : IFNONNULL, jump));
      proxy.instructions.add(new VarInsnNode(ALOAD, 0));
      proxy.instructions.add(getThrowableRandomFieldSet(true));
      proxy.instructions.add(jump);
      proxy.instructions.add(new VarInsnNode(ALOAD, 0));
      proxy.instructions.add(new InsnNode(ARETURN));

      selfReturnProxy = proxy;
      nonInitializing.addMethod(selfReturnProxy);
    }
    return selfReturnProxy.toReference().createStaticInstruction();
  }

  private AbstractInsnNode getThrowableRandomFieldSet(boolean fromInsideProxy) {
    if (dummyThrowableFieldSet == null || (fromInsideProxy && R.nextBoolean())) {
      BClass nonInitClass = task.dataUtilities.getPreparedNSEClass();
      String fieldDescriptor = "Ljava/lang/Throwable;";
      BField fn = new BField(nonInitClass, ACC_PUBLIC | ACC_STATIC,
        task.nameFactory.getUniqueFieldName(nonInitClass, fieldDescriptor),
        fieldDescriptor, null, null);
      nonInitClass.addField(fn);
      dummyThrowableFieldSet = Reference.of(nonInitClass.getName(), fn.name, fn.desc, Reference.RefType.FIELD_SET);
    }
    return dummyThrowableFieldSet.createStaticInstruction();
  }

  private int randFromTo(int min, int max) {
    return R.nextInt((max - min) + 1) + min;
  }

  private void collectLocalExceptions() {
    turnNSEIntoThrowable();
    localThrowableList = task.dataProvider.streamInputClasses()
      .filter(bc -> {
        return Access.isPublic(bc.access) && bc.isAssertableTo("java/lang/Throwable");
      })
      .collect(Collectors.toList());
    Branchlock.LOGGER.info("Collected {} local Throwables.", localThrowableList.size());
  }

  private void turnNSEIntoThrowable() {
    AtomicInteger count = new AtomicInteger();
    task.dataUtilities.getUnpreparedNSEClasses().forEach(bc -> {
      // turn non initializing classes that are not initialized often into throwable, so we can use them!
      ClassNode node = bc;
      if (!"java/lang/Object".equals(node.superName) || bc.isInterface() || bc.isEnum())
        return;

      List<BMethod> constructors = bc.methods.stream().filter(BMethod::isConstructor).collect(Collectors.toList());
      if (!constructors.stream().allMatch(bm -> {
        return Access.isPrivate(bm.access);
      }))
        return;

      task.dataUtilities.prepareClassForUse(bc);
      count.incrementAndGet();
      node.superName = "java/lang/Throwable";
      task.dataProvider.populateSubClassesForParents(bc); // as the super class changed, we need to update

      // also update "this" initialization inside <init>.
      updateConstructors(constructors);
    });
    Branchlock.LOGGER.info("Turned {} classes into Throwables.", count.get());
  }

  private String getRandomException() {
    return UNUSUAL_THROWABLES.get(R.nextInt(UNUSUAL_THROWABLES.size()));
  }
}
