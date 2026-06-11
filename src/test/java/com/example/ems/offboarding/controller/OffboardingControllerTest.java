package com.example.ems.offboarding.controller;

import com.example.ems.asset.dto.AssetReturnRequest;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.offboarding.dto.ExitInterviewFeedbackRequest;
import com.example.ems.offboarding.dto.ExitInterviewRequest;
import com.example.ems.offboarding.dto.HandoverRequest;
import com.example.ems.offboarding.dto.OffboardingDashboardResponse;
import com.example.ems.offboarding.dto.OffboardingRequest;
import com.example.ems.offboarding.dto.OffboardingResponse;
import com.example.ems.offboarding.dto.OffboardingTaskResponse;
import com.example.ems.offboarding.dto.SettlementRequest;
import com.example.ems.offboarding.service.OffboardingService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OffboardingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OffboardingService offboardingService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private OffboardingController offboardingController;

    private User hrUser;
    private User empUser;
    private String hrEmail = "hr@example.com";
    private String empEmail = "emp@example.com";
    private String mockToken = "mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(offboardingController).build();

        hrUser = new User();
        hrUser.setWorkEmail(hrEmail);

        empUser = new User();
        empUser.setWorkEmail(empEmail);
    }

    private void setupManagerMock() {
        when(jwtService.validateAccessToken(mockToken)).thenReturn(true);
        when(jwtService.getEmailFromToken(mockToken)).thenReturn(hrEmail);
        when(userRepository.findByWorkEmail(hrEmail)).thenReturn(Optional.of(hrUser));
        when(roleService.hasPermission(hrEmail, "employee.delete")).thenReturn(true);
    }

    private void setupEmployeeMock() {
        when(jwtService.validateAccessToken(mockToken)).thenReturn(true);
        when(jwtService.getEmailFromToken(mockToken)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
        when(roleService.hasPermission(empEmail, "employee.delete")).thenReturn(false);
        when(roleService.hasPermission(empEmail, "employee.update")).thenReturn(false);
        when(roleService.hasPermission(empEmail, "recruitment.manage")).thenReturn(false);
    }

    // ── 1. GET DASHBOARD ────────────────────────────────────────────────────
    @Test
    public void testGetDashboardSuccess() throws Exception {
        setupManagerMock();

        OffboardingDashboardResponse stats = new OffboardingDashboardResponse();
        stats.setTotalOnboardings(5);
        stats.setPendingOffboardings(2);
        stats.setInProgressOffboardings(1);
        stats.setCompletedOffboardings(1);
        stats.setApprovedOffboardings(1);
        stats.setTotalTasksAssigned(10);
        stats.setCompletedTasksCount(5);
        stats.setTaskCompletionRate(50.0);

        when(offboardingService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/offboarding-records/dashboard")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOffboardings").value(5))
                .andExpect(jsonPath("$.data.pendingOffboardings").value(2))
                .andExpect(jsonPath("$.data.taskCompletionRate").value(50.0));
    }

    @Test
    public void testGetDashboardForbidden() throws Exception {
        setupEmployeeMock();

        mockMvc.perform(get("/api/v1/offboarding-records/dashboard")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
    }

    // ── 2. CREATE OFFBOARDING ────────────────────────────────────────────────
    @Test
    public void testCreateOffboardingSuccess() throws Exception {
        setupManagerMock();

        OffboardingRequest request = new OffboardingRequest();
        request.setEmployeeId(1L);
        request.setReason("Resignation");
        request.setExitDate(LocalDate.of(2026, 7, 10));

        OffboardingResponse response = new OffboardingResponse();
        response.setId(10L);
        response.setEmployeeId(1L);
        response.setEmployeeEmail("john@example.com");
        response.setStatus("PENDING");
        response.setReason("Resignation");

        when(offboardingService.createOffboarding(any(OffboardingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/offboarding-records")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.reason").value("Resignation"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    public void testCreateOffboardingForbidden() throws Exception {
        setupEmployeeMock();

        OffboardingRequest request = new OffboardingRequest();
        request.setEmployeeId(1L);
        request.setReason("Resignation");

        mockMvc.perform(post("/api/v1/offboarding-records")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ── 3. LIST OFFBOARDINGS ─────────────────────────────────────────────────
    @Test
    public void testListOffboardingsManager() throws Exception {
        setupManagerMock();

        OffboardingResponse r = new OffboardingResponse();
        r.setId(10L);
        r.setEmployeeEmail("john@example.com");
        r.setStatus("PENDING");

        when(offboardingService.getOffboardings()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/v1/offboarding-records")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10L));
    }

    @Test
    public void testListOffboardingsEmployeeSelf() throws Exception {
        setupEmployeeMock();

        OffboardingResponse r = new OffboardingResponse();
        r.setId(10L);
        r.setEmployeeEmail(empEmail);
        r.setStatus("PENDING");

        when(offboardingService.getOffboardingByEmployeeEmail(empEmail)).thenReturn(Optional.of(r));

        mockMvc.perform(get("/api/v1/offboarding-records")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].employeeEmail").value(empEmail));
    }

    // ── 4. GET OFFBOARDING BY ID ─────────────────────────────────────────────
    @Test
    public void testGetOffboardingByIdSuccess() throws Exception {
        setupEmployeeMock(); // employee reading their own record

        OffboardingResponse r = new OffboardingResponse();
        r.setId(10L);
        r.setEmployeeEmail(empEmail);
        r.setStatus("PENDING");

        when(offboardingService.getOffboardingById(10L)).thenReturn(Optional.of(r));

        mockMvc.perform(get("/api/v1/offboarding-records/10")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeEmail").value(empEmail));
    }

    @Test
    public void testGetOffboardingByIdForbidden() throws Exception {
        setupEmployeeMock(); // employee reading another person's record

        OffboardingResponse r = new OffboardingResponse();
        r.setId(10L);
        r.setEmployeeEmail("other@example.com");
        r.setStatus("PENDING");

        when(offboardingService.getOffboardingById(10L)).thenReturn(Optional.of(r));

        mockMvc.perform(get("/api/v1/offboarding-records/10")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isForbidden());
    }

    // ── 5. APPROVE OFFBOARDING ───────────────────────────────────────────────
    @Test
    public void testApproveOffboardingSuccess() throws Exception {
        setupManagerMock();

        OffboardingResponse r = new OffboardingResponse();
        r.setId(10L);
        r.setStatus("APPROVED");

        when(offboardingService.approveOffboarding(10L)).thenReturn(Optional.of(r));

        mockMvc.perform(patch("/api/v1/offboarding-records/10/approve")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    // ── 6. GET TASKS ────────────────────────────────────────────────────────
    @Test
    public void testGetTasksSuccess() throws Exception {
        setupEmployeeMock();

        OffboardingResponse r = new OffboardingResponse();
        r.setId(10L);
        r.setEmployeeEmail(empEmail);

        OffboardingTaskResponse t = new OffboardingTaskResponse();
        t.setId(1L);
        t.setTitle("Task 1");
        t.setStatus("PENDING");

        when(offboardingService.getOffboardingById(10L)).thenReturn(Optional.of(r));
        when(offboardingService.getTasks(10L)).thenReturn(List.of(t));

        mockMvc.perform(get("/api/v1/offboarding-records/10/tasks")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Task 1"));
    }

    @Test
    public void testUpdateTaskStatusSuccess() throws Exception {
        setupEmployeeMock();

        OffboardingTaskResponse t = new OffboardingTaskResponse();
        t.setId(1L);
        t.setTitle("Task 1");
        t.setStatus("COMPLETED");

        Map<String, String> body = Map.of("status", "COMPLETED");

        when(offboardingService.updateTaskStatus(1L, "COMPLETED")).thenReturn(Optional.of(t));

        mockMvc.perform(patch("/api/v1/offboarding-records/tasks/1/status")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    // ── 7. RETURN ASSET ─────────────────────────────────────────────────────
    @Test
    public void testRecordAssetReturnSuccess() throws Exception {
        setupManagerMock();

        AssetReturnRequest request = new AssetReturnRequest();
        request.setOffboardingId(10L);
        request.setAssetName("MacBook Pro");
        request.setSerialNumber("SN1234");
        request.setReturnStatus("RETURNED");

        OffboardingResponse r = new OffboardingResponse();
        r.setId(10L);
        r.setStatus("IN_PROGRESS");

        when(offboardingService.recordAssetReturn(any(AssetReturnRequest.class))).thenReturn(r);

        mockMvc.perform(post("/api/v1/offboarding-records/assets/return")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    // ── 8. SETTLEMENT ────────────────────────────────────────────────────────
    @Test
    public void testProcessSettlementSuccess() throws Exception {
        setupManagerMock();

        SettlementRequest request = new SettlementRequest();
        request.setOffboardingId(10L);
        request.setGratuity(BigDecimal.valueOf(1000));
        request.setSeverance(BigDecimal.valueOf(2000));
        request.setPendingSalary(BigDecimal.valueOf(1500));
        request.setDeductions(BigDecimal.valueOf(200));

        OffboardingResponse r = new OffboardingResponse();
        r.setId(10L);
        r.setStatus("IN_PROGRESS");

        when(offboardingService.processSettlement(any(SettlementRequest.class))).thenReturn(r);

        mockMvc.perform(post("/api/v1/offboarding-records/10/settlement")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    // ── 9. COMPLETE & REJECT ENDPOINTS ──────────────────────────────────────
    @Test
    public void testCompleteOffboardingSuccess() throws Exception {
        setupEmployeeMock();

        OffboardingResponse r = new OffboardingResponse();
        r.setId(10L);
        r.setEmployeeEmail(empEmail);
        r.setStatus("COMPLETED");

        when(offboardingService.getOffboardingById(10L)).thenReturn(Optional.of(r));
        when(offboardingService.completeOffboarding(10L)).thenReturn(Optional.of(r));

        mockMvc.perform(patch("/api/v1/offboarding-records/10/complete")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    public void testRejectOffboardingSuccess() throws Exception {
        setupManagerMock();

        OffboardingResponse r = new OffboardingResponse();
        r.setId(10L);
        r.setStatus("REJECTED");

        when(offboardingService.rejectOffboarding(10L)).thenReturn(Optional.of(r));

        mockMvc.perform(patch("/api/v1/offboarding-records/10/reject")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    // ── 10. HANDOVER ────────────────────────────────────────────────────────
    @Test
    public void testRecordHandoverSuccess() throws Exception {
        setupManagerMock();

        HandoverRequest request = new HandoverRequest();
        request.setOffboardingId(10L);
        request.setTaskName("Transition Project X");
        request.setRecipientId(2L);

        OffboardingResponse r = new OffboardingResponse();
        r.setId(10L);
        r.setStatus("IN_PROGRESS");

        when(offboardingService.recordHandover(any(HandoverRequest.class))).thenReturn(r);

        mockMvc.perform(post("/api/v1/offboarding-records/handover")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    // ── 11. EXIT INTERVIEWS ─────────────────────────────────────────────────
    @Test
    public void testScheduleExitInterviewSuccess() throws Exception {
        setupManagerMock();

        ExitInterviewRequest request = new ExitInterviewRequest();
        request.setOffboardingId(10L);
        request.setInterviewDate(LocalDate.of(2026, 6, 20));
        request.setInterviewerName("HR Alice");

        OffboardingResponse r = new OffboardingResponse();
        r.setId(10L);
        r.setStatus("IN_PROGRESS");

        when(offboardingService.scheduleExitInterview(any(ExitInterviewRequest.class))).thenReturn(r);

        mockMvc.perform(post("/api/v1/offboarding-records/exit-interviews")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testAddExitFeedbackSuccess() throws Exception {
        setupManagerMock();

        ExitInterviewFeedbackRequest request = new ExitInterviewFeedbackRequest();
        request.setFeedback("Great working environment.");
        request.setReasonsForLeaving("Better opportunity");
        request.setRating(5);

        OffboardingResponse r = new OffboardingResponse();
        r.setId(10L);
        r.setStatus("IN_PROGRESS");

        when(offboardingService.addExitFeedback(eq(1L), any(ExitInterviewFeedbackRequest.class))).thenReturn(Optional.of(r));

        mockMvc.perform(post("/api/v1/offboarding-records/exit-interviews/1/feedback")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── 12. REVOKE ACCESS ───────────────────────────────────────────────────
    @Test
    public void testRevokeAccessSuccess() throws Exception {
        setupManagerMock();

        Map<String, Object> serviceResponse = Map.of(
                "offboardingId", 10L,
                "slackStatus", "DEACTIVATED",
                "employeeProfileStatus", "INACTIVE"
        );

        when(offboardingService.revokeAccess(10L)).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/v1/offboarding-records/10/revoke-access")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slackStatus").value("DEACTIVATED"));
    }

    // ── 13. REPORTS & ANALYTICS ──────────────────────────────────────────────
    @Test
    public void testGetReportSuccess() throws Exception {
        setupManagerMock();

        Map<String, Object> reportData = Map.of(
                "reportType", "exit-stats",
                "totalOffboardingsInitiated", 12
        );

        when(offboardingService.getReportData("exit-stats")).thenReturn(reportData);

        mockMvc.perform(get("/api/v1/offboarding-records/reports/exit-stats")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportType").value("exit-stats"));
    }

    @Test
    public void testGetAnalyticsSuccess() throws Exception {
        setupManagerMock();

        Map<String, Object> analyticsData = Map.of(
                "exitSatisfactoryIndex", 4.2
        );

        when(offboardingService.getAnalyticsData()).thenReturn(analyticsData);

        mockMvc.perform(get("/api/v1/offboarding-records/analytics")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.exitSatisfactoryIndex").value(4.2));
    }
}
