package com.example.ems.performance.service;

import com.example.ems.common.exception.ConflictException;
import com.example.ems.organization.entity.Organization;
import com.example.ems.performance.entity.PerformanceCommandIdempotency;
import com.example.ems.performance.repository.PerformanceCommandIdempotencyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class PerformanceIdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceIdempotencyService.class);
    private static final int STATUS_PROCESSING = 102;
    private static final int STATUS_COMPLETED = 200;

    @Autowired
    private PerformanceCommandIdempotencyRepository idempotencyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public String computeRequestHash(Object requestPayload) {
        if (requestPayload == null) return "EMPTY_PAYLOAD";
        try {
            String json = objectMapper.writeValueAsString(requestPayload);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            return String.valueOf(requestPayload.hashCode());
        }
    }

    /**
     * Executes the supplier within an idempotency guard.
     * Concurrency-safe: claims the key via unique insert.
     */
    public <T> T executeWithIdempotency(Organization organization, String idempotencyKey, String commandType,
                                        Object requestPayload, Long resourceId, Class<T> responseType,
                                        Supplier<T> commandExecution) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return commandExecution.get();
        }

        Long orgId = organization.getId();
        String reqHash = computeRequestHash(requestPayload);

        // 1. Check if already exists
        Optional<PerformanceCommandIdempotency> existing = idempotencyRepository.findByOrganizationIdAndIdempotencyKey(orgId, idempotencyKey);
        if (existing.isPresent()) {
            return handleExistingRecord(existing.get(), reqHash, responseType);
        }

        // 2. Concurrency-safe claim via insert
        PerformanceCommandIdempotency claim = new PerformanceCommandIdempotency();
        claim.setOrganization(organization);
        claim.setIdempotencyKey(idempotencyKey);
        claim.setCommandType(commandType);
        claim.setRequestHash(reqHash);
        claim.setResourceId(resourceId);
        claim.setResponseStatus(STATUS_PROCESSING);
        claim.setCreatedAt(LocalDateTime.now());

        PerformanceCommandIdempotency savedClaim;
        try {
            savedClaim = idempotencyRepository.saveAndFlush(claim);
        } catch (DataIntegrityViolationException e) {
            // Concurrent request won the race
            PerformanceCommandIdempotency winner = idempotencyRepository.findByOrganizationIdAndIdempotencyKey(orgId, idempotencyKey)
                    .orElseThrow(() -> new ConflictException("Concurrent request in progress for idempotency key: " + idempotencyKey));
            return handleExistingRecord(winner, reqHash, responseType);
        }

        // 3. Execute the command
        T result = commandExecution.get();

        // 4. Record completion payload
        try {
            String payloadJson = objectMapper.writeValueAsString(result);
            savedClaim.setResponseStatus(STATUS_COMPLETED);
            savedClaim.setResponsePayload(payloadJson);
            savedClaim.setCompletedAt(LocalDateTime.now());
            idempotencyRepository.save(savedClaim);
        } catch (Exception e) {
            log.warn("Failed to persist idempotency completion payload for key {}: {}", idempotencyKey, e.getMessage());
        }

        return result;
    }

    private <T> T handleExistingRecord(PerformanceCommandIdempotency record, String reqHash, Class<T> responseType) {
        if (!record.getRequestHash().equals(reqHash)) {
            throw new ConflictException("IDEMPOTENCY_KEY_REUSED: Idempotency key '" + record.getIdempotencyKey()
                    + "' was previously used with a different request payload.");
        }

        if (record.getResponseStatus() == STATUS_PROCESSING || record.getCompletedAt() == null) {
            throw new ConflictException("IDEMPOTENCY_IN_PROGRESS: Operation with key '" + record.getIdempotencyKey()
                    + "' is currently in progress. Please retry shortly.");
        }

        try {
            return objectMapper.readValue(record.getResponsePayload(), responseType);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize cached idempotency response", e);
        }
    }
}
