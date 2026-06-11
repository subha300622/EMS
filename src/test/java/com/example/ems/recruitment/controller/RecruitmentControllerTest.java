package com.example.ems.recruitment.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.recruitment.dto.JobRequest;
import com.example.ems.recruitment.dto.JobResponse;
import com.example.ems.recruitment.dto.RecruitmentDashboardResponse;
import com.example.ems.recruitment.entity.Job;
import com.example.ems.recruitment.service.RecruitmentService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RecruitmentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RecruitmentService recruitmentService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RecruitmentController recruitmentController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(recruitmentController).build();
    }

    @Test
    public void testGetDashboardSuccess() throws Exception {
        String hrEmail = "hr@example.com";
        User hrUser = new User();
        hrUser.setWorkEmail(hrEmail);

        RecruitmentDashboardResponse response = new RecruitmentDashboardResponse();
        response.setTotalJobs(5);
        response.setActiveJobs(3);
        response.setTotalCandidates(10);
        response.setCandidatesHired(2);
        response.setConversionRate(20.0);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(hrEmail);
        when(userRepository.findByWorkEmail(hrEmail)).thenReturn(Optional.of(hrUser));
        when(roleService.hasPermission(hrEmail, "recruitment.manage")).thenReturn(true);
        when(recruitmentService.getDashboardStats()).thenReturn(response);

        mockMvc.perform(get("/api/v1/recruitments/dashboard")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalJobs").value(5))
                .andExpect(jsonPath("$.data.activeJobs").value(3))
                .andExpect(jsonPath("$.data.totalCandidates").value(10))
                .andExpect(jsonPath("$.data.candidatesHired").value(2))
                .andExpect(jsonPath("$.data.conversionRate").value(20.0));
    }

    @Test
    public void testCreateJobSuccess() throws Exception {
        String hrEmail = "hr@example.com";
        User hrUser = new User();
        hrUser.setWorkEmail(hrEmail);

        JobRequest request = new JobRequest();
        request.setTitle("Senior Java Developer");
        request.setDepartment("Engineering");
        request.setLocation("Remote");
        request.setDescription("Design and implement microservices");
        request.setSalaryRange("$120k - $150k");

        Job job = new Job();
        job.setId(101L);
        job.setTitle(request.getTitle());
        job.setDepartment(request.getDepartment());
        job.setLocation(request.getLocation());
        job.setDescription(request.getDescription());
        job.setSalaryRange(request.getSalaryRange());
        job.setStatus("DRAFT");
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        JobResponse response = new JobResponse(job);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(hrEmail);
        when(userRepository.findByWorkEmail(hrEmail)).thenReturn(Optional.of(hrUser));
        when(roleService.hasPermission(hrEmail, "recruitment.manage")).thenReturn(true);
        when(recruitmentService.createJob(any(JobRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/recruitments/jobs")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Senior Java Developer"))
                .andExpect(jsonPath("$.data.department").value("Engineering"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    public void testHireCandidateSuccess() throws Exception {
        String hrEmail = "hr@example.com";
        User hrUser = new User();
        hrUser.setWorkEmail(hrEmail);

        Employee hiredEmployee = new Employee();
        hiredEmployee.setId(5L);
        hiredEmployee.setEmployeeId("EMP005");
        hiredEmployee.setFullName("Alice Green");
        hiredEmployee.setEmail("alice.green@example.com");
        hiredEmployee.setDepartment("Engineering");
        hiredEmployee.setDesignation("Software Engineer");
        hiredEmployee.setAnnualSalary(new BigDecimal("100000.00"));
        hiredEmployee.setJoiningDate(LocalDate.now());

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(hrEmail);
        when(userRepository.findByWorkEmail(hrEmail)).thenReturn(Optional.of(hrUser));
        when(roleService.hasPermission(hrEmail, "recruitment.manage")).thenReturn(true);
        when(recruitmentService.hireCandidate(12L)).thenReturn(hiredEmployee);

        mockMvc.perform(post("/api/v1/recruitments/candidates/12/hire")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Alice Green"))
                .andExpect(jsonPath("$.data.employeeId").value("EMP005"))
                .andExpect(jsonPath("$.data.email").value("alice.green@example.com"));
    }

    @Test
    public void testGetDashboardAccessDenied() throws Exception {
        String employeeEmail = "employee@example.com";
        User empUser = new User();
        empUser.setWorkEmail(employeeEmail);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(employeeEmail);
        when(userRepository.findByWorkEmail(employeeEmail)).thenReturn(Optional.of(empUser));
        when(roleService.hasPermission(employeeEmail, "recruitment.manage")).thenReturn(false);

        mockMvc.perform(get("/api/v1/recruitments/dashboard")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
    }

    @Test
    public void testGetDashboardUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/recruitments/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_014"));
    }
}
