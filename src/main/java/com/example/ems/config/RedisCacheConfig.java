package com.example.ems.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import org.springframework.cache.concurrent.ConcurrentMapCache;

/**
 * Redis infrastructure and cache configuration.
 *
 * <h3>Design</h3>
 * <ul>
 * <li>{@link StringRedisTemplate} — used by
 * {@link com.example.ems.reminder.service.ReminderCacheService}
 * for manual, explicit Redis operations (JSON string values).</li>
 * <li>{@link CacheManager} — a simple in-memory {@link SimpleCacheManager}
 * backed by
 * {@link ConcurrentMapCache}. Used by services that rely on Spring's
 * {@code @Cacheable}
 * annotation (e.g. {@link com.example.ems.auth.service.RoleService} for
 * userPermissions).</li>
 * </ul>
 *
 * <p>
 * The Reminder module does NOT use Spring Cache annotations; it manages Redis
 * directly
 * via {@link com.example.ems.reminder.service.ReminderCacheService}.
 * </p>
 */
@Configuration
public class RedisCacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheConfig.class);

    /**
     * Shared ObjectMapper with Java-time support.
     * Marked @Primary so it is the application-wide default.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * StringRedisTemplate for raw String (JSON) operations.
     * Used by {@link com.example.ems.reminder.service.ReminderCacheService}.
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * In-memory CacheManager for services using {@code @Cacheable}.
     *
     * <p>
     * Uses {@link ConcurrentMapCache} (no external dependency) so the app
     * starts cleanly even when Redis is unavailable. Named caches are registered
     * on demand and added here explicitly for clarity.
     * </p>
     */
    @Bean
    @Override
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                new ConcurrentMapCache("userPermissions"),
                new ConcurrentMapCache("userBootstrap"),
                new ConcurrentMapCache("roles"),
                new ConcurrentMapCache("appraisalDashboard"),
                new ConcurrentMapCache("subscriptionsOverview")));
        return manager;
    }

    /**
     * Silent error handler for Spring Cache annotations.
     * Failures in @Cacheable / @CacheEvict are logged but never propagate.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException ex, org.springframework.cache.Cache cache, Object key) {
                log.warn("[Cache] GET failed key='{}' cache='{}': {}", key, cache.getName(), ex.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException ex, org.springframework.cache.Cache cache, Object key,
                    Object value) {
                log.warn("[Cache] PUT failed key='{}' cache='{}': {}", key, cache.getName(), ex.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException ex, org.springframework.cache.Cache cache, Object key) {
                log.warn("[Cache] EVICT failed key='{}' cache='{}': {}", key, cache.getName(), ex.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException ex, org.springframework.cache.Cache cache) {
                log.warn("[Cache] CLEAR failed cache='{}': {}", cache.getName(), ex.getMessage());
            }
        };
    }
}
