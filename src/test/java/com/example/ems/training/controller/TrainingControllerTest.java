package com.example.ems.training.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.dto.TrainingAssessmentRequest;
import com.example.ems.training.dto.TrainingAttendanceRequest;
import com.example.ems.training.dto.TrainingCertificateResponse;
import com.example.ems.training.dto.TrainingCourseRequest;
import com.example.ems.training.dto.TrainingCourseResponse;
import com.example.ems.training.dto.TrainingDashboardResponse;
import com.example.ems.training.dto.TrainingEnrollmentRequest;
import com.example.ems.training.dto.TrainingEnrollmentResponse;
import com.example.ems.training.dto.TrainingSessionRequest;
import com.example.ems.training.dto.TrainingSessionResponse;
import com.example.ems.training.service.TrainingService;

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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TrainingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private TrainingService trainingService;
    @Mock private UserRepository userRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private JwtService jwtService;
    @Mock private RoleService roleService;

    @InjectMocks
    private TrainingController trainingController;

    private User hrUser;
    private User empUser;
    private final String hrEmail = "hr@example.com";
    private final String empEmail = "emp@example.com";
    private final String token = "mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(trainingController).build();

        hrUser = new User();
        hrUser.setWorkEmail(hrEmail);
        hrUser.setEmployeeId("1");

        empUser = new User();
        empUser.setWorkEmail(empEmail);
        empUser.setEmployeeId("2");
    }

    private void setupManager() {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(hrEmail);
        when(userRepository.findByWorkEmail(hrEmail)).thenReturn(Optional.of(hrUser));
        when(roleService.hasPermission(hrEmail, "employee.update")).thenReturn(true);
    }

    private void setupEmployee() {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
        when(roleService.hasPermission(empEmail, "employee.update")).thenReturn(false);
        when(roleService.hasPermission(empEmail, "employee.delete")).thenReturn(false);
        when(roleService.hasPermission(empEmail, "recruitment.manage")).thenReturn(false);
    }

    // ── 1. DASHBOARD ──────────────────────────────────────────────────────────
    @Test
    public void testGetDashboardSuccess() throws Exception {
        setupManager();

        TrainingDashboardResponse stats = new TrainingDashboardResponse();
        stats.setTotalCourses(10);
        stats.setActiveCourses(8);
        stats.setTotalSessions(5);
        stats.setTotalEnrollments(20);
        stats.setCompletedEnrollments(12);
        stats.setAverageProgress(75.5);
        stats.setWithdrawalRate(5.0);

        when(trainingService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/trainings/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCourses").value(10))
                .andExpect(jsonPath("$.data.activeCourses").value(8))
                .andExpect(jsonPath("$.data.totalSessions").value(5))
                .andExpect(jsonPath("$.data.totalEnrollments").value(20))
                .andExpect(jsonPath("$.data.completedEnrollments").value(12))
                .andExpect(jsonPath("$.data.averageProgress").value(75.5))
                .andExpect(jsonPath("$.data.withdrawalRate").value(5.0));
    }

    @Test
    public void testGetDashboardForbidden() throws Exception {
        setupEmployee();

        mockMvc.perform(get("/api/v1/trainings/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
    }

    @Test
    public void testGetDashboardUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/trainings/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_014"));
    }

    // ── 2. COURSES ────────────────────────────────────────────────────────────
    @Test
    public void testCreateCourseSuccess() throws Exception {
        setupManager();

        TrainingCourseRequest request = new TrainingCourseRequest();
        request.setTitle("Java Basics");
        request.setDescription("Introduction to Java");
        request.setCategory("Technical");
        request.setDurationHours(16);

        TrainingCourseResponse response = new TrainingCourseResponse();
        response.setId(1L);
        response.setTitle("Java Basics");
        response.setDescription("Introduction to Java");
        response.setCategory("Technical");
        response.setStatus("ACTIVE");
        response.setDurationHours(16);

        when(trainingService.createCourse(any(TrainingCourseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/trainings/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Java Basics"));
    }

    @Test
    public void testCreateCourseForbidden() throws Exception {
        setupEmployee();

        TrainingCourseRequest request = new TrainingCourseRequest();
        request.setTitle("Java Basics");

        mockMvc.perform(post("/api/v1/trainings/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetCoursesSuccess() throws Exception {
        setupEmployee();

        TrainingCourseResponse course = new TrainingCourseResponse();
        course.setId(1L);
        course.setTitle("Java Basics");

        when(trainingService.getCourses()).thenReturn(List.of(course));

        mockMvc.perform(get("/api/v1/trainings/courses")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Java Basics"));
    }

    @Test
    public void testGetCourseByIdSuccess() throws Exception {
        setupEmployee();

        TrainingCourseResponse course = new TrainingCourseResponse();
        course.setId(1L);
        course.setTitle("Java Basics");

        when(trainingService.getCourseById(1L)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/api/v1/trainings/courses/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Java Basics"));
    }

    @Test
    public void testGetCourseByIdNotFound() throws Exception {
        setupEmployee();

        when(trainingService.getCourseById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/trainings/courses/99" )
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("TRN_001"));
    }

    @Test
    public void testUpdateCourseSuccess() throws Exception {
        setupManager();

        TrainingCourseRequest request = new TrainingCourseRequest();
        request.setTitle("Java Basics Updated");
        request.setDescription("Advanced Java Basics");
        request.setCategory("Technical");
        request.setDurationHours(20);

        TrainingCourseResponse response = new TrainingCourseResponse();
        response.setId(1L);
        response.setTitle("Java Basics Updated");
        response.setDescription("Advanced Java Basics");
        response.setCategory("Technical");
        response.setStatus("ACTIVE");
        response.setDurationHours(20);

        when(trainingService.updateCourse(eq(1L), any(TrainingCourseRequest.class))).thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/v1/trainings/courses/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Java Basics Updated"));
    }

    @Test
    public void testUpdateCourseNotFound() throws Exception {
        setupManager();

        TrainingCourseRequest request = new TrainingCourseRequest();
        request.setTitle("Java Basics Updated");

        when(trainingService.updateCourse(eq(99L), any(TrainingCourseRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/trainings/courses/99")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testDeleteCourseSuccess() throws Exception {
        setupManager();

        when(trainingService.deleteCourse(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/trainings/courses/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testDeleteCourseNotFound() throws Exception {
        setupManager();

        when(trainingService.deleteCourse(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/trainings/courses/99")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testUpdateCourseStatusSuccess() throws Exception {
        setupManager();

        TrainingCourseResponse response = new TrainingCourseResponse();
        response.setId(1L);
        response.setStatus("INACTIVE");

        when(trainingService.updateCourseStatus(eq(1L), eq("INACTIVE"))).thenReturn(Optional.of(response));

        mockMvc.perform(patch("/api/v1/trainings/courses/1/status")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    public void testUpdateCourseStatusMissingStatus() throws Exception {
        setupManager();

        mockMvc.perform(patch("/api/v1/trainings/courses/1/status")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VAL_001"));
    }

    @Test
    public void testUpdateCourseStatusNotFound() throws Exception {
        setupManager();

        when(trainingService.updateCourseStatus(eq(99L), eq("INACTIVE"))).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/v1/trainings/courses/99/status")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"INACTIVE\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("TRN_001"));
    }

    // ── 3. SESSIONS ───────────────────────────────────────────────────────────
    @Test
    public void testCreateSessionSuccess() throws Exception {
        setupManager();

        TrainingSessionRequest request = new TrainingSessionRequest();
        request.setCourseId(1L);
        request.setTrainerName("Alice");
        request.setScheduleDate(LocalDate.of(2026, 6, 15));
        request.setCapacity(20);

        TrainingSessionResponse response = new TrainingSessionResponse();
        response.setId(1L);
        response.setCourseId(1L);
        response.setTrainerName("Alice");
        response.setScheduleDate(LocalDate.of(2026, 6, 15));
        response.setCapacity(20);

        when(trainingService.createSession(any(TrainingSessionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/trainings/sessions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trainerName").value("Alice"));
    }

    @Test
    public void testCreateSessionBadRequest() throws Exception {
        setupManager();

        TrainingSessionRequest request = new TrainingSessionRequest();
        request.setCourseId(99L);
        request.setTrainerName("Alice");
        request.setScheduleDate(LocalDate.of(2026, 6, 15));

        when(trainingService.createSession(any(TrainingSessionRequest.class)))
                .thenThrow(new IllegalArgumentException("Course not found with ID: 99"));

        mockMvc.perform(post("/api/v1/trainings/sessions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("TRN_002"));
    }

    @Test
    public void testGetSessionsSuccess() throws Exception {
        setupEmployee();

        TrainingSessionResponse session = new TrainingSessionResponse();
        session.setId(1L);
        session.setTrainerName("Alice");
        session.setScheduleDate(LocalDate.of(2026, 6, 15));

        when(trainingService.getSessions()).thenReturn(List.of(session));

        mockMvc.perform(get("/api/v1/trainings/sessions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].trainerName").value("Alice"));
    }

    // ── 4. ENROLLMENTS ────────────────────────────────────────────────────────
    @Test
    public void testEnrollEmployeeSuccessByManager() throws Exception {
        setupManager();

        TrainingEnrollmentRequest request = new TrainingEnrollmentRequest();
        request.setEmployeeId("2");
        request.setSessionId(1L);

        TrainingEnrollmentResponse response = new TrainingEnrollmentResponse();
        response.setId(10L);
        response.setEmployeeId(2L);
        response.setSessionId(1L);
        response.setStatus("ENROLLED");

        when(trainingService.enrollEmployee(any(TrainingEnrollmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/trainings/enrollments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ENROLLED"));
    }

    @Test
    public void testEnrollEmployeeSuccessBySelf() throws Exception {
        setupEmployee(); // User empUser has employeeId = "2"

        TrainingEnrollmentRequest request = new TrainingEnrollmentRequest();
        request.setEmployeeId("2");
        request.setSessionId(1L);

        TrainingEnrollmentResponse response = new TrainingEnrollmentResponse();
        response.setId(10L);
        response.setEmployeeId(2L);
        response.setSessionId(1L);
        response.setStatus("ENROLLED");

        when(trainingService.enrollEmployee(any(TrainingEnrollmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/trainings/enrollments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testEnrollEmployeeForbiddenForOther() throws Exception {
        setupEmployee(); // empUser employeeId is "2"

        TrainingEnrollmentRequest request = new TrainingEnrollmentRequest();
        request.setEmployeeId("3"); // trying to enroll employee 3
        request.setSessionId(1L);

        mockMvc.perform(post("/api/v1/trainings/enrollments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
    }

    @Test
    public void testEnrollEmployeeBadRequest() throws Exception {
        setupManager();

        TrainingEnrollmentRequest request = new TrainingEnrollmentRequest();
        request.setEmployeeId("2");
        request.setSessionId(1L);

        when(trainingService.enrollEmployee(any(TrainingEnrollmentRequest.class)))
                .thenThrow(new IllegalArgumentException("Session capacity has been reached."));

        mockMvc.perform(post("/api/v1/trainings/enrollments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("TRN_003"));
    }

    @Test
    public void testGetMyEnrollmentsSuccess() throws Exception {
        setupEmployee();

        TrainingEnrollmentResponse enrollment = new TrainingEnrollmentResponse();
        enrollment.setId(10L);
        enrollment.setStatus("ENROLLED");

        when(trainingService.getMyEnrollments(empEmail)).thenReturn(List.of(enrollment));

        mockMvc.perform(get("/api/v1/trainings/my")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].enrollmentId").value(10L));
    }

    @Test
    public void testWithdrawEnrollmentSuccessByManager() throws Exception {
        setupManager();

        TrainingEnrollmentResponse response = new TrainingEnrollmentResponse();
        response.setId(10L);
        response.setStatus("WITHDRAWN");

        when(trainingService.withdrawEnrollment(10L)).thenReturn(Optional.of(response));

        mockMvc.perform(patch("/api/v1/trainings/enrollments/10/withdraw")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("WITHDRAWN"));
    }

    @Test
    public void testWithdrawEnrollmentSuccessBySelf() throws Exception {
        setupEmployee();

        TrainingEnrollmentResponse myEnrollment = new TrainingEnrollmentResponse();
        myEnrollment.setId(10L);

        // Employee owns the enrollment, so getMyEnrollments contains it
        when(trainingService.getMyEnrollments(empEmail)).thenReturn(List.of(myEnrollment));

        TrainingEnrollmentResponse response = new TrainingEnrollmentResponse();
        response.setId(10L);
        response.setStatus("WITHDRAWN");

        when(trainingService.withdrawEnrollment(10L)).thenReturn(Optional.of(response));

        mockMvc.perform(patch("/api/v1/trainings/enrollments/10/withdraw")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testWithdrawEnrollmentForbidden() throws Exception {
        setupEmployee();

        // Employee does not own the enrollment, getMyEnrollments is empty
        when(trainingService.getMyEnrollments(empEmail)).thenReturn(List.of());

        mockMvc.perform(patch("/api/v1/trainings/enrollments/10/withdraw")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
    }

    @Test
    public void testWithdrawEnrollmentNotFound() throws Exception {
        setupManager();

        when(trainingService.withdrawEnrollment(99L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/v1/trainings/enrollments/99/withdraw")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("TRN_003"));
    }

    // ── 5. ATTENDANCE ─────────────────────────────────────────────────────────
    @Test
    public void testSubmitAttendanceSuccess() throws Exception {
        setupManager();

        TrainingAttendanceRequest request = new TrainingAttendanceRequest();
        request.setEmployeeId("2");
        request.setAttendanceDate(LocalDate.of(2026, 6, 15));
        request.setStatus("PRESENT");

        Map<String, Object> result = Map.of(
                "attendanceId", 15L,
                "status", "PRESENT",
                "progressPercent", 100
        );

        when(trainingService.submitAttendance(eq(1L), any(TrainingAttendanceRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/trainings/sessions/1/attendance")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PRESENT"));
    }

    @Test
    public void testSubmitAttendanceForbidden() throws Exception {
        setupEmployee();

        TrainingAttendanceRequest request = new TrainingAttendanceRequest();
        request.setEmployeeId("2");
        request.setAttendanceDate(LocalDate.of(2026, 6, 15));
        request.setStatus("PRESENT");

        mockMvc.perform(post("/api/v1/trainings/sessions/1/attendance")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testSubmitAttendanceBadRequest() throws Exception {
        setupManager();

        TrainingAttendanceRequest request = new TrainingAttendanceRequest();
        request.setEmployeeId("2");
        request.setAttendanceDate(LocalDate.of(2026, 6, 15));
        request.setStatus("PRESENT");

        when(trainingService.submitAttendance(eq(1L), any(TrainingAttendanceRequest.class)))
                .thenThrow(new IllegalArgumentException("Enrollment not found"));

        mockMvc.perform(post("/api/v1/trainings/sessions/1/attendance")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("TRN_004"));
    }

    // ── 6. ASSESSMENTS ────────────────────────────────────────────────────────
    @Test
    public void testSubmitAssessmentSuccess() throws Exception {
        setupManager();

        TrainingAssessmentRequest request = new TrainingAssessmentRequest();
        request.setScore(85);
        request.setFeedback("Great performance");

        Map<String, Object> result = Map.of(
                "submissionId", 12L,
                "score", 85,
                "grade", "B",
                "status", "COMPLETED",
                "certificateNumber", "CERT-12345"
        );

        when(trainingService.submitAssessment(eq(10L), any(TrainingAssessmentRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/trainings/assessments/10/submissions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.grade").value("B"));
    }

    @Test
    public void testSubmitAssessmentForbidden() throws Exception {
        setupEmployee();

        TrainingAssessmentRequest request = new TrainingAssessmentRequest();
        request.setScore(85);

        mockMvc.perform(post("/api/v1/trainings/assessments/10/submissions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testSubmitAssessmentBadRequest() throws Exception {
        setupManager();

        TrainingAssessmentRequest request = new TrainingAssessmentRequest();
        request.setScore(85);

        when(trainingService.submitAssessment(eq(10L), any(TrainingAssessmentRequest.class)))
                .thenThrow(new IllegalArgumentException("Enrollment not found"));

        mockMvc.perform(post("/api/v1/trainings/assessments/10/submissions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("TRN_005"));
    }

    // ── 7. CERTIFICATE ────────────────────────────────────────────────────────
    @Test
    public void testDownloadCertificateSuccessByManager() throws Exception {
        setupManager();

        TrainingCertificateResponse response = new TrainingCertificateResponse();
        response.setId(1L);
        response.setEmployeeName("John Doe");
        response.setIssueDate(LocalDate.of(2026, 6, 15));

        when(trainingService.getCertificate(10L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/trainings/certificates/10/download")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeName").value("John Doe"));
    }

    @Test
    public void testDownloadCertificateSuccessBySelf() throws Exception {
        setupEmployee();

        TrainingCertificateResponse response = new TrainingCertificateResponse();
        response.setId(1L);
        response.setEmployeeName("John Doe");
        response.setIssueDate(LocalDate.of(2026, 6, 15));

        when(trainingService.getCertificate(10L)).thenReturn(Optional.of(response));

        Employee emp = new Employee();
        emp.setFullName("John Doe");
        emp.setEmail(empEmail);

        when(employeeRepository.findByEmail(empEmail)).thenReturn(Optional.of(emp));

        mockMvc.perform(get("/api/v1/trainings/certificates/10/download")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testDownloadCertificateForbidden() throws Exception {
        setupEmployee();

        TrainingCertificateResponse response = new TrainingCertificateResponse();
        response.setId(1L);
        response.setEmployeeName("John Doe"); // Certificate is for John Doe
        response.setIssueDate(LocalDate.of(2026, 6, 15));

        when(trainingService.getCertificate(10L)).thenReturn(Optional.of(response));

        Employee emp = new Employee();
        emp.setFullName("Jane Smith"); // Logged in employee is Jane Smith
        emp.setEmail(empEmail);

        when(employeeRepository.findByEmail(empEmail)).thenReturn(Optional.of(emp));

        mockMvc.perform(get("/api/v1/trainings/certificates/10/download")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
    }

    @Test
    public void testDownloadCertificateNotFound() throws Exception {
        setupEmployee();

        when(trainingService.getCertificate(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/trainings/certificates/99/download")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("TRN_006"));
    }

    // ── 8. REPORTS ────────────────────────────────────────────────────────────
    @Test
    public void testGetTrainingsReportSuccess() throws Exception {
        setupManager();

        Map<String, Object> data = Map.of(
                "reportType", "general",
                "totalCoursesCount", 5L
        );

        when(trainingService.getTrainingsReport("general")).thenReturn(data);

        mockMvc.perform(get("/api/v1/trainings/reports/general")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportType").value("general"));
    }

    @Test
    public void testGetTrainingsReportForbidden() throws Exception {
        setupEmployee();

        mockMvc.perform(get("/api/v1/trainings/reports/general")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ── 9. NOTIFICATIONS ──────────────────────────────────────────────────────
    @Test
    public void testSendNotificationSuccess() throws Exception {
        setupManager();

        Map<String, String> body = Map.of("message", "Custom reminder");

        mockMvc.perform(post("/api/v1/trainings/notifications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SENT"))
                .andExpect(jsonPath("$.data.message").value("Custom reminder"));
    }

    @Test
    public void testSendNotificationForbidden() throws Exception {
        setupEmployee();

        Map<String, String> body = Map.of("message", "Custom reminder");

        mockMvc.perform(post("/api/v1/trainings/notifications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }
}
