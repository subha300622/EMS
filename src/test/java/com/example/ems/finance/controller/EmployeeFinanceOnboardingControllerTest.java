package com.example.ems.finance.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.onboarding.repository.OnboardingRepository;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.employee.entity.Employee;
import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import com.example.ems.finance.service.EmployeeFinanceOnboardingService;
import com.example.ems.finance.validation.FinanceCommandValidator;
import com.example.ems.finance.handler.FinanceCommandRouter;
import com.example.ems.finance.dto.FinanceCommandEnvelope;
import com.example.ems.onboarding.controller.DashboardController;
import com.example.ems.onboarding.controller.ApprovalController;
import com.example.ems.security.service.JwtService;
import com.example.ems.common.service.IdempotencyService;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private com.example.ems.finance.repository.EmployeeFinanceOnboardingRepository repository;

    @Mock
    private OnboardingRepository onboardingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private FinanceCommandValidator validator;

    @Mock
    private FinanceCommandRouter router;

    @InjectMocks
    private EmployeeFinanceOnboardingController controller;

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
        mockMvc = MockMvcBuilders.standaloneSetup(controller, analyticsController, dashboardController, approvalController).build();

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

    private void mockOnboardingResolve(Long coreOnboardingId, Long employeeId) {
        Employee employee = new Employee();
        employee.setId(employeeId);

        Onboarding onboarding = new Onboarding();
        onboarding.setId(coreOnboardingId);
        onboarding.setEmployee(employee);

        when(onboardingRepository.findById(coreOnboardingId)).thenReturn(Optional.of(onboarding));
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
    public void testGetBankDetailsSuccess() throws Exception {
        mockAuthSuccess();
        mockOnboardingResolve(3L, 3L);
        EmployeeFinanceOnboarding ob = new EmployeeFinanceOnboarding();
        ob.setBankName("HDFC Bank");
        ob.setBankAccountNumber("123456");
        ob.setBankIfsc("HDFC000123");
        ob.setBankVerificationStatus("PENDING");

        when(service.getByEmployeeId(3L)).thenReturn(ob);

        mockMvc.perform(get("/api/v1/onboarding/3/finance/bank-details")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bankName").value("HDFC Bank"))
                .andExpect(jsonPath("$.data.bankAccountNumber").value("123456"));
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

    @Test
    public void testAssignSalaryStructureWithTemplate() throws Exception {
        mockAuthSuccess();
        mockOnboardingResolve(3L, 3L);
        EmployeeFinanceOnboarding ob = new EmployeeFinanceOnboarding();
        ob.setId(1L);
        EmployeeFinanceOnboarding updated = new EmployeeFinanceOnboarding();
        updated.setId(1L);
        updated.setBasicSalary(BigDecimal.valueOf(50000.00));
        updated.setHra(BigDecimal.valueOf(25000.00));
        updated.setAllowances(BigDecimal.valueOf(10000.00));
        updated.setSalaryStructureAssigned(true);

        when(service.getByEmployeeId(3L)).thenReturn(ob);
        when(repository.findByEmployeeId(3L)).thenReturn(Optional.of(ob));
        when(router.route(eq(3L), any(FinanceCommandEnvelope.class), any(String.class), any())).thenReturn(updated);

        mockMvc.perform(post("/api/v1/onboarding/3/finance/command")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"ASSIGN_SALARY\",\"version\":1,\"payload\":{\"salaryStructureId\":1,\"effectiveDate\":\"2026-06-20\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.basicSalary").value(50000.00))
                .andExpect(jsonPath("$.data.salaryStructureAssigned").value(true));
    }

    @Test
    public void testGetSalaryPreviewSuccess() throws Exception {
        mockAuthSuccess();
        mockOnboardingResolve(3L, 3L);
        EmployeeFinanceOnboarding ob = new EmployeeFinanceOnboarding();
        ob.setId(1L);

        Map<String, Object> previewMap = Map.of(
            "basic", 50000.00,
            "hra", 25000.00,
            "allowances", 10000.00,
            "grossSalary", 85000.00,
            "pf", 1800.00,
            "netSalary", 83200.00
        );

        when(service.getByEmployeeId(3L)).thenReturn(ob);
        when(service.getSalaryPreview(1L)).thenReturn(previewMap);

        mockMvc.perform(get("/api/v1/onboarding/3/finance/salary-preview")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.basic").value(50000.00))
                .andExpect(jsonPath("$.data.grossSalary").value(85000.00))
                .andExpect(jsonPath("$.data.pf").value(1800.00))
                .andExpect(jsonPath("$.data.netSalary").value(83200.00));
    }
}
