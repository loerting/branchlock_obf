package net.branchlock.task.implementation.naming.drivers;

import net.branchlock.Branchlock;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMember;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.driver.implementations.IndividualDriver;
import net.branchlock.task.implementation.naming.Renamer;

import java.util.*;
import java.util.stream.Stream;

public class MemberRemapperDriver implements IndividualDriver {

  private final Renamer renamer;
  private final RemappableClassCollectorDriver remappableClassCollectorDriver;
  private final RemappableMethodCollectorDriver remappableMethodCollectorDriver;
  private final RemappableFieldCollectorDriver remappableFieldCollectorDriver;


  public MemberRemapperDriver(Renamer renamer, RemappableClassCollectorDriver remappableClassCollectorDriver, RemappableMethodCollectorDriver remappableMethodCollectorDriver, RemappableFieldCollectorDriver remappableFieldCollectorDriver) {
    this.renamer = renamer;
    this.remappableClassCollectorDriver = remappableClassCollectorDriver;
    this.remappableMethodCollectorDriver = remappableMethodCollectorDriver;
    this.remappableFieldCollectorDriver = remappableFieldCollectorDriver;
  }

  @Override
  public boolean drive(Stream<Void> stream) {

    List<BClass> remappableClassNames = remappableClassCollectorDriver.remappableClassNames;
    List<BMethod> remappableMethods = remappableMethodCollectorDriver.remappableMethods;
    List<BField> remappableFieldNames = remappableFieldCollectorDriver.remappableFieldNames;

    Branchlock.LOGGER.info("Renaming {} classes, {} methods, {} fields.", remappableClassNames.size(), remappableMethods.size(), remappableFieldNames.size());

    List<BMember> combined = new ArrayList<>();
    combined.addAll(remappableMethods);
    combined.addAll(remappableFieldNames);

    Collections.shuffle(remappableClassNames, Branchlock.R);
    Collections.shuffle(combined, Branchlock.R);

    renamer.nameTransformer.transformMembers(remappableClassNames, combined, renamer.nullByteTrick, renamer.createPackages);
    return true;
  }

  @Override
  public String identifier() {
    return "member-remapper";
  }


}
