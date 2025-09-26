package net.branchlock.task.implementation.generic;

import net.branchlock.Branchlock;
import net.branchlock.commons.java.Pair;
import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.implementations.ClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.metadata.TaskMetadata;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@TaskMetadata(name = "Member shuffler", priority = TaskMetadata.Level.EIGHTH, ids = {"shuffle", "shuffle-members"})
public class ShuffleMembers extends Task implements ClassDriver {

  public ShuffleMembers(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public void preExecute() {
    LOGGER.info("Be aware that rearranging members may cause problems with reflection.");
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    return List.of(this);
  }

  @Override
  public boolean drive(Stream<BClass> stream) {
    stream.forEach(bc -> {
      bc.methods.shuffle();
      bc.fields.shuffle();
      // TODO shuffle debug attributes if it works
    });
    return true;
  }
  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    return defaultClassExclusionHandlers();
  }
}
