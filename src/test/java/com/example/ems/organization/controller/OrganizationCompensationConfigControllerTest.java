package com.example.ems.organization.controller;

import com.example.ems.organization.dto.CompensationConfigRequest;
import com.example.ems.organization.dto.CompensationConfigResponse;
import com.example.ems.organization.service.OrganizationCompensationConfigService;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrganizationCompensationConfigControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrganizationCompensationConfigService configService;

    @InjectMocks
    private OrganizationCompensationConfigController configController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(configController).build();
        TenantContext.setCurrentTenant(10L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("GET /api/v1/organizations/compensation-config returns 200 with config")
    void testGetConfig() throws Exception {
        CompensationConfigResponse response = new CompensationConfigResponse(10L, true, false, true, null);
        when(configService.getConfig(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/organizations/compensation-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.organizationId").value(10))
                .andExpect(jsonPath("$.data.overtimeEnabled").value(true))
                .andExpect(jsonPath("$.data.incentiveEnabled").value(false))
                .andExpect(jsonPath("$.data.bonusEnabled").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/organizations/compensation-config updates config without organizationId in body")
    void testUpdateConfig() throws Exception {
        CompensationConfigRequest request = new CompensationConfigRequest(false, true, true);
        CompensationConfigResponse response = new CompensationConfigResponse(10L, false, true, true, null);

        when(configService.updateConfig(eq(10L), any(CompensationConfigRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/organizations/compensation-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.organizationId").value(10))
                .andExpect(jsonPath("$.data.overtimeEnabled").value(false))
                .andExpect(jsonPath("$.data.incentiveEnabled").value(true))
                .andExpect(jsonPath("$.data.bonusEnabled").value(true));
    }
}
