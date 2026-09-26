package com.example.ems.maintenance.controller;

import com.example.ems.maintenance.dto.PublicMaintenanceResponse;
import com.example.ems.maintenance.service.MaintenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PublicMaintenanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MaintenanceService maintenanceService;

    @InjectMocks
    private PublicMaintenanceController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/v1/public/maintenance when inactive returns maintenance=false")
    void testGetPublicStatusInactive() throws Exception {
        when(maintenanceService.getPublicStatus()).thenReturn(new PublicMaintenanceResponse(false));

        mockMvc.perform(get("/api/v1/public/maintenance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maintenance").value(false))
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.startAt").doesNotExist())
                .andExpect(jsonPath("$.endAt").doesNotExist())
                .andExpect(jsonPath("$.updatedBy").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/public/maintenance when active returns public banner info without internal fields")
    void testGetPublicStatusActive() throws Exception {
        PublicMaintenanceResponse response = new PublicMaintenanceResponse(
                true,
                "The EMS platform is currently under maintenance.",
                OffsetDateTime.parse("2026-09-26T22:00:00+05:30"),
                OffsetDateTime.parse("2026-09-27T01:00:00+05:30")
        );
        when(maintenanceService.getPublicStatus()).thenReturn(response);

        mockMvc.perform(get("/api/v1/public/maintenance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maintenance").value(true))
                .andExpect(jsonPath("$.message").value("The EMS platform is currently under maintenance."))
                .andExpect(jsonPath("$.startAt").isNotEmpty())
                .andExpect(jsonPath("$.endAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedBy").doesNotExist());
    }
}
