package net.branchlock.structure.provider;

import net.branchlock.structure.BResource;

import java.util.Map;

public interface IResourceProvider {
  Map<String, BResource> getResources();
}
