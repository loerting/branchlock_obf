package net.branchlock.cryptography;

import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DESedeECBTest {
    @Test
    public void testEncryptDecrypt() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        DESedeECB desedeECB = new DESedeECB("12345678");
        String abc = desedeECB.encrypt("abcöäü");
        assertEquals("abcöäü", desedeECB.decrypt(abc));
    }

    @Test
    public void testEncryptDecryptLine() {
        DESedeECB desedeECB = new DESedeECB("12345678");
        assertEquals(0, desedeECB.decryptLine(desedeECB.encryptLine(0)));
        for(int i = 0; i < Short.MAX_VALUE; i += 13) {
            assertEquals(i, desedeECB.decryptLine(desedeECB.encryptLine(i)));
            i *= 2;
        }
    }
}
