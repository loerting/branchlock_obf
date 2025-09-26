package net.branchlock.task.data;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Access;
import net.branchlock.structure.BClass;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.agent.DriverRunner;
import net.branchlock.task.driver.implementations.ClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import org.objectweb.asm.Opcodes;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Various utilities on the class data.
 * One DataUtilities instance is created per task.
 */
public class DataUtilities implements Opcodes {
  private static final Random R = Branchlock.R;
  private final DataProvider dataProvider;
  private Task task;
  private final IPassThrough<BClass> noSideEffectFilter = t -> {
    int targetVersion = task.settingsManager.getTargetVersion();
    return t.filter(bc -> {
      if (bc.isInterface() || !bc.isLocal()) return false;
      return bc.version <= targetVersion && !bc.hasStaticInitSideEffect();
    });
  };


  private List<BClass> noSideEffectClasses;

  public DataUtilities(Task task, DataProvider dataProvider) {
    this.task = task;
    this.dataProvider = dataProvider;
  }

  /**
   * Find a random class that has no side effects if a method is called on it.
   */
  protected BClass randomClassNoSideEffects() {
    if (noSideEffectClasses == null) collectNoSideEffectClasses();
    if (noSideEffectClasses.isEmpty()) {
      Branchlock.LOGGER.info("No classes found that have no side effects. Creating a new class.");
      // create a new class.
      BClass bClass = createNewNoSideEffectClass();

      noSideEffectClasses.add(bClass);
      dataProvider.addClass(bClass);
    }
    return noSideEffectClasses.get(R.nextInt(noSideEffectClasses.size()));
  }

  public BClass createNewNoSideEffectClass() {
    BClass classNode = new BClass(dataProvider);
    classNode.access = ACC_PUBLIC;
    classNode.name = task.nameFactory.getUniqueClassNameInPackage();
    classNode.superName = "java/lang/Object";
    classNode.version = task.settingsManager.getTargetVersion();
    classNode.setOriginalName("net/branchlock/generated/NoSideEffectClass");
    return classNode;
  }

  private void collectNoSideEffectClasses() {
    ClassDriver classDriver = new ClassDriver() {

      @Override
      public boolean drive(Stream<BClass> stream) {
        noSideEffectClasses = stream.collect(Collectors.toList());
        return true;
      }

      @Override
      public Collection<IPassThrough<BClass>> passThroughs() {
        return task.defaultClassExclusionHandlersPlus(noSideEffectFilter);
      }

      @Override
      public String identifier() {
        return "nse-class-collector";
      }
    };

    if (!new DriverRunner(dataProvider).runDriver(classDriver)) {
      throw new RuntimeException("Failed to collect no side effect classes.");
    }
    if (noSideEffectClasses == null) throw new IllegalStateException("noSideEffectClasses is null");
  }

  /**
   * Get a random class that has no side effects and is accessible from anywhere.
   */
  public BClass getPreparedNSEClass() {
    return prepareClassForUse(randomClassNoSideEffects());
  }

  public List<BClass> getUnpreparedNSEClasses() {
    if (noSideEffectClasses == null) collectNoSideEffectClasses();
    return noSideEffectClasses;
  }

  public BClass prepareClassForUse(BClass bClass) {
    // make sure it is public (remove ACC_PRIVATE and ACC_PROTECTED, then add ACC_PUBLIC)
    bClass.access = Access.removeAccess(bClass.access, ACC_PRIVATE | ACC_PROTECTED);
    bClass.access |= ACC_PUBLIC;

    return bClass;
  }

  public void addNoSideEffectClass(BClass bClass) {
    if (noSideEffectClasses == null) collectNoSideEffectClasses();
    noSideEffectClasses.add(bClass);
  }
}
