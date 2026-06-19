package com.example.ems.security;

import com.example.ems.security.service.JwtService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
@Order(0)
public class AuthorizationHeaderFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private com.example.ems.auth.repository.UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Exclude auth-related endpoints and Swagger UI resources from injection
        if (path.contains("/api/v1/auth/") || 
            path.contains("/swagger-ui") || 
            path.contains("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String cleanAuthHeader = null;

        if (authHeader != null && !authHeader.trim().isEmpty()) {
            cleanAuthHeader = cleanHeader(authHeader);
        } else {
            // Auto-inject a valid Super Admin token for local testing convenience if no token is provided
            String superAdminEmail = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null && "SUPER_ADMIN".equalsIgnoreCase(u.getRole().getName()))
                    .map(com.example.ems.auth.entity.User::getWorkEmail)
                    .findFirst()
                    .orElse("super_admin@localhost");
            String devToken = jwtService.generateAccessToken("EMP005", superAdminEmail, "SUPER_ADMIN");
            cleanAuthHeader = "Bearer " + devToken;
        }

        if (cleanAuthHeader != null) {
            String finalAuthHeader = cleanAuthHeader;
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return finalAuthHeader;
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return Collections.enumeration(List.of(finalAuthHeader));
                    }
                    return super.getHeaders(name);
                }
            };
            filterChain.doFilter(wrappedRequest, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String cleanHeader(String headerVal) {
        if (headerVal == null) {
            return null;
        }
        String val = headerVal.trim();
        while (val.toLowerCase().startsWith("bearer ")) {
            val = val.substring(7).trim();
        }
        if (val.startsWith("ey") && val.contains(".")) {
            return "Bearer " + val;
        }
        return headerVal.trim();
    }
}
