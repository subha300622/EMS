package com.example.ems.onboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.onboarding.dto.*;
import com.example.ems.onboarding.service.OnboardingService;
import com.example.ems.security.service.JwtService;

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

import static org.mockito.ArgumentMatchers.any;
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
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private OnboardingController onboardingController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(onboardingController).build();
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

        mockMvc.perform(get("/api/v1/onboarding/me")
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

        mockMvc.perform(put("/api/v1/onboarding/me")
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

        mockMvc.perform(get("/api/v1/onboarding/me/documents")
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

        mockMvc.perform(post("/api/v1/onboarding/me/submit")
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

        OnboardingDashboardResponse response = new OnboardingDashboardResponse();
        response.setTotalOnboardings(10);
        response.setCompletedOnboardings(4);
        response.setTaskCompletionRate(75.5);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(hrEmail);
        when(userRepository.findByWorkEmail(hrEmail)).thenReturn(Optional.of(hrUser));
        when(roleService.hasPermission(hrEmail, "employee.create")).thenReturn(true);
        when(onboardingService.getDashboardStats()).thenReturn(response);

        mockMvc.perform(get("/api/v1/onboarding-records/dashboard")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOnboardings").value(10))
                .andExpect(jsonPath("$.data.completedOnboardings").value(4))
                .andExpect(jsonPath("$.data.taskCompletionRate").value(75.5));
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

        mockMvc.perform(post("/api/v1/onboarding-records")
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
        // Standard user has no management permissions
        when(roleService.hasPermission(testEmail, "employee.create")).thenReturn(false);
        when(roleService.hasPermission(testEmail, "employee.update")).thenReturn(false);
        when(roleService.hasPermission(testEmail, "recruitment.manage")).thenReturn(false);
        when(onboardingService.getOnboardingByEmployeeEmail(testEmail)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/onboarding-records")
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
        when(roleService.hasPermission(hrEmail, "employee.create")).thenReturn(true);
        when(onboardingService.approveOnboarding(10L)).thenReturn(Optional.of(response));

        mockMvc.perform(patch("/api/v1/onboarding-records/10/approve")
                .header("Authorization", "Bearer mock-token"))
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
        when(roleService.hasPermission(employeeEmail, "employee.create")).thenReturn(false);
        when(roleService.hasPermission(employeeEmail, "employee.update")).thenReturn(false);
        when(roleService.hasPermission(employeeEmail, "recruitment.manage")).thenReturn(false);

        mockMvc.perform(get("/api/v1/onboarding-records/dashboard")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
    }
}
