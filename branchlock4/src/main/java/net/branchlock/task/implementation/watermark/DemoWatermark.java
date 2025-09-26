package net.branchlock.task.implementation.watermark;

import net.branchlock.commons.asm.Access;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.implementations.ClassDriver;
import net.branchlock.task.driver.implementations.MethodDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.metadata.TaskMetadata;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@TaskMetadata(name = "Demo watermark", priority = TaskMetadata.Level.FIRST, ids = "demo_watermark")
public class DemoWatermark extends Task {

  private static final String WATERMARK =
    "-----------------------------------------------------------------------------------------\n" +
      "| This java application has been obfuscated using a demo version of Branchlock 4.       |\n" +
      "| Did you know that anyone can read the source code of your exported java software?     |\n" +
      "| For more information about how to protect your projects, visit https://branchlock.net |\n" +
      "-----------------------------------------------------------------------------------------\n";
  private static final String EXPLOIT_NAME =
    "<html><init><img src=\"https://assets.branchlock.net/media/brand.jpg\"><br><br><h2>Premium protection " +
      "for an affordable price - <a href=\"http:\\\">branchlock.net</a><br>";

  public DemoWatermark(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    return List.of(
      new MethodDriver() {
        @Override
        public boolean drive(Stream<BMethod> stream) {
          stream.forEach(m -> {
            InsnList wm = new InsnList();
            wm.add(new LabelNode());
            wm.add(new LdcInsnNode(WATERMARK));
            wm.add(new InsnNode(DUP));
            wm.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
            wm.add(new InsnNode(SWAP));
            wm.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V"));
            wm.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I"));
            wm.add(Instructions.intPush(WATERMARK.hashCode()));
            LabelNode target = new LabelNode();
            wm.add(new JumpInsnNode(IF_ICMPEQ, target));
            wm.add(new VarInsnNode(ALOAD, 0)); //String[] args
            wm.add(new InsnNode(DUP));
            // not reachable for android obfuscation. (Monitors can cause problems in ART verifier)
            wm.add(new InsnNode(MONITORENTER));
            wm.add(new InsnNode(MONITORENTER));
            wm.add(target);
              m.instructions.insert(wm);
          });
          return true;
        }

        @Override
        public Collection<IPassThrough<BMethod>> passThroughs() {
          return List.of(t -> t.filter(BMethod::isMain));
        }
      },
      new ClassDriver() {
        @Override
        public boolean drive(Stream<BClass> stream) {
          stream.forEach(c -> {
            if (R.nextDouble() < 0.15) {
              c.addField(
                new BField(c, ACC_PUBLIC, "BRANCHLOCK_DOT_NET_DEMO", "Ljava/lang/String;", null,
                  "Obfuscated using a demo version of Branchlock."));
            } else {
              c.addField(new BField(c, ACC_PUBLIC, "BRANCHLOCK_DOT_NET_DEMO", "I", null, null));
            }
          });
          return true;
        }

        @Override
        public Collection<IPassThrough<BClass>> passThroughs() {
          return List.of(t -> t.filter(c -> {
            if (Access.isInterface(c.access)) return false;
            return !Access.isEnum(c.access);
          }));
        }
      }

    );
  }
}
