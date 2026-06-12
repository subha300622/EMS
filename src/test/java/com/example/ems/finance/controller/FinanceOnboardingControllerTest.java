package com.example.ems.finance.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.finance.entity.FinanceOnboarding;
import com.example.ems.finance.service.FinanceOnboardingService;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FinanceOnboardingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FinanceOnboardingService service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private FinanceOnboardingController controller;

    private User adminUser;
    private String token = "Bearer mock-token";
    private String email = "admin@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        adminUser = new User();
        adminUser.setWorkEmail(email);
        Role role = new Role();
        role.setName("ADMIN");
        adminUser.setRole(role);
    }

    private void mockAuthSuccess() {
        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(adminUser));
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
    public void testCreateOnboardingSuccess() throws Exception {
        mockAuthSuccess();
        FinanceOnboarding ob = new FinanceOnboarding();
        ob.setId(1L);
        ob.setStatus("DRAFT");

        when(service.createOnboarding()).thenReturn(ob);

        mockMvc.perform(post("/api/v1/finance/onboarding")
                .header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    public void testGetCurrentOnboardingSuccess() throws Exception {
        mockAuthSuccess();
        FinanceOnboarding ob = new FinanceOnboarding();
        ob.setId(1L);

        when(service.getCurrentOnboarding()).thenReturn(Optional.of(ob));

        mockMvc.perform(get("/api/v1/finance/onboarding/current")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testGetOnboardingDetailsSuccess() throws Exception {
        mockAuthSuccess();
        FinanceOnboarding ob = new FinanceOnboarding();
        ob.setId(1L);

        when(service.getOnboardingById(1L)).thenReturn(Optional.of(ob));

        mockMvc.perform(get("/api/v1/finance/onboarding/1")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetProgressSuccess() throws Exception {
        mockAuthSuccess();
        FinanceOnboarding ob = new FinanceOnboarding();
        ob.setId(1L);
        ob.setStatus("DRAFT");

        when(service.getOnboardingById(1L)).thenReturn(Optional.of(ob));
        when(service.calculateProgress(any(FinanceOnboarding.class))).thenReturn(50);

        mockMvc.perform(get("/api/v1/finance/onboarding/1/progress")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.progressPercentage").value(50));
    }

    @Test
    public void testUpdateCompanySuccess() throws Exception {
        mockAuthSuccess();
        FinanceOnboarding ob = new FinanceOnboarding();
        ob.setId(1L);
        ob.setCompanyName("Acme Corp");

        Map<String, String> payload = Map.of("companyName", "Acme Corp");
        when(service.updateCompany(eq(1L), any())).thenReturn(ob);

        mockMvc.perform(patch("/api/v1/finance/onboarding/1/company")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.companyName").value("Acme Corp"));
    }

    @Test
    public void testUpdateBankAccountSuccess() throws Exception {
        mockAuthSuccess();
        FinanceOnboarding ob = new FinanceOnboarding();
        ob.setId(1L);
        ob.setBankName("Chase");

        Map<String, String> payload = Map.of("bankName", "Chase");
        when(service.updateBankAccount(eq(1L), any())).thenReturn(ob);

        mockMvc.perform(patch("/api/v1/finance/onboarding/1/bank-account")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bankName").value("Chase"));
    }

    @Test
    public void testUpdateTaxSuccess() throws Exception {
        mockAuthSuccess();
        FinanceOnboarding ob = new FinanceOnboarding();
        ob.setId(1L);
        ob.setTaxRegime("Standard");

        Map<String, Object> payload = Map.of("taxRegime", "Standard");
        when(service.updateTax(eq(1L), any())).thenReturn(ob);

        mockMvc.perform(patch("/api/v1/finance/onboarding/1/tax")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taxRegime").value("Standard"));
    }

    @Test
    public void testUpdatePaymentMethodSuccess() throws Exception {
        mockAuthSuccess();
        FinanceOnboarding ob = new FinanceOnboarding();
        ob.setId(1L);
        ob.setPaymentMethod("ACH");

        Map<String, String> payload = Map.of("paymentMethod", "ACH");
        when(service.updatePaymentMethod(eq(1L), any())).thenReturn(ob);

        mockMvc.perform(patch("/api/v1/finance/onboarding/1/payment-method")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentMethod").value("ACH"));
    }

    @Test
    public void testUpdatePayrollSuccess() throws Exception {
        mockAuthSuccess();
        FinanceOnboarding ob = new FinanceOnboarding();
        ob.setId(1L);
        ob.setPayrollCycleStartDay(1);

        Map<String, Integer> payload = Map.of("payrollCycleStartDay", 1);
        when(service.updatePayroll(eq(1L), any())).thenReturn(ob);

        mockMvc.perform(patch("/api/v1/finance/onboarding/1/payroll")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.payrollCycleStartDay").value(1));
    }

    @Test
    public void testUpdateBudgetSuccess() throws Exception {
        mockAuthSuccess();
        FinanceOnboarding ob = new FinanceOnboarding();
        ob.setId(1L);
        ob.setBudgetTotal(BigDecimal.valueOf(100000));

        Map<String, Object> payload = Map.of("budgetTotal", 100000);
        when(service.updateBudget(eq(1L), any())).thenReturn(ob);

        mockMvc.perform(patch("/api/v1/finance/onboarding/1/budget")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.budgetTotal").value(100000));
    }

    @Test
    public void testValidateSuccess() throws Exception {
        mockAuthSuccess();
        when(service.validateOnboarding(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/v1/finance/onboarding/1/validate")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testValidateFailure() throws Exception {
        mockAuthSuccess();
        when(service.validateOnboarding(1L)).thenReturn(List.of("Company name is required"));

        mockMvc.perform(post("/api/v1/finance/onboarding/1/validate")
                .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testCompleteSuccess() throws Exception {
        mockAuthSuccess();
        FinanceOnboarding ob = new FinanceOnboarding();
        ob.setId(1L);
        ob.setStatus("COMPLETED");

        when(service.completeOnboarding(1L)).thenReturn(ob);

        mockMvc.perform(post("/api/v1/finance/onboarding/1/complete")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    public void testListOnboardingRecordsSuccess() throws Exception {
        mockAuthSuccess();
        FinanceOnboarding ob = new FinanceOnboarding();
        ob.setId(1L);

        when(service.listAll()).thenReturn(List.of(ob));

        mockMvc.perform(get("/api/v1/finance/onboarding")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    public void testArchiveOnboardingSuccess() throws Exception {
        mockAuthSuccess();
        when(service.archiveOnboarding(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/finance/onboarding/1")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
