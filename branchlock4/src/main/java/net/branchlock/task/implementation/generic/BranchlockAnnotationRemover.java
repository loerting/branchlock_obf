package net.branchlock.task.implementation.generic;

import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.implementations.SingleClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.metadata.TaskMetadata;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InnerClassNode;

import java.util.Collection;
import java.util.List;

@TaskMetadata(name = "Branchlock annotation trimmer", priority = TaskMetadata.Level.LAST, ids = "bl-anno-remover")
public class BranchlockAnnotationRemover extends Task implements SingleClassDriver {

  public BranchlockAnnotationRemover(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    return List.of(this);
  }

  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    return List.of();
  }

  @Override
  public boolean driveEach(BClass c) {
    if (c.getName().startsWith("net/branchlock/annotations/")) {
      dataProvider.removeClass(c.getName());
      return true;
    }

    removeAnnotations(c.invisibleAnnotations);
    removeAnnotations(c.visibleAnnotations);

    for (BField f : c.fields) {
      removeAnnotations(f.invisibleAnnotations);
      removeAnnotations(f.visibleAnnotations);
    }
    for (BMethod m : c.methods) {
      removeAnnotations(m.invisibleAnnotations);
      removeAnnotations(m.visibleAnnotations);
    }
    return true;
  }

  private static void removeAnnotations(List<AnnotationNode> list) {
    if (list == null) return;
    list.removeIf(a -> a.desc.startsWith("Lnet/branchlock/annotations/"));
  }
}
