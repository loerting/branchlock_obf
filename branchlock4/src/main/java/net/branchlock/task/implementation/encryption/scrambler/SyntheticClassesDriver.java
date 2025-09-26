package net.branchlock.task.implementation.encryption.scrambler;

import net.branchlock.structure.BClass;
import net.branchlock.task.driver.implementations.ClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import org.objectweb.asm.Opcodes;

import java.util.Collection;
import java.util.stream.Stream;

public class SyntheticClassesDriver implements ClassDriver, Opcodes {
  private final Scrambler scrambler;

  public SyntheticClassesDriver(Scrambler scrambler) {
    this.scrambler = scrambler;
  }

  @Override
  public boolean drive(Stream<BClass> stream) {
    stream.forEach(bc -> {
      bc.access |= ACC_SYNTHETIC;
      if (!bc.isInterface()) {
        bc.fields.forEach(f -> f.access |= ACC_SYNTHETIC | ACC_ENUM);
      }
      bc.methods.forEach(m -> {
        m.access |= ACC_SYNTHETIC;
      });
    });
    return true;
  }

  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    return scrambler.defaultClassExclusionHandlersPlus(st -> st.filter(bc -> !bc.isAnnotation()));
  }

  @Override
  public String identifier() {
    return "synthetic-classes";
  }
}
