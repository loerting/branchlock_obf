package net.branchlock.structure.provider;

import net.branchlock.structure.BClass;
import net.branchlock.structure.BField;
import net.branchlock.structure.BMethod;
import net.branchlock.structure.BResource;
import net.branchlock.structure.equivalenceclass.IEquivalenceClass;

import java.util.Map;
import java.util.stream.Stream;

public interface IDataStreamProvider {
  default Stream<BClass> streamInputClasses() {
    throw new UnsupportedOperationException();
  }
  default Stream<BMethod> streamInputMethods() {
    throw new UnsupportedOperationException();
  }
  default Stream<BField> streamInputFields() {
    throw new UnsupportedOperationException();
  }
  default Stream<Map.Entry<String, BClass>> streamClasspathEntries() {
    throw new UnsupportedOperationException();
  }

  default Stream<BClass> streamClasspath() {
    throw new UnsupportedOperationException();
  }

  default Stream<BResource> streamResources() {
    throw new UnsupportedOperationException();
  }

  default Stream<IEquivalenceClass<BMethod>> streamInputMethodEquivalenceClasses() {
    throw new UnsupportedOperationException();
  }
}
