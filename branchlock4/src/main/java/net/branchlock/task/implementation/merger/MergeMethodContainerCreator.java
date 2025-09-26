package net.branchlock.task.implementation.merger;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Access;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMember;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;
import net.branchlock.task.driver.implementations.MethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.driver.passthrough.LambdaExclusionPassThrough;
import net.branchlock.task.driver.passthrough.MemberReflectionDetectionPassThrough;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.invoke.CallSite;
import java.util.*;
import java.util.stream.Stream;

public class MergeMethodContainerCreator implements MethodDriver, Opcodes {
  private final MethodMerger methodMerger;
  private final Type callSiteType = Type.getType(CallSite.class);
  public Map<Type, MergeMethodContainer> containers = new HashMap<>();

  public MergeMethodContainerCreator(MethodMerger methodMerger) {
    this.methodMerger = methodMerger;
  }

  @Override
  public boolean drive(Stream<BMethod> stream) {
    stream.forEach(bm -> {
      // we have to make sure method resolution works the same as before after merging.
      // method resolution depends on super classes, for static and virtual methods.
      // we have to therefore turn some methods public, as they may not resolve after being moved.

      Set<BMember> turnPublic = new HashSet<>();

      bm.getCode().iterateOverReferences(ref -> {
        if (ref.owner.startsWith("[") || ref.owner.endsWith(";")) return;
        BClass bClass = methodMerger.dataProvider.resolveBClass(ref.owner, bm);
        if (bClass == null) return;
        switch (ref.type) {
          case METHOD_INVOKE -> {
            BMethod bMethod = bClass.resolveMethod(ref.name, ref.desc);
            if (bMethod == null) return;
            IEquivalenceClass<BMethod> equivalenceClass = bMethod.requireEquivalenceClass();
            for (BMethod member : equivalenceClass.getMembers()) {
              if (Access.isPublic(member.getAccess()))
                continue;
              turnPublic.add(member);
            }
          }
          case FIELD_GET, FIELD_SET -> {
            BField field = bClass.resolveField(ref.name, ref.desc);
            if (field == null) return;
            if (Access.isPublic(field.getAccess())) return;
            turnPublic.add(field);
          }
          case CLASS_TYPE -> {
            if (Access.isPublic(bClass.getAccess())) return;
            turnPublic.add(bClass);
          }
        }
      });

      if (turnPublic.stream().anyMatch(bMember -> !bMember.isLocal())) {
        // if any method is not changeable, we can't continue.
        return;
      }

      for (BMember member : turnPublic) {
        member.setAccess(Access.publicAndGeneric(member.getAccess()));
      }

      Type genSigId = addInt(bm.getGeneralSignature());
      containers.computeIfAbsent(genSigId, k -> new MergeMethodContainer(genSigId));
      MergeMethodContainer container = containers.get(genSigId);
      container.addMethod(bm);
    });
    return true;
  }

  private Type addInt(Type methodDesc) {
    Type[] args = methodDesc.getArgumentTypes();
    Type[] newArgs = new Type[args.length + 1];
    System.arraycopy(args, 0, newArgs, 0, args.length);
    newArgs[args.length] = Type.INT_TYPE;
    return Type.getMethodType(methodDesc.getReturnType(), newArgs);
  }

  @Override
  public void postDrive() {
    // remove all containers with only one method, as it wouldn't make sense to create a merge method for it
    containers.values().removeIf(MergeMethodContainer::isSingleMethod);

    int count = 0;
    for (MergeMethodContainer container : containers.values()) {
      count += container.buckets.stream().mapToInt(MergeMethodBucket::getSize).sum();
    }
    Branchlock.LOGGER.info("{} methods will be merged into {} container(s).", count, containers.size());
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return methodMerger.defaultMemberExclusionHandlersPlus(t -> t.filter(bm -> bm.isSignatureChangeable() && bm.isStatic()
        && !Access.isSynchronized(bm.getAccess()) && !Access.isNative(bm.getAccess()) && !Access.isStrictfp(bm.getAccess())
        && hasEmptyAnnotations(bm)
        && !Type.getReturnType(bm.getDescriptor()).equals(callSiteType)),
      new MemberReflectionDetectionPassThrough<>(methodMerger.dataProvider), new LambdaExclusionPassThrough(methodMerger.dataProvider));
  }

  private boolean hasEmptyAnnotations(BMethod bm) {
    List<AnnotationNode> visibleAnnotations = bm.visibleAnnotations;
    if (visibleAnnotations == null) return true;
    for (AnnotationNode visibleAnnotation : visibleAnnotations) {
      BClass bClass = methodMerger.dataProvider.resolveBClass(Type.getType(visibleAnnotation.desc).getInternalName(), bm.getOwner());
      if (bClass != null && bClass.isLocal()) return false;
    }
    return true;
  }

  @Override
  public String identifier() {
    return "merge-method-container-creator";
  }
}
