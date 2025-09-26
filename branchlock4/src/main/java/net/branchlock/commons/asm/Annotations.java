package net.branchlock.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;

public class Annotations {
  public static boolean has(String annotation, List<AnnotationNode>... annoLists) {
    if (annoLists == null) return false;
    for (List<? extends AnnotationNode> annos : annoLists) {
      if (annos == null) continue;
      if (annos.stream().anyMatch(ant -> {
        String internalName = Type.getType(ant.desc).getInternalName();
        return internalName.equals(annotation) || internalName.endsWith('/' + annotation) || ant.desc.endsWith('/' + annotation.replace('.', '$')); // if dot is used for inner class annotations
      })) return true;
    }
    return false;
  }
}
