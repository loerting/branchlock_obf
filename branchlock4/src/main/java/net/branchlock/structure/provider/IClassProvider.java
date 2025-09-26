package net.branchlock.structure.provider;

import net.branchlock.structure.BClass;

import java.util.Map;

public interface IClassProvider {
  Map<String, BClass> getClasses();

}
