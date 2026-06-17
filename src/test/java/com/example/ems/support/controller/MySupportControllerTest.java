package com.example.ems.support.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.support.dto.*;
import com.example.ems.support.service.MySupportService;
import com.example.ems.security.service.JwtService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MySupportControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private MySupportService supportService;
    @Mock private RoleService roleService;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    @InjectMocks
    private MySupportController supportController;

    private User empUser;
    private final String empEmail = "employee@company.com";
    private final String token = "mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(supportController).build();

        empUser = new User();
        empUser.setWorkEmail(empEmail);
        empUser.setFullName("John Doe");

        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
    }

    private void setupMockPermissions(boolean allowed) {
        when(roleService.isSuperAdmin(empEmail)).thenReturn(false);
        when(roleService.hasPermission(eq(empEmail), any(String.class))).thenReturn(allowed);
    }

    @Test
    public void testGetDashboard() throws Exception {
        setupMockPermissions(true);
        SupportDashboardResponse resp = new SupportDashboardResponse();
        when(supportService.getDashboard(empEmail)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/support/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testCreateTicket() throws Exception {
        setupMockPermissions(true);
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCategoryId(1L);
        request.setSubCategoryId(1L);
        request.setSubject("VPN issues");
        request.setDescription("VPN connection keeps failing");
        request.setPriority("HIGH");

        CreateTicketResponse resp = new CreateTicketResponse(1L, "SUP-2026-0001", "OPEN", "HIGH", "2026-06-16T12:00:00Z", "2026-06-16T14:00:00Z", "2026-06-17T20:00:00Z", "Created");
        when(supportService.createTicket(eq(empEmail), any(CreateTicketRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/support/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketNumber").value("SUP-2026-0001"));
    }

    @Test
    public void testGetMyTickets() throws Exception {
        setupMockPermissions(true);
        MyTicketsResponse resp = new MyTicketsResponse(List.of(), new MyTicketsResponse.PaginationDto());
        when(supportService.getMyTickets(eq(empEmail), any(), any(), any(), any(), any())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/support/tickets")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetTicketDetails() throws Exception {
        setupMockPermissions(true);
        TicketDetailsResponse resp = new TicketDetailsResponse();
        resp.setTicketId(1L);
        resp.setTicketNumber("SUP-2026-0001");
        when(supportService.getTicketDetails(empEmail, 1L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/support/tickets/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketId").value(1L));
    }

    @Test
    public void testAddComment() throws Exception {
        setupMockPermissions(true);
        AddCommentRequest request = new AddCommentRequest();
        request.setCommentText("This is a test comment");

        AddCommentResponse resp = new AddCommentResponse(10L, empEmail, "2026-06-16T12:30:00Z", "Comment added");
        when(supportService.addComment(eq(empEmail), eq(1L), any(AddCommentRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/support/tickets/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentId").value(10L));
    }

    @Test
    public void testEscalateTicket() throws Exception {
        setupMockPermissions(true);
        EscalateTicketRequest request = new EscalateTicketRequest();
        request.setEscalationReason("Crucial bug blocker");

        EscalateTicketResponse resp = new EscalateTicketResponse(1L, "SUP-2026-0001", "HIGH", "CRITICAL", "2026-06-16T12:45:00Z", "Escalated");
        when(supportService.escalateTicket(eq(empEmail), eq(1L), any(EscalateTicketRequest.class))).thenReturn(resp);

        mockMvc.perform(patch("/api/v1/support/tickets/1/escalate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentPriority").value("CRITICAL"));
    }

    @Test
    public void testCloseTicket() throws Exception {
        setupMockPermissions(true);
        CloseTicketRequest request = new CloseTicketRequest();
        request.setRating(5);
        request.setFeedback("Super service");

        CloseTicketResponse resp = new CloseTicketResponse(1L, "SUP-2026-0001", "CLOSED", "2026-06-16T13:00:00Z", 5, "Super service", "Closed");
        when(supportService.closeTicket(eq(empEmail), eq(1L), any(CloseTicketRequest.class))).thenReturn(resp);

        mockMvc.perform(patch("/api/v1/support/tickets/1/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    public void testGetCategories() throws Exception {
        setupMockPermissions(true);
        when(supportService.getCategories()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/support/categories")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testUploadAttachment() throws Exception {
        setupMockPermissions(true);
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "log data".getBytes());

        AttachmentUploadResponse resp = new AttachmentUploadResponse("FILE-101", "test.txt", "text/plain", 8L, "2026-06-16T12:00:00Z");
        when(supportService.uploadAttachment(any())).thenReturn(resp);

        mockMvc.perform(multipart("/api/v1/support/attachments")
                .file(file)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileId").value("FILE-101"));
    }

    @Test
    public void testGetTicketTimeline() throws Exception {
        setupMockPermissions(true);
        TicketTimelineResponse resp = new TicketTimelineResponse(1L, "SUP-2026-0001", List.of());
        when(supportService.getTicketTimeline(empEmail, 1L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/support/tickets/1/timeline")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testSearchFAQ() throws Exception {
        setupMockPermissions(true);
        FAQSearchResponse resp = new FAQSearchResponse(List.of());
        when(supportService.searchFAQ("VPN")).thenReturn(resp);

        mockMvc.perform(get("/api/v1/support/knowledge-base")
                .param("keyword", "VPN")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
