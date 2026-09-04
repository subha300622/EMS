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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RazorpayXPaymentIntegrationTest {

    private MockMvc salaryComponentMockMvc;
    private MockMvc salaryStructureMockMvc;
    private MockMvc salaryAssignmentMockMvc;
    private MockMvc payrollRunMockMvc;
    private MockMvc paymentConfigMockMvc;
    private MockMvc employeeAccountMockMvc;
    private MockMvc payrollPaymentMockMvc;
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
    private PaymentConfigurationController paymentConfigurationController;

    @Autowired
    private EmployeePaymentAccountController employeePaymentAccountController;

    @Autowired
    private PayrollPaymentController payrollPaymentController;

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
        com.example.ems.config.GlobalExceptionHandler exceptionHandler = new com.example.ems.config.GlobalExceptionHandler();
        salaryComponentMockMvc = MockMvcBuilders.standaloneSetup(salaryComponentController).setControllerAdvice(exceptionHandler).build();
        salaryStructureMockMvc = MockMvcBuilders.standaloneSetup(salaryStructureController).setControllerAdvice(exceptionHandler).build();
        salaryAssignmentMockMvc = MockMvcBuilders.standaloneSetup(employeeSalaryAssignmentController).setControllerAdvice(exceptionHandler).build();
        payrollRunMockMvc = MockMvcBuilders.standaloneSetup(payrollRunController).setControllerAdvice(exceptionHandler).build();
        paymentConfigMockMvc = MockMvcBuilders.standaloneSetup(paymentConfigurationController).setControllerAdvice(exceptionHandler).build();
        employeeAccountMockMvc = MockMvcBuilders.standaloneSetup(employeePaymentAccountController).setControllerAdvice(exceptionHandler).build();
        payrollPaymentMockMvc = MockMvcBuilders.standaloneSetup(payrollPaymentController).setControllerAdvice(exceptionHandler).build();
        webhookMockMvc = MockMvcBuilders.standaloneSetup(razorpayXWebhookController).setControllerAdvice(exceptionHandler).build();

        organization = new Organization();
        organization.setName("FinTech Global " + System.currentTimeMillis());
        organization.setOrganizationCode("FINTECH_" + System.currentTimeMillis());
        organization = organizationRepository.save(organization);

        employee = new Employee();
        employee.setFullName("Alice Johnson");
        employee.setEmail("alice." + System.currentTimeMillis() + "@fintech.com");
        employee.setEmployeeId("EMP_PAY_" + System.currentTimeMillis());
        employee.setOrganization(organization);
        employee = employeeRepository.save(employee);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private String calculateHmacSha256(String data, String secret) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    @Test
    @DisplayName("Complete RazorpayX Payment Flow: Config -> Account -> Finalize Run -> Payout -> Webhook Reconciliation")
    void testCompleteRazorpayXPaymentLifecycle() throws Exception {
        TenantContext.setCurrentTenant(organization.getId());

        // 1. Configure Organization Payment Settings
        String webhookSecret = "whsec_test_secret_key_123456";
        PaymentConfigRequest configReq = new PaymentConfigRequest(
                PaymentProviderType.RAZORPAYX,
                PaymentEnvironment.TEST,
                "rzp_test_key_12345",
                "rzp_test_secret_67890",
                "2323230055443322",
                webhookSecret
        );

        paymentConfigMockMvc.perform(post("/api/v1/payroll/payment-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provider").value("RAZORPAYX"))
                .andExpect(jsonPath("$.data.environment").value("TEST"))
                .andExpect(jsonPath("$.data.maskedAccountNumber").value("****3322"));

        // 2. Register Employee Bank Account
        EmployeePaymentAccountRequest accReq = new EmployeePaymentAccountRequest(
                PaymentProviderType.RAZORPAYX,
                PaymentAccountType.BANK_ACCOUNT,
                "987654321012",
                "HDFC0001234",
                "Alice Johnson"
        );

        employeeAccountMockMvc.perform(post("/api/v1/employees/" + employee.getId() + "/payment-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contactId").isNotEmpty())
                .andExpect(jsonPath("$.data.fundAccountId").isNotEmpty())
                .andExpect(jsonPath("$.data.maskedAccountNumber").value("****1012"));

        // 3. Setup Structure and Run
        SalaryComponentCreateRequest cBasic = new SalaryComponentCreateRequest("Basic Salary", "BASIC", "Basic", SalaryComponentType.EARNING, true, true);
        Long idBasic = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cBasic))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest("Standard Sal", "STD_SAL", "Standard", "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null);
        Long structId = objectMapper.readTree(salaryStructureMockMvc.perform(post("/api/v1/salary-structures").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(structReq))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new StructureComponentCreateRequest(idBasic, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(75000), null, null, 1))));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/activate")).andExpect(status().isOk());

        salaryAssignmentMockMvc.perform(post("/api/v1/employees/" + employee.getId() + "/salary-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EmployeeSalaryAssignmentCreateRequest(structId, LocalDate.of(2026, 1, 1), null, "Hired"))))
                .andExpect(status().isCreated());

        // 4. Create, Process & Finalize Run
        PayrollRunCreateRequest runReq = new PayrollRunCreateRequest(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR");
        Long runId = objectMapper.readTree(payrollRunMockMvc.perform(post("/api/v1/payroll/runs").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(runReq))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        payrollRunMockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/process")).andExpect(status().isOk());
        payrollRunMockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/finalize")).andExpect(status().isOk());

        // 5. Execute Payments
        MvcResult payExecResult = payrollPaymentMockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/payments/execute?mode=NEFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPayments").value(1))
                .andExpect(jsonPath("$.data.successfulDispatches").value(1))
                .andExpect(jsonPath("$.data.failedDispatches").value(0))
                .andReturn();

        PaymentExecutionResponse payExec = objectMapper.treeToValue(objectMapper.readTree(payExecResult.getResponse().getContentAsString()).path("data"), PaymentExecutionResponse.class);
        PayrollPaymentResponse payment = payExec.getPayments().get(0);

        assertEquals("PAYROLL-" + runId + "-EMP-" + employee.getId(), payment.getIdempotencyKey());
        assertNotNull(payment.getPayoutId());
        assertEquals(PayrollPaymentStatus.PROCESSING, payment.getStatus());

        // 6. Simulate RazorpayX Webhook: payout.processed with HMAC-SHA256
        String payoutId = payment.getPayoutId();
        String webhookPayload = String.format("""
                {
                  "entity": "event",
                  "event": "payout.processed",
                  "event_id": "evt_rzpx_%s",
                  "payload": {
                    "payout": {
                      "entity": {
                        "id": "%s",
                        "status": "processed",
                        "utr": "UTR_RZPX_BANK_12345"
                      }
                    }
                  }
                }
                """, System.currentTimeMillis(), payoutId);

        String signature = calculateHmacSha256(webhookPayload, webhookSecret);

        webhookMockMvc.perform(post("/api/v1/webhooks/razorpayx")
                        .header("X-Razorpay-Signature", signature)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isOk());

        // 7. Verify Payment is reconciled to PAID with UTR, and PayrollRun is marked PAID
        PayrollPayment updatedPayment = payrollPaymentRepository.findByPayoutId(payoutId).orElseThrow();
        assertEquals(PayrollPaymentStatus.PAID, updatedPayment.getStatus());
        assertEquals("UTR_RZPX_BANK_12345", updatedPayment.getUtr());

        PayrollRun updatedRun = payrollRunRepository.findById(runId).orElseThrow();
        assertEquals(PayrollRunStatus.PAID, updatedRun.getStatus());

        // 8. Re-executing payments for the same run does NOT duplicate payouts (deterministic idempotency)
        MvcResult reExecResult = payrollPaymentMockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/payments/execute"))
                .andExpect(status().isOk())
                .andReturn();
        PaymentExecutionResponse reExec = objectMapper.treeToValue(objectMapper.readTree(reExecResult.getResponse().getContentAsString()).path("data"), PaymentExecutionResponse.class);
        assertEquals(1, reExec.getTotalPayments());
        assertEquals(1, reExec.getSuccessfulDispatches());
        assertEquals(payoutId, reExec.getPayments().get(0).getPayoutId());
    }
}
