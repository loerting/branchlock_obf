package net.branchlock.structure.provider;

import net.branchlock.structure.BClass;
import net.branchlock.structure.BMember;
import net.branchlock.structure.BResource;

import java.util.Map;

public interface IDataProvider extends IClassProvider, IResourceProvider {

  Map<String, BClass> getLibs();

  Map<String, BResource> getUnloadableResources();

  BClass resolveBClass(String className, BMember accessedFrom);
}
