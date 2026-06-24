package com.example.ems.common.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {
    private final ConcurrentHashMap<String, Boolean> processedKeys = new ConcurrentHashMap<>();

    /**
     * Checks if a key has already been processed. If not, registers it.
     * Scoped key format: userId:onboardingId:commandType:key
     */
    public boolean isDuplicate(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        return processedKeys.putIfAbsent(key, Boolean.TRUE) != null;
    }

    public void clear(String key) {
        if (key != null) {
            processedKeys.remove(key);
        }
    }
}
