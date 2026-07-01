package com.example.ems.storage;

import com.example.ems.auth.controller.AuthController;
import com.example.ems.auth.dto.LoginRequest;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.repository.UserSessionRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.JwtAuthenticationFilter;
import com.example.ems.security.RateLimitingFilter;
import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.storage.controller.FileController;
import com.example.ems.storage.entity.FileMetadata;
import com.example.ems.storage.repository.FileMetadataRepository;
import com.example.ems.storage.service.MockStorageServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class FileStorageIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private FileController fileController;

    @Autowired
    private AuthController authController;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private Environment environment;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private MockStorageServiceImpl mockStorageService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final String PASSWORD = "password123";

    private String empOwnerToken;
    private String empOtherToken;
    private String hrSameToken;
    private String hrOtherToken;
    private String mgrSameToken;
    private String mgrDirectToken;
    private String mgrOtherToken;
    private String adminToken;

    @BeforeEach
    public void setUp() throws Exception {
        SecurityContextHolder.clearContext();
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
                authenticationManager, authenticationEntryPoint, environment
        );

        mockMvc = MockMvcBuilders.standaloneSetup(fileController, authController)
                .addFilters(jwtAuthenticationFilter, rateLimitingFilter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        cleanup();
        mockStorageService.clear();

        // 1. Roles Setup
        Role employeeRole = getOrCreateRole("EMPLOYEE");
        Role hrRole = getOrCreateRole("HR");
        Role managerRole = getOrCreateRole("MANAGER");
        Role adminRole = getOrCreateRole("ADMIN");

        // 2. Employees (in DB for department/manager verification)
        Employee mgrDirectEmp = createEmployee("EMP_MGR_DIRECT", "Marketing Manager", "Marketing", "test.marketing_mgr@company.com", null);
        createEmployee("EMP_OWNER", "Software Engineer", "Engineering", "test.owner@company.com", mgrDirectEmp);
        createEmployee("EMP_OTHER", "Analyst", "Finance", "test.other@company.com", null);
        createEmployee("EMP_HR_SAME", "HR Rep", "Engineering", "test.hr_same@company.com", null);
        createEmployee("EMP_HR_OTHER", "HR Lead", "Finance", "test.hr_other@company.com", null);
        createEmployee("EMP_MGR_SAME", "Engineering Mgr", "Engineering", "test.engineering_mgr@company.com", null);
        createEmployee("EMP_MGR_OTHER", "Finance Manager", "Finance", "test.finance_mgr@company.com", null);
        createEmployee("EMP_ADMIN", "System Admin", "IT", "test.admin@company.com", null);

        // 3. Users Setup (matching employees)
        createUser("EMP_OWNER", "test.owner@company.com", "Engineering", employeeRole);
        createUser("EMP_OTHER", "test.other@company.com", "Finance", employeeRole);
        createUser("EMP_HR_SAME", "test.hr_same@company.com", "Engineering", hrRole);
        createUser("EMP_HR_OTHER", "test.hr_other@company.com", "Finance", hrRole);
        createUser("EMP_MGR_SAME", "test.engineering_mgr@company.com", "Engineering", managerRole);
        createUser("EMP_MGR_DIRECT", "test.marketing_mgr@company.com", "Marketing", managerRole);
        createUser("EMP_MGR_OTHER", "test.finance_mgr@company.com", "Finance", managerRole);
        createUser("EMP_ADMIN", "test.admin@company.com", "IT", adminRole);

        // 4. Log in and get Access Tokens
        empOwnerToken = loginUser("test.owner@company.com");
        empOtherToken = loginUser("test.other@company.com");
        hrSameToken = loginUser("test.hr_same@company.com");
        hrOtherToken = loginUser("test.hr_other@company.com");
        mgrSameToken = loginUser("test.engineering_mgr@company.com");
        mgrDirectToken = loginUser("test.marketing_mgr@company.com");
        mgrOtherToken = loginUser("test.finance_mgr@company.com");
        adminToken = loginUser("test.admin@company.com");
    }

    @AfterEach
    public void tearDown() {
        cleanup();
        mockStorageService.clear();
        SecurityContextHolder.clearContext();
    }

    private void cleanup() {
        fileMetadataRepository.deleteAll();
        deleteUserAndSessions("test.owner@company.com");
        deleteUserAndSessions("test.other@company.com");
        deleteUserAndSessions("test.hr_same@company.com");
        deleteUserAndSessions("test.hr_other@company.com");
        deleteUserAndSessions("test.engineering_mgr@company.com");
        deleteUserAndSessions("test.marketing_mgr@company.com");
        deleteUserAndSessions("test.finance_mgr@company.com");
        deleteUserAndSessions("test.admin@company.com");

        String[] testEmployeeIds = {
            "EMP_OWNER", "EMP_OTHER", "EMP_HR_SAME", "EMP_HR_OTHER",
            "EMP_MGR_SAME", "EMP_MGR_DIRECT", "EMP_MGR_OTHER", "EMP_ADMIN"
        };
        for (String empId : testEmployeeIds) {
            employeeRepository.findByEmployeeId(empId).ifPresent(emp -> {
                emp.setManager(null);
                employeeRepository.saveAndFlush(emp);
            });
        }
        for (String empId : testEmployeeIds) {
            employeeRepository.findByEmployeeId(empId).ifPresent(emp -> {
                employeeRepository.delete(emp);
            });
        }
    }

    private void deleteUserAndSessions(String email) {
        Optional<User> optUser = userRepository.findByWorkEmail(email);
        if (optUser.isPresent()) {
            User u = optUser.get();
            userSessionRepository.findAll().stream()
                    .filter(s -> u.getUserId().equals(s.getUserId()))
                    .forEach(s -> userSessionRepository.delete(s));
            userRepository.delete(u);
        }
    }

    private Role getOrCreateRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(name);
                    r.setDescription(name + " role");
                    return roleRepository.save(r);
                });
    }

    private Employee createEmployee(String employeeId, String designation, String department, String email, Employee manager) {
        Employee emp = new Employee();
        emp.setEmployeeId(employeeId);
        emp.setFullName(employeeId.replace("EMP_", "") + " Employee");
        emp.setEmail(email);
        emp.setDesignation(designation);
        emp.setDepartment(department);
        emp.setManager(manager);
        emp.setPhone("1234567890");
        return employeeRepository.save(emp);
    }

    private User createUser(String userId, String email, String department, Role role) {
        User user = new User();
        user.setUserId(userId);
        user.setFullName(userId.replace("EMP_", "") + " User");
        user.setWorkEmail(email);
        user.setDepartment(department);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setStatus("ACTIVE");
        return userRepository.save(user);
    }

    private String loginUser(String email) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("tokens").path("accessToken").asText();
    }

    @Test
    public void testProfileImageUploadSuccessAndDownload() throws Exception {
        // === 1. Upload Profile Image ===
        MockMultipartFile file = new MockMultipartFile(
                "file", "profile.png", MediaType.IMAGE_PNG_VALUE, "fake image bytes".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/files/profile-image")
                .file(file)
                .header("Authorization", "Bearer " + empOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("profile.png"))
                .andExpect(jsonPath("$.data.fileType").value("PROFILE_IMAGE"))
                .andExpect(jsonPath("$.data.uploadedByUserId").value("EMP_OWNER"))
                .andReturn();

        String uploadBody = uploadResult.getResponse().getContentAsString();
        Long fileId = objectMapper.readTree(uploadBody).path("data").path("id").asLong();

        // Verify Employee profileImage field was updated to point to the secure download endpoint
        Employee emp = employeeRepository.findByEmployeeId("EMP_OWNER").orElse(null);
        assertNotNull(emp);
        assertEquals("/api/files/" + fileId + "/download", emp.getProfileImage());

        // === 2. Verify Storage Content ===
        FileMetadata metadata = fileMetadataRepository.findById(fileId).orElse(null);
        assertNotNull(metadata);
        assertNotNull(mockStorageService.getFileData(metadata.getFilePath()));

        // === 3. Download Profile Image (Self Access - Should succeed) ===
        mockMvc.perform(get("/api/files/" + fileId + "/download")
                .header("Authorization", "Bearer " + empOwnerToken))
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testDocumentUploadSuccessAndRBACAccess() throws Exception {
        // === 1. Upload Document ===
        MockMultipartFile file = new MockMultipartFile(
                "file", "contract.pdf", MediaType.APPLICATION_PDF_VALUE, "fake pdf bytes".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/files/upload-document")
                .file(file)
                .param("fileType", "DOCUMENT")
                .header("Authorization", "Bearer " + empOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("contract.pdf"))
                .andExpect(jsonPath("$.data.fileType").value("DOCUMENT"))
                .andReturn();

        String uploadBody = uploadResult.getResponse().getContentAsString();
        Long fileId = objectMapper.readTree(uploadBody).path("data").path("id").asLong();

        // === 2. RBAC Tests for Download ===

        // A. EMPLOYEE self (Should succeed)
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/files/" + fileId + "/download")
                .header("Authorization", "Bearer " + empOwnerToken))
                .andExpect(status().isOk());

        // B. EMPLOYEE other (Should fail 403)
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/files/" + fileId + "/download")
                .header("Authorization", "Bearer " + empOtherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));

        // C. HR same department ("Engineering" vs file owner "Engineering") (Should succeed)
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/files/" + fileId + "/download")
                .header("Authorization", "Bearer " + hrSameToken))
                .andExpect(status().isOk());

        // D. HR other department ("Finance" vs file owner "Engineering") (Should fail 403)
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/files/" + fileId + "/download")
                .header("Authorization", "Bearer " + hrOtherToken))
                .andExpect(status().isForbidden());

        // E. MANAGER same department ("Engineering" vs file owner "Engineering") (Should succeed)
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/files/" + fileId + "/download")
                .header("Authorization", "Bearer " + mgrSameToken))
                .andExpect(status().isOk());

        // F. MANAGER direct manager (Marketing manager directly manages Marketing/Engineering employee) (Should succeed)
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/files/" + fileId + "/download")
                .header("Authorization", "Bearer " + mgrDirectToken))
                .andExpect(status().isOk());

        // G. MANAGER other department ("Finance" and does not manage) (Should fail 403)
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/files/" + fileId + "/download")
                .header("Authorization", "Bearer " + mgrOtherToken))
                .andExpect(status().isForbidden());

        // H. ADMIN (Should succeed)
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/files/" + fileId + "/download")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // I. No auth header (Should fail 401/403)
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/files/" + fileId + "/download"))
                .andExpect(status().is4xxClientError());
    }
}
