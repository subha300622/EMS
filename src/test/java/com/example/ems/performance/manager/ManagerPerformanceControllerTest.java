package com.example.ems.performance.manager;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.exception.AccessDeniedException;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.performance.manager.calculator.PerformanceScoreCalculator;
import com.example.ems.performance.manager.controller.ManagerPerformanceController;
import com.example.ems.performance.manager.dto.*;
import com.example.ems.performance.manager.entity.CompetencyRating;
import com.example.ems.performance.manager.entity.PerformanceGoal;
import com.example.ems.performance.manager.entity.ReviewStatus;
import com.example.ems.performance.manager.service.ManagerPerformanceService;
import com.example.ems.performance.manager.validator.ReviewStateValidator;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ManagerPerformanceControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ManagerPerformanceService performanceService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ManagerPerformanceController controller;

    private User managerUser;
    private final String managerEmail = "manager@example.com";
    private final String token = "mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        managerUser = new User();
        managerUser.setWorkEmail(managerEmail);

        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(managerEmail);
        when(userRepository.findByWorkEmail(managerEmail)).thenReturn(Optional.of(managerUser));
    }

    // ── UNIT TESTS ──────────────────────────────────────────────────────────

    @Test
    public void testScoreCalculator() {
        PerformanceScoreCalculator calculator = new PerformanceScoreCalculator();

        // 1. Goal Score Calculator
        List<PerformanceGoal> goals = new ArrayList<>();
        PerformanceGoal g1 = new PerformanceGoal();
        g1.setProgress(100);
        g1.setWeight(50);

        PerformanceGoal g2 = new PerformanceGoal();
        g2.setProgress(80);
        g2.setWeight(50);

        goals.add(g1);
        goals.add(g2);

        double goalScore = calculator.calculateGoalScore(goals);
        assertEquals(4.5, goalScore, 0.01);

        // 2. Competency Rating Calculator
        List<CompetencyRating> comps = new ArrayList<>();
        CompetencyRating c1 = new CompetencyRating();
        c1.setManagerScore(5);
        CompetencyRating c2 = new CompetencyRating();
        c2.setManagerScore(4);

        comps.add(c1);
        comps.add(c2);

        double compScore = calculator.calculateCompetencyScore(comps);
        assertEquals(4.5, compScore, 0.01);

        // 3. Final Score
        double finalScore = calculator.calculateFinalScore(4.5, 4.6);
        assertEquals(4.56, finalScore, 0.01);

        // 4. Labels
        assertEquals("Exceptional", calculator.getFinalScoreLabel(4.56));
        assertEquals("Good", calculator.getFinalScoreLabel(4.0));
        assertEquals("Needs Improvement", calculator.getFinalScoreLabel(3.4));
    }

    @Test
    public void testStateValidator() {
        ReviewStateValidator validator = new ReviewStateValidator();

        // Valid transitions
        assertDoesNotThrow(() -> validator.validateTransition(ReviewStatus.NOT_STARTED, ReviewStatus.SELF_REVIEW));
        assertDoesNotThrow(() -> validator.validateTransition(ReviewStatus.SELF_REVIEW, ReviewStatus.MANAGER_REVIEW));

        // Blocked transitions from COMPLETED
        assertThrows(BadRequestException.class,
                () -> validator.validateTransition(ReviewStatus.COMPLETED, ReviewStatus.SELF_REVIEW));
    }

    // ── CONTROLLER TESTS ──────────────────────────────────────────────────────

    @Test
    public void testGetTeamDashboardSuccess() throws Exception {
        TeamDashboardResponse response = new TeamDashboardResponse();
        response.setTotalReports(5);
        response.setCompleted(2);
        response.setPending(3);
        response.setAvgTeamRating(4.3);
        response.setTeamBand("B");

        List<TeamDashboardResponse.ReviewSummary> list = new ArrayList<>();
        TeamDashboardResponse.ReviewSummary item = new TeamDashboardResponse.ReviewSummary();
        item.setEmployeeId(101L);
        item.setEmployeeName("Arjun Mehta");
        item.setDesignation("Sr Developer");
        item.setSelfRating(4.5);
        item.setManagerRating(4.6);
        item.setGoalsMet(9);
        item.setFinalScore("Exceptional");
        item.setStatus("COMPLETED");
        list.add(item);
        response.setReviews(list);

        when(performanceService.getTeamDashboard(managerEmail, "FY2024-25")).thenReturn(response);

        mockMvc.perform(get("/api/v1/performance/team")
                .header("Authorization", "Bearer " + token)
                .param("cycle", "FY2024-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalReports").value(5))
                .andExpect(jsonPath("$.data.avgTeamRating").value(4.3))
                .andExpect(jsonPath("$.data.reviews[0].employeeName").value("Arjun Mehta"))
                .andExpect(jsonPath("$.data.reviews[0].finalScore").value("Exceptional"));
    }

    @Test
    public void testGetEmployeeReviewSuccess() throws Exception {
        ReviewDetailResponse response = new ReviewDetailResponse();
        ReviewDetailResponse.EmployeeInfo empInfo = new ReviewDetailResponse.EmployeeInfo();
        empInfo.setId(101L);
        empInfo.setName("Arjun Mehta");
        empInfo.setDepartment("Engineering");
        empInfo.setRole("Sr Developer");
        response.setEmployee(empInfo);

        ReviewDetailResponse.ReviewSummary summary = new ReviewDetailResponse.ReviewSummary();
        summary.setFinalScore("Exceptional");
        summary.setManagerRating(4.6);
        summary.setSelfRating(4.5);
        summary.setGoalsMet(9);
        response.setSummary(summary);

        List<ReviewDetailResponse.CompetencyInfo> comps = new ArrayList<>();
        ReviewDetailResponse.CompetencyInfo comp = new ReviewDetailResponse.CompetencyInfo();
        comp.setName("Technical Skills");
        comp.setSelfScore(4.0);
        comp.setManagerScore(5.0);
        comp.setFeedback("Exceptional coding standards");
        comps.add(comp);
        response.setCompetencies(comps);

        List<ReviewDetailResponse.GoalInfo> goals = new ArrayList<>();
        ReviewDetailResponse.GoalInfo goal = new ReviewDetailResponse.GoalInfo();
        goal.setTitle("Complete React Migration");
        goal.setProgress(100);
        goal.setStatus("MET");
        goals.add(goal);
        response.setGoals(goals);

        when(performanceService.getEmployeeReview(managerEmail, 101L, "FY2024-25")).thenReturn(response);

        mockMvc.perform(get("/api/v1/performance/101/review")
                .header("Authorization", "Bearer " + token)
                .param("cycle", "FY2024-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employee.name").value("Arjun Mehta"))
                .andExpect(jsonPath("$.data.competencies[0].name").value("Technical Skills"))
                .andExpect(jsonPath("$.data.goals[0].title").value("Complete React Migration"));
    }

    @Test
    public void testSaveManagerRatingSuccess() throws Exception {
        SaveManagerRatingRequest request = new SaveManagerRatingRequest();
        request.setManagerComment("Excellent performance overall");
        request.setRecommendation("PROMOTION");

        SaveManagerRatingResponse response = new SaveManagerRatingResponse();
        response.setReviewId(501L);
        response.setStatus("IN_PROGRESS");
        response.setManagerRating(4.6);
        response.setFinalScore(4.55);
        response.setUpdatedAt(LocalDateTime.of(2026, 6, 23, 15, 0, 0));

        when(performanceService.saveManagerRating(eq(managerEmail), eq(501L), any(SaveManagerRatingRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/performance/501/manager-rating")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviewId").value(501))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.finalScore").value(4.55));
    }

    @Test
    public void testSubmitFinalReviewSuccess() throws Exception {
        SubmitReviewResponse response = new SubmitReviewResponse();
        response.setReviewId(501L);
        response.setStatus("COMPLETED");
        response.setSubmittedAt(LocalDateTime.of(2026, 6, 23, 15, 10, 0));
        response.setFinalScore(4.6);

        when(performanceService.submitFinalReview(managerEmail, 501L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/performance/501/submit")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviewId").value(501))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.finalScore").value(4.6));
    }

    @Test
    public void testGetTeamSummarySuccess() throws Exception {
        TeamSummaryResponse response = new TeamSummaryResponse();
        response.setReviewsCompleted(5);
        response.setPendingReviews(4);
        response.setAvgTeamRating(4.1);
        response.setTeamBand("B");
        response.setPromotionEligibleCount(2);

        when(performanceService.getTeamSummary(managerEmail, "FY2024-25")).thenReturn(response);

        mockMvc.perform(get("/api/v1/performance/team/summary")
                .header("Authorization", "Bearer " + token)
                .param("cycle", "FY2024-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviewsCompleted").value(5))
                .andExpect(jsonPath("$.data.pendingReviews").value(4))
                .andExpect(jsonPath("$.data.promotionEligibleCount").value(2));
    }

    @Test
    public void testAccessDeniedHandling() throws Exception {
        when(performanceService.submitFinalReview(managerEmail, 501L))
                .thenThrow(new AccessDeniedException("Only the direct manager can rate this employee."));

        mockMvc.perform(post("/api/v1/performance/501/submit")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
    }
}
