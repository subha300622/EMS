package com.example.ems.security.service;

import com.example.ems.auth.entity.UserSession;
import com.example.ems.security.dto.AuthAuthenticationToken;
import com.example.ems.security.dto.AuthDecisionTrace;
import com.example.ems.security.dto.AuthPrincipal;
import com.example.ems.security.dto.AuthenticationOutcome;
import com.example.ems.security.service.SecurityTelemetryPublisher.TelemetryEvent;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationDecisionService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationDecisionService.class);

    @Autowired
    private JwtVerificationService jwtVerificationService;

    @Autowired
    private SessionAuthorityService sessionAuthorityService;

    @Autowired
    private SecurityTelemetryPublisher telemetryPublisher;

    public AuthPrincipal authenticateToken(String rawToken) {
        long startTime = System.currentTimeMillis();

        // 1. Idempotency guard: prevent double execution
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        if (existingAuth instanceof AuthAuthenticationToken && existingAuth.isAuthenticated()) {
            Object principalObj = existingAuth.getPrincipal();
            if (principalObj instanceof AuthPrincipal) {
                log.debug("AuthenticationDecisionService: Returning cached authentication principal.");
                return (AuthPrincipal) principalObj;
            }
        }

        String userId = "UNKNOWN";
        String sessionId = "UNKNOWN";
        int version = 0;
        long epoch = 0;

        try {
            // 2. Parse and verify JWT signature and expiry (Fail Closed)
            Claims claims;
            try {
                claims = jwtVerificationService.verifyAndExtractClaims(rawToken);
            } catch (Exception e) {
                long latency = System.currentTimeMillis() - startTime;
                AuthDecisionTrace trace = new AuthDecisionTrace(
                        userId, sessionId, epoch, version,
                        AuthenticationOutcome.FAIL_CLOSED, "JWT Verification Exception: " + e.getMessage(), latency
                );
                telemetryPublisher.publish(TelemetryEvent.TOKEN_REJECTED, trace);
                throw new BadCredentialsException("Token verification failed", e);
            }

            userId = claims.get("userId", String.class);
            sessionId = claims.get("sessionId", String.class);
            Object verVal = claims.get("sessionVersion");
            Object epVal = claims.get("sessionEpoch");

            version = 1;
            if (verVal instanceof Number) {
                version = ((Number) verVal).intValue();
            }
            epoch = 1L;
            if (epVal instanceof Number) {
                epoch = ((Number) epVal).longValue();
            }

            // 3. Database Pessimistic Lock validation check (Fail Closed)
            Optional<UserSession> sessionOpt = sessionAuthorityService.validateClaimsAndGetSession(claims);
            if (sessionOpt.isEmpty()) {
                long latency = System.currentTimeMillis() - startTime;
                AuthDecisionTrace trace = new AuthDecisionTrace(
                        userId, sessionId, epoch, version,
                        AuthenticationOutcome.FAIL_CLOSED_LOGGED, "Active session validation failed in DB", latency
                );
                telemetryPublisher.publish(TelemetryEvent.LOGIN_FAIL, trace);
                throw new BadCredentialsException("Session is invalid, rotated, or revoked");
            }

            UserSession session = sessionOpt.get();

            // 4. Construct principal and trace success
            AuthPrincipal principal = new AuthPrincipal(
                    session.getUserId(),
                    session.getSessionId(),
                    session.getSessionVersion(),
                    session.getSessionEpoch(),
                    session.getEmail(),
                    claims.get("role", String.class)
            );

            long latency = System.currentTimeMillis() - startTime;
            AuthDecisionTrace trace = new AuthDecisionTrace(
                    principal.getUserId(), principal.getSessionId(), principal.getSessionEpoch(), principal.getSessionVersion(),
                    AuthenticationOutcome.SUCCESS, "Authentication successful", latency
            );
            telemetryPublisher.publish(TelemetryEvent.LOGIN_SUCCESS, trace);

            return principal;

        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            // Fail closed everywhere: catch any unexpected exceptions
            long latency = System.currentTimeMillis() - startTime;
            AuthDecisionTrace trace = new AuthDecisionTrace(
                    userId, sessionId, epoch, version,
                    AuthenticationOutcome.FAIL_CLOSED, "System Exception: " + e.getMessage(), latency
            );
            telemetryPublisher.publish(TelemetryEvent.TOKEN_REJECTED, trace);
            throw new BadCredentialsException("Authentication failed closed due to system error", e);
        }
    }
}
