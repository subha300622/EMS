package com.example.ems.auth.controller;

import com.example.ems.auth.dto.AdminUserDtos;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.util.*;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Designation;
import com.example.ems.employee.entity.EmploymentType;
import com.example.ems.employee.entity.JobLevel;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.DesignationRepository;
import com.example.ems.employee.repository.EmploymentTypeRepository;
import com.example.ems.employee.repository.JobLevelRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class MultiTenantIsolationIntegrationTest {

        private MockMvc adminUserMvc;

        @Autowired
        private AdminUserController adminUserController;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private DepartmentRepository departmentRepository;

        @Autowired
        private DesignationRepository designationRepository;

        @Autowired
        private JobLevelRepository jobLevelRepository;

        @Autowired
        private EmploymentTypeRepository employmentTypeRepository;

        @Autowired
        private OrganizationRepository organizationRepository;

        @Autowired
        private PermissionRepository permissionRepository;

        @Autowired
        private JwtService jwtService;

        private final ObjectMapper objectMapper = new ObjectMapper();

        private Organization orgA;
        private Organization orgB;

        private User adminA;
        private String tokenA;

        private Role roleA;
        private Role roleB;

        private Department deptA;
        private Department deptB;

        private Designation desA;
        private Designation desB;

        private JobLevel jlA;
        private JobLevel jlB;

        private EmploymentType etA;
        private EmploymentType etB;

        @BeforeEach
        public void setUp() {
                adminUserMvc = MockMvcBuilders.standaloneSetup(adminUserController).build();

                // 1. Create Organization A
                orgA = new Organization();
                orgA.setName("Organization A");
                orgA.setOrganizationCode("ORGA");
                orgA.setStatus(OrganizationStatus.ACTIVE);
                orgA = organizationRepository.save(orgA);

                // 2. Create Organization B
                orgB = new Organization();
                orgB.setName("Organization B");
                orgB.setOrganizationCode("ORGB");
                orgB.setStatus(OrganizationStatus.ACTIVE);
                orgB = organizationRepository.save(orgB);

                // 3. Create Admin Role for A with user.manage/user.create permission
                Role adminRoleA = new Role();
                adminRoleA.setName("ADMIN");
                adminRoleA.setOrganization(orgA);
                adminRoleA.setPlatformTemplate(false);
                adminRoleA.setStatus("ACTIVE");

                Permission userManage = permissionRepository.findByName("user.manage")
                                .orElseGet(() -> {
                                        Permission p = new Permission();
                                        p.setName("user.manage");
                                        p.setDescription("Manage Users");
                                        return permissionRepository.save(p);
                                });
                adminRoleA.setPermissions(new java.util.HashSet<>(List.of(userManage)));
                adminRoleA = roleRepository.save(adminRoleA);

                // 4. Create Admin A User
                adminA = new User();
                adminA.setFirstName("OrgA");
                adminA.setLastName("Admin");
                adminA.setFullName("OrgA Admin");
                adminA.setWorkEmail("admina@orga.com");
                adminA.setUserId("EMP_ADMIN_A");
                adminA.setOrganizationId(orgA.getId());
                adminA.setRoleId(adminRoleA.getId());
                adminA.setRole(adminRoleA);
                adminA.setStatus("ACTIVE");
                adminA = userRepository.save(adminA);

                tokenA = jwtService.generateAccessToken(adminA.getUserId(), adminA.getWorkEmail(), "ADMIN");

                // 5. Seed other aggregates for Org A
                roleA = new Role();
                roleA.setName("Employee");
                roleA.setOrganization(orgA);
                roleA.setStatus("ACTIVE");
                roleA = roleRepository.save(roleA);

                deptA = new Department();
                deptA.setName("Engineering MT A");
                deptA.setCode("ENG_MTA");
                deptA.setOrganization(orgA);
                deptA.setStatus("ACTIVE");
                deptA = departmentRepository.save(deptA);

                desA = new Designation();
                desA.setDesignation("Software Engineer MT A");
                desA.setOrganization(orgA);
                desA.setStatus("ACTIVE");
                desA = designationRepository.save(desA);

                jlA = new JobLevel();
                jlA.setJobLevel("L1 MT A");
                jlA.setDesignation(desA);
                jlA.setOrganization(orgA);
                jlA.setStatus("ACTIVE");
                jlA = jobLevelRepository.save(jlA);

                etA = new EmploymentType();
                etA.setEmploymentType("Full-Time MT A");
                etA.setJobLevel(jlA);
                etA.setOrganization(orgA);
                etA.setStatus("ACTIVE");
                etA = employmentTypeRepository.save(etA);

                // 6. Seed aggregates for Org B (to test IDOR tenant boundary isolation)
                roleB = new Role();
                roleB.setName("Employee");
                roleB.setOrganization(orgB);
                roleB.setStatus("ACTIVE");
                roleB = roleRepository.save(roleB);

                deptB = new Department();
                deptB.setName("Sales MT B");
                deptB.setCode("SAL_MTB");
                deptB.setOrganization(orgB);
                deptB.setStatus("ACTIVE");
                deptB = departmentRepository.save(deptB);

                desB = new Designation();
                desB.setDesignation("Sales Executive MT B");
                desB.setOrganization(orgB);
                desB.setStatus("ACTIVE");
                desB = designationRepository.save(desB);

                jlB = new JobLevel();
                jlB.setJobLevel("L2 MT B");
                jlB.setDesignation(desB);
                jlB.setOrganization(orgB);
                jlB.setStatus("ACTIVE");
                jlB = jobLevelRepository.save(jlB);

                etB = new EmploymentType();
                etB.setEmploymentType("Contract MT B");
                etB.setJobLevel(jlB);
                etB.setOrganization(orgB);
                etB.setStatus("ACTIVE");
                etB = employmentTypeRepository.save(etB);

                // Set TenantContext for Org A admin execution context
                TenantContext.setCurrentTenant(orgA.getId());
        }

        @AfterEach
        public void tearDown() {
                TenantContext.clear();
        }

        private AdminUserDtos.CreateAdminUserRequest.CreateAdminUserRequestBuilder createRequestBuilder() {
                return AdminUserDtos.CreateAdminUserRequest.builder()
                                .firstName("John")
                                .lastName("Doe")
                                .email("johndoe@orga.com")
                                .password("Password123!")
                                .confirmPassword("Password123!")
                                .roleId(RoleIdResolver.formatId(roleA.getId()))
                                .departmentId(DepartmentIdResolver.formatId(deptA.getId()))
                                .designationId(DesignationIdResolver.formatId(desA.getId()))
                                .jobLevelId(JobLevelIdResolver.formatId(jlA.getId()))
                                .employmentTypeId(EmploymentTypeIdResolver.formatId(etA.getId()))
                                .workInformation(AdminUserDtos.WorkInformation.builder()
                                                .locationId("LOC-001")
                                                .employeeStatus("ACTIVE")
                                                .sourceOfHire("REFERRAL")
                                                .dateOfJoining("2026-08-18")
                                                .totalExperienceYears(3)
                                                .build())
                                .personalInformation(AdminUserDtos.PersonalInformation.builder()
                                                .dateOfBirth("1998-01-01")
                                                .gender("MALE")
                                                .maritalStatus("SINGLE")
                                                .nationality("INDIAN")
                                                .build())
                                .identityInformation(AdminUserDtos.IdentityInformation.builder()
                                                .build())
                                .notificationPreferences(AdminUserDtos.NotificationPreferences.builder()
                                                .sendInviteEmail(true)
                                                .notifyHr(true)
                                                .build());
        }

        @Test
        public void testCreateUser_Success() throws Exception {
                AdminUserDtos.CreateAdminUserRequest request = createRequestBuilder().build();

                adminUserMvc.perform(post("/api/v1/admin/users")
                                .header("Authorization", "Bearer " + tokenA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.firstName").value("John"))
                                .andExpect(jsonPath("$.data.email").value("johndoe@orga.com"))
                                .andExpect(jsonPath("$.data.roleId").value(RoleIdResolver.formatId(roleA.getId())));
        }

        @Test
        public void testCreateUser_IDORProtection_RoleFromAnotherTenant() throws Exception {
                // Use Org B RoleId (roleB) inside Org A request context
                AdminUserDtos.CreateAdminUserRequest request = createRequestBuilder()
                                .roleId(RoleIdResolver.formatId(roleB.getId()))
                                .build();

                adminUserMvc.perform(post("/api/v1/admin/users")
                                .header("Authorization", "Bearer " + tokenA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.message").value(
                                                "Role not found with ID: " + RoleIdResolver.formatId(roleB.getId())));
        }

        @Test
        public void testCreateUser_IDORProtection_DepartmentFromAnotherTenant() throws Exception {
                // Use Org B DepartmentId inside Org A request context
                AdminUserDtos.CreateAdminUserRequest request = createRequestBuilder()
                                .departmentId(DepartmentIdResolver.formatId(deptB.getId()))
                                .build();

                adminUserMvc.perform(post("/api/v1/admin/users")
                                .header("Authorization", "Bearer " + tokenA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.message").value("Department not found with ID: "
                                                + DepartmentIdResolver.formatId(deptB.getId())));
        }

        @Test
        public void testCreateUser_ActiveStatusError() throws Exception {
                // Mark Department A inactive
                deptA.setStatus("INACTIVE");
                departmentRepository.save(deptA);

                AdminUserDtos.CreateAdminUserRequest request = createRequestBuilder().build();

                adminUserMvc.perform(post("/api/v1/admin/users")
                                .header("Authorization", "Bearer " + tokenA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.message")
                                                .value("Department is not active"));
        }

        @Test
        public void testCreateUser_CircularReportingManagerLoop() throws Exception {
                // Create an existing employee user to act as manager
                User manager1 = new User();
                manager1.setFirstName("Manager");
                manager1.setLastName("One");
                manager1.setFullName("Manager One");
                manager1.setWorkEmail("manager1@orga.com");
                manager1.setUserId("EMP_MGR_1");
                manager1.setOrganizationId(orgA.getId());
                manager1.setRoleId(roleA.getId());
                manager1.setStatus("ACTIVE");
                manager1 = userRepository.save(manager1);

                // Assign manager1 reporting manager to adminA
                manager1.setReportingManagerId(adminA.getId());
                userRepository.save(manager1);

                // Try to create a user that reports to manager1, where manager1 reports to self
                // (Direct cycle)
                AdminUserDtos.CreateAdminUserRequest requestSelf = createRequestBuilder()
                                .reportingManagerId(UserIdResolver.formatId(manager1.getId()))
                                .build();

                manager1.setReportingManagerId(manager1.getId());
                userRepository.save(manager1);

                adminUserMvc.perform(post("/api/v1/admin/users")
                                .header("Authorization", "Bearer " + tokenA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestSelf)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.message")
                                                .value("Circular reporting manager loop detected"));
        }
}
