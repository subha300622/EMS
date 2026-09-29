package com.example.ems.offboarding.service;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.repository.ApprovalWorkflowInstanceRepository;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.entity.EmployeeExit;
import com.example.ems.offboarding.entity.FnfSettlement;
import com.example.ems.offboarding.repository.EmployeeExitRepository;
import com.example.ems.offboarding.repository.ExitClearanceRepository;
import com.example.ems.offboarding.repository.ExitFnfSettlementRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FnfSettlementWorkflowTest {

    @Mock
    private ExitFnfSettlementRepository fnfRepository;

    @Mock
    private EmployeeExitRepository exitRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private com.example.ems.employee.service.EmployeeService employeeService;

    @Mock
    private ApprovalWorkflowEngineService approvalWorkflowEngineService;

    @Mock
    private ExitAuthorizationService exitAuthService;

    @Mock
    private com.example.ems.offboarding.repository.ExitFnfAuditRepository auditRepository;

    @Mock
    private ApprovalWorkflowInstanceRepository instanceRepository;

    @Mock
    private ExitClearanceRepository clearanceRepository;

    @Mock
    private FnfReadinessService readinessService;

    @org.mockito.Spy
    private FnfSnapshotService snapshotService = new FnfSnapshotService();

    @org.mockito.Spy
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @InjectMocks
    private FnfSettlementService fnfSettlementService;

    @InjectMocks
    private FnfReportService fnfReportService;

    @InjectMocks
    private ExitWorkflowEventListener eventListener;

    private Organization testOrg;
    private Employee employee;
    private EmployeeExit exit;
    private User hrUser;
    private User superAdminUser;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(10L);

        testOrg = new Organization();
        testOrg.setId(10L);
        testOrg.setName("Acme Corporation");

        employee = new Employee();
        employee.setId(101L);
        employee.setFullName("Alice Developer");
        employee.setEmail("alice@acme.com");
        employee.setEmployeeId("EMP-101");
        employee.setAnnualSalary(BigDecimal.valueOf(1200000.00)); // 100,000 / month
        employee.setOrganization(testOrg);
        employee.setStatus("ACTIVE");
        employee.setCurrentStatus("ACTIVE");
        employee.setDepartment("Engineering");

        exit = new EmployeeExit();
        exit.setId(501L);
        exit.setOrganization(testOrg);
        exit.setEmployee(employee);
        exit.setStatus("FNF_CALCULATION_PENDING");
        exit.setLastWorkingDate(LocalDate.now());

        hrUser = new User();
        hrUser.setId(10L);
        hrUser.setWorkEmail("hr@acme.com");
        hrUser.setFullName("HR Manager");
        hrUser.setOrganization(testOrg);
        Role hrRole = new Role();
        hrRole.setName("HR");
        hrUser.setRole(hrRole);

        superAdminUser = new User();
        superAdminUser.setId(1L);
        superAdminUser.setWorkEmail("admin@acme.com");
        superAdminUser.setFullName("System Admin");
        superAdminUser.setOrganization(testOrg);
        Role adminRole = new Role();
        adminRole.setName("SUPER_ADMIN");
        superAdminUser.setRole(adminRole);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Tamper-proof calculation strictly derives salary, earnings, deductions, and net settlement on backend")
    void testCalculateSettlement_TamperProofFormulas() {
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(exit));
        when(fnfRepository.findByExitIdAndOrganizationId(501L, 10L)).thenReturn(Optional.empty());
        when(fnfRepository.save(any(FnfSettlement.class))).thenAnswer(invocation -> {
            FnfSettlement s = invocation.getArgument(0);
            s.setId(901L);
            return s;
        });

        FnfCalculationRequest request = new FnfCalculationRequest();
        request.setSalaryDaysWorked(15); // Monthly is 100,000 -> 15 days @ 3,333.3333 = 50,000.00
        request.setUnpaidSalary(BigDecimal.valueOf(10000.00));
        request.setLeaveEncashment(BigDecimal.valueOf(15000.00));
        request.setBonus(BigDecimal.valueOf(20000.00));
        request.setIncentives(BigDecimal.valueOf(5000.00));
        request.setOvertime(BigDecimal.valueOf(2500.00));
        request.setReimbursements(BigDecimal.valueOf(3500.00));
        request.setGratuity(BigDecimal.valueOf(50000.00));
        request.setOtherAllowances(BigDecimal.valueOf(1000.00));

        // Total Earnings = 50,000 + 10,000 + 15,000 + 20,000 + 5,000 + 2,500 + 3,500 + 50,000 + 1,000 = 157,000.00

        request.setNoticePeriodRecovery(BigDecimal.valueOf(5000.00));
        request.setAssetDamage(BigDecimal.valueOf(2000.00));
        request.setTaxDeduction(BigDecimal.valueOf(15000.00));
        request.setOtherDeductions(BigDecimal.valueOf(1000.00));

        // Total Deductions = 5,000 + 2,000 + 15,000 + 1,000 = 23,000.00
        // Net Settlement = 157,000 - 23,000 = 134,000.00

        FnfCalculationResponse response = fnfSettlementService.calculateSettlement(hrUser, 501L, request);

        assertNotNull(response);
        assertEquals(901L, response.getFnfId());
        assertEquals(BigDecimal.valueOf(50000.00).setScale(2, RoundingMode.HALF_UP), response.getEarnings().getSalary());
        assertEquals(BigDecimal.valueOf(157000.00).setScale(2, RoundingMode.HALF_UP), response.getEarnings().getTotal());
        assertEquals(BigDecimal.valueOf(23000.00).setScale(2, RoundingMode.HALF_UP), response.getDeductions().getTotal());
        assertEquals(BigDecimal.valueOf(134000.00).setScale(2, RoundingMode.HALF_UP), response.getNetSettlement());
        assertEquals("FINANCE_APPROVAL_PENDING", response.getStatus());

        // Verify 3-stage F&F approval workflow initiated
        verify(approvalWorkflowEngineService, times(1)).startWorkflow(
                eq(WorkflowType.FNF_APPROVAL),
                eq("FNF_SETTLEMENT"),
                eq("901"),
                eq(employee),
                isNull()
        );
    }

    @Test
    @DisplayName("Editing settlement after payment release is prohibited")
    void testUpdateSettlement_WhenPaymentAlreadyReleased_ThrowsException() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(exit);
        settlement.setStatus("PAYMENT_RELEASED");

        when(fnfRepository.findByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(settlement));

        FnfCalculationRequest req = new FnfCalculationRequest();
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                fnfSettlementService.updateSettlement(hrUser, 901L, req)
        );
        assertTrue(ex.getMessage().contains("Payment has already been released"));
    }

    @Test
    @DisplayName("Approval Workflow completion event updates settlement and exit status to SETTLEMENT_APPROVED")
    void testFnfWorkflowCompleted_EventTransitionsStatus() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(exit);
        settlement.setStatus("FINANCE_APPROVAL_PENDING");

        when(fnfRepository.findById(901L)).thenReturn(Optional.of(settlement));

        ApprovalWorkflowCompletedEvent completedEvent = new ApprovalWorkflowCompletedEvent(
                this,
                "INST-FNF-901",
                WorkflowType.FNF_APPROVAL,
                "FNF_SETTLEMENT",
                "901",
                10L,
                ApprovalStatus.APPROVED
        );

        eventListener.onApprovalWorkflowCompleted(completedEvent);

        verify(fnfRepository, times(1)).save(argThat(s -> "SETTLEMENT_APPROVED".equals(s.getStatus())));
        verify(exitRepository, times(1)).save(argThat(e -> "SETTLEMENT_APPROVED".equals(e.getStatus())));
    }

    @Test
    @DisplayName("Payment disbursement succeeds: validates amount, marks PAYMENT_RELEASED and updates Employee to EXITED")
    void testPaymentDisbursement_Success() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(exit);
        settlement.setStatus("SETTLEMENT_APPROVED");
        settlement.setNetSettlement(BigDecimal.valueOf(134000.00));

        when(fnfRepository.findWithLockByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(settlement));
        when(employeeRepository.findByEmail("hr@acme.com")).thenReturn(Optional.of(employee));
        when(fnfRepository.saveAndFlush(any(FnfSettlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FnfPaymentRequest payReq = new FnfPaymentRequest();
        payReq.setAmount(BigDecimal.valueOf(134000.00));
        payReq.setPaymentMethod("BANK_TRANSFER");
        payReq.setPaymentDate(LocalDate.now());
        payReq.setTransactionReference("UTR-2026-987654321");
        payReq.setIdempotencyKey("KEY-2026-001");
        payReq.setRemarks("NEFT final settlement transfer");

        FnfPaymentResponse response = fnfSettlementService.processPayment(hrUser, 901L, payReq);

        assertNotNull(response);
        assertEquals("PAYMENT_RELEASED", response.getStatus());
        assertEquals(BigDecimal.valueOf(134000.00), response.getAmount());
        assertEquals("UTR-2026-987654321", response.getTransactionReference());

        // Verify Exit transitions to SETTLEMENT_COMPLETED
        verify(exitRepository, times(1)).save(argThat(e -> "SETTLEMENT_COMPLETED".equals(e.getStatus())));

        // Verify Employee lifecycle termination boundary invoked
        verify(employeeService, times(1)).terminateEmployee(eq(101L), eq("Full and final settlement payment disbursed"));
    }

    @Test
    @DisplayName("Payment disbursement fails if paid amount does not match net settlement")
    void testPaymentDisbursement_AmountMismatch_ThrowsException() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(exit);
        settlement.setStatus("SETTLEMENT_APPROVED");
        settlement.setNetSettlement(BigDecimal.valueOf(134000.00));

        when(fnfRepository.findWithLockByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(settlement));

        FnfPaymentRequest payReq = new FnfPaymentRequest();
        payReq.setAmount(BigDecimal.valueOf(100000.00)); // Mismatched amount!
        payReq.setPaymentMethod("BANK_TRANSFER");
        payReq.setPaymentDate(LocalDate.now());
        payReq.setTransactionReference("UTR-MISMATCH");
        payReq.setIdempotencyKey("KEY-MISMATCH");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fnfSettlementService.processPayment(hrUser, 901L, payReq)
        );
        assertTrue(ex.getMessage().contains("does not match calculated net settlement amount"));
    }

    @Test
    @DisplayName("Payment disbursement fails if settlement is not yet approved")
    void testPaymentDisbursement_BeforeApproval_ThrowsException() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(exit);
        settlement.setStatus("FINANCE_APPROVAL_PENDING"); // Not approved!
        settlement.setNetSettlement(BigDecimal.valueOf(134000.00));

        when(fnfRepository.findWithLockByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(settlement));

        FnfPaymentRequest payReq = new FnfPaymentRequest();
        payReq.setAmount(BigDecimal.valueOf(134000.00));
        payReq.setPaymentMethod("BANK_TRANSFER");
        payReq.setIdempotencyKey("KEY-BEFORE-APP");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                fnfSettlementService.processPayment(hrUser, 901L, payReq)
        );
        assertTrue(ex.getMessage().contains("All 3 approval stages (Finance, HR, Admin) must be approved"));
    }

    @Test
    @DisplayName("F&F Report Service computes accurate totals and breakdowns")
    void testFnfReports_MetricsAndBreakdown() {
        FnfSettlement s1 = new FnfSettlement();
        s1.setId(901L);
        s1.setOrganization(testOrg);
        s1.setExit(exit);
        s1.setStatus("PAYMENT_RELEASED");
        s1.setTotalEarnings(BigDecimal.valueOf(100000.00));
        s1.setTotalDeductions(BigDecimal.valueOf(10000.00));
        s1.setNetSettlement(BigDecimal.valueOf(90000.00));
        s1.setPaidAmount(BigDecimal.valueOf(90000.00));

        Pageable pageable = PageRequest.of(0, 10);
        when(fnfRepository.findByOrganizationId(10L, pageable)).thenReturn(new PageImpl<>(List.of(s1), pageable, 1));
        when(fnfRepository.sumTotalPaidSettlements(10L)).thenReturn(BigDecimal.valueOf(90000.00));
        when(fnfRepository.sumTotalPendingSettlements(10L)).thenReturn(BigDecimal.valueOf(50000.00));

        FnfReportResponse report = fnfReportService.getAllReports(hrUser, pageable);

        assertNotNull(report);
        assertEquals(1, report.getTotalRecords());
        assertEquals(BigDecimal.valueOf(140000.00), report.getTotalAmount());
        assertEquals(BigDecimal.valueOf(90000.00), report.getTotalPaidAmount());
        assertEquals(BigDecimal.valueOf(50000.00), report.getTotalPendingAmount());
        assertEquals(1, report.getSettlements().size());
        assertEquals(BigDecimal.valueOf(90000.00), report.getSettlements().get(0).getNetSettlement());
    }
}
