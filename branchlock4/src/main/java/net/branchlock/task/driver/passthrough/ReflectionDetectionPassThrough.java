package net.branchlock.task.driver.passthrough;

import net.branchlock.commons.java.MultiMap;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMember;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.structure.provider.IDataProvider;
import net.branchlock.task.data.ReflectionDetector;
import org.objectweb.asm.Opcodes;

import java.util.Set;

public abstract class ReflectionDetectionPassThrough<T extends BMember> implements IPassThrough<T>, Opcodes {

  protected final IDataProvider dataProvider;
  protected final ReflectionDetector reflectionDetector;

  public ReflectionDetectionPassThrough(DataProvider dataProvider) {
    this.dataProvider = dataProvider;

    reflectionDetector = new ReflectionDetector(dataProvider);
    reflectionDetector.findAffectedClasses();
  }
}
