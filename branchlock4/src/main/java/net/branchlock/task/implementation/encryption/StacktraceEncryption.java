package net.branchlock.task.implementation.encryption;

import net.branchlock.commons.string.StringUtils;
import net.branchlock.config.Config;
import net.branchlock.config.SettingsManager;
import net.branchlock.cryptography.DESedeECB;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;
import net.branchlock.task.driver.implementations.MethodDriver;
import net.branchlock.task.driver.implementations.SingleClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.metadata.TaskMetadata;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@TaskMetadata(name = "Stacktrace encryption", priority = TaskMetadata.Level.FIRST, ids = {"encrypt-stacktrace", "stacktrace-encryption"}, demoApplicable = false)
public class StacktraceEncryption extends Task {
  private String key = innerConfig.getOrDefaultValue("key", "");
  private DESedeECB des;

  public StacktraceEncryption(SettingsManager settingsManager, Config innerConfig, DataProvider dataProvider) {
    super(settingsManager, innerConfig, dataProvider);
  }

  @Override
  public void preExecute() {
    if (key.trim().isEmpty()) {
      LOGGER.info("No key specified, generating a key.");
      key = StringUtils.generateAlphanumericString(16);
      LOGGER.info("Your decryption key is: {}.", key);
    }
    des = new DESedeECB(key);

    LOGGER.info("Encrypting source file constant and line numbers with the specified key!");
    LOGGER.warning("Some other task(s) may modify line numbers or source attributes, which could lead to an invalid decryption.");
  }

  @Override
  public List<IDriver<?>> getDrivers() {
    return List.of(new SingleClassDriver() {
      @Override
      public String identifier() {
        return "class-debug-encryption";
      }

      @Override
      public boolean driveEach(BClass c) {
        try {
          ClassNode node = c;
          if (node.sourceFile != null)
            node.sourceFile = des.encrypt(cutSourceType(node.sourceFile));
          if (node.sourceDebug != null)
            node.sourceDebug = des.encrypt(cutSourceType(node.sourceDebug));
          return true;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
          LOGGER.error("Failed to encrypt stacktrace element: {}", e.toString());
        }
        return false;
      }

      @Override
      public Collection<IPassThrough<BClass>> passThroughs() {
        return defaultClassExclusionHandlers();
      }
    }, new MethodDriver() {

      @Override
      public String identifier() {
        return "line-number-encryption";
      }

      @Override
      public boolean drive(Stream<BMethod> methods) {
        methods.forEach(m -> {
          StreamSupport.stream(m.instructions.spliterator(), false)
            .filter(i -> i.getType() == AbstractInsnNode.LINE).map(LineNumberNode.class::cast).forEach(l -> l.line = des.encryptLine(l.line));
        });
        return true;
      }

      @Override
      public Collection<IPassThrough<BMethod>> passThroughs() {
        return defaultMemberExclusionHandlers();
      }
    });
  }

  private String cutSourceType(String str) {
    return str.split("\\.")[0];
  }
}
