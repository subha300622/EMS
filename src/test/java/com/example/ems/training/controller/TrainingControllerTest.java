package com.example.ems.training.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.dto.*;
import com.example.ems.training.entity.*;
import com.example.ems.training.service.TrainingManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TrainingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TrainingManagementService trainingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TrainingController trainingController;

    @InjectMocks
    private TrainingLibraryController libraryController;

    @InjectMocks
    private TrainingReportController reportController;

    private User mockUser;
    private final String mockToken = "mock-jwt-token";
    private final String mockEmail = "admin@company.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(trainingController, libraryController, reportController).build();

        mockUser = new User();
        mockUser.setWorkEmail(mockEmail);
        mockUser.setEmployeeId("EMP100");

        when(jwtService.validateAccessToken(mockToken)).thenReturn(true);
        when(jwtService.getEmailFromToken(mockToken)).thenReturn(mockEmail);
        when(userRepository.findByWorkEmail(mockEmail)).thenReturn(Optional.of(mockUser));
    }

    // ── 1. TrainingController Lifecycle & CRUD Tests ─────────────────────────

    @Test
    public void testCreateTraining() throws Exception {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTitle("Java Advanced Concepts");
        request.setCategory("Technical");
        request.setTrainingType(TrainingType.TECHNICAL);
        request.setTrainerId(1L);
        request.setDeliveryMethod(DeliveryMethod.ONLINE);
        request.setStartDateTime(LocalDateTime.now().plusDays(1));
        request.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(2));
        request.setMeetingLink("https://meet.google.com/abc-xyz");

        Training training = new Training();
        training.setId(1L);
        training.setTitle("Java Advanced Concepts");
        training.setStatus(TrainingStatus.PENDING_APPROVAL);

        when(trainingService.createTraining(any(TrainingCreateRequest.class), any(User.class))).thenReturn(training);

        mockMvc.perform(post("/api/v1/trainings")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java Advanced Concepts"));
    }

    @Test
    public void testGetTrainings() throws Exception {
        Training t = new Training();
        t.setId(1L);
        t.setTitle("Git Bootcamp");
        t.setStatus(TrainingStatus.APPROVED);

        when(trainingService.getTrainingsWithFilters(any(), any(), any(), any(User.class)))
                .thenReturn(List.of(t));

        mockMvc.perform(get("/api/v1/trainings")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Git Bootcamp"));
    }

    @Test
    public void testGetTrainingById() throws Exception {
        Training t = new Training();
        t.setId(1L);
        t.setTitle("React Native");

        when(trainingService.getTrainingById(eq(1L), any(User.class))).thenReturn(t);

        mockMvc.perform(get("/api/v1/trainings/1")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("React Native"));
    }

    @Test
    public void testSubmitForApproval() throws Exception {
        Training t = new Training();
        t.setId(1L);
        t.setStatus(TrainingStatus.PENDING_APPROVAL);

        when(trainingService.submitForApproval(eq(1L), any(User.class))).thenReturn(t);

        mockMvc.perform(post("/api/v1/trainings/1/submit")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    public void testApproveTraining() throws Exception {
        Training t = new Training();
        t.setId(1L);
        t.setStatus(TrainingStatus.APPROVED);

        when(trainingService.approveTraining(eq(1L), any(), any(User.class))).thenReturn(t);

        mockMvc.perform(post("/api/v1/trainings/1/approve")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\":\"Looks good\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    public void testRejectTraining() throws Exception {
        Training t = new Training();
        t.setId(1L);
        t.setStatus(TrainingStatus.REJECTED);

        when(trainingService.rejectTraining(eq(1L), any(), any(User.class))).thenReturn(t);

        mockMvc.perform(post("/api/v1/trainings/1/reject")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\":\"Not approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    public void testSendBackTraining() throws Exception {
        Training t = new Training();
        t.setId(1L);
        t.setStatus(TrainingStatus.CHANGES_REQUESTED);

        when(trainingService.sendBackTraining(eq(1L), any(), any(User.class))).thenReturn(t);

        mockMvc.perform(post("/api/v1/trainings/1/send-back")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\":\"Need more details\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHANGES_REQUESTED"));
    }

    @Test
    public void testPublishTraining() throws Exception {
        Training t = new Training();
        t.setId(1L);
        t.setStatus(TrainingStatus.PUBLISHED);

        when(trainingService.publishTraining(eq(1L), any(User.class))).thenReturn(t);

        mockMvc.perform(post("/api/v1/trainings/1/publish")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    public void testCancelTraining() throws Exception {
        Training t = new Training();
        t.setId(1L);
        t.setStatus(TrainingStatus.CANCELLED);

        when(trainingService.cancelTraining(eq(1L), any(), any(User.class))).thenReturn(t);

        mockMvc.perform(post("/api/v1/trainings/1/cancel")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\":\"Scheduling conflict\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    // ── 2. TrainingController Participants & Attendance Tests ──────────────────

    @Test
    public void testAssignUnified() throws Exception {
        TrainingUnifiedAssignmentRequest request = new TrainingUnifiedAssignmentRequest();
        request.setAssignmentType(AssignmentTargetType.DEPARTMENT);
        request.setTargetIds(List.of("1"));
        request.setMandatory(true);

        TrainingParticipant tp = new TrainingParticipant();
        tp.setId(1L);
        tp.setTrainingId(2L);
        tp.setEmployeeId(10L);

        when(trainingService.assignUnified(eq(2L), any(), any(User.class))).thenReturn(List.of(tp));

        mockMvc.perform(post("/api/v1/trainings/2/assignments")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].employeeId").value(10));
    }

    @Test
    public void testRecordAttendance() throws Exception {
        AttendanceBulkMarkRequest request = new AttendanceBulkMarkRequest();
        request.setSessionId(5L);

        AttendanceItemRequest item = new AttendanceItemRequest();
        item.setEmployeeId(10L);
        item.setAttendanceStatus(AttendanceStatus.ATTENDED);
        request.setItems(List.of(item));

        TrainingAttendance ta = new TrainingAttendance();
        ta.setId(1L);
        ta.setEmployeeId(10L);
        ta.setAttendanceStatus(AttendanceStatus.ATTENDED);

        when(trainingService.bulkRecordAttendance(eq(2L), any(), any(User.class))).thenReturn(List.of(ta));

        mockMvc.perform(post("/api/v1/trainings/2/attendance")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].attendanceStatus").value("ATTENDED"));
    }

    @Test
    public void testGetCalendarEvents() throws Exception {
        CalendarEventResponse event = new CalendarEventResponse();
        event.setTrainingId(1L);
        event.setTitle("Spring Boot Training");

        when(trainingService.getCalendarEvents(any(), any(), any(User.class))).thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/trainings/calendar")
                .header("Authorization", "Bearer " + mockToken)
                .param("startDate", "2026-08-01T00:00:00")
                .param("endDate", "2026-08-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring Boot Training"));
    }

    // ── 3. TrainingLibraryController Tests ────────────────────────────────────

    @Test
    public void testCreateLibraryResource() throws Exception {
        LibraryResourceCreateRequest request = new LibraryResourceCreateRequest();
        request.setTitle("Effective Java 3rd Ed");
        request.setCategory("Java");
        request.setTechnology("Backend");
        request.setMaterialType(MaterialType.PDF);
        request.setResourceUrl("https://company-drive.com/effective-java.pdf");

        TrainingLibraryResource resource = new TrainingLibraryResource();
        resource.setId(1L);
        resource.setTitle("Effective Java 3rd Ed");

        when(trainingService.createLibraryResource(any(LibraryResourceCreateRequest.class), any(User.class)))
                .thenReturn(resource);

        mockMvc.perform(post("/api/v1/training-library")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Effective Java 3rd Ed"));
    }

    @Test
    public void testGetLibraryResources() throws Exception {
        TrainingLibraryResource resource = new TrainingLibraryResource();
        resource.setId(1L);
        resource.setTitle("Hibernate Docs");

        when(trainingService.getLibraryResources(any(), any(), any(), any(User.class)))
                .thenReturn(List.of(resource));

        mockMvc.perform(get("/api/v1/training-library")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Hibernate Docs"));
    }

    // ── 4. TrainingReportController Tests ─────────────────────────────────────

    @Test
    public void testGetSummaryReport() throws Exception {
        TrainingReportSummaryResponse summary = new TrainingReportSummaryResponse();
        summary.setTotalTrainings(10L);
        summary.setCompletedTrainings(4L);

        when(trainingService.getReportSummary(any(User.class))).thenReturn(summary);

        mockMvc.perform(get("/api/v1/training-reports/summary")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrainings").value(10))
                .andExpect(jsonPath("$.completedTrainings").value(4));
    }

    @Test
    public void testExportReport() throws Exception {
        AttendanceReportResponse r = new AttendanceReportResponse();
        r.setDepartmentName("Engineering");
        r.setTotalAssigned(5L);
        r.setTotalAttended(4L);
        r.setTotalAbsent(1L);
        r.setCompletionPercentage(80.0);

        when(trainingService.getAttendanceReport(any(User.class))).thenReturn(List.of(r));

        mockMvc.perform(get("/api/v1/training-reports/export")
                .header("Authorization", "Bearer " + mockToken)
                .param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"Engineering\",5,4,1,80.0%")));
    }
}
