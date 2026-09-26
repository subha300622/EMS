package com.example.ems.employee.service;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.DepartmentTransfer;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.event.EmployeeTransferredEvent;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.DepartmentTransferRepository;
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
@Import(EmployeeDepartmentTransferIntegrationTest.TestTransferEventListener.class)
public class EmployeeDepartmentTransferIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentTransferRepository departmentTransferRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private TestTransferEventListener testEventListener;

    private Organization tenantA;
    private Organization tenantB;

    private Department deptEngineeringA;
    private Department deptFinanceA;
    private Department deptInactiveA;
    private Department deptSalesB;

    private Employee employeeOrgA;
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

        deptFinanceA = new Department();
        deptFinanceA.setName("Finance " + ts);
        deptFinanceA.setCode("FIN_" + ts);
        deptFinanceA.setOrganization(tenantA);
        deptFinanceA.setStatus("ACTIVE");
        deptFinanceA = departmentRepository.save(deptFinanceA);

        deptInactiveA = new Department();
        deptInactiveA.setName("Inactive " + ts);
        deptInactiveA.setCode("INA_" + ts);
        deptInactiveA.setOrganization(tenantA);
        deptInactiveA.setStatus("INACTIVE");
        deptInactiveA = departmentRepository.save(deptInactiveA);

        deptSalesB = new Department();
        deptSalesB.setName("Sales " + ts);
        deptSalesB.setCode("SALES_" + ts);
        deptSalesB.setOrganization(tenantB);
        deptSalesB.setStatus("ACTIVE");
        deptSalesB = departmentRepository.save(deptSalesB);

        employeeOrgA = createEmployee("EMP_A_" + ts, "Bob Tester", "bob." + ts + "@tenanta.com", tenantA,
                "ACTIVE", deptEngineeringA.getName());

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
            String deptName) {
        Employee emp = new Employee();
        emp.setEmployeeId(empCode);
        emp.setFirstName(name);
        emp.setLastName("TransferTest");
        emp.setEmail(email);
        emp.setOrganization(org);
        emp.setStatus(status);
        emp.setDepartment(deptName);
        emp.setDesignation("Engineer");
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
    @DisplayName("1. validTransfer: updates employee department, saves transfer record, and publishes event")
    void validTransfer() {
        LocalDate effectiveDate = LocalDate.now().plusDays(1);
        String remarks = "Promoted and transferred to Finance";

        DepartmentTransfer transfer = employeeService.transferEmployee(
                employeeOrgA.getId(),
                deptEngineeringA.getId(),
                deptFinanceA.getId(),
                effectiveDate,
                remarks
        );

        assertNotNull(transfer);
        assertNotNull(transfer.getId());
        assertEquals(employeeOrgA.getId(), transfer.getEmployeeId());
        assertEquals(deptEngineeringA.getId(), transfer.getFromDepartmentId());
        assertEquals(deptFinanceA.getId(), transfer.getToDepartmentId());
        assertEquals(effectiveDate, transfer.getEffectiveDate());
        assertEquals(remarks, transfer.getRemarks());

        // Verify employee department updated
        Employee updatedEmployee = employeeRepository.findById(employeeOrgA.getId()).orElseThrow();
        assertEquals(deptFinanceA.getName(), updatedEmployee.getDepartment());

        // Verify event published
        assertEquals(1, testEventListener.transferEvents.size());
        EmployeeTransferredEvent event = testEventListener.transferEvents.get(0);
        assertEquals(employeeOrgA.getId(), event.employeeId());
        assertEquals(tenantA.getId(), event.organizationId());
        assertEquals(deptEngineeringA.getId(), event.fromDepartmentId());
        assertEquals(deptFinanceA.getId(), event.toDepartmentId());
        assertNotNull(event.occurredAt());
    }

    @Test
    @DisplayName("validTransfer with optional null fromDepartmentId succeeds")
    void validTransferWithNullSourceDepartment() {
        LocalDate effectiveDate = LocalDate.now();
        String remarks = "Transfer without explicit source department";

        DepartmentTransfer transfer = employeeService.transferEmployee(
                employeeOrgA.getId(),
                null,
                deptFinanceA.getId(),
                effectiveDate,
                remarks
        );

        assertNotNull(transfer);
        assertNull(transfer.getFromDepartmentId());
        assertEquals(deptFinanceA.getId(), transfer.getToDepartmentId());

        Employee updatedEmployee = employeeRepository.findById(employeeOrgA.getId()).orElseThrow();
        assertEquals(deptFinanceA.getName(), updatedEmployee.getDepartment());

        assertEquals(1, testEventListener.transferEvents.size());
        EmployeeTransferredEvent event = testEventListener.transferEvents.get(0);
        assertNull(event.fromDepartmentId());
        assertEquals(deptFinanceA.getId(), event.toDepartmentId());
    }

    @Test
    @DisplayName("2. rejectsSameDepartment: throws BadRequestException when fromDeptId equals toDeptId")
    void rejectsSameDepartment_WhenIdsMatch() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.transferEmployee(
                        employeeOrgA.getId(),
                        deptEngineeringA.getId(),
                        deptEngineeringA.getId(),
                        LocalDate.now(),
                        "Same dept"
                )
        );
        assertTrue(ex.getMessage().contains("Source and destination departments must be different"));
    }

    @Test
    @DisplayName("rejectsSameDepartment: throws BadRequestException when employee is already assigned to destination")
    void rejectsSameDepartment_WhenAlreadyAssigned() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.transferEmployee(
                        employeeOrgA.getId(),
                        null,
                        deptEngineeringA.getId(),
                        LocalDate.now(),
                        "Already in engineering"
                )
        );
        assertTrue(ex.getMessage().contains("already assigned to the destination department"));
    }

    @Test
    @DisplayName("3. rejectsNonexistentTargetDepartment: throws BadRequestException")
    void rejectsNonexistentTargetDepartment() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.transferEmployee(
                        employeeOrgA.getId(),
                        deptEngineeringA.getId(),
                        999999L,
                        LocalDate.now(),
                        "Nonexistent dept"
                )
        );
        assertTrue(ex.getMessage().contains("Destination department not found with ID: 999999"));
    }

    @Test
    @DisplayName("4. rejectsCrossTenantTargetDepartment: throws BadRequestException")
    void rejectsCrossTenantTargetDepartment() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.transferEmployee(
                        employeeOrgA.getId(),
                        deptEngineeringA.getId(),
                        deptSalesB.getId(),
                        LocalDate.now(),
                        "Cross tenant transfer"
                )
        );
        assertTrue(ex.getMessage().contains("Destination department does not belong to the employee's organization"));
    }

    @Test
    @DisplayName("5. rejectsCrossTenantSourceDepartment: throws BadRequestException")
    void rejectsCrossTenantSourceDepartment() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.transferEmployee(
                        employeeOrgA.getId(),
                        deptSalesB.getId(),
                        deptFinanceA.getId(),
                        LocalDate.now(),
                        "Cross tenant source"
                )
        );
        // Either caught by name mismatch or assignment validator tenant check
        assertTrue(ex.getMessage().contains("Source department does not belong to the employee's organization")
                || ex.getMessage().contains("Source department does not match employee's current department"));
    }

    @Test
    @DisplayName("6. rejectsSourceDepartmentMismatch: throws BadRequestException when source dept name doesn't match employee's dept")
    void rejectsSourceDepartmentMismatch() {
        // Employee is in Engineering, but caller specifies Finance as source
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.transferEmployee(
                        employeeOrgA.getId(),
                        deptFinanceA.getId(),
                        deptEngineeringA.getId(),
                        LocalDate.now(),
                        "Mismatched source"
                )
        );
        assertTrue(ex.getMessage().contains("Source department does not match employee's current department"));
    }

    @Test
    @DisplayName("7. rejectsInactiveTargetDepartment: throws BadRequestException")
    void rejectsInactiveTargetDepartment() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                employeeService.transferEmployee(
                        employeeOrgA.getId(),
                        deptEngineeringA.getId(),
                        deptInactiveA.getId(),
                        LocalDate.now(),
                        "Transfer to inactive"
                )
        );
        assertTrue(ex.getMessage().contains("Destination department is not active"));
    }

    @Test
    @DisplayName("8. createsDepartmentTransferRecord: verifies persisted fields in repository")
    void createsDepartmentTransferRecord() {
        LocalDate effectiveDate = LocalDate.now().plusWeeks(2);
        String remarks = "Audit check transfer record";

        DepartmentTransfer transfer = employeeService.transferEmployee(
                employeeOrgA.getId(),
                deptEngineeringA.getId(),
                deptFinanceA.getId(),
                effectiveDate,
                remarks
        );

        DepartmentTransfer persisted = departmentTransferRepository.findById(transfer.getId()).orElseThrow();
        assertEquals(employeeOrgA.getId(), persisted.getEmployeeId());
        assertEquals(deptEngineeringA.getId(), persisted.getFromDepartmentId());
        assertEquals(deptFinanceA.getId(), persisted.getToDepartmentId());
        assertEquals(effectiveDate, persisted.getEffectiveDate());
        assertEquals(remarks, persisted.getRemarks());
        assertNotNull(persisted.getTransferDate());
    }

    @Test
    @DisplayName("9. publishesEmployeeTransferredEvent: verified with explicit event payload inspection")
    void publishesEmployeeTransferredEvent() {
        employeeService.transferEmployee(
                employeeOrgA.getId(),
                deptEngineeringA.getId(),
                deptFinanceA.getId(),
                LocalDate.now(),
                "Event inspection test"
        );

        assertEquals(1, testEventListener.transferEvents.size());
        EmployeeTransferredEvent event = testEventListener.transferEvents.get(0);
        assertEquals(employeeOrgA.getId(), event.employeeId());
        assertEquals(tenantA.getId(), event.organizationId());
        assertEquals(deptEngineeringA.getId(), event.fromDepartmentId());
        assertEquals(deptFinanceA.getId(), event.toDepartmentId());
        assertNotNull(event.occurredAt());
    }

    @Test
    @DisplayName("10. rollbackDoesNotPublishTransferEvent: rolling back transaction suppresses event and leaves state unchanged")
    void rollbackDoesNotPublishTransferEvent() {
        assertThrows(RuntimeException.class, () -> {
            transactionTemplate.execute(status -> {
                employeeService.transferEmployee(
                        employeeOrgA.getId(),
                        deptEngineeringA.getId(),
                        deptFinanceA.getId(),
                        LocalDate.now(),
                        "Will rollback"
                );
                throw new RuntimeException("Forced rollback");
            });
        });

        // Event must NOT be published due to AFTER_COMMIT
        assertTrue(testEventListener.transferEvents.isEmpty());

        // Employee department remains unchanged
        Employee reloaded = employeeRepository.findById(employeeOrgA.getId()).orElseThrow();
        assertEquals(deptEngineeringA.getName(), reloaded.getDepartment());

        // No transfer record saved
        List<DepartmentTransfer> transfers = departmentTransferRepository.findAll();
        boolean hasTransferForEmp = transfers.stream().anyMatch(t -> t.getEmployeeId().equals(employeeOrgA.getId()));
        assertFalse(hasTransferForEmp);
    }

    // ── Test Event Listener Component ────────────────────────────────────────

    @Component
    public static class TestTransferEventListener {
        public final List<EmployeeTransferredEvent> transferEvents = Collections.synchronizedList(new ArrayList<>());

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void onTransferred(EmployeeTransferredEvent e) {
            transferEvents.add(e);
        }

        public void clearEvents() {
            transferEvents.clear();
        }
    }
}
