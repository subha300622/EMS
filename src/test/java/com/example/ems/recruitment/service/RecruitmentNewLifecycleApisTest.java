package com.example.ems.recruitment.service;

import com.example.ems.audit.service.AuditLogService;
import com.example.ems.common.exception.BadRequestException;

import com.example.ems.recruitment.dto.*;
import com.example.ems.recruitment.entity.*;
import com.example.ems.recruitment.repository.*;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
 
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RecruitmentNewLifecycleApisTest {

    @Mock private JobRepository jobRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private InterviewRepository interviewRepository;
    @Mock private OfferRepository offerRepository;
    @Mock private CandidateRepository candidateRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private ApplicationService applicationService;

    @InjectMocks private JobService jobService;
    @InjectMocks private InterviewService interviewService;
    @InjectMocks private OfferService offerService;
    @InjectMocks private TalentPoolService talentPoolService;

    private AutoCloseable closeable;
    private final Long TEST_ORG_ID = 100L;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        TenantContext.setCurrentTenant(TEST_ORG_ID);
    }

    @AfterEach
    public void tearDown() throws Exception {
        TenantContext.clear();
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    @DisplayName("Job Reopen: Valid deadline reopens to PUBLISHED")
    public void testReopenJob_ValidDeadline() {
        Job job = new Job();
        job.setId(1L);
        job.setOrganizationId(TEST_ORG_ID);
        job.setStatus(JobStatus.CLOSED);
        job.setApplicationDeadline(LocalDate.now().plusDays(10));

        when(jobRepository.findByOrganizationIdAndId(TEST_ORG_ID, 1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = jobService.reopenJob(1L);
        assertEquals(JobStatus.PUBLISHED, response.getStatus());
    }

    @Test
    @DisplayName("Job Reopen: Expired deadline reopens to DRAFT")
    public void testReopenJob_ExpiredDeadline() {
        Job job = new Job();
        job.setId(1L);
        job.setOrganizationId(TEST_ORG_ID);
        job.setStatus(JobStatus.CLOSED);
        job.setApplicationDeadline(LocalDate.now().minusDays(1));

        when(jobRepository.findByOrganizationIdAndId(TEST_ORG_ID, 1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = jobService.reopenJob(1L);
        assertEquals(JobStatus.DRAFT, response.getStatus());
    }

    @Test
    @DisplayName("Job Delete: Rejects deletion if applications exist")
    public void testDeleteJob_FailsWithApplications() {
        Job job = new Job();
        job.setId(1L);
        job.setOrganizationId(TEST_ORG_ID);
        job.setStatus(JobStatus.CLOSED);

        when(jobRepository.findByOrganizationIdAndId(TEST_ORG_ID, 1L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByOrganizationIdAndJobId(TEST_ORG_ID, 1L)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> jobService.deleteJob(1L));
        assertTrue(ex.getMessage().contains("Cannot delete job with existing candidate applications"));
    }

    @Test
    @DisplayName("Job Duplicate: Clones into DRAFT titled Copy of")
    public void testDuplicateJob() {
        Job original = new Job();
        original.setId(1L);
        original.setOrganizationId(TEST_ORG_ID);
        original.setTitle("Software Engineer");
        original.setStatus(JobStatus.PUBLISHED);

        when(jobRepository.findByOrganizationIdAndId(TEST_ORG_ID, 1L)).thenReturn(Optional.of(original));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            Job j = invocation.getArgument(0);
            if (j.getId() == null) j.setId(2L);
            return j;
        });

        JobResponse response = jobService.duplicateJob(1L);
        assertEquals("Copy of Software Engineer", response.getTitle());
        assertEquals(JobStatus.DRAFT, response.getStatus());
    }

    @Test
    @DisplayName("Interview Cancel: Transitions status to CANCELLED")
    public void testCancelInterview() {
        Interview interview = new Interview();
        interview.setId(10L);
        interview.setOrganizationId(TEST_ORG_ID);
        interview.setStatus(InterviewStatus.SCHEDULED);

        when(interviewRepository.findByOrganizationIdAndId(TEST_ORG_ID, 10L)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(i -> i.getArgument(0));

        InterviewResponse response = interviewService.cancelInterview(10L);
        assertEquals(InterviewStatus.CANCELLED, response.getStatus());
    }

    @Test
    @DisplayName("Offer Withdraw: Transitions DRAFT or SENT offer to WITHDRAWN")
    public void testWithdrawOffer() {
        Offer offer = new Offer();
        offer.setId(5L);
        offer.setOrganizationId(TEST_ORG_ID);
        offer.setStatus(OfferStatus.SENT);
        offer.setOfferNumber("OFFER-123");

        when(offerRepository.findByOrganizationIdAndId(TEST_ORG_ID, 5L)).thenReturn(Optional.of(offer));
        when(offerRepository.save(any(Offer.class))).thenAnswer(i -> i.getArgument(0));

        OfferResponse response = offerService.withdrawOffer(5L);
        assertEquals(OfferStatus.WITHDRAWN, response.getStatus());
    }

    @Test
    @DisplayName("Offer Decline Public: Public candidate token decline transitions status to REJECTED")
    public void testDeclineOfferPublic() {
        Candidate candidate = new Candidate();
        candidate.setEmail("candidate@example.com");

        Application app = new Application();
        app.setId(100L);
        app.setStatus(ApplicationStatus.OFFER_SENT);
        app.setCandidate(candidate);

        Offer offer = new Offer();
        offer.setId(5L);
        offer.setAcceptanceToken("valid-token");
        offer.setStatus(OfferStatus.SENT);
        offer.setOfferNumber("OFFER-123");
        offer.setApplication(app);

        when(offerRepository.findByAcceptanceToken("valid-token")).thenReturn(Optional.of(offer));
        when(offerRepository.save(any(Offer.class))).thenAnswer(i -> i.getArgument(0));

        OfferResponse response = offerService.declineOfferPublic("valid-token");
        assertEquals(OfferStatus.REJECTED, response.getStatus());
    }

    @Test
    @DisplayName("Talent Pool Add Candidate: Creates candidate with AVAILABLE status")
    public void testAddCandidate() {
        CandidateCreateRequest req = new CandidateCreateRequest();
        req.setFullName("John Doe");
        req.setEmail("john.doe@example.com");
        req.setExperienceYears(3.5);

        when(candidateRepository.findByOrganizationIdAndEmail(TEST_ORG_ID, "john.doe@example.com")).thenReturn(Optional.empty());
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(i -> {
            Candidate c = i.getArgument(0);
            c.setId(1001L);
            return c;
        });

        TalentPoolCandidateResponse response = talentPoolService.addCandidate(req);
        assertEquals("John Doe", response.getFullName());
        assertEquals(TalentPoolStatus.AVAILABLE, response.getTalentPoolStatus());
    }
}
