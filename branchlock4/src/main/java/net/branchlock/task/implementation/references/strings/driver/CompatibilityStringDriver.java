package net.branchlock.task.implementation.references.strings.driver;

import net.branchlock.Branchlock;
import net.branchlock.commons.asm.ASMLimits;
import net.branchlock.commons.asm.Conversion;
import net.branchlock.commons.asm.Instructions;
import net.branchlock.commons.java.Pair;
import net.branchlock.commons.string.StringUtils;
import net.branchlock.layout.strings.CompatibilityStringEncryption;
import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.BMethodCode;
import net.branchlock.task.driver.implementations.ClassDriver;
import net.branchlock.task.driver.passthrough.IPassThrough;
import net.branchlock.task.implementation.flow.ControlFlow;
import net.branchlock.task.implementation.flow.passes.FlatteningPass;
import net.branchlock.task.implementation.references.numbers.Numbers;
import net.branchlock.task.implementation.references.strings.Strings;
import net.branchlock.task.implementation.salting.MethodSalt;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompatibilityStringDriver implements ClassDriver, Opcodes {
  private static final String DECRYPTION_ARRAY_NAME = "DECRYPTED_RESULT";
  private static final String DECRYPTION_ARRAY_DESC = Type.getType(String[].class).getDescriptor();
  private static final Random R = Branchlock.R;
  private final Strings task;

  private final BMethod stringDecryptionModel;

  public CompatibilityStringDriver(Strings task) {
    this.task = task;

    BClass encryptionMethodOwner = Conversion.loadProgramClass(task.dataProvider, CompatibilityStringEncryption.class);
    stringDecryptionModel = encryptionMethodOwner.methods.find("initializer", null);
  }

  private static void tangleTryCatchBlock(BMethod proxy, HashMap<Integer, LabelNode> switchXORLabels) {
    // let's make it very hard to decompile by tangling the code up a bit.
    TryCatchBlockNode outOfBoundsTcb = Objects.requireNonNull(proxy.tryCatchBlocks.get(0));
    outOfBoundsTcb.type = null;
    outOfBoundsTcb.end = switchXORLabels.values().iterator().next();
    LabelNode newHandler = new LabelNode();
      proxy.instructions.add(newHandler);
      proxy.instructions.add(new JumpInsnNode(GOTO, outOfBoundsTcb.handler));
    outOfBoundsTcb.handler = newHandler;
  }

  private static List<Pair<Integer, Integer>> generateInputSwaps(int len) {
    List<Pair<Integer, Integer>> swaps = new ArrayList<>();
    swaps.add(new Pair<>(R.nextInt(len), 0));
    if (len > 10) {
      for (int i = 0; i < 2; i++) {
        int a = R.nextInt(len);
        int b = R.nextInt(len);
        if (a == b) continue;
        swaps.add(new Pair<>(a, b));
      }
    }
    return swaps;
  }

  @Override
  public boolean drive(Stream<BClass> stream) {
    stream.forEach(c -> {
      List<String> stringsToEncrypt = c.methods.stream()
        .map(BMethod::getCode)
        .flatMap(BMethodCode::streamLdcValues)
        .filter(o -> o instanceof String)
        .map(o -> (String) o)
        .filter(task::canEncrypt)
        .distinct().collect(Collectors.toList());

      if (stringsToEncrypt.isEmpty()) return;

      Collections.shuffle(stringsToEncrypt, Branchlock.R);

      for (BMethod method : c.methods) {
          for (AbstractInsnNode ain : method.instructions.toArray()) {
          if (ain.getType() == AbstractInsnNode.LDC_INSN) {
            LdcInsnNode ldc = (LdcInsnNode) ain;
            if (!(ldc.cst instanceof String)) continue;
            int index = stringsToEncrypt.indexOf(ldc.cst);
            if (index != -1) {
              replaceWithArrayLoad(method, ldc, index);
            }
          }
        }
      }

      placeDecryptionCode(c, stringsToEncrypt);
    });

    return true;
  }

  private void replaceWithArrayLoad(BMethod method, LdcInsnNode ldc, int index) {
    InsnList il = new InsnList();
    il.add(new FieldInsnNode(GETSTATIC, method.getOwner().getName(), DECRYPTION_ARRAY_NAME, DECRYPTION_ARRAY_DESC));
    MethodSalt salt = method.getSalt();
    if (method.getInstructionCount() > ASMLimits.MAX_METHOD_SIZE * 0.5f) {
      il.add(Instructions.intPush(index));
    } else if (salt != null) {
      il.add(salt.loadEncryptedInt(index));
    } else {
      il.add(Numbers.generateCalculation(index, Numbers.NumbersStrength.WEAK));
    }
    il.add(new InsnNode(AALOAD));

      method.instructions.insert(ldc, il);
      method.instructions.remove(ldc);
  }

  private void placeDecryptionCode(BClass bc, List<String> strings) {
    BMethod proxy = stringDecryptionModel.duplicateMethod();
    BMethodCode code = proxy.getCode();

    int xor1 = R.nextInt(256);
    int xor2 = R.nextInt(256);
    int xor3 = R.nextInt(256);

    code.replacePlaceholder("RANDOM_XOR", Numbers.generateCalculation(xor1, Numbers.NumbersStrength.STRONG));
    code.replacePlaceholder("RANDOM_XOR_2", Numbers.generateCalculation(xor2, Numbers.NumbersStrength.STRONG));
    code.replacePlaceholder("RANDOM_XOR_3", Numbers.generateCalculation(xor3, Numbers.NumbersStrength.STRONG));
    // TODO salt pools. check bl3 for implementation.

    LookupSwitchInsnNode switchNode = code.findByOpcode(LOOKUPSWITCH);
    if (switchNode == null) throw new IllegalStateException("Could not find switch node in string decryption method");

    switchNode.keys.clear();
    switchNode.labels.clear();

    HashMap<Integer, Integer> charXORSwitchTable = new HashMap<>();
    // we don't want more than 4 switch cases, so only use 5 different xor values.
    byte[] xorValuesSwitch = new byte[4];
    R.nextBytes(xorValuesSwitch);
    char[] key = bc.getRuntimeName().toCharArray();
    for (char ch : key) {
      ch ^= xor1;
      charXORSwitchTable.put((int) ch, (int) xorValuesSwitch[R.nextInt(xorValuesSwitch.length)]);
    }

    LocalVariableNode currentChar = proxy.getLocalVariable("CURRENT_CHAR");

    HashMap<Integer, LabelNode> switchXORLabels = new HashMap<>();

    InsnList il = new InsnList();
    charXORSwitchTable.forEach((ch, xor) -> {
      LabelNode labelNode = new LabelNode();
      il.add(labelNode);
      if (switchXORLabels.containsKey(xor)) {
        // too many switch cases can cause too much file size
        il.add(new JumpInsnNode(GOTO, switchXORLabels.get(xor)));
      } else {
        il.add(new VarInsnNode(ILOAD, currentChar.index));
        il.add(Numbers.generateCalculation(xor, Numbers.NumbersStrength.WEAK));
        il.add(new InsnNode(IXOR));
        il.add(new VarInsnNode(ISTORE, currentChar.index));
        il.add(new JumpInsnNode(GOTO, switchNode.dflt));
      }
      switchNode.keys.add(ch);
      switchNode.labels.add(labelNode);
      switchXORLabels.put(xor, labelNode);
    });
    code.getInstructions().add(il);
    Instructions.orderSwitch(switchNode);

    int debugCheck = "<clinit>".hashCode() & 0xffff;
    StringBuilder encrypted = new StringBuilder();
    encrypted.append((char) (strings.size() ^ xor3 ^ debugCheck));
    for (String s : strings) {
      encrypted.append((char) (s.length() ^ xor2 ^ debugCheck));
      char[] chars = s.toCharArray();
      for (char aChar : chars) {
        int totalIndex = encrypted.length();
        aChar ^= charXORSwitchTable.get((int) key[totalIndex % key.length] ^ xor1); // equivalent to switch
        encrypted.append(aChar);
      }
    }
    String strInput = encrypted.toString();
    int inputLen = strInput.length();

    List<Pair<Integer, Integer>> swaps = generateInputSwaps(inputLen);

    LocalVariableNode arrayToSwap = proxy.getLocalVariable("COMPRESSED_ARRAY");
    InsnList swapIl = new InsnList();
    for (Pair<Integer, Integer> swap : swaps) {
      strInput = StringUtils.swap(strInput, swap.a, swap.b);
      // swaps in code have to be in inverse order, therefore insert at the beginning.
      swapIl.insert(swapArrayValuesCode(arrayToSwap.index, swap.a, swap.b));
    }
    // intentional out-of-bounds swap.
    swapIl.add(swapArrayValuesCode(arrayToSwap.index, R.nextInt(inputLen), inputLen + R.nextInt(inputLen)));
    if (R.nextBoolean()) swapIl.add(swapArrayValuesCode(arrayToSwap.index, R.nextInt(inputLen), R.nextInt(inputLen)));

    swapIl.add(Instructions.intPush(0));
    proxy.maxStack = Math.max(proxy.maxStack, 7);
    code.replacePlaceholder("STR_INPUT", strInput);
    code.replacePlaceholder("SWAP_PLACEHOLDER", swapIl);

    tangleTryCatchBlock(proxy, switchXORLabels);
    new FlatteningPass(task, ControlFlow.MAX_COVERAGE).drive(Stream.of(proxy));

    BField decryptionResult = new BField(bc, (bc.isInterface() ? ACC_PUBLIC : ACC_PRIVATE) | ACC_STATIC | ACC_FINAL, DECRYPTION_ARRAY_NAME, DECRYPTION_ARRAY_DESC, null, null);
    for (AbstractInsnNode ain : code.getInstructions()) {
      if (ain.getType() == AbstractInsnNode.FIELD_INSN) {
        FieldInsnNode fin = (FieldInsnNode) ain;
        if (fin.name.equals(DECRYPTION_ARRAY_NAME) && fin.desc.equals(DECRYPTION_ARRAY_DESC)) {
          fin.owner = bc.getName();
        }
      }
    }
    bc.addField(decryptionResult);

    BMethod staticInitializer = bc.getOrMakeStaticInitializer();
    staticInitializer.injectMethod(proxy);

    bc = task.nameTransformer.transformFieldsInsideClass(bc, decryptionResult);
  }

  private InsnList swapArrayValuesCode(int arrayIndex, int idx1, int idx2) {
    InsnList il = new InsnList();

    il.add(new VarInsnNode(ALOAD, arrayIndex));
    il.add(new InsnNode(DUP));
    il.add(Instructions.intPush(idx1));
    il.add(new InsnNode(SWAP));
    il.add(Numbers.generateCalculation(idx2, Numbers.NumbersStrength.WEAK));
    il.add(new InsnNode(CALOAD));
    il.add(new VarInsnNode(ALOAD, arrayIndex));
    il.add(new InsnNode(DUP));
    il.add(Numbers.generateCalculation(idx2, Numbers.NumbersStrength.WEAK));
    il.add(new InsnNode(SWAP));
    il.add(Instructions.intPush(idx1));
    il.add(new InsnNode(CALOAD));
    il.add(new InsnNode(CASTORE));
    il.add(new InsnNode(CASTORE));

    return il;
  }


  @Override
  public Collection<IPassThrough<BClass>> passThroughs() {
    return task.defaultClassExclusionHandlers();
  }

  @Override
  public String identifier() {
    return "compatibility-string-encryption";
  }
}
