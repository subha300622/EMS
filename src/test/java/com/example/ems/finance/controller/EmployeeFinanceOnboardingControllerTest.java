package com.example.ems.finance.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import com.example.ems.finance.service.EmployeeFinanceOnboardingService;
import com.example.ems.onboarding.controller.DashboardController;
import com.example.ems.onboarding.controller.ApprovalController;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmployeeFinanceOnboardingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EmployeeFinanceOnboardingService service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private FinanceAnalyticsController analyticsController;

    @InjectMocks
    private DashboardController dashboardController;

    @InjectMocks
    private ApprovalController approvalController;

    private User financeUser;
    private String token = "Bearer mock-token";
    private String email = "finance@company.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController, dashboardController, approvalController).build();

        financeUser = new User();
        financeUser.setWorkEmail(email);
        Role role = new Role();
        role.setName("FINANCE");
        financeUser.setRole(role);
    }

    private void mockAuthSuccess() {
        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(financeUser));
        when(roleService.hasRoleOrGreater(any(User.class), any(String.class))).thenReturn(true);
    }

    @Test
    public void testGetDashboardSummarySuccess() throws Exception {
        mockAuthSuccess();
        when(service.getDashboardSummary()).thenReturn(Map.of(
            "pendingVerification", 5L,
            "salaryAssignmentPending", 10L,
            "payrollActivationPending", 2L,
            "completed", 120L
        ));

        mockMvc.perform(get("/api/v1/dashboard?role=FINANCE")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pendingVerification").value(5))
                .andExpect(jsonPath("$.data.completed").value(120));
    }

    @Test
    public void testGetPendingListSuccess() throws Exception {
        mockAuthSuccess();
        when(service.getPendingReviews(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/finance/analytics/pending-reviews")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testCalculateCtcSuccess() throws Exception {
        mockAuthSuccess();
        when(service.calculateCtcBreakup(any())).thenReturn(Map.of("monthlyCtc", 100000, "basicSalary", 50000));

        mockMvc.perform(post("/api/v1/finance/analytics/calculate-ctc")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ctc\":1200000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.monthlyCtc").value(100000))
                .andExpect(jsonPath("$.data.basicSalary").value(50000));
    }

    @Test
    public void testGetDashboardForbiddenForEmployee() throws Exception {
        User employeeUser = new User();
        employeeUser.setWorkEmail("employee@company.com");
        Role role = new Role();
        role.setName("EMPLOYEE");
        employeeUser.setRole(role);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn("employee@company.com");
        when(userRepository.findByWorkEmail("employee@company.com")).thenReturn(Optional.of(employeeUser));
        when(roleService.hasRoleOrGreater(any(User.class), any(String.class))).thenReturn(false);
        when(roleService.hasPermission(any(String.class), any(String.class))).thenReturn(false);

        mockMvc.perform(get("/api/v1/dashboard?role=FINANCE")
                .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetPendingReviewsWithFilters() throws Exception {
        mockAuthSuccess();
        EmployeeFinanceOnboarding ob = new EmployeeFinanceOnboarding();
        ob.setId(1L);
        ob.setStatus("PENDING");

        when(service.getPendingReviews("IT", "PENDING")).thenReturn(List.of(ob));

        mockMvc.perform(get("/api/v1/finance/analytics/pending-reviews")
                .header("Authorization", token)
                .param("department", "IT")
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }
}
