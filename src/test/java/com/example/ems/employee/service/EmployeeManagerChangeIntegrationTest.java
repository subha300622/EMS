package com.example.ems.employee.service;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.event.EmployeeManagerChangedEvent;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
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
@Import(EmployeeManagerChangeIntegrationTest.TestManagerChangeEventListener.class)
public class EmployeeManagerChangeIntegrationTest {

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
    private TestManagerChangeEventListener testEventListener;

    private Organization tenantA;
    private Organization tenantB;

    private Employee manager1OrgA;
    private Employee manager2OrgA;
    private Employee inactiveManagerOrgA;
    private Employee employeeOrgA;
    private Employee managerOrgB;

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

        manager1OrgA = createEmployee("MGR1_" + ts, "Manager Alice", "mgr1." + ts + "@tenanta.com", tenantA,
                "ACTIVE", null);
        manager2OrgA = createEmployee("MGR2_" + ts, "Manager Bob", "mgr2." + ts + "@tenanta.com", tenantA,
                "ACTIVE", null);
        inactiveManagerOrgA = createEmployee("INACT_MGR_" + ts, "Inactive Eve", "eve." + ts + "@tenanta.com", tenantA,
                "INACTIVE", null);

        employeeOrgA = createEmployee("EMP_A_" + ts, "Worker Charlie", "charlie." + ts + "@tenanta.com", tenantA,
                "ACTIVE", manager1OrgA);

        managerOrgB = createEmployee("MGR_B_" + ts, "Manager OrgB", "mgr." + ts + "@tenantb.com", tenantB,
                "ACTIVE", null);

        Role adminRole = roleRepository.findByName("HR_ADMIN").orElseGet(() -> {
            Role r = new Role();
            r.setName("HR_ADMIN");
            r.setDescription("HR Admin");
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

    private Employee createEmployee(String empCode, String name, String email, Organization org, String status,
            Employee manager) {
        Employee emp = new Employee();
        emp.setEmployeeId(empCode);
        emp.setFirstName(name);
        emp.setLastName("MgrTest");
        emp.setEmail(email);
        emp.setOrganization(org);
        emp.setStatus(status);
        emp.setDepartment("Engineering");
        emp.setDesignation("Software Engineer");
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

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("1. validManagerChange: changes manager, updates state, and publishes event")
    void validManagerChange() {
        assertEquals(manager1OrgA.getId(), employeeOrgA.getManager().getId());

        EmployeeManagerChangedEvent event = employeeService.changeManager(
                employeeOrgA.getId(),
                manager2OrgA.getId()
        );

        assertNotNull(event);
        assertEquals(employeeOrgA.getId(), event.employeeId());
        assertEquals(tenantA.getId(), event.organizationId());
        assertEquals(manager1OrgA.getId(), event.oldManagerId());
        assertEquals(manager2OrgA.getId(), event.newManagerId());
        assertNotNull(event.occurredAt());

        // Verify entity updated
        Employee updated = employeeRepository.findById(employeeOrgA.getId()).orElseThrow();
        assertNotNull(updated.getManager());
        assertEquals(manager2OrgA.getId(), updated.getManager().getId());

        // Verify event captured by listener
        assertEquals(1, testEventListener.managerChangedEvents.size());
        assertEquals(event, testEventListener.managerChangedEvents.get(0));
    }

    @Test
    @DisplayName("2. validManagerChangeFromNullManager: changes manager when employee currently has null manager")
    void validManagerChangeFromNullManager() {
        long ts = System.currentTimeMillis();
        Employee empNoManager = createEmployee("NO_MGR_" + ts, "Solo Dev", "solo." + ts + "@tenanta.com", tenantA,
                "ACTIVE", null);
        assertNull(empNoManager.getManager());

        EmployeeManagerChangedEvent event = employeeService.changeManager(
                empNoManager.getId(),
                manager1OrgA.getId()
        );

        assertNotNull(event);
        assertNull(event.oldManagerId());
        assertEquals(manager1OrgA.getId(), event.newManagerId());

        Employee updated = employeeRepository.findById(empNoManager.getId()).orElseThrow();
        assertNotNull(updated.getManager());
        assertEquals(manager1OrgA.getId(), updated.getManager().getId());
    }

    @Test
    @DisplayName("3. rejectsMissingEmployee: throws BadRequestException when employeeId is null or nonexistent")
    void rejectsMissingEmployee() {
        BadRequestException exNull = assertThrows(BadRequestException.class, () ->
                employeeService.changeManager(null, manager1OrgA.getId()));
        assertTrue(exNull.getMessage().contains("Employee ID is required"));

        BadRequestException exUnknown = assertThrows(BadRequestException.class, () ->
                employeeService.changeManager(999999L, manager1OrgA.getId()));
        assertTrue(exUnknown.getMessage().contains("Employee not found with ID: 999999"));
    }

    @Test
    @DisplayName("4. rejectsMissingManager: throws BadRequestException when newManagerId is null")
    void rejectsMissingManager() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.changeManager(employeeOrgA.getId(), null));
        assertTrue(ex.getMessage().contains("New reporting manager ID is required"));
    }

    @Test
    @DisplayName("5. rejectsUnknownManager: throws BadRequestException when manager does not exist")
    void rejectsUnknownManager() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.changeManager(employeeOrgA.getId(), 999999L));
        assertTrue(ex.getMessage().contains("Reporting manager not found with ID: 999999"));
    }

    @Test
    @DisplayName("6. rejectsSelfManager: throws BadRequestException when employee attempts to report to themselves")
    void rejectsSelfManager() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.changeManager(employeeOrgA.getId(), employeeOrgA.getId()));
        assertTrue(ex.getMessage().contains("Employee cannot report to themselves"));
    }

    @Test
    @DisplayName("7. rejectsInactiveManager: throws BadRequestException when manager is not ACTIVE")
    void rejectsInactiveManager() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.changeManager(employeeOrgA.getId(), inactiveManagerOrgA.getId()));
        assertTrue(ex.getMessage().contains("Reporting Manager is not active"));
    }

    @Test
    @DisplayName("8. rejectsCrossTenantManager: throws BadRequestException when manager belongs to different organization")
    void rejectsCrossTenantManager() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.changeManager(employeeOrgA.getId(), managerOrgB.getId()));
        assertTrue(ex.getMessage().contains("Reporting Manager does not belong to the same organization"));
    }

    @Test
    @DisplayName("9. rejectsCircularHierarchy: A -> B, B -> C, then C -> A throws BadRequestException")
    void rejectsCircularHierarchy() {
        long ts = System.currentTimeMillis();
        // Construct hierarchy: A -> reports to B, B -> reports to C
        Employee empC = createEmployee("EMP_C_" + ts, "Node C", "c." + ts + "@tenanta.com", tenantA, "ACTIVE", null);
        Employee empB = createEmployee("EMP_B_" + ts, "Node B", "b." + ts + "@tenanta.com", tenantA, "ACTIVE", empC);
        Employee empA = createEmployee("EMP_A_" + ts, "Node A", "a." + ts + "@tenanta.com", tenantA, "ACTIVE", empB);

        // Attempt C -> reports to A (forming cycle C -> A -> B -> C)
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.changeManager(empC.getId(), empA.getId()));
        assertTrue(ex.getMessage().contains("Circular reporting chain detected"));

        // Verify relationships remain intact
        Employee reloadedC = employeeRepository.findById(empC.getId()).orElseThrow();
        Employee reloadedB = employeeRepository.findById(empB.getId()).orElseThrow();
        Employee reloadedA = employeeRepository.findById(empA.getId()).orElseThrow();

        assertNull(reloadedC.getManager());
        assertEquals(empC.getId(), reloadedB.getManager().getId());
        assertEquals(empB.getId(), reloadedA.getManager().getId());
    }

    @Test
    @DisplayName("10. rejectsSameExistingManager: throws BadRequestException when employee already reports to manager")
    void rejectsSameExistingManager() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.changeManager(employeeOrgA.getId(), manager1OrgA.getId()));
        assertTrue(ex.getMessage().contains("Employee already reports to this manager"));
    }

    @Test
    @DisplayName("11. persistsManagerChange: changes are committed and verified in database")
    void persistsManagerChange() {
        employeeService.changeManager(employeeOrgA.getId(), manager2OrgA.getId());

        Employee persisted = employeeRepository.findById(employeeOrgA.getId()).orElseThrow();
        assertNotNull(persisted.getManager());
        assertEquals(manager2OrgA.getId(), persisted.getManager().getId());
    }

    @Test
    @DisplayName("12. publishesEmployeeManagerChangedEvent: verifies event details")
    void publishesEmployeeManagerChangedEvent() {
        employeeService.changeManager(employeeOrgA.getId(), manager2OrgA.getId());

        assertEquals(1, testEventListener.managerChangedEvents.size());
        EmployeeManagerChangedEvent event = testEventListener.managerChangedEvents.get(0);
        assertEquals(employeeOrgA.getId(), event.employeeId());
        assertEquals(tenantA.getId(), event.organizationId());
        assertEquals(manager1OrgA.getId(), event.oldManagerId());
        assertEquals(manager2OrgA.getId(), event.newManagerId());
        assertNotNull(event.occurredAt());
    }

    @Test
    @DisplayName("13. rollbackDoesNotPublishEvent: rolling back transaction suppresses event and leaves state unchanged")
    void rollbackDoesNotPublishEvent() {
        assertThrows(RuntimeException.class, () -> {
            transactionTemplate.execute(status -> {
                employeeService.changeManager(employeeOrgA.getId(), manager2OrgA.getId());
                throw new RuntimeException("Forced rollback in manager change test");
            });
        });

        // Event must NOT be published due to AFTER_COMMIT
        assertTrue(testEventListener.managerChangedEvents.isEmpty());

        // Employee manager remains unchanged in database
        Employee reloaded = employeeRepository.findById(employeeOrgA.getId()).orElseThrow();
        assertNotNull(reloaded.getManager());
        assertEquals(manager1OrgA.getId(), reloaded.getManager().getId());
    }

    // ── Test Event Listener Component ────────────────────────────────────────

    @Component
    public static class TestManagerChangeEventListener {
        public final List<EmployeeManagerChangedEvent> managerChangedEvents = Collections.synchronizedList(new ArrayList<>());

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void onManagerChanged(EmployeeManagerChangedEvent e) {
            managerChangedEvents.add(e);
        }

        public void clearEvents() {
            managerChangedEvents.clear();
        }
    }
}
