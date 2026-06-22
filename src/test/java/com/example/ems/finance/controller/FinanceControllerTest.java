package com.example.ems.finance.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.finance.service.FinanceService;
import com.example.ems.security.service.JwtService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FinanceControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FinanceService financeService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private FinanceController financeController;

    private User superAdminUser;
    private String token = "Bearer mock-token";
    private String email = "admin@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(financeController).build();

        superAdminUser = new User();
        superAdminUser.setWorkEmail(email);
        Role role = new Role();
        role.setName("SUPER_ADMIN");
        superAdminUser.setRole(role);
    }

    private void mockAuthSuccess() {
        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(superAdminUser));
        when(roleService.hasRole(any(User.class), any(String.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            String r = invocation.getArgument(1);
            return u != null && u.getRole() != null && r.equals(u.getRole().getName());
        });
        when(roleService.hasRoleOrGreater(any(User.class), any(String.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            String r = invocation.getArgument(1);
            if (u == null || u.getRole() == null || r == null) return false;
            java.util.Map<String, Integer> hierarchy = java.util.Map.of(
                "SUPER_ADMIN", 1,
                "ADMIN", 2,
                "HR", 3,
                "MANAGER", 4,
                "FINANCE", 5,
                "EMPLOYEE", 6
            );
            Integer userLevel = hierarchy.get(u.getRole().getName());
            Integer targetLevel = hierarchy.get(r);
            if (userLevel == null || targetLevel == null) return false;
            return userLevel <= targetLevel;
        });
    }

    @Test
    public void testGetDashboardSuccess() throws Exception {
        mockAuthSuccess();
        when(financeService.getDashboardData()).thenReturn(Map.of("totalExpenses", BigDecimal.valueOf(150)));

        mockMvc.perform(get("/api/v1/finance/dashboard")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalExpenses").value(150));
    }

    @Test
    public void testGetMonthlyAnalyticsSuccess() throws Exception {
        mockAuthSuccess();
        when(financeService.getMonthlyAnalytics()).thenReturn(List.of(Map.of("month", "Jun", "expense", BigDecimal.valueOf(200))));

        mockMvc.perform(get("/api/v1/finance/analytics/monthly")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].month").value("Jun"));
    }

    @Test
    public void testGetRecentTransactionsSuccess() throws Exception {
        mockAuthSuccess();
        when(financeService.getRecentTransactions()).thenReturn(List.of(Map.of("type", "EXPENSE", "amount", BigDecimal.valueOf(100))));

        mockMvc.perform(get("/api/v1/finance/transactions/recent")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value("EXPENSE"));
    }


    @Test
    public void testGetSalarySummarySuccess() throws Exception {
        mockAuthSuccess();
        when(financeService.getSalarySummary()).thenReturn(Map.of("totalNetPay", BigDecimal.valueOf(5000)));

        mockMvc.perform(get("/api/v1/finance/salary/summary")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalNetPay").value(5000));
    }

    @Test
    public void testGetPendingPaymentsSuccess() throws Exception {
        mockAuthSuccess();
        when(financeService.getPendingPayments()).thenReturn(List.of(Map.of("type", "PAYROLL", "amount", BigDecimal.valueOf(3000))));

        mockMvc.perform(get("/api/v1/finance/payments/pending")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value("PAYROLL"));
    }

    @Test
    public void testGetCustomReportSuccess() throws Exception {
        mockAuthSuccess();
        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();
        when(financeService.getCustomReport(start, end, "ALL")).thenReturn(Map.of("expenseCount", 1L));

        mockMvc.perform(get("/api/v1/finance/report")
                .header("Authorization", token)
                .param("startDate", start.toString())
                .param("endDate", end.toString())
                .param("type", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.expenseCount").value(1));
    }

    @Test
    public void testGetDashboardUnauthorized() throws Exception {
        when(jwtService.validateAccessToken("mock-token")).thenReturn(false);

        mockMvc.perform(get("/api/v1/finance/dashboard")
                .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetDashboardForbidden() throws Exception {
        User employee = new User();
        employee.setWorkEmail("emp@example.com");
        Role role = new Role();
        role.setName("EMPLOYEE");
        employee.setRole(role);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn("emp@example.com");
        when(userRepository.findByWorkEmail("emp@example.com")).thenReturn(Optional.of(employee));
        when(roleService.hasPermission("emp@example.com", "reports.finance")).thenReturn(false);
        when(roleService.hasPermission("emp@example.com", "expense.manage")).thenReturn(false);

        mockMvc.perform(get("/api/v1/finance/dashboard")
                .header("Authorization", token))
                .andExpect(status().isForbidden());
    }
}
