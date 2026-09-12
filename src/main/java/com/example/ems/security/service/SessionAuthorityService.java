package com.example.ems.security.service;

import com.example.ems.auth.entity.UserSession;
import com.example.ems.auth.service.DatabaseSessionStore;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SessionAuthorityService {

    @Autowired
    private DatabaseSessionStore databaseSessionStore;

    /**
     * Validates claims against user session in database with concurrency-safe pessimistic row locking.
     */
    @Transactional
    public Optional<UserSession> validateClaimsAndGetSession(Claims claims) {
        try {
            String sessionId = claims.get("sessionId", String.class);
            if (sessionId == null) {
                return Optional.empty();
            }

            Object versionVal = claims.get("sessionVersion");
            Object epochVal = claims.get("sessionEpoch");

            int sessionVersion = 1;
            if (versionVal instanceof Number) {
                sessionVersion = ((Number) versionVal).intValue();
            }
            long sessionEpoch = 1L;
            if (epochVal instanceof Number) {
                sessionEpoch = ((Number) epochVal).longValue();
            }

            // Query database atomically with Pessimistic Locking FOR UPDATE
            Optional<UserSession> sessionOpt = databaseSessionStore.findBySessionIdAndStatusAndSessionVersionAndSessionEpoch(
                    sessionId, "ACTIVE", sessionVersion, sessionEpoch
            );

            if (sessionOpt.isEmpty()) {
                return Optional.empty();
            }

            UserSession session = sessionOpt.get();

            // Strict time-based freeze guard (grace window check if needed)
            if (session.isRevoked() || session.getRevokedAt() != null) {
                return Optional.empty();
            }

            if (LocalDateTime.now().isAfter(session.getExpiryTime())) {
                return Optional.empty();
            }

            return Optional.of(session);

        } catch (Exception e) {
            // Fail closed: return empty if any exception occurs (e.g. database connection failure)
            System.err.println("Session validation exception: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
