package net.branchlock.task.driver.passthrough;

import net.branchlock.config.Config;
import net.branchlock.structure.BClass;
import net.branchlock.structure.provider.DataProvider;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigClassExclusionPassThrough extends ConfigExclusionPassThrough<BClass> {

  public ConfigClassExclusionPassThrough(Config taskConfig, DataProvider dataProvider) {
    super(taskConfig, dataProvider);
  }

  @Override
  public Stream<BClass> passThrough(Stream<BClass> t) {
    List<String> excludedClassTokens = getExcludedTokens().stream().filter(s -> !isMemberToken(s)).collect(Collectors.toList());
    List<String> includedClassTokens = getIncludedTokens().stream().filter(s -> !isMemberToken(s)).collect(Collectors.toList());

    Set<BClass> input = t.collect(Collectors.toSet());

    Set<BClass> excludedClasses = input.stream().filter(bClass -> matchesAnyToken(excludedClassTokens, bClass)).collect(Collectors.toSet());
    Set<BClass> includedClasses = input.stream().filter(bClass -> matchesAnyToken(includedClassTokens, bClass)).collect(Collectors.toSet());
    warnIncludedNotInExcluded(excludedClasses, includedClasses);


    return input.stream().filter(bClass -> !excludedClasses.contains(bClass) || includedClasses.contains(bClass))
      .filter(bClass -> !dataProvider.getLibs().containsKey(bClass.getOriginalName()));
  }

}
