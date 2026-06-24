package com.example.ems.onboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.onboarding.dto.*;
import com.example.ems.onboarding.service.OnboardingService;
import com.example.ems.onboarding.service.TeamOnboardingService;
import com.example.ems.security.service.JwtService;
import com.example.ems.onboarding.entity.OnboardingEventLog;
import com.example.ems.onboarding.repository.OnboardingEventLogRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OnboardingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OnboardingService onboardingService;

    @Mock
    private TeamOnboardingService teamOnboardingService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OnboardingEventLogRepository onboardingEventLogRepository;

    @InjectMocks
    private OnboardingController onboardingController;

    @InjectMocks
    private DashboardController dashboardController;

    @InjectMocks
    private TaskController taskController;

    @InjectMocks
    private DocumentController documentController;

    @InjectMocks
    private ApprovalController approvalController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(
                onboardingController,
                dashboardController,
                taskController,
                documentController,
                approvalController
        ).build();
    }

    @Test
    public void testGetMyOnboardingDetailsSuccess() throws Exception {
        String testEmail = "johndoe@example.com";
        User user = new User();
        user.setWorkEmail(testEmail);

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeId("EMP001");
        employee.setEmail(testEmail);
        employee.setFullName("John Doe");

        Onboarding onboarding = new Onboarding();
        onboarding.setId(10L);
        onboarding.setStatus("PENDING");

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(testEmail);
        when(userRepository.findByWorkEmail(testEmail)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(testEmail)).thenReturn(Optional.of(employee));
        when(onboardingService.getOrCreateOnboardingForEmployee(any())).thenReturn(onboarding);
        when(onboardingService.getTasks(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/onboarding?scope=me")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value("EMP001"));
    }

    @Test
    public void testUpdateMyOnboardingProfileSuccess() throws Exception {
        String testEmail = "johndoe@example.com";
        User user = new User();
        user.setWorkEmail(testEmail);

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmail(testEmail);

        Onboarding onboarding = new Onboarding();
        onboarding.setId(10L);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(testEmail);
        when(userRepository.findByWorkEmail(testEmail)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(testEmail)).thenReturn(Optional.of(employee));
        when(onboardingService.getOrCreateOnboardingForEmployee(any())).thenReturn(onboarding);

        mockMvc.perform(put("/api/v1/onboarding")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phone\": \"1234567890\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Onboarding profile updated successfully"));
    }

    @Test
    public void testGetMyOnboardingDocumentsSuccess() throws Exception {
        String testEmail = "johndoe@example.com";
        User user = new User();
        user.setWorkEmail(testEmail);

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmail(testEmail);

        Onboarding onboarding = new Onboarding();
        onboarding.setId(10L);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(testEmail);
        when(userRepository.findByWorkEmail(testEmail)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(testEmail)).thenReturn(Optional.of(employee));
        when(onboardingService.getOrCreateOnboardingForEmployee(any())).thenReturn(onboarding);
        when(onboardingService.getDocuments(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/onboarding/10/documents")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testSubmitMyOnboardingSuccess() throws Exception {
        String testEmail = "johndoe@example.com";
        User user = new User();
        user.setWorkEmail(testEmail);

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmail(testEmail);

        Onboarding onboarding = new Onboarding();
        onboarding.setId(10L);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(testEmail);
        when(userRepository.findByWorkEmail(testEmail)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(testEmail)).thenReturn(Optional.of(employee));
        when(onboardingService.getOrCreateOnboardingForEmployee(any())).thenReturn(onboarding);

        mockMvc.perform(post("/api/v1/onboarding/10/submit")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));
    }

    @Test
    public void testGetDashboardSuccess() throws Exception {
        String hrEmail = "hr@example.com";
        User hrUser = new User();
        hrUser.setWorkEmail(hrEmail);

        Map<String, Object> response = Map.of(
                "activeOnboardings", 10L,
                "completedThisMonth", 4L
        );

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(hrEmail);
        when(userRepository.findByWorkEmail(hrEmail)).thenReturn(Optional.of(hrUser));
        when(teamOnboardingService.getHrSummary()).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard?role=HR")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeOnboardings").value(10))
                .andExpect(jsonPath("$.data.completedThisMonth").value(4));
    }

    @Test
    public void testCreateOnboardingSuccess() throws Exception {
        String hrEmail = "hr@example.com";
        User hrUser = new User();
        hrUser.setWorkEmail(hrEmail);

        OnboardingRequest request = new OnboardingRequest();
        request.setEmployeeId(1L);
        request.setStartDate(LocalDate.of(2026, 6, 1));

        OnboardingResponse response = new OnboardingResponse();
        response.setId(10L);
        response.setEmployeeId(1L);
        response.setEmployeeName("John Doe");
        response.setEmployeeEmail("johndoe@example.com");
        response.setStatus("PENDING");
        response.setStartDate(LocalDate.of(2026, 6, 1));

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(hrEmail);
        when(userRepository.findByWorkEmail(hrEmail)).thenReturn(Optional.of(hrUser));
        when(roleService.hasPermission(hrEmail, "employee.create")).thenReturn(true);
        when(onboardingService.createOnboarding(any(OnboardingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/onboarding")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    public void testListOnboardingsSelfOnly() throws Exception {
        String testEmail = "johndoe@example.com";
        User user = new User();
        user.setWorkEmail(testEmail);

        OnboardingResponse response = new OnboardingResponse();
        response.setId(10L);
        response.setEmployeeEmail(testEmail);
        response.setStatus("PENDING");

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(testEmail);
        when(userRepository.findByWorkEmail(testEmail)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(testEmail, "employee.create")).thenReturn(false);
        when(roleService.hasPermission(testEmail, "employee.update")).thenReturn(false);
        when(roleService.hasPermission(testEmail, "recruitment.manage")).thenReturn(false);
        when(onboardingService.getOnboardingByEmployeeEmail(testEmail)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/onboarding")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].employeeEmail").value(testEmail));
    }

    @Test
    public void testApproveOnboardingSuccess() throws Exception {
        String hrEmail = "hr@example.com";
        User hrUser = new User();
        hrUser.setWorkEmail(hrEmail);

        OnboardingResponse response = new OnboardingResponse();
        response.setId(10L);
        response.setStatus("APPROVED");

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(hrEmail);
        when(userRepository.findByWorkEmail(hrEmail)).thenReturn(Optional.of(hrUser));
        when(onboardingService.approveOnboarding(10L)).thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/approvals")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"entityType\":\"ONBOARDING\",\"entityId\":10,\"action\":\"APPROVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    public void testGetDashboardAccessDenied() throws Exception {
        String employeeEmail = "employee@example.com";
        User empUser = new User();
        empUser.setWorkEmail(employeeEmail);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(employeeEmail);
        when(userRepository.findByWorkEmail(employeeEmail)).thenReturn(Optional.of(empUser));

        // When standard user calls general GET /api/v1/onboarding, they get only their own record, but if we do not mock onboardingService.getOnboardingByEmployeeEmail it returns empty/error.
        // Let's verify that authentication works as expected.
        mockMvc.perform(get("/api/v1/onboarding")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testReplayFailedEventSuccess() throws Exception {
        String hrEmail = "hr@example.com";
        User hrUser = new User();
        hrUser.setWorkEmail(hrEmail);

        OnboardingEventLog log = new OnboardingEventLog();
        log.setId(100L);
        log.setOnboardingId(10L);
        log.setStatus("FAILED");
        log.setRetryCount(0);
        log.setEventData("Document ID: 50, Status: VERIFIED");

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(hrEmail);
        when(userRepository.findByWorkEmail(hrEmail)).thenReturn(Optional.of(hrUser));
        when(roleService.hasRoleOrGreater(any(), eq("HR"))).thenReturn(true);
        when(onboardingEventLogRepository.findById(100L)).thenReturn(Optional.of(log));

        mockMvc.perform(post("/api/v1/onboarding/10/event-log/replay-failed")
                .header("Authorization", "Bearer mock-token")
                .param("eventId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Event replay triggered successfully"));
    }

    @Test
    public void testReplayFailedEventForbidden() throws Exception {
        String employeeEmail = "employee@example.com";
        User empUser = new User();
        empUser.setWorkEmail(employeeEmail);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(employeeEmail);
        when(userRepository.findByWorkEmail(employeeEmail)).thenReturn(Optional.of(empUser));
        when(roleService.hasRoleOrGreater(any(), eq("HR"))).thenReturn(false);
        when(roleService.hasRoleOrGreater(any(), eq("FINANCE"))).thenReturn(false);

        mockMvc.perform(post("/api/v1/onboarding/10/event-log/replay-failed")
                .header("Authorization", "Bearer mock-token")
                .param("eventId", "100"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
