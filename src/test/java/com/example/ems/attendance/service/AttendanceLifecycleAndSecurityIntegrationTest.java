package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.AttendanceCoreResponse;
import com.example.ems.attendance.dto.AttendanceDaySummaryDto;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.exception.AttendanceNotFoundException;
import com.example.ems.attendance.exception.DuplicateCheckInException;
import com.example.ems.attendance.exception.InvalidAttendanceStateException;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.dto.AuthAuthenticationToken;
import com.example.ems.security.dto.AuthPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AttendanceLifecycleAndSecurityIntegrationTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.ems.attendance.repository.AttendancePolicyRepository attendancePolicyRepository;

    @Autowired
    private AttendancePolicyService attendancePolicyService;

    @Autowired
    private Clock clock;

    private Organization tenantA;
    private Organization tenantB;

    private Employee employee1OrgA;
    private Employee employee2OrgA;
    private Employee employeeOrgB;

    private User user1OrgA;
    private User user2OrgA;
    private User userOrgB;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        // 1. Create 2 Distinct Tenants
        tenantA = new Organization();
        tenantA.setName("Tenant A " + ts);
        tenantA.setOrganizationCode("ORG_A_" + ts);
        tenantA = organizationRepository.save(tenantA);

        tenantB = new Organization();
        tenantB.setName("Tenant B " + ts);
        tenantB.setOrganizationCode("ORG_B_" + ts);
        tenantB = organizationRepository.save(tenantB);

        // Seed Active Policies
        com.example.ems.attendance.entity.AttendancePolicy polA = attendancePolicyService.createSystemDefaultPolicy(tenantA.getId());
        polA.setOrganization(tenantA);
        attendancePolicyRepository.save(polA);

        com.example.ems.attendance.entity.AttendancePolicy polB = attendancePolicyService.createSystemDefaultPolicy(tenantB.getId());
        polB.setOrganization(tenantB);
        attendancePolicyRepository.save(polB);

        // 2. Create Employees in Tenant A
        employee1OrgA = createEmployee("EMP_A1_" + ts, "Alice Worker", "alice." + ts + "@tenanta.com", tenantA);
        employee2OrgA = createEmployee("EMP_A2_" + ts, "Bob Colleague", "bob." + ts + "@tenanta.com", tenantA);

        // 3. Create Employee in Tenant B
        employeeOrgB = createEmployee("EMP_B1_" + ts, "Charlie TenantB", "charlie." + ts + "@tenantb.com", tenantB);

        // 4. Create Users linked to employees
        user1OrgA = createUser(employee1OrgA, tenantA);
        user2OrgA = createUser(employee2OrgA, tenantA);
        userOrgB = createUser(employeeOrgB, tenantB);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    private Employee createEmployee(String empCode, String name, String email, Organization org) {
        Employee emp = new Employee();
        emp.setEmployeeId(empCode);
        emp.setFirstName(name);
        emp.setLastName("Test");
        emp.setEmail(email);
        emp.setOrganization(org);
        emp.setStatus("ACTIVE");
        emp.setWorkMode("OFFICE");
        emp.setLocation("OFFICE_GATE");
        return employeeRepository.save(emp);
    }

    private User createUser(Employee emp, Organization org) {
        User u = new User();
        u.setUserId(emp.getEmployeeId());
        u.setWorkEmail(emp.getEmail());
        u.setOrganization(org);
        u.setOrganizationId(org.getId());
        u.setEmployeeId(emp.getEmployeeId());
        return userRepository.save(u);
    }

    private void authenticate(User user, Organization org, List<String> permissions) {
        TenantContext.setCurrentTenant(org.getId());
        AuthPrincipal principal = new AuthPrincipal(
                user.getUserId(),
                "SESS_" + user.getUserId(),
                1,
                1L,
                user.getWorkEmail(),
                "ROLE_USER"
        );

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (permissions != null) {
            for (String p : permissions) {
                authorities.add(new SimpleGrantedAuthority(p));
            }
        }

        AuthAuthenticationToken token = new AuthAuthenticationToken(principal, authorities);
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test
    @DisplayName("Check-In: Valid check-in creates WORKING record; duplicate check-in is prevented")
    void testCheckIn_LifecycleAndDuplicatePrevention() {
        // Authenticate employee 1 with self check-in authority
        authenticate(user1OrgA, tenantA, List.of("attendance.self.checkin", "attendance.self.read"));

        // 1. Initial check-in -> succeeds
        AttendanceCoreResponse checkInRes = attendanceService.checkInCore();
        assertNotNull(checkInRes);
        assertNotNull(checkInRes.getAttendanceId());
        assertEquals("WORKING", checkInRes.getStatus());
        assertEquals(employee1OrgA.getId(), checkInRes.getEmployeeId());

        // Verify record in database
        LocalDate today = LocalDate.now(clock);
        Attendance record = attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(
                employee1OrgA.getId(), today, tenantA.getId()).orElse(null);
        assertNotNull(record);
        assertEquals(AttendanceStatus.WORKING, record.getAttendanceStatus());
        assertNotNull(record.getCheckInTime());

        // 2. Duplicate check-in on the same day -> DuplicateCheckInException
        assertThrows(DuplicateCheckInException.class, () -> attendanceService.checkInCore());
    }

    @Test
    @DisplayName("Check-Out: Requires active check-in, succeeds from WORKING state, and prevents duplicate checkout")
    void testCheckOut_LifecycleAndDuplicatePrevention() {
        authenticate(user1OrgA, tenantA, List.of("attendance.self.checkin", "attendance.self.checkout", "attendance.self.read"));

        // 1. Check-out without check-in -> AttendanceNotFoundException
        assertThrows(AttendanceNotFoundException.class, () -> attendanceService.checkOutCore());

        // 2. Check-in first
        attendanceService.checkInCore();

        // 3. Check-out -> transitions to COMPLETED
        AttendanceCoreResponse checkOutRes = attendanceService.checkOutCore();
        assertNotNull(checkOutRes);
        assertEquals("COMPLETED", checkOutRes.getStatus());
        assertNotNull(checkOutRes.getCheckOutTime());
        assertTrue(checkOutRes.getTotalWorkingMinutes() >= 0);

        // 4. Duplicate check-out on already COMPLETED attendance -> InvalidAttendanceStateException
        assertThrows(InvalidAttendanceStateException.class, () -> attendanceService.checkOutCore());
    }

    @Test
    @DisplayName("Team Summary Authorization: Own summary allowed with self.read; another employee allowed with team.read, rejected without team.read")
    void testAttendanceDaySummary_AuthorizationBoundaries() {
        LocalDate today = LocalDate.now(clock);

        // Employee 1 checks in
        authenticate(user1OrgA, tenantA, List.of("attendance.self.checkin", "attendance.self.read"));
        attendanceService.checkInCore();

        // 1. Own employee viewing own summary with attendance.self.read -> ALLOWED
        AttendanceDaySummaryDto ownSummary = attendanceService.getAttendanceDaySummary(employee1OrgA.getId(), today);
        assertNotNull(ownSummary);
        assertEquals(employee1OrgA.getId(), ownSummary.getEmployeeId());
        assertEquals(AttendanceStatus.WORKING, ownSummary.getStatus());

        // 2. Employee 2 without team.read attempting to view Employee 1's summary -> REJECTED (403 AccessDeniedException)
        authenticate(user2OrgA, tenantA, List.of("attendance.self.read"));
        assertThrows(AccessDeniedException.class, () -> {
            attendanceService.getAttendanceDaySummary(employee1OrgA.getId(), today);
        });

        // 3. Employee 2 WITH team.read viewing Employee 1's summary -> ALLOWED
        authenticate(user2OrgA, tenantA, List.of("attendance.self.read", "attendance.team.read"));
        AttendanceDaySummaryDto colleagueSummary = attendanceService.getAttendanceDaySummary(employee1OrgA.getId(), today);
        assertNotNull(colleagueSummary);
        assertEquals(employee1OrgA.getId(), colleagueSummary.getEmployeeId());
        assertEquals(AttendanceStatus.WORKING, colleagueSummary.getStatus());
    }

    @Test
    @DisplayName("Cross-Tenant Isolation: User from Tenant B cannot perform attendance operations or view data in Tenant A context")
    void testCrossTenantIsolation() {
        // User from Tenant B attempts check-in under Tenant A context -> rejected
        authenticate(userOrgB, tenantA, List.of("attendance.self.checkin", "attendance.self.read"));
        assertThrows(AttendanceNotFoundException.class, () -> {
            attendanceService.checkInCore();
        });

        // User from Tenant B under Tenant B context cannot view Employee from Tenant A
        authenticate(userOrgB, tenantB, List.of("attendance.self.read", "attendance.team.read"));
        LocalDate today = LocalDate.now(clock);
        assertThrows(AttendanceNotFoundException.class, () -> {
            attendanceService.getAttendanceDaySummary(employee1OrgA.getId(), today);
        });
    }
}
