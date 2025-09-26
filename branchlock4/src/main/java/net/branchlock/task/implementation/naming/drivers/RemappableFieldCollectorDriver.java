package net.branchlock.task.implementation.naming.drivers;

import net.branchlock.structure.BField;
import net.branchlock.task.data.ReflectionDetector;
import net.branchlock.task.driver.implementations.SingleFieldDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.naming.Renamer;

import java.util.*;

public class RemappableFieldCollectorDriver implements SingleFieldDriver {

  private final Renamer renamer;
  private final ReflectionDetector reflectionDetector;

  public List<BField> remappableFieldNames = new ArrayList<>();

  public RemappableFieldCollectorDriver(Renamer renamer, ReflectionDetector reflectionDetector) {
    this.renamer = renamer;
    this.reflectionDetector = reflectionDetector;
  }

  @Override
  public Collection<IPassThrough<BField>> passThroughs() {
    if (renamer.disableReflectionDetection)
      return renamer.defaultMemberExclusionHandlers();
    return renamer.defaultMemberExclusionHandlersPlus(t -> t.filter(bf -> !reflectionDetector.isAffected(bf)));
  }

  @Override
  public String identifier() {
    return "remappable-field-collector";
  }

  @Override
  public boolean driveEach(BField bf) {
    boolean forceRename = bf.hasAnnotation("ForceRename");
    if (!bf.isNameChangeable() && !forceRename) return true;
    remappableFieldNames.add(bf);
    return true;
  }
}
