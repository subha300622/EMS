package com.example.ems.offboarding.service;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.repository.ApprovalTaskRepository;
import com.example.ems.approval.repository.ApprovalWorkflowInstanceRepository;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.service.EmployeeService;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.entity.EmployeeExit;
import com.example.ems.offboarding.entity.ExitClearance;
import com.example.ems.offboarding.entity.ExitFnfAudit;
import com.example.ems.offboarding.entity.FnfSettlement;
import com.example.ems.offboarding.repository.EmployeeExitRepository;
import com.example.ems.offboarding.repository.ExitClearanceRepository;
import com.example.ems.offboarding.repository.ExitFnfAuditRepository;
import com.example.ems.offboarding.repository.ExitFnfSettlementRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FnfAdversarialSecurityTest {

    @Mock
    private ExitFnfSettlementRepository fnfRepository;

    @Mock
    private EmployeeExitRepository exitRepository;

    @Mock
    private ExitClearanceRepository clearanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private ExitFnfAuditRepository auditRepository;

    @Mock
    private ApprovalWorkflowEngineService approvalWorkflowEngineService;

    @Mock
    private ApprovalWorkflowInstanceRepository instanceRepository;

    @Mock
    private ApprovalTaskRepository taskRepository;

    @Mock
    private FnfReadinessService readinessService;

    @Spy
    private FnfSnapshotService snapshotService = new FnfSnapshotService();

    @Spy
    private ExitAuthorizationService exitAuthService = new ExitAuthorizationService();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private FnfSettlementService fnfSettlementService;

    @InjectMocks
    private EmployeeExitService exitService;

    @InjectMocks
    private ExitClearanceService clearanceService;

    @InjectMocks
    private ExitWorkflowEventListener eventListener;

    private Organization testOrg;
    private Organization otherOrg;
    private Employee employeeAlice;
    private Employee employeeBob;
    private Employee managerCharlie;
    private Employee itOfficer;
    private Employee financeOfficer;
    private EmployeeExit aliceExit;
    private ExitClearance itClearance;
    private ExitClearance financeClearance;
    private User aliceUser;
    private User bobUser;
    private User charlieUser;
    private User itUser;
    private User financeUser;
    private User hrUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
        TenantContext.setCurrentTenant(10L);

        testOrg = new Organization();
        testOrg.setId(10L);
        testOrg.setName("Acme Primary Corp");

        otherOrg = new Organization();
        otherOrg.setId(20L);
        otherOrg.setName("Initech Competitor Corp");

        // Alice (Resigning Developer)
        employeeAlice = new Employee();
        employeeAlice.setId(101L);
        employeeAlice.setFullName("Alice Developer");
        employeeAlice.setEmail("alice@acme.com");
        employeeAlice.setEmployeeId("EMP-101");
        employeeAlice.setDepartment("Engineering");
        employeeAlice.setStatus("ACTIVE");
        employeeAlice.setAnnualSalary(BigDecimal.valueOf(1200000.00)); // 100k/month
        employeeAlice.setOrganization(testOrg);

        // Bob (Colleague Developer)
        employeeBob = new Employee();
        employeeBob.setId(102L);
        employeeBob.setFullName("Bob Colleague");
        employeeBob.setEmail("bob@acme.com");
        employeeBob.setEmployeeId("EMP-102");
        employeeBob.setDepartment("Engineering");
        employeeBob.setStatus("ACTIVE");
        employeeBob.setOrganization(testOrg);

        // Charlie (Direct Manager)
        managerCharlie = new Employee();
        managerCharlie.setId(201L);
        managerCharlie.setFullName("Charlie Manager");
        managerCharlie.setEmail("charlie@acme.com");
        managerCharlie.setEmployeeId("MGR-201");
        managerCharlie.setOrganization(testOrg);
        employeeAlice.setManager(managerCharlie);

        // IT Officer
        itOfficer = new Employee();
        itOfficer.setId(301L);
        itOfficer.setFullName("Ian IT");
        itOfficer.setEmail("ian.it@acme.com");
        itOfficer.setEmployeeId("IT-301");
        itOfficer.setDepartment("IT");
        itOfficer.setStatus("ACTIVE");
        itOfficer.setOrganization(testOrg);

        // Finance Officer
        financeOfficer = new Employee();
        financeOfficer.setId(401L);
        financeOfficer.setFullName("Fiona Finance");
        financeOfficer.setEmail("fiona.fin@acme.com");
        financeOfficer.setEmployeeId("FIN-401");
        financeOfficer.setDepartment("Finance");
        financeOfficer.setStatus("ACTIVE");
        financeOfficer.setOrganization(testOrg);

        // Alice Exit Request
        aliceExit = new EmployeeExit();
        aliceExit.setId(501L);
        aliceExit.setOrganization(testOrg);
        aliceExit.setEmployee(employeeAlice);
        aliceExit.setReportingManager(managerCharlie);
        aliceExit.setStatus("FNF_CALCULATION_PENDING");
        aliceExit.setLastWorkingDate(LocalDate.of(2026, 3, 31)); // 31-day month

        // Clearances
        itClearance = new ExitClearance();
        itClearance.setId(601L);
        itClearance.setOrganization(testOrg);
        itClearance.setExit(aliceExit);
        itClearance.setDepartment("IT");
        itClearance.setAssignedTo(itOfficer);
        itClearance.setStatus("PENDING");

        financeClearance = new ExitClearance();
        financeClearance.setId(602L);
        financeClearance.setOrganization(testOrg);
        financeClearance.setExit(aliceExit);
        financeClearance.setDepartment("FINANCE");
        financeClearance.setAssignedTo(financeOfficer);
        financeClearance.setStatus("PENDING");

        // User Personas
        aliceUser = createUser(101L, "alice@acme.com", "EMP-101", "EMPLOYEE");
        bobUser = createUser(102L, "bob@acme.com", "EMP-102", "EMPLOYEE");
        charlieUser = createUser(201L, "charlie@acme.com", "MGR-201", "MANAGER");
        itUser = createUser(301L, "ian.it@acme.com", "IT-301", "EMPLOYEE");
        financeUser = createUser(401L, "fiona.fin@acme.com", "FIN-401", "FINANCE");
        hrUser = createUser(501L, "hr@acme.com", "HR-501", "HR");


        org.springframework.test.util.ReflectionTestUtils.setField(exitService, "exitClearanceService",
                clearanceService);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    private User createUser(Long id, String email, String empCode, String roleName) {
        User u = new User();
        u.setId(id);
        u.setWorkEmail(email);
        u.setEmployeeId(empCode);
        u.setOrganization(testOrg);
        Role r = new Role();
        r.setName(roleName);
        u.setRole(r);
        return u;
    }

    // =========================================================================
    // SECTION 1: PAYMENT CONCURRENCY & IDEMPOTENCY
    // =========================================================================

    @Test
    @DisplayName("01. Payment Idempotent Replay returns existing receipt when retry payload matches")
    void testPaymentReplayAfterTransactionCommit() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(aliceExit);
        settlement.setStatus("PAYMENT_RELEASED");
        settlement.setPaidAmount(BigDecimal.valueOf(87500.00));
        settlement.setNetSettlement(BigDecimal.valueOf(87500.00));
        settlement.setPaymentReference("UTR-123456");
        settlement.setIdempotencyKey("IDEM-KEY-001");
        settlement.setPaymentMethod("BANK_TRANSFER");
        settlement.setPaidAt(LocalDateTime.now());
        settlement.setPaidBy(financeOfficer);

        when(fnfRepository.findWithLockByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(settlement));

        FnfPaymentRequest retryRequest = new FnfPaymentRequest();
        retryRequest.setAmount(BigDecimal.valueOf(87500.00));
        retryRequest.setTransactionReference("UTR-123456");
        retryRequest.setIdempotencyKey("IDEM-KEY-001");
        retryRequest.setPaymentMethod("BANK_TRANSFER");
        retryRequest.setPaymentDate(LocalDate.now());

        // Call processPayment
        FnfPaymentResponse response = fnfSettlementService.processPayment(financeUser, 901L, retryRequest);

        assertNotNull(response);
        assertEquals("PAYMENT_RELEASED", response.getStatus());
        assertEquals("UTR-123456", response.getTransactionReference());
        assertEquals(BigDecimal.valueOf(87500.00), response.getAmount());
        // Verify NO duplicate save or downstream mutations occurred
        verify(fnfRepository, never()).saveAndFlush(any());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("02. Payment Idempotency Conflict: Modified amount on retry is strictly rejected")
    void testPaymentPayloadMismatchOnRetry_Rejected() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(aliceExit);
        settlement.setStatus("PAYMENT_RELEASED");
        settlement.setPaidAmount(BigDecimal.valueOf(50000.00)); // Originally paid 50k
        settlement.setPaymentReference("UTR-123456");
        settlement.setIdempotencyKey("IDEM-KEY-001");
        settlement.setPaymentMethod("BANK_TRANSFER");

        when(fnfRepository.findWithLockByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(settlement));

        // Malicious or conflicting retry with altered amount
        FnfPaymentRequest modifiedRetry = new FnfPaymentRequest();
        modifiedRetry.setAmount(BigDecimal.valueOf(90000.00)); // Attempting to pay 90k instead!
        modifiedRetry.setTransactionReference("UTR-123456");
        modifiedRetry.setIdempotencyKey("IDEM-KEY-001");
        modifiedRetry.setPaymentMethod("BANK_TRANSFER");
        modifiedRetry.setPaymentDate(LocalDate.now());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> fnfSettlementService.processPayment(financeUser, 901L, modifiedRetry));
        assertTrue(ex.getMessage().contains("Payment idempotency conflict"));
    }

    @Test
    @DisplayName("03. Payment with different idempotency key on already released settlement is rejected")
    void testConcurrentPayment_DifferentIdempotencyKeysAreRejectedAfterFirstPayment() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(aliceExit);
        settlement.setStatus("PAYMENT_RELEASED");
        settlement.setPaidAmount(BigDecimal.valueOf(87500.00));
        settlement.setPaymentReference("UTR-123456");
        settlement.setIdempotencyKey("KEY-TX-A");

        when(fnfRepository.findWithLockByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(settlement));

        FnfPaymentRequest newDisbursement = new FnfPaymentRequest();
        newDisbursement.setAmount(BigDecimal.valueOf(87500.00));
        newDisbursement.setTransactionReference("UTR-DIFFERENT-999");
        newDisbursement.setIdempotencyKey("KEY-TX-B"); // Different key!
        newDisbursement.setPaymentMethod("BANK_TRANSFER");
        newDisbursement.setPaymentDate(LocalDate.now());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> fnfSettlementService.processPayment(financeUser, 901L, newDisbursement));
        assertTrue(ex.getMessage().contains("Payment has already been released for settlement ID"));
    }

    @Test
    @DisplayName("04. Duplicate payment reference in same tenant is rejected")
    void testDuplicatePaymentReferenceInSameTenant_Rejected() {
        FnfSettlement targetSettlement = new FnfSettlement();
        targetSettlement.setId(901L);
        targetSettlement.setOrganization(testOrg);
        targetSettlement.setExit(aliceExit);
        targetSettlement.setStatus("SETTLEMENT_APPROVED");
        targetSettlement.setNetSettlement(BigDecimal.valueOf(87500.00));

        FnfSettlement existingOther = new FnfSettlement();
        existingOther.setId(902L); // Different settlement in same org
        existingOther.setPaymentReference("UTR-COLLISION");

        when(fnfRepository.findWithLockByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(targetSettlement));
        when(fnfRepository.findByOrganizationIdAndPaymentReference(10L, "UTR-COLLISION"))
                .thenReturn(Optional.of(existingOther));

        FnfPaymentRequest req = new FnfPaymentRequest();
        req.setAmount(BigDecimal.valueOf(87500.00));
        req.setTransactionReference("UTR-COLLISION");
        req.setIdempotencyKey("KEY-NEW");
        req.setPaymentMethod("BANK_TRANSFER");
        req.setPaymentDate(LocalDate.now());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> fnfSettlementService.processPayment(financeUser, 901L, req));
        assertTrue(ex.getMessage().contains("Payment reference 'UTR-COLLISION' has already been used in organization"));
    }

    @Test
    @DisplayName("05. Payment amount mismatch is rejected with IllegalArgumentException")
    void testPaymentAmountMismatch_Rejected() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(aliceExit);
        settlement.setStatus("SETTLEMENT_APPROVED");
        settlement.setNetSettlement(BigDecimal.valueOf(87500.00));

        when(fnfRepository.findWithLockByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(settlement));

        FnfPaymentRequest req = new FnfPaymentRequest();
        req.setAmount(BigDecimal.valueOf(80000.00)); // Mismatch!
        req.setTransactionReference("UTR-123");
        req.setIdempotencyKey("KEY-123");
        req.setPaymentMethod("BANK_TRANSFER");
        req.setPaymentDate(LocalDate.now());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fnfSettlementService.processPayment(financeUser, 901L, req));
        assertTrue(ex.getMessage().contains("does not match calculated net settlement amount"));
    }

    @Test
    @DisplayName("06. Payment failure during employee status update throws exception and causes atomic rollback")
    void testPaymentEmployeeUpdateFailure_RollsBackSettlement() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(aliceExit);
        settlement.setStatus("SETTLEMENT_APPROVED");
        settlement.setNetSettlement(BigDecimal.valueOf(87500.00));

        when(fnfRepository.findWithLockByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(settlement));
        when(employeeRepository.findByEmail("fiona.fin@acme.com")).thenReturn(Optional.of(financeOfficer));
        when(fnfRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        // Downstream failure simulation: EmployeeService fails to terminate employee
        when(employeeService.terminateEmployee(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Database error updating employee status"));

        FnfPaymentRequest req = new FnfPaymentRequest();
        req.setAmount(BigDecimal.valueOf(87500.00));
        req.setTransactionReference("UTR-FAIL");
        req.setIdempotencyKey("KEY-FAIL");
        req.setPaymentMethod("BANK_TRANSFER");
        req.setPaymentDate(LocalDate.now());

        assertThrows(RuntimeException.class, () -> fnfSettlementService.processPayment(financeUser, 901L, req));
    }

    // =========================================================================
    // SECTION 2: OBJECT AUTHORIZATION & BOLA/IDOR DEFENSES
    // =========================================================================

    @Test
    @DisplayName("07. Resigning employee cannot release own F&F payment")
    void testEmployeeCannotReleaseOwnFnfPayment() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(aliceExit);
        settlement.setStatus("SETTLEMENT_APPROVED");
        settlement.setNetSettlement(BigDecimal.valueOf(87500.00));

        when(fnfRepository.findWithLockByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(settlement));

        FnfPaymentRequest req = new FnfPaymentRequest();
        req.setAmount(BigDecimal.valueOf(87500.00));
        req.setTransactionReference("UTR-SELF");
        req.setIdempotencyKey("KEY-SELF");
        req.setPaymentMethod("UPI");
        req.setPaymentDate(LocalDate.now());

        // Alice attempts to call release payment on her own F&F
        assertThrows(AccessDeniedException.class, () -> fnfSettlementService.processPayment(aliceUser, 901L, req));
    }

    @Test
    @DisplayName("08. Peer employee Bob cannot view Alice's F&F settlement")
    void testEmployeeCannotReadAnotherEmployeesFnf() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(aliceExit);

        when(fnfRepository.findByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(settlement));

        // Bob attempts to view Alice's F&F settlement details
        assertThrows(AccessDeniedException.class, () -> fnfSettlementService.getSettlementById(bobUser, 901L));
    }

    @Test
    @DisplayName("09. Peer employee Bob cannot view Alice's exit details")
    void testEmployeeCannotViewColleagueExit() {
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));

        // Bob attempts to inspect Alice's exit request
        assertThrows(AccessDeniedException.class, () -> exitService.getExitById(bobUser, 501L));
    }

    @Test
    @DisplayName("10. Alice can view her own exit details")
    void testEmployeeCanViewOwnExit() {
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));
        when(clearanceRepository.findByExitIdAndOrganizationId(501L, 10L)).thenReturn(List.of(itClearance));

        ExitDetailResponse response = exitService.getExitById(aliceUser, 501L);
        assertNotNull(response);
        assertEquals("Alice Developer", response.getEmployeeName());
    }

    @Test
    @DisplayName("11. Direct Manager Charlie can view Alice's exit details, but unrelated manager cannot")
    void testManagerCannotReadUnrelatedExit() {
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));
        when(clearanceRepository.findByExitIdAndOrganizationId(501L, 10L)).thenReturn(List.of());

        // 1. Direct manager Charlie CAN view
        ExitDetailResponse directResp = exitService.getExitById(charlieUser, 501L);
        assertNotNull(directResp);

        // 2. Unrelated manager Dan from another team CANNOT view
        User unrelatedManager = createUser(777L, "dan.mgr@acme.com", "MGR-777", "MANAGER");
        assertThrows(AccessDeniedException.class, () -> exitService.getExitById(unrelatedManager, 501L));
    }

    @Test
    @DisplayName("12. Resigning employee cannot calculate or modify own F&F settlement")
    void testEmployeeCannotModifyOwnFnf() {
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));

        FnfCalculationRequest calcReq = new FnfCalculationRequest();
        calcReq.setSalaryDaysWorked(31);

        // Alice attempts to compute/tamper with her own settlement
        assertThrows(AccessDeniedException.class,
                () -> fnfSettlementService.calculateSettlement(aliceUser, 501L, calcReq));
    }

    @Test
    @DisplayName("13. Resigning employee cannot action their own clearance tasks")
    void testEmployeeCannotActionOwnClearance() {
        when(clearanceRepository.findByIdAndOrganizationId(601L, 10L)).thenReturn(Optional.of(itClearance));

        ClearanceActionRequest actionReq = new ClearanceActionRequest();
        actionReq.setAction("CLEAR");
        actionReq.setRemarks("Self clearing IT assets");

        // Alice attempts to clear her own IT handover
        assertThrows(AccessDeniedException.class,
                () -> clearanceService.processClearanceAction(aliceUser, 501L, 601L, actionReq));
    }

    @Test
    @DisplayName("14. IT user cannot modify Finance clearance; Finance user cannot modify IT clearance")
    void testCrossDepartmentClearanceTamper_Rejected() {
        when(clearanceRepository.findByIdAndOrganizationId(601L, 10L)).thenReturn(Optional.of(itClearance));
        when(clearanceRepository.findByIdAndOrganizationId(602L, 10L)).thenReturn(Optional.of(financeClearance));

        ClearanceActionRequest actionReq = new ClearanceActionRequest();
        actionReq.setAction("CLEAR");

        // Finance user Fiona tries to clear IT clearance 601
        assertThrows(AccessDeniedException.class,
                () -> clearanceService.processClearanceAction(financeUser, 501L, 601L, actionReq));

        // IT user Ian tries to clear Finance clearance 602
        assertThrows(AccessDeniedException.class,
                () -> clearanceService.processClearanceAction(itUser, 501L, 602L, actionReq));
    }

    // =========================================================================
    // SECTION 3: STRICT STATE MACHINE GUARDS
    // =========================================================================

    @Test
    @DisplayName("15. Rejected exit request cannot calculate F&F settlement")
    void testRejectedExitCannotCalculateFnf() {
        aliceExit.setStatus("REJECTED");
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));

        FnfCalculationRequest calcReq = new FnfCalculationRequest();
        calcReq.setSalaryDaysWorked(15);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> fnfSettlementService.calculateSettlement(hrUser, 501L, calcReq));
        assertTrue(ex.getMessage().contains("Exit request has been rejected"));
    }

    @Test
    @DisplayName("16. Premature F&F calculation before clearances complete is rejected")
    void testIncompleteClearanceCannotCalculateFnf() {
        aliceExit.setStatus("CLEARANCE_PENDING"); // Clearances still pending!
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));

        FnfCalculationRequest calcReq = new FnfCalculationRequest();
        calcReq.setSalaryDaysWorked(15);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> fnfSettlementService.calculateSettlement(hrUser, 501L, calcReq));
        assertTrue(ex.getMessage().contains(
                "Calculation is only permitted when clearances are complete and status is FNF_CALCULATION_PENDING"));
    }

    @Test
    @DisplayName("17. Completed exit cannot be re-offboarded")
    void testCompletedExitCannotBeOffboarded() {
        aliceExit.setStatus("SETTLEMENT_COMPLETED");
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));

        HrOffboardingRequest offboardReq = new HrOffboardingRequest();
        offboardReq.setLastWorkingDate(LocalDate.now());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> exitService.initiateOffboarding(hrUser, 501L, offboardReq));
        assertTrue(ex.getMessage().contains("Exit has already completed settlement"));
    }

    @Test
    @DisplayName("18. In-flight F&F approval cannot be silently overwritten by calculate API")
    void testFinanceApprovalPendingCannotRecalculateSilently() {
        aliceExit.setStatus("FINANCE_APPROVAL_PENDING");
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));

        FnfCalculationRequest calcReq = new FnfCalculationRequest();
        calcReq.setSalaryDaysWorked(20);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> fnfSettlementService.calculateSettlement(hrUser, 501L, calcReq));
        assertTrue(ex.getMessage().contains(
                "Calculation is only permitted when clearances are complete and status is FNF_CALCULATION_PENDING"));
    }

    @Test
    @DisplayName("19. Finalized settlement with payment released cannot be recalculated or updated")
    void testPaymentReleasedCannotBeRecalculatedOrUpdated() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(aliceExit);
        settlement.setStatus("PAYMENT_RELEASED");

        when(fnfRepository.findByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(settlement));

        FnfCalculationRequest updateReq = new FnfCalculationRequest();
        updateReq.setSalaryDaysWorked(10);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> fnfSettlementService.updateSettlement(hrUser, 901L, updateReq));
        assertTrue(ex.getMessage().contains("Payment has already been released"));
    }

    // =========================================================================
    // SECTION 4: EVENT TENANT ISOLATION & FORGERY DEFENSE
    // =========================================================================

    @Test
    @DisplayName("20. Approval event listener restores TenantContext after completion")
    void testAsyncApprovalEvent_RestoresTenantContext() {
        Long initialTenant = 10L;
        TenantContext.setCurrentTenant(initialTenant);

        when(exitRepository.findById(501L)).thenReturn(Optional.of(aliceExit));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this, "INST-001", WorkflowType.EMPLOYEE_EXIT, "EMPLOYEE_EXIT", "501", 10L, ApprovalStatus.APPROVED);

        eventListener.onApprovalWorkflowCompleted(event);

        // Verify TenantContext was restored back to initialTenant
        assertEquals(initialTenant, TenantContext.getCurrentTenant());
        assertEquals("HR_OFFBOARDING_PENDING", aliceExit.getStatus());
    }

    @Test
    @DisplayName("21. Forged cross-tenant event targeting another organization's exit is rejected without mutation")
    void testForgedCrossTenantEventRejected() {
        aliceExit.setStatus("MANAGER_APPROVAL_PENDING");
        when(exitRepository.findById(501L)).thenReturn(Optional.of(aliceExit)); // belongs to testOrg (10L)

        // Attacker emits an event claiming to be for organization 20L targeting exit
        // 501L
        ApprovalWorkflowCompletedEvent forgedEvent = new ApprovalWorkflowCompletedEvent(
                this, "INST-FORGED", WorkflowType.EMPLOYEE_EXIT, "EMPLOYEE_EXIT", "501", 20L, ApprovalStatus.APPROVED);

        eventListener.onApprovalWorkflowCompleted(forgedEvent);

        // Verify exit status remained MANAGER_APPROVAL_PENDING and was NOT modified
        assertEquals("MANAGER_APPROVAL_PENDING", aliceExit.getStatus());
        verify(exitRepository, never()).save(any());
    }

    // =========================================================================
    // SECTION 5: CALENDAR CALCULATION PRECISION & NO DATA FABRICATION
    // =========================================================================

    @Test
    @DisplayName("22. February non-leap year (28 days) prorates using exact 28-day divisor")
    void testFebruaryNonLeapYearCalculation() {
        aliceExit.setLastWorkingDate(LocalDate.of(2025, 2, 14)); // 2025 is non-leap (28 days)
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));
        when(fnfRepository.findByExitIdAndOrganizationId(501L, 10L)).thenReturn(Optional.empty());
        when(fnfRepository.save(any(FnfSettlement.class))).thenAnswer(i -> i.getArgument(0));

        FnfCalculationRequest req = new FnfCalculationRequest();
        req.setSalaryDaysWorked(14); // Exactly half of February

        FnfCalculationResponse resp = fnfSettlementService.calculateSettlement(hrUser, 501L, req);

        // Monthly salary = 1,200,000 / 12 = 100,000.00
        // Daily rate = 100,000 / 28 = 3571.4286
        // 14 days = 3571.4286 * 14 = 50000.00
        assertEquals(new BigDecimal("50000.00"), resp.getEarnings().getSalary());
    }

    @Test
    @DisplayName("23. February leap year (29 days) prorates using exact 29-day divisor")
    void testFebruaryLeapYearCalculation() {
        aliceExit.setLastWorkingDate(LocalDate.of(2024, 2, 29)); // 2024 is leap year (29 days)
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));
        when(fnfRepository.findByExitIdAndOrganizationId(501L, 10L)).thenReturn(Optional.empty());
        when(fnfRepository.save(any(FnfSettlement.class))).thenAnswer(i -> i.getArgument(0));

        FnfCalculationRequest req = new FnfCalculationRequest();
        req.setSalaryDaysWorked(29); // Full leap month

        FnfCalculationResponse resp = fnfSettlementService.calculateSettlement(hrUser, 501L, req);

        // 29 days out of 29 = full monthly salary 100,000.00
        assertEquals(new BigDecimal("100000.00"), resp.getEarnings().getSalary());
    }

    @Test
    @DisplayName("24. 31-day month prorates using exact 31-day divisor")
    void testThirtyOneDayMonthCalculation() {
        aliceExit.setLastWorkingDate(LocalDate.of(2026, 3, 31)); // March has 31 days
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));
        when(fnfRepository.findByExitIdAndOrganizationId(501L, 10L)).thenReturn(Optional.empty());
        when(fnfRepository.save(any(FnfSettlement.class))).thenAnswer(i -> i.getArgument(0));

        FnfCalculationRequest req = new FnfCalculationRequest();
        req.setSalaryDaysWorked(31);

        FnfCalculationResponse resp = fnfSettlementService.calculateSettlement(hrUser, 501L, req);

        assertEquals(new BigDecimal("100000.00"), resp.getEarnings().getSalary());
    }

    @Test
    @DisplayName("25. Salary days exceeding exit month days is rejected with validation error")
    void testSalaryDaysExceedingMonthRejected() {
        aliceExit.setLastWorkingDate(LocalDate.of(2025, 2, 10)); // Feb 2025 has 28 days
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));

        FnfCalculationRequest req = new FnfCalculationRequest();
        req.setSalaryDaysWorked(29); // 29 days in 28-day month!

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fnfSettlementService.calculateSettlement(hrUser, 501L, req));
        assertTrue(ex.getMessage().contains("cannot exceed total days in the exit month"));
    }

    @Test
    @DisplayName("26. Negative salary days is rejected with validation error")
    void testNegativeSalaryDaysRejected() {
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));

        FnfCalculationRequest req = new FnfCalculationRequest();
        req.setSalaryDaysWorked(-5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fnfSettlementService.calculateSettlement(hrUser, 501L, req));
        assertTrue(ex.getMessage().contains("cannot be negative"));
    }

    @Test
    @DisplayName("27. Unconfigured or zero employee salary fails fast without fabricating data")
    void testZeroOrNullSalaryFailsFast() {
        employeeAlice.setAnnualSalary(BigDecimal.ZERO); // Zero compensation configured
        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(aliceExit));

        FnfCalculationRequest req = new FnfCalculationRequest();
        req.setSalaryDaysWorked(10);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> fnfSettlementService.calculateSettlement(hrUser, 501L, req));
        assertTrue(ex.getMessage().contains("Employee annual salary is not configured or is zero"));

        // Null salary test
        employeeAlice.setAnnualSalary(null);
        IllegalStateException ex2 = assertThrows(IllegalStateException.class,
                () -> fnfSettlementService.calculateSettlement(hrUser, 501L, req));
        assertTrue(ex2.getMessage().contains("Employee annual salary is not configured or is zero"));
    }

    // =========================================================================
    // SECTION 6: FINANCIAL AUDIT TRAIL
    // =========================================================================

    @Test
    @DisplayName("28. F&F modification creates an audit snapshot with before and after financial values")
    void testFnfCalculationModificationCreatesAuditSnapshot() {
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(901L);
        settlement.setOrganization(testOrg);
        settlement.setExit(aliceExit);
        settlement.setStatus("FINANCE_APPROVAL_PENDING");
        settlement.setNetSettlement(BigDecimal.valueOf(85000.00)); // Before: 85,000

        when(fnfRepository.findByIdAndOrganizationId(901L, 10L)).thenReturn(Optional.of(settlement));
        when(fnfRepository.save(any(FnfSettlement.class))).thenAnswer(i -> i.getArgument(0));

        FnfCalculationRequest updateReq = new FnfCalculationRequest();
        updateReq.setSalaryDaysWorked(31);
        updateReq.setBonus(BigDecimal.valueOf(5000.00)); // Adds 5k bonus -> After: 105,000

        fnfSettlementService.updateSettlement(hrUser, 901L, updateReq);

        // Verify audit snapshot recorded
        ArgumentCaptor<ExitFnfAudit> auditCaptor = ArgumentCaptor.forClass(ExitFnfAudit.class);
        verify(auditRepository, atLeastOnce()).save(auditCaptor.capture());

        ExitFnfAudit captured = auditCaptor.getValue();
        assertEquals(901L, captured.getSettlementId());
        assertEquals("UPDATE", captured.getAction());
        assertEquals(BigDecimal.valueOf(85000.00), captured.getBeforeAmount());
        assertEquals(new BigDecimal("105000.00"), captured.getAfterAmount());
        assertNotNull(captured.getSnapshotData());
    }

    @Test
    @DisplayName("29. Clearance action records audit details with cleared_by and checklist data")
    void testClearanceActionRecordsAuditData() {
        when(clearanceRepository.findByIdAndOrganizationId(601L, 10L)).thenReturn(Optional.of(itClearance));
        when(employeeRepository.findByEmail("ian.it@acme.com")).thenReturn(Optional.of(itOfficer));
        when(clearanceRepository.save(any(ExitClearance.class))).thenAnswer(i -> i.getArgument(0));

        ClearanceActionRequest actReq = new ClearanceActionRequest();
        actReq.setAction("CLEAR");
        actReq.setRemarks("All 2 laptops and badge recovered");
        actReq.setClearanceData(java.util.Map.of("laptopsRecovered", 2, "accessRevoked", true));

        ExitClearanceDto result = clearanceService.processClearanceAction(itUser, 501L, 601L, actReq);

        assertNotNull(result);
        assertEquals("CLEARED", result.getStatus());
        assertEquals(itOfficer.getId(), result.getClearedById());
        assertEquals("All 2 laptops and badge recovered", result.getRemarks());
        assertNotNull(result.getClearanceData());
    }
}
