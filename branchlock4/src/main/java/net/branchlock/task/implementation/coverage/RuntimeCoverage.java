package net.branchlock.task.implementation.coverage;

import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.metadata.TaskMetadata;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@TaskMetadata(name = "Runtime class wrapper", priority = TaskMetadata.Level.FOURTH, performanceCost = TaskMetadata.PerformanceCost.MINIMAL, ids = {"cover-instantiation", "runtime-wrapping"})
public class RuntimeCoverage extends Task {

  private final CoverInstantiationDriver coverDriver = new CoverInstantiationDriver(this);
  private final StaticMergeDriver staticMergeDriver = new StaticMergeDriver(this);
  private final MergeMetafactoryDriver mergeMetafactoryDriver = new MergeMetafactoryDriver(this);
  private List<String> localClassNames;

  public RuntimeCoverage(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }


  @Override
  public void preExecute() {
    LOGGER.info("Replacing all runtime class instantiations with cover classes!");
    localClassNames = dataProvider.getClasses().values().stream().map(BClass::getName).collect(Collectors.toUnmodifiableList());
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    List<IDriver<?>> drivers = new LinkedList<>();
    drivers.add(coverDriver);
    if (!innerConfig.getOrDefaultValue("no_static_cover", false))
      drivers.add(staticMergeDriver);
    if (!settingsManager.isAndroid() && !innerConfig.getOrDefaultValue("no_metafactory_cover", false)) {
      // making custom invokedynamic instructions will break the reverse compatibility conversion of lambdas.
      // those invokedynamics cannot be translated to non-dynamic instructions anymore (dexer).
      // this will result in "invoke-customs are only supported starting with Android O" error (transform API).
      drivers.add(mergeMetafactoryDriver);
    }
    return drivers;
  }

  @Override
  public void postExecute() {
    LOGGER.info("Renaming cover methods.");
    Set<BMethod> toRemap = coverDriver.coverClasses.values().stream()
      .flatMap(c -> c.methods.stream())
      .filter(m -> !(m.isConstructor() || m.isStaticInitializer()))
      .collect(Collectors.toSet());

    for (MethodGate mg : staticMergeDriver.gates.values()) {
      BMethod e = mg.gateOwner.methods.get(mg.gateMethod.name, mg.gateMethod.desc);
      if (e == null) {
        throw new IllegalStateException("Gate method not added");
      }
      toRemap.add(e);
    }

    // Be careful when replacing this: We do not want to rename the whole equivalence class here.
    // Coverage adds cover methods with the same name as runtime classes.
    // They will be detected as being in the same equivalence class, but there aren't intended to be renamed as a whole.
    nameTransformer.transformMethodsOnly(toRemap);
  }

  public String getRandomLocalClassName() {
    return localClassNames.get(R.nextInt(localClassNames.size()));
  }
}
