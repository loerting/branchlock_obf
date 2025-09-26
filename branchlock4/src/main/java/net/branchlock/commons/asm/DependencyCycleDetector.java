package net.branchlock.commons.asm;

import net.branchlock.structure.BClass;
import net.branchlock.structure.provider.IDataProvider;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Dependency cycle detection using Tarjan's SCC algorithm.
 * This also deals with the problem that not all classes are known at the start of the analysis.
 */
public class DependencyCycleDetector {
  private final Stack<BClass> stack;
  private final Map<BClass, TarjanProperties> tarjanProperties;
  private final IDataProvider dataProvider;
  private final List<DependencyCycle> cycles = new ArrayList<>();
  private int index = 0;

  public DependencyCycleDetector(IDataProvider dataProvider) {
    this.dataProvider = dataProvider;
    // we use a stack to keep track of the current path
    stack = new Stack<>();
    tarjanProperties = new HashMap<>();
  }

  /**
   * Detect all dependency cycles for all currently loaded input and lib classes, and then return all cycles.
   * Single classes are not considered cycles, unless they are a parent of themselves.
   */
  public List<DependencyCycle> detect() {
    if (index != 0) {
      throw new IllegalStateException("Cannot call detect() twice");
    }

    // first, transform all classes into a graph:
    // - each BClass is a node in the graph
    // - each BClass is one-way connected to its parent classes, which can be retrieved using BClass#getDirectParentClasses().

    // for cycle detection we use Tarjan's Strongly Connected Components algorithm.

    // for each node, if it has not been visited yet, visit it
    for (BClass bClass : dataProvider.getClasses().values()) {
      if (!getOrMakeProperties(bClass).visited) {
        visit(bClass);
      }
    }
    // same for libs
    for (BClass bClass : dataProvider.getLibs().values()) {
      if (!getOrMakeProperties(bClass).visited) {
        visit(bClass);
      }
    }

    // make sure all strongly connected components of size 1 are removed, unless they are a loop. (parent class is itself)
    return cycles.stream()
      .filter(cycle -> cycle.classes.size() > 1 ||
        cycle.classes.get(0).getDirectParentClasses().contains(cycle.classes.get(0)))
      .collect(Collectors.toList());
  }

  private TarjanProperties getOrMakeProperties(BClass bClass) {
    return tarjanProperties.computeIfAbsent(bClass, k -> new TarjanProperties());
  }

  private void visit(BClass bClass) {
    TarjanProperties properties = getOrMakeProperties(bClass);
    properties.index = index;
    properties.lowlink = index;
    index++;

    stack.push(bClass);
    properties.onStack = true;
    properties.visited = true;

    // for each node, visit all its children
    for (BClass child : bClass.getDirectParentClasses()) {
      TarjanProperties childProperties = getOrMakeProperties(child);
      if (!childProperties.visited) {
        visit(child);
        properties.lowlink = Math.min(properties.lowlink, childProperties.lowlink);
      } else if (childProperties.onStack) {
        properties.lowlink = Math.min(properties.lowlink, childProperties.index);
      }
    }

    // if the node is a root node, pop the stack and generate a cycle
    if (properties.lowlink == properties.index) {
      BClass node;
      List<BClass> cycle = new ArrayList<>();
      do {
        node = stack.pop();
        cycle.add(node);
        getOrMakeProperties(node).onStack = false;
      } while (node != bClass);
      cycles.add(new DependencyCycle(cycle));
    }
  }


  public static class DependencyCycle {
    private final List<BClass> classes;

    public DependencyCycle(List<BClass> classes) {
      this.classes = classes;
    }

    @Override
    public String toString() {
      if (classes.size() == 1) {
        return classes.get(0).getName() + " -> " + classes.get(0).getName();
      }
      return classes.stream().map(BClass::getName).collect(Collectors.joining(" -> "));
    }
  }

  private class TarjanProperties {
    public int index = -1;
    public int lowlink = -1;
    public boolean visited;
    public boolean onStack;
  }
}
