package net.branchlock.commons.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * (C) Copyright 2013 Faur Ioan-Aurel.
 * <p/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * Contributors:
 * faur
 * <p/>
 * Created at:
 * 5:06 PM       5/14/13
 */

/**
 * Instructions for boxing and unboxing the primitives.
 */
public class Boxing {
  public static String getBoxClassName(Type type) {
    String t = type.getDescriptor();
    if (t.length() > 1) {
      return null;
    } else if ("Z".equals(t)) {
      return "java/lang/Boolean";
    } else if ("B".equals(t)) {
      return "java/lang/Byte";
    } else if ("C".equals(t)) {
      return "java/lang/Character";
    } else if ("S".equals(t)) {
      return "java/lang/Short";
    } else if ("I".equals(t)) {
      return "java/lang/Integer";
    } else if ("J".equals(t)) {
      return "java/lang/Long";
    } else if ("F".equals(t)) {
      return "java/lang/Float";
    } else if ("D".equals(t)) {
      return "java/lang/Double";
    }
    return null;
  }

  public static String getUnBoxInvoke(Type type) {
    String t = type.getDescriptor();
    if (t.length() > 1) {
      return null;
    } else if ("Z".equals(t)) {
      return "java/lang/Boolean.booleanValue";
    } else if ("B".equals(t)) {
      return "java/lang/Number.byteVale";
    } else if ("C".equals(t)) {
      return "java/lang/Character.charValue";
    } else if ("S".equals(t)) {
      return "java/lang/Number.shortValue";
    } else if ("I".equals(t)) {
      return "java/lang/Number.intValue";
    } else if ("J".equals(t)) {
      return "java/lang/Number.longValue";
    } else if ("F".equals(t)) {
      return "java/lang/Number.floatValue";
    } else if ("D".equals(t)) {
      return "java/lang/Number.doubleValue";
    }
    return null;
  }

  public static InsnList box(char type) {
    switch (type) {
      case 'Z':
        return boxBoolean();
      case 'B':
        return boxByte();
      case 'C':
        return boxChar();
      case 'S':
        return boxShort();
      case 'I':
        return boxInt();
      case 'J':
        return boxLong();
      case 'F':
        return boxFloat();
      case 'D':
        return boxDouble();
      default:
        return null;
    }
  }

  public static InsnList box(Type type) {
    String t = type.getDescriptor();
    if (t.length() > 1) {
      return null;
    } else if ("Z".equals(t)) {
      return box('Z');
    } else if ("B".equals(t)) {
      return box('B');
    } else if ("C".equals(t)) {
      return box('C');
    } else if ("S".equals(t)) {
      return box('S');
    } else if ("I".equals(t)) {
      return box('I');
    } else if ("J".equals(t)) {
      return box('J');
    } else if ("F".equals(t)) {
      return box('F');
    } else if ("D".equals(t)) {
      return box('D');
    }
    return null;
  }


  public static InsnList unbox(char type) {
    switch (type) {
      case 'Z':
        return unboxBoolean();
      case 'B':
        return unboxByte();
      case 'C':
        return unboxChar();
      case 'S':
        return unboxShort();
      case 'I':
        return unboxInt();
      case 'J':
        return unboxLong();
      case 'F':
        return unboxFloat();
      case 'D':
        return unboxDouble();
      default:
        return null;
    }
  }

  public static InsnList unbox(Type type) {
    String t = type.getDescriptor();
    if (t.length() > 1) {
      return null;
    } else if ("Z".equals(t)) {
      return unbox('Z');
    } else if ("B".equals(t)) {
      return unbox('B');
    } else if ("C".equals(t)) {
      return unbox('C');
    } else if ("S".equals(t)) {
      return unbox('S');
    } else if ("I".equals(t)) {
      return unbox('I');
    } else if ("J".equals(t)) {
      return unbox('J');
    } else if ("F".equals(t)) {
      return unbox('F');
    } else if ("D".equals(t)) {
      return unbox('D');
    }
    return null;
  }

  public static boolean isPrimitive(String type) {
    if (type.length() > 1) {
      return false;
    } else if ("Z".equals(type)) {
      return true;
    } else if ("B".equals(type)) {
      return true;
    } else if ("C".equals(type)) {
      return true;
    } else if ("S".equals(type)) {
      return true;
    } else if ("I".equals(type)) {
      return true;
    } else if ("J".equals(type)) {
      return true;
    } else if ("F".equals(type)) {
      return true;
    } else return "D".equals(type);
  }

  private static InsnList boxBoolean() {
    InsnList il = new InsnList();
    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;"));
    return il;
  }

  private static InsnList unboxBoolean() {
    InsnList il = new InsnList();
    il.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Boolean"));
    il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z"));
    return il;
  }

  private static InsnList boxByte() {
    InsnList il = new InsnList();
    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;"));
    return il;
  }

  private static InsnList unboxByte() {
    InsnList il = new InsnList();
    il.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Number"));
    il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "byteValue", "()B"));
    return il;
  }

  private static InsnList boxChar() {
    InsnList il = new InsnList();
    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;"));
    return il;
  }

  private static InsnList unboxChar() {
    InsnList il = new InsnList();
    il.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Character"));
    il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C"));
    return il;
  }

  private static InsnList boxShort() {
    InsnList il = new InsnList();
    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;"));
    return il;
  }

  private static InsnList unboxShort() {
    InsnList il = new InsnList();
    il.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Number"));
    il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "shortValue", "()S"));
    return il;
  }

  private static InsnList boxInt() {
    InsnList il = new InsnList();
    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"));
    return il;
  }

  private static InsnList unboxInt() {
    InsnList il = new InsnList();
    il.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Number"));
    il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I"));
    return il;
  }

  private static InsnList boxLong() {
    InsnList il = new InsnList();
    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;"));
    return il;
  }

  private static InsnList unboxLong() {
    InsnList il = new InsnList();
    il.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Number"));
    il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J"));
    return il;
  }

  private static InsnList boxFloat() {
    InsnList il = new InsnList();
    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;"));
    return il;
  }

  private static InsnList unboxFloat() {
    InsnList il = new InsnList();
    il.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Number"));
    il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "floatValue", "()F"));
    return il;
  }

  private static InsnList boxDouble() {
    InsnList il = new InsnList();
    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;"));
    return il;
  }

  private static InsnList unboxDouble() {
    InsnList il = new InsnList();
    il.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Number"));
    il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "doubleValue", "()D"));
    return il;
  }
}