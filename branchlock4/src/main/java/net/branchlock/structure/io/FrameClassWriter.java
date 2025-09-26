package net.branchlock.structure.io;

import net.branchlock.commons.asm.Access;
import net.branchlock.structure.BClass;
import net.branchlock.structure.provider.DataProvider;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public class FrameClassWriter extends ClassWriter {

  private static final Type OBJECT_TYPE = Type.getType(Object.class);
  private final DataProvider dataProvider;

  public FrameClassWriter(DataProvider dataProvider, int flags) {
    super(flags);
    this.dataProvider = dataProvider;
  }

  @Override
  protected String getCommonSuperClass(String type1, String type2) {
    Type first = Type.getObjectType(type1);
    Type second = Type.getObjectType(type2);
    if (first.getSort() != Type.OBJECT || second.getSort() != Type.OBJECT)
      return OBJECT_TYPE.getInternalName();
    return findCommonParent(getClassByName(type1), getClassByName(type2)).name;
  }


  private ClassNode findCommonParent(ClassNode c1, ClassNode c2) {
    if (c1.name.equals(c2.name)) {
      return c1;
    }
    if (isAssignableFrom(c1, c2)) {
      return c1;
    }
    if (isAssignableFrom(c2, c1)) {
      return c2;
    }
    if (Access.isInterface(c1.access) || Access.isInterface(c2.access) || c1.superName == null ||
      c2.superName == null) {
      return getClassByName("java/lang/Object");
    } else {
      do {
        c1 = getClassByName(c1.superName);
      } while (c1.superName != null && !isAssignableFrom(c1, c2));
      return c1;
    }
  }

  private ClassNode getClassByName(String name) {
    BClass bClass = dataProvider.resolveBClass(name, null);
    if (bClass != null) {
      return bClass;
    }
    if ("java/lang/Object".equals(name)) {
      throw new Error("Could not find java/lang/Object");
    }
    return getClassByName("java/lang/Object");
  }

  private boolean isAssignableFrom(ClassNode cn, ClassNode cn2) {
    if (cn2.name.equals(cn.name)) {
      return true;
    }
    for (String itfn : cn2.interfaces) {
      ClassNode itf = getClassByName(itfn);
      if (itf == null)
        continue;
      if (isAssignableFrom(cn, itf)) {
        return true;
      }
    }
    if (cn2.superName != null) {
      ClassNode sn = getClassByName(cn2.superName);
      return sn != null && isAssignableFrom(cn, sn);
    }
    return false;
  }
}
