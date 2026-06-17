package com.example.ems.performance.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.performance.dto.*;
import com.example.ems.performance.entity.Goal;
import com.example.ems.performance.repository.GoalRepository;
import com.example.ems.performance.service.GoalService;
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

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

public class GoalControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Mock private GoalService goalService;
    @Mock private GoalRepository goalRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private RoleService roleService;

    @InjectMocks
    private GoalController goalController;

    private String token = "Bearer mock-token";
    private String email = "employee@company.com";
    private User mockUser;
    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(goalController)
                .setControllerAdvice(new com.example.ems.config.GlobalExceptionHandler())
                .build();

        mockUser = new User();
        mockUser.setWorkEmail(email);
        mockUser.setFullName("John Doe");
        Role role = new Role();
        role.setName("EMPLOYEE");
        mockUser.setRole(role);

        mockEmployee = new Employee();
        mockEmployee.setId(101L);
        mockEmployee.setFullName("John Doe");
        mockEmployee.setEmail(email);

        when(jwtService.validateAccessToken(anyString())).thenReturn(true);
        when(jwtService.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(mockUser));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(mockEmployee));
        when(roleService.isSuperAdmin(email)).thenReturn(false);
        when(roleService.hasPermission(eq(email), anyString())).thenReturn(true);
    }

    @Test
    void testCreateGoal() throws Exception {
        CreateGoalRequest req = new CreateGoalRequest();
        req.setTitle("Improve API Test Coverage");
        req.setGoalType("INDIVIDUAL");
        req.setPriority("HIGH");
        req.setStartDate(LocalDate.of(2026, 7, 1));
        req.setTargetDate(LocalDate.of(2026, 12, 31));
        req.setEmployeeId(101L);

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("goalId", 501);
        mockResponse.put("goalCode", "GOAL-2026-501");
        mockResponse.put("status", "DRAFT");

        when(goalService.createGoal(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/goals")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.goalCode").value("GOAL-2026-501"));
    }

    @Test
    void testGetMyGoals() throws Exception {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("summary", Map.of("totalGoals", 1, "completedGoals", 0));
        mockResponse.put("goals", List.of());

        when(goalService.getMyGoals(email)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/goals/my")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetGoalDetails() throws Exception {
        Goal goal = new Goal();
        goal.setId(501L);
        goal.setEmployee(mockEmployee);
        
        when(goalRepository.findById(501L)).thenReturn(Optional.of(goal));
        when(goalService.getGoalDetails(501L)).thenReturn(Map.of("goalId", 501L));

        mockMvc.perform(get("/api/v1/goals/501")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.goalId").value(501));
    }

    @Test
    void testUpdateGoalProgress() throws Exception {
        Goal goal = new Goal();
        goal.setId(501L);
        goal.setEmployee(mockEmployee);

        UpdateGoalProgressRequest req = new UpdateGoalProgressRequest();
        req.setProgress(75);
        req.setComment("Done testing");

        Map<String, Object> mockResponse = Map.of("goalId", 501, "currentProgress", 75);

        when(goalRepository.findById(501L)).thenReturn(Optional.of(goal));
        when(goalService.updateGoalProgress(eq(501L), any(), anyString())).thenReturn(mockResponse);

        mockMvc.perform(patch("/api/v1/goals/501/progress")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentProgress").value(75));
    }

    @Test
    void testSubmitGoal() throws Exception {
        Goal goal = new Goal();
        goal.setId(501L);
        goal.setEmployee(mockEmployee);

        when(goalRepository.findById(501L)).thenReturn(Optional.of(goal));

        mockMvc.perform(post("/api/v1/goals/501/submit")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Goal submitted for manager review"));
    }

    @Test
    void testApproveGoal() throws Exception {
        Goal goal = new Goal();
        goal.setId(501L);
        goal.setEmployee(mockEmployee);
        goal.setManager(mockEmployee);

        when(goalRepository.findById(501L)).thenReturn(Optional.of(goal));

        GoalDecisionRequest dec = new GoalDecisionRequest();
        dec.setComments("Looks good");

        mockMvc.perform(post("/api/v1/goals/501/approve")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dec)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testRejectGoal() throws Exception {
        Goal goal = new Goal();
        goal.setId(501L);
        goal.setEmployee(mockEmployee);
        goal.setManager(mockEmployee);

        when(goalRepository.findById(501L)).thenReturn(Optional.of(goal));

        GoalDecisionRequest dec = new GoalDecisionRequest();
        dec.setReason("Need measurable KPIs");

        mockMvc.perform(post("/api/v1/goals/501/reject")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dec)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGoalComments() throws Exception {
        Goal goal = new Goal();
        goal.setId(501L);
        goal.setEmployee(mockEmployee);

        when(goalRepository.findById(501L)).thenReturn(Optional.of(goal));
        when(goalService.addComment(eq(501L), any(), any())).thenReturn(Map.of("commentId", 101L));

        GoalCommentRequest req = new GoalCommentRequest();
        req.setComment("Good progress");

        mockMvc.perform(post("/api/v1/goals/501/comments")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentId").value(101));
    }

    @Test
    void testGetGoalHistory() throws Exception {
        Goal goal = new Goal();
        goal.setId(501L);
        goal.setEmployee(mockEmployee);

        when(goalRepository.findById(501L)).thenReturn(Optional.of(goal));
        when(goalService.getHistory(501L)).thenReturn(List.of(Map.of("progress", 30)));

        mockMvc.perform(get("/api/v1/goals/501/history")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGoalDashboard() throws Exception {
        when(goalService.getDashboardData(eq(email), anyBoolean())).thenReturn(Map.of("totalGoals", 15));

        mockMvc.perform(get("/api/v1/goals/dashboard")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalGoals").value(15));
    }

    @Test
    void testGoalAnalytics() throws Exception {
        when(goalService.getAnalytics()).thenReturn(Map.of("completionRate", 82.5));

        mockMvc.perform(get("/api/v1/goals/analytics")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.completionRate").value(82.5));
    }

    @Test
    void testCreateGoalValidationError() throws Exception {
        CreateGoalRequest req = new CreateGoalRequest();
        // title, startDate, targetDate are null / missing

        mockMvc.perform(post("/api/v1/goals")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VAL_001"))
                .andExpect(jsonPath("$.message").value(containsString("Validation failed")));
    }

    @Test
    void testUnauthorizedRequestInvalidToken() throws Exception {
        when(jwtService.validateAccessToken("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/api/v1/goals/my")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_014"))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void testNonExistentGoalId() throws Exception {
        when(goalRepository.findById(99999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/goals/99999")
                .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("GOAL_001"))
                .andExpect(jsonPath("$.message").value("Goal not found with ID: 99999"));
    }

    @Test
    void testUpdateGoalProgressMissingProgress() throws Exception {
        Goal goal = new Goal();
        goal.setId(501L);
        goal.setEmployee(mockEmployee);
        when(goalRepository.findById(501L)).thenReturn(Optional.of(goal));

        UpdateGoalProgressRequest req = new UpdateGoalProgressRequest();
        // progress is null

        mockMvc.perform(patch("/api/v1/goals/501/progress")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VAL_001"))
                .andExpect(jsonPath("$.message").value("progress field is required"));
    }

    @Test
    void testRegularEmployeeAccessingAnalyticsForbidden() throws Exception {
        when(roleService.hasPermission(email, "goal.analytics.read")).thenReturn(false);

        mockMvc.perform(get("/api/v1/goals/analytics")
                .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"))
                .andExpect(jsonPath("$.message").value("Access Denied: Requires 'goal.analytics.read' permission."));
    }

    @Test
    void testRegularEmployeeApprovingGoalForbidden() throws Exception {
        Goal goal = new Goal();
        goal.setId(501L);
        goal.setEmployee(mockEmployee);
        
        Employee differentManager = new Employee();
        differentManager.setId(999L);
        goal.setManager(differentManager);

        when(goalRepository.findById(501L)).thenReturn(Optional.of(goal));
        when(roleService.hasPermission(email, "goal.approve")).thenReturn(false);

        GoalDecisionRequest dec = new GoalDecisionRequest();
        dec.setComments("Approve it");

        mockMvc.perform(post("/api/v1/goals/501/approve")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dec)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"))
                .andExpect(jsonPath("$.message").value("Access Denied: Requires 'goal.approve' permission."));
    }
}
