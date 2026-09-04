package com.example.ems.common.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PasswordGeneratorUtil {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGeneratorUtil() {}

    public static String generateSecurePassword() {
        return generateSecurePassword(12);
    }

    public static String generateSecurePassword(int length) {
        if (length < 8) {
            length = 8;
        }

        List<Character> charList = new ArrayList<>();
        charList.add(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        charList.add(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        charList.add(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        charList.add(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

        for (int i = 4; i < length; i++) {
            charList.add(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }

        Collections.shuffle(charList, RANDOM);

        StringBuilder password = new StringBuilder(length);
        for (char c : charList) {
            password.append(c);
        }

        return password.toString();
    }
}
