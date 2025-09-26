package net.branchlock.task.implementation.generic;

import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.implementations.IndividualDriver;
import net.branchlock.task.metadata.TaskMetadata;
import net.branchlock.task.naming.nameiterator.implementation.AlphabetIterator;
import net.branchlock.task.naming.nameiterator.implementation.AlphabeticNameIterator;

import java.util.List;
import java.util.stream.Stream;

@TaskMetadata(name = "Crash reverse engineering tools", priority = TaskMetadata.Level.LAST, ids = "crasher", androidCompatible = false, demoApplicable = false)
public class CrashTools extends Task implements IndividualDriver {

  public CrashTools(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    return List.of(this);
  }

  @Override
  public boolean drive(Stream<Void> stream) {
    addSwingHTMLExploitClass();
    addStackOverflowClass();
    addCirculatingClasses();
    return true;
  }

  private void addCirculatingClasses() {
    // add three classes that all extend each other
    BClass newNoSideEffectClass = dataUtilities.createNewNoSideEffectClass();
    dataProvider.addClass(newNoSideEffectClass);
    BClass newNoSideEffectClass2 = dataUtilities.createNewNoSideEffectClass();
    dataProvider.addClass(newNoSideEffectClass2);
    BClass newNoSideEffectClass3 = dataUtilities.createNewNoSideEffectClass();
    dataProvider.addClass(newNoSideEffectClass3);

    // they require interface access, or branchlock itself will crash.

    newNoSideEffectClass.superName = newNoSideEffectClass2.name;
    newNoSideEffectClass.access |= ACC_INTERFACE;
    newNoSideEffectClass2.superName = newNoSideEffectClass3.name;
    newNoSideEffectClass2.access |= ACC_INTERFACE;
    newNoSideEffectClass3.superName = newNoSideEffectClass.name;
    newNoSideEffectClass3.access |= ACC_INTERFACE;
  }

  private void addSwingHTMLExploitClass() {
    BClass bc = dataUtilities.createNewNoSideEffectClass();
    bc.name = "<html><img src=\"http:\\\">";
    BMethod mn = new BMethod(bc, ACC_PUBLIC, "<init>", "(Z)V", null, null);
    bc.addMethod(mn);

    dataProvider.addClass(bc);
  }


  private void addStackOverflowClass() {
    BClass bc = dataUtilities.createNewNoSideEffectClass();
    AlphabetIterator iterator = new AlphabeticNameIterator();

    StringBuilder sb = new StringBuilder("META-INF");
    iterator.reset();
    for (int i = 0; i < Short.MAX_VALUE - ".class".length() - "META-INF/\0".length(); i++) {
      sb.append("/");
      sb.append(iterator.next().charAt(0));
    }
    bc.name = sb.toString();

    dataProvider.addClass(bc);
  }

  public boolean isFolderTrick() {
    return innerConfig.getOrDefaultValue("folder_trick", false);
  }
}
