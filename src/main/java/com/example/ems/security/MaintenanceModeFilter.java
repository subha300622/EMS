package com.example.ems.security;

import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.service.AuditLogService;
import com.example.ems.maintenance.dto.MaintenanceBlockedResponse;
import com.example.ems.maintenance.service.MaintenanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Security filter that enforces Maintenance Mode for all non-exempt requests.
 *
 * <p>Placed in {@code com.example.ems.security} so that access to
 * {@code SecurityContextHolder} satisfies the project's ArchUnit rule
 * restricting such access to the security layer.</p>
 *
 * <p>Runs immediately after {@link JwtAuthenticationFilter} in the Spring Security
 * filter chain so that the authenticated principal (and its role) is already
 * available in the {@code SecurityContextHolder} when this filter is evaluated.</p>
 */
public class MaintenanceModeFilter extends OncePerRequestFilter {

    private final MaintenanceService maintenanceService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> EXCLUDED_PATTERNS = List.of(
            "/api/v1/platform/maintenance/**",
            "/api/v1/platform/maintenance",
            "/api/v1/public/maintenance/**",
            "/api/v1/public/maintenance",
            "/actuator/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/api/v1/auth/login");

    public MaintenanceModeFilter(MaintenanceService maintenanceService, AuditLogService auditLogService) {
        this.maintenanceService = maintenanceService;
        this.auditLogService = auditLogService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // 1. Check if the endpoint is excluded
        for (String pattern : EXCLUDED_PATTERNS) {
            if (pathMatcher.match(pattern, requestUri)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 2. Check if maintenance mode is active
        if (!maintenanceService.isMaintenanceActive()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Check if current authenticated user has platform admin bypass access.
        //    SecurityContextHolder access is valid here because this class resides in
        //    com.example.ems.security, satisfying the project's ArchUnit restriction.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (maintenanceService.canAccessDuringMaintenance(authentication)) {
            recordAdminBypassAudit(authentication, request);
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Block with 503 Service Unavailable
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        MaintenanceBlockedResponse blocked = maintenanceService.getBlockedResponse();
        response.getWriter().write(objectMapper.writeValueAsString(blocked));
    }

    private void recordAdminBypassAudit(Authentication authentication, HttpServletRequest request) {
        if (auditLogService != null && authentication != null) {
            try {
                String actor = authentication.getName();
                auditLogService.logAction(
                        actor != null ? actor : "platform-admin",
                        actor != null ? actor : "platform-admin",
                        AuditAction.MAINTENANCE_ADMIN_ACCESS_USED.name(),
                        "PLATFORM_MAINTENANCE",
                        "1",
                        request.getRemoteAddr(),
                        "Platform admin accessed " + request.getMethod() + " " + request.getRequestURI() + " during active maintenance"
                );
            } catch (Exception ignored) {
            }
        }
    }
}
