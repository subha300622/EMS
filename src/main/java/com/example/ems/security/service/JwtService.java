package com.example.ems.security.service;

import com.example.ems.auth.service.SessionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
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

    // A secure 256-bit key for HMAC-SHA
    private static final String SECRET_STRING = "jwtSecretKeyForEmsBackendDevelopmentShouldBeLongAndSecure32Bytes!";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    // Access Token validity: 24 hours
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    public String generateAccessToken(String userId, String email, String role) {
        return generateAccessToken(userId, email, role, null);
    }

    public String generateAccessToken(String userId, String email, String role, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        if (sessionId != null) {
            claims.put("sessionId", sessionId);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            String sessionId = claims.get("sessionId", String.class);
            String userId = claims.get("userId", String.class);
            if (sessionId != null && userId != null) {
                if (sessionService != null) {
                    return sessionService.isSessionActive(userId, sessionId);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
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
}
