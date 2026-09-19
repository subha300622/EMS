package com.example.ems.recruitment.service;

import com.example.ems.audit.service.AuditLogService;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.recruitment.dto.*;
import com.example.ems.recruitment.entity.*;
import com.example.ems.recruitment.repository.*;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RecruitmentModuleComprehensiveTest {

    private static final Long ORG_ID_A = 100L;

    @Mock
    private JobRepository jobRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private CandidateRepository candidateRepository;
    @Mock
    private ApplicationStatusHistoryRepository statusHistoryRepository;
    @Mock
    private InterviewRepository interviewRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private OfferRepository offerRepository;
    @Mock
    private TalentPoolInvitationRepository talentPoolInvitationRepository;
    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ApplicationService mockApplicationService;

    @InjectMocks
    private JobService jobService;
    @InjectMocks
    private ApplicationService applicationService;
    @InjectMocks
    private InterviewService interviewService;
    @InjectMocks
    private OfferService offerService;
    @InjectMocks
    private EmployeeConversionService employeeConversionService;
    @InjectMocks
    private TalentPoolService talentPoolService;
    @InjectMocks
    private RecruitmentDashboardService recruitmentDashboardService;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_ID_A);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ==========================================
    // 1. JOB MANAGEMENT SCENARIOS
    // ==========================================

    @Test
    @DisplayName("JOB-001: Create job with valid data -> DRAFT")
    void testCreateJob_Success() {
        JobCreateRequest req = createSampleJobRequest();
        Job savedJob = new Job();
        savedJob.setId(1L);
        savedJob.setOrganizationId(ORG_ID_A);
        savedJob.setTitle(req.getTitle());
        savedJob.setStatus(JobStatus.DRAFT);

        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        JobResponse res = jobService.createJob(req);
        assertNotNull(res);
        assertEquals(JobStatus.DRAFT, res.getStatus());
        assertEquals("Flutter Developer", res.getTitle());
        verify(jobRepository, times(2)).save(any(Job.class));
    }

    @Test
    @DisplayName("JOB-004: Experience min > max -> BadRequestException")
    void testCreateJob_InvalidExperienceRange() {
        JobCreateRequest req = createSampleJobRequest();
        req.setExperienceMin(5);
        req.setExperienceMax(2);

        assertThrows(BadRequestException.class, () -> jobService.createJob(req));
    }

    @Test
    @DisplayName("JOB-005: Salary min > max -> BadRequestException")
    void testCreateJob_InvalidSalaryRange() {
        JobCreateRequest req = createSampleJobRequest();
        req.setSalaryMin(new BigDecimal("800000"));
        req.setSalaryMax(new BigDecimal("500000"));

        assertThrows(BadRequestException.class, () -> jobService.createJob(req));
    }

    @Test
    @DisplayName("JOB-006 & JOB-007: Openings <= 0 -> BadRequestException")
    void testCreateJob_InvalidOpenings() {
        JobCreateRequest req = createSampleJobRequest();
        req.setOpenings(0);
        assertThrows(BadRequestException.class, () -> jobService.createJob(req));

        req.setOpenings(-3);
        assertThrows(BadRequestException.class, () -> jobService.createJob(req));
    }

    @Test
    @DisplayName("JOB-008: Deadline in the past -> BadRequestException")
    void testCreateJob_PastDeadline() {
        JobCreateRequest req = createSampleJobRequest();
        req.setApplicationDeadline(LocalDate.now().minusDays(1));

        assertThrows(BadRequestException.class, () -> jobService.createJob(req));
    }

    @Test
    @DisplayName("JOB-013: Publish DRAFT job")
    void testPublishJob() {
        Job job = createSampleJob(1L, JobStatus.DRAFT);
        when(jobRepository.findByOrganizationIdAndId(ORG_ID_A, 1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenAnswer(i -> i.getArgument(0));

        JobResponse res = jobService.publishJob(1L);
        assertEquals(JobStatus.PUBLISHED, res.getStatus());
        assertNotNull(res.getPublishedAt());
    }

    @Test
    @DisplayName("JOB-012: Update CLOSED job -> BadRequestException")
    void testUpdateClosedJob_Rejected() {
        Job closedJob = createSampleJob(1L, JobStatus.CLOSED);
        when(jobRepository.findByOrganizationIdAndId(ORG_ID_A, 1L)).thenReturn(Optional.of(closedJob));

        JobUpdateRequest updateReq = new JobUpdateRequest();
        updateReq.setTitle("Updated Title");

        assertThrows(BadRequestException.class, () -> jobService.updateJob(1L, updateReq));
    }

    // ==========================================
    // 2. PUBLIC CAREER PORTAL SCENARIOS
    // ==========================================

    @Test
    @DisplayName("PUB-001 & PUB-008: Get public jobs returns published jobs")
    void testGetPublicJobs() {
        Organization org = new Organization();
        org.setId(ORG_ID_A);
        org.setName("Tech Corp");
        org.setOrganizationCode("tech-corp");
        org.setStatus(OrganizationStatus.ACTIVE);

        Job publishedJob = createSampleJob(1L, JobStatus.PUBLISHED);
        publishedJob.setSlug("flutter-developer");

        when(organizationRepository.findByOrganizationCode("tech-corp")).thenReturn(Optional.of(org));
        when(jobRepository.findByOrganizationIdAndStatus(ORG_ID_A, JobStatus.PUBLISHED)).thenReturn(List.of(publishedJob));

        List<PublicJobResponse> publicJobs = jobService.getPublicJobsForCompany("tech-corp");
        assertEquals(1, publicJobs.size());
        assertEquals("Flutter Developer", publicJobs.get(0).getTitle());
    }

    @Test
    @DisplayName("PUB-002: Invalid company slug -> ResourceNotFoundException")
    void testGetPublicJobs_InvalidCompanySlug() {
        when(organizationRepository.findByOrganizationCode("unknown-org")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.getPublicJobsForCompany("unknown-org"));
    }

    // ==========================================
    // 3. CANDIDATE APPLICATION & DUPLICATE HANDLING
    // ==========================================

    @Test
    @DisplayName("APP-001 & APP-006: New candidate applies to job -> Candidate + Application (APPLIED)")
    void testApplyForJob_NewCandidate_Success() {
        Job publishedJob = createSampleJob(10L, JobStatus.PUBLISHED);
        when(jobRepository.findById(10L)).thenReturn(Optional.of(publishedJob));
        when(candidateRepository.findByOrganizationIdAndEmailOrPhone(anyLong(), anyString(), anyString())).thenReturn(Optional.empty());

        Candidate savedCandidate = new Candidate();
        savedCandidate.setId(50L);
        savedCandidate.setOrganizationId(ORG_ID_A);
        savedCandidate.setEmail("arun@example.com");

        when(candidateRepository.save(any(Candidate.class))).thenReturn(savedCandidate);
        when(applicationRepository.existsByOrganizationIdAndCandidateIdAndJobId(ORG_ID_A, 50L, 10L)).thenReturn(false);

        Application savedApp = new Application();
        savedApp.setId(100L);
        savedApp.setOrganizationId(ORG_ID_A);
        savedApp.setApplicationNumber("APP-10001");
        savedApp.setCandidate(savedCandidate);
        savedApp.setJob(publishedJob);
        savedApp.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.save(any(Application.class))).thenReturn(savedApp);

        JobApplicationRequest appReq = new JobApplicationRequest();
        appReq.setFullName("Arun Kumar");
        appReq.setEmail("arun@example.com");
        appReq.setPhone("9876543210");
        appReq.setExperienceYears(3.0);

        ApplicationResponse res = applicationService.applyForJob(10L, appReq);
        assertNotNull(res);
        assertEquals(ApplicationStatus.APPLIED, res.getStatus());
        assertEquals("APP-10001", res.getApplicationNumber());
    }

    @Test
    @DisplayName("DUP-001: Same candidate + Same job -> ConflictException (ALREADY_APPLIED)")
    void testApplyForJob_DuplicateApplication_Blocked() {
        Job publishedJob = createSampleJob(10L, JobStatus.PUBLISHED);
        Candidate existingCandidate = new Candidate();
        existingCandidate.setId(50L);
        existingCandidate.setOrganizationId(ORG_ID_A);

        when(jobRepository.findById(10L)).thenReturn(Optional.of(publishedJob));
        when(candidateRepository.findByOrganizationIdAndEmailOrPhone(anyLong(), anyString(), anyString())).thenReturn(Optional.of(existingCandidate));
        when(applicationRepository.existsByOrganizationIdAndCandidateIdAndJobId(ORG_ID_A, 50L, 10L)).thenReturn(true);

        JobApplicationRequest appReq = new JobApplicationRequest();
        appReq.setFullName("Arun Kumar");
        appReq.setEmail("arun@example.com");
        appReq.setPhone("9876543210");

        ConflictException ex = assertThrows(ConflictException.class, () ->
                applicationService.applyForJob(10L, appReq));
        assertTrue(ex.getMessage().contains("ALREADY_APPLIED"));
    }

    // ==========================================
    // 4. MULTI-TENANT ISOLATION SCENARIOS
    // ==========================================

    @Test
    @DisplayName("SEC-001: Organization A cannot access Organization B's applications")
    void testMultiTenantIsolation_OrgACannotAccessOrgBApplication() {
        TenantContext.setCurrentTenant(ORG_ID_A);
        when(applicationRepository.findByOrganizationIdAndId(ORG_ID_A, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> applicationService.getApplicationById(999L));
    }

    // ==========================================
    // 5. APPLICATION SHORTLIST & REJECT
    // ==========================================

    @Test
    @DisplayName("ST-002: APPLIED -> SHORTLISTED")
    void testShortlistApplication_Success() {
        Application app = createSampleApplication(100L, ApplicationStatus.APPLIED);
        when(applicationRepository.findByOrganizationIdAndId(ORG_ID_A, 100L)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        ApplicationResponse res = applicationService.shortlistCandidate(100L);
        assertEquals(ApplicationStatus.SHORTLISTED, res.getStatus());
        verify(statusHistoryRepository, times(1)).save(any(ApplicationStatusHistory.class));
    }

    @Test
    @DisplayName("REJ-001: Reject Candidate -> REJECTED & Candidate TalentPool = AVAILABLE")
    void testRejectApplication_CandidateRetainedInTalentPool() {
        Application app = createSampleApplication(100L, ApplicationStatus.SCREENING);
        when(applicationRepository.findByOrganizationIdAndId(ORG_ID_A, 100L)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        ApplicationResponse res = applicationService.rejectCandidate(100L, "Skill mismatch");
        assertEquals(ApplicationStatus.REJECTED, res.getStatus());
        assertEquals(TalentPoolStatus.AVAILABLE, app.getCandidate().getTalentPoolStatus());
    }

    // ==========================================
    // 6. INTERVIEW SCHEDULING & CONFLICT CHECKS
    // ==========================================

    @Test
    @DisplayName("INT-001: Schedule Interview for SHORTLISTED candidate -> INTERVIEW_SCHEDULED")
    void testScheduleInterview_Success() {
        Application app = createSampleApplication(100L, ApplicationStatus.SHORTLISTED);
        when(applicationRepository.findByOrganizationIdAndId(ORG_ID_A, 100L)).thenReturn(Optional.of(app));

        Organization org = new Organization();
        org.setId(ORG_ID_A);

        Employee interviewer = new Employee();
        interviewer.setId(5L);
        interviewer.setOrganization(org);
        interviewer.setFullName("Interviewer Jane");
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(interviewer));

        when(interviewRepository.findConflictingInterviewerSchedule(anyLong(), anyLong(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(interviewRepository.findConflictingCandidateSchedule(anyLong(), anyLong(), any(), any(), any())).thenReturn(Collections.emptyList());

        Interview savedInterview = new Interview();
        savedInterview.setId(200L);
        savedInterview.setOrganizationId(ORG_ID_A);
        savedInterview.setApplication(app);
        savedInterview.setScheduledDate(LocalDate.now().plusDays(2));
        savedInterview.setStartTime(LocalTime.of(10, 0));
        savedInterview.setEndTime(LocalTime.of(11, 0));
        savedInterview.setStatus(InterviewStatus.SCHEDULED);

        when(interviewRepository.save(any(Interview.class))).thenReturn(savedInterview);
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        InterviewScheduleRequest scheduleReq = new InterviewScheduleRequest();
        scheduleReq.setInterviewerId(5L);
        scheduleReq.setInterviewType(InterviewType.TECHNICAL);
        scheduleReq.setScheduledDate(LocalDate.now().plusDays(2));
        scheduleReq.setStartTime(LocalTime.of(10, 0));
        scheduleReq.setEndTime(LocalTime.of(11, 0));

        InterviewResponse res = interviewService.scheduleInterview(100L, scheduleReq);
        assertNotNull(res);
        assertEquals(InterviewStatus.SCHEDULED, res.getStatus());
        assertEquals(ApplicationStatus.INTERVIEW_SCHEDULED, app.getStatus());
    }

    @Test
    @DisplayName("INT-003: Interviewer already booked -> ConflictException")
    void testScheduleInterview_InterviewerConflict() {
        Application app = createSampleApplication(100L, ApplicationStatus.SHORTLISTED);
        when(applicationRepository.findByOrganizationIdAndId(ORG_ID_A, 100L)).thenReturn(Optional.of(app));

        Organization org = new Organization();
        org.setId(ORG_ID_A);

        Employee interviewer = new Employee();
        interviewer.setId(5L);
        interviewer.setOrganization(org);
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(interviewer));

        when(interviewRepository.findConflictingInterviewerSchedule(anyLong(), anyLong(), any(), any(), any()))
                .thenReturn(List.of(new Interview()));

        InterviewScheduleRequest scheduleReq = new InterviewScheduleRequest();
        scheduleReq.setInterviewerId(5L);
        scheduleReq.setScheduledDate(LocalDate.now().plusDays(2));
        scheduleReq.setStartTime(LocalTime.of(10, 0));
        scheduleReq.setEndTime(LocalTime.of(11, 0));

        assertThrows(ConflictException.class, () -> interviewService.scheduleInterview(100L, scheduleReq));
    }

    @Test
    @DisplayName("INT-RATING: Feedback rating > 5 or < 1 -> BadRequestException")
    void testSubmitFeedback_InvalidRating() {
        Interview interview = new Interview();
        interview.setId(200L);
        interview.setOrganizationId(ORG_ID_A);
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setApplication(createSampleApplication(100L, ApplicationStatus.INTERVIEW_SCHEDULED));

        when(interviewRepository.findByOrganizationIdAndId(ORG_ID_A, 200L)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(i -> i.getArgument(0));

        InterviewFeedbackRequest feedbackReq = new InterviewFeedbackRequest();
        feedbackReq.setTechnicalRating(6); // Invalid > 5
        feedbackReq.setOverallRating(4);
        feedbackReq.setRecommendation(InterviewRecommendation.SELECT);

        assertThrows(BadRequestException.class, () -> interviewService.submitFeedback(200L, feedbackReq));
    }

    // ==========================================
    // 7. OFFER GENERATION & ACCEPTANCE
    // ==========================================

    @Test
    @DisplayName("OFF-001: Generate offer for SELECTED candidate -> DRAFT offer")
    void testGenerateOffer_Success() {
        Application app = createSampleApplication(100L, ApplicationStatus.SELECTED);
        when(applicationRepository.findByOrganizationIdAndId(ORG_ID_A, 100L)).thenReturn(Optional.of(app));
        when(offerRepository.existsByOrganizationIdAndApplicationIdAndStatusIn(anyLong(), anyLong(), anyList())).thenReturn(false);

        Offer savedOffer = new Offer();
        savedOffer.setId(300L);
        savedOffer.setOrganizationId(ORG_ID_A);
        savedOffer.setApplication(app);
        savedOffer.setOfferNumber("OFF-10001");
        savedOffer.setDesignation("Software Engineer");
        savedOffer.setAnnualSalary(new BigDecimal("600000"));
        savedOffer.setStatus(OfferStatus.DRAFT);

        when(offerRepository.save(any(Offer.class))).thenReturn(savedOffer);

        OfferGenerateRequest offerReq = new OfferGenerateRequest();
        offerReq.setDesignation("Software Engineer");
        offerReq.setAnnualSalary(new BigDecimal("600000"));
        offerReq.setJoiningDate(LocalDate.now().plusDays(15));

        OfferResponse res = offerService.generateOffer(100L, offerReq);
        assertNotNull(res);
        assertEquals(OfferStatus.DRAFT, res.getStatus());
    }

    @Test
    @DisplayName("OFF-002: Generate offer for APPLIED candidate -> BadRequestException")
    void testGenerateOffer_NotSelected_Rejected() {
        Application app = createSampleApplication(100L, ApplicationStatus.APPLIED);
        when(applicationRepository.findByOrganizationIdAndId(ORG_ID_A, 100L)).thenReturn(Optional.of(app));

        OfferGenerateRequest offerReq = new OfferGenerateRequest();
        offerReq.setDesignation("Software Engineer");
        offerReq.setAnnualSalary(new BigDecimal("600000"));

        assertThrows(BadRequestException.class, () -> offerService.generateOffer(100L, offerReq));
    }

    @Test
    @DisplayName("OFF-ACCEPT: Public offer acceptance via token -> OFFER_ACCEPTED")
    void testAcceptOfferPublic_Success() {
        Application app = createSampleApplication(100L, ApplicationStatus.OFFER_SENT);
        Offer offer = new Offer();
        offer.setId(300L);
        offer.setOrganizationId(ORG_ID_A);
        offer.setApplication(app);
        offer.setStatus(OfferStatus.SENT);
        offer.setAcceptanceToken("valid-token-123");

        when(offerRepository.findByAcceptanceToken("valid-token-123")).thenReturn(Optional.of(offer));
        when(offerRepository.save(any(Offer.class))).thenAnswer(i -> i.getArgument(0));

        OfferResponse res = offerService.acceptOfferPublic("valid-token-123");
        assertEquals(OfferStatus.ACCEPTED, res.getStatus());
        assertEquals(ApplicationStatus.OFFER_ACCEPTED, app.getStatus());
    }

    // ==========================================
    // 8. CANDIDATE -> EMPLOYEE CONVERSION
    // ==========================================

    @Test
    @DisplayName("CONV-001: Convert OFFER_ACCEPTED candidate to Employee -> JOINED")
    void testConvertToEmployee_Success() {
        Application app = createSampleApplication(100L, ApplicationStatus.OFFER_ACCEPTED);
        Candidate candidate = app.getCandidate();

        when(applicationRepository.findByOrganizationIdAndId(ORG_ID_A, 100L)).thenReturn(Optional.of(app));

        Organization org = new Organization();
        org.setId(ORG_ID_A);

        Department dept = new Department();
        dept.setId(10L);
        dept.setName("Engineering");
        dept.setOrganization(org);
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(dept));

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setOrganization(org);
        when(employeeRepository.findByIdAndOrganizationId(2L, ORG_ID_A)).thenReturn(Optional.of(manager));
        when(organizationRepository.findById(ORG_ID_A)).thenReturn(Optional.of(org));

        Offer offer = new Offer();
        offer.setDesignation("Software Engineer");
        offer.setAnnualSalary(new BigDecimal("700000"));
        offer.setJoiningDate(LocalDate.now());
        when(offerRepository.findByOrganizationIdAndApplicationId(ORG_ID_A, 100L)).thenReturn(Optional.of(offer));

        Employee savedEmp = new Employee();
        savedEmp.setId(99L);
        savedEmp.setEmployeeId("EMP-10099");
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmp);
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        CandidateConversionRequest convReq = new CandidateConversionRequest();
        convReq.setDepartmentId(10L);
        convReq.setManagerId(2L);

        ApplicationResponse res = employeeConversionService.convertToEmployee(100L, convReq);
        assertEquals(ApplicationStatus.JOINED, res.getStatus());
        assertEquals(TalentPoolStatus.HIRED, candidate.getTalentPoolStatus());
    }

    // ==========================================
    // 9. DASHBOARD METRICS
    // ==========================================

    @Test
    @DisplayName("DASH-001: Recruitment dashboard metrics calculation")
    void testGetDashboardStats() {
        when(jobRepository.countByOrganizationIdAndStatus(ORG_ID_A, JobStatus.PUBLISHED)).thenReturn(5L);
        when(applicationRepository.countByOrganizationId(ORG_ID_A)).thenReturn(100L);
        when(applicationRepository.countByOrganizationIdAndStatus(ORG_ID_A, ApplicationStatus.JOINED)).thenReturn(10L);

        RecruitmentDashboardResponse res = recruitmentDashboardService.getDashboardStats();
        assertNotNull(res);
        assertEquals(5L, res.getOpenJobs());
        assertEquals(100L, res.getApplications());
        assertEquals(10.0, res.getConversionRate());
    }

    // --- Helper Methods ---

    private JobCreateRequest createSampleJobRequest() {
        JobCreateRequest req = new JobCreateRequest();
        req.setTitle("Flutter Developer");
        req.setLocation("Chennai");
        req.setEmploymentType(EmploymentType.FULL_TIME);
        req.setExperienceMin(1);
        req.setExperienceMax(3);
        req.setOpenings(2);
        req.setSalaryMin(new BigDecimal("400000"));
        req.setSalaryMax(new BigDecimal("700000"));
        req.setDescription("Flutter Dev role");
        req.setApplicationDeadline(LocalDate.now().plusDays(30));
        return req;
    }

    private Job createSampleJob(Long id, JobStatus status) {
        Job job = new Job();
        job.setId(id);
        job.setOrganizationId(ORG_ID_A);
        job.setDepartment("Engineering");
        job.setLocation("Chennai");
        job.setApplicationDeadline(LocalDate.now().plusDays(30));
        job.setTitle("Flutter Developer");
        job.setStatus(status);
        job.setOpenings(2);
        job.setExperienceMin(1);
        job.setExperienceMax(3);
        job.setSalaryMin(new BigDecimal("400000"));
        job.setSalaryMax(new BigDecimal("700000"));
        job.setDescription("Flutter Dev role");
        job.setRequirements("Flutter, Dart");
        return job;
    }

    private Application createSampleApplication(Long id, ApplicationStatus status) {
        Candidate candidate = new Candidate();
        candidate.setId(50L);
        candidate.setOrganizationId(ORG_ID_A);
        candidate.setFullName("Arun Kumar");
        candidate.setEmail("arun@example.com");
        candidate.setPhone("9876543210");
        candidate.setTalentPoolStatus(TalentPoolStatus.AVAILABLE);

        Job job = createSampleJob(10L, JobStatus.PUBLISHED);

        Application app = new Application();
        app.setId(id);
        app.setOrganizationId(ORG_ID_A);
        app.setApplicationNumber("APP-" + id);
        app.setCandidate(candidate);
        app.setJob(job);
        app.setStatus(status);
        app.setAppliedAt(LocalDateTime.now());
        return app;
    }
}
