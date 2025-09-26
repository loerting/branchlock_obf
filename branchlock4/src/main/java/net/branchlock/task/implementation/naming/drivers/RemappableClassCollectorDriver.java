package net.branchlock.task.implementation.naming.drivers;

import net.branchlock.structure.BClass;
import net.branchlock.task.data.ReflectionDetector;
import net.branchlock.task.driver.implementations.SingleClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.naming.Renamer;

import java.util.*;

public class RemappableClassCollectorDriver implements SingleClassDriver {

  private final Renamer renamer;
  private final ReflectionDetector reflectionDetector;

  public List<BClass> remappableClassNames = new ArrayList<>();

  public RemappableClassCollectorDriver(Renamer renamer, ReflectionDetector reflectionDetector) {
    this.renamer = renamer;
    this.reflectionDetector = reflectionDetector;
  }

  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    if (renamer.disableReflectionDetection)
      return renamer.defaultClassExclusionHandlers();
    return renamer.defaultClassExclusionHandlersPlus(t -> t.filter(bc -> !reflectionDetector.isAffected(bc, ReflectionDetector.ReflectionUsage.NAME_USED)));
  }

  @Override
  public String identifier() {
    return "remappable-class-collector";
  }

  @Override
  public boolean driveEach(BClass c) {
    boolean forceRename = c.hasAnnotation("ForceRename");
    if (!forceRename && !c.isNameChangeable()) return true;

    remappableClassNames.add(c);

    return true;
  }

}
