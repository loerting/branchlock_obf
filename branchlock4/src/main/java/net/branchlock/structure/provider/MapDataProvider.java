package net.branchlock.structure.provider;


import net.branchlock.structure.BClass;
import net.branchlock.structure.BMember;
import net.branchlock.structure.BResource;

import java.util.HashMap;
import java.util.Map;

/**
 * A data provider that can be used for testing purposes.
 */
public class MapDataProvider implements IDataProvider {

  public final Map<String, BClass> classes = new HashMap<>();
  public final Map<String, BClass> libs = new HashMap<>();
  public final Map<String, BResource> unloadable = new HashMap<>();
  public final Map<String, BResource> resources = new HashMap<>();

  public MapDataProvider() {
  }

  @Override
  public Map<String, BClass> getClasses() {
    return classes;
  }

  @Override
  public Map<String, BClass> getLibs() {
    return libs;
  }

  @Override
  public Map<String, BResource> getUnloadableResources() {
    return unloadable;
  }

  @Override
  public BClass resolveBClass(String className, BMember accessedFrom) {
    if (classes.containsKey(className)) {
      return classes.get(className);
    }
    if (libs.containsKey(className)) {
      return libs.get(className);
    }
    return null;
  }

  @Override
  public Map<String, BResource> getResources() {
    return resources;
  }
}
