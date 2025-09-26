package net.branchlock.task.driver.passthrough;

import net.branchlock.structure.BMember;
import net.branchlock.structure.provider.DataProvider;

import java.util.stream.Stream;

public class MemberReflectionDetectionPassThrough<T extends BMember> extends ReflectionDetectionPassThrough<T> {

  public MemberReflectionDetectionPassThrough(DataProvider dataProvider) {
    super(dataProvider);
  }

  @Override
  public Stream<T> passThrough(Stream<T> t) {
    return t.filter(bMember -> !reflectionDetector.isClassMemberAffected(bMember));
  }
}
