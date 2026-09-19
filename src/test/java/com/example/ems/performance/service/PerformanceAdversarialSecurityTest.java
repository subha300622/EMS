package com.example.ems.performance.service;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.performance.dto.EnterpriseManagerReviewRequest;
import com.example.ems.performance.dto.PerformanceCalculationRequest;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PerformanceAdversarialSecurityTest {

    @Mock private PerformanceReviewRecordRepository reviewRepository;
    @Mock private PerformanceReviewCycleRepository cycleRepository;
    @Mock private PerformanceKpiDefinitionRepository kpiDefinitionRepository;
    @Mock private PerformanceReviewKpiScoreRepository kpiScoreRepository;
    @Mock private PerformanceCalculationRunRepository calculationRunRepository;
    @Mock private PerformanceReviewAuditRepository auditRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private PerformanceCommandIdempotencyRepository idempotencyRepository;

    @Spy private PerformanceReviewStateMachine stateMachine = new PerformanceReviewStateMachine();
    @Spy private PerformanceCalculationEngine calculationEngine = new PerformanceCalculationEngine();
    @Spy private PerformanceSnapshotService snapshotService = new PerformanceSnapshotService();
    @Spy private PerformanceIdempotencyService idempotencyService = new PerformanceIdempotencyService();
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PerformanceReviewService reviewService;

    @InjectMocks
    private PerformanceWorkflowEventListener workflowEventListener;

    private Organization org1;
    private Organization org2;
    private Employee employeeOrg1;
    private PerformanceReviewCycle cycleOrg1;
    private User userOrg1;

    private static final Long TENANT_1 = 100L;
    private static final Long TENANT_2 = 200L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(TENANT_1);

        ReflectionTestUtils.setField(idempotencyService, "idempotencyRepository", idempotencyRepository);
        ReflectionTestUtils.setField(idempotencyService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(snapshotService, "kpiScoreRepository", kpiScoreRepository);
        ReflectionTestUtils.setField(workflowEventListener, "reviewRepository", reviewRepository);
        ReflectionTestUtils.setField(workflowEventListener, "auditRepository", auditRepository);

        org1 = new Organization();
        org1.setId(TENANT_1);

        org2 = new Organization();
        org2.setId(TENANT_2);

        employeeOrg1 = new Employee();
        employeeOrg1.setId(10L);
        employeeOrg1.setFullName("Alice Org1");
        employeeOrg1.setEmail("alice@org1.com");
        employeeOrg1.setOrganization(org1);

        Role role = new Role();
        role.setName("ROLE_MANAGER");
        userOrg1 = new User();
        userOrg1.setId(1L);
        userOrg1.setWorkEmail("alice@org1.com");
        userOrg1.setFullName("Alice Org1");
        userOrg1.setRole(role);
        userOrg1.setOrganization(org1);

        cycleOrg1 = new PerformanceReviewCycle();
        cycleOrg1.setId(1L);
        cycleOrg1.setOrganization(org1);
        cycleOrg1.setCode("CYC-1");
        cycleOrg1.setFormulaVersion("v1.0");
        cycleOrg1.setStartDate(LocalDate.now().minusMonths(1));
        cycleOrg1.setEndDate(LocalDate.now());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Cross-Tenant Isolation: accessing review from different tenant returns 404 / ResourceNotFoundException")
    void testCrossTenantReviewAccess_RejectedWithResourceNotFoundException() {
        when(reviewRepository.findByIdAndOrganizationId(999L, TENANT_1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewById(userOrg1, 999L));
    }

    @Test
    @DisplayName("Cross-Tenant Calculate: calculation on foreign tenant review is rejected")
    void testCrossTenantCalculate_Rejected() {
        when(reviewRepository.findByIdAndOrganizationId(999L, TENANT_1)).thenReturn(Optional.empty());

        PerformanceCalculationRequest req = new PerformanceCalculationRequest();
        assertThrows(ResourceNotFoundException.class, () -> reviewService.calculateReview(userOrg1, 999L, req));
    }

    @Test
    @DisplayName("Anti-Forgery Guard: cross-tenant forged approval event is rejected and entity is NOT mutated")
    void testCrossTenantApprovalEvent_RejectedByAntiForgeryGuard() {
        PerformanceReviewRecord reviewOrg2 = new PerformanceReviewRecord();
        reviewOrg2.setId(500L);
        reviewOrg2.setOrganization(org2); // Belongs to org 200
        reviewOrg2.setStatus("APPROVAL_PENDING");

        when(reviewRepository.findById(500L)).thenReturn(Optional.of(reviewOrg2));

        // Forged event claiming to be from tenant 100 for review belonging to tenant 200
        ApprovalWorkflowCompletedEvent forgedEvent = new ApprovalWorkflowCompletedEvent(
                this,
                "wf-inst-1",
                WorkflowType.PERFORMANCE_REVIEW,
                "PERFORMANCE_REVIEW",
                "500",
                TENANT_1, // Attacker's org
                ApprovalStatus.APPROVED
        );

        workflowEventListener.onApprovalWorkflowCompleted(forgedEvent);

        // Verify review status was NOT changed to APPROVED
        assertEquals("APPROVAL_PENDING", reviewOrg2.getStatus());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Immutability: PUBLISHED review cannot be recalculated -> ConflictException")
    void testImmutability_PublishedReviewCannotBeRecalculated() {
        PerformanceReviewRecord review = new PerformanceReviewRecord();
        review.setId(500L);
        review.setOrganization(org1);
        review.setStatus("PUBLISHED");

        when(reviewRepository.findByIdAndOrganizationId(500L, TENANT_1)).thenReturn(Optional.of(review));

        PerformanceCalculationRequest req = new PerformanceCalculationRequest();
        ConflictException ex = assertThrows(ConflictException.class, () -> reviewService.calculateReview(userOrg1, 500L, req));
        assertTrue(ex.getMessage().contains("PERFORMANCE_LOCKED"));
    }

    @Test
    @DisplayName("Immutability: PUBLISHED review cannot submit manager review -> ConflictException")
    void testImmutability_PublishedReviewCannotBeUpdated() {
        PerformanceReviewRecord review = new PerformanceReviewRecord();
        review.setId(500L);
        review.setOrganization(org1);
        review.setStatus("PUBLISHED");

        when(reviewRepository.findByIdAndOrganizationId(500L, TENANT_1)).thenReturn(Optional.of(review));

        EnterpriseManagerReviewRequest req = new EnterpriseManagerReviewRequest(new BigDecimal("99.00"), "Late modification");
        ConflictException ex = assertThrows(ConflictException.class, () -> reviewService.submitManagerReview(userOrg1, 500L, req));
        assertTrue(ex.getMessage().contains("PERFORMANCE_LOCKED"));
    }

    @Test
    @DisplayName("Immutability: LOCKED review cannot undergo any mutation -> ConflictException")
    void testImmutability_LockedReviewCannotBeMutated() {
        PerformanceReviewRecord review = new PerformanceReviewRecord();
        review.setId(500L);
        review.setOrganization(org1);
        review.setStatus("LOCKED");

        when(reviewRepository.findByIdAndOrganizationId(500L, TENANT_1)).thenReturn(Optional.of(review));

        ConflictException ex = assertThrows(ConflictException.class, () -> reviewService.publishReview(userOrg1, 500L, null));
        assertTrue(ex.getMessage().contains("PERFORMANCE_LOCKED"));
    }

    @Test
    @DisplayName("Idempotency: Reusing same idempotency key with different request payload throws 409 IDEMPOTENCY_KEY_REUSED")
    void testIdempotency_KeyReusedWithDifferentPayload_ThrowsConflictException() {
        PerformanceCommandIdempotency existing = new PerformanceCommandIdempotency();
        existing.setIdempotencyKey("KEY-12345");
        existing.setRequestHash("HASH_A");
        existing.setResponseStatus(200);
        existing.setResponsePayload("{\"status\":\"SUCCESS\"}");
        existing.setCompletedAt(LocalDateTime.now());

        when(idempotencyRepository.findByOrganizationIdAndIdempotencyKey(TENANT_1, "KEY-12345"))
                .thenReturn(Optional.of(existing));

        // Attempting to run with different payload resulting in HASH_B
        ConflictException ex = assertThrows(ConflictException.class, () ->
                idempotencyService.executeWithIdempotency(
                        org1,
                        "KEY-12345",
                        "CALCULATE",
                        "DIFFERENT_PAYLOAD_B",
                        500L,
                        String.class,
                        () -> "FRESH_RESULT"
                )
        );

        assertTrue(ex.getMessage().contains("IDEMPOTENCY_KEY_REUSED"));
    }

    @Test
    @DisplayName("Idempotency: Replaying exact duplicate request returns cached payload without re-execution")
    void testIdempotency_DuplicateRequest_ReturnsCachedPayload() {
        String payload = "SAME_PAYLOAD";
        String reqHash = idempotencyService.computeRequestHash(payload);

        PerformanceCommandIdempotency existing = new PerformanceCommandIdempotency();
        existing.setIdempotencyKey("KEY-EXACT");
        existing.setRequestHash(reqHash);
        existing.setResponseStatus(200);
        existing.setResponsePayload("\"CACHED_RESULT\"");
        existing.setCompletedAt(LocalDateTime.now());

        when(idempotencyRepository.findByOrganizationIdAndIdempotencyKey(TENANT_1, "KEY-EXACT"))
                .thenReturn(Optional.of(existing));

        String result = idempotencyService.executeWithIdempotency(
                org1,
                "KEY-EXACT",
                "CALCULATE",
                payload,
                500L,
                String.class,
                () -> "FRESH_EXECUTION"
        );

        assertEquals("CACHED_RESULT", result);
    }

    @Test
    @DisplayName("Fail-Closed Security: TenantContext unset throws IllegalStateException")
    void testTenantContextUnset_FailsClosed() {
        TenantContext.clear();
        assertThrows(IllegalStateException.class, () -> reviewService.getReviewById(userOrg1, 500L));
    }
}
