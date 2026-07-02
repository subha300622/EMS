package com.example.ems.support.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.security.service.JwtService;
import com.example.ems.support.dto.*;
import com.example.ems.support.entity.*;
import com.example.ems.support.service.PlatformSupportSlaService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlatformSupportSlaControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PlatformSupportSlaService slaService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private RoleService roleService;

    @InjectMocks
    private PlatformSupportSlaController controller;

    private final String adminEmail = "admin@company.com";
    private final String token = "admin-mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setWorkEmail(adminEmail);
        adminUser.setFullName("Platform Admin");
        adminUser.setUserId("user-001");

        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(adminEmail);
        when(userRepository.findByWorkEmail(adminEmail)).thenReturn(Optional.of(adminUser));
        when(roleService.isSuperAdmin(adminEmail)).thenReturn(true);
    }

    private void setupMockPermissions(boolean allowed) {
        when(roleService.isSuperAdmin(adminEmail)).thenReturn(false);
        when(roleService.hasPermission(eq(adminEmail), any(String.class))).thenReturn(allowed);
    }

    @Test
    public void testGetSlasSuccess() throws Exception {
        SupportSla sla = new SupportSla();
        sla.setId(1L);
        sla.setName("Critical Priority SLA");
        sla.setPriority(SupportTicketPriority.CRITICAL);
        sla.setResponseTimeMinutes(15);
        sla.setResolutionTimeMinutes(120);
        sla.setEnabled(true);

        SlaResponse slaResponse = new SlaResponse(sla, 10, 2, 80.0);
        PageImpl<SlaResponse> page = new PageImpl<>(List.of(slaResponse));

        when(slaService.getSlas(any(), any(), any(), any(), any(), any(), eq(1), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/platform/support/slas")
                .header("Authorization", "Bearer " + token)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].name").value("Critical Priority SLA"))
                .andExpect(jsonPath("$.data.pagination.totalElements").value(1));
    }

    @Test
    public void testGetSlaDetailsSuccess() throws Exception {
        SupportSla sla = new SupportSla();
        sla.setId(1L);
        sla.setName("Critical Priority SLA");
        sla.setPriority(SupportTicketPriority.CRITICAL);
        sla.setResponseTimeMinutes(15);
        sla.setResolutionTimeMinutes(120);

        SlaResponse slaResponse = new SlaResponse(sla, 156, 3, 98.08);

        when(slaService.getSlaDetails(1L)).thenReturn(slaResponse);

        mockMvc.perform(get("/api/platform/support/slas/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Critical Priority SLA"))
                .andExpect(jsonPath("$.data.priority.code").value("CRITICAL"))
                .andExpect(jsonPath("$.data.firstResponse.displayValue").value("15 Minutes"));
    }

    @Test
    public void testCreateSlaSuccess() throws Exception {
        SupportSlaRequest req = new SupportSlaRequest();
        req.setName("Critical SLA");
        req.setDescription("Default SLA for critical tickets.");
        req.setPriority("CRITICAL");
        req.setFirstResponseMinutes(15);
        req.setResolutionMinutes(120);
        req.setIsDefault(true);
        req.setStatus("ACTIVE");

        SupportSla created = new SupportSla();
        created.setId(1L);
        created.setName("Critical SLA");
        created.setEnabled(true);

        when(slaService.createSla(any(SupportSlaRequest.class), any(User.class))).thenReturn(created);

        mockMvc.perform(post("/api/platform/support/slas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Critical SLA"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    public void testDeleteSlaSuccess() throws Exception {
        mockMvc.perform(delete("/api/platform/support/slas/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("SLA policy deleted successfully."));

        verify(slaService, times(1)).deleteSla(eq(1L), any(User.class));
    }

    @Test
    public void testChangeStatusSuccess() throws Exception {
        SupportSla updated = new SupportSla();
        updated.setId(1L);
        updated.setEnabled(true);

        when(slaService.updateStatus(eq(1L), eq("ACTIVE"), any(User.class))).thenReturn(updated);

        HashMap<String, String> body = new HashMap<>();
        body.put("status", "ACTIVE");

        mockMvc.perform(patch("/api/platform/support/slas/1/status")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    public void testSetDefaultSuccess() throws Exception {
        SupportSla updated = new SupportSla();
        updated.setId(1L);
        updated.setDefault(true);

        when(slaService.setDefaultSla(eq(1L), any(User.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/platform/support/slas/1/default")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isDefault").value(true));
    }

    @Test
    public void testDuplicateSuccess() throws Exception {
        SupportSla duplicated = new SupportSla();
        duplicated.setId(2L);
        duplicated.setName("Critical SLA (Copy)");
        duplicated.setEnabled(true);

        when(slaService.duplicateSla(eq(1L), any(User.class))).thenReturn(duplicated);

        mockMvc.perform(post("/api/platform/support/slas/1/duplicate")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Critical SLA (Copy)"));
    }

    @Test
    public void testGetDashboardSummarySuccess() throws Exception {
        SlaDashboardResponse response = new SlaDashboardResponse(
                new SlaDashboardResponse.SummaryDto(4, 4, 0, "Critical SLA"),
                new SlaDashboardResponse.TicketMetricsDto(325, 8, 97.6));

        when(slaService.getDashboard()).thenReturn(response);

        mockMvc.perform(get("/api/platform/support/slas/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary.totalPolicies").value(4))
                .andExpect(jsonPath("$.data.ticketMetrics.breached").value(8));
    }

    @Test
    public void testForbiddenAccess() throws Exception {
        setupMockPermissions(false);

        SupportSlaRequest req = new SupportSlaRequest();
        req.setName("Test SLA");
        req.setPriority("CRITICAL");
        req.setFirstResponseMinutes(15);
        req.setResolutionMinutes(120);

        mockMvc.perform(post("/api/platform/support/slas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access Denied: Requires 'support.manage' permission."));
    }
}
