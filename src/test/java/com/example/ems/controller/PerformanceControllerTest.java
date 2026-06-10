package com.example.ems.controller;

import com.example.ems.dto.*;
import com.example.ems.entity.*;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.JwtService;
import com.example.ems.service.PerformanceService;
import com.example.ems.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PerformanceControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private PerformanceService performanceService;
    @Mock private RoleService roleService;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    @InjectMocks
    private PerformanceController performanceController;

    private User hrUser;
    private User empUser;
    private final String hrEmail = "hr@example.com";
    private final String empEmail = "emp@example.com";
    private final String token = "mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(performanceController).build();

        hrUser = new User();
        hrUser.setWorkEmail(hrEmail);

        empUser = new User();
        empUser.setWorkEmail(empEmail);
    }

    private void setupManager() {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(hrEmail);
        when(userRepository.findByWorkEmail(hrEmail)).thenReturn(Optional.of(hrUser));
        when(roleService.hasPermission(hrEmail, "employee.update")).thenReturn(true);
    }

    private void setupEmployee() {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
        when(roleService.hasPermission(empEmail, "employee.update")).thenReturn(false);
        when(roleService.hasPermission(empEmail, "employee.delete")).thenReturn(false);
        when(roleService.hasPermission(empEmail, "recruitment.manage")).thenReturn(false);
    }

    // ── 1. DASHBOARD ──────────────────────────────────────────────────────────
    @Test
    public void testGetDashboardSuccess() throws Exception {
        setupManager();

        PerformanceDashboardResponse stats = new PerformanceDashboardResponse();
        stats.setTotalCycles(5);
        stats.setTotalActiveCycles(3);
        stats.setTotalClosedCycles(2);
        stats.setTotalGoals(20);
        stats.setAchievedGoals(12);
        stats.setGoalCompletionRate(60.0);
        stats.setAverageGoalProgress(75.5);
        stats.setTotalReviews(10);
        stats.setSelfReviews(4);
        stats.setManagerReviews(6);
        stats.setPendingReviews(2);
        stats.setFinalizedReviews(8);
        stats.setAverageRating(4.2);
        stats.setTotalPips(3);
        stats.setActivePips(1);
        stats.setCompletedPips(2);

        when(performanceService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/performances/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCycles").value(5))
                .andExpect(jsonPath("$.data.totalActiveCycles").value(3))
                .andExpect(jsonPath("$.data.totalClosedCycles").value(2))
                .andExpect(jsonPath("$.data.achievedGoals").value(12))
                .andExpect(jsonPath("$.data.averageGoalProgress").value(75.5))
                .andExpect(jsonPath("$.data.selfReviews").value(4))
                .andExpect(jsonPath("$.data.managerReviews").value(6))
                .andExpect(jsonPath("$.data.pendingReviews").value(2))
                .andExpect(jsonPath("$.data.averageRating").value(4.2))
                .andExpect(jsonPath("$.data.totalPips").value(3))
                .andExpect(jsonPath("$.data.completedPips").value(2));
    }

    @Test
    public void testGetDashboardForbidden() throws Exception {
        setupEmployee();

        mockMvc.perform(get("/api/performances/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
    }

    // ── 2. CYCLES ─────────────────────────────────────────────────────────────
    @Test
    public void testGetCyclesSuccess() throws Exception {
        setupEmployee();

        PerformanceCycle cycle = new PerformanceCycle();
        cycle.setId(1L);
        cycle.setName("Q1 2026");
        cycle.setStatus("ACTIVE");

        PerformanceCycleResponse resp = new PerformanceCycleResponse(cycle);
        resp.enrichGoalStats(0, 0, 0, 0);
        resp.enrichReviewStats(0, 0);

        when(performanceService.getCycles()).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/performances/cycles")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Q1 2026"));
    }

    @Test
    public void testCreateCycleSuccess() throws Exception {
        setupManager();

        PerformanceCycleRequest req = new PerformanceCycleRequest();
        req.setName("Q2 2026");
        req.setStartDate(LocalDate.of(2026, 4, 1));
        req.setEndDate(LocalDate.of(2026, 6, 30));

        PerformanceCycle cycle = new PerformanceCycle();
        cycle.setId(2L);
        cycle.setName("Q2 2026");
        cycle.setStatus("ACTIVE");

        PerformanceCycleResponse resp = new PerformanceCycleResponse(cycle);
        resp.enrichGoalStats(0, 0, 0, 0);
        resp.enrichReviewStats(0, 0);

        when(performanceService.createCycle(any(PerformanceCycleRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/performances/cycles")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Q2 2026"));
    }

    @Test
    public void testUpdateCycleSuccess() throws Exception {
        setupManager();

        PerformanceCycleRequest req = new PerformanceCycleRequest();
        req.setName("Q2 2026 Updated");
        req.setStartDate(LocalDate.of(2026, 4, 1));
        req.setEndDate(LocalDate.of(2026, 6, 30));
        req.setStatus("CLOSED");

        PerformanceCycle cycle = new PerformanceCycle();
        cycle.setId(2L);
        cycle.setName("Q2 2026 Updated");
        cycle.setStatus("CLOSED");

        PerformanceCycleResponse resp = new PerformanceCycleResponse(cycle);
        resp.enrichGoalStats(0, 0, 0, 0);
        resp.enrichReviewStats(0, 0);

        when(performanceService.updateCycle(eq(2L), any(PerformanceCycleRequest.class))).thenReturn(Optional.of(resp));

        mockMvc.perform(put("/api/performances/cycles/2")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    // ── 3. GOALS ──────────────────────────────────────────────────────────────
    @Test
    public void testCreateGoalSuccess() throws Exception {
        setupManager();

        PerformanceGoalRequest req = new PerformanceGoalRequest();
        req.setEmployeeId(1L);
        req.setTitle("Improve code review quality");
        req.setDueDate(LocalDate.of(2026, 9, 30));

        PerformanceGoalResponse goal = new PerformanceGoalResponse();
        goal.setId(5L);
        goal.setEmployeeId(1L);
        goal.setTitle("Improve code review quality");
        goal.setProgressPercent(0);
        goal.setStatus("IN_PROGRESS");

        when(performanceService.createGoal(any(PerformanceGoalRequest.class))).thenReturn(goal);

        mockMvc.perform(post("/api/performances/goals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Improve code review quality"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    public void testUpdateGoalProgressSuccess() throws Exception {
        setupManager();

        PerformanceGoalResponse goal = new PerformanceGoalResponse();
        goal.setId(5L);
        goal.setProgressPercent(75);
        goal.setStatus("IN_PROGRESS");

        when(performanceService.updateGoalProgress(5L, 75)).thenReturn(Optional.of(goal));

        mockMvc.perform(patch("/api/performances/goals/5/progress")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"progressPercent\": 75}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.progressPercent").value(75));
    }

    @Test
    public void testDeleteGoalSuccess() throws Exception {
        setupManager();

        when(performanceService.deleteGoal(5L)).thenReturn(true);

        mockMvc.perform(delete("/api/performances/goals/5")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testDeleteGoalNotFound() throws Exception {
        setupManager();

        when(performanceService.deleteGoal(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/performances/goals/99")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ── 4. SELF-REVIEWS ───────────────────────────────────────────────────────
    @Test
    public void testSubmitSelfReviewSuccess() throws Exception {
        setupEmployee();

        SelfReviewRequest req = new SelfReviewRequest();
        req.setEmployeeId(2L);
        req.setAchievements("Delivered project on time");
        req.setComments("Happy with the results");
        req.setRating(4);

        PerformanceReviewResponse review = new PerformanceReviewResponse();
        review.setId(10L);
        review.setEmployeeId(2L);
        review.setReviewType("SELF");
        review.setRating(4);
        review.setStatus("SUBMITTED");

        when(performanceService.submitSelfReview(any(SelfReviewRequest.class))).thenReturn(review);

        mockMvc.perform(post("/api/performances/self-reviews")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviewType").value("SELF"))
                .andExpect(jsonPath("$.data.rating").value(4));
    }

    // ── 5. MANAGER REVIEWS ────────────────────────────────────────────────────
    @Test
    public void testSubmitManagerReviewSuccess() throws Exception {
        setupManager();

        ManagerReviewRequest req = new ManagerReviewRequest();
        req.setEmployeeId(2L);
        req.setReviewerId(1L);
        req.setAchievements("Exceeded targets");
        req.setRating(5);

        PerformanceReviewResponse review = new PerformanceReviewResponse();
        review.setId(11L);
        review.setReviewType("MANAGER");
        review.setRating(5);
        review.setStatus("SUBMITTED");

        when(performanceService.submitManagerReview(any(ManagerReviewRequest.class))).thenReturn(review);

        mockMvc.perform(post("/api/performances/manager-reviews")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviewType").value("MANAGER"))
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    public void testSubmitManagerReviewForbidden() throws Exception {
        setupEmployee();

        ManagerReviewRequest req = new ManagerReviewRequest();
        req.setEmployeeId(2L);
        req.setReviewerId(1L);

        mockMvc.perform(post("/api/performances/manager-reviews")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ── 6. FEEDBACKS ──────────────────────────────────────────────────────────
    @Test
    public void testGetFeedbacksManager() throws Exception {
        setupManager();

        PerformanceReviewResponse r = new PerformanceReviewResponse();
        r.setId(10L);
        r.setReviewType("SELF");
        r.setStatus("SUBMITTED");

        when(performanceService.getFeedbacks()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/performances/feedbacks")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10L));
    }

    // ── 7. FINALIZE REVIEW ────────────────────────────────────────────────────
    @Test
    public void testFinalizeReviewSuccess() throws Exception {
        setupManager();

        PerformanceReviewResponse review = new PerformanceReviewResponse();
        review.setId(11L);
        review.setStatus("FINALIZED");

        when(performanceService.finalizeReview(11L)).thenReturn(Optional.of(review));

        mockMvc.perform(post("/api/performances/reviews/11/finalize")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("FINALIZED"));
    }

    // ── 8. PIP ────────────────────────────────────────────────────────────────
    @Test
    public void testCreatePipSuccess() throws Exception {
        setupManager();

        PipRequest req = new PipRequest();
        req.setEmployeeId(2L);
        req.setImprovementPlan("Complete advanced Java training, improve PR quality");
        req.setStartDate(LocalDate.of(2026, 7, 1));
        req.setEndDate(LocalDate.of(2026, 9, 30));

        PerformancePip pip = new PerformancePip();
        pip.setId(3L);
        pip.setStatus("ACTIVE");

        PerformancePipResponse resp = new PerformancePipResponse(pip);

        when(performanceService.createPip(any(PipRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/performances/pips")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    // ── 9. REPORTS ────────────────────────────────────────────────────────────
    @Test
    public void testGetReportSuccess() throws Exception {
        setupManager();

        Map<String, Object> reportData = Map.of(
                "reportType", "summary",
                "totalGoals", 20,
                "achievedGoals", 12
        );
        when(performanceService.getReportData("summary")).thenReturn(reportData);

        mockMvc.perform(get("/api/performances/reports/summary")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportType").value("summary"))
                .andExpect(jsonPath("$.data.totalGoals").value(20));
    }

    // ── 10. NOTIFICATIONS ─────────────────────────────────────────────────────
    @Test
    public void testSendNotificationSuccess() throws Exception {
        setupManager();

        Map<String, String> body = Map.of("message", "Please complete your self-review");

        mockMvc.perform(post("/api/performances/notifications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SENT"));
    }
}
