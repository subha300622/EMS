package com.example.ems.training.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.dto.*;
import com.example.ems.training.entity.TrainingCourse;
import com.example.ems.training.service.TrainingAssignmentService;
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
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TrainingAssignmentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TrainingAssignmentService assignmentService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private RoleService roleService;

    @InjectMocks
    private TrainingAssignmentController trainingAssignmentController;

    private User managerUser;
    private User empUser;
    private Employee managerEmp;
    private Employee regularEmp;

    private final String managerEmail = "manager@example.com";
    private final String empEmail = "emp@example.com";
    private final String token = "mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(trainingAssignmentController).build();

        managerUser = new User();
        managerUser.setWorkEmail(managerEmail);
        managerUser.setEmployeeId("101");

        empUser = new User();
        empUser.setWorkEmail(empEmail);
        empUser.setEmployeeId("201");

        managerEmp = new Employee();
        managerEmp.setId(101L);
        managerEmp.setEmployeeId("101");
        managerEmp.setFullName("Ravi Manager");
        managerEmp.setEmail(managerEmail);

        regularEmp = new Employee();
        regularEmp.setId(201L);
        regularEmp.setEmployeeId("201");
        regularEmp.setFullName("Arjun Mehta");
        regularEmp.setEmail(empEmail);
    }

    private void setupManager() {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(managerEmail);
        when(userRepository.findByWorkEmail(managerEmail)).thenReturn(Optional.of(managerUser));
        when(employeeRepository.findByEmployeeId("101")).thenReturn(Optional.of(managerEmp));
        when(roleService.hasPermission(managerEmail, "employee.update")).thenReturn(true);
    }

    private void setupEmployee() {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
        when(employeeRepository.findByEmployeeId("201")).thenReturn(Optional.of(regularEmp));
        when(roleService.hasPermission(empEmail, "employee.update")).thenReturn(false);
        when(roleService.hasPermission(empEmail, "employee.delete")).thenReturn(false);
        when(roleService.hasPermission(empEmail, "recruitment.manage")).thenReturn(false);
    }

    // ── A. Catalog APIs ──────────────────────────────────────────────────────
    @Test
    public void testCreateCourseSuccess() throws Exception {
        setupManager();

        TrainingCatalogRequest request = new TrainingCatalogRequest();
        request.setName("AWS Cloud Architect");
        request.setDescription("AWS fundamentals");
        request.setDurationHours(12);
        request.setCategory("CLOUD");
        request.setDifficulty("INTERMEDIATE");
        request.setIsMandatory(true);

        TrainingCourse savedCourse = new TrainingCourse();
        savedCourse.setId(101L);
        savedCourse.setTitle("AWS Cloud Architect");
        savedCourse.setStatus("ACTIVE");

        when(assignmentService.createCourse(any(TrainingCatalogRequest.class))).thenReturn(savedCourse);

        mockMvc.perform(post("/api/v1/training/catalog")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.message").value("Training course created successfully"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    public void testCreateCourseForbidden() throws Exception {
        setupEmployee();

        TrainingCatalogRequest request = new TrainingCatalogRequest();
        request.setName("AWS Cloud Architect");
        request.setDurationHours(12);

        mockMvc.perform(post("/api/v1/training/catalog")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetCatalogSuccess() throws Exception {
        TrainingCourse course = new TrainingCourse();
        course.setId(101L);
        course.setTitle("AWS Cloud Architect");

        when(assignmentService.getCourses()).thenReturn(List.of(course));

        mockMvc.perform(get("/api/v1/training/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("AWS Cloud Architect"));
    }

    // ── B. Assignment APIs ───────────────────────────────────────────────────
    @Test
    public void testAssignTrainingSuccess() throws Exception {
        setupManager();

        TrainingAssignRequest request = new TrainingAssignRequest();
        request.setCourseId(101L);
        request.setAssignedToEmployeeIds(List.of(201L));
        request.setDueDate(LocalDate.of(2026, 7, 15));
        request.setPriority("HIGH");

        Map<String, Object> respMap = Map.of(
                "assignmentId", 9001L,
                "message", "Training assigned successfully",
                "assignedCount", 1,
                "status", "ASSIGNED",
                "assignedOn", "2026-06-24"
        );

        when(assignmentService.assignTraining(any(TrainingAssignRequest.class))).thenReturn(respMap);

        mockMvc.perform(post("/api/v1/training/assign")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentId").value(9001))
                .andExpect(jsonPath("$.assignedCount").value(1));
    }

    @Test
    public void testGetAssignmentDetailsSuccess() throws Exception {
        Map<String, Object> details = Map.of(
                "assignmentId", 9001L,
                "dueDate", "2026-07-15",
                "priority", "HIGH",
                "status", "IN_PROGRESS",
                "progress", 40
        );

        when(assignmentService.getAssignmentDetails(9001L)).thenReturn(Optional.of(details));

        mockMvc.perform(get("/api/v1/training/assignments/9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentId").value(9001))
                .andExpect(jsonPath("$.progress").value(40));
    }

    // ── C. Employee APIs ─────────────────────────────────────────────────────
    @Test
    public void testGetMyTrainingsSuccess() throws Exception {
        setupEmployee();

        Map<String, Object> myTraining = Map.of(
                "assignmentId", 9001L,
                "courseName", "AWS Cloud Architect",
                "priority", "HIGH",
                "dueDate", "2026-07-15",
                "status", "IN_PROGRESS",
                "progress", 40
        );

        when(assignmentService.getMyTrainings(empEmail)).thenReturn(List.of(myTraining));

        mockMvc.perform(get("/api/v1/training/my")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseName").value("AWS Cloud Architect"));
    }

    @Test
    public void testUpdateProgressSuccess() throws Exception {
        setupEmployee();

        TrainingProgressUpdateRequest request = new TrainingProgressUpdateRequest();
        request.setEmployeeId(201L);
        request.setProgress(70);
        request.setStatus("IN_PROGRESS");

        Map<String, Object> respMap = Map.of(
                "message", "Progress updated successfully",
                "currentProgress", 70,
                "status", "IN_PROGRESS"
        );

        when(assignmentService.updateProgress(eq(9001L), any(TrainingProgressUpdateRequest.class))).thenReturn(respMap);

        mockMvc.perform(put("/api/v1/training/9001/progress")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentProgress").value(70));
    }

    @Test
    public void testCompleteTrainingSuccess() throws Exception {
        setupEmployee();

        Map<String, Object> respMap = Map.of(
                "message", "Training completed successfully",
                "status", "COMPLETED",
                "completedDate", "2026-06-24"
        );

        when(assignmentService.completeTraining(9001L, 201L)).thenReturn(respMap);

        mockMvc.perform(put("/api/v1/training/9001/complete")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"employeeId\": 201}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    // ── D. Team APIs ─────────────────────────────────────────────────────────
    @Test
    public void testGetTeamSummarySuccess() throws Exception {
        setupManager();

        TeamSummaryResponse summary = new TeamSummaryResponse(4, 12, 6, 4, 2, 75.0);
        when(assignmentService.getTeamSummary(101L)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/training/team/summary?managerId=101")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.complianceRate").value(75.0))
                .andExpect(jsonPath("$.totalAssigned").value(12));
    }

    @Test
    public void testGetTeamProgressSuccess() throws Exception {
        setupManager();

        Map<String, Object> progMap = Map.of("content", List.of(
                Map.of("employeeId", 201, "employeeName", "Arjun Mehta", "totalAssigned", 3, "risk", false)
        ));

        when(assignmentService.getTeamProgressList(101L)).thenReturn(progMap);

        mockMvc.perform(get("/api/v1/training/team?managerId=101")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].employeeName").value("Arjun Mehta"));
    }

    @Test
    public void testGetTeamRiskSuccess() throws Exception {
        setupManager();

        TeamRiskResponse riskItem = new TeamRiskResponse(203L, "Dev Patel", 2, "HIGH");
        when(assignmentService.getTeamRiskList(101L)).thenReturn(List.of(riskItem));

        mockMvc.perform(get("/api/v1/training/team/risk?managerId=101")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeName").value("Dev Patel"))
                .andExpect(jsonPath("$[0].riskLevel").value("HIGH"));
    }

    @Test
    public void testGetEmployeeDetailSuccess() throws Exception {
        setupManager();

        EmployeeTrainingDetailResponse details = new EmployeeTrainingDetailResponse(
                203L, "Dev Patel",
                List.of(new EmployeeTrainingDetailResponse.CourseProgressDto("Git CI/CD", "OVERDUE", 0, LocalDate.of(2026, 6, 10))),
                List.of(new EmployeeTrainingDetailResponse.CertificationDto("Git Basics", LocalDate.of(2026, 5, 10))),
                "AT_RISK"
        );

        when(assignmentService.getEmployeeDetail(203L)).thenReturn(details);

        mockMvc.perform(get("/api/v1/training/employee/203")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("Dev Patel"))
                .andExpect(jsonPath("$.overallStatus").value("AT_RISK"));
    }
}
