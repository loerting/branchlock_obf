/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.branchlock.task.naming;

import net.branchlock.Branchlock;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;

import java.util.Arrays;
import java.util.List;

public class BetterClassRemapper extends ClassRemapper {
  public BetterClassRemapper(ClassVisitor classVisitor, Remapper remapper) {
    super(classVisitor, remapper);
  }

  @Override
  protected MethodVisitor createMethodRemapper(MethodVisitor methodVisitor) {
    return new InvokeDynamicFixVisitor(methodVisitor, remapper);
  }

  private static class InvokeDynamicFixVisitor extends MethodRemapper {
    public InvokeDynamicFixVisitor(MethodVisitor methodVisitor, Remapper remapper) {
      super(methodVisitor, remapper);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
      // don't check for META_FACTORIES.contains(bsm). Scrambler for example changes the metafactory, but it should also be updated.
      if (bsmArgs != null && bsmArgs.length > 0 && bsmArgs[0] instanceof Type && desc.startsWith("(")) {
        try {
          String owner = Type.getReturnType(desc).getInternalName();
          String odesc = ((Type) bsmArgs[0]).getDescriptor(); // First constant argument is "samMethodType - Signature and return type of method to be implemented by the function object."
          name = remapper.mapMethodName(owner, name, odesc); // if it is a false detection, mapMethodName will just return name back and the idyn will remain unchanged.
        } catch (Exception e) {
          Branchlock.LOGGER.error("Invokedynamic name update exception. Could be malformed: {}", e.toString());
        }
      }
      super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }
  }
}
