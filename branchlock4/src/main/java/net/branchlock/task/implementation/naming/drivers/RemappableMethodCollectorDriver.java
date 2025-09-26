package net.branchlock.task.implementation.naming.drivers;

import net.branchlock.structure.BMethod;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.task.data.ReflectionDetector;
import net.branchlock.task.driver.implementations.SingleMethodDriver;
import net.branchlock.task.driver.implementations.SingleMethodEquivalenceClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.naming.Renamer;

import java.util.*;

public class RemappableMethodCollectorDriver implements SingleMethodEquivalenceClassDriver {

  private final Renamer renamer;
  private final ReflectionDetector reflectionDetector;

  public List<BMethod> remappableMethods = new ArrayList<>();

  public RemappableMethodCollectorDriver(Renamer renamer, ReflectionDetector reflectionDetector) {
    this.renamer = renamer;
    this.reflectionDetector = reflectionDetector;
  }

  @Override
  public Collection<IPassThrough<IEquivalenceClass<BMethod>>> passThroughs() {
    if (renamer.disableReflectionDetection)
      return renamer.defaultEquivalenceClassExclusionHandlers();
    return renamer.defaultEquivalenceClassExclusionHandlersPlus(t ->
      t.filter(ec -> ec.getMembers().stream().anyMatch(bm -> !reflectionDetector.isAffected(bm))));
  }

  @Override
  public String identifier() {
    return "remappable-method-collector";
  }

  @Override
  public boolean driveEach(IEquivalenceClass<BMethod> equivalenceClass) {
    boolean forceRename = equivalenceClass.getMembers().stream().anyMatch(bm -> bm.hasAnnotation("ForceRename"));
    if (!forceRename && !equivalenceClass.isNameChangeable()) return true;

    remappableMethods.addAll(equivalenceClass.getMembers());
    return true;
  }
}
