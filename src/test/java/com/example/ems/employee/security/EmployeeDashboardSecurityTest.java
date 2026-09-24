package com.example.ems.employee.security;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.entity.UserSession;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.DatabaseSessionStore;
import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.controller.EmployeeDashboardController;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.repository.LeaveTypeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.JwtAuthenticationFilter;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EmployeeDashboardSecurityTest {

    private MockMvc mockMvc;

    @Autowired
    private EmployeeDashboardController employeeDashboardController;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private Environment environment;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseSessionStore databaseSessionStore;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private com.example.ems.auth.repository.PermissionRepository permissionRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    private com.example.ems.auth.entity.Permission getOrCreatePermission(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> {
                    com.example.ems.auth.entity.Permission p = new com.example.ems.auth.entity.Permission();
                    p.setName(name);
                    p.setDescription(name);
                    p.setActive(true);
                    return permissionRepository.save(p);
                });
    }

    private Organization org;
    private Employee empA;
    private Employee empB;
    private Leave leaveB;
    private String tokenEmpA;
    private String tokenNoPerm;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(
                authenticationManager, authenticationEntryPoint, environment, jwtService, userRepository
        );

        mockMvc = MockMvcBuilders.standaloneSetup(employeeDashboardController)
                .addFilters(jwtFilter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        var permDashboard = getOrCreatePermission("employee.dashboard.read");
        var permAttendance = getOrCreatePermission("employee.attendance.read");
        var permLeave = getOrCreatePermission("employee.leave.read");
        var permComp = getOrCreatePermission("employee.compensation.read");
        var permPerf = getOrCreatePermission("employee.performance.read");
        var permDoc = getOrCreatePermission("employee.document.read");
        var permTraining = getOrCreatePermission("employee.training.read");
        var permAction = getOrCreatePermission("employee.action-center.read");

        Role empRole = roleRepository.findByName("EMPLOYEE_PORTAL_TEST_ROLE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE_PORTAL_TEST_ROLE");
                    r.setPermissions(new java.util.HashSet<>(Set.of(
                            permDashboard, permAttendance, permLeave, permComp,
                            permPerf, permDoc, permTraining, permAction
                    )));
                    return roleRepository.save(r);
                });

        Role noPermRole = roleRepository.findByName("NO_PORTAL_PERM_ROLE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("NO_PORTAL_PERM_ROLE");
                    r.setPermissions(new java.util.HashSet<>());
                    return roleRepository.save(r);
                });

        org = new Organization();
        org.setName("Portal Org " + System.currentTimeMillis());
        org.setOrganizationCode("ORG-PORTAL-" + System.currentTimeMillis());
        org.setStatus(OrganizationStatus.ACTIVE);
        org = organizationRepository.save(org);

        empA = new Employee();
        empA.setFullName("Sarah Jenkins");
        empA.setEmail("sarah.jenkins@portal.com");
        empA.setEmployeeId("EMP-SJ-" + System.currentTimeMillis());
        empA.setOrganization(org);
        empA = employeeRepository.save(empA);

        empB = new Employee();
        empB.setFullName("Bob Smith");
        empB.setEmail("bob.smith@portal.com");
        empB.setEmployeeId("EMP-BS-" + System.currentTimeMillis());
        empB.setOrganization(org);
        empB = employeeRepository.save(empB);

        LeaveType lt = leaveTypeRepository.findByName("Casual Leave")
                .orElseGet(() -> {
                    LeaveType t = new LeaveType();
                    t.setName("Casual Leave " + System.currentTimeMillis());
                    t.setOrganization(org);
                    return leaveTypeRepository.save(t);
                });

        leaveB = new Leave();
        leaveB.setEmployee(empB);
        leaveB.setLeaveType(lt);
        leaveB.setOrganization(org);
        leaveB.setStartDate(LocalDate.now().plusDays(2));
        leaveB.setEndDate(LocalDate.now().plusDays(3));
        leaveB.setStatus("PENDING");
        leaveB = leaveRepository.save(leaveB);

        // User for Employee A
        User userA = new User();
        userA.setUserId(empA.getEmployeeId());
        userA.setWorkEmail("sarah.jenkins@portal.com");
        userA.setFullName("Sarah Jenkins");
        userA.setRole(empRole);
        userA.setOrganizationId(org.getId());
        userA.setStatus("ACTIVE");
        userA = userRepository.save(userA);

        String sessionIdA = "session-a-" + System.currentTimeMillis();
        UserSession sessionA = new UserSession(
                sessionIdA, empA.getEmployeeId(), userA.getWorkEmail(),
                "Employee-Agent", "127.0.0.1", "refresh-token-a",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                false, 1, 1L, "ACTIVE"
        );
        databaseSessionStore.save(sessionA);

        tokenEmpA = jwtService.generateAccessToken(
                empA.getEmployeeId(), userA.getWorkEmail(), empRole.getName(),
                org.getId(), sessionIdA, 1, 1L
        );

        // User with no permissions
        User userNoPerm = new User();
        userNoPerm.setUserId("EMP-NP-" + System.currentTimeMillis());
        userNoPerm.setWorkEmail("noperm@portal.com");
        userNoPerm.setFullName("No Perm User");
        userNoPerm.setRole(noPermRole);
        userNoPerm.setOrganizationId(org.getId());
        userNoPerm.setStatus("ACTIVE");
        userNoPerm = userRepository.save(userNoPerm);

        String sessionIdNP = "session-np-" + System.currentTimeMillis();
        UserSession sessionNP = new UserSession(
                sessionIdNP, userNoPerm.getUserId(), userNoPerm.getWorkEmail(),
                "Employee-Agent", "127.0.0.1", "refresh-token-np",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                false, 1, 1L, "ACTIVE"
        );
        databaseSessionStore.save(sessionNP);

        tokenNoPerm = jwtService.generateAccessToken(
                userNoPerm.getUserId(), userNoPerm.getWorkEmail(), noPermRole.getName(),
                org.getId(), sessionIdNP, 1, 1L
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void unauthenticatedAccessToDashboard_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/employee/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedEmployeeWithoutPermission_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/employee/dashboard")
                        .header("Authorization", "Bearer " + tokenNoPerm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedEmployeeWithPermission_ReceivesDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/employee/dashboard")
                        .header("Authorization", "Bearer " + tokenEmpA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.summary.attendance").exists())
                .andExpect(jsonPath("$.summary.leaveBalance").exists())
                .andExpect(jsonPath("$.summary.compensation").exists())
                .andExpect(jsonPath("$.summary.performance").exists())
                .andExpect(jsonPath("$.pendingActions").exists());
    }

    @Test
    void antiIdor_QueryParamEmployeeIdIsIgnored() throws Exception {
        // Manipulated query param ?employeeId=99999 must not switch identity or cause error
        mockMvc.perform(get("/api/v1/employee/dashboard?employeeId=99999")
                        .header("Authorization", "Bearer " + tokenEmpA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").exists());
    }

    @Test
    void attendanceSummary_AccessControl() throws Exception {
        mockMvc.perform(get("/api/v1/employee/attendance/summary")
                        .header("Authorization", "Bearer " + tokenEmpA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").exists())
                .andExpect(jsonPath("$.workingDays").exists())
                .andExpect(jsonPath("$.attendancePercentage").exists());
    }

    @Test
    void leaveBalance_AccessControl() throws Exception {
        mockMvc.perform(get("/api/v1/employee/leave-balance")
                        .header("Authorization", "Bearer " + tokenEmpA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAvailable").exists())
                .andExpect(jsonPath("$.leaveTypes").isArray());
    }

    @Test
    void compensationCurrent_AccessControl() throws Exception {
        mockMvc.perform(get("/api/v1/employee/compensation/current")
                        .header("Authorization", "Bearer " + tokenEmpA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentCtc").exists())
                .andExpect(jsonPath("$.period").value("ANNUAL"));
    }

    @Test
    void actionCenter_AccessControl() throws Exception {
        mockMvc.perform(get("/api/v1/employee/action-center")
                        .header("Authorization", "Bearer " + tokenEmpA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void antiIdor_LeaveDetail_ForbiddenWhenNotOwner() throws Exception {
        // Employee A attempts to read Employee B's leave request by id
        mockMvc.perform(get("/api/v1/employee/leave/" + leaveB.getId())
                        .header("Authorization", "Bearer " + tokenEmpA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
