package net.branchlock.task.implementation.salting.drivers;

import net.branchlock.structure.BMethod;
import net.branchlock.task.driver.implementations.SingleMethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.salting.Salting;
import org.objectweb.asm.Opcodes;

import java.util.Collection;
import java.util.List;

public class SaltingSignatureUpdaterDriver implements SingleMethodDriver, Opcodes {
  private final Salting salting;
  private final SaltingSignatureCollectorDriver saltingSignatureCollectorDriver;

  public SaltingSignatureUpdaterDriver(Salting salting, SaltingSignatureCollectorDriver saltingSignatureCollectorDriver) {
    this.salting = salting;
    this.saltingSignatureCollectorDriver = saltingSignatureCollectorDriver;
  }

  @Override
  public boolean driveEach(BMethod bm) {
    String newDescriptor = saltingSignatureCollectorDriver.newDescriptors.get(bm);
    if (newDescriptor == null) return true;

    if (bm.hasAccess(ACC_VARARGS))
      bm.removeAccess(ACC_VARARGS);

    bm.changeSignature(bm.getName(), newDescriptor); // this also updates equivalence class
    bm.signature = null;
    return true;
  }

  @Override
  public Collection<IPassThrough<BMethod>> passThroughs() {
    return List.of(f -> f.filter(BMethod::isLocal));
  }

  @Override
  public String identifier() {
    return "signature-remapper";
  }
}
