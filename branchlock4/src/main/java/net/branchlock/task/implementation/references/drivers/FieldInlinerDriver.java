package net.branchlock.task.implementation.references.drivers;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.Access;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMethod;
import net.branchlock.task.Task;
import net.branchlock.task.driver.implementations.SingleFieldDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class FieldInlinerDriver implements SingleFieldDriver {
  private final Task task;
  private final Predicate<FieldNode> fieldPredicate;
  private final AtomicInteger count = new AtomicInteger();

  public FieldInlinerDriver(Task task, Predicate<FieldNode> fieldPredicate) {
    this.task = task;
    this.fieldPredicate = fieldPredicate;
  }

  private static void inlineField(BClass c, FieldNode f) {
    if (!Access.isStatic(f.access)) {
      // JVM spec 4.7.2: If a field_info structure representing a non-static field has a ConstantValue attribute, then that attribute must silently be ignored.
      // In other words, we can remove the value attribute of non-static fields without inlining them.
      f.value = null;
      return;
    }
    BMethod staticInitializer = c.getOrMakeStaticInitializer();
    InsnList il = new InsnList();
    if (f.value instanceof Number) {
      il.add(Instructions.numberPush((Number) f.value));
      il.add(new FieldInsnNode(Opcodes.PUTSTATIC, c.getName(), f.name, f.desc));
    } else {
      il.add(new LdcInsnNode(f.value));
      il.add(new FieldInsnNode(Opcodes.PUTSTATIC, c.getName(), f.name, f.desc));
    }
      staticInitializer.instructions.insert(il);
    f.value = null; // clear value
  }

  @Override
  public Collection<IPassThrough<BField>> passThroughs() {
    return task.defaultMemberExclusionHandlersPlus(bf -> bf.filter(f -> !"serialVersionUID".equals(f.getName()) && fieldPredicate.test(f)));
  }

  @Override
  public String identifier() {
    return "static-field-inliner";
  }

  @Override
  public boolean driveEach(BField bf) {
    inlineField(bf.getOwner(), bf);
    count.incrementAndGet();
    return true;
  }

  @Override
  public void postDrive() {
    Branchlock.LOGGER.info("Inlined {} static fields.", count.get());
  }
}
