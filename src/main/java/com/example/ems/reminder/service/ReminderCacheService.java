package com.example.ems.reminder.service;

import com.example.ems.config.BaseCacheService;
import com.example.ems.reminder.dto.ReminderListResponse;
import com.example.ems.reminder.dto.ReminderResponse;
import com.example.ems.reminder.repository.ReminderRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Production-grade cache facade for the Reminder module, extending {@link BaseCacheService}.
 */
@Service
public class ReminderCacheService extends BaseCacheService {

    private static final Logger log = LoggerFactory.getLogger(ReminderCacheService.class);

    // ── Key templates ────────────────────────────────────────────────────────
    private static final String PREFIX        = "ems:%s:reminder:v1:";
    private static final String KEY_SINGLE    = PREFIX + "%d";
    private static final String KEY_ALL       = PREFIX + "all";
    private static final String KEY_BY_USER   = PREFIX + "user:%d";

    @Autowired
    private ReminderRepository repository;

    // ── Key builders ─────────────────────────────────────────────────────────

    String keySingle(Long id)  { return String.format(KEY_SINGLE,  env, id); }
    String keyAll()            { return String.format(KEY_ALL,     env); }
    String keyByUser(Long uid) { return String.format(KEY_BY_USER, env, uid); }

    // ── Cache warm-up ────────────────────────────────────────────────────────

    @PostConstruct
    public void warmUpCache() {
        try {
            log.info("[Cache] Starting cache warm-up (top 100 reminders)...");
            List<ReminderResponse> top100 = repository
                    .findAll(org.springframework.data.domain.PageRequest.of(0, 100,
                            org.springframework.data.domain.Sort.by("id").descending()))
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            // Cache each individual reminder in L1 & L2
            for (ReminderResponse r : top100) {
                put(keySingle(r.getId()), CacheCategory.PROFILE, r);
            }

            // Cache the full list (only if ≤ 100 records)
            long totalCount = repository.count();
            if (totalCount <= 100) {
                ReminderListResponse all = new ReminderListResponse(top100);
                put(keyAll(), CacheCategory.LIST, all);
            }

            log.info("[Cache] Warm-up complete: {} reminders loaded into L1/L2 caches.", top100.size());
        } catch (Exception ex) {
            log.warn("[Cache] Warm-up failed (non-fatal): {}", ex.getMessage());
        }
    }

    // ── GET: single reminder ─────────────────────────────────────────────────

    public Optional<ReminderResponse> getById(Long id) {
        ReminderResponse response = get(
                keySingle(id),
                CacheCategory.PROFILE,
                ReminderResponse.class,
                () -> repository.findById(id).map(this::toResponse).orElse(null)
        );
        return Optional.ofNullable(response);
    }

    // ── GET: all reminders ───────────────────────────────────────────────────

    public ReminderListResponse getAll() {
        return get(
                keyAll(),
                CacheCategory.LIST,
                ReminderListResponse.class,
                () -> {
                    List<ReminderResponse> list = repository.findAll().stream()
                            .map(this::toResponse)
                            .collect(Collectors.toList());
                    return new ReminderListResponse(list);
                }
        );
    }

    // ── GET: by user ─────────────────────────────────────────────────────────

    public ReminderListResponse getByUser(Long userId) {
        return get(
                keyByUser(userId),
                CacheCategory.LIST,
                ReminderListResponse.class,
                () -> {
                    List<ReminderResponse> list = repository.findByEmployeeId(userId).stream()
                            .map(this::toResponse)
                            .collect(Collectors.toList());
                    return new ReminderListResponse(list);
                }
        );
    }

    // ── POST: create reminder ─────────────────────────────────────────────────

    public void onCreated(ReminderResponse saved) {
        put(keySingle(saved.getId()), CacheCategory.PROFILE, saved);
        evict(keyAll(), CacheCategory.LIST);
        evict(keyByUser(saved.getEmployeeId()), CacheCategory.LIST);
    }

    // ── PUT: update reminder ─────────────────────────────────────────────────

    public void onUpdated(ReminderResponse updated) {
        put(keySingle(updated.getId()), CacheCategory.PROFILE, updated);
        evict(keyAll(), CacheCategory.LIST);
        evict(keyByUser(updated.getEmployeeId()), CacheCategory.LIST);
    }

    // ── DELETE: reminder ──────────────────────────────────────────────────────

    public void onDeleted(Long id, Long employeeId) {
        evict(keySingle(id), CacheCategory.PROFILE);
        evict(keyAll(), CacheCategory.LIST);
        evict(keyByUser(employeeId), CacheCategory.LIST);
    }

    // ── Mapper helper ─────────────────────────────────────────────────────────

    private ReminderResponse toResponse(com.example.ems.reminder.entity.Reminder r) {
        ReminderResponse res = new ReminderResponse();
        res.setId(r.getId());
        res.setTitle(r.getTitle());
        res.setDescription(r.getDescription());
        res.setReminderDate(r.getReminderDate());
        res.setEmployeeId(r.getEmployeeId());
        res.setCreatedAt(r.getCreatedAt());
        res.setUpdatedAt(r.getUpdatedAt());
        return res;
    }
}
