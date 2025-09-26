package net.branchlock.structure;

import net.branchlock.structure.provider.DataProvider;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.jar.JarEntry;

public class BResource implements BMember {
  public final String originalName;
  public String name;
  public byte[] replacement = null;

  public BResource(String originalName) {
    this.originalName = originalName;
    this.name = originalName;
  }

  public String getPackage() {
    if (name.indexOf('/') == -1)
      return "";
    return name.substring(0, name.lastIndexOf('/') + 1);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isLocal() {
    return true;
  }

  @Override
  public boolean hasOwner() {
    return false;
  }

  @Override
  public BMember getOwner() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDescriptor() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasAnnotation(String annotation) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getAccess() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAccess(int access) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getIdentifier() {
    return name;
  }

  @Override
  public String getOriginalName() {
    return originalName;
  }

  public String getExtension() {
    if (name.indexOf('.') == -1)
      return "";
    return name.substring(name.lastIndexOf('.') + 1);
  }

  public byte[] getContent(DataProvider provider, long maxSize) throws IOException {
    if(replacement != null)
      return replacement;
    JarEntry jarEntry = provider.openJarFile.getJarEntry(originalName);
    if (jarEntry == null)
      return null;
    if (maxSize != -1 && jarEntry.getSize() > maxSize)
      return null;
    return IOUtils.toByteArray(provider.openJarFile.getInputStream(jarEntry));
  }
}
