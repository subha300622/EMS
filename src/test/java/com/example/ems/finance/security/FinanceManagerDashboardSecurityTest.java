package com.example.ems.finance.security;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.entity.UserSession;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.DatabaseSessionStore;
import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.finance.controller.FinanceManagerDashboardController;
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

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FinanceManagerDashboardSecurityTest {

    private MockMvc mockMvc;

    @Autowired
    private FinanceManagerDashboardController financeManagerDashboardController;

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

    private Organization orgA;
    private Employee managerA;
    private Employee reporteeA;
    private Employee employeeOther;
    private String tokenManagerA;
    private String tokenManagerANoPayroll;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(
                authenticationManager, authenticationEntryPoint, environment, jwtService, userRepository
        );

        mockMvc = MockMvcBuilders.standaloneSetup(financeManagerDashboardController)
                .addFilters(jwtFilter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        com.example.ems.auth.entity.Permission teamView = getOrCreatePermission("finance.team.view");
        com.example.ems.auth.entity.Permission payrollView = getOrCreatePermission("finance.payroll.view");
        com.example.ems.auth.entity.Permission salaryView = getOrCreatePermission("finance.salary.view");
        com.example.ems.auth.entity.Permission expenseApprove = getOrCreatePermission("finance.expense.approve");

        Role managerRoleWithPayroll = roleRepository.findByName("FINANCE_MANAGER_ROLE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("FINANCE_MANAGER_ROLE");
                    r.setPermissions(new java.util.HashSet<>(Set.of(teamView, payrollView, salaryView, expenseApprove)));
                    return roleRepository.save(r);
                });

        Role managerRoleNoPayroll = roleRepository.findByName("MANAGER_NO_PAYROLL_ROLE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("MANAGER_NO_PAYROLL_ROLE");
                    r.setPermissions(new java.util.HashSet<>(Set.of(teamView, expenseApprove)));
                    return roleRepository.save(r);
                });

        // 1. Create Org A
        orgA = new Organization();
        orgA.setName("Org Alpha Finance " + System.currentTimeMillis());
        orgA.setOrganizationCode("ORGA-FIN-" + System.currentTimeMillis());
        orgA.setStatus(OrganizationStatus.ACTIVE);
        orgA = organizationRepository.save(orgA);

        // Manager A
        managerA = new Employee();
        managerA.setFullName("Alpha Manager");
        managerA.setEmail("manager.alpha@company.com");
        managerA.setEmployeeId("EMP-MGR-" + System.currentTimeMillis());
        managerA.setDepartment("Finance");
        managerA.setDesignation("Finance Director");
        managerA.setOrganization(orgA);
        managerA = employeeRepository.save(managerA);

        // Reportee A (reports to Manager A)
        reporteeA = new Employee();
        reporteeA.setFullName("Rajan Kumar");
        reporteeA.setEmail("rajan.kumar@company.com");
        reporteeA.setEmployeeId("EMP-REP-" + System.currentTimeMillis());
        reporteeA.setDepartment("Finance");
        reporteeA.setDesignation("Senior Accountant");
        reporteeA.setOrganization(orgA);
        reporteeA.setManager(managerA);
        reporteeA = employeeRepository.save(reporteeA);

        // Other Employee in Org A (reports to someone else, not Manager A)
        employeeOther = new Employee();
        employeeOther.setFullName("Other User");
        employeeOther.setEmail("other.user@company.com");
        employeeOther.setEmployeeId("EMP-OTH-" + System.currentTimeMillis());
        employeeOther.setDepartment("Engineering");
        employeeOther.setDesignation("Software Engineer");
        employeeOther.setOrganization(orgA);
        employeeOther = employeeRepository.save(employeeOther);

        // User for Manager A with full payroll permission
        User userManagerA = new User();
        userManagerA.setUserId(managerA.getEmployeeId());
        userManagerA.setWorkEmail("manager.alpha@company.com");
        userManagerA.setFullName("Alpha Manager");
        userManagerA.setRole(managerRoleWithPayroll);
        userManagerA.setOrganizationId(orgA.getId());
        userManagerA.setStatus("ACTIVE");
        userManagerA = userRepository.save(userManagerA);

        String sessionIdA = "session-mgr-" + System.currentTimeMillis();
        UserSession sessionA = new UserSession(
                sessionIdA, managerA.getEmployeeId(), userManagerA.getWorkEmail(),
                "Finance-Manager-Agent", "127.0.0.1", "refresh-token-mgr",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                false, 1, 1L, "ACTIVE"
        );
        databaseSessionStore.save(sessionA);

        tokenManagerA = jwtService.generateAccessToken(
                managerA.getEmployeeId(), userManagerA.getWorkEmail(), managerRoleWithPayroll.getName(),
                orgA.getId(), sessionIdA, 1, 1L
        );

        // Token for Manager without payroll view permission
        User userManagerNoPayroll = new User();
        userManagerNoPayroll.setUserId(managerA.getEmployeeId() + "-NP");
        userManagerNoPayroll.setWorkEmail("manager.nopayroll@company.com");
        userManagerNoPayroll.setFullName("Manager No Payroll");
        userManagerNoPayroll.setRole(managerRoleNoPayroll);
        userManagerNoPayroll.setOrganizationId(orgA.getId());
        userManagerNoPayroll.setStatus("ACTIVE");
        userManagerNoPayroll = userRepository.save(userManagerNoPayroll);

        Employee managerNoPayrollEmp = new Employee();
        managerNoPayrollEmp.setFullName("Manager No Payroll");
        managerNoPayrollEmp.setEmail("manager.nopayroll@company.com");
        managerNoPayrollEmp.setEmployeeId("EMP-MNP-" + System.currentTimeMillis());
        managerNoPayrollEmp.setDepartment("Finance");
        managerNoPayrollEmp.setDesignation("Finance Lead");
        managerNoPayrollEmp.setOrganization(orgA);
        managerNoPayrollEmp = employeeRepository.save(managerNoPayrollEmp);

        // Link reportee also to this manager for testing scoping
        Employee reporteeNP = new Employee();
        reporteeNP.setFullName("Priya Nair");
        reporteeNP.setEmail("priya.nair@company.com");
        reporteeNP.setEmployeeId("EMP-PN-" + System.currentTimeMillis());
        reporteeNP.setDepartment("Finance");
        reporteeNP.setDesignation("Accountant");
        reporteeNP.setOrganization(orgA);
        reporteeNP.setManager(managerNoPayrollEmp);
        reporteeNP = employeeRepository.save(reporteeNP);

        String sessionIdNP = "session-mnp-" + System.currentTimeMillis();
        UserSession sessionNP = new UserSession(
                sessionIdNP, managerNoPayrollEmp.getEmployeeId(), userManagerNoPayroll.getWorkEmail(),
                "Finance-Manager-Agent", "127.0.0.1", "refresh-token-mnp",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                false, 1, 1L, "ACTIVE"
        );
        databaseSessionStore.save(sessionNP);

        tokenManagerANoPayroll = jwtService.generateAccessToken(
                managerNoPayrollEmp.getEmployeeId(), userManagerNoPayroll.getWorkEmail(), managerRoleNoPayroll.getName(),
                orgA.getId(), sessionIdNP, 1, 1L
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void testGetDashboard_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/finance/manager/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetDashboard_Authenticated_Returns200WithScopedData() throws Exception {
        mockMvc.perform(get("/api/v1/finance/manager/dashboard")
                        .header("Authorization", "Bearer " + tokenManagerA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamSummary").exists())
                .andExpect(jsonPath("$.leaveSummary").exists())
                .andExpect(jsonPath("$.expenseSummary").exists())
                .andExpect(jsonPath("$.payrollSummary").exists())
                .andExpect(jsonPath("$.pendingActions").isArray())
                .andExpect(jsonPath("$.teamMembers").isArray())
                .andExpect(jsonPath("$.teamMembers[*].name", hasItem("Rajan Kumar")))
                .andExpect(jsonPath("$.teamMembers[*].name", not(hasItem("Other User"))));
    }

    @Test
    void testGetDashboard_IdorProtection_EmployeeParameterIgnored() throws Exception {
        // Supplying arbitrary ?employeeId=500 should NOT bypass or alter reporting hierarchy
        mockMvc.perform(get("/api/v1/finance/manager/dashboard")
                        .param("employeeId", "500")
                        .header("Authorization", "Bearer " + tokenManagerA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamMembers[*].name", hasItem("Rajan Kumar")))
                .andExpect(jsonPath("$.teamMembers[*].name", not(hasItem("Other User"))));
    }

    @Test
    void testGetDashboard_PayrollPermissionCheck_OmittedWhenPermissionMissing() throws Exception {
        // Manager without finance.payroll.view or finance.salary.view should not receive payrollSummary
        mockMvc.perform(get("/api/v1/finance/manager/dashboard")
                        .header("Authorization", "Bearer " + tokenManagerANoPayroll)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamSummary").exists())
                .andExpect(jsonPath("$.payrollSummary").doesNotExist());
    }

    @Test
    void testGetTeamMember_TargetOutsideReportingHierarchy_Returns403() throws Exception {
        // Attempting to access an employee that is not managed by Manager A
        mockMvc.perform(get("/api/v1/finance/manager/team/" + employeeOther.getId())
                        .header("Authorization", "Bearer " + tokenManagerA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("not within your reporting hierarchy")));
    }

    @Test
    void testGetTeamMember_AuthorizedDirectReport_Returns200() throws Exception {
        // Accessing direct report Rajan Kumar
        mockMvc.perform(get("/api/v1/finance/manager/team/" + reporteeA.getId())
                        .header("Authorization", "Bearer " + tokenManagerA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(reporteeA.getId()))
                .andExpect(jsonPath("$.name").value("Rajan Kumar"));
    }
}
