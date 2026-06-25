package com.example.ems.security.service;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtVerificationService {

    @Autowired
    private JwtService jwtService;

    public Claims verifyAndExtractClaims(String token) {
        Claims claims = jwtService.getClaims(token);
        java.util.Set<String> audience = claims.getAudience();
        if (audience == null || !audience.contains("ems-backend")) {
            throw new io.jsonwebtoken.security.SignatureException("Invalid token audience");
        }
        return claims;
    }
}
