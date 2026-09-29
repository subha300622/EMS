package com.example.ems.employee.service;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.EmployeeStatus;
import com.example.ems.employee.event.EmployeeTerminatedEvent;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.rls.PostgresRlsSessionBinder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.ems.offboarding.dto.FnfPaymentRequest;
import com.example.ems.offboarding.entity.EmployeeExit;
import com.example.ems.offboarding.entity.ExitClearance;
import com.example.ems.offboarding.entity.FnfSettlement;
import com.example.ems.offboarding.repository.EmployeeExitRepository;
import com.example.ems.offboarding.repository.ExitClearanceRepository;
import com.example.ems.offboarding.repository.ExitFnfSettlementRepository;
import com.example.ems.offboarding.service.FnfSettlementService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(EmployeeTerminationIntegrationTest.TestTerminationEventListener.class)
public class EmployeeTerminationIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private FnfSettlementService fnfSettlementService;

    @Autowired
    private EmployeeExitRepository exitRepository;

    @Autowired
    private ExitClearanceRepository clearanceRepository;

    @Autowired
    private ExitFnfSettlementRepository fnfRepository;

    @Autowired(required = false)
    private PostgresRlsSessionBinder rlsSessionBinder;

    @Autowired
    private TestTerminationEventListener testEventListener;

    private Organization tenantA;
    private Organization tenantB;

    private Employee employeeActiveA;
    private Employee employeeNoticeA;
    private Employee employeeSuspendedA;
    private Employee employeeOnboardingA;
    private Employee employeeOrgB;

    private User adminUserA;

    @BeforeEach
    void setUp() {
        testEventListener.clearEvents();

        long ts = System.currentTimeMillis();

        tenantA = new Organization();
        tenantA.setName("Tenant A " + ts);
        tenantA.setOrganizationCode("ORG_A_" + ts);
        tenantA.setStatus(OrganizationStatus.ACTIVE);
        tenantA = organizationRepository.save(tenantA);

        tenantB = new Organization();
        tenantB.setName("Tenant B " + ts);
        tenantB.setOrganizationCode("ORG_B_" + ts);
        tenantB.setStatus(OrganizationStatus.ACTIVE);
        tenantB = organizationRepository.save(tenantB);

        employeeActiveA = createEmployee("ACT_" + ts, "Active Alice", "alice." + ts + "@tenanta.com", tenantA,
                EmployeeStatus.ACTIVE.name());
        employeeNoticeA = createEmployee("NOT_" + ts, "Notice Bob", "bob." + ts + "@tenanta.com", tenantA,
                EmployeeStatus.NOTICE_PERIOD.name());
        employeeSuspendedA = createEmployee("SUSP_" + ts, "Suspended Sam", "sam." + ts + "@tenanta.com", tenantA,
                EmployeeStatus.SUSPENDED.name());
        employeeOnboardingA = createEmployee("ONB_" + ts, "Onboarding Dave", "dave." + ts + "@tenanta.com", tenantA,
                EmployeeStatus.ONBOARDING.name());

        employeeOrgB = createEmployee("ORGB_" + ts, "OrgB Charlie", "charlie." + ts + "@tenantb.com", tenantB,
                EmployeeStatus.ACTIVE.name());

        Role adminRole = roleRepository.findByName("SUPER_ADMIN").orElseGet(() -> {
            Role r = new Role();
            r.setName("SUPER_ADMIN");
            r.setDescription("Super Admin");
            return roleRepository.save(r);
        });

        adminUserA = createUser("admin.a." + ts + "@tenanta.com", tenantA, adminRole);
        authenticateAs(adminUserA);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    private void authenticateAs(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getWorkEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())));
        SecurityContextHolder.getContext().setAuthentication(auth);
        TenantContext.setCurrentTenant(user.getOrganizationId());
    }

    private Employee createEmployee(String empCode, String name, String email, Organization org, String status) {
        Employee emp = new Employee();
        emp.setEmployeeId(empCode);
        emp.setFirstName(name);
        emp.setLastName("TermTest");
        emp.setEmail(email);
        emp.setOrganization(org);
        emp.setStatus(status);
        emp.setCurrentStatus("WORKING");
        emp.setAvailability("AVAILABLE");
        emp.setDepartment("Engineering");
        emp.setDesignation("Software Engineer");
        emp.setAnnualSalary(new BigDecimal("95000.00"));
        emp.setJoiningDate(LocalDate.now());
        return employeeRepository.save(emp);
    }

    private User createUser(String email, Organization org, Role role) {
        User u = new User();
        u.setWorkEmail(email);
        u.setFullName("Admin User");
        u.setOrganization(org);
        u.setRole(role);
        u.setStatus("ACTIVE");
        return userRepository.save(u);
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("1. terminatesActiveEmployee: transitions ACTIVE -> TERMINATED")
    void terminatesActiveEmployee() {
        EmployeeTerminatedEvent event = employeeService.terminateEmployee(
                employeeActiveA.getId(),
                "Performance-based termination"
        );

        assertNotNull(event);
        assertEquals(employeeActiveA.getId(), event.employeeId());
        assertEquals(tenantA.getId(), event.organizationId());
        assertEquals("Performance-based termination", event.reason());
        assertNotNull(event.occurredAt());

        Employee reloaded = employeeRepository.findById(employeeActiveA.getId()).orElseThrow();
        assertEquals("TERMINATED", reloaded.getStatus());
        assertEquals("EXITED", reloaded.getCurrentStatus());
        assertEquals("UNAVAILABLE", reloaded.getAvailability());

        assertEquals(1, testEventListener.terminatedEvents.size());
    }

    @Test
    @DisplayName("2. terminatesNoticePeriodEmployee: transitions NOTICE_PERIOD -> TERMINATED")
    void terminatesNoticePeriodEmployee() {
        EmployeeTerminatedEvent event = employeeService.terminateEmployee(
                employeeNoticeA.getId(),
                "Resignation notice served"
        );

        assertNotNull(event);
        assertEquals(employeeNoticeA.getId(), event.employeeId());
        assertEquals("Resignation notice served", event.reason());

        Employee reloaded = employeeRepository.findById(employeeNoticeA.getId()).orElseThrow();
        assertEquals("TERMINATED", reloaded.getStatus());
        assertEquals("EXITED", reloaded.getCurrentStatus());
        assertEquals("UNAVAILABLE", reloaded.getAvailability());
    }

    @Test
    @DisplayName("3. terminatesSuspendedEmployee: transitions SUSPENDED -> TERMINATED")
    void terminatesSuspendedEmployee() {
        EmployeeTerminatedEvent event = employeeService.terminateEmployee(
                employeeSuspendedA.getId(),
                "Disciplinary termination following suspension"
        );

        assertNotNull(event);
        assertEquals(employeeSuspendedA.getId(), event.employeeId());

        Employee reloaded = employeeRepository.findById(employeeSuspendedA.getId()).orElseThrow();
        assertEquals("TERMINATED", reloaded.getStatus());
        assertEquals("EXITED", reloaded.getCurrentStatus());
        assertEquals("UNAVAILABLE", reloaded.getAvailability());
    }

    @Test
    @DisplayName("4. rejectsInvalidTerminationTransition: ONBOARDING -> TERMINATED is prohibited by FSM")
    void rejectsInvalidTerminationTransition() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                employeeService.terminateEmployee(employeeOnboardingA.getId(), "Cannot terminate onboarding directly"));
        assertTrue(ex.getMessage().contains("Invalid status transition from ONBOARDING to TERMINATED"));

        Employee reloaded = employeeRepository.findById(employeeOnboardingA.getId()).orElseThrow();
        assertEquals("ONBOARDING", reloaded.getStatus());
        assertTrue(testEventListener.terminatedEvents.isEmpty());
    }

    @Test
    @DisplayName("5. rejectsAlreadyTerminatedEmployee: throws IllegalStateException")
    void rejectsAlreadyTerminatedEmployee() {
        employeeService.terminateEmployee(employeeActiveA.getId(), "First termination");
        assertEquals(1, testEventListener.terminatedEvents.size());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                employeeService.terminateEmployee(employeeActiveA.getId(), "Second termination attempt"));
        assertTrue(ex.getMessage().contains("Employee is already terminated"));

        assertEquals(1, testEventListener.terminatedEvents.size());
    }

    @Test
    @DisplayName("6. rejectsMissingEmployee: throws IllegalArgumentException when ID is null or missing")
    void rejectsMissingEmployee() {
        IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, () ->
                employeeService.terminateEmployee(null, "Reason"));
        assertTrue(exNull.getMessage().contains("Employee ID cannot be null"));

        IllegalArgumentException exNotFound = assertThrows(IllegalArgumentException.class, () ->
                employeeService.terminateEmployee(999999L, "Reason"));
        assertTrue(exNotFound.getMessage().contains("Employee not found with ID: 999999"));
    }

    @Test
    @DisplayName("7. persistsTerminatedStatus: verifies database persistence")
    void persistsTerminatedStatus() {
        employeeService.terminateEmployee(employeeActiveA.getId(), "Audit test");

        Employee inDb = employeeRepository.findById(employeeActiveA.getId()).orElseThrow();
        assertEquals("TERMINATED", inDb.getStatus());
    }

    @Test
    @DisplayName("8. setsCurrentStatusToExited: verifies working status is updated to EXITED")
    void setsCurrentStatusToExited() {
        employeeService.terminateEmployee(employeeActiveA.getId(), "Audit test");

        Employee inDb = employeeRepository.findById(employeeActiveA.getId()).orElseThrow();
        assertEquals("EXITED", inDb.getCurrentStatus());
    }

    @Test
    @DisplayName("9. setsAvailabilityUnavailable: verifies availability is UNAVAILABLE")
    void setsAvailabilityUnavailable() {
        employeeService.terminateEmployee(employeeActiveA.getId(), "Audit test");

        Employee inDb = employeeRepository.findById(employeeActiveA.getId()).orElseThrow();
        assertEquals("UNAVAILABLE", inDb.getAvailability());
    }

    @Test
    @DisplayName("10. publishesEmployeeTerminatedEvent: payload verification")
    void publishesEmployeeTerminatedEvent() {
        employeeService.terminateEmployee(employeeActiveA.getId(), "Standard exit");

        assertEquals(1, testEventListener.terminatedEvents.size());
        EmployeeTerminatedEvent event = testEventListener.terminatedEvents.get(0);
        assertEquals(employeeActiveA.getId(), event.employeeId());
        assertEquals(tenantA.getId(), event.organizationId());
        assertEquals("Standard exit", event.reason());
        assertNotNull(event.occurredAt());
    }

    @Test
    @DisplayName("11. rollbackDoesNotPublishTerminationEvent: rollback suppresses event and preserves status")
    void rollbackDoesNotPublishTerminationEvent() {
        assertThrows(RuntimeException.class, () -> {
            transactionTemplate.execute(status -> {
                employeeService.terminateEmployee(employeeActiveA.getId(), "Will rollback");
                throw new RuntimeException("Forced rollback in termination test");
            });
        });

        // Event must NOT be published due to AFTER_COMMIT
        assertTrue(testEventListener.terminatedEvents.isEmpty());

        // Status remains unchanged
        Employee reloaded = employeeRepository.findById(employeeActiveA.getId()).orElseThrow();
        assertEquals(EmployeeStatus.ACTIVE.name(), reloaded.getStatus());
        assertEquals("WORKING", reloaded.getCurrentStatus());
        assertEquals("AVAILABLE", reloaded.getAvailability());
    }

    @Test
    @DisplayName("12. terminationFromFnfPaymentDisbursement: payment release triggers EmployeeService.terminateEmployee")
    void terminationFromFnfPaymentDisbursement() {
        Long[] ids = transactionTemplate.execute(status -> {
            if (rlsSessionBinder != null) {
                rlsSessionBinder.bindTenantToCurrentTransaction(tenantA.getId(), false);
            }
            EmployeeExit exit = new EmployeeExit();
            exit.setOrganization(tenantA);
            exit.setEmployee(employeeActiveA);
            exit.setStatus("SETTLEMENT_APPROVED");
            exit.setExitType("RESIGNATION");
            exit.setResignationDate(LocalDate.now().minusDays(30));
            exit.setLastWorkingDate(LocalDate.now());
            exit.setCreatedAt(LocalDateTime.now());
            exit.setUpdatedAt(LocalDateTime.now());
            exit = exitRepository.save(exit);

            FnfSettlement settlement = new FnfSettlement();
            settlement.setOrganization(tenantA);
            settlement.setExit(exit);
            settlement.setStatus("SETTLEMENT_APPROVED");
            settlement.setNetSettlement(new BigDecimal("50000.00"));
            settlement = fnfRepository.save(settlement);

            ExitClearance clearance = new ExitClearance();
            clearance.setOrganization(tenantA);
            clearance.setExit(exit);
            clearance.setDepartment("FINANCE");
            clearance.setAssignedTo(employeeActiveA);
            clearance.setClearanceReason("Standard exit clearance");
            clearance.setStatus("CLEARED");
            clearance.setCreatedAt(LocalDateTime.now());
            clearance.setUpdatedAt(LocalDateTime.now());
            clearanceRepository.save(clearance);

            return new Long[]{exit.getId(), settlement.getId()};
        });

        Long exitId = ids[0];
        Long settlementId = ids[1];

        FnfPaymentRequest request = new FnfPaymentRequest();
        request.setAmount(new BigDecimal("50000.00"));
        request.setPaymentMethod("BANK_TRANSFER");
        request.setPaymentDate(LocalDate.now());
        request.setTransactionReference("UTR" + System.currentTimeMillis());
        request.setIdempotencyKey("IDEM-" + System.currentTimeMillis());
        request.setRemarks("F&F settlement release");

        fnfSettlementService.processPayment(adminUserA, settlementId, request);

        // Verify settlement status with RLS bound
        transactionTemplate.execute(status -> {
            if (rlsSessionBinder != null) {
                rlsSessionBinder.bindTenantToCurrentTransaction(tenantA.getId(), false);
            }
            FnfSettlement reloadedSettlement = fnfRepository.findById(settlementId).orElseThrow();
            assertEquals("PAYMENT_RELEASED", reloadedSettlement.getStatus());

            EmployeeExit reloadedExit = exitRepository.findById(exitId).orElseThrow();
            assertEquals("SETTLEMENT_COMPLETED", reloadedExit.getStatus());
            return null;
        });

        // Verify employee lifecycle mutation
        Employee reloadedEmp = employeeRepository.findById(employeeActiveA.getId()).orElseThrow();
        assertEquals("TERMINATED", reloadedEmp.getStatus());
        assertEquals("EXITED", reloadedEmp.getCurrentStatus());
        assertEquals("UNAVAILABLE", reloadedEmp.getAvailability());

        // Verify event publication
        assertEquals(1, testEventListener.terminatedEvents.size());
        EmployeeTerminatedEvent event = testEventListener.terminatedEvents.get(0);
        assertEquals(employeeActiveA.getId(), event.employeeId());
        assertEquals(tenantA.getId(), event.organizationId());
        assertEquals("Full and final settlement payment disbursed", event.reason());
    }

    @Test
    @DisplayName("13. paymentDisbursementAndTerminationAreAtomic: termination failure rolls back settlement and exit updates")
    void paymentDisbursementAndTerminationAreAtomic() {
        // Put employee into an invalid state for termination (e.g. ONBOARDING, which cannot transition to TERMINATED)
        employeeActiveA.setStatus("ONBOARDING");
        employeeRepository.save(employeeActiveA);

        Long[] ids = transactionTemplate.execute(status -> {
            if (rlsSessionBinder != null) {
                rlsSessionBinder.bindTenantToCurrentTransaction(tenantA.getId(), false);
            }
            EmployeeExit exit = new EmployeeExit();
            exit.setOrganization(tenantA);
            exit.setEmployee(employeeActiveA);
            exit.setStatus("SETTLEMENT_APPROVED");
            exit.setExitType("RESIGNATION");
            exit.setResignationDate(LocalDate.now().minusDays(30));
            exit.setLastWorkingDate(LocalDate.now());
            exit.setCreatedAt(LocalDateTime.now());
            exit.setUpdatedAt(LocalDateTime.now());
            exit = exitRepository.save(exit);

            FnfSettlement settlement = new FnfSettlement();
            settlement.setOrganization(tenantA);
            settlement.setExit(exit);
            settlement.setStatus("SETTLEMENT_APPROVED");
            settlement.setNetSettlement(new BigDecimal("50000.00"));
            settlement = fnfRepository.save(settlement);

            ExitClearance clearance = new ExitClearance();
            clearance.setOrganization(tenantA);
            clearance.setExit(exit);
            clearance.setDepartment("FINANCE");
            clearance.setAssignedTo(employeeActiveA);
            clearance.setClearanceReason("Standard exit clearance");
            clearance.setStatus("CLEARED");
            clearance.setCreatedAt(LocalDateTime.now());
            clearance.setUpdatedAt(LocalDateTime.now());
            clearanceRepository.save(clearance);

            return new Long[]{exit.getId(), settlement.getId()};
        });

        Long exitId = ids[0];
        Long settlementId = ids[1];

        FnfPaymentRequest request = new FnfPaymentRequest();
        request.setAmount(new BigDecimal("50000.00"));
        request.setPaymentMethod("BANK_TRANSFER");
        request.setPaymentDate(LocalDate.now());
        request.setTransactionReference("UTR" + System.currentTimeMillis());
        request.setIdempotencyKey("IDEM-FAIL-" + System.currentTimeMillis());
        request.setRemarks("F&F settlement release should fail");

        assertThrows(IllegalStateException.class, () ->
                fnfSettlementService.processPayment(adminUserA, settlementId, request));

        // Verify atomic rollback with RLS bound
        transactionTemplate.execute(status -> {
            if (rlsSessionBinder != null) {
                rlsSessionBinder.bindTenantToCurrentTransaction(tenantA.getId(), false);
            }
            FnfSettlement rolledBackSettlement = fnfRepository.findById(settlementId).orElseThrow();
            assertEquals("SETTLEMENT_APPROVED", rolledBackSettlement.getStatus());

            EmployeeExit rolledBackExit = exitRepository.findById(exitId).orElseThrow();
            assertEquals("SETTLEMENT_APPROVED", rolledBackExit.getStatus());
            return null;
        });

        Employee rolledBackEmp = employeeRepository.findById(employeeActiveA.getId()).orElseThrow();
        assertEquals("ONBOARDING", rolledBackEmp.getStatus());

        assertTrue(testEventListener.terminatedEvents.isEmpty());
    }

    @Test
    @DisplayName("14. crossTenantEmployeeCannotBeTerminated: employee from Tenant B cannot be terminated by Tenant A user")
    void crossTenantEmployeeCannotBeTerminated() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                employeeService.terminateEmployee(employeeOrgB.getId(), "Cross tenant termination"));
        assertTrue(ex.getMessage().contains("Employee not found with ID: " + employeeOrgB.getId()));

        Employee reloaded = employeeRepository.findById(employeeOrgB.getId()).orElseThrow();
        assertEquals(EmployeeStatus.ACTIVE.name(), reloaded.getStatus());
        assertTrue(testEventListener.terminatedEvents.isEmpty());
    }

    // ── Test Event Listener Component ────────────────────────────────────────

    @Component
    public static class TestTerminationEventListener {
        public final List<EmployeeTerminatedEvent> terminatedEvents = Collections.synchronizedList(new ArrayList<>());

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void onTerminated(EmployeeTerminatedEvent e) {
            terminatedEvents.add(e);
        }

        public void clearEvents() {
            terminatedEvents.clear();
        }
    }
}
