package net.branchlock.commons.java;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class UnusableList<T> implements List<T> {
  private final String warning;

  public UnusableList(String warning) {
    this.warning = warning;
  }

  @Override
  public int size() {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public boolean contains(Object o) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public Iterator<T> iterator() {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public Object[] toArray() {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public boolean add(T t) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public T get(int index) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public T set(int index, T element) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public void add(int index, T element) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public T remove(int index) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public int indexOf(Object o) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public int lastIndexOf(Object o) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public ListIterator<T> listIterator() {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public Spliterator<T> spliterator() {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public boolean removeIf(Predicate<? super T> filter) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public Stream<T> stream() {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public Stream<T> parallelStream() {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public void forEach(java.util.function.Consumer<? super T> action) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public String toString() {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public boolean equals(Object o) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public List<T> clone() {
    throw new UnsupportedOperationException(warning);
  }


  @Override
  public void sort(Comparator<? super T> c) {
    throw new UnsupportedOperationException(warning);
  }

  @Override
  public void replaceAll(java.util.function.UnaryOperator<T> operator) {
    throw new UnsupportedOperationException(warning);
  }
}


