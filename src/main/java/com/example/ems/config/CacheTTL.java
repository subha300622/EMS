package com.example.ems.config;

import java.time.Duration;

/**
 * Centralised TTL constants for all cache entries (L1 Caffeine + L2 Redis).
 * Adjust per-entity to reflect business SLAs.
 */
public final class CacheTTL {

    private CacheTTL() {}

    // ── Negative Cache TTL ───────────────────────────────────────────────────
    /** Negative cache TTL for non-existent items to prevent DB hammered */
    public static final Duration L2_NEGATIVE_CACHE = Duration.ofMinutes(2);

    // ── L1 Cache (Caffeine) TTLs (Fast Local In-Memory, short lived) ──────────
    public static final Duration L1_DASHBOARD      = Duration.ofMinutes(1);
    public static final Duration L1_DASHBOARD_CHART = Duration.ofMinutes(2);
    public static final Duration L1_PROFILE        = Duration.ofMinutes(2);
    public static final Duration L1_LIST           = Duration.ofMinutes(1);
    public static final Duration L1_APPROVAL_QUEUE = Duration.ofSeconds(30);
    public static final Duration L1_MY_DATA        = Duration.ofMinutes(2);
    public static final Duration L1_REPORT         = Duration.ofMinutes(5);
    public static final Duration L1_REFERENCE_DATA = Duration.ofMinutes(10);

    // ── L2 Cache (Redis) TTLs (Distributed, longer lived) ────────────────────
    public static final Duration L2_DASHBOARD      = Duration.ofMinutes(5);
    public static final Duration L2_DASHBOARD_CHART = Duration.ofMinutes(15);
    public static final Duration L2_PROFILE        = Duration.ofMinutes(15);
    public static final Duration L2_LIST           = Duration.ofMinutes(10);
    public static final Duration L2_APPROVAL_QUEUE = Duration.ofMinutes(3);
    public static final Duration L2_MY_DATA        = Duration.ofMinutes(10);
    public static final Duration L2_REPORT         = Duration.ofMinutes(60);
    public static final Duration L2_REFERENCE_DATA = Duration.ofHours(6);

    // ── Reminder cache legacy TTLs (compatible with existing code) ───────────
    public static final Duration REMINDER_SINGLE   = Duration.ofMinutes(15);
    public static final Duration REMINDER_ALL      = Duration.ofMinutes(10);
    public static final Duration REMINDER_BY_USER  = Duration.ofMinutes(10);
    public static final Duration REMINDER_NOT_FOUND = Duration.ofMinutes(2);
}
