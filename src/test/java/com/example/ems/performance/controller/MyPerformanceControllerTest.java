package com.example.ems.performance.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.performance.dto.*;
import com.example.ems.performance.service.MyPerformanceService;
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

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class MyPerformanceControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MyPerformanceService performanceService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private MyPerformanceController performanceController;

    private String token = "Bearer mock-token";
    private String email = "employee@company.com";
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(performanceController).build();

        mockUser = new User();
        mockUser.setWorkEmail(email);
        Role role = new Role();
        role.setName("EMPLOYEE");
        mockUser.setRole(role);

        when(jwtService.validateAccessToken(anyString())).thenReturn(true);
        when(jwtService.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(mockUser));
        when(roleService.isSuperAdmin(email)).thenReturn(false);
        when(roleService.hasPermission(eq(email), anyString())).thenReturn(true);
    }



    @Test
    void testGetDashboard() throws Exception {
        MyPerformanceDashboardResponse mockResp = new MyPerformanceDashboardResponse();
        mockResp.setSelfReviewDueDate("April 25");
        mockResp.setCurrentRating(4.6);
        mockResp.setGoalsMet(8);
        mockResp.setTotalGoals(10);
        mockResp.setReviewStatus("Open");
        mockResp.setMyBand("A+");
        mockResp.setSelfAssessment(new MyPerformanceDashboardResponse.SelfAssessmentRatings(
            4.5, 4.8, 4.6, 4.4, 4.7, 4.6
        ));

        when(performanceService.getDashboard(email)).thenReturn(mockResp);

        mockMvc.perform(get("/api/v1/my-performance/dashboard")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.selfReviewDueDate").value("April 25"))
                .andExpect(jsonPath("$.data.currentRating").value(4.6))
                .andExpect(jsonPath("$.data.goalsMet").value(8))
                .andExpect(jsonPath("$.data.totalGoals").value(10))
                .andExpect(jsonPath("$.data.reviewStatus").value("Open"))
                .andExpect(jsonPath("$.data.myBand").value("A+"))
                .andExpect(jsonPath("$.data.selfAssessment.leadershipOwnership").value(4.5))
                .andExpect(jsonPath("$.data.selfAssessment.technicalExcellence").value(4.8))
                .andExpect(jsonPath("$.data.selfAssessment.deliveryManagement").value(4.6))
                .andExpect(jsonPath("$.data.selfAssessment.communicationInfluence").value(4.4))
                .andExpect(jsonPath("$.data.selfAssessment.teamMentorship").value(4.7))
                .andExpect(jsonPath("$.data.selfAssessment.innovationInitiative").value(4.6));
    }

    @Test
    void testGetReviewCycles() throws Exception {
        when(performanceService.getReviewCycles(email)).thenReturn(new ReviewCyclesResponse());

        mockMvc.perform(get("/api/v1/my-performance/reviews")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testSubmitSelfAssessment() throws Exception {
        SelfAssessmentRequest req = new SelfAssessmentRequest();
        req.setSelfRating(5.0);
        when(performanceService.submitSelfAssessment(eq(email), eq(1L), any()))
                .thenReturn(new SelfAssessmentResponse("Success", "SUBMITTED", "now", new ArrayList<>()));

        mockMvc.perform(post("/api/v1/my-performance/reviews/1/self-assessment")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetFeedback() throws Exception {
        when(performanceService.getFeedback(email)).thenReturn(new FeedbackListResponse());

        mockMvc.perform(get("/api/v1/my-performance/feedback")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetHistory() throws Exception {
        when(performanceService.getHistory(email)).thenReturn(new AppraisalHistoryResponse());

        mockMvc.perform(get("/api/v1/my-performance/history")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetCompetencies() throws Exception {
        when(performanceService.getCompetencies(email)).thenReturn(new CompetenciesResponse());

        mockMvc.perform(get("/api/v1/my-performance/competencies")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetTimeline() throws Exception {
        when(performanceService.getTimeline(email)).thenReturn(new PerformanceTimelineResponse());

        mockMvc.perform(get("/api/v1/my-performance/timeline")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetPolicies() throws Exception {
        when(performanceService.getPolicies()).thenReturn(new PerformancePolicyResponse());

        mockMvc.perform(get("/api/v1/my-performance/policies")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
