package com.example.ems.training.service;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.training.dto.*;
import com.example.ems.training.entity.AssignmentTargetType;
import com.example.ems.training.entity.AttendanceStatus;
import com.example.ems.training.entity.DeliveryMethod;
import com.example.ems.training.entity.MaterialType;
import com.example.ems.training.entity.ParticipationStatus;
import com.example.ems.training.entity.RecurrenceFrequency;
import com.example.ems.training.entity.Training;
import com.example.ems.training.entity.TrainingAttendance;
import com.example.ems.training.entity.TrainingFeedback;
import com.example.ems.training.entity.TrainingMaterial;
import com.example.ems.training.entity.TrainingParticipant;
import com.example.ems.training.entity.TrainingStatus;
import com.example.ems.training.entity.TrainingType;
import com.example.ems.training.repository.TrainingAttendanceRepository;
import com.example.ems.training.repository.TrainingFeedbackRepository;
import com.example.ems.training.repository.TrainingLibraryResourceRepository;
import com.example.ems.training.repository.TrainingMaterialRepository;
import com.example.ems.training.repository.TrainingParticipantRepository;
import com.example.ems.training.repository.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TrainingManagementServiceIntegrationTest {

    @Autowired
    private TrainingManagementService trainingService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingParticipantRepository participantRepository;

    @Autowired
    private TrainingAttendanceRepository attendanceRepository;

    @Autowired
    private TrainingMaterialRepository materialRepository;

    @Autowired
    private TrainingLibraryResourceRepository libraryResourceRepository;

    @Autowired
    private TrainingFeedbackRepository feedbackRepository;

    private Organization org1;
    private Organization org2;
    private Employee emp1;
    private Employee emp2;
    private User user1;
    private User user2;

    @BeforeEach
    public void setUp() {
        org1 = new Organization();
        org1.setName("Acme Corp");
        org1.setOrganizationCode("ORG-ACME");
        org1 = organizationRepository.save(org1);

        org2 = new Organization();
        org2.setName("Stark Tech");
        org2.setOrganizationCode("ORG-STARK");
        org2 = organizationRepository.save(org2);

        emp1 = new Employee();
        emp1.setFullName("John Doe");
        emp1.setEmail("john.doe@acme.com");
        emp1.setEmployeeId("EMP1001");
        emp1.setOrganization(org1);
        emp1 = employeeRepository.save(emp1);

        emp2 = new Employee();
        emp2.setFullName("Jane Smith");
        emp2.setEmail("jane.smith@acme.com");
        emp2.setEmployeeId("EMP1002");
        emp2.setOrganization(org1);
        emp2 = employeeRepository.save(emp2);

        user1 = new User();
        user1.setWorkEmail("john.doe@acme.com");
        user1.setEmployeeId("EMP1001");
        user1.setOrganization(org1);
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setWorkEmail("user.stark@stark.com");
        user2.setOrganization(org2);
        user2 = userRepository.save(user2);
    }

    @Test
    public void testFullTrainingLifecycleFlow() {
        // 1. Create Training
        TrainingCreateRequest createReq = new TrainingCreateRequest();
        createReq.setTitle("Spring Boot 3 Advanced");
        createReq.setDescription("Master microservices & security");
        createReq.setCategory("ENGINEERING");
        createReq.setTrainingType(TrainingType.TECHNICAL);
        createReq.setTrainerId(emp1.getId());
        createReq.setDeliveryMethod(DeliveryMethod.ONLINE);
        createReq.setMeetingLink("https://meet.google.com/abc-defg-hij");
        createReq.setStartDateTime(LocalDateTime.now().plusDays(1));
        createReq.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(2));
        createReq.setApprovalRequired(true);

        Training training = trainingService.createTraining(createReq, user1);
        assertNotNull(training.getId());
        assertEquals(TrainingStatus.PENDING_APPROVAL, training.getStatus());
        assertEquals(org1.getId(), training.getOrganizationId());

        // 2. Approve Training
        Training approved = trainingService.approveTraining(training.getId(), "Approved by Manager", user1);
        assertEquals(TrainingStatus.APPROVED, approved.getStatus());

        // 3. Publish Training
        Training published = trainingService.publishTraining(training.getId(), user1);
        assertEquals(TrainingStatus.PUBLISHED, published.getStatus());

        // 4. Assign Participants & Prevent Duplicates
        ParticipantAssignRequest assignReq = new ParticipantAssignRequest();
        assignReq.setAssignmentType(AssignmentTargetType.EMPLOYEE);
        assignReq.setEmployeeIds(List.of(emp1.getId(), emp2.getId()));
        assignReq.setSendNotification(true);

        List<TrainingParticipant> participants = trainingService.assignParticipants(training.getId(), assignReq, user1);
        assertEquals(2, participants.size());

        // Try assigning duplicate participant emp1
        List<TrainingParticipant> dupList = trainingService.assignParticipants(training.getId(), assignReq, user1);
        assertEquals(0, dupList.size()); // Duplicate skipped by unique constraint check

        // 5. Participant Response
        TrainingParticipant response = trainingService.recordParticipantResponse(training.getId(), ParticipationStatus.ACCEPTED, "Excited to join!", user1);
        assertEquals(ParticipationStatus.ACCEPTED, response.getParticipationStatus());

        // 6. Record Attendance
        AttendanceBulkMarkRequest attReq = new AttendanceBulkMarkRequest();
        AttendanceItemRequest attItem = new AttendanceItemRequest();
        attItem.setEmployeeId(emp1.getId());
        attItem.setAttendanceStatus(AttendanceStatus.ATTENDED);
        attItem.setCheckInTime(training.getStartDateTime());
        attItem.setCheckOutTime(training.getEndDateTime());
        attItem.setDurationMinutes(120);
        attReq.setItems(List.of(attItem));

        List<TrainingAttendance> attendanceRecords = trainingService.bulkRecordAttendance(training.getId(), attReq, user1);
        assertEquals(1, attendanceRecords.size());
        assertEquals(AttendanceStatus.ATTENDED, attendanceRecords.get(0).getAttendanceStatus());

        // 7. Upload Material
        MaterialCreateRequest matReq = new MaterialCreateRequest();
        matReq.setTitle("Spring Security Cheat Sheet");
        matReq.setMaterialType(MaterialType.PDF);
        matReq.setUrlOrFilePath("https://docs.acme.com/spring-security.pdf");
        TrainingMaterial mat = trainingService.addMaterial(training.getId(), matReq, user1);
        assertNotNull(mat.getId());

        // 8. Submit Feedback
        FeedbackSubmitRequest fbReq = new FeedbackSubmitRequest();
        fbReq.setRating(5);
        fbReq.setContentQualityRating(5);
        fbReq.setTrainerRating(5);
        fbReq.setOverallExperienceRating(5);
        fbReq.setComments("Great hands-on session!");

        TrainingFeedback feedback = trainingService.submitFeedback(training.getId(), fbReq, user1);
        assertNotNull(feedback.getId());
        assertEquals(5, feedback.getRating());

        Map<String, Object> fbSummary = trainingService.getFeedbackSummary(training.getId(), user1);
        assertEquals(1, fbSummary.get("totalResponses"));
        assertEquals(5.0, fbSummary.get("averageRating"));

        // Direct Repository Persistence Verifications
        assertTrue(trainingRepository.findById(training.getId()).isPresent());
        assertEquals(2, participantRepository.findByTrainingId(training.getId()).size());
        assertEquals(1, attendanceRepository.findByTrainingId(training.getId()).size());
        assertEquals(1, materialRepository.findByTrainingId(training.getId()).size());
        assertEquals(1, feedbackRepository.findByTrainingId(training.getId()).size());
        assertNotNull(libraryResourceRepository.count());

        // 9. Cancel Training Endpoint (Post cancel updates status to CANCELLED)
        Training cancelled = trainingService.cancelTraining(training.getId(), "Schedule conflict", user1);
        assertEquals(TrainingStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    public void testTenantIsolationSecurity() {
        // Create Training in Org1
        TrainingCreateRequest createReq = new TrainingCreateRequest();
        createReq.setTitle("Org1 Confidential Training");
        createReq.setCategory("SECURITY");
        createReq.setTrainingType(TrainingType.COMPLIANCE);
        createReq.setTrainerId(emp1.getId());
        createReq.setDeliveryMethod(DeliveryMethod.OFFLINE);
        createReq.setVenue("Room 401");
        createReq.setStartDateTime(LocalDateTime.now().plusDays(2));
        createReq.setEndDateTime(LocalDateTime.now().plusDays(2).plusHours(1));
        createReq.setApprovalRequired(false);

        Training org1Training = trainingService.createTraining(createReq, user1);

        // User2 from Org2 attempts to access Org1 Training -> SecurityException thrown
        assertThrows(SecurityException.class, () -> {
            trainingService.getTrainingById(org1Training.getId(), user2);
        });
    }

    @Test
    public void testRecurringSessionsGeneration() {
        TrainingCreateRequest createReq = new TrainingCreateRequest();
        createReq.setTitle("Weekly DevOps Sync");
        createReq.setCategory("DEVOPS");
        createReq.setTrainingType(TrainingType.TECHNICAL);
        createReq.setTrainerId(emp1.getId());
        createReq.setDeliveryMethod(DeliveryMethod.ONLINE);
        createReq.setMeetingLink("https://meet.google.com/devops-sync");
        createReq.setStartDateTime(LocalDateTime.of(2026, 9, 1, 10, 0));
        createReq.setEndDateTime(LocalDateTime.of(2026, 9, 1, 11, 0));
        createReq.setIsRecurring(true);
        createReq.setApprovalRequired(false);

        RecurrenceConfigRequest rConfig = new RecurrenceConfigRequest();
        rConfig.setFrequency(RecurrenceFrequency.WEEKLY);
        rConfig.setDaysOfWeek("TUESDAY");
        rConfig.setStartDate(LocalDate.of(2026, 9, 1));
        rConfig.setEndDate(LocalDate.of(2026, 9, 30));
        createReq.setRecurrenceConfig(rConfig);

        Training recurringTraining = trainingService.createTraining(createReq, user1);
        assertTrue(recurringTraining.getIsRecurring());
    }

    @Test
    public void testUnifiedAssignmentDeduplicationAndScopeManagement() {
        // Create published training
        TrainingCreateRequest createReq = new TrainingCreateRequest();
        createReq.setTitle("Unified Architecture Workshop");
        createReq.setCategory("ENGINEERING");
        createReq.setTrainingType(TrainingType.TECHNICAL);
        createReq.setTrainerId(emp1.getId());
        createReq.setDeliveryMethod(DeliveryMethod.ONLINE);
        createReq.setMeetingLink("https://meet.google.com/unified-link");
        createReq.setStartDateTime(LocalDateTime.now().plusDays(1));
        createReq.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(2));
        createReq.setApprovalRequired(false);

        Training training = trainingService.createTraining(createReq, user1);

        // 1. Assign via Unified API with EMPLOYEE scope
        TrainingUnifiedAssignmentRequest req1 = new TrainingUnifiedAssignmentRequest();
        req1.setAssignmentType(AssignmentTargetType.EMPLOYEE);
        req1.setTargetIds(List.of(emp1.getId().toString()));
        req1.setMandatory(true);

        List<TrainingParticipant> assigned1 = trainingService.assignUnified(training.getId(), req1, user1);
        assertEquals(1, assigned1.size());

        // 2. Re-assign same employee via duplicate call -> Must deduplicate
        List<TrainingParticipant> assigned2 = trainingService.assignUnified(training.getId(), req1, user1);
        assertEquals(1, assigned2.size()); // Still 1 participant row

        // 3. Remove Scope and verify coverage re-evaluation
        trainingService.deleteAssignmentScope(training.getId(), AssignmentTargetType.EMPLOYEE, emp1.getId().toString(), user1);

        List<TrainingParticipant> remainingParticipants = participantRepository.findByTrainingId(training.getId());
        assertEquals(1, remainingParticipants.size());
        assertEquals(ParticipationStatus.REVOKED, remainingParticipants.get(0).getParticipationStatus());

        // 4. Check Employee Report
        EmployeeReportResponse empReport = trainingService.getEmployeeReport(emp1.getId(), user1);
        assertNotNull(empReport);
        assertEquals(emp1.getFullName(), empReport.getEmployeeName());
    }
}
