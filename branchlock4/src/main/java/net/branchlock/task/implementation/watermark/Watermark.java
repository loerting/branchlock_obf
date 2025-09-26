package net.branchlock.task.implementation.watermark;

import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.implementations.IndividualDriver;
import net.branchlock.task.metadata.TaskMetadata;

import java.util.List;
import java.util.stream.Stream;

@TaskMetadata(name = "Watermark", priority = TaskMetadata.Level.EIGHTH, ids = "watermark")
public class Watermark extends Task implements IndividualDriver {

  private static final String WATERMARK_ASCII_ART = """
                           *                                 \s
                        *###.                                \s
                     *#######(                               \s
                  *#################                         \s
             *##    .##################                      \s
           (#######    .####. .*#########                    \s
          ########,       .#.     .#######.                  \s
         #######,                   #######.                 \s
         ######,                     #######.                \s
        (######.                     #######.                \s
         ######,                     #######.                \s
         #######.                   #######,                 \s
          ########                (#######,       ##         \s
                               ##########.        #####      \s
     #################################/.   ###############   \s
     ##############################*.   *##################,.\s
     ((((((((((((((((((((((/,,,..    /((((((((((((######,.   \s
                                                  ###,.      \s
                                                   .         \s
                                                             \s
    This application has been obfuscated using branchlock.net\s""";

  public Watermark(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    return List.of(this);
  }

  @Override
  public boolean drive(Stream<Void> stream) {
    nameFactory.resetNameIterator();

    BClass watermarkClass = new BClass(dataProvider);
    watermarkClass.access = ACC_PUBLIC;
    watermarkClass.version = settingsManager.getTargetVersion();
    watermarkClass.name = "Branchlock_" + nameFactory.getNameIteratorNext();
    watermarkClass.superName = "java/lang/Object";
    for (String line : WATERMARK_ASCII_ART.split("\n")) {
      BField fn = new BField(watermarkClass, ACC_PUBLIC | ACC_STATIC | ACC_FINAL, nameFactory.getNameIteratorNext(), "Ljava/lang/String;", null, line);
      watermarkClass.addField(fn);
    }

    dataProvider.addClass(watermarkClass);
    dataUtilities.addNoSideEffectClass(watermarkClass); // make sure it is actually used.
    return true;
  }
}
