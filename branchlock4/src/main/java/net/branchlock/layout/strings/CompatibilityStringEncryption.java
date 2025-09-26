package net.branchlock.layout.strings;


import net.branchlock.commons.generics.Placeholder;

@SuppressWarnings("ALL")
public class CompatibilityStringEncryption {

  // don't make them final, the compiler inlines them
  @Placeholder
  private static String STR_INPUT;
  @Placeholder
  private static int RANDOM_XOR = 678;
  @Placeholder
  private static int RANDOM_XOR_2 = 123;
  @Placeholder
  private static int RANDOM_XOR_3 = 202;

  @Placeholder
  private static int SWAP_PLACEHOLDER = 0;

  @Placeholder
  private static String[] DECRYPTED_RESULT;

  /**
   * Proguard:
   * -keepattributes LocalVariableTable,LocalVariableTypeTable class me.nov.layout.strings.CompatibilityStringEncryption
   */
  private static void initializer() {
    char[] COMPRESSED_ARRAY = STR_INPUT.toCharArray();
    int totalIndex = 0;
    int length;

    try {
      int unused = SWAP_PLACEHOLDER;
    } catch (Throwable e) {
      StackTraceElement stackTraceElement = e.getStackTrace()[0];
      int hashCodeOfClinit = stackTraceElement.getMethodName().hashCode() & 0xffff;
      char[] srcFile = stackTraceElement.getClassName().toCharArray();
      String[] resultArr = new String[COMPRESSED_ARRAY[totalIndex++] ^ RANDOM_XOR_3 ^ hashCodeOfClinit];
      int decryptedArrayIndex = 0;
      do {
        length = COMPRESSED_ARRAY[totalIndex++] ^ RANDOM_XOR_2 ^ hashCodeOfClinit;
        char[] result = new char[length];
        int resultIndex = 0;
        while (length > 0) {
          char CURRENT_CHAR = COMPRESSED_ARRAY[totalIndex];
          //noinspection SwitchStatementWithTooFewBranches
          switch (srcFile[totalIndex % srcFile.length] ^ RANDOM_XOR) {
            // BEGIN will be replaced with actual cases, placeholder so switch doesn't get optimized away
            case 65:
              System.gc();
              break;
            // END
          }
          result[resultIndex] = CURRENT_CHAR;
          resultIndex++;
          totalIndex++;
          length--;
        }
        resultArr[decryptedArrayIndex++] = new String(result).intern();
      } while (totalIndex < COMPRESSED_ARRAY.length);
      DECRYPTED_RESULT = resultArr;
    }
  }
}
