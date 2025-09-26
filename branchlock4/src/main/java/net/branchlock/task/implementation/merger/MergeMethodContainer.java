package net.branchlock.task.implementation.merger;

import net.branchlock.commons.java.Pair;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MergeMethodContainer {
  public Type generalSignaturePlusId;

  public List<MergeMethodBucket> buckets = new ArrayList<>();

  public MergeMethodContainer(Type generalSignaturePlusId) {
    this.generalSignaturePlusId = generalSignaturePlusId;
  }

  public void addMethod(BMethod method) {
    if (buckets.isEmpty() || getLastBucket().isFull()) {
      buckets.add(new MergeMethodBucket());
    }
    getLastBucket().containedMethods.add(method);
  }

  private MergeMethodBucket getLastBucket() {
    return buckets.get(buckets.size() - 1);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MergeMethodContainer that = (MergeMethodContainer) o;
    return Objects.equals(generalSignaturePlusId, that.generalSignaturePlusId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(generalSignaturePlusId);
  }

  public void removeMethodsFromInput() {
    buckets.forEach(bucket -> bucket.containedMethods.forEach(bm -> bm.getOwner().methods.remove(bm)));
  }

  public void createMethods(MethodMerger methodMerger, Map<BMethod, Pair<BMethod, Integer>> methodMap, BClass container) {
    buckets.forEach(bucket -> bucket.createMethod(methodMerger, methodMap, generalSignaturePlusId, container));
  }

  public boolean isSingleMethod() {
    return buckets.size() == 1 && buckets.get(0).containedMethods.size() == 1;
  }
}
