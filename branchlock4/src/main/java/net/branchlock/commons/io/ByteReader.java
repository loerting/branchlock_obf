package net.branchlock.commons.io;

public class ByteReader {

  private final byte[] b;

  public ByteReader(byte[] b) {
    this.b = b;
  }

  public int readUnsignedShort(final int offset) {
    return ((b[offset] & 0xFF) << 8) | (b[offset + 1] & 0xFF);
  }
}
