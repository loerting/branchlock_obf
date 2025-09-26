package net.branchlock.task.implementation.references.strings.utils;

import net.branchlock.commons.asm.Instructions;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashSet;
import java.util.Set;

public class StringsToChars implements Opcodes {
  /**
   * Warning: Don't forget to increase maxLocals afterwards.
   */
  public static InsnList toChars(LdcInsnNode ldc, int maxLocals) {
    String value = (String) ldc.cst;
    InsnList il = new InsnList();
    il.add(new TypeInsnNode(NEW, "java/lang/String"));
    il.add(new InsnNode(DUP));
    il.add(Instructions.intPush(value.length()));
    il.add(new IntInsnNode(NEWARRAY, 5));
    // we are using a set to shuffle the insertion order, have fun decompiling that
    HashSet<InsnList> arrayStores = new HashSet<>();

    char[] chars = value.toCharArray();
    Set<Integer> characters = new HashSet<>();

    for (int i = 0; i < chars.length; i++)
      characters.add((int) chars[i]);

    for (int aChar : characters) {
      HashSet<Integer> indexes = new HashSet<>();

      for (int idx = 0; idx < chars.length; idx++)
        if (chars[idx] == aChar)
          indexes.add(idx);
      InsnList store = new InsnList();
      if (indexes.size() == 1) {
        store.add(new InsnNode(DUP));
        store.add(Instructions.intPush(indexes.iterator().next()));
        store.add(Instructions.intPush(aChar));
        store.add(new InsnNode(CASTORE));
      } else {
        store.add(Instructions.intPush(aChar));
        store.add(new VarInsnNode(ISTORE, maxLocals));

        for (int index : indexes) {
          store.add(new InsnNode(DUP));
          store.add(Instructions.intPush(index));
          store.add(new VarInsnNode(ILOAD, maxLocals));
          store.add(new InsnNode(CASTORE));
        }
      }
      arrayStores.add(store);
    }

    for (InsnList store : arrayStores) {
      il.add(store);
    }

    il.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V"));
    return il;
  }
}
