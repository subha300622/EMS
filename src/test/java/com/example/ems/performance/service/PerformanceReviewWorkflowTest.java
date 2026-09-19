package com.example.ems.performance.service;

import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.performance.dto.*;
import com.example.ems.performance.entity.*;
import com.example.ems.performance.repository.*;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PerformanceReviewWorkflowTest {

    @Mock private PerformanceReviewRecordRepository reviewRepository;
    @Mock private PerformanceReviewCycleRepository cycleRepository;
    @Mock private PerformanceKpiDefinitionRepository kpiDefinitionRepository;
    @Mock private PerformanceReviewKpiScoreRepository kpiScoreRepository;
    @Mock private PerformanceCalculationRunRepository calculationRunRepository;
    @Mock private PerformanceReviewAuditRepository auditRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private PerformanceCommandIdempotencyRepository idempotencyRepository;
    @Mock private ApprovalWorkflowEngineService approvalWorkflowEngineService;

    @Spy private PerformanceReviewStateMachine stateMachine = new PerformanceReviewStateMachine();
    @Spy private PerformanceCalculationEngine calculationEngine = new PerformanceCalculationEngine();
    @Spy private PerformanceSnapshotService snapshotService = new PerformanceSnapshotService();
    @Spy private PerformanceIdempotencyService idempotencyService = new PerformanceIdempotencyService();
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PerformanceReviewService reviewService;

    private Organization organization;
    private Employee employee;
    private Employee reviewer;
    private User currentUser;
    private PerformanceReviewCycle cycle;
    private PerformanceKpiDefinition kpiDef;
    private static final Long ORG_ID = 100L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(ORG_ID);

        ReflectionTestUtils.setField(idempotencyService, "idempotencyRepository", idempotencyRepository);
        ReflectionTestUtils.setField(idempotencyService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(snapshotService, "kpiScoreRepository", kpiScoreRepository);

        organization = new Organization();
        organization.setId(ORG_ID);

        employee = new Employee();
        employee.setId(10L);
        employee.setFullName("John Doe");
        employee.setEmail("john.doe@example.com");
        employee.setOrganization(organization);

        reviewer = new Employee();
        reviewer.setId(20L);
        reviewer.setFullName("Manager Bob");
        reviewer.setEmail("bob@example.com");
        reviewer.setOrganization(organization);

        Role role = new Role();
        role.setName("ROLE_MANAGER");
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setWorkEmail("bob@example.com");
        currentUser.setFullName("Manager Bob");
        currentUser.setRole(role);
        currentUser.setOrganization(organization);

        cycle = new PerformanceReviewCycle();
        cycle.setId(1L);
        cycle.setOrganization(organization);
        cycle.setCode("CYC-2026-Q1");
        cycle.setName("Q1 2026 Appraisal");
        cycle.setPeriodType("QUARTERLY");
        cycle.setStartDate(LocalDate.of(2026, 1, 1));
        cycle.setEndDate(LocalDate.of(2026, 3, 31));
        cycle.setFormulaVersion("v1.0");
        cycle.setCalculationVersion(1);

        kpiDef = new PerformanceKpiDefinition();
        kpiDef.setId(50L);
        kpiDef.setOrganization(organization);
        kpiDef.setCycle(cycle);
        kpiDef.setCode("KPI_DEV");
        kpiDef.setName("Feature Delivery");
        kpiDef.setCategory("TECHNICAL");
        kpiDef.setMeasurementType("HIGHER_IS_BETTER");
        kpiDef.setDefaultWeight(new BigDecimal("100.00"));
        kpiDef.setMinThreshold(BigDecimal.ZERO);
        kpiDef.setMaxThreshold(new BigDecimal("100.00"));
        kpiDef.setTargetValue(new BigDecimal("10.00"));
        kpiDef.setIsMandatory(true);
        kpiDef.setStatus("ACTIVE");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Initiate Review: creates review record in DRAFT status")
    void testInitiateReview_DraftCreated() {
        when(cycleRepository.findByIdAndOrganizationId(1L, ORG_ID)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(20L)).thenReturn(Optional.of(reviewer));
        when(reviewRepository.findByOrganizationIdAndCycleIdAndEmployeeId(ORG_ID, 1L, 10L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(PerformanceReviewRecord.class))).thenAnswer(i -> {
            PerformanceReviewRecord r = i.getArgument(0);
            r.setId(500L);
            return r;
        });

        EnterprisePerformanceReviewResponse response = reviewService.initiateReview(currentUser, 1L, 10L, 20L);
        assertNotNull(response);
        assertEquals("DRAFT", response.getStatus());
        assertEquals(10L, response.getEmployeeId());
        assertEquals(20L, response.getReviewerId());
    }

    @Test
    @DisplayName("Self Review: transitions review to SELF_REVIEW_SUBMITTED")
    void testSelfReviewSubmission_TransitionToSelfSubmitted() {
        PerformanceReviewRecord review = new PerformanceReviewRecord();
        review.setId(500L);
        review.setOrganization(organization);
        review.setCycle(cycle);
        review.setEmployee(employee);
        review.setStatus("DRAFT");

        when(reviewRepository.findByIdAndOrganizationId(500L, ORG_ID)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(PerformanceReviewRecord.class))).thenAnswer(i -> i.getArgument(0));

        EnterpriseSelfReviewRequest request = new EnterpriseSelfReviewRequest(new BigDecimal("85.00"), "Good progress on core deliverables");
        EnterprisePerformanceReviewResponse response = reviewService.submitSelfReview(currentUser, 500L, request);

        assertEquals("SELF_REVIEW_SUBMITTED", response.getStatus());
        assertEquals(new BigDecimal("85.00"), response.getSelfScore());
    }

    @Test
    @DisplayName("Manager Review: transitions review to MANAGER_REVIEW_SUBMITTED")
    void testManagerReviewSubmission_TransitionToManagerSubmitted() {
        PerformanceReviewRecord review = new PerformanceReviewRecord();
        review.setId(500L);
        review.setOrganization(organization);
        review.setCycle(cycle);
        review.setEmployee(employee);
        review.setStatus("SELF_REVIEW_SUBMITTED");

        when(reviewRepository.findByIdAndOrganizationId(500L, ORG_ID)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(PerformanceReviewRecord.class))).thenAnswer(i -> i.getArgument(0));

        EnterpriseManagerReviewRequest request = new EnterpriseManagerReviewRequest(new BigDecimal("90.00"), "Strong execution");
        EnterprisePerformanceReviewResponse response = reviewService.submitManagerReview(currentUser, 500L, request);

        assertEquals("MANAGER_REVIEW_SUBMITTED", response.getStatus());
        assertEquals(new BigDecimal("90.00"), response.getManagerScore());
    }

    @Test
    @DisplayName("Calculate Review: evaluates KPIs, creates run, attaches snapshot and transitions to CALCULATED")
    void testCalculateReview_ComputesScore_CreatesRunAndSnapshot() {
        PerformanceReviewRecord review = new PerformanceReviewRecord();
        review.setId(500L);
        review.setOrganization(organization);
        review.setCycle(cycle);
        review.setEmployee(employee);
        review.setStatus("MANAGER_REVIEW_SUBMITTED");
        review.setSelfScore(new BigDecimal("80.00"));
        review.setManagerScore(new BigDecimal("90.00"));
        review.setCalculationVersion(1);

        when(reviewRepository.findByIdAndOrganizationId(500L, ORG_ID)).thenReturn(Optional.of(review));
        when(kpiDefinitionRepository.findByIdAndOrganizationId(50L, ORG_ID)).thenReturn(Optional.of(kpiDef));
        when(reviewRepository.saveAndFlush(any(PerformanceReviewRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(reviewRepository.save(any(PerformanceReviewRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(calculationRunRepository.saveAndFlush(any(PerformanceCalculationRun.class))).thenAnswer(i -> {
            PerformanceCalculationRun run = i.getArgument(0);
            run.setId(1001L);
            return run;
        });

        PerformanceCalculationRequest request = new PerformanceCalculationRequest();
        request.setManagerScore(new BigDecimal("90.00"));
        request.getKpiScores().add(new PerformanceCalculationRequest.KpiScoreInputDto(50L, new BigDecimal("10.00"), "Met 100%"));

        EnterprisePerformanceReviewResponse response = reviewService.calculateReview(currentUser, 500L, request);

        assertEquals("CALCULATED", response.getStatus());
        assertNotNull(response.getCalculatedScore());
        assertNotNull(response.getRatingBand());
        assertNotNull(response.getSnapshotHash());
        verify(calculationRunRepository, times(1)).saveAndFlush(any(PerformanceCalculationRun.class));
    }

    @Test
    @DisplayName("Submit For Approval: transitions to APPROVAL_PENDING and initiates approval engine")
    void testSubmitForApproval_InitiatesDelegatedApprovalWorkflow() {
        PerformanceReviewRecord review = new PerformanceReviewRecord();
        review.setId(500L);
        review.setOrganization(organization);
        review.setCycle(cycle);
        review.setEmployee(employee);
        review.setStatus("CALCULATED");
        review.setFinalScore(new BigDecimal("88.00"));

        when(reviewRepository.findByIdAndOrganizationId(500L, ORG_ID)).thenReturn(Optional.of(review));
        when(reviewRepository.saveAndFlush(any(PerformanceReviewRecord.class))).thenAnswer(i -> i.getArgument(0));

        EnterprisePerformanceReviewResponse response = reviewService.submitForApproval(currentUser, 500L, null);

        assertEquals("APPROVAL_PENDING", response.getStatus());
        verify(approvalWorkflowEngineService, times(1)).startWorkflow(
                eq(WorkflowType.PERFORMANCE_REVIEW),
                eq("PERFORMANCE_REVIEW"),
                eq("500"),
                eq(employee),
                any()
        );
    }

    @Test
    @DisplayName("Gated Publish: fails if review status is not APPROVED")
    void testGatedPublish_FailsIfNotApproved() {
        PerformanceReviewRecord review = new PerformanceReviewRecord();
        review.setId(500L);
        review.setOrganization(organization);
        review.setCycle(cycle);
        review.setEmployee(employee);
        review.setStatus("CALCULATED"); // Not approved

        when(reviewRepository.findByIdAndOrganizationId(500L, ORG_ID)).thenReturn(Optional.of(review));

        assertThrows(IllegalStateException.class, () -> reviewService.publishReview(currentUser, 500L, null));
    }

    @Test
    @DisplayName("Gated Publish: succeeds when approved, snapshot valid and mandatory KPIs scored")
    void testGatedPublish_SuccessWhenApproved() {
        PerformanceReviewRecord review = new PerformanceReviewRecord();
        review.setId(500L);
        review.setOrganization(organization);
        review.setCycle(cycle);
        review.setEmployee(employee);
        review.setStatus("APPROVED");
        review.setFinalScore(new BigDecimal("88.00"));

        PerformanceSnapshotDto snapDto = new PerformanceSnapshotDto();
        snapDto.getEmployee().setEmployeeId(10L);
        String snapJson = snapshotService.serializeToJson(snapDto);
        String snapHash = snapshotService.calculateSha256(snapJson);
        review.setSnapshotData(snapJson);
        review.setSnapshotHash(snapHash);

        when(reviewRepository.findByIdAndOrganizationId(500L, ORG_ID)).thenReturn(Optional.of(review));
        when(kpiDefinitionRepository.findByOrganizationIdAndCycleId(ORG_ID, cycle.getId())).thenReturn(List.of(kpiDef));
        when(kpiScoreRepository.findByOrganizationIdAndReviewIdAndKpiId(ORG_ID, 500L, 50L))
                .thenReturn(Optional.of(new PerformanceReviewKpiScore()));
        when(reviewRepository.save(any(PerformanceReviewRecord.class))).thenAnswer(i -> i.getArgument(0));

        EnterprisePerformanceReviewResponse response = reviewService.publishReview(currentUser, 500L, null);
        assertEquals("PUBLISHED", response.getStatus());
        assertNotNull(response.getPublishedAt());
    }

    @Test
    @DisplayName("Lock Review: transitions PUBLISHED review to terminal LOCKED status")
    void testLockReview_TransitionsToLocked() {
        PerformanceReviewRecord review = new PerformanceReviewRecord();
        review.setId(500L);
        review.setOrganization(organization);
        review.setCycle(cycle);
        review.setEmployee(employee);
        review.setStatus("PUBLISHED");
        review.setFinalScore(new BigDecimal("88.00"));

        when(reviewRepository.findByIdAndOrganizationId(500L, ORG_ID)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(PerformanceReviewRecord.class))).thenAnswer(i -> i.getArgument(0));

        EnterprisePerformanceReviewResponse response = reviewService.lockReview(currentUser, 500L, null);
        assertEquals("LOCKED", response.getStatus());
        assertNotNull(response.getLockedAt());
    }

    @Test
    @DisplayName("Reopen Review: transitions REJECTED review to MANAGER_REVIEW_SUBMITTED")
    void testReopenReview_Success() {
        PerformanceReviewRecord review = new PerformanceReviewRecord();
        review.setId(500L);
        review.setOrganization(organization);
        review.setCycle(cycle);
        review.setEmployee(employee);
        review.setStatus("REJECTED");
        review.setRejectionReason("KPI targets need adjustment");

        when(reviewRepository.findByIdAndOrganizationId(500L, ORG_ID)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(PerformanceReviewRecord.class))).thenAnswer(i -> i.getArgument(0));

        ReopenReviewRequest request = new ReopenReviewRequest("Adjusting score with new project data");
        EnterprisePerformanceReviewResponse response = reviewService.reopenReview(currentUser, 500L, request);

        assertEquals("MANAGER_REVIEW_SUBMITTED", response.getStatus());
        assertNotNull(response.getReopenedAt());
    }
}
