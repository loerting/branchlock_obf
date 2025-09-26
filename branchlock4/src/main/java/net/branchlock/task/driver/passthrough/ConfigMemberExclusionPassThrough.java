package net.branchlock.task.driver.passthrough;

import net.branchlock.config.Config;
import net.branchlock.structure.BMember;
import net.branchlock.structure.provider.DataProvider;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is not for usage with BClass, only BMethod and BField.
 *
 * @param <T> BMethod or BField
 */
public class ConfigMemberExclusionPassThrough<T extends BMember> extends ConfigExclusionPassThrough<T> {

  public ConfigMemberExclusionPassThrough(Config taskConfig, DataProvider dataProvider) {
    super(taskConfig, dataProvider);
  }

  @Override
  public Stream<T> passThrough(Stream<T> t) {
    Set<T> input = t.collect(Collectors.toSet());
    Set<T> excludedMember = input.stream().filter(bMeth -> matchesAnyToken(getExcludedTokens(), bMeth)).collect(Collectors.toSet());
    Set<T> includedMember = input.stream().filter(bMeth -> matchesAnyToken(getIncludedTokens(), bMeth)).collect(Collectors.toSet());
    warnIncludedNotInExcluded(excludedMember, includedMember);
    return input.stream().filter(bClass -> !excludedMember.contains(bClass) || includedMember.contains(bClass));
  }
}
