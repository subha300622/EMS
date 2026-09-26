package com.example.ems.maintenance.controller;

import com.example.ems.maintenance.dto.MaintenanceEnableRequest;
import com.example.ems.maintenance.dto.MaintenanceResponse;
import com.example.ems.maintenance.dto.MaintenanceUpdateRequest;
import com.example.ems.maintenance.service.MaintenanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlatformMaintenanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MaintenanceService maintenanceService;

    @InjectMocks
    private PlatformMaintenanceController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("GET /api/v1/platform/maintenance returns configuration and effective status")
    void testGetSettings() throws Exception {
        MaintenanceResponse response = new MaintenanceResponse(
                true, true, "MAINTENANCE_ENABLED", true,
                "Scheduled maintenance is in progress.",
                OffsetDateTime.parse("2026-09-26T22:00:00+05:30"),
                OffsetDateTime.parse("2026-09-27T01:00:00+05:30"),
                false, OffsetDateTime.now(), "platform-admin"
        );
        when(maintenanceService.getSettings()).thenReturn(response);

        mockMvc.perform(get("/api/v1/platform/maintenance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.effective").value(true))
                .andExpect(jsonPath("$.allowAdminAccess").value(true))
                .andExpect(jsonPath("$.message").value("Scheduled maintenance is in progress."))
                .andExpect(jsonPath("$.updatedBy").value("platform-admin"));
    }

    @Test
    @DisplayName("PUT /api/v1/platform/maintenance updates configuration")
    void testUpdateSettings() throws Exception {
        MaintenanceUpdateRequest request = new MaintenanceUpdateRequest(
                true, true, "Updated message", null, null, false
        );
        MaintenanceResponse response = new MaintenanceResponse(
                true, true, "MAINTENANCE_ENABLED", true,
                "Updated message", null, null, false, OffsetDateTime.now(), "platform-admin"
        );
        when(maintenanceService.updateSettings(any(MaintenanceUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/platform/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.message").value("Updated message"));
    }

    @Test
    @DisplayName("POST /api/v1/platform/maintenance/enable immediately activates maintenance")
    void testEnable() throws Exception {
        MaintenanceEnableRequest request = new MaintenanceEnableRequest(
                "Immediate platform maintenance", null, null, true, false
        );
        MaintenanceResponse response = new MaintenanceResponse(
                true, true, "MAINTENANCE_ENABLED", true,
                "Immediate platform maintenance", null, null, false, OffsetDateTime.now(), "platform-admin"
        );
        when(maintenanceService.enable(any(MaintenanceEnableRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/platform/maintenance/enable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.effective").value(true))
                .andExpect(jsonPath("$.message").value("Immediate platform maintenance"));
    }

    @Test
    @DisplayName("POST /api/v1/platform/maintenance/disable turns off maintenance")
    void testDisable() throws Exception {
        MaintenanceResponse response = new MaintenanceResponse(
                false, false, "MAINTENANCE_DISABLED", true,
                "Maintenance mode disabled.", null, null, false, OffsetDateTime.now(), "platform-admin"
        );
        when(maintenanceService.disable()).thenReturn(response);

        mockMvc.perform(post("/api/v1/platform/maintenance/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.effective").value(false))
                .andExpect(jsonPath("$.message").value("Maintenance mode disabled."));
    }
}
