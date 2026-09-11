package com.example.ems.payroll.integration;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.payroll.controller.*;
import com.example.ems.payroll.dto.*;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.*;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.example.ems.config.GlobalExceptionHandler;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MockPaymentProviderIntegrationTest {

    private MockMvc salaryComponentMockMvc;
    private MockMvc salaryStructureMockMvc;
    private MockMvc salaryAssignmentMockMvc;
    private MockMvc payrollRunMockMvc;
    private MockMvc employeeAccountMockMvc;
    private MockMvc webhookMockMvc;

    @Autowired
    private SalaryComponentController salaryComponentController;

    @Autowired
    private SalaryStructureController salaryStructureController;

    @Autowired
    private EmployeeSalaryAssignmentController employeeSalaryAssignmentController;

    @Autowired
    private PayrollRunController payrollRunController;

    @Autowired
    private com.example.ems.payroll.service.PaymentConfigurationService paymentConfigurationService;

    @Autowired
    private EmployeePaymentAccountController employeePaymentAccountController;

    @Autowired
    private com.example.ems.payroll.service.PayrollPaymentService payrollPaymentService;

    @Autowired
    private RazorpayXWebhookController razorpayXWebhookController;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRunRepository payrollRunRepository;

    @Autowired
    private PayrollPaymentRepository payrollPaymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Organization organization;
    private Employee employee;

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        salaryComponentMockMvc = MockMvcBuilders.standaloneSetup(salaryComponentController).setControllerAdvice(exceptionHandler).build();
        salaryStructureMockMvc = MockMvcBuilders.standaloneSetup(salaryStructureController).setControllerAdvice(exceptionHandler).build();
        salaryAssignmentMockMvc = MockMvcBuilders.standaloneSetup(employeeSalaryAssignmentController).setControllerAdvice(exceptionHandler).build();
        payrollRunMockMvc = MockMvcBuilders.standaloneSetup(payrollRunController).setControllerAdvice(exceptionHandler).build();
        employeeAccountMockMvc = MockMvcBuilders.standaloneSetup(employeePaymentAccountController).setControllerAdvice(exceptionHandler).build();
        webhookMockMvc = MockMvcBuilders.standaloneSetup(razorpayXWebhookController).setControllerAdvice(exceptionHandler).build();

        organization = new Organization();
        organization.setName("Offline Dev Corp " + System.currentTimeMillis());
        organization.setOrganizationCode("OFFLINE_" + System.currentTimeMillis());
        organization = organizationRepository.save(organization);

        employee = new Employee();
        employee.setFullName("John Mock");
        employee.setEmail("john.mock." + System.currentTimeMillis() + "@offline.com");
        employee.setEmployeeId("EMP_MOCK_" + System.currentTimeMillis());
        employee.setOrganization(organization);
        employee = employeeRepository.save(employee);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Stage 1 - Offline Mock Flow: Config -> Mock Account -> Payment Failure Simulation -> Retry -> Mock Webhook -> Paid")
    void testMockPaymentLifecycleWithRetryAndWebhook() throws Exception {
        TenantContext.setCurrentTenant(organization.getId());

        // 1. Configure MOCK Payment Provider
        PaymentConfigRequest configReq = new PaymentConfigRequest();
        configReq.setProvider(PaymentProviderType.MOCK);
        configReq.setEnvironment(PaymentEnvironment.TEST);
        configReq.setApiKey("mock_api_key");
        configReq.setApiSecret("mock_api_secret");
        configReq.setAccountNumber("2323230000000000");
        configReq.setWebhookSecret("mock_webhook_secret");

        paymentConfigurationService.saveConfiguration(configReq);

        // 2. Register Mock Employee Bank Account
        EmployeePaymentAccountRequest accReq = new EmployeePaymentAccountRequest(
                PaymentProviderType.MOCK,
                PaymentAccountType.BANK_ACCOUNT,
                "123456789012",
                "HDFC0001234",
                "John Mock"
        );

        employeeAccountMockMvc.perform(post("/api/v1/employees/" + employee.getId() + "/payment-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contactId").value(org.hamcrest.Matchers.startsWith("mock_cont_")))
                .andExpect(jsonPath("$.data.fundAccountId").value(org.hamcrest.Matchers.startsWith("mock_fa_")));

        // 3. Setup Structure, Assignment, Run, Process & Finalize
        SalaryComponentCreateRequest cBasic = new SalaryComponentCreateRequest("Basic Salary", "BASIC", "Basic", SalaryComponentType.EARNING, true, true);
        Long idBasic = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cBasic))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest("Mock Salary Structure", "MOCK_STRUCT", "Mock", "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null);
        Long structId = objectMapper.readTree(salaryStructureMockMvc.perform(post("/api/v1/salary-structures").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(structReq))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new StructureComponentCreateRequest(idBasic, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(58950), null, null, 1))));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/activate")).andExpect(status().isOk());

        salaryAssignmentMockMvc.perform(post("/api/v1/employees/" + employee.getId() + "/salary-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EmployeeSalaryAssignmentCreateRequest(structId, LocalDate.of(2026, 1, 1), null, "Assigned"))))
                .andExpect(status().isCreated());

        PayrollRunCreateRequest runReq = new PayrollRunCreateRequest(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR");
        Long runId = objectMapper.readTree(payrollRunMockMvc.perform(post("/api/v1/payroll/runs").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(runReq))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        payrollRunMockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/process")).andExpect(status().isOk());
        payrollRunMockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/finalize")).andExpect(status().isOk());

        // 4. Initiate Mock Payment with Failure Simulation
        PaymentExecutionResponse failedResp = payrollPaymentService.executePayrollPayments(runId, "MOCK", "FAILED");
        assertEquals(1, failedResp.getFailedDispatches());
        assertEquals(PayrollPaymentStatus.FAILED, failedResp.getPayments().get(0).getStatus());

        Long paymentId = failedResp.getPayments().get(0).getId();
        String idempotencyKey = failedResp.getPayments().get(0).getIdempotencyKey();
        assertEquals("PAYROLL-" + runId + "-EMP-" + employee.getId(), idempotencyKey);

        // 5. Test Payment Retry
        PayrollPaymentResponse retriedPayment = payrollPaymentService.retryPayment(paymentId);
        assertEquals(paymentId, retriedPayment.getId());
        assertEquals(idempotencyKey, retriedPayment.getIdempotencyKey());
        assertEquals(PayrollPaymentStatus.PROCESSING, retriedPayment.getStatus());
        assertTrue(retriedPayment.getPayoutId().startsWith("mock_pout_"));

        String payoutId = retriedPayment.getPayoutId();

        // 6. Test Webhook Alias endpoint (/api/v1/integrations/razorpayx/webhooks/payouts)
        String webhookPayload = String.format("""
                {
                  "event": "payout.processed",
                  "event_id": "mock_evt_%s",
                  "payload": {
                    "payout": {
                      "entity": {
                        "id": "%s",
                        "status": "processed",
                        "utr": "MOCK_UTR_SETTLED_123"
                      }
                    }
                  }
                }
                """, System.currentTimeMillis(), payoutId);

        webhookMockMvc.perform(post("/api/v1/integrations/razorpayx/webhooks/payouts")
                        .header("X-Razorpay-Signature", "mock_signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isOk());

        // 7. Verify Payment is reconciled to PAID and PayrollRun is marked PAID
        PayrollPayment updatedPayment = payrollPaymentRepository.findById(paymentId).orElseThrow();
        assertEquals(PayrollPaymentStatus.PAID, updatedPayment.getStatus());
        assertEquals("MOCK_UTR_SETTLED_123", updatedPayment.getUtr());

        PayrollRun updatedRun = payrollRunRepository.findById(runId).orElseThrow();
        assertEquals(PayrollRunStatus.PAID, updatedRun.getStatus());
    }
}
