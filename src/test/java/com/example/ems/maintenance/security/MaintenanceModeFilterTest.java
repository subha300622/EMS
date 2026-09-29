package com.example.ems.maintenance.security;

import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.service.AuditLogService;
import com.example.ems.maintenance.dto.MaintenanceBlockedResponse;
import com.example.ems.maintenance.service.MaintenanceService;
import com.example.ems.security.MaintenanceModeFilter;
import com.example.ems.security.dto.AuthPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceModeFilterTest {

    @Mock
    private MaintenanceService maintenanceService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private FilterChain filterChain;

    private MaintenanceModeFilter filter;

    @BeforeEach
    void setUp() {
        filter = new MaintenanceModeFilter(maintenanceService, auditLogService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Excluded endpoints pass through unconditionally")
    void testExcludedEndpointPassesThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/public/maintenance");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(maintenanceService, never()).isMaintenanceActive();
    }

    @Test
    @DisplayName("Normal endpoints pass through when maintenance mode is inactive")
    void testInactiveMaintenancePassesThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/employees");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(maintenanceService.isMaintenanceActive()).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("Active maintenance blocks non-admin with 503 and MAINTENANCE_MODE JSON response")
    void testActiveMaintenanceBlocksNonAdmin() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/employees");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthPrincipal empPrincipal = new AuthPrincipal("EMP001", "s1", 1, 1L, "emp@corp.com", "EMPLOYEE");
        Authentication empAuth = new UsernamePasswordAuthenticationToken(empPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        SecurityContextHolder.getContext().setAuthentication(empAuth);

        when(maintenanceService.isMaintenanceActive()).thenReturn(true);
        when(maintenanceService.canAccessDuringMaintenance(empAuth)).thenReturn(false);
        when(maintenanceService.getBlockedResponse()).thenReturn(new MaintenanceBlockedResponse(
                "The EMS platform is currently under maintenance.",
                "Scheduled maintenance from 10 PM to 1 AM.",
                OffsetDateTime.parse("2026-09-26T22:00:00+05:30"),
                OffsetDateTime.parse("2026-09-27T01:00:00+05:30")
        ));

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(503, response.getStatus());
        assertTrue(response.getContentType().contains("application/json"));
        assertTrue(response.getContentAsString().contains("MAINTENANCE_MODE"));
        assertTrue(response.getContentAsString().contains("Scheduled maintenance from 10 PM to 1 AM."));
    }

    @Test
    @DisplayName("Active maintenance allows Platform Admin when bypass is active")
    void testActiveMaintenanceAllowsPlatformAdmin() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/platform-admin/organizations");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthPrincipal adminPrincipal = new AuthPrincipal("ADM001", "s2", 1, 1L, "admin@platform.com", "PLATFORM_ADMIN");
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(adminPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        when(maintenanceService.isMaintenanceActive()).thenReturn(true);
        when(maintenanceService.canAccessDuringMaintenance(adminAuth)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(auditLogService, times(1)).logAction(
                any(), any(), eq(AuditAction.MAINTENANCE_ADMIN_ACCESS_USED.name()),
                eq("PLATFORM_MAINTENANCE"), any(), any(), any()
        );
    }
}
