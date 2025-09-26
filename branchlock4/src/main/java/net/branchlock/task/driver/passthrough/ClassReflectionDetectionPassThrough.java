package net.branchlock.task.driver.passthrough;

import net.branchlock.structure.BClass;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.data.ReflectionDetector;

import java.util.stream.Stream;

public class ClassReflectionDetectionPassThrough extends ReflectionDetectionPassThrough<BClass> {

  private final ReflectionDetector.ReflectionUsage[] characteristics;

  public ClassReflectionDetectionPassThrough(DataProvider dataProvider, ReflectionDetector.ReflectionUsage... characteristics) {
    super(dataProvider);
    this.characteristics = characteristics;
  }

  @Override
  public Stream<BClass> passThrough(Stream<BClass> t) {
    return t.filter(bClass -> !reflectionDetector.isAffected(bClass, characteristics));
  }
}
