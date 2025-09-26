package net.branchlock.task.implementation.references.calls.container;

import net.branchlock.commons.java.MultiMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EncryptedMemberContainer {
  /**
   * Maps an EncryptedMember hash to its EncryptedMembers.
   * Note that two EncryptedMembers, one with field set and the other with field get have the same hash / code. Therefore, the MultiMap.
   */
  private final MultiMap<Long, EncryptedMember> memberMap = new MultiMap<>();
  private final Map<EncryptedMember, Integer> indexMap = new HashMap<>(25);


  public int getIndex(EncryptedMember em) {
    if (!indexMap.containsKey(em))
      throw new IllegalStateException("Encrypted member not in container");
    return indexMap.get(em);
  }

  public boolean addMember(EncryptedMember em) {
    if (memberMap.containsKey(em.hash)) {
      Collection<EncryptedMember> others = memberMap.get(em.hash);
      if (others.stream().anyMatch(o -> !o.equalsNoType(em))) {
        // Collision! Two different references are used with the same hash.
        // It could happen that a field and method have the same hash.

        // Two different Reference access types are allowed, this is why we use equalsNoType.
        return false;
      }
    }

    memberMap.put(em.hash, em);
    indexMap.computeIfAbsent(em, (k) -> indexMap.size());

    return true;
  }

  public boolean isEmpty() {
    return memberMap.isEmpty();
  }

  public int size() {
    return indexMap.size();
  }

  public Set<Long> hashes() {
    return memberMap.keySet();
  }

  public Collection<EncryptedMember> members() {
    return memberMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
  }
}
