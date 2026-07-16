package com.example.ems.auth.controller;

import com.example.ems.auth.dto.SignupRequest;
import com.example.ems.auth.entity.EmailVerification;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.EmailVerificationRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.Tenant;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.repository.TenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class SignupControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmailVerificationRepository verificationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Clean up test data
        Optional<User> uOpt = userRepository.findByWorkEmail("testsignupadmin@company.com");
        uOpt.ifPresent(u -> {
            verificationRepository.findByUserId(u.getId()).ifPresent(verificationRepository::delete);
            employeeRepository.findByEmail(u.getWorkEmail()).ifPresent(employeeRepository::delete);
            userRepository.delete(u);
        });

        organizationRepository.findAll().stream()
                .filter(o -> "Test Registration Org".equalsIgnoreCase(o.getName()))
                .forEach(o -> {
                    // Delete referencing entities
                    employeeRepository.findAll().stream()
                            .filter(e -> o.getId().equals(e.getOrganization() != null ? e.getOrganization().getId() : null))
                            .forEach(employeeRepository::delete);
                    userRepository.findAll().stream()
                            .filter(u -> o.getId().equals(u.getOrganization() != null ? u.getOrganization().getId() : null))
                            .forEach(userRepository::delete);
                    departmentRepository.findByOrganizationId(o.getId()).forEach(departmentRepository::delete);
                    roleRepository.findAll().stream()
                            .filter(r -> o.getId().equals(r.getOrganization() != null ? r.getOrganization().getId() : null))
                            .forEach(roleRepository::delete);
                    organizationRepository.delete(o);
                });
    }

    @Test
    public void testSignupValidationFailures() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setFullName("T"); // Invalid length
        request.setEmail("invalid-email");
        request.setPassword("12345"); // Too short
        request.setOrgName("Test");
        request.setIndustry("Tech");
        request.setCountry("India");
        request.setState("Delhi");
        request.setCity("Delhi");
        request.setAddress("Short"); // Too short address
        request.setCompanySize("10-50");
        request.setPlan("STARTER");
        request.setBillingCycle("MONTHLY");

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSignupAndEmailVerificationSuccess() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setFullName("Test Signup Admin");
        request.setEmail("testsignupadmin@company.com");
        request.setCountryCode("+91");
        request.setPhone("+91 98765 43210");
        request.setPassword("SecurePass123!");
        request.setOrgName("Test Registration Org");
        request.setIndustry("ITServices");
        request.setCountry("India");
        request.setState("Delhi");
        request.setCity("Delhi");
        request.setAddress("123, Ring Road, Okhla Phase 3");
        request.setCompanySize("11-50");
        request.setPlan("STARTER");
        request.setBillingCycle("YEARLY");
        request.setGstNumber("07AAAAA1111A1Z1"); // valid format

        // === 1. Perform Signup ===
        MvcResult signupResult = mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SIGNUP_SUCCESS"))
                .andExpect(jsonPath("$.data.organizationId").exists())
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.emailVerificationRequired").value(true))
                .andReturn();

        String responseStr = signupResult.getResponse().getContentAsString();
        String orgCode = objectMapper.readTree(responseStr).path("data").path("organizationId").asText();
        String userId = objectMapper.readTree(responseStr).path("data").path("userId").asText();

        assertNotNull(orgCode);
        assertNotNull(userId);

        // === 2. Verify Database Records ===
        Optional<Organization> orgOpt = organizationRepository.findByOrganizationCode(orgCode);
        assertTrue(orgOpt.isPresent());
        Organization org = orgOpt.get();
        assertEquals("Test Registration Org", org.getName());
        assertEquals("testregistrationorg", org.getNormalizedName());
        assertEquals("PENDING_VERIFICATION", org.getStatus().name());

        Optional<User> userOpt = userRepository.findByUserId(userId);
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();
        assertEquals("testsignupadmin@company.com", user.getWorkEmail());
        assertEquals("PENDING_EMAIL_VERIFICATION", user.getStatus());

        Optional<Tenant> tenantOpt = tenantRepository.findBySubdomain("test-registration-org");
        assertTrue(tenantOpt.isPresent());
        Tenant tenant = tenantOpt.get();
        assertEquals(org.getId(), tenant.getOrganization().getId());
        assertEquals("ACTIVE", tenant.getStatus().name());

        Optional<EmailVerification> verificationOpt = verificationRepository.findByUserId(user.getId());
        assertTrue(verificationOpt.isPresent());
        String token = verificationOpt.get().getToken();
        assertNotNull(token);

        // === 3. Verify Email ===
        Map<String, String> verifyReq = new HashMap<>();
        verifyReq.put("token", token);

        mockMvc.perform(post("/api/v1/auth/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_SUCCESS"));

        // Verify status updates
        User activeUser = userRepository.findByUserId(userId).get();
        assertEquals("ACTIVE", activeUser.getStatus());

        Organization activeOrg = organizationRepository.findByOrganizationCode(orgCode).get();
        assertEquals("ACTIVE", activeOrg.getStatus().name());
    }
}
