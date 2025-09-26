package net.branchlock.task.implementation.removal;

import net.branchlock.Branchlock;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMember;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.driver.implementations.IndividualDriver;
import org.objectweb.asm.Opcodes;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalledMemberCollector implements IndividualDriver, Opcodes {

  public final Set<BMember> calledMembers = new HashSet<>();
  private final Trimmer task;
  public Set<BClass> usedClasses;

  public CalledMemberCollector(Trimmer task) {
    this.task = task;
  }

  @Override
  public boolean drive(Stream<Void> stream) {
    Branchlock.LOGGER.info("Use the @EntryPoint annotation to mark methods as entry points.");
    Set<BMethod> entryPoints = task.dataProvider.streamInputMethods()
      .filter(bm -> (task.onlyAnnotatedEntryPoints ? bm.hasAnnotation("EntryPoint") : bm.isPotentialEntryPoint()) || !bm.requireEquivalenceClass().isLocal())
      .collect(Collectors.toSet());

    for (BMethod entryPoint : entryPoints) {
      buildExecutionTree(entryPoint, calledMembers);
    }

    usedClasses = calledMembers.stream()
      .map(m -> m.hasOwner() ? (BClass) m.getOwner() : (BClass) m)
      .collect(Collectors.toSet());

    Set<BClass> parents = new HashSet<>();
    usedClasses.forEach(bc -> parents.addAll(bc.getDirectParentClasses()));
    usedClasses.addAll(parents);

    return !entryPoints.isEmpty() && !usedClasses.isEmpty();
  }

  private void buildExecutionTree(BMethod currentMethod, Set<BMember> usedMembers) {
    Deque<BMethod> stack = new ArrayDeque<>();
    stack.push(currentMethod);

    while (!stack.isEmpty()) {
      BMethod method = stack.pop();
      if (!method.isLocal()) continue;
      if (usedMembers.contains(method)) continue;
      usedMembers.add(method);

      method.getCode().iterateOverReferences(ref -> {
        BClass bClass = task.dataProvider.getClasses().get(ref.owner);
        if (bClass == null) return;
        switch (ref.type) {
          case METHOD_INVOKE -> {
            BMethod bMethod = bClass.resolveMethod(ref.name, ref.desc);
            if (bMethod == null) return;
            IEquivalenceClass<BMethod> equivalenceClass = bMethod.requireEquivalenceClass();
            for (BMethod member : equivalenceClass.getMembers()) {
              stack.push(member);
            }
          }
          case FIELD_GET, FIELD_SET -> {
            BField field = bClass.resolveField(ref.name, ref.desc);
            if (field != null)
              usedMembers.add(field);
          }
          case CLASS_TYPE -> usedMembers.add(bClass);
        }
      });
    }
  }

  @Override
  public String identifier() {
    return "call-graph-analyzer";
  }
}
