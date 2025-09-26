package net.branchlock.structure.equivalenceclass;

public class EquivalentMethodDefinitionFinderTest {
  /*
      Set<BMethod> analyzedMethods = new HashSet<>();
    for (BClass value : getClasses().values()) {
      for (BMethod method : value.methods) {
        if(method.isStatic())
          continue;
        if (analyzedMethods.contains(method))
          continue;
        analyzedMethods.add(method);
        EquivalentMethodDefinitionFinder equivalentMethodDefinitionFinder = new EquivalentMethodDefinitionFinder(method);
        EquivalentMethodDefinitionFinder.ScanState scanState = equivalentMethodDefinitionFinder.scanForEquivalence();
        if(scanState != EquivalentMethodDefinitionFinder.ScanState.METHOD_SEEN)
          throw new IllegalStateException("Method " + method + " was not seen during equivalence scan");
        Set<BMethod> results = equivalentMethodDefinitionFinder.getResults();
        analyzedMethods.addAll(results);
        for (BMethod result : results) {
          EquivalentMethodDefinitionFinder equivalentMethodDefinitionFinder2 = new EquivalentMethodDefinitionFinder(result);
          EquivalentMethodDefinitionFinder.ScanState scanState2 = equivalentMethodDefinitionFinder2.scanForEquivalence();
          if(scanState2 != EquivalentMethodDefinitionFinder.ScanState.METHOD_SEEN)
            throw new IllegalStateException("Method " + method + " was not seen during equivalence scan");
          Set<BMethod> backwards = equivalentMethodDefinitionFinder2.getResults();
          if (!results.equals(backwards)) {
            System.out.println(results);
            System.out.println(backwards);
            throw new IllegalStateException("Backwards equivalence class mismatch: " + result + " vs " + method);
          }
        }
      }
    }
   */
}
