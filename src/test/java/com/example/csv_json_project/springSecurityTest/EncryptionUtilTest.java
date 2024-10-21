package com.example.csv_json_project.springSecurityTest;

import com.example.csv_json_project.springSecurity.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EncryptionUtilTest {

    @Test
    void testEncryptAndDecrypt() {
        String originalText = "Hello, World!";
        String encryptedText = EncryptionUtil.encrypt(originalText);
        assertNotNull(encryptedText);
        assertNotEquals(originalText, encryptedText);

        String decryptedText = EncryptionUtil.decrypt(encryptedText);
        assertNotNull(decryptedText);
        assertEquals(originalText, decryptedText);
    }

    //Test the encryption of a null value.
    @Test
    void testEncryptNull() {
        String encryptedText = EncryptionUtil.encrypt(null);
        assertNull(encryptedText);
    }

    //Test the decryption of a null value.
    @Test
    void testDecryptNull() {
        String decryptedText = EncryptionUtil.decrypt(null);
        assertNull(decryptedText);
    }

    //encryption and decryption of an empty string.
    @Test
    void testEncryptEmptyString() {
        String originalText = "";
        String encryptedText = EncryptionUtil.encrypt(originalText);
        assertNotNull(encryptedText);
        assertNotEquals(originalText, encryptedText);

        String decryptedText = EncryptionUtil.decrypt(encryptedText);
        assertNotNull(decryptedText);
        assertEquals(originalText, decryptedText);
    }

    //encryption and decryption of a long string.
    @Test
    void testEncryptLongString() {
        String originalText = "This is a very long string that should be encrypted and decrypted correctly. " +
                "It contains a lot of characters to ensure that the encryption and decryption process works " +
                "properly with longer texts.";
        String encryptedText = EncryptionUtil.encrypt(originalText);
        assertNotNull(encryptedText);
        assertNotEquals(originalText, encryptedText);

        String decryptedText = EncryptionUtil.decrypt(encryptedText);
        assertNotNull(decryptedText);
        assertEquals(originalText, decryptedText);
    }

    //encryption and decryption of a string containing special characters.
    @Test
    void testEncryptSpecialCharacters() {
        String originalText = "!@#$%^&*()_+-={}[]|\\:;\"'<>,.?/";
        String encryptedText = EncryptionUtil.encrypt(originalText);
        assertNotNull(encryptedText);
        assertNotEquals(originalText, encryptedText);

        String decryptedText = EncryptionUtil.decrypt(encryptedText);
        assertNotNull(decryptedText);
        assertEquals(originalText, decryptedText);
    }
}