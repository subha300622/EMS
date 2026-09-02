package com.example.ems.security;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.util.OrganizationIdResolver;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.dto.AuthAuthenticationToken;
import com.example.ems.security.dto.AuthPrincipal;
import com.example.ems.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final Environment environment;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   AuthenticationEntryPoint authenticationEntryPoint,
                                   Environment environment) {
        this(authenticationManager, authenticationEntryPoint, environment, null, null);
    }

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   AuthenticationEntryPoint authenticationEntryPoint,
                                   Environment environment,
                                   JwtService jwtService,
                                   UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.environment = environment;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Long headerOrgId = resolveOrganizationId(request);
        String token = extractToken(request);

        try {
            if (token != null) {
                // Build unauthenticated token
                AuthAuthenticationToken unauthenticatedToken = new AuthAuthenticationToken(token);

                // Delegate to AuthenticationManager
                Authentication authenticated = authenticationManager.authenticate(unauthenticatedToken);

                // Set SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authenticated);

                // Determine user role and authorized organization
                String role = null;
                Long userOrgId = null;

                if (authenticated != null && authenticated.getPrincipal() instanceof AuthPrincipal principal) {
                    role = principal.getRole();
                }

                if (jwtService != null) {
                    try {
                        userOrgId = jwtService.getOrgIdFromToken(token);
                    } catch (Exception ignored) {
                    }
                }

                if (userOrgId == null && authenticated != null && authenticated.getPrincipal() instanceof AuthPrincipal principal && userRepository != null) {
                    Optional<User> userOpt = Optional.empty();
                    if (principal.getUserId() != null) {
                        userOpt = userRepository.findByUserId(principal.getUserId());
                    }
                    if (userOpt.isEmpty() && principal.getEmail() != null) {
                        userOpt = userRepository.findByWorkEmail(principal.getEmail());
                    }
                    if (userOpt.isPresent()) {
                        User u = userOpt.get();
                        userOrgId = u.getOrganization() != null ? u.getOrganization().getId() : u.getOrganizationId();
                        if (role == null && u.getRole() != null) {
                            role = u.getRole().getName();
                        }
                    }
                }

                String method = request.getMethod();
                boolean isWriteMethod = "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) 
                                     || "DELETE".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method);
                String path = request.getRequestURI();

                if (isWriteMethod && headerOrgId == null && !path.startsWith("/api/v1/auth") 
                        && !path.startsWith("/v3/api-docs") && !path.startsWith("/swagger-ui")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"errorCode\":\"BAD_REQUEST\",\"message\":\"Missing required header 'X-Organization-Id' for " + method + " operation.\"}");
                    return;
                }

                boolean isPlatformAdmin = "PLATFORM_ADMIN".equalsIgnoreCase(role);

                if (isPlatformAdmin) {
                    // Platform Admin: Platform-level scope. Can explicitly target a tenant context via header.
                    if (headerOrgId != null) {
                        TenantContext.setCurrentTenant(headerOrgId);
                    }
                } else {
                    // Tenant User (SUPER_ADMIN, ADMIN, etc.): Bound strictly to userOrgId from JWT
                    if (headerOrgId != null && userOrgId != null && !headerOrgId.equals(userOrgId)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\":false,\"errorCode\":\"AUTH_003\",\"message\":\"Cross-tenant access forbidden: Header organization ID (" + headerOrgId + ") does not match user's authorized organization (" + userOrgId + ").\"}");
                        return;
                    }
                    Long effectiveOrgId = (userOrgId != null) ? userOrgId : headerOrgId;
                    if (effectiveOrgId != null) {
                        TenantContext.setCurrentTenant(effectiveOrgId);
                    }
                }
            } else if (headerOrgId != null) {
                // If no token is provided, set header org if present (for unauthenticated multi-tenant public flows if any)
                TenantContext.setCurrentTenant(headerOrgId);
            }

            filterChain.doFilter(request, response);

        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, e);
        } finally {
            TenantContext.clear();
        }
    }

    private Long resolveOrganizationId(HttpServletRequest request) {
        String orgHeader = request.getHeader("X-Organization-Id");
        if (orgHeader == null || orgHeader.isBlank()) {
            orgHeader = request.getHeader("X-Tenant-Id");
        }
        if (orgHeader == null || orgHeader.isBlank()) {
            orgHeader = request.getHeader("organization-id");
        }
        if (orgHeader != null && !orgHeader.isBlank()) {
            try {
                return OrganizationIdResolver.parseId(orgHeader.trim());
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }

        // Support passing the JWT token as a query parameter for browser downloads / img tags (exclude public auth endpoints)
        String requestUri = request.getRequestURI();
        if (requestUri == null || !requestUri.startsWith("/api/v1/auth/")) {
            String tokenParam = request.getParameter("token");
            if (tokenParam != null && !tokenParam.trim().isEmpty()) {
                return tokenParam.trim();
            }
        }

        // Developer token bypass for non-prod environments
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        boolean isProduction = activeProfiles.contains("prod") || activeProfiles.contains("production");

        if (!isProduction) {
            String devHeader = request.getHeader("X-DEV-TOKEN");
            if (devHeader != null && !devHeader.trim().isEmpty()) {
                return devHeader.trim();
            }
        }

        return null;
    }
}
