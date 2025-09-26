package net.branchlock.task.implementation.generic;

import net.branchlock.commons.asm.Access;
import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.implementations.ClassDriver;
import net.branchlock.task.driver.implementations.FieldDriver;
import net.branchlock.task.driver.implementations.MethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.metadata.TaskMetadata;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@TaskMetadata(name = "Unify member visibility", priority = TaskMetadata.Level.FIRST, androidCompatible = false,
  ids = {"unify-visibility", "generalize-access", "generalize-visibility"})
public class UnifyVisibility extends Task {

  public UnifyVisibility(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    return List.of(new ClassDriver() {
      @Override
      public boolean drive(Stream<BClass> stream) {
        stream.forEach(bc -> {
          bc.access = Access.publicAndGeneric(bc.access);
        });
        return true;
      }

      @Override
      public Collection<IPassThrough<BClass>> passThroughs() {
        return defaultClassExclusionHandlers();
      }
    }, new MethodDriver() {
      @Override
      public boolean drive(Stream<BMethod> stream) {
        stream.forEach(bm -> {
          bm.access = Access.publicAndGeneric(bm.access);
        });
        return true;
      }

      @Override
      public Collection<IPassThrough<BMethod>> passThroughs() {
        return defaultMemberExclusionHandlersPlus(t -> t.filter(bm -> !bm.getOwner().isInterface()));
      }
    }, new FieldDriver() {
      @Override
      public boolean drive(Stream<BField> stream) {
        stream.forEach(bf -> bf.access = Access.publicAndGeneric(bf.access));
        return true;
      }

      @Override
      public Collection<IPassThrough<BField>> passThroughs() {
        return defaultMemberExclusionHandlersPlus(t -> t.filter(bm -> !bm.getOwner().isInterface()));
      }
    });
  }

}
