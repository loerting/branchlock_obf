package net.branchlock.task.implementation.references.numbers;

import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.implementation.references.drivers.FieldInlinerDriver;
import net.branchlock.task.implementation.references.numbers.drivers.FloatingPointConverterDriver;
import net.branchlock.task.implementation.references.numbers.drivers.NumberToCalculationDriver;
import net.branchlock.task.implementation.references.numbers.drivers.SwitchOperandExtractorDriver;
import net.branchlock.task.implementation.references.numbers.term.BiTerm;
import net.branchlock.task.implementation.references.numbers.term.NumTerm;
import net.branchlock.task.metadata.TaskMetadata;
import org.objectweb.asm.tree.InsnList;

import java.util.List;

@TaskMetadata(name = "Number encryption", priority = TaskMetadata.Level.FOURTH, performanceCost = TaskMetadata.PerformanceCost.MINIMAL, ids = "numbers")
public class Numbers extends Task {
  public Numbers(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  public static InsnList generateCalculation(Number n, NumbersStrength strength) {
    if (!(n instanceof Integer || n instanceof Long))
      throw new IllegalArgumentException("Number must be an integer or long");

    NumTerm tn = new NumTerm(n);
    BiTerm obf = tn.obfuscate();
    if (strength.ordinal() >= NumbersStrength.MEDIUM.ordinal())
      obf.obfuscateRecursive(0);
    return obf.getTerm(strength == NumbersStrength.STRONG);
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    return List.of(new FieldInlinerDriver(this, f -> (f.value instanceof Number)),
      new SwitchOperandExtractorDriver(this),
      // removed extraction of common constants in method into variables.
      new FloatingPointConverterDriver(this),
      new NumberToCalculationDriver(this)
    );
  }

  public enum NumbersStrength {
    WEAK, MEDIUM, STRONG
  }

}
