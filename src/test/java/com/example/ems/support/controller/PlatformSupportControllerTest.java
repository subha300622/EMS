package com.example.ems.support.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.support.dto.*;
import com.example.ems.support.entity.*;
import com.example.ems.support.service.PlatformSupportService;
import com.example.ems.security.service.JwtService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlatformSupportControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private PlatformSupportService supportService;
    @Mock private RoleService roleService;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    @InjectMocks
    private PlatformSupportController platformSupportController;

    private User adminUser;
    private final String adminEmail = "admin@company.com";
    private final String token = "admin-mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(platformSupportController).build();

        adminUser = new User();
        adminUser.setWorkEmail(adminEmail);
        adminUser.setFullName("System Admin");

        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(adminEmail);
        when(userRepository.findByWorkEmail(adminEmail)).thenReturn(Optional.of(adminUser));
    }

    private void setupMockPermissions(boolean allowed, String permission) {
        when(roleService.isSuperAdmin(adminEmail)).thenReturn(false);
        when(roleService.hasPermission(adminEmail, permission)).thenReturn(allowed);
    }

    @Test
    public void testGetDashboardUnauthorized() throws Exception {
        mockMvc.perform(get("/api/platform/support/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetDashboardForbidden() throws Exception {
        setupMockPermissions(false, "support.view");
        mockMvc.perform(get("/api/platform/support/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetDashboardSuccess() throws Exception {
        setupMockPermissions(true, "support.view");
        PlatformSupportDashboardResponse resp = new PlatformSupportDashboardResponse();
        when(supportService.getDashboardData()).thenReturn(resp);

        mockMvc.perform(get("/api/platform/support/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testCreateTicketOnBehalf() throws Exception {
        setupMockPermissions(true, "support.manage");

        PlatformCreateTicketRequest req = new PlatformCreateTicketRequest();
        req.setSubject("Business Network Down");
        req.setDescription("Switches offline");
        req.setCategoryId(1L);
        req.setPriority("CRITICAL");
        req.setBusinessId(2L);

        MySupportTicket ticket = new MySupportTicket();
        ticket.setId(10L);
        ticket.setTicketNumber("SUP-2026-000100");
        ticket.setSubject("Business Network Down");
        ticket.setPriority(SupportTicketPriority.CRITICAL);
        ticket.setStatus(SupportTicketStatus.OPEN);

        when(supportService.createTicket(any(PlatformCreateTicketRequest.class), eq(adminEmail))).thenReturn(ticket);

        mockMvc.perform(post("/api/platform/support/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketNumber").value("SUP-2026-000100"));
    }

    @Test
    public void testAssignAgent() throws Exception {
        setupMockPermissions(true, "support.manage");

        PlatformAssignAgentRequest req = new PlatformAssignAgentRequest();
        req.setAgentId("agent@company.com");
        req.setReason("Network expert needed");

        MySupportTicket ticket = new MySupportTicket();
        ticket.setId(10L);
        ticket.setTicketNumber("SUP-2026-000100");
        ticket.setAssignedAgent("Agent Smith");
        ticket.setStatus(SupportTicketStatus.ASSIGNED);

        when(supportService.assignTicket(eq(10L), any(PlatformAssignAgentRequest.class), eq(adminEmail))).thenReturn(ticket);

        mockMvc.perform(patch("/api/platform/support/tickets/10/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assignedAgent").value("Agent Smith"));
    }

    @Test
    public void testMergeTickets() throws Exception {
        setupMockPermissions(true, "support.manage");

        MergeTicketsRequest req = new MergeTicketsRequest();
        req.setPrimaryTicketId(1L);
        req.setMergeTicketIds(List.of(2L, 3L));
        req.setMergeReason("Duplicate issues");

        MySupportTicket primary = new MySupportTicket();
        primary.setId(1L);
        primary.setTicketNumber("SUP-0001");
        primary.setStatus(SupportTicketStatus.OPEN);

        when(supportService.mergeTickets(any(MergeTicketsRequest.class), eq(adminEmail))).thenReturn(primary);

        mockMvc.perform(post("/api/platform/support/tickets/merge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetTemplates() throws Exception {
        setupMockPermissions(true, "support.view");

        SupportTemplate t = new SupportTemplate();
        t.setId(1L);
        t.setTitle("Welcome template");
        t.setContent("Hello, how can we help?");

        when(supportService.listTemplates()).thenReturn(Collections.singletonList(t));

        mockMvc.perform(get("/api/platform/support/templates")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Welcome template"));
    }
}
