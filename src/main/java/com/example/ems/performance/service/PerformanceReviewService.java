package com.example.ems.performance.service;

import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.User;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.performance.dto.*;
import com.example.ems.performance.entity.*;
import com.example.ems.performance.repository.*;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.rls.PostgresRlsSessionBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PerformanceReviewService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceReviewService.class);

    @Autowired
    private PerformanceReviewRecordRepository reviewRepository;

    @Autowired
    private PerformanceReviewCycleRepository cycleRepository;

    @Autowired
    private PerformanceKpiDefinitionRepository kpiDefinitionRepository;

    @Autowired
    private PerformanceReviewKpiScoreRepository kpiScoreRepository;

    @Autowired
    private PerformanceCalculationRunRepository calculationRunRepository;

    @Autowired
    private PerformanceReviewAuditRepository auditRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PerformanceReviewStateMachine stateMachine;

    @Autowired
    private PerformanceCalculationEngine calculationEngine;

    @Autowired
    private PerformanceSnapshotService snapshotService;

    @Autowired
    private PerformanceIdempotencyService idempotencyService;

    @Autowired(required = false)
    private ApprovalWorkflowEngineService approvalWorkflowEngineService;

    @Autowired(required = false)
    private PostgresRlsSessionBinder rlsSessionBinder;

    private void bindRlsIfAvailable() {
        if (rlsSessionBinder != null) {
            try {
                rlsSessionBinder.bindCurrentTenant();
            } catch (Exception ignored) {}
        }
    }

    // ── 1. INITIATE REVIEW ────────────────────────────────────────────────────
    @Transactional
    public EnterprisePerformanceReviewResponse initiateReview(User currentUser, Long cycleId, Long employeeId, Long reviewerId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        PerformanceReviewCycle cycle = cycleRepository.findByIdAndOrganizationId(cycleId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review cycle not found with ID: " + cycleId));

        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (emp.getOrganization() == null || !orgId.equals(emp.getOrganization().getId())) {
            throw new AccessDeniedException("Employee does not belong to the current organization context.");
        }

        Employee reviewer = null;
        if (reviewerId != null) {
            reviewer = employeeRepository.findById(reviewerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with ID: " + reviewerId));
            if (reviewer.getOrganization() == null || !orgId.equals(reviewer.getOrganization().getId())) {
                throw new AccessDeniedException("Reviewer does not belong to the current organization context.");
            }
        }

        Optional<PerformanceReviewRecord> existing = reviewRepository.findByOrganizationIdAndCycleIdAndEmployeeId(orgId, cycleId, employeeId);
        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        PerformanceReviewRecord review = new PerformanceReviewRecord();
        review.setOrganization(cycle.getOrganization());
        review.setCycle(cycle);
        review.setEmployee(emp);
        review.setReviewer(reviewer);
        review.setStatus(PerformanceReviewStatus.DRAFT.name());
        review.setCalculationVersion(1);
        review.setFormulaVersion(cycle.getFormulaVersion());
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        PerformanceReviewRecord saved = reviewRepository.save(review);
        recordAuditSnapshot(saved, "INITIATE", currentUser, null, BigDecimal.ZERO, "Review record initiated");
        return mapToResponse(saved);
    }

    // ── 2. SUBMIT SELF REVIEW ────────────────────────────────────────────────
    @Transactional
    public EnterprisePerformanceReviewResponse submitSelfReview(User currentUser, Long reviewId, EnterpriseSelfReviewRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        PerformanceReviewRecord review = reviewRepository.findByIdAndOrganizationId(reviewId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with ID: " + reviewId));

        return idempotencyService.executeWithIdempotency(
                review.getOrganization(),
                request.getIdempotencyKey(),
                "SELF_REVIEW",
                request,
                reviewId,
                EnterprisePerformanceReviewResponse.class,
                () -> {
                    stateMachine.assertEditableState(review.getStatus(), "submitSelfReview");
                    stateMachine.assertTransition(review.getStatus(), PerformanceReviewStatus.SELF_REVIEW_SUBMITTED, "submitSelfReview");

                    BigDecimal beforeScore = review.getFinalScore();
                    review.setSelfScore(request.getSelfScore());
                    review.setSelfFeedback(request.getSelfFeedback());
                    review.setSelfSubmittedAt(LocalDateTime.now());
                    review.setStatus(PerformanceReviewStatus.SELF_REVIEW_SUBMITTED.name());
                    review.setUpdatedAt(LocalDateTime.now());

                    PerformanceReviewRecord saved = reviewRepository.save(review);
                    recordAuditSnapshot(saved, "SELF_REVIEW", currentUser, beforeScore, saved.getFinalScore(), "Self review submitted");
                    return mapToResponse(saved);
                }
        );
    }

    // ── 3. SUBMIT MANAGER REVIEW ──────────────────────────────────────────────
    @Transactional
    public EnterprisePerformanceReviewResponse submitManagerReview(User currentUser, Long reviewId, EnterpriseManagerReviewRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        PerformanceReviewRecord review = reviewRepository.findByIdAndOrganizationId(reviewId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with ID: " + reviewId));

        return idempotencyService.executeWithIdempotency(
                review.getOrganization(),
                request.getIdempotencyKey(),
                "MANAGER_REVIEW",
                request,
                reviewId,
                EnterprisePerformanceReviewResponse.class,
                () -> {
                    stateMachine.assertEditableState(review.getStatus(), "submitManagerReview");
                    stateMachine.assertTransition(review.getStatus(), PerformanceReviewStatus.MANAGER_REVIEW_SUBMITTED, "submitManagerReview");
                    BigDecimal beforeScore = review.getFinalScore();
                    review.setManagerScore(request.getManagerScore());
                    review.setManagerFeedback(request.getManagerFeedback());
                    review.setManagerSubmittedAt(LocalDateTime.now());
                    review.setStatus(PerformanceReviewStatus.MANAGER_REVIEW_SUBMITTED.name());
                    review.setUpdatedAt(LocalDateTime.now());

                    PerformanceReviewRecord saved = reviewRepository.save(review);
                    recordAuditSnapshot(saved, "MANAGER_REVIEW", currentUser, beforeScore, saved.getFinalScore(), "Manager review submitted");
                    return mapToResponse(saved);
                }
        );
    }

    // ── 4. CALCULATE REVIEW ──────────────────────────────────────────────────
    @Transactional
    public EnterprisePerformanceReviewResponse calculateReview(User currentUser, Long reviewId, PerformanceCalculationRequest request) {
        return executeCalculation(currentUser, reviewId, request, false);
    }

    // ── 5. RECALCULATE REVIEW ────────────────────────────────────────────────
    @Transactional
    public EnterprisePerformanceReviewResponse recalculateReview(User currentUser, Long reviewId, PerformanceCalculationRequest request) {
        return executeCalculation(currentUser, reviewId, request, true);
    }

    private EnterprisePerformanceReviewResponse executeCalculation(User currentUser, Long reviewId,
                                                                   PerformanceCalculationRequest request, boolean isRecalculation) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        PerformanceReviewRecord review = reviewRepository.findByIdAndOrganizationId(reviewId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with ID: " + reviewId));

        stateMachine.assertEditableState(review.getStatus(), isRecalculation ? "recalculate" : "calculate");
        stateMachine.assertTransition(review.getStatus(), PerformanceReviewStatus.CALCULATED, isRecalculation ? "recalculate" : "calculate");

        return idempotencyService.executeWithIdempotency(
                review.getOrganization(),
                request.getIdempotencyKey(),
                isRecalculation ? "RECALCULATE" : "CALCULATE",
                request,
                reviewId,
                EnterprisePerformanceReviewResponse.class,
                () -> {
                    PerformanceReviewCycle cycle = review.getCycle();
                    Employee calcBy = resolveEmployeeForUser(currentUser, orgId);

                    // 1. Process KPI Score Inputs
                    List<PerformanceCalculationEngine.KpiEvaluationInput> kpiInputs = new ArrayList<>();
                    if (request.getKpiScores() != null && !request.getKpiScores().isEmpty()) {
                        for (PerformanceCalculationRequest.KpiScoreInputDto item : request.getKpiScores()) {
                            PerformanceKpiDefinition def = kpiDefinitionRepository.findByIdAndOrganizationId(item.getKpiId(), orgId)
                                    .orElseThrow(() -> new ResourceNotFoundException("KPI definition not found: " + item.getKpiId()));

                            kpiInputs.add(new PerformanceCalculationEngine.KpiEvaluationInput(
                                    def.getCode(),
                                    KpiMeasurementType.fromString(def.getMeasurementType()),
                                    def.getDefaultWeight(),
                                    def.getMinThreshold(),
                                    def.getMaxThreshold(),
                                    def.getTargetValue(),
                                    item.getActualValue()
                            ));
                        }
                    } else {
                        // Fallback to active cycle KPIs
                        List<PerformanceKpiDefinition> defs = kpiDefinitionRepository.findByOrganizationIdAndCycleIdAndStatus(orgId, cycle.getId(), "ACTIVE");
                        for (PerformanceKpiDefinition def : defs) {
                            kpiInputs.add(new PerformanceCalculationEngine.KpiEvaluationInput(
                                    def.getCode(),
                                    KpiMeasurementType.fromString(def.getMeasurementType()),
                                    def.getDefaultWeight(),
                                    def.getMinThreshold(),
                                    def.getMaxThreshold(),
                                    def.getTargetValue(),
                                    BigDecimal.ZERO
                            ));
                        }
                    }

                    // Level 1: Evaluate KPIs
                    List<PerformanceCalculationEngine.KpiEvaluationResult> kpiResults = calculationEngine.evaluateKpis(kpiInputs);
                    BigDecimal kpiWeightedScore = calculationEngine.calculateKpiWeightedScore(kpiResults);

                    // Update KPI score records in DB
                    for (PerformanceCalculationRequest.KpiScoreInputDto item : (request.getKpiScores() != null ? request.getKpiScores() : Collections.<PerformanceCalculationRequest.KpiScoreInputDto>emptyList())) {
                        PerformanceKpiDefinition def = kpiDefinitionRepository.findByIdAndOrganizationId(item.getKpiId(), orgId).orElse(null);
                        if (def == null) continue;
                        PerformanceCalculationEngine.KpiEvaluationResult evalRes = kpiResults.stream()
                                .filter(r -> r.getKpiCode().equals(def.getCode()))
                                .findFirst().orElse(null);

                        PerformanceReviewKpiScore scoreRec = kpiScoreRepository.findByOrganizationIdAndReviewIdAndKpiId(orgId, review.getId(), def.getId())
                                .orElseGet(() -> {
                                    PerformanceReviewKpiScore s = new PerformanceReviewKpiScore();
                                    s.setOrganization(review.getOrganization());
                                    s.setReview(review);
                                    s.setKpi(def);
                                    return s;
                                });
                        scoreRec.setWeight(def.getDefaultWeight());
                        scoreRec.setTargetValue(def.getTargetValue());
                        scoreRec.setActualValue(item.getActualValue());
                        scoreRec.setNormalizedScore(evalRes != null ? evalRes.getNormalizedScore() : BigDecimal.ZERO);
                        scoreRec.setComments(item.getComments());
                        kpiScoreRepository.save(scoreRec);
                    }

                    // 2. Prepare Level 2 Component Scores
                    if (request.getManagerScore() != null) review.setManagerScore(request.getManagerScore());
                    if (request.getManagerFeedback() != null) review.setManagerFeedback(request.getManagerFeedback());
                    if (request.getLeavesTaken() != null) review.setLeavesTaken(request.getLeavesTaken());
                    if (request.getAttendancePercentage() != null) {
                        review.setAttendancePercentage(request.getAttendancePercentage());
                        review.setAttendanceScore(request.getAttendancePercentage());
                    } else if (review.getAttendanceScore() == null || review.getAttendanceScore().compareTo(BigDecimal.ZERO) == 0) {
                        if (review.getAttendancePercentage() != null) {
                            review.setAttendanceScore(review.getAttendancePercentage());
                        }
                    }

                    PerformanceCalculationEngine.OverallCalculationResult overall = calculationEngine.calculateOverallScore(
                            kpiWeightedScore,
                            review.getManagerScore(),
                            review.getSelfScore(),
                            review.getAttendanceScore(),
                            cycle.getKpiWeight(),
                            cycle.getManagerWeight(),
                            cycle.getSelfWeight(),
                            cycle.getAttendanceWeight(),
                            kpiResults
                    );

                    BigDecimal beforeScore = review.getFinalScore();
                    review.setKpiWeightedScore(kpiWeightedScore);
                    review.setCalculatedScore(overall.getFinalScore());
                    review.setFinalScore(overall.getFinalScore());
                    review.setRatingBand(overall.getRatingBand());
                    review.setCalculatedAt(LocalDateTime.now());
                    review.setCalculatedBy(calcBy);
                    review.setStatus(PerformanceReviewStatus.CALCULATED.name());

                    // Increment version on recalculation
                    int calcVersion = isRecalculation ? (review.getCalculationVersion() + 1) : review.getCalculationVersion();
                    review.setCalculationVersion(calcVersion);

                    // 3. Capture Cryptographic Snapshot
                    PerformanceSnapshotService.SnapshotResult snap = snapshotService.captureSnapshot(review);
                    review.setSnapshotData(snap.getJson());
                    review.setSnapshotHash(snap.getHash());
                    review.setSnapshotVersion(isRecalculation ? review.getSnapshotVersion() + 1 : review.getSnapshotVersion());
                    review.setSnapshotCreatedAt(LocalDateTime.now());
                    review.setUpdatedAt(LocalDateTime.now());

                    PerformanceReviewRecord savedReview = reviewRepository.saveAndFlush(review);

                    // 4. Record Calculation Run (Historical Evidence Ledger)
                    PerformanceCalculationRun run = new PerformanceCalculationRun();
                    run.setOrganization(savedReview.getOrganization());
                    run.setReview(savedReview);
                    run.setCalculationVersion(calcVersion);
                    run.setFormulaVersion(cycle.getFormulaVersion());
                    run.setInputSnapshotHash(snap.getHash());
                    run.setKpiScore(kpiWeightedScore);
                    run.setManagerScore(overall.getManagerScore());
                    run.setSelfScore(overall.getSelfScore());
                    run.setAttendanceScore(overall.getAttendanceScore());
                    run.setFinalScore(overall.getFinalScore());
                    run.setRatingBand(overall.getRatingBand());
                    run.setCalculatedBy(calcBy);
                    run.setCalculatedAt(LocalDateTime.now());
                    PerformanceCalculationRun savedRun = calculationRunRepository.saveAndFlush(run);

                    savedReview.setCurrentCalculationRunId(savedRun.getId());
                    savedReview = reviewRepository.save(savedReview);

                    recordAuditSnapshot(savedReview, isRecalculation ? "RECALCULATE" : "CALCULATE",
                            currentUser, beforeScore, savedReview.getFinalScore(),
                            isRecalculation ? "Review recalculated with new inputs" : "Review calculated with snapshot and calculation run");

                    return mapToResponse(savedReview);
                }
        );
    }

    // ── 6. SUBMIT FOR APPROVAL ───────────────────────────────────────────────
    @Transactional
    public EnterprisePerformanceReviewResponse submitForApproval(User currentUser, Long reviewId, String idempotencyKey) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        PerformanceReviewRecord review = reviewRepository.findByIdAndOrganizationId(reviewId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with ID: " + reviewId));

        return idempotencyService.executeWithIdempotency(
                review.getOrganization(),
                idempotencyKey,
                "SUBMIT",
                Collections.singletonMap("reviewId", reviewId),
                reviewId,
                EnterprisePerformanceReviewResponse.class,
                () -> {
                    stateMachine.assertEditableState(review.getStatus(), "submitForApproval");
                    stateMachine.assertTransition(review.getStatus(), PerformanceReviewStatus.APPROVAL_PENDING, "submitForApproval");

                    Employee submitter = resolveEmployeeForUser(currentUser, orgId);
                    review.setStatus(PerformanceReviewStatus.APPROVAL_PENDING.name());
                    review.setSubmittedAt(LocalDateTime.now());
                    review.setSubmittedBy(submitter);
                    review.setUpdatedAt(LocalDateTime.now());

                    PerformanceReviewRecord saved = reviewRepository.saveAndFlush(review);
                    recordAuditSnapshot(saved, "SUBMIT", currentUser, saved.getFinalScore(), saved.getFinalScore(), "Submitted for multi-stage approval");

                    // Start delegated approval workflow engine if available
                    if (approvalWorkflowEngineService != null) {
                        try {
                            approvalWorkflowEngineService.startWorkflow(
                                    WorkflowType.PERFORMANCE_REVIEW,
                                    "PERFORMANCE_REVIEW",
                                    saved.getId().toString(),
                                    saved.getEmployee(),
                                    null
                            );
                        } catch (Exception e) {
                            log.warn("Delegated approval workflow initiation notice: {}", e.getMessage());
                        }
                    }

                    return mapToResponse(saved);
                }
        );
    }

    // ── 7. REOPEN REVIEW (Controlled Rejection Path) ─────────────────────────
    @Transactional
    public EnterprisePerformanceReviewResponse reopenReview(User currentUser, Long reviewId, ReopenReviewRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        PerformanceReviewRecord review = reviewRepository.findByIdAndOrganizationId(reviewId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with ID: " + reviewId));

        return idempotencyService.executeWithIdempotency(
                review.getOrganization(),
                request.getIdempotencyKey(),
                "REOPEN",
                request,
                reviewId,
                EnterprisePerformanceReviewResponse.class,
                () -> {
                    stateMachine.assertTransition(review.getStatus(), PerformanceReviewStatus.MANAGER_REVIEW_SUBMITTED, "reopenReview");

                    Employee reopener = resolveEmployeeForUser(currentUser, orgId);
                    review.setStatus(PerformanceReviewStatus.MANAGER_REVIEW_SUBMITTED.name());
                    review.setReopenedAt(LocalDateTime.now());
                    review.setReopenedBy(reopener);
                    review.setUpdatedAt(LocalDateTime.now());

                    PerformanceReviewRecord saved = reviewRepository.save(review);
                    recordAuditSnapshot(saved, "REOPEN", currentUser, saved.getFinalScore(), saved.getFinalScore(),
                            "Review reopened for correction: " + (request.getReason() != null ? request.getReason() : "Reopened by manager/HR"));
                    return mapToResponse(saved);
                }
        );
    }

    // ── 8. PUBLISH REVIEW (Gated Verification) ────────────────────────────────
    @Transactional
    public EnterprisePerformanceReviewResponse publishReview(User currentUser, Long reviewId, String idempotencyKey) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        PerformanceReviewRecord review = reviewRepository.findByIdAndOrganizationId(reviewId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with ID: " + reviewId));

        return idempotencyService.executeWithIdempotency(
                review.getOrganization(),
                idempotencyKey,
                "PUBLISH",
                Collections.singletonMap("reviewId", reviewId),
                reviewId,
                EnterprisePerformanceReviewResponse.class,
                () -> {
                    stateMachine.assertTransition(review.getStatus(), PerformanceReviewStatus.PUBLISHED, "publishReview");

                    // Strict Gating Checks:
                    if (!"APPROVED".equalsIgnoreCase(review.getStatus())) {
                        throw new IllegalStateException("Cannot publish review: Review must be in APPROVED status, but was " + review.getStatus());
                    }
                    if (review.getSnapshotData() == null || review.getSnapshotHash() == null) {
                        throw new IllegalStateException("Cannot publish review: Cryptographic snapshot is missing.");
                    }
                    if (!snapshotService.verifyIntegrity(review.getSnapshotData(), review.getSnapshotHash())) {
                        throw new ConflictException("SNAPSHOT_TAMPERED: Cannot publish review: Snapshot integrity check failed.");
                    }
                    if (review.getFinalScore() == null) {
                        throw new IllegalStateException("Cannot publish review: Calculation has not been completed.");
                    }

                    // Verify all mandatory KPIs have scores
                    List<PerformanceKpiDefinition> mandatoryDefs = kpiDefinitionRepository.findByOrganizationIdAndCycleId(orgId, review.getCycle().getId()).stream()
                            .filter(d -> Boolean.TRUE.equals(d.getIsMandatory()))
                            .toList();
                    for (PerformanceKpiDefinition def : mandatoryDefs) {
                        boolean scored = kpiScoreRepository.findByOrganizationIdAndReviewIdAndKpiId(orgId, review.getId(), def.getId()).isPresent();
                        if (!scored) {
                            throw new IllegalStateException("Cannot publish review: Mandatory KPI '" + def.getName() + "' has not been evaluated.");
                        }
                    }

                    Employee publisher = resolveEmployeeForUser(currentUser, orgId);
                    review.setStatus(PerformanceReviewStatus.PUBLISHED.name());
                    review.setPublishedAt(LocalDateTime.now());
                    review.setPublishedBy(publisher);
                    review.setUpdatedAt(LocalDateTime.now());

                    PerformanceReviewRecord saved = reviewRepository.save(review);
                    recordAuditSnapshot(saved, "PUBLISH", currentUser, saved.getFinalScore(), saved.getFinalScore(), "Performance review published (Immutable)");
                    return mapToResponse(saved);
                }
        );
    }

    // ── 9. LOCK REVIEW ────────────────────────────────────────────────────────
    @Transactional
    public EnterprisePerformanceReviewResponse lockReview(User currentUser, Long reviewId, String idempotencyKey) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        PerformanceReviewRecord review = reviewRepository.findByIdAndOrganizationId(reviewId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with ID: " + reviewId));

        return idempotencyService.executeWithIdempotency(
                review.getOrganization(),
                idempotencyKey,
                "LOCK",
                Collections.singletonMap("reviewId", reviewId),
                reviewId,
                EnterprisePerformanceReviewResponse.class,
                () -> {
                    stateMachine.assertTransition(review.getStatus(), PerformanceReviewStatus.LOCKED, "lockReview");

                    Employee locker = resolveEmployeeForUser(currentUser, orgId);
                    review.setStatus(PerformanceReviewStatus.LOCKED.name());
                    review.setLockedAt(LocalDateTime.now());
                    review.setLockedBy(locker);
                    review.setUpdatedAt(LocalDateTime.now());

                    PerformanceReviewRecord saved = reviewRepository.save(review);
                    recordAuditSnapshot(saved, "LOCK", currentUser, saved.getFinalScore(), saved.getFinalScore(), "Performance review locked (Terminal)");
                    return mapToResponse(saved);
                }
        );
    }

    // ── 10. CANCEL REVIEW ─────────────────────────────────────────────────────
    @Transactional
    public EnterprisePerformanceReviewResponse cancelReview(User currentUser, Long reviewId, String reason) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        PerformanceReviewRecord review = reviewRepository.findByIdAndOrganizationId(reviewId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with ID: " + reviewId));

        stateMachine.assertTransition(review.getStatus(), PerformanceReviewStatus.CANCELLED, "cancelReview");

        review.setStatus(PerformanceReviewStatus.CANCELLED.name());
        review.setRejectionReason(reason != null ? reason : "Review cancelled");
        review.setUpdatedAt(LocalDateTime.now());

        PerformanceReviewRecord saved = reviewRepository.save(review);
        recordAuditSnapshot(saved, "CANCEL", currentUser, saved.getFinalScore(), BigDecimal.ZERO, "Review cancelled: " + reason);
        return mapToResponse(saved);
    }

    // ── 11. READ METHODS ──────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public EnterprisePerformanceReviewResponse getReviewById(User currentUser, Long reviewId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        PerformanceReviewRecord review = reviewRepository.findByIdAndOrganizationId(reviewId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with ID: " + reviewId));
        return mapToResponse(review);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReviewStatus(User currentUser, Long reviewId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        PerformanceReviewRecord review = reviewRepository.findByIdAndOrganizationId(reviewId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with ID: " + reviewId));

        Map<String, Object> statusMap = new LinkedHashMap<>();
        statusMap.put("reviewId", review.getId());
        statusMap.put("cycleId", review.getCycle().getId());
        statusMap.put("employeeId", review.getEmployee().getId());
        statusMap.put("status", review.getStatus());
        statusMap.put("finalScore", review.getFinalScore());
        statusMap.put("ratingBand", review.getRatingBand());
        statusMap.put("calculationVersion", review.getCalculationVersion());
        statusMap.put("snapshotVersion", review.getSnapshotVersion());
        statusMap.put("snapshotHash", review.getSnapshotHash());
        statusMap.put("submittedAt", review.getSubmittedAt());
        statusMap.put("approvedAt", review.getApprovedAt());
        statusMap.put("publishedAt", review.getPublishedAt());
        statusMap.put("lockedAt", review.getLockedAt());

        List<String> allowedActions = new ArrayList<>();
        String st = review.getStatus();
        if ("DRAFT".equalsIgnoreCase(st)) {
            allowedActions.add("SELF_REVIEW");
            allowedActions.add("MANAGER_REVIEW");
            allowedActions.add("CANCEL");
        } else if ("SELF_REVIEW_SUBMITTED".equalsIgnoreCase(st)) {
            allowedActions.add("MANAGER_REVIEW");
            allowedActions.add("CANCEL");
        } else if ("MANAGER_REVIEW_SUBMITTED".equalsIgnoreCase(st)) {
            allowedActions.add("CALCULATE");
            allowedActions.add("CANCEL");
        } else if ("CALCULATED".equalsIgnoreCase(st)) {
            allowedActions.add("RECALCULATE");
            allowedActions.add("SUBMIT");
            allowedActions.add("CANCEL");
        } else if ("APPROVAL_PENDING".equalsIgnoreCase(st)) {
            allowedActions.add("APPROVE");
            allowedActions.add("REJECT");
            allowedActions.add("CANCEL");
        } else if ("REJECTED".equalsIgnoreCase(st)) {
            allowedActions.add("REOPEN");
            allowedActions.add("CANCEL");
        } else if ("APPROVED".equalsIgnoreCase(st)) {
            allowedActions.add("PUBLISH");
        } else if ("PUBLISHED".equalsIgnoreCase(st)) {
            allowedActions.add("LOCK");
        }
        statusMap.put("allowedActions", allowedActions);
        return statusMap;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSnapshotDetails(User currentUser, Long reviewId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        PerformanceReviewRecord review = reviewRepository.findByIdAndOrganizationId(reviewId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with ID: " + reviewId));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("reviewId", review.getId());
        map.put("snapshotVersion", review.getSnapshotVersion());
        map.put("snapshotHash", review.getSnapshotHash());
        map.put("snapshotCreatedAt", review.getSnapshotCreatedAt());

        PerformanceSnapshotDto dto = snapshotService.deserializeFromJson(review.getSnapshotData());
        map.put("snapshotData", dto);
        map.put("hashIntegrityValid", snapshotService.verifyIntegrity(review.getSnapshotData(), review.getSnapshotHash()));
        return map;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> refreshInputsComparison(User currentUser, Long reviewId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        PerformanceReviewRecord review = reviewRepository.findByIdAndOrganizationId(reviewId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with ID: " + reviewId));

        PerformanceSnapshotDto stored = snapshotService.deserializeFromJson(review.getSnapshotData());
        PerformanceSnapshotService.SnapshotResult live = snapshotService.captureSnapshot(review);

        String storedHash = review.getSnapshotHash();
        String liveHash = live.getHash();
        boolean differs = !Objects.equals(storedHash, liveHash);

        Map<String, Object> comp = new LinkedHashMap<>();
        comp.put("reviewId", review.getId());
        comp.put("storedHash", storedHash);
        comp.put("liveHash", liveHash);
        comp.put("inputsModified", differs);
        comp.put("storedSnapshot", stored);
        comp.put("liveSnapshot", live.getDto());
        comp.put("message", differs ? "Live dependencies have changed since calculation." : "Snapshot matches live dependencies.");
        return comp;
    }

    @Transactional(readOnly = true)
    public List<PerformanceCalculationRun> getCalculationRuns(User currentUser, Long reviewId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();
        return calculationRunRepository.findByOrganizationIdAndReviewIdOrderByCalculationVersionDesc(orgId, reviewId);
    }

    @Transactional(readOnly = true)
    public List<PerformanceReviewAudit> getAuditTrail(User currentUser, Long reviewId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();
        return auditRepository.findByOrganizationIdAndReviewIdOrderByCreatedAtDesc(orgId, reviewId);
    }

    // ── AUDIT HELPER ─────────────────────────────────────────────────────────
    private void recordAuditSnapshot(PerformanceReviewRecord review, String action, User actorUser,
                                     BigDecimal before, BigDecimal after, String reason) {
        try {
            Long orgId = review.getOrganization().getId();
            Employee actor = resolveEmployeeForUser(actorUser, orgId);
            if (actor != null && (actor.getOrganization() == null || !orgId.equals(actor.getOrganization().getId()))) {
                actor = null;
            }
            String actorName = (actorUser != null && actorUser.getFullName() != null)
                    ? actorUser.getFullName()
                    : (actor != null ? actor.getFullName() : "System");
            String snapshotJson = snapshotService.toCanonicalJson(mapToResponse(review));

            PerformanceReviewAudit audit = new PerformanceReviewAudit(
                    review.getOrganization(),
                    review.getId(),
                    action,
                    actor,
                    actorName,
                    null,
                    review.getStatus(),
                    before,
                    after,
                    reason,
                    snapshotJson
            );
            auditRepository.save(audit);
        } catch (Exception e) {
            log.warn("Failed to record performance review audit snapshot: {}", e.getMessage());
        }
    }

    private Employee resolveEmployeeForUser(User user, Long orgId) {
        if (user != null && user.getWorkEmail() != null) {
            Optional<Employee> emp = employeeRepository.findByEmail(user.getWorkEmail());
            if (emp.isPresent() && emp.get().getOrganization() != null && orgId.equals(emp.get().getOrganization().getId())) {
                return emp.get();
            }
        }
        List<Employee> emps = employeeRepository.findByOrganizationId(orgId);
        return emps.isEmpty() ? null : emps.get(0);
    }

    private EnterprisePerformanceReviewResponse mapToResponse(PerformanceReviewRecord r) {
        EnterprisePerformanceReviewResponse resp = new EnterprisePerformanceReviewResponse();
        resp.setId(r.getId());
        resp.setCycleId(r.getCycle().getId());
        resp.setCycleCode(r.getCycle().getCode());
        resp.setCycleName(r.getCycle().getName());
        resp.setEmployeeId(r.getEmployee().getId());
        resp.setEmployeeName(r.getEmployee().getFullName());
        resp.setEmployeeEmail(r.getEmployee().getEmail());
        if (r.getReviewer() != null) {
            resp.setReviewerId(r.getReviewer().getId());
            resp.setReviewerName(r.getReviewer().getFullName());
        }
        resp.setStatus(r.getStatus());
        resp.setSelfScore(r.getSelfScore());
        resp.setSelfFeedback(r.getSelfFeedback());
        resp.setSelfSubmittedAt(r.getSelfSubmittedAt());
        resp.setManagerScore(r.getManagerScore());
        resp.setManagerFeedback(r.getManagerFeedback());
        resp.setManagerSubmittedAt(r.getManagerSubmittedAt());
        resp.setAttendanceScore(r.getAttendanceScore());
        resp.setLeavesTaken(r.getLeavesTaken());
        resp.setAttendancePercentage(r.getAttendancePercentage());
        resp.setKpiWeightedScore(r.getKpiWeightedScore());
        resp.setCalculatedScore(r.getCalculatedScore());
        resp.setFinalScore(r.getFinalScore());
        resp.setRatingBand(r.getRatingBand());
        resp.setCalculationVersion(r.getCalculationVersion());
        resp.setFormulaVersion(r.getFormulaVersion());
        resp.setCurrentCalculationRunId(r.getCurrentCalculationRunId());
        resp.setSnapshotVersion(r.getSnapshotVersion());
        resp.setSnapshotHash(r.getSnapshotHash());
        resp.setSnapshotCreatedAt(r.getSnapshotCreatedAt());
        resp.setCalculatedAt(r.getCalculatedAt());
        if (r.getCalculatedBy() != null) resp.setCalculatedByName(r.getCalculatedBy().getFullName());
        resp.setSubmittedAt(r.getSubmittedAt());
        if (r.getSubmittedBy() != null) resp.setSubmittedByName(r.getSubmittedBy().getFullName());
        resp.setApprovalWorkflowId(r.getApprovalWorkflowId());
        resp.setApprovedAt(r.getApprovedAt());
        if (r.getApprovedBy() != null) resp.setApprovedByName(r.getApprovedBy().getFullName());
        resp.setRejectionReason(r.getRejectionReason());
        resp.setRejectedAt(r.getRejectedAt());
        if (r.getRejectedBy() != null) resp.setRejectedByName(r.getRejectedBy().getFullName());
        resp.setReopenedAt(r.getReopenedAt());
        if (r.getReopenedBy() != null) resp.setReopenedByName(r.getReopenedBy().getFullName());
        resp.setPublishedAt(r.getPublishedAt());
        if (r.getPublishedBy() != null) resp.setPublishedByName(r.getPublishedBy().getFullName());
        resp.setLockedAt(r.getLockedAt());
        if (r.getLockedBy() != null) resp.setLockedByName(r.getLockedBy().getFullName());
        resp.setVersion(r.getVersion());
        resp.setCreatedAt(r.getCreatedAt());
        resp.setUpdatedAt(r.getUpdatedAt());

        // Attach KPI scores
        List<PerformanceReviewKpiScore> kpiScores = kpiScoreRepository.findByOrganizationIdAndReviewId(r.getOrganization().getId(), r.getId());
        List<EnterprisePerformanceReviewResponse.KpiScoreResponseDto> kpiList = new ArrayList<>();
        for (PerformanceReviewKpiScore s : kpiScores) {
            EnterprisePerformanceReviewResponse.KpiScoreResponseDto kDto = new EnterprisePerformanceReviewResponse.KpiScoreResponseDto();
            kDto.setKpiId(s.getKpi().getId());
            kDto.setKpiCode(s.getKpi().getCode());
            kDto.setKpiName(s.getKpi().getName());
            kDto.setMeasurementType(s.getKpi().getMeasurementType());
            kDto.setWeight(s.getWeight());
            kDto.setTargetValue(s.getTargetValue());
            kDto.setActualValue(s.getActualValue());
            kDto.setNormalizedScore(s.getNormalizedScore());
            kDto.setComments(s.getComments());
            kpiList.add(kDto);
        }
        resp.setKpiScores(kpiList);

        return resp;
    }

    @Transactional(readOnly = true)
    public List<EnterprisePerformanceReviewResponse> getMyReviews(User currentUser) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        Employee emp = employeeRepository.findByEmail(currentUser.getWorkEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for user: " + currentUser.getWorkEmail()));

        List<PerformanceReviewRecord> list = reviewRepository.findByOrganizationIdAndEmployeeId(orgId, emp.getId());
        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCycleSummaryReport(User currentUser, Long cycleId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        List<PerformanceReviewRecord> reviews = reviewRepository.findByOrganizationIdAndCycleId(orgId, cycleId);

        long total = reviews.size();
        long published = reviews.stream().filter(r -> "PUBLISHED".equalsIgnoreCase(r.getStatus()) || "LOCKED".equalsIgnoreCase(r.getStatus())).count();
        long pending = total - published;

        double avgScore = reviews.stream()
                .filter(r -> r.getFinalScore() != null)
                .mapToDouble(r -> r.getFinalScore().doubleValue())
                .average()
                .orElse(0.0);

        Map<String, Long> ratingDistribution = new LinkedHashMap<>();
        ratingDistribution.put("OUTSTANDING", reviews.stream().filter(r -> "OUTSTANDING".equalsIgnoreCase(r.getRatingBand())).count());
        ratingDistribution.put("EXCEEDS_EXPECTATIONS", reviews.stream().filter(r -> "EXCEEDS_EXPECTATIONS".equalsIgnoreCase(r.getRatingBand())).count());
        ratingDistribution.put("MEETS_EXPECTATIONS", reviews.stream().filter(r -> "MEETS_EXPECTATIONS".equalsIgnoreCase(r.getRatingBand())).count());
        ratingDistribution.put("NEEDS_IMPROVEMENT", reviews.stream().filter(r -> "NEEDS_IMPROVEMENT".equalsIgnoreCase(r.getRatingBand())).count());
        ratingDistribution.put("UNSATISFACTORY", reviews.stream().filter(r -> "UNSATISFACTORY".equalsIgnoreCase(r.getRatingBand())).count());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("cycleId", cycleId);
        report.put("generatedAt", LocalDateTime.now());
        report.put("totalReviews", total);
        report.put("publishedReviews", published);
        report.put("pendingReviews", pending);
        report.put("averageFinalScore", BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP));
        report.put("ratingDistribution", ratingDistribution);
        return report;
    }
}
