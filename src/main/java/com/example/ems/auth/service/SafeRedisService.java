package com.example.ems.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

@Service
public class SafeRedisService {

    private static final Logger log = LoggerFactory.getLogger(SafeRedisService.class);

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // Propagating Set
    public void set(String key, String value, Duration timeout) {
        if (redisTemplate != null) {
            if (timeout != null) {
                redisTemplate.opsForValue().set(key, value, timeout);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
        }
    }

    // Propagating Get
    public String get(String key) {
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().get(key);
        }
        return null;
    }

    // Propagating Delete
    public void delete(String key) {
        if (redisTemplate != null) {
            redisTemplate.delete(key);
        }
    }

    // Propagating HasKey
    public Boolean hasKey(String key) {
        if (redisTemplate != null) {
            return redisTemplate.hasKey(key);
        }
        return false;
    }

    // Safe GetExpire (catching exceptions for utility usage)
    public Long getExpire(String key) {
        if (redisTemplate != null) {
            try {
                return redisTemplate.getExpire(key);
            } catch (Exception e) {
                log.warn("Redis getExpire failed for key '{}': {}", key, e.getMessage());
            }
        }
        return -2L;
    }

    // Safe Keys pattern matching
    public Set<String> keys(String pattern) {
        if (redisTemplate != null) {
            try {
                Set<String> redisKeys = redisTemplate.keys(pattern);
                if (redisKeys != null) {
                    return redisKeys;
                }
            } catch (Exception e) {
                log.warn("Redis keys failed for pattern '{}': {}", pattern, e.getMessage());
            }
        }
        return Collections.emptySet();
    }

    // Safe Increment
    public Long increment(String key) {
        if (redisTemplate != null) {
            try {
                return redisTemplate.opsForValue().increment(key);
            } catch (Exception e) {
                log.warn("Redis increment failed for key '{}': {}", key, e.getMessage());
            }
        }
        return null;
    }

    // Safe Expire
    public void expire(String key, Duration timeout) {
        if (redisTemplate != null) {
            try {
                redisTemplate.expire(key, timeout);
            } catch (Exception e) {
                log.warn("Redis expire failed for key '{}': {}", key, e.getMessage());
            }
        }
    }

    // Check if Redis is up
    public boolean isRedisAvailable() {
        if (redisTemplate == null) {
            return false;
        }
        try {
            var connFactory = redisTemplate.getConnectionFactory();
            if (connFactory == null) return false;
            try (var conn = connFactory.getConnection()) {
                String pingResult = conn.ping();
                return "PONG".equalsIgnoreCase(pingResult) || pingResult != null;
            }
        } catch (Exception e) {
            log.warn("Redis availability check failed: {}", e.getMessage());
            return false;
        }
    }
}
