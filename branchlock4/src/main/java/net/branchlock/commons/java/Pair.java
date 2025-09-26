package net.branchlock.commons.java;

import java.util.Objects;

public class Pair<A, B> {

  public final A a;
  public final B b;

  public Pair(A a, B b) {
    this.a = a;
    this.b = b;
  }

  public static <A, B> Pair<A, B> of(A a, B b) {
    return new Pair<>(a, b);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Pair<?, ?> pair = (Pair<?, ?>) o;

    if (!Objects.equals(a, pair.a)) return false;
    return Objects.equals(b, pair.b);
  }

  @Override
  public int hashCode() {
    int result = a != null ? a.hashCode() : 0;
    result = 31 * result + (b != null ? b.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Pair{" +
      "a=" + a +
      ", b=" + b +
      '}';
  }
}
