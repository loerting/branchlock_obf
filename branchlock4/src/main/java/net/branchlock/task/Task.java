package net.branchlock.task;

import mjson.Json;
import net.branchlock.Branchlock;
import net.branchlock.config.Config;
import net.branchlock.config.JsonConfig;
import net.branchlock.config.SettingsManager;
import net.branchlock.logging.LogWrapper;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMember;
import net.branchlock.structure.BResource;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.structure.equivalenceclass.MultiEquivalenceClass;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.data.DataUtilities;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.passthrough.ConfigClassExclusionPassThrough;
import net.branchlock.task.driver.passthrough.ConfigEquivalenceClassExclusionPassThrough;
import net.branchlock.task.driver.passthrough.ConfigMemberExclusionPassThrough;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.metadata.TaskMetadata;
import net.branchlock.task.naming.NameFactory;
import net.branchlock.task.naming.NameTransformer;
import org.objectweb.asm.Opcodes;

import java.util.*;

public abstract class Task implements Opcodes {
  protected static final Random R = Branchlock.R;
  protected static final LogWrapper LOGGER = Branchlock.LOGGER;
  public final SettingsManager settingsManager;
  public final DataProvider dataProvider;
  public final NameFactory nameFactory;
  public final DataUtilities dataUtilities;
  public final NameTransformer nameTransformer;
  protected final Config innerConfig;

  protected Task(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    this.settingsManager = Objects.requireNonNull(settingsManager);
    this.innerConfig = innerConfig == null ? new JsonConfig(Json.object()) : innerConfig;
    this.dataProvider = Objects.requireNonNull(dataProvider);
    this.nameFactory = new NameFactory(dataProvider);
    this.nameTransformer = new NameTransformer(nameFactory, dataProvider);
    this.dataUtilities = new DataUtilities(this, dataProvider);
  }

  /**
   * Each driver will be touched in the order they are added to the list.
   */
  public abstract List<IDriver<?>> getDrivers();

  public final TaskMetadata getMetadata() {
    return getClass().getAnnotation(TaskMetadata.class);
  }

  @SafeVarargs
  public final Collection<IPassThrough<BClass>> defaultClassExclusionHandlersPlus(IPassThrough<BClass>... handlers) {
    Collection<IPassThrough<BClass>> iPassThroughs = new ArrayList<>(defaultClassExclusionHandlers());
    iPassThroughs.addAll(List.of(handlers));

    return iPassThroughs;
  }

  public final Collection<IPassThrough<BClass>> defaultClassExclusionHandlers() {
    return List.of(new ConfigClassExclusionPassThrough(settingsManager.getGeneralConfig(), dataProvider), new ConfigClassExclusionPassThrough(innerConfig, dataProvider));
  }

  @SafeVarargs
  public final <T extends BMember> Collection<IPassThrough<T>> defaultMemberExclusionHandlersPlus(IPassThrough<T>... handlers) {
    Collection<IPassThrough<T>> iPassThroughs = new ArrayList<>(defaultMemberExclusionHandlers());
    iPassThroughs.addAll(List.of(handlers));
    return iPassThroughs;
  }

  public <T extends BMember> Collection<IPassThrough<T>> defaultMemberExclusionHandlers() {
    return List.of(new ConfigMemberExclusionPassThrough<>(settingsManager.getGeneralConfig(), dataProvider),
      new ConfigMemberExclusionPassThrough<>(innerConfig, dataProvider));
  }

  public <E extends BMember, T extends IEquivalenceClass<E>> Collection<IPassThrough<T>> defaultEquivalenceClassExclusionHandlers() {
    return List.of(new ConfigEquivalenceClassExclusionPassThrough<>(settingsManager.getGeneralConfig(), dataProvider),
      new ConfigEquivalenceClassExclusionPassThrough<>(innerConfig, dataProvider));
  }

  @SafeVarargs
  public final <E extends BMember, T extends IEquivalenceClass<E>> Collection<IPassThrough<T>> defaultEquivalenceClassExclusionHandlersPlus(IPassThrough<T>... handlers) {
    Collection<IPassThrough<T>> iPassThroughs = new ArrayList<>(defaultEquivalenceClassExclusionHandlers());
    iPassThroughs.addAll(List.of(handlers));
    return iPassThroughs;
  }

  public final <T> Collection<IPassThrough<T>> noExclusionHandlers() {
    return List.of();
  }

  public void preExecute() {
  }

  public void postExecute() {
  }

  public Collection<IPassThrough<MultiEquivalenceClass>> localEquivalenceClassPassThrough() {
    return List.of(t -> t.filter(MultiEquivalenceClass::isLocal));
  }

}
