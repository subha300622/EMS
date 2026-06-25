package com.example.ems.security;

import com.example.ems.auth.service.SafeRedisService;
import com.example.ems.security.service.JwtService;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(2)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Autowired
    private SafeRedisService safeRedisService;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Exclude auth-related endpoints and Swagger UI resources from rate limiting
        if (path.contains("/api/v1/auth/") || 
            path.contains("/swagger-ui") || 
            path.contains("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        
        String email = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtService.validateAccessToken(token)) {
                    email = jwtService.getEmailFromToken(token);
                }
            } catch (Exception e) {
            }
        }

        String targetIdentifier = (email != null) ? email : clientIp;
        String redisKey = "rate:limit:" + targetIdentifier;

        Long currentCount = safeRedisService.increment(redisKey);
        
        if (currentCount == null) {
            log.warn("Redis is unavailable. Rate limiting is degraded to soft mode (request allowed) for identifier: {}", targetIdentifier);
            filterChain.doFilter(request, response);
            return;
        }

        if (currentCount == 1) {
            safeRedisService.expire(redisKey, Duration.ofSeconds(60));
        }

        if (currentCount > 100) {
            response.setStatus(429);
            response.setContentType("application/json");
            
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", "error");
            errorDetails.put("message", "Too many requests. Please try again later.");
            
            Map<String, String> errorObj = new HashMap<>();
            errorObj.put("code", "RATE_LIMIT_EXCEEDED");
            errorObj.put("message", "Rate limit of 100 requests per minute exceeded.");
            errorDetails.put("error", errorObj);

            errorDetails.put("errorCode", "RATE_LIMIT_EXCEEDED");

            response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
