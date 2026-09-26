package com.example.ems.employee.service;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.security.context.TenantContext;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.EmployeeStatus;
import com.example.ems.employee.event.*;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.organization.repository.OrganizationRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(EmployeeLifecycleServiceIntegrationTest.TestServiceLifecycleEventListener.class)
public class EmployeeLifecycleServiceIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private TestServiceLifecycleEventListener testEventListener;

    private Organization tenantA;
    private Organization tenantB;
    private Department deptEngineeringA;
    private Department deptSalesB;

    private Employee employeeOrgA;
    private Employee managerOrgA;
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

        deptEngineeringA = new Department();
        deptEngineeringA.setName("Engineering " + ts);
        deptEngineeringA.setCode("ENG_" + ts);
        deptEngineeringA.setOrganization(tenantA);
        deptEngineeringA.setStatus("ACTIVE");
        deptEngineeringA = departmentRepository.save(deptEngineeringA);

        deptSalesB = new Department();
        deptSalesB.setName("Sales " + ts);
        deptSalesB.setCode("SALES_" + ts);
        deptSalesB.setOrganization(tenantB);
        deptSalesB.setStatus("ACTIVE");
        deptSalesB = departmentRepository.save(deptSalesB);

        managerOrgA = createEmployee("MGR_A_" + ts, "Manager Alice", "mgr.alice." + ts + "@tenanta.com", tenantA,
                "ACTIVE", deptEngineeringA.getName(), null);
        employeeOrgA = createEmployee("EMP_A_" + ts, "Bob Employee", "bob." + ts + "@tenanta.com", tenantA,
                "ONBOARDING", deptEngineeringA.getName(), managerOrgA);
        employeeOrgB = createEmployee("EMP_B_" + ts, "Charlie OrgB", "charlie." + ts + "@tenantb.com", tenantB,
                "ACTIVE", deptSalesB.getName(), null);

        Role adminRole = roleRepository.findByName("HR_ADMIN").orElseGet(() -> {
            Role r = new Role();
            r.setName("HR_ADMIN");
            r.setDescription("HR Admin");
            return roleRepository.save(r);
        });

        adminUserA = createUser("admin.a." + ts + "@tenanta.com", tenantA, adminRole);
        createUser("admin.b." + ts + "@tenantb.com", tenantB, adminRole);

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

    private Employee createEmployee(String empCode, String name, String email, Organization org, String status,
            String deptName, Employee manager) {
        Employee emp = new Employee();
        emp.setEmployeeId(empCode);
        emp.setFirstName(name);
        emp.setLastName("Test");
        emp.setEmail(email);
        emp.setOrganization(org);
        emp.setStatus(status);
        emp.setDepartment(deptName);
        emp.setDesignation("Engineer");
        emp.setAnnualSalary(new BigDecimal("95000.00"));
        emp.setJoiningDate(LocalDate.now());
        emp.setManager(manager);
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

    // ── Status Transitions Integration Tests ─────────────────────────────────

    @Test
    @DisplayName("ONBOARDING -> PROBATION emits EmployeeJoinedEvent and updates state")
    void testOnboardingToProbation() {
        assertEquals("ONBOARDING", employeeOrgA.getStatus());

        Employee updated = employeeService.updateEmployeeStatus(employeeOrgA.getId(), EmployeeStatus.PROBATION,
                "Passed onboarding checks");

        assertEquals("PROBATION", updated.getStatus());
        assertEquals(1, testEventListener.joinedEvents.size());
        assertEquals(employeeOrgA.getId(), testEventListener.joinedEvents.get(0).employeeId());
        assertEquals(tenantA.getId(), testEventListener.joinedEvents.get(0).organizationId());
    }

    @Test
    @DisplayName("PROBATION -> ACTIVE emits EmployeeActivatedEvent and updates state")
    void testProbationToActive() {
        employeeOrgA.setStatus("PROBATION");
        employeeRepository.save(employeeOrgA);

        Employee updated = employeeService.updateEmployeeStatus(employeeOrgA.getId(), EmployeeStatus.ACTIVE,
                "Probation completed");

        assertEquals("ACTIVE", updated.getStatus());
        assertEquals(1, testEventListener.activatedEvents.size());
        assertEquals(employeeOrgA.getId(), testEventListener.activatedEvents.get(0).employeeId());
    }

    @Test
    @DisplayName("ACTIVE -> SUSPENDED emits EmployeeSuspendedEvent with reason")
    void testActiveToSuspended() {
        Employee updated = employeeService.updateEmployeeStatus(managerOrgA.getId(), EmployeeStatus.SUSPENDED,
                "Investigation pending");

        assertEquals("SUSPENDED", updated.getStatus());
        assertEquals(1, testEventListener.suspendedEvents.size());
        assertEquals(managerOrgA.getId(), testEventListener.suspendedEvents.get(0).employeeId());
        assertEquals("Investigation pending", testEventListener.suspendedEvents.get(0).reason());
    }

    @Test
    @DisplayName("SUSPENDED -> ACTIVE emits EmployeeActivatedEvent")
    void testSuspendedToActive() {
        managerOrgA.setStatus("SUSPENDED");
        employeeRepository.save(managerOrgA);

        Employee updated = employeeService.updateEmployeeStatus(managerOrgA.getId(), EmployeeStatus.ACTIVE,
                "Reinstated");

        assertEquals("ACTIVE", updated.getStatus());
        assertEquals(1, testEventListener.activatedEvents.size());
        assertEquals(managerOrgA.getId(), testEventListener.activatedEvents.get(0).employeeId());
    }

    @Test
    @DisplayName("ACTIVE -> NOTICE_PERIOD succeeds without generic lifecycle event")
    void testActiveToNoticePeriod() {
        Employee updated = employeeService.updateEmployeeStatus(managerOrgA.getId(), EmployeeStatus.NOTICE_PERIOD,
                "Resignation submitted");

        assertEquals("NOTICE_PERIOD", updated.getStatus());
        assertTrue(testEventListener.activatedEvents.isEmpty());
        assertTrue(testEventListener.suspendedEvents.isEmpty());
        assertTrue(testEventListener.terminatedEvents.isEmpty());
    }

    @Test
    @DisplayName("Direct update to TERMINATED is rejected to protect offboarding workflow")
    void testDirectTerminationRejected() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> employeeService.updateEmployeeStatus(managerOrgA.getId(), EmployeeStatus.TERMINATED, "Bypass"));

        assertTrue(ex.getMessage().contains("Direct termination via status update is not allowed"));
    }

    @Test
    @DisplayName("Invalid status transition is rejected with IllegalStateException")
    void testInvalidTransitionRejected() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> employeeService
                .updateEmployeeStatus(managerOrgA.getId(), EmployeeStatus.ONBOARDING, "Invalid reverse"));

        assertTrue(ex.getMessage().contains("Invalid status transition"));
    }

    @Test
    @DisplayName("Same status transition is rejected with IllegalStateException")
    void testSameStatusRejected() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> employeeService.updateEmployeeStatus(managerOrgA.getId(), EmployeeStatus.ACTIVE, "Redundant"));

        assertTrue(ex.getMessage().contains("already in ACTIVE status"));
    }

    @Test
    @DisplayName("Terminated employee cannot reactivate via updateEmployeeStatus")
    void testTerminatedEmployeeCannotReactivate() {
        managerOrgA.setStatus("TERMINATED");
        employeeRepository.save(managerOrgA);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> employeeService.updateEmployeeStatus(managerOrgA.getId(), EmployeeStatus.ACTIVE, "Reactivate"));

        assertTrue(ex.getMessage().contains("Invalid status transition from TERMINATED to ACTIVE"));
    }

    // ── Activation Flow Tests ────────────────────────────────────────────────

    @Test
    @DisplayName("activateEmployee succeeds and publishes EmployeeActivatedEvent")
    void testActivateEmployeeSuccess() {
        employeeOrgA.setStatus("PROBATION");
        employeeRepository.save(employeeOrgA);

        Employee activated = employeeService.activateEmployee(employeeOrgA.getId());

        assertEquals("ACTIVE", activated.getStatus());
        assertEquals(1, testEventListener.activatedEvents.size());
        assertEquals(employeeOrgA.getId(), testEventListener.activatedEvents.get(0).employeeId());
    }

    @Test
    @DisplayName("activateEmployee rejects if reporting manager is not active")
    void testActivateEmployeeRejectsInactiveManager() {
        managerOrgA.setStatus("SUSPENDED");
        employeeRepository.save(managerOrgA);

        employeeOrgA.setStatus("PROBATION");
        employeeRepository.save(employeeOrgA);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> employeeService.activateEmployee(employeeOrgA.getId()));

        assertTrue(ex.getMessage().contains("Reporting Manager is not active"));
    }

    @Test
    @DisplayName("activateEmployee rejects if reporting manager belongs to another tenant")
    void testActivateEmployeeRejectsCrossTenantManager() {
        employeeOrgA.setStatus("PROBATION");
        employeeOrgA.setManager(employeeOrgB); // Cross-tenant manager
        employeeRepository.save(employeeOrgA);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> employeeService.activateEmployee(employeeOrgA.getId()));

        assertTrue(ex.getMessage().contains("Reporting Manager does not belong to the same organization"));
    }

    @Test
    @DisplayName("activateEmployee rejects self-reporting manager")
    void testActivateEmployeeRejectsSelfReporting() {
        employeeOrgA.setStatus("PROBATION");
        employeeOrgA.setManager(employeeOrgA); // Self reporting
        employeeRepository.save(employeeOrgA);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> employeeService.activateEmployee(employeeOrgA.getId()));

        assertTrue(ex.getMessage().contains("Employee cannot report to themselves"));
    }

    @Test
    @DisplayName("activateEmployee rejects circular reporting chain")
    void testActivateEmployeeRejectsCircularReporting() {
        // managerOrgA reports to employeeOrgA, while employeeOrgA reports to
        // managerOrgA
        managerOrgA.setManager(employeeOrgA);
        employeeRepository.save(managerOrgA);

        employeeOrgA.setStatus("PROBATION");
        employeeOrgA.setManager(managerOrgA);
        employeeRepository.save(employeeOrgA);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> employeeService.activateEmployee(employeeOrgA.getId()));

        assertTrue(ex.getMessage().contains("Circular reporting chain detected"));
    }

    // ── Tenant Isolation Tests ───────────────────────────────────────────────

    @Test
    @DisplayName("Tenant A admin cannot update Employee from Tenant B")
    void testCrossTenantStatusUpdateBlocked() {
        authenticateAs(adminUserA);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> employeeService
                .updateEmployeeStatus(employeeOrgB.getId(), EmployeeStatus.SUSPENDED, "Cross tenant"));

        assertTrue(ex.getMessage().contains("Employee not found with ID"));
    }

    @Test
    @DisplayName("Tenant A admin cannot activate Employee from Tenant B")
    void testCrossTenantActivationBlocked() {
        authenticateAs(adminUserA);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.activateEmployee(employeeOrgB.getId()));

        assertTrue(ex.getMessage().contains("Employee not found with ID"));
    }

    // ── Transaction Rollback Event Suppression ───────────────────────────────

    @Test
    @DisplayName("Failed transaction produces no AFTER_COMMIT lifecycle events")
    void testRollbackSuppressesEvents() {
        employeeOrgA.setStatus("PROBATION");
        employeeRepository.save(employeeOrgA);

        try {
            transactionTemplate.execute(status -> {
                employeeService.activateEmployee(employeeOrgA.getId());
                throw new RuntimeException("Forced rollback in test");
            });
        } catch (RuntimeException ignored) {
        }

        // State was rolled back and no commit events were delivered
        assertTrue(testEventListener.activatedEvents.isEmpty());
        Employee reloaded = employeeRepository.findById(employeeOrgA.getId()).orElseThrow();
        assertEquals("PROBATION", reloaded.getStatus());
    }

    // ── Test Event Listener Component ────────────────────────────────────────

    @Component
    public static class TestServiceLifecycleEventListener {
        public final List<EmployeeJoinedEvent> joinedEvents = Collections.synchronizedList(new ArrayList<>());
        public final List<EmployeeActivatedEvent> activatedEvents = Collections.synchronizedList(new ArrayList<>());
        public final List<EmployeeSuspendedEvent> suspendedEvents = Collections.synchronizedList(new ArrayList<>());
        public final List<EmployeeTerminatedEvent> terminatedEvents = Collections.synchronizedList(new ArrayList<>());

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void onJoined(EmployeeJoinedEvent e) {
            joinedEvents.add(e);
        }

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void onActivated(EmployeeActivatedEvent e) {
            activatedEvents.add(e);
        }

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void onSuspended(EmployeeSuspendedEvent e) {
            suspendedEvents.add(e);
        }

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void onTerminated(EmployeeTerminatedEvent e) {
            terminatedEvents.add(e);
        }

        public void clearEvents() {
            joinedEvents.clear();
            activatedEvents.clear();
            suspendedEvents.clear();
            terminatedEvents.clear();
        }
    }
}
