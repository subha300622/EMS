package com.example.ems.maintenance.service;

import com.example.ems.maintenance.dto.MaintenanceEnableRequest;
import com.example.ems.maintenance.dto.MaintenanceResponse;
import com.example.ems.maintenance.dto.MaintenanceUpdateRequest;
import com.example.ems.maintenance.dto.PublicMaintenanceResponse;
import com.example.ems.maintenance.entity.MaintenanceConfig;
import com.example.ems.maintenance.event.MaintenanceDisabledEvent;
import com.example.ems.maintenance.event.MaintenanceEnabledEvent;
import com.example.ems.maintenance.repository.MaintenanceConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private MaintenanceConfigRepository repository;

    @Mock
    private MaintenanceSessionService sessionService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private MaintenanceEvaluator evaluator;
    private MaintenanceServiceImpl maintenanceService;

    @BeforeEach
    void setUp() {
        evaluator = new MaintenanceEvaluator();
        maintenanceService = new MaintenanceServiceImpl(repository, evaluator, sessionService, eventPublisher);
    }

    @Test
    @DisplayName("getSettings returns correct status and effective flag")
    void testGetSettings() {
        MaintenanceConfig config = new MaintenanceConfig(
                1L, true, true, "Platform update in progress",
                null, null, false, OffsetDateTime.now(), "platform-admin"
        );
        when(repository.findDefaultConfig()).thenReturn(Optional.of(config));

        MaintenanceResponse response = maintenanceService.getSettings();
        assertTrue(response.isEnabled());
        assertTrue(response.isEffective());
        assertEquals("MAINTENANCE_ENABLED", response.getStatus());
        assertEquals("Platform update in progress", response.getMessage());
    }

    @Test
    @DisplayName("enable immediately activates maintenance and triggers session logout when requested")
    void testEnableWithSessionLogout() {
        MaintenanceConfig config = new MaintenanceConfig(
                1L, false, true, "Old message",
                null, null, false, OffsetDateTime.now(), "SYSTEM"
        );
        when(repository.findDefaultConfig()).thenReturn(Optional.of(config));
        when(repository.save(any(MaintenanceConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MaintenanceEnableRequest request = new MaintenanceEnableRequest(
                "Maintenance under way", null, null, true, true
        );

        MaintenanceResponse response = maintenanceService.enable(request);

        assertTrue(response.isEnabled());
        assertTrue(response.isEffective());
        assertTrue(response.isLogoutActiveSessions());
        assertEquals("Maintenance under way", response.getMessage());

        verify(sessionService, times(1)).invalidateActiveSessions();
        verify(eventPublisher, times(1)).publishEvent(any(MaintenanceEnabledEvent.class));
    }

    @Test
    @DisplayName("disable turns off maintenance and publishes MaintenanceDisabledEvent")
    void testDisable() {
        MaintenanceConfig config = new MaintenanceConfig(
                1L, true, true, "Maintenance in progress",
                null, null, false, OffsetDateTime.now(), "platform-admin"
        );
        when(repository.findDefaultConfig()).thenReturn(Optional.of(config));
        when(repository.save(any(MaintenanceConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MaintenanceResponse response = maintenanceService.disable();

        assertFalse(response.isEnabled());
        assertFalse(response.isEffective());
        assertEquals("MAINTENANCE_DISABLED", response.getStatus());
        assertEquals("Maintenance mode disabled.", response.getMessage());

        verify(eventPublisher, times(1)).publishEvent(any(MaintenanceDisabledEvent.class));
    }

    @Test
    @DisplayName("getPublicStatus masks internal metadata")
    void testGetPublicStatus() {
        MaintenanceConfig config = new MaintenanceConfig(
                1L, true, true, "Maintenance scheduled",
                OffsetDateTime.now().minusHours(1), OffsetDateTime.now().plusHours(2),
                false, OffsetDateTime.now(), "platform-admin"
        );
        when(repository.findDefaultConfig()).thenReturn(Optional.of(config));

        PublicMaintenanceResponse publicResponse = maintenanceService.getPublicStatus();
        assertTrue(publicResponse.isMaintenance());
        assertEquals("Maintenance scheduled", publicResponse.getMessage());
        assertNotNull(publicResponse.getStartAt());
        assertNotNull(publicResponse.getEndAt());
    }

    @Test
    @DisplayName("updateSettings updates configuration successfully")
    void testUpdateSettings() {
        MaintenanceConfig config = new MaintenanceConfig(
                1L, false, true, "Original message",
                null, null, false, OffsetDateTime.now(), "SYSTEM"
        );
        when(repository.findDefaultConfig()).thenReturn(Optional.of(config));
        when(repository.save(any(MaintenanceConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MaintenanceUpdateRequest request = new MaintenanceUpdateRequest(
                true, false, "Updated message", null, null, false
        );

        MaintenanceResponse response = maintenanceService.updateSettings(request);

        assertTrue(response.isEnabled());
        assertFalse(response.isAllowAdminAccess());
        assertEquals("Updated message", response.getMessage());
        verify(eventPublisher, times(1)).publishEvent(any(MaintenanceEnabledEvent.class));
    }
}
