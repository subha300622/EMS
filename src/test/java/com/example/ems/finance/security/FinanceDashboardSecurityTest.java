package com.example.ems.finance.security;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.finance.controller.FinanceDashboardController;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.ems.auth.entity.UserSession;
import com.example.ems.auth.service.DatabaseSessionStore;
import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FinanceDashboardSecurityTest {

    private MockMvc mockMvc;

    @Autowired
    private FinanceDashboardController financeDashboardController;

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

    private Organization orgA;
    private Organization orgB;
    private Employee employeeA;
    private Employee employeeB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(
                authenticationManager, authenticationEntryPoint, environment, jwtService, userRepository
        );

        mockMvc = MockMvcBuilders.standaloneSetup(financeDashboardController)
                .addFilters(jwtFilter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE");
                    return roleRepository.save(r);
                });

        // 1. Create Org A and Employee A
        orgA = new Organization();
        orgA.setName("Org Alpha Finance " + System.currentTimeMillis());
        orgA.setOrganizationCode("ORGA-" + System.currentTimeMillis());
        orgA.setStatus(OrganizationStatus.ACTIVE);
        orgA = organizationRepository.save(orgA);

        employeeA = new Employee();
        employeeA.setFullName("Alpha User");
        employeeA.setEmail("emp.alpha@company.com");
        employeeA.setEmployeeId("EMP-ALPHA-" + System.currentTimeMillis());
        employeeA.setDepartment("Finance");
        employeeA.setDesignation("Finance Analyst");
        employeeA.setOrganization(orgA);
        employeeA = employeeRepository.save(employeeA);

        User userA = new User();
        userA.setUserId(employeeA.getEmployeeId());
        userA.setWorkEmail("emp.alpha@company.com");
        userA.setFullName("Alpha User");
        userA.setRole(employeeRole);
        userA.setOrganizationId(orgA.getId());
        userA.setStatus("ACTIVE");
        userA = userRepository.save(userA);

        String sessionIdA = "session-alpha-" + System.currentTimeMillis();
        UserSession sessionA = new UserSession(
                sessionIdA,
                employeeA.getEmployeeId(),
                userA.getWorkEmail(),
                "Finance-Test-Agent",
                "127.0.0.1",
                "refresh-token-alpha",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                false,
                1,
                1L,
                "ACTIVE"
        );
        databaseSessionStore.save(sessionA);

        tokenA = jwtService.generateAccessToken(
                employeeA.getEmployeeId(), userA.getWorkEmail(), "EMPLOYEE", orgA.getId(), sessionIdA, 1, 1L
        );

        // 2. Create Org B and Employee B
        orgB = new Organization();
        orgB.setName("Org Beta Finance " + (System.currentTimeMillis() + 1));
        orgB.setOrganizationCode("ORGB-" + (System.currentTimeMillis() + 1));
        orgB.setStatus(OrganizationStatus.ACTIVE);
        orgB = organizationRepository.save(orgB);

        employeeB = new Employee();
        employeeB.setFullName("Beta User");
        employeeB.setEmail("emp.beta@company.com");
        employeeB.setEmployeeId("EMP-BETA-" + System.currentTimeMillis());
        employeeB.setDepartment("Finance");
        employeeB.setDesignation("Finance Lead");
        employeeB.setOrganization(orgB);
        employeeB = employeeRepository.save(employeeB);

        User userB = new User();
        userB.setUserId(employeeB.getEmployeeId());
        userB.setWorkEmail("emp.beta@company.com");
        userB.setFullName("Beta User");
        userB.setRole(employeeRole);
        userB.setOrganizationId(orgB.getId());
        userB.setStatus("ACTIVE");
        userB = userRepository.save(userB);

        String sessionIdB = "session-beta-" + System.currentTimeMillis();
        UserSession sessionB = new UserSession(
                sessionIdB,
                employeeB.getEmployeeId(),
                userB.getWorkEmail(),
                "Finance-Test-Agent",
                "127.0.0.1",
                "refresh-token-beta",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                false,
                1,
                1L,
                "ACTIVE"
        );
        databaseSessionStore.save(sessionB);

        tokenB = jwtService.generateAccessToken(
                employeeB.getEmployeeId(), userB.getWorkEmail(), "EMPLOYEE", orgB.getId(), sessionIdB, 1, 1L
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void testGetDashboard_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/finance/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetDashboard_Authenticated_Returns200WithScopedData() throws Exception {
        mockMvc.perform(get("/api/v1/finance/dashboard")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendance").exists())
                .andExpect(jsonPath("$.leaveBalance").exists())
                .andExpect(jsonPath("$.ctc").exists())
                .andExpect(jsonPath("$.rating").exists())
                .andExpect(jsonPath("$.pendingActions").isArray())
                .andExpect(jsonPath("$.todaySchedule").exists())
                .andExpect(jsonPath("$.financeTeam").isArray());
    }

    @Test
    void testGetDashboard_IdorProtection_EmployeeParameterIgnored() throws Exception {
        // Attempting to supply employeeId of Employee B should NOT switch to Employee B
        // Request uses tokenA (Alpha User) and attempts ?employeeId=EmployeeB.id
        mockMvc.perform(get("/api/v1/finance/dashboard")
                        .param("employeeId", String.valueOf(employeeB.getId()))
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Verify returned finance team belongs to Org A, not Org B
                .andExpect(jsonPath("$.financeTeam[*].name", hasItem("Alpha User")))
                .andExpect(jsonPath("$.financeTeam[*].name", not(hasItem("Beta User"))));
    }

    @Test
    void testGetDashboard_TenantIsolation_SeparateTenantsDoNotLeakData() throws Exception {
        // Request as Employee B should only see Org B's team members
        mockMvc.perform(get("/api/v1/finance/dashboard")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financeTeam[*].name", hasItem("Beta User")))
                .andExpect(jsonPath("$.financeTeam[*].name", not(hasItem("Alpha User"))));
    }
}
