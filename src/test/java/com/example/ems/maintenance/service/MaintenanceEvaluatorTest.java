package com.example.ems.maintenance.service;

import com.example.ems.maintenance.dto.MaintenanceEnableRequest;
import com.example.ems.maintenance.dto.MaintenanceUpdateRequest;
import com.example.ems.maintenance.entity.MaintenanceConfig;
import com.example.ems.maintenance.entity.MaintenanceStatus;
import com.example.ems.security.dto.AuthPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceEvaluatorTest {

    private MaintenanceEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new MaintenanceEvaluator();
    }

    @Test
    @DisplayName("Disabled maintenance returns MAINTENANCE_DISABLED and inactive")
    void testDisabledMaintenance() {
        MaintenanceConfig config = new MaintenanceConfig();
        config.setEnabled(false);

        OffsetDateTime now = OffsetDateTime.now();
        assertEquals(MaintenanceStatus.MAINTENANCE_DISABLED, evaluator.calculateStatus(config, now));
        assertFalse(evaluator.isMaintenanceActive(config, now));
    }

    @Test
    @DisplayName("Enabled maintenance without dates returns MAINTENANCE_ENABLED and active")
    void testEnabledIndefiniteMaintenance() {
        MaintenanceConfig config = new MaintenanceConfig();
        config.setEnabled(true);

        OffsetDateTime now = OffsetDateTime.now();
        assertEquals(MaintenanceStatus.MAINTENANCE_ENABLED, evaluator.calculateStatus(config, now));
        assertTrue(evaluator.isMaintenanceActive(config, now));
    }

    @Test
    @DisplayName("Future scheduled maintenance returns MAINTENANCE_SCHEDULED and inactive")
    void testScheduledMaintenanceInFuture() {
        OffsetDateTime now = OffsetDateTime.now();
        MaintenanceConfig config = new MaintenanceConfig();
        config.setEnabled(true);
        config.setStartAt(now.plusHours(2));
        config.setEndAt(now.plusHours(5));

        assertEquals(MaintenanceStatus.MAINTENANCE_SCHEDULED, evaluator.calculateStatus(config, now));
        assertFalse(evaluator.isMaintenanceActive(config, now));
    }

    @Test
    @DisplayName("Active window between startAt and endAt returns MAINTENANCE_ENABLED and active")
    void testActiveMaintenanceWindow() {
        OffsetDateTime now = OffsetDateTime.now();
        MaintenanceConfig config = new MaintenanceConfig();
        config.setEnabled(true);
        config.setStartAt(now.minusHours(1));
        config.setEndAt(now.plusHours(2));

        assertEquals(MaintenanceStatus.MAINTENANCE_ENABLED, evaluator.calculateStatus(config, now));
        assertTrue(evaluator.isMaintenanceActive(config, now));
    }

    @Test
    @DisplayName("Past window beyond endAt returns MAINTENANCE_EXPIRED and inactive")
    void testExpiredMaintenanceWindow() {
        OffsetDateTime now = OffsetDateTime.now();
        MaintenanceConfig config = new MaintenanceConfig();
        config.setEnabled(true);
        config.setStartAt(now.minusHours(5));
        config.setEndAt(now.minusHours(1));

        assertEquals(MaintenanceStatus.MAINTENANCE_EXPIRED, evaluator.calculateStatus(config, now));
        assertFalse(evaluator.isMaintenanceActive(config, now));
    }

    @Test
    @DisplayName("Scenario 1: Maintenance OFF -> all users allowed")
    void testScenario1_MaintenanceOff() {
        MaintenanceConfig config = new MaintenanceConfig();
        config.setEnabled(false);
        config.setAllowAdminAccess(true);

        OffsetDateTime now = OffsetDateTime.now();

        // Normal Employee
        AuthPrincipal empPrincipal = new AuthPrincipal("EMP001", "s1", 1, 1L, "emp@corp.com", "EMPLOYEE");
        Authentication empAuth = new UsernamePasswordAuthenticationToken(empPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        assertTrue(evaluator.canAccessDuringMaintenance(config, empAuth, now));

        // Platform Admin
        AuthPrincipal adminPrincipal = new AuthPrincipal("ADM001", "s2", 1, 1L, "admin@platform.com", "PLATFORM_ADMIN");
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(adminPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN")));
        assertTrue(evaluator.canAccessDuringMaintenance(config, adminAuth, now));
    }

    @Test
    @DisplayName("Scenario 2: Maintenance ON + Allow Admin Access ON -> Platform Admin allowed, tenant roles blocked")
    void testScenario2_MaintenanceOn_AllowAdminOn() {
        MaintenanceConfig config = new MaintenanceConfig();
        config.setEnabled(true);
        config.setAllowAdminAccess(true);

        OffsetDateTime now = OffsetDateTime.now();

        // Platform Admin -> ALLOW
        AuthPrincipal adminPrincipal = new AuthPrincipal("ADM001", "s2", 1, 1L, "admin@platform.com", "PLATFORM_ADMIN");
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(adminPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN")));
        assertTrue(evaluator.canAccessDuringMaintenance(config, adminAuth, now));

        // Tenant SUPER_ADMIN -> BLOCK
        AuthPrincipal tenantSuperAdmin = new AuthPrincipal("SUP001", "s3", 1, 1L, "super@tenant.com", "SUPER_ADMIN");
        Authentication tenantAuth = new UsernamePasswordAuthenticationToken(tenantSuperAdmin, null, List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
        assertFalse(evaluator.canAccessDuringMaintenance(config, tenantAuth, now));

        // Tenant Employee -> BLOCK
        AuthPrincipal empPrincipal = new AuthPrincipal("EMP001", "s1", 1, 1L, "emp@corp.com", "EMPLOYEE");
        Authentication empAuth = new UsernamePasswordAuthenticationToken(empPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        assertFalse(evaluator.canAccessDuringMaintenance(config, empAuth, now));

        // Unauthenticated -> BLOCK
        assertFalse(evaluator.canAccessDuringMaintenance(config, null, now));
    }

    @Test
    @DisplayName("Scenario 3: Maintenance ON + Allow Admin Access OFF -> All users blocked including Platform Admin")
    void testScenario3_MaintenanceOn_AllowAdminOff() {
        MaintenanceConfig config = new MaintenanceConfig();
        config.setEnabled(true);
        config.setAllowAdminAccess(false);

        OffsetDateTime now = OffsetDateTime.now();

        // Platform Admin -> BLOCK
        AuthPrincipal adminPrincipal = new AuthPrincipal("ADM001", "s2", 1, 1L, "admin@platform.com", "PLATFORM_ADMIN");
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(adminPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN")));
        assertFalse(evaluator.canAccessDuringMaintenance(config, adminAuth, now));

        // Employee -> BLOCK
        AuthPrincipal empPrincipal = new AuthPrincipal("EMP001", "s1", 1, 1L, "emp@corp.com", "EMPLOYEE");
        Authentication empAuth = new UsernamePasswordAuthenticationToken(empPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        assertFalse(evaluator.canAccessDuringMaintenance(config, empAuth, now));
    }

    @Test
    @DisplayName("Validation fails when endAt is before or equal to startAt")
    void testValidationEndAtBeforeStartAt() {
        OffsetDateTime now = OffsetDateTime.now();

        MaintenanceUpdateRequest updateReq = new MaintenanceUpdateRequest(
                true, true, "Maintenance in progress",
                now.plusHours(3), now.plusHours(1), false
        );
        assertThrows(IllegalArgumentException.class, () -> evaluator.validateUpdateRequest(updateReq));

        MaintenanceEnableRequest enableReq = new MaintenanceEnableRequest(
                "Maintenance in progress", now.plusHours(3), now.plusHours(1), true, false
        );
        assertThrows(IllegalArgumentException.class, () -> evaluator.validateEnableRequest(enableReq));
    }

    @Test
    @DisplayName("Validation fails when message is blank on enabled maintenance")
    void testValidationBlankMessage() {
        MaintenanceUpdateRequest updateReq = new MaintenanceUpdateRequest(
                true, true, "   ", null, null, false
        );
        assertThrows(IllegalArgumentException.class, () -> evaluator.validateUpdateRequest(updateReq));

        MaintenanceEnableRequest enableReq = new MaintenanceEnableRequest(
                "", null, null, true, false
        );
        assertThrows(IllegalArgumentException.class, () -> evaluator.validateEnableRequest(enableReq));
    }

    @Test
    @DisplayName("Boundary condition: startAt == now -> maintenance becomes immediately active")
    void testBoundaryStartAtEqualsNow() {
        OffsetDateTime now = OffsetDateTime.now();
        MaintenanceConfig config = new MaintenanceConfig();
        config.setEnabled(true);
        config.setStartAt(now);
        config.setEndAt(now.plusHours(2));

        assertEquals(MaintenanceStatus.MAINTENANCE_ENABLED, evaluator.calculateStatus(config, now));
        assertTrue(evaluator.isMaintenanceActive(config, now));
    }

    @Test
    @DisplayName("Boundary condition: endAt == now -> maintenance becomes inactive/expired")
    void testBoundaryEndAtEqualsNow() {
        OffsetDateTime now = OffsetDateTime.now();
        MaintenanceConfig config = new MaintenanceConfig();
        config.setEnabled(true);
        config.setStartAt(now.minusHours(2));
        config.setEndAt(now);

        assertEquals(MaintenanceStatus.MAINTENANCE_EXPIRED, evaluator.calculateStatus(config, now));
        assertFalse(evaluator.isMaintenanceActive(config, now));
    }

    @Test
    @DisplayName("Schedule crossing midnight activates and deactivates correctly across date boundaries")
    void testScheduleCrossingMidnight() {
        OffsetDateTime startAt = OffsetDateTime.parse("2026-09-26T22:00:00+05:30");
        OffsetDateTime endAt = OffsetDateTime.parse("2026-09-27T02:00:00+05:30");

        MaintenanceConfig config = new MaintenanceConfig();
        config.setEnabled(true);
        config.setStartAt(startAt);
        config.setEndAt(endAt);

        // Before window (21:59:59)
        OffsetDateTime beforeWindow = OffsetDateTime.parse("2026-09-26T21:59:59+05:30");
        assertEquals(MaintenanceStatus.MAINTENANCE_SCHEDULED, evaluator.calculateStatus(config, beforeWindow));
        assertFalse(evaluator.isMaintenanceActive(config, beforeWindow));

        // Exact start (22:00:00)
        assertEquals(MaintenanceStatus.MAINTENANCE_ENABLED, evaluator.calculateStatus(config, startAt));
        assertTrue(evaluator.isMaintenanceActive(config, startAt));

        // Midnight crossover (00:00:00 next day)
        OffsetDateTime midnight = OffsetDateTime.parse("2026-09-27T00:00:00+05:30");
        assertEquals(MaintenanceStatus.MAINTENANCE_ENABLED, evaluator.calculateStatus(config, midnight));
        assertTrue(evaluator.isMaintenanceActive(config, midnight));

        // Inside window (01:59:59)
        OffsetDateTime insideWindow = OffsetDateTime.parse("2026-09-27T01:59:59+05:30");
        assertEquals(MaintenanceStatus.MAINTENANCE_ENABLED, evaluator.calculateStatus(config, insideWindow));
        assertTrue(evaluator.isMaintenanceActive(config, insideWindow));

        // Exact end (02:00:00)
        assertEquals(MaintenanceStatus.MAINTENANCE_EXPIRED, evaluator.calculateStatus(config, endAt));
        assertFalse(evaluator.isMaintenanceActive(config, endAt));

        // After window (02:00:01)
        OffsetDateTime afterWindow = OffsetDateTime.parse("2026-09-27T02:00:01+05:30");
        assertEquals(MaintenanceStatus.MAINTENANCE_EXPIRED, evaluator.calculateStatus(config, afterWindow));
        assertFalse(evaluator.isMaintenanceActive(config, afterWindow));
    }

    @Test
    @DisplayName("Expired maintenance window allows normal tenant access to resume")
    void testExpiredMaintenanceWindow_NormalAccessAllowed() {
        OffsetDateTime now = OffsetDateTime.now();
        MaintenanceConfig config = new MaintenanceConfig();
        config.setEnabled(true);
        config.setAllowAdminAccess(false);
        config.setStartAt(now.minusHours(4));
        config.setEndAt(now.minusHours(1));

        AuthPrincipal empPrincipal = new AuthPrincipal("EMP001", "s1", 1, 1L, "emp@corp.com", "EMPLOYEE");
        Authentication empAuth = new UsernamePasswordAuthenticationToken(empPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));

        assertTrue(evaluator.canAccessDuringMaintenance(config, empAuth, now));
    }
}
