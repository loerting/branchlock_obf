package net.branchlock.structure.io;

import net.branchlock.Branchlock;
import net.branchlock.structure.BMember;
import net.branchlock.structure.provider.DataProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MissingReferenceHandler {

  private static final float RATIO_THRESHOLD = 0.25f;
  private final DataProvider dataProvider;
  private final Map<String, MissingReference> missingReferences = new ConcurrentHashMap<>();

  public MissingReferenceHandler(DataProvider dataProvider) {
    this.dataProvider = dataProvider;
  }

  public void printMissingLibTree() {
    // convert it into a tree structure
    TreeElement root = new TreeElement("root", 0);
    for (MissingReference missingRef : missingReferences.values()) {
      String missingName = missingRef.getClassName();
      int referencedFromCount = missingRef.getReferencedFrom().size();

      TreeElement current = root;
      String[] structure = missingName.split("\\.");
        for (int i = 0; i < structure.length; i++) {
            String pckg = structure[i];
            if (pckg.isEmpty())
                continue;
            current.childs.computeIfAbsent(pckg, name -> new TreeElement(name, referencedFromCount));
            current = current.childs.get(pckg);
        }
    }

    combineSingleChildren(root);

    for (TreeElement pckg : root.childs.values()) {
      printTreeRec("", pckg, false);
    }
  }

  public void combineSingleChildren(TreeElement pckg) {
    if (pckg.childs.size() == 1) {
      TreeElement child = pckg.childs.values().iterator().next();
      pckg.name += "." + child.name;
      pckg.childs.clear();
      pckg.childs.putAll(child.childs);
      combineSingleChildren(pckg);
    }
    for (TreeElement child : pckg.childs.values()) {
      combineSingleChildren(child);
    }
  }

  /**
   * Print a tree structure in a pretty ASCII format.
   *
   * @param prefix Current prefix. Use "" in initial call!
   * @param node   The current node. Pass the root node of your tree in initial call.
   * @param isTail Is node the last of its siblings. Use true in initial call. (This is needed for pretty printing.)
   */
  private void printTreeRec(String prefix, TreeElement node, boolean isTail) {
    String nodeName = node.toString();
    List<TreeElement> sortedChildren = new ArrayList<>(node.childs.values());
    sortedChildren.sort(Comparator.comparing(TreeElement::toString));

    int totalChilds = node.totalChilds();
    int totalReferences = node.totalReferencedFrom();
    boolean leaf = totalChilds == 0;

    String nodeConnection = leaf ? "|== " : "|-- ";

    if (sortedChildren.size() > 1 && (totalChilds > 2 && totalChilds < missingReferences.size() * RATIO_THRESHOLD)) {
      Branchlock.LOGGER.info(prefix + nodeConnection + nodeName + " [" + totalReferences + "]");
      return;
    }
    Branchlock.LOGGER.info(prefix + nodeConnection + nodeName + (leaf ? " [" + totalReferences + "]" : ""));
    for (int i = 0; i < sortedChildren.size(); i++) {
      String newPrefix = prefix + (isTail ? "    " : "|   ");
      printTreeRec(newPrefix, sortedChildren.get(i), i == sortedChildren.size() - 1);
    }
  }

  public void observeMissingReference(String className, BMember accessedFrom) {
    if ("null".equals(className)) return;
    String formatted = className.replace("/", ".");
    missingReferences.computeIfAbsent(formatted, MissingReference::new).getReferencedFrom().add(accessedFrom);
  }

  public void logMissingReferences() {
    if (missingReferences.isEmpty()) {
      Branchlock.LOGGER.info("No missing references detected.");
      return;
    }
    int totalReferences = missingReferences.values().stream().mapToInt(m -> m.getReferencedFrom().size()).sum();

    Branchlock.LOGGER.warning("{} unknown classes referenced {} times:", missingReferences.size(), totalReferences);
    printMissingLibTree();
    Branchlock.LOGGER.info("For a correct obfuscation minimize the amount of unknown classes.");
  }

  public static class TreeElement {
    public final Map<String, TreeElement> childs = new LinkedHashMap<>();
    public String name;
    private final int referencedFromCount;

    public TreeElement(String name, int referencedFromCount) {
      this.name = name;
      this.referencedFromCount = referencedFromCount;
    }

    @Override
    public String toString() {
      return name;
    }

    public boolean isLeaf() {
      return childs.isEmpty();
    }

    public int totalChilds() {
      return childs.size() + childs.values().stream().mapToInt(TreeElement::totalChilds).sum();
    }

    public int totalReferencedFrom() {
      return referencedFromCount + childs.values().stream().mapToInt(TreeElement::totalReferencedFrom).sum();
    }
  }

}
