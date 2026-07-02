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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class SupportWorkflowSimulationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private PlatformSupportService supportService;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private RoleService roleService;

    @InjectMocks
    private PlatformSupportController controller;

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

        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(adminEmail);
        when(userRepository.findByWorkEmail(adminEmail)).thenReturn(Optional.of(adminUser));
        when(roleService.isSuperAdmin(adminEmail)).thenReturn(true);
    }

    @Test
    public void runFullWorkflowSimulation() throws Exception {
        System.out.println("================================================================================");
        System.out.println("                   SUPPORT MODULE LIFECYCLE WORKFLOW SIMULATION                  ");
        System.out.println("================================================================================");

        // STEP 1: CREATE SUPPORT TICKET
        PlatformCreateTicketRequest createReq = new PlatformCreateTicketRequest();
        createReq.setSubject("Multi-Tenant DB Connection Latency");
        createReq.setDescription("PostgreSQL connection pools exhaust under peak traffic spikes.");
        createReq.setCategoryId(2L);
        createReq.setPriority("CRITICAL");
        createReq.setBusinessId(5L);

        MySupportTicket seededTicket = new MySupportTicket();
        seededTicket.setId(101L);
        seededTicket.setTicketNumber("SUP-2026-000101");
        seededTicket.setSubject(createReq.getSubject());
        seededTicket.setDescription(createReq.getDescription());
        seededTicket.setStatus(SupportTicketStatus.OPEN);
        seededTicket.setPriority(SupportTicketPriority.CRITICAL);
        seededTicket.setCreatedAt(LocalDateTime.now());
        seededTicket.setUpdatedAt(LocalDateTime.now());

        when(supportService.createTicket(any(PlatformCreateTicketRequest.class), eq(adminEmail))).thenReturn(seededTicket);

        String createJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(createReq);
        System.out.println("\n[HTTP REQUEST] POST /api/platform/support/tickets");
        System.out.println("Headers: Authorization = Bearer admin-mock-token");
        System.out.println("Body:\n" + createJson);

        MvcResult createResult = mockMvc.perform(post("/api/platform/support/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andReturn();

        String createResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(createResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 201 Created");
        System.out.println("Body:\n" + createResponseJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 2: ASSIGN TICKET TO SUPPORT AGENT
        PlatformAssignAgentRequest assignReq = new PlatformAssignAgentRequest();
        assignReq.setAgentId("agent.smith@company.com");
        assignReq.setReason("Assigning Database specialist agent to resolve pooling latency.");

        MySupportTicket assignedTicket = new MySupportTicket();
        assignedTicket.setId(101L);
        assignedTicket.setTicketNumber("SUP-2026-000101");
        assignedTicket.setSubject(createReq.getSubject());
        assignedTicket.setAssignedAgent("Agent Smith");
        assignedTicket.setAssignedTeam("Technical Support");
        assignedTicket.setStatus(SupportTicketStatus.ASSIGNED);
        assignedTicket.setPriority(SupportTicketPriority.CRITICAL);
        assignedTicket.setCreatedAt(LocalDateTime.now());
        assignedTicket.setUpdatedAt(LocalDateTime.now());

        when(supportService.assignTicket(eq(101L), any(PlatformAssignAgentRequest.class), eq(adminEmail))).thenReturn(assignedTicket);

        String assignJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(assignReq);
        System.out.println("\n[HTTP REQUEST] PATCH /api/platform/support/tickets/101/assign");
        System.out.println("Headers: Authorization = Bearer admin-mock-token");
        System.out.println("Body:\n" + assignJson);

        MvcResult assignResult = mockMvc.perform(patch("/api/platform/support/tickets/101/assign")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(assignJson))
                .andReturn();

        String assignResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(assignResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 200 OK");
        System.out.println("Body:\n" + assignResponseJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 3: AGENT STARTS INVESTIGATION (IN_PROGRESS STATUS)
        PlatformUpdateTicketRequest statusReq = new PlatformUpdateTicketRequest();
        statusReq.setStatus("IN_PROGRESS");

        MySupportTicket inProgressTicket = new MySupportTicket();
        inProgressTicket.setId(101L);
        inProgressTicket.setTicketNumber("SUP-2026-000101");
        inProgressTicket.setSubject(createReq.getSubject());
        inProgressTicket.setAssignedAgent("Agent Smith");
        inProgressTicket.setAssignedTeam("Technical Support");
        inProgressTicket.setStatus(SupportTicketStatus.IN_PROGRESS);
        inProgressTicket.setPriority(SupportTicketPriority.CRITICAL);
        inProgressTicket.setCreatedAt(LocalDateTime.now());
        inProgressTicket.setUpdatedAt(LocalDateTime.now());

        when(supportService.updateTicket(eq(101L), any(PlatformUpdateTicketRequest.class), eq(adminEmail))).thenReturn(inProgressTicket);

        String statusJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(statusReq);
        System.out.println("\n[HTTP REQUEST] PUT /api/platform/support/tickets/101");
        System.out.println("Headers: Authorization = Bearer admin-mock-token");
        System.out.println("Body:\n" + statusJson);

        MvcResult statusResult = mockMvc.perform(put("/api/platform/support/tickets/101")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusJson))
                .andReturn();

        String statusResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(statusResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 200 OK");
        System.out.println("Body:\n" + statusResponseJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 4: AGENT POSTS INTERNAL NOTE
        PlatformMessageRequest internalMsg = new PlatformMessageRequest();
        internalMsg.setMessage("Reviewing database pooling configurations. Max pool size is currently set to 20; raising it to 50 should relieve constraints.");
        internalMsg.setInternal(true);

        MySupportComment internalComment = new MySupportComment(1L, inProgressTicket, internalMsg.getMessage(), adminEmail, true);
        internalComment.setCreatedAt(LocalDateTime.now());

        when(supportService.addComment(eq(101L), any(PlatformMessageRequest.class), eq(adminEmail))).thenReturn(internalComment);

        String internalJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(internalMsg);
        System.out.println("\n[HTTP REQUEST] POST /api/platform/support/tickets/101/messages");
        System.out.println("Headers: Authorization = Bearer admin-mock-token");
        System.out.println("Body:\n" + internalJson);

        MvcResult internalResult = mockMvc.perform(post("/api/platform/support/tickets/101/messages")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(internalJson))
                .andReturn();

        String internalRespJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(internalResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 201 Created");
        System.out.println("Body:\n" + internalRespJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 5: AGENT POSTS PUBLIC REPLY (RESOLVED NOTIFICATION)
        PlatformMessageRequest publicMsg = new PlatformMessageRequest();
        publicMsg.setMessage("We have increased the Database connection pool limits. Please verify the performance on your tenant branch.");
        publicMsg.setInternal(false);

        MySupportComment publicComment = new MySupportComment(2L, inProgressTicket, publicMsg.getMessage(), adminEmail, false);
        publicComment.setCreatedAt(LocalDateTime.now());

        when(supportService.addComment(eq(101L), any(PlatformMessageRequest.class), eq(adminEmail))).thenReturn(publicComment);

        String publicJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(publicMsg);
        System.out.println("\n[HTTP REQUEST] POST /api/platform/support/tickets/101/messages");
        System.out.println("Headers: Authorization = Bearer admin-mock-token");
        System.out.println("Body:\n" + publicJson);

        MvcResult publicResult = mockMvc.perform(post("/api/platform/support/tickets/101/messages")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(publicJson))
                .andReturn();

        String publicRespJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(publicResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 201 Created");
        System.out.println("Body:\n" + publicRespJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 6: RESOLVE TICKET
        PlatformUpdateTicketRequest resolveReq = new PlatformUpdateTicketRequest();
        resolveReq.setStatus("RESOLVED");

        MySupportTicket resolvedTicket = new MySupportTicket();
        resolvedTicket.setId(101L);
        resolvedTicket.setTicketNumber("SUP-2026-000101");
        resolvedTicket.setSubject(createReq.getSubject());
        resolvedTicket.setAssignedAgent("Agent Smith");
        resolvedTicket.setStatus(SupportTicketStatus.RESOLVED);
        resolvedTicket.setPriority(SupportTicketPriority.CRITICAL);
        resolvedTicket.setCreatedAt(LocalDateTime.now());
        resolvedTicket.setUpdatedAt(LocalDateTime.now());
        resolvedTicket.setResolvedAt(LocalDateTime.now());

        when(supportService.updateTicket(eq(101L), any(PlatformUpdateTicketRequest.class), eq(adminEmail))).thenReturn(resolvedTicket);

        String resolveJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resolveReq);
        System.out.println("\n[HTTP REQUEST] PUT /api/platform/support/tickets/101");
        System.out.println("Headers: Authorization = Bearer admin-mock-token");
        System.out.println("Body:\n" + resolveJson);

        MvcResult resolveResult = mockMvc.perform(put("/api/platform/support/tickets/101")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(resolveJson))
                .andReturn();

        String resolveResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(resolveResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 200 OK");
        System.out.println("Body:\n" + resolveResponseJson);
        System.out.println("================================================================================");
    }
}
