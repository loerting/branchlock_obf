package net.branchlock.commons.io;

import net.branchlock.Branchlock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClassStreamConverter {
  /**
   * @return the byte[] produced by the stream if it is a class file (CAFEBABE magic value), else null.
   */
  public static byte[] toBytesOnlyClass(InputStream is) {
    try {
      byte[] magix = new byte[4];
      if (is.read(magix, 0, magix.length) != 4) {
        return null;
      }
      if (magix[0] != (byte) 0xCA || magix[1] != (byte) 0xFE || magix[2] != (byte) 0xBA || magix[3] != (byte) 0xBE)
        return null;
      try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
        buffer.write(magix, 0, 4);

        int bufferSize = computeBufferSize(is);

        int nRead;
        byte[] data = new byte[bufferSize];

        while ((nRead = is.read(data, 0, bufferSize)) != -1) {
          buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
      }
    } catch (IOException e) {
      Branchlock.LOGGER.error("Error while reading class file", e);
      return null;
    }
  }

  private static int computeBufferSize(final InputStream inputStream) throws IOException {
    int expectedLength = inputStream.available();
    /*
     * Some implementations can return 0 while holding available data (e.g. new
     * FileInputStream("/proc/a_file")). Also in some pathological cases a very small number might
     * be returned, and in this case we use a default size.
     */
    if (expectedLength < 256) {
      return 4096;
    }
    return Math.min(expectedLength, 1024 * 1024);
  }

}
