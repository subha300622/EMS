package com.example.ems.payroll.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public final class AesEncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final String DEFAULT_SECRET = "RazorpayXSecureKeyOrgConfig2026!"; // 32 chars = 256 bits

    private AesEncryptionUtil() {}

    public static String encrypt(String plaintext) {
        return encrypt(plaintext, DEFAULT_SECRET);
    }

    public static String encrypt(String plaintext, String secretKey) {
        if (plaintext == null || plaintext.isEmpty()) return plaintext;
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv);

            byte[] keyBytes = getKeyBytes(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decrypt(String ciphertext) {
        return decrypt(ciphertext, DEFAULT_SECRET);
    }

    public static String decrypt(String ciphertext, String secretKey) {
        if (ciphertext == null || ciphertext.isEmpty()) return ciphertext;
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            if (combined.length < IV_LENGTH_BYTE) {
                return ciphertext; // Not encrypted / legacy plain
            }

            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTE);

            byte[] cipherText = new byte[combined.length - IV_LENGTH_BYTE];
            System.arraycopy(combined, IV_LENGTH_BYTE, cipherText, 0, cipherText.length);

            byte[] keyBytes = getKeyBytes(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Fallback for non-encrypted test strings
            return ciphertext;
        }
    }

    private static byte[] getKeyBytes(String key) {
        byte[] bytes = new byte[32];
        byte[] source = (key != null ? key : DEFAULT_SECRET).getBytes(StandardCharsets.UTF_8);
        System.arraycopy(source, 0, bytes, 0, Math.min(source.length, 32));
        return bytes;
    }
}
