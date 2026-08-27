package com.example.ems.employee.controller;

import com.example.ems.auth.controller.AuthController;
import com.example.ems.auth.dto.LoginRequest;
import com.example.ems.auth.dto.SignupRequest;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.EmailVerificationRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.dto.DepartmentCreateRequest;
import com.example.ems.employee.dto.TeamDtos;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.TeamRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class EndToEndOrgSignupLoginDeptTeamIntegrationTest {

        private MockMvc mockMvcAuth;
        private MockMvc mockMvcDept;
        private MockMvc mockMvcTeam;

        @Autowired
        private AuthController authController;

        @Autowired
        private DepartmentController departmentController;

        @Autowired
        private TeamController teamController;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private EmployeeRepository employeeRepository;

        @Autowired
        private OrganizationRepository organizationRepository;

        @Autowired
        private DepartmentRepository departmentRepository;

        @Autowired
        private TeamRepository teamRepository;

        @Autowired
        private EmailVerificationRepository verificationRepository;

        private final ObjectMapper objectMapper = new ObjectMapper();

        private static final String TEST_EMAIL = "admin@acmeglobal.com";
        private static final String TEST_ORG_NAME = "Acme Global Inc";

        @BeforeEach
        public void setUp() {
                mockMvcAuth = MockMvcBuilders.standaloneSetup(authController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();

                mockMvcDept = MockMvcBuilders.standaloneSetup(departmentController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();

                mockMvcTeam = MockMvcBuilders.standaloneSetup(teamController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();

                // Clean up test organization and data if existing
                departmentRepository.findAll().stream()
                                .filter(d -> "SOFT-ENG".equalsIgnoreCase(d.getCode())
                                                || "Software Engineering".equalsIgnoreCase(d.getName()))
                                .forEach(departmentRepository::delete);

                Optional<User> uOpt = userRepository.findByWorkEmail(TEST_EMAIL);
                uOpt.ifPresent(u -> {
                        verificationRepository.findByUserId(u.getId()).ifPresent(verificationRepository::delete);
                        employeeRepository.findByEmail(u.getWorkEmail()).ifPresent(employeeRepository::delete);
                        userRepository.delete(u);
                });

                organizationRepository.findAll().stream()
                                .filter(o -> TEST_ORG_NAME.equalsIgnoreCase(o.getName()))
                                .forEach(o -> {
                                        teamRepository.findByDepartmentIdAndOrganizationIdAndDeletedFalse(null,
                                                        o.getId())
                                                        .forEach(teamRepository::delete);
                                        departmentRepository.findByOrganizationId(o.getId()).forEach(dept -> {
                                                teamRepository.findByDepartmentIdAndOrganizationIdAndDeletedFalse(
                                                                dept.getId(), o.getId())
                                                                .forEach(teamRepository::delete);
                                                departmentRepository.delete(dept);
                                        });
                                        employeeRepository.findAll().stream()
                                                        .filter(e -> o.getId()
                                                                        .equals(e.getOrganization() != null
                                                                                        ? e.getOrganization().getId()
                                                                                        : null))
                                                        .forEach(employeeRepository::delete);
                                        userRepository.findAll().stream()
                                                        .filter(u -> o.getId()
                                                                        .equals(u.getOrganization() != null
                                                                                        ? u.getOrganization().getId()
                                                                                        : null))
                                                        .forEach(u -> {
                                                                verificationRepository.findByUserId(u.getId()).ifPresent(verificationRepository::delete);
                                                                userRepository.delete(u);
                                                        });
                                        roleRepository.findAll().stream()
                                                        .filter(r -> o.getId()
                                                                        .equals(r.getOrganization() != null
                                                                                        ? r.getOrganization().getId()
                                                                                        : null))
                                                        .forEach(roleRepository::delete);
                                        organizationRepository.delete(o);
                                });
        }

        @Test
        public void testEndToEndSignupLoginAddDepartmentAndTeam() throws Exception {
                // =========================================================================
                // 1. REGISTER ORGANIZATION (SaaS Signup)
                // =========================================================================
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setFullName("Acme Corp Admin");
                signupRequest.setEmail(TEST_EMAIL);
                signupRequest.setCountryCode("+1");
                signupRequest.setPhone("+1 555 123 4567");
                signupRequest.setPassword("SecurePass!2026");
                signupRequest.setOrgName(TEST_ORG_NAME);
                signupRequest.setIndustry("Technology");
                signupRequest.setCountry("USA");
                signupRequest.setState("California");
                signupRequest.setCity("San Francisco");
                signupRequest.setAddress("100 Innovation Way, Suite 400");
                signupRequest.setCompanySize("50-100");
                signupRequest.setPlan("ENTERPRISE");
                signupRequest.setBillingCycle("ANNUAL");
                signupRequest.setGstNumber("07AAAAA1111A1Z1");

                MvcResult signupResult = mockMvcAuth.perform(post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.code").value("SIGNUP_SUCCESS"))
                                .andExpect(jsonPath("$.data.organizationId").exists())
                                .andExpect(jsonPath("$.data.userId").exists())
                                .andExpect(jsonPath("$.data.emailVerificationRequired").value(false))
                                .andReturn();

                String signupResponseStr = signupResult.getResponse().getContentAsString();
                String orgCode = objectMapper.readTree(signupResponseStr).path("data").path("organizationId").asText();
                String userId = objectMapper.readTree(signupResponseStr).path("data").path("userId").asText();

                assertNotNull(orgCode);
                assertNotNull(userId);

                // Verify that account & org are directly ACTIVE in database
                User activeUser = userRepository.findByUserId(userId).orElseThrow();
                assertEquals("ACTIVE", activeUser.getStatus());

                Organization activeOrg = organizationRepository.findByOrganizationCode(orgCode).orElseThrow();
                assertEquals("ACTIVE", activeOrg.getStatus().name());

                // =========================================================================
                // 3. LOGIN AS ORGANIZATION ADMIN
                // =========================================================================
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail(TEST_EMAIL);
                loginRequest.setPassword("SecurePass!2026");

                MvcResult loginResult = mockMvcAuth.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andReturn();

                System.out.println("LOGIN RESPONSE STATUS: " + loginResult.getResponse().getStatus());
                System.out.println("LOGIN RESPONSE BODY: " + loginResult.getResponse().getContentAsString());
                assertEquals(200, loginResult.getResponse().getStatus());

                String loginResponseStr = loginResult.getResponse().getContentAsString();
                String accessToken = objectMapper.readTree(loginResponseStr).path("data").path("tokens")
                                .path("accessToken").asText();
                assertNotNull(accessToken);

                // =========================================================================
                // 4. ADD DEPARTMENT
                // =========================================================================
                DepartmentCreateRequest deptReq = new DepartmentCreateRequest();
                deptReq.setName("Software Engineering");
                deptReq.setCode("SOFT-ENG");
                deptReq.setDescription("Core Software Engineering Department");

                MvcResult deptResult = mockMvcDept.perform(post("/api/v1/departments")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(deptReq)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").exists())
                                .andReturn();

                String deptResponseStr = deptResult.getResponse().getContentAsString();
                Long departmentId = objectMapper.readTree(deptResponseStr).path("data").path("id").asLong();
                assertNotNull(departmentId);

                // =========================================================================
                // 5. ADD TEAM (Associated with Department)
                // =========================================================================
                TeamDtos.TeamCreateRequest teamReq = new TeamDtos.TeamCreateRequest();
                teamReq.setTeamName("Backend Platform Team");
                teamReq.setTeamCode("BACKEND-PLATFORM");
                teamReq.setDescription("Core Backend Microservices & API Development Team");
                teamReq.setDepartmentId(departmentId);

                MvcResult teamResult = mockMvcTeam.perform(post("/api/v1/teams")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(teamReq)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.teamId").exists())
                                .andExpect(jsonPath("$.data.teamName").value("Backend Platform Team"))
                                .andExpect(jsonPath("$.data.teamCode").value("BACKEND-PLATFORM"))
                                .andExpect(jsonPath("$.data.department.departmentId").value(departmentId))
                                .andReturn();

                String teamResponseStr = teamResult.getResponse().getContentAsString();
                Long teamId = objectMapper.readTree(teamResponseStr).path("data").path("teamId").asLong();
                assertNotNull(teamId);

                // =========================================================================
                // 6. VERIFY TEAM & DEPARTMENT VIA GET APIS
                // =========================================================================
                // 6a. GET /api/v1/teams/{teamId}
                mockMvcTeam.perform(get("/api/v1/teams/" + teamId)
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.teamId").value(teamId))
                                .andExpect(jsonPath("$.data.teamName").value("Backend Platform Team"))
                                .andExpect(jsonPath("$.data.department.departmentId").value(departmentId));

                // 6b. GET /api/v1/departments/{departmentId}/teams
                mockMvcTeam.perform(get("/api/v1/departments/" + departmentId + "/teams")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].teamId").value(teamId))
                                .andExpect(jsonPath("$.data[0].teamName").value("Backend Platform Team"));
        }
}
