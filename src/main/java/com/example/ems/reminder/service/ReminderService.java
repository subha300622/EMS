package com.example.ems.reminder.service;

import com.example.ems.reminder.dto.ReminderListResponse;
import com.example.ems.reminder.dto.ReminderRequest;
import com.example.ems.reminder.dto.ReminderResponse;
import com.example.ems.reminder.entity.Reminder;
import com.example.ems.reminder.repository.ReminderRepository;
import com.example.ems.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business-logic layer for Reminders.
 *
 * <p>This service is intentionally free of caching annotations and cache logic.
 * All cache concerns are delegated to {@link ReminderCacheService}.</p>
 *
 * <h3>Flow for each operation</h3>
 * <pre>
 *   GET    → CacheService.getById/getAll/getByUser → DB fallback → async cache refresh
 *   POST   → DB insert (Transactional) → CacheService.onCreated  (async evict+write)
 *   PUT    → DB update (Transactional) → CacheService.onUpdated  (async evict+write)
 *   DELETE → DB delete (Transactional) → CacheService.onDeleted  (async evict)
 * </pre>
 */
@Service
public class ReminderService {

    @Autowired
    private ReminderRepository repository;

    @Autowired
    private ReminderCacheService cacheService;

    @Autowired
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    // ── READ ─────────────────────────────────────────────────────────────────

    /**
     * Returns a single reminder by ID.
     * Cache → DB fallback, with negative caching for missing IDs.
     *
     * @throws RuntimeException if the reminder is not found.
     */
    @Transactional(readOnly = true)
    public ReminderResponse getReminder(Long id) {
        return cacheService.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));
    }

    /**
     * Returns all reminders (cache-first).
     */
    @Transactional(readOnly = true)
    public ReminderListResponse getAllReminders() {
        return cacheService.getAll();
    }

    /**
     * Returns all reminders for a given employee (cache-first).
     */
    @Transactional(readOnly = true)
    public ReminderListResponse getRemindersByUser(Long userId) {
        return cacheService.getByUser(userId);
    }

    // ── WRITE ────────────────────────────────────────────────────────────────

    /**
     * Creates a new reminder, saves to DB first, then triggers async cache update.
     */
    @Transactional
    public ReminderResponse createReminder(ReminderRequest request) {
        Reminder reminder = new Reminder();
        reminder.setTitle(request.getTitle());
        reminder.setDescription(request.getDescription());
        reminder.setReminderDate(request.getReminderDate());
        reminder.setEmployeeId(request.getEmployeeId());

        Reminder saved = repository.save(reminder);
        ReminderResponse response = toResponse(saved);

        // Publish event for transaction-aware cache eviction
        eventPublisher.publishEvent(new com.example.ems.reminder.event.ReminderCreatedEvent(this, saved));
        return response;
    }

    /**
     * Updates an existing reminder, saves to DB first, then triggers async cache refresh.
     *
     * @throws RuntimeException if the reminder is not found.
     */
    @Transactional
    public ReminderResponse updateReminder(Long id, ReminderRequest request) {
        Reminder reminder = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));

        reminder.setTitle(request.getTitle());
        reminder.setDescription(request.getDescription());
        reminder.setReminderDate(request.getReminderDate());
        reminder.setEmployeeId(request.getEmployeeId());

        Reminder saved = repository.save(reminder);
        ReminderResponse response = toResponse(saved);

        // Publish event for transaction-aware cache eviction
        eventPublisher.publishEvent(new com.example.ems.reminder.event.ReminderUpdatedEvent(this, saved));
        return response;
    }

    /**
     * Deletes a reminder, removes from DB first, then evicts cache entries.
     *
     * @throws RuntimeException if the reminder is not found.
     */
    @Transactional
    public void deleteReminder(Long id) {
        Reminder reminder = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));

        repository.deleteById(id);

        // Publish event for transaction-aware cache eviction
        eventPublisher.publishEvent(new com.example.ems.reminder.event.ReminderDeletedEvent(this, reminder));
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private ReminderResponse toResponse(Reminder reminder) {
        ReminderResponse res = new ReminderResponse();
        res.setId(reminder.getId());
        res.setTitle(reminder.getTitle());
        res.setDescription(reminder.getDescription());
        res.setReminderDate(reminder.getReminderDate());
        res.setEmployeeId(reminder.getEmployeeId());
        res.setCreatedAt(reminder.getCreatedAt());
        res.setUpdatedAt(reminder.getUpdatedAt());
        return res;
    }
}
