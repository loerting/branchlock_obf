package net.branchlock.structure;

import net.branchlock.Branchlock;

import java.util.*;
import java.util.stream.Stream;

/**
 * Container with O(1) lookup for members.
 *
 * @param <T>
 */
public class BMemberContainer<T extends BMember> implements List<T> {

  private final Map<String, T> membersTable = new HashMap<>();
  private final List<T> members = new ArrayList<>();

  public BMemberContainer() {
  }

  @Override
  public int size() {
    return members.size();
  }

  @Override
  public boolean isEmpty() {
    return members.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return members.contains(o);
  }

  @Override
  public BMemberIterator iterator() {
    return new BMemberIterator();
  }

  @Override
  public Object[] toArray() {
    return members.toArray();
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    return members.toArray(a);
  }

  @Override
  public boolean add(T t) {
    members.add(t);
    if(membersTable.put(t.getIdentifier(), t) != null) throw new IllegalStateException("element + " + t.getIdentifier() + " already exists in member table");
    return true;
  }

  @Override
  public boolean remove(Object o) {
    int index = members.indexOf(o);
    if (index != -1) {
      members.remove(index);
      if (membersTable.remove(((T) o).getIdentifier()) == null) throw new IllegalStateException("member table is inconsistent with members");
      return true;
    }
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return members.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    boolean result = false;
    for (T t : c) {
      result |= add(t);
    }
    return result;
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    for (T t : c) {
      add(index++, t);
    }
    return !c.isEmpty();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    boolean result = false;
    for (Object o : c) {
      result |= remove(o);
    }
    return result;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    boolean result = false;
    for (T t : members) {
      if (!c.contains(t)) {
        result |= remove(t);
      }
    }
    return result;
  }

  @Override
  public void clear() {
    members.clear();
    membersTable.clear();
  }

  @Override
  public T get(int index) {
    return members.get(index);
  }

  @Override
  public T set(int index, T element) {
    T oldElement = members.set(index, element);
    if(membersTable.remove(oldElement.getIdentifier()) == null) throw new IllegalStateException("member table is inconsistent with members");
    if(membersTable.put(element.getIdentifier(), element) != null) throw new IllegalStateException("element already exists in memberIndices");
    return oldElement;
  }

  @Override
  public void add(int index, T element) {
    members.add(index, element);
    if(membersTable.put(element.getIdentifier(), element) != null) throw new IllegalStateException("element already exists in memberIndices");
  }

  @Override
  public T remove(int index) {
    T removedElement = members.remove(index);
    if(membersTable.remove(removedElement.getIdentifier()) == null) throw new IllegalStateException("member table is inconsistent with members");
    return removedElement;
  }

  @Override
  public int indexOf(Object o) {
    return members.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return members.lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator() {
    // do not return members.listIterator() because it does not update memberIndices
    throw new UnsupportedOperationException();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return members.subList(fromIndex, toIndex);
  }

  @Override
  public String toString() {
    return members.toString();
  }

  @Override
  public Stream<T> stream() {
    return members.stream();
  }

  public List<T> getCollection() {
    // unmodifiable so memberIndices is not messed up
    return Collections.unmodifiableList(members);
  }

  public void shuffle() {
    // shuffle using Sattolo's algorithm
    for (int i = members.size() - 1; i > 0; i--) {
      int j = Branchlock.R.nextInt(i + 1);
      T temp = members.get(i);
      members.set(i, members.get(j));
      members.set(j, temp);
    }
  }

  public T get(String name, String desc) {
    if(name == null) throw new IllegalArgumentException("name cannot be null");
    if(desc == null) throw new IllegalArgumentException("desc cannot be null");
    // get using memberIndices
    String identifier = name + desc;
    return membersTable.get(identifier);
  }

  public void observeIdentifierChanged(T member, String oldIdentifier) {
    if (membersTable.remove(oldIdentifier) == null) {
      throw new IllegalArgumentException("oldIdentifier does not exist in memberIndices");
    }
    if (membersTable.put(member.getIdentifier(), member) != null) {
      throw new IllegalArgumentException("newIdentifier already exists in memberIndices");
    }
    if (members.size() != membersTable.size()) throw new IllegalStateException("member table is inconsistent with members");
  }

  public boolean containsSignature(String name, String desc) {
    if(name == null) throw new IllegalArgumentException("name cannot be null");
    if(desc == null) throw new IllegalArgumentException("desc cannot be null");
    return membersTable.containsKey(name + desc);
  }

  public T find(String name, String desc) {
    return stream().filter(m -> (name == null || m.getName().equals(name)) && (desc == null || m.getDescriptor().equals(desc))).findFirst().orElse(null);
  }

  public class BMemberIterator implements Iterator<T> {
    private final Iterator<T> iterator = members.iterator();
    private T last = null;

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public T next() {
      return last = iterator.next();
    }

    @Override
    public void remove() {
      iterator.remove();
      membersTable.remove(last.getIdentifier());
    }
  }
}
