package com.example.ems.common.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Logback converter that masks sensitive data such as passwords, JWT tokens,
 * secrets, and OTPs from log output.
 */
public class MaskingMessageConverter extends MessageConverter {

    private static final String MASK_REPLACEMENT = "******";

    // Matches JSON fields: "password": "...", "token": "..."
    private static final Pattern JSON_SENSITIVE_PATTERN = Pattern.compile(
            "\"(?i)(password|pass|passwd|secret|jwt|token|accessToken|refreshToken|idToken|otp|otpCode|verificationCode|bearer|authorization|apiKey|keySecret|webhookSecret|aesKey|jwtSecret)\"\\s*:\\s*\"([^\"]*)\""
    );

    // Matches key-value pairs: password=..., token: ..., etc.
    private static final Pattern KV_SENSITIVE_PATTERN = Pattern.compile(
            "(?i)\\b(password|pass|passwd|secret|jwt|token|accessToken|refreshToken|idToken|otp|otpCode|verificationCode|apiKey|keySecret|webhookSecret|aesKey|jwtSecret)\\s*[:=]\\s*([\"']?[^\"'\\s,;}\\]]+[\"']?)"
    );

    // Matches Bearer authorization tokens: Bearer <token>
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile(
            "(?i)Bearer\\s+([A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_=]*|[A-Za-z0-9-_]{20,})"
    );

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        if (message == null || message.isEmpty()) {
            return message;
        }
        return mask(message);
    }

    public static String mask(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 1. Mask JSON sensitive fields
        Matcher jsonMatcher = JSON_SENSITIVE_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (jsonMatcher.find()) {
            String key = jsonMatcher.group(1);
            jsonMatcher.appendReplacement(sb, "\"" + Matcher.quoteReplacement(key) + "\": \"" + MASK_REPLACEMENT + "\"");
        }
        jsonMatcher.appendTail(sb);
        String result = sb.toString();

        // 2. Mask Bearer tokens
        Matcher bearerMatcher = BEARER_TOKEN_PATTERN.matcher(result);
        sb = new StringBuffer();
        while (bearerMatcher.find()) {
            bearerMatcher.appendReplacement(sb, "Bearer " + MASK_REPLACEMENT);
        }
        bearerMatcher.appendTail(sb);
        result = sb.toString();

        // 3. Mask Key-Value pairs
        Matcher kvMatcher = KV_SENSITIVE_PATTERN.matcher(result);
        sb = new StringBuffer();
        while (kvMatcher.find()) {
            String fullMatch = kvMatcher.group(0);
            String key = kvMatcher.group(1);
            String separator = fullMatch.contains(":") ? ":" : "=";
            boolean hasSpace = fullMatch.contains(separator + " ");
            String replacement = key + separator + (hasSpace ? " " : "") + MASK_REPLACEMENT;
            kvMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        kvMatcher.appendTail(sb);

        return sb.toString();
    }
}
