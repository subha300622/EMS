package com.example.ems.performance.service;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.common.exception.ConflictException;
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
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PerformanceGoldenE2ETest {

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

    @InjectMocks
    private PerformanceWorkflowEventListener workflowEventListener;

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
        ReflectionTestUtils.setField(workflowEventListener, "reviewRepository", reviewRepository);
        ReflectionTestUtils.setField(workflowEventListener, "auditRepository", auditRepository);

        organization = new Organization();
        organization.setId(ORG_ID);

        employee = new Employee();
        employee.setId(10L);
        employee.setFullName("Jane Doe");
        employee.setEmail("jane@example.com");
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
        cycle.setCode("CYC-GOLDEN");
        cycle.setName("Golden Acceptance Cycle");
        cycle.setPeriodType("QUARTERLY");
        cycle.setStartDate(LocalDate.of(2026, 7, 1));
        cycle.setEndDate(LocalDate.of(2026, 9, 30));
        cycle.setFormulaVersion("v1.0");
        cycle.setCalculationVersion(1);

        kpiDef = new PerformanceKpiDefinition();
        kpiDef.setId(50L);
        kpiDef.setOrganization(organization);
        kpiDef.setCycle(cycle);
        kpiDef.setCode("KPI_CORE");
        kpiDef.setName("Delivery Score");
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
    @DisplayName("Golden E2E Flow: Complete Lifecycle + Immutability Verification + 3 Adversarial Attacks")
    void testGoldenE2EAcceptanceFlow() {
        // 1. INITIATE REVIEW -> DRAFT
        PerformanceReviewRecord review = new PerformanceReviewRecord();
        review.setId(777L);
        review.setOrganization(organization);
        review.setCycle(cycle);
        review.setEmployee(employee);
        review.setReviewer(reviewer);
        review.setStatus("DRAFT");

        when(reviewRepository.findByIdAndOrganizationId(777L, ORG_ID)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(PerformanceReviewRecord.class))).thenAnswer(i -> i.getArgument(0));

        // 2. SELF REVIEW -> SELF_REVIEW_SUBMITTED
        EnterpriseSelfReviewRequest selfReq = new EnterpriseSelfReviewRequest(new BigDecimal("85.00"), "Self evaluation");
        EnterprisePerformanceReviewResponse selfResp = reviewService.submitSelfReview(currentUser, 777L, selfReq);
        assertEquals("SELF_REVIEW_SUBMITTED", selfResp.getStatus());

        // 3. MANAGER REVIEW -> MANAGER_REVIEW_SUBMITTED
        EnterpriseManagerReviewRequest mgrReq = new EnterpriseManagerReviewRequest(new BigDecimal("90.00"), "Strong performance");
        EnterprisePerformanceReviewResponse mgrResp = reviewService.submitManagerReview(currentUser, 777L, mgrReq);
        assertEquals("MANAGER_REVIEW_SUBMITTED", mgrResp.getStatus());

        // 4. CALCULATE -> CALCULATED (Snapshot & Run created)
        when(kpiDefinitionRepository.findByIdAndOrganizationId(50L, ORG_ID)).thenReturn(Optional.of(kpiDef));
        when(reviewRepository.saveAndFlush(any(PerformanceReviewRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(calculationRunRepository.saveAndFlush(any(PerformanceCalculationRun.class))).thenAnswer(i -> {
            PerformanceCalculationRun r = i.getArgument(0);
            r.setId(888L);
            return r;
        });

        PerformanceCalculationRequest calcReq = new PerformanceCalculationRequest();
        calcReq.setManagerScore(new BigDecimal("90.00"));
        calcReq.getKpiScores().add(new PerformanceCalculationRequest.KpiScoreInputDto(50L, new BigDecimal("10.00"), "Target achieved"));

        EnterprisePerformanceReviewResponse calcResp = reviewService.calculateReview(currentUser, 777L, calcReq);
        assertEquals("CALCULATED", calcResp.getStatus());
        assertNotNull(calcResp.getSnapshotHash());
        String snapshotHashAtCalculation = calcResp.getSnapshotHash();
        BigDecimal finalScoreAtCalculation = calcResp.getFinalScore();

        // 5. SUBMIT FOR APPROVAL -> APPROVAL_PENDING
        EnterprisePerformanceReviewResponse submitResp = reviewService.submitForApproval(currentUser, 777L, null);
        assertEquals("APPROVAL_PENDING", submitResp.getStatus());

        // 6. COMPLETE DELEGATED APPROVAL -> APPROVED
        when(reviewRepository.findById(777L)).thenReturn(Optional.of(review));
        ApprovalWorkflowCompletedEvent approvalEvent = new ApprovalWorkflowCompletedEvent(
                this,
                "wf-inst-golden-1",
                WorkflowType.PERFORMANCE_REVIEW,
                "PERFORMANCE_REVIEW",
                "777",
                ORG_ID,
                ApprovalStatus.APPROVED
        );
        workflowEventListener.onApprovalWorkflowCompleted(approvalEvent);
        assertEquals("APPROVED", review.getStatus());

        // 7. GATED PUBLISH -> PUBLISHED (Immutable)
        when(kpiDefinitionRepository.findByOrganizationIdAndCycleId(ORG_ID, cycle.getId())).thenReturn(List.of(kpiDef));
        when(kpiScoreRepository.findByOrganizationIdAndReviewIdAndKpiId(ORG_ID, 777L, 50L))
                .thenReturn(Optional.of(new PerformanceReviewKpiScore()));

        EnterprisePerformanceReviewResponse pubResp = reviewService.publishReview(currentUser, 777L, null);
        assertEquals("PUBLISHED", pubResp.getStatus());

        // 8. LOCK -> LOCKED (Terminal)
        EnterprisePerformanceReviewResponse lockResp = reviewService.lockReview(currentUser, 777L, null);
        assertEquals("LOCKED", lockResp.getStatus());

        // 9. ATTEMPT MUTATION -> ConflictException 409 PERFORMANCE_LOCKED
        ConflictException ex = assertThrows(ConflictException.class, () ->
                reviewService.submitManagerReview(currentUser, 777L, mgrReq)
        );
        assertTrue(ex.getMessage().contains("PERFORMANCE_LOCKED"));

        // 10. VERIFY IMMUTABILITY: Underlying data modifications do not touch snapshot or final score
        assertEquals(snapshotHashAtCalculation, review.getSnapshotHash());
        assertEquals(finalScoreAtCalculation, review.getFinalScore());

        // ── 3 ATTACKS ──────────────────────────────────────────────────────────

        // Attack 1: Replay exact same idempotency key + same request payload -> returns cached original response
        String sampleReq = "CALC_PAYLOAD_SAMPLE";
        String sampleHash = idempotencyService.computeRequestHash(sampleReq);
        PerformanceCommandIdempotency cachedCmd = new PerformanceCommandIdempotency();
        cachedCmd.setIdempotencyKey("IDEM-001");
        cachedCmd.setRequestHash(sampleHash);
        cachedCmd.setResponseStatus(200);
        cachedCmd.setResponsePayload("\"CACHED_PAYLOAD_RESPONSE\"");
        cachedCmd.setCompletedAt(LocalDateTime.now());

        when(idempotencyRepository.findByOrganizationIdAndIdempotencyKey(ORG_ID, "IDEM-001"))
                .thenReturn(Optional.of(cachedCmd));

        String cachedResult = idempotencyService.executeWithIdempotency(
                organization, "IDEM-001", "CALCULATE", sampleReq, 777L, String.class, () -> "FAIL_IF_EXECUTED"
        );
        assertEquals("CACHED_PAYLOAD_RESPONSE", cachedResult);

        // Attack 2: Replay same idempotency key with different request payload -> 409 IDEMPOTENCY_KEY_REUSED
        ConflictException reuseEx = assertThrows(ConflictException.class, () ->
                idempotencyService.executeWithIdempotency(
                        organization, "IDEM-001", "CALCULATE", "DIFFERENT_PAYLOAD_ATTACK", 777L, String.class, () -> "FAIL"
                )
        );
        assertTrue(reuseEx.getMessage().contains("IDEMPOTENCY_KEY_REUSED"));

        // Attack 3: Mutation on terminal locked state rejected
        assertThrows(ConflictException.class, () -> reviewService.publishReview(currentUser, 777L, null));
    }
}
