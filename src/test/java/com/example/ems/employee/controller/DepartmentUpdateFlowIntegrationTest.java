package com.example.ems.employee.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.dto.DepartmentUpdateRequest;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.DepartmentAuditLog;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.DepartmentAuditLogRepository;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
public class DepartmentUpdateFlowIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private DepartmentController departmentController;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentAuditLogRepository departmentAuditLogRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "deptadmin@company.com";
    private User adminUser;
    private Department testDept;
    private Employee headEmployee;
    private String jwtToken;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(departmentController)
                .build();

        cleanup();

        // 1. Seed Role and Admin User
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("SUPER_ADMIN");
                    r.setDescription("Super Admin Role");
                    return roleRepository.save(r);
                });

        adminUser = userRepository.findByWorkEmail(ADMIN_EMAIL).orElse(null);
        if (adminUser == null) {
            adminUser = new User();
            adminUser.setWorkEmail(ADMIN_EMAIL);
            adminUser.setFullName("Super Admin User");
            adminUser.setPassword(passwordEncoder.encode("SecurePass123!"));
            adminUser.setStatus("ACTIVE");
            adminUser.setRole(superAdminRole);
            adminUser = userRepository.save(adminUser);
            adminUser.setUserId("USR_DEPT_FLOW");
            adminUser = userRepository.save(adminUser);
        }

        jwtToken = "Bearer " + jwtService.generateAccessToken(
                adminUser.getUserId(),
                adminUser.getWorkEmail(),
                adminUser.getRole().getName()
        );

        // 2. Seed Employee for department head
        headEmployee = employeeRepository.findByEmployeeId("EMP_DEPT_FLOW").orElse(null);
        if (headEmployee == null) {
            headEmployee = new Employee();
            headEmployee.setEmployeeId("EMP_DEPT_FLOW");
            headEmployee.setFirstName("Sarah");
            headEmployee.setLastName("Connor");
            headEmployee.setEmail("sarah.connor.flow@example.com");
            headEmployee = employeeRepository.save(headEmployee);
        }

        // 3. Seed initial Department
        testDept = new Department();
        testDept.setName("Sales Flow Test");
        testDept.setCode("SFT");
        testDept.setDescription("Outbound sales department.");
        testDept = departmentRepository.save(testDept);
    }

    @AfterEach
    public void tearDown() {
        cleanup();
    }

    private void cleanup() {
        if (testDept != null && testDept.getId() != null) {
            departmentAuditLogRepository.findAll().stream()
                    .filter(log -> log.getDepartmentId().equals(testDept.getId()))
                    .forEach(departmentAuditLogRepository::delete);
            departmentRepository.findById(testDept.getId()).ifPresent(departmentRepository::delete);
        }
        if (headEmployee != null && headEmployee.getId() != null) {
            employeeRepository.findById(headEmployee.getId()).ifPresent(employeeRepository::delete);
        }
        Optional<User> adminOpt = userRepository.findByWorkEmail(ADMIN_EMAIL);
        adminOpt.ifPresent(userRepository::delete);
    }

    @Test
    public void testDepartmentUpdateFlow() throws Exception {
        // 1. Send Update Request (PUT /api/v1/departments/{id})
        DepartmentUpdateRequest request = new DepartmentUpdateRequest();
        request.setName("Sales & Support");
        request.setCode("SLS");
        request.setHeadId("EMP_DEPT_FLOW"); // Sarah Connor
        request.setDescription("Customer relations and outbound sales.");
        request.setParentDepartmentId("None");
        request.setTeams(List.of());

        mockMvc.perform(put("/api/v1/departments/" + testDept.getId())
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Sales & Support"))
                .andExpect(jsonPath("$.data.headName").value("Sarah Connor"));

        // 2. Verify audit logs are generated in DB
        List<DepartmentAuditLog> logs = departmentAuditLogRepository.findByDepartmentId(testDept.getId());
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(log -> "Department Head".equals(log.getField()) && "Sarah Connor".equals(log.getNewValue())));

        // 3. Retrieve Change History (GET /api/v1/departments/{id}/history)
        mockMvc.perform(get("/api/v1/departments/" + testDept.getId() + "/history")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].field").value("name"))
                .andExpect(jsonPath("$.data[0].newValue").value("Sales & Support"));
    }
}
