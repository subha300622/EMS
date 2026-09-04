package com.example.ems.security.service;

import com.example.ems.auth.service.SessionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Autowired
    @Lazy
    private SessionService sessionService;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    private SecretKey key;

    // Access Token validity: 24 hours
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    @PostConstruct
    public void validateSecret() {
        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be configured and at least 32 bytes (256 bits) long.");
        }
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getSigningKey() {
        if (this.key == null) {
            if (jwtSecret != null && !jwtSecret.isBlank()) {
                this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            } else {
                throw new IllegalStateException("JWT_SECRET is not configured");
            }
        }
        return this.key;
    }

    public String generateAccessToken(String userId, String email, String role) {
        return generateAccessToken(userId, email, role, null, null, 1, 1L);
    }

    public String generateAccessToken(String userId, String email, String role, String sessionId) {
        return generateAccessToken(userId, email, role, null, sessionId, 1, 1L);
    }

    public String generateAccessToken(String userId, String email, String role, String sessionId, int sessionVersion, long sessionEpoch) {
        return generateAccessToken(userId, email, role, null, sessionId, sessionVersion, sessionEpoch);
    }

    public String generateAccessToken(String userId, String email, String role, Long orgId, String sessionId, int sessionVersion, long sessionEpoch) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        if (orgId != null) {
            claims.put("orgId", orgId);
        }
        if (sessionId != null) {
            claims.put("sessionId", sessionId);
            claims.put("sessionVersion", sessionVersion);
            claims.put("sessionEpoch", sessionEpoch);
        }

        claims.put("aud", "ems-backend");

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_MS))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            String sessionId = claims.get("sessionId", String.class);
            String userId = claims.get("userId", String.class);
            if (sessionId != null && userId != null) {
                if (sessionService != null) {
                    try {
                        return sessionService.isSessionActive(userId, sessionId);
                    } catch (Exception e) {
                        // Redis is unreachable on hosted deployment; fallback to JWT validation
                        return true;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getUserIdFromToken(String token) {
        return getClaims(token).get("userId", String.class);
    }

    public Long getOrgIdFromToken(String token) {
        Object orgId = getClaims(token).get("orgId");
        if (orgId == null) return null;
        if (orgId instanceof Number) {
            return ((Number) orgId).longValue();
        }
        return Long.valueOf(orgId.toString());
    }
}
