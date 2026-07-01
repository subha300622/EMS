package com.example.ems.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Base abstract class for multi-level (L1 Caffeine + L2 Redis) caching.
 * Provides distributed/JVM stampede protection, negative caching,
 * async writes, Redis resilience, and metrics.
 */
public abstract class BaseCacheService {

    protected static final Logger log = LoggerFactory.getLogger(BaseCacheService.class);

    // ── Key Stampede Protection Locks ────────────────────────────────────────
    private final ConcurrentHashMap<String, RefCountedLock> keyLocks = new ConcurrentHashMap<>();

    @Autowired(required = false)
    protected StringRedisTemplate redis;

    @Autowired
    protected ObjectMapper objectMapper;

    @Qualifier("cacheTaskExecutor")
    @Autowired
    protected Executor cacheTaskExecutor;

    @Value("${spring.profiles.active:default}")
    protected String env;

    private final AtomicLong l2Hits = new AtomicLong();
    private final AtomicLong l2Misses = new AtomicLong();
    private final AtomicLong dbLookups = new AtomicLong();

    private final Cache<String, Object> dashboardCache = Caffeine.newBuilder()
            .expireAfterWrite(CacheTTL.L1_DASHBOARD)
            .maximumSize(200)
            .recordStats()
            .build();

    private final Cache<String, Object> dashboardChartCache = Caffeine.newBuilder()
            .expireAfterWrite(CacheTTL.L1_DASHBOARD_CHART)
            .maximumSize(200)
            .recordStats()
            .build();

    private final Cache<String, Object> profileCache = Caffeine.newBuilder()
            .expireAfterWrite(CacheTTL.L1_PROFILE)
            .maximumSize(2000)
            .recordStats()
            .build();

    private final Cache<String, Object> listCache = Caffeine.newBuilder()
            .expireAfterWrite(CacheTTL.L1_LIST)
            .maximumSize(500)
            .recordStats()
            .build();

    private final Cache<String, Object> approvalQueueCache = Caffeine.newBuilder()
            .expireAfterWrite(CacheTTL.L1_APPROVAL_QUEUE)
            .maximumSize(200)
            .recordStats()
            .build();

    private final Cache<String, Object> myDataCache = Caffeine.newBuilder()
            .expireAfterWrite(CacheTTL.L1_MY_DATA)
            .maximumSize(2000)
            .recordStats()
            .build();

    private final Cache<String, Object> reportCache = Caffeine.newBuilder()
            .expireAfterWrite(CacheTTL.L1_REPORT)
            .maximumSize(100)
            .recordStats()
            .build();

    private final Cache<String, Object> referenceDataCache = Caffeine.newBuilder()
            .expireAfterWrite(CacheTTL.L1_REFERENCE_DATA)
            .maximumSize(1000)
            .recordStats()
            .build();

    private final Cache<String, Object> defaultCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(1000)
            .recordStats()
            .build();

    /**
     * Cache categories that determine which Caffeine bucket and L2 TTL to use.
     */
    public enum CacheCategory {
        DASHBOARD,
        DASHBOARD_CHART,
        PROFILE,
        LIST,
        APPROVAL_QUEUE,
        MY_DATA,
        REPORT,
        REFERENCE_DATA,
        DEFAULT
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Cache<String, Object> getL1Cache(CacheCategory category) {
        if (category == null) {
            return defaultCache;
        }

        return switch (category) {
            case DASHBOARD -> dashboardCache;
            case DASHBOARD_CHART -> dashboardChartCache;
            case PROFILE -> profileCache;
            case LIST -> listCache;
            case APPROVAL_QUEUE -> approvalQueueCache;
            case MY_DATA -> myDataCache;
            case REPORT -> reportCache;
            case REFERENCE_DATA -> referenceDataCache;
            default -> defaultCache;
        };
    }

    private Duration getL2Ttl(CacheCategory category) {
        if (category == null) {
            return Duration.ofMinutes(10);
        }

        return switch (category) {
            case DASHBOARD -> CacheTTL.L2_DASHBOARD;
            case DASHBOARD_CHART -> CacheTTL.L2_DASHBOARD_CHART;
            case PROFILE -> CacheTTL.L2_PROFILE;
            case LIST -> CacheTTL.L2_LIST;
            case APPROVAL_QUEUE -> CacheTTL.L2_APPROVAL_QUEUE;
            case MY_DATA -> CacheTTL.L2_MY_DATA;
            case REPORT -> CacheTTL.L2_REPORT;
            case REFERENCE_DATA -> CacheTTL.L2_REFERENCE_DATA;
            default -> Duration.ofMinutes(10);
        };
    }

    // -------------------------------------------------------------------------
    // Read API  (L1 -> L2 -> DB fallback)
    // -------------------------------------------------------------------------

    /**
     * Retrieves an item using a Cache-Aside multi-level pattern.
     *
     * @param key        The cache key.
     * @param category   The cache category (defines TTL / Caffeine instance).
     * @param type       The destination Class type.
     * @param dbFallback The DB lookup supplier (invoked on cache miss).
     * @param <T>        The generic type.
     * @return The cached or DB-retrieved value.
     */
    protected <T> T get(String key, CacheCategory category,
                        Class<T> type, Supplier<? extends T> dbFallback) {

        // 1. Negative cache check
        if (isNegativeCached(key)) {
            log.debug("[L1/L2 Cache] Negative HIT for key: {}", key);
            return null;
        }

        Cache<String, Object> l1 = getL1Cache(category);

        // 2. L1 (Caffeine)
        Object l1Val = l1.getIfPresent(key);
        if (l1Val != null && type.isInstance(l1Val)) {
            log.debug("[L1 Cache] HIT for key: {}", key);
            return type.cast(l1Val);
        }

        // 3. L2 (Redis)
        T l2Val = readFromL2(key, type);
        if (l2Val != null) {
            log.debug("[L2 Cache] HIT for key: {}", key);
            l1.put(key, l2Val);
            l2Hits.incrementAndGet();
            return l2Val;
        }
        l2Misses.incrementAndGet();

        // 4. Stampede protection
        String lockKey = "lock:" + key;
        boolean hasRedisLock = acquireRedisLock(lockKey);
        RefCountedLock jvmLock = null;

        if (!hasRedisLock && redis != null) {
            log.debug("[Cache Lock] Lock busy for '{}'. Retrying read.", key);
            for (int i = 0; i < 5; i++) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                l1Val = l1.getIfPresent(key);
                if (l1Val != null && type.isInstance(l1Val)) {
                    return type.cast(l1Val);
                }
                l2Val = readFromL2(key, type);
                if (l2Val != null) {
                    l1.put(key, l2Val);
                    l2Hits.incrementAndGet();
                    return l2Val;
                }
                if (acquireRedisLock(lockKey)) {
                    hasRedisLock = true;
                    break;
                }
            }
        }

        if (!hasRedisLock) {
            jvmLock = keyLocks.compute(key, (k, existing) -> {
                if (existing == null) {
                    return new RefCountedLock();
                } else {
                    existing.refCount++;
                    return existing;
                }
            });
            jvmLock.lock.lock();
        }

        try {
            // Double-check inside lock
            l1Val = l1.getIfPresent(key);
            if (l1Val != null && type.isInstance(l1Val)) {
                return type.cast(l1Val);
            }
            l2Val = readFromL2(key, type);
            if (l2Val != null) {
                l1.put(key, l2Val);
                return l2Val;
            }

            // 5. DB fallback
            log.debug("[Cache] MISS for key: {}. Loading from DB.", key);
            dbLookups.incrementAndGet();
            T dbVal = dbFallback.get();

            if (dbVal != null) {
                T finalDbVal = dbVal;
                l1.put(key, finalDbVal);
                runAsync(() -> writeToRedis(key, finalDbVal, getL2Ttl(category)));
            } else {
                runAsync(() -> markNegativeCache(key));
            }

            return dbVal;
        } finally {
            if (hasRedisLock) {
                releaseRedisLock(lockKey);
            }
            if (jvmLock != null) {
                jvmLock.lock.unlock();
                keyLocks.compute(key, (k, existing) -> {
                    if (existing != null) {
                        existing.refCount--;
                        if (existing.refCount == 0) {
                            return null;
                        }
                    }
                    return existing;
                });
            }
        }
    }

    // -------------------------------------------------------------------------
    // Write / Evict API
    // -------------------------------------------------------------------------

    protected void put(String key, CacheCategory category, Object value) {
        if (value == null) {
            evict(key, category);
            return;
        }
        getL1Cache(category).put(key, value);
        evictNegativeCache(key);
        runAsync(() -> writeToRedis(key, value, getL2Ttl(category)));
    }

    protected void evict(String key, CacheCategory category) {
        getL1Cache(category).invalidate(key);
        evictNegativeCache(key);
        runAsync(() -> deleteFromRedis(key));
    }

    protected void clearL1(CacheCategory category) {
        getL1Cache(category).invalidateAll();
    }

    // -------------------------------------------------------------------------
    // Stats
    // -------------------------------------------------------------------------

    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("dashboard_L1", getL1Stats(dashboardCache));
        stats.put("dashboardChart_L1", getL1Stats(dashboardChartCache));
        stats.put("profile_L1", getL1Stats(profileCache));
        stats.put("list_L1", getL1Stats(listCache));
        stats.put("approvalQueue_L1", getL1Stats(approvalQueueCache));
        stats.put("myData_L1", getL1Stats(myDataCache));
        stats.put("report_L1", getL1Stats(reportCache));
        stats.put("referenceData_L1", getL1Stats(referenceDataCache));
        stats.put("default_L1", getL1Stats(defaultCache));
        stats.put("l2_hits", l2Hits.get());
        stats.put("l2_misses", l2Misses.get());
        stats.put("db_lookups", dbLookups.get());
        return stats;
    }

    private Map<String, Object> getL1Stats(Cache<String, Object> cache) {
        var s = cache.stats();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("hitCount", s.hitCount());
        m.put("missCount", s.missCount());
        m.put("hitRate", s.hitRate());
        m.put("size", cache.estimatedSize());
        return m;
    }

    // -------------------------------------------------------------------------
    // Redis operations
    // -------------------------------------------------------------------------

    private <T> T readFromL2(String key, Class<T> type) {
        if (redis == null) {
            return null;
        }
        try {
            String json = redis.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            log.warn("[L2 Cache] READ failed for key '{}': {}", key, ex.getMessage());
            return null;
        }
    }

    private void writeToRedis(String key, Object value, Duration ttl) {
        if (redis == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(value);
            redis.opsForValue().set(key, json, ttl);
            log.debug("[L2 Cache] WRITE key='{}' ttl={}", key, ttl);
        } catch (JsonProcessingException ex) {
            log.warn("[L2 Cache] Serialization failed for key '{}': {}", key, ex.getMessage());
        } catch (Exception ex) {
            log.warn("[L2 Cache] WRITE failed for key '{}': {}", key, ex.getMessage());
        }
    }

    private void deleteFromRedis(String key) {
        if (redis == null) {
            return;
        }
        try {
            redis.delete(key);
            log.debug("[L2 Cache] EVICT key='{}'", key);
        } catch (Exception ex) {
            log.warn("[L2 Cache] EVICT failed for key '{}': {}", key, ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Distributed lock helpers
    // -------------------------------------------------------------------------

    private boolean acquireRedisLock(String lockKey) {
        if (redis == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(
                    redis.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(5)));
        } catch (Exception ex) {
            log.warn("[Cache Lock] Redis error acquiring lock '{}': {}", lockKey, ex.getMessage());
            return false;
        }
    }

    private void releaseRedisLock(String lockKey) {
        if (redis == null) {
            return;
        }
        try {
            redis.delete(lockKey);
        } catch (Exception ex) {
            log.warn("[Cache Lock] Redis error releasing lock '{}': {}", lockKey, ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Negative cache helpers
    // -------------------------------------------------------------------------

    private String getNegativeKey(String key) {
        return key + ":null";
    }

    private boolean isNegativeCached(String key) {
        if (redis == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redis.hasKey(getNegativeKey(key)));
        } catch (Exception ex) {
            log.warn("[Cache] Negative-cache check failed for '{}': {}", key, ex.getMessage());
            return false;
        }
    }

    private void markNegativeCache(String key) {
        if (redis == null) {
            return;
        }
        String negKey = getNegativeKey(key);
        try {
            redis.opsForValue().set(negKey, "1", CacheTTL.L2_NEGATIVE_CACHE);
            log.debug("[Cache] Negative cache set for '{}' ttl={}", key, CacheTTL.L2_NEGATIVE_CACHE);
        } catch (Exception ex) {
            log.warn("[Cache] Failed to write null-marker for '{}': {}", key, ex.getMessage());
        }
    }

    private void evictNegativeCache(String key) {
        if (redis == null) {
            return;
        }
        String negKey = getNegativeKey(key);
        try {
            redis.delete(negKey);
        } catch (Exception ex) {
            log.warn("[Cache] Failed to evict null-marker for '{}': {}", key, ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Async helper
    // -------------------------------------------------------------------------

    protected void runAsync(Runnable task) {
        cacheTaskExecutor.execute(() -> {
            try {
                task.run();
            } catch (Exception ex) {
                log.error("[Cache] Async task failed: {}", ex.getMessage(), ex);
            }
        });
    }

    private static class RefCountedLock {
        private final ReentrantLock lock = new ReentrantLock();
        private int refCount = 1;
    }
}
