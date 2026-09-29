package com.example.ems.performance.service;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.event.ApprovalWorkflowRejectedEvent;
import com.example.ems.performance.entity.PerformanceReviewAudit;
import com.example.ems.performance.entity.PerformanceReviewRecord;
import com.example.ems.performance.repository.PerformanceReviewAuditRepository;
import com.example.ems.performance.repository.PerformanceReviewRecordRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.rls.PostgresRlsSessionBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class PerformanceWorkflowEventListener {

    private static final Logger log = LoggerFactory.getLogger(PerformanceWorkflowEventListener.class);

    @Autowired
    private PerformanceReviewRecordRepository reviewRepository;

    @Autowired
    private PerformanceReviewAuditRepository auditRepository;

    @Autowired(required = false)
    private PostgresRlsSessionBinder rlsSessionBinder;

    private void bindRls() {
        if (rlsSessionBinder != null) {
            try {
                rlsSessionBinder.bindCurrentTenant();
            } catch (Exception ignored) {}
        }
    }

    @EventListener
    @Transactional
    public void onApprovalWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (event.getWorkflowType() != WorkflowType.PERFORMANCE_REVIEW) {
            return;
        }

        log.info("PerformanceWorkflowEventListener: received completed event for refId={}, org={}, status={}",
                event.getBusinessReferenceId(), event.getOrganizationId(), event.getStatus());

        Long prevTenant = TenantContext.getCurrentTenant();
        try {
            if (event.getOrganizationId() != null) {
                TenantContext.setCurrentTenant(event.getOrganizationId());
                bindRls();
            }

            handleReviewWorkflow(event.getBusinessReferenceId(), event.getOrganizationId(), event.getStatus(), null);
        } finally {
            if (prevTenant != null) {
                TenantContext.setCurrentTenant(prevTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    @EventListener
    @Transactional
    public void onApprovalWorkflowRejected(ApprovalWorkflowRejectedEvent event) {
        if (event.getWorkflowType() != WorkflowType.PERFORMANCE_REVIEW) {
            return;
        }

        log.info("PerformanceWorkflowEventListener: received rejected event for refId={}, org={}",
                event.getBusinessReferenceId(), event.getOrganizationId());

        Long prevTenant = TenantContext.getCurrentTenant();
        try {
            if (event.getOrganizationId() != null) {
                TenantContext.setCurrentTenant(event.getOrganizationId());
                bindRls();
            }

            handleReviewWorkflow(event.getBusinessReferenceId(), event.getOrganizationId(), ApprovalStatus.REJECTED, null);
        } finally {
            if (prevTenant != null) {
                TenantContext.setCurrentTenant(prevTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    private void handleReviewWorkflow(String refId, Long eventOrgId, ApprovalStatus status, String rejectionReason) {
        try {
            Long reviewId = Long.parseLong(refId);

            reviewRepository.findById(reviewId).ifPresentOrElse(review -> {
                Long actualOrgId = review.getOrganization() != null ? review.getOrganization().getId() : null;
                // Anti-Forgery Guard: Entity must belong to the exact event organization context
                if (eventOrgId != null && actualOrgId != null && !eventOrgId.equals(actualOrgId)) {
                    log.error("SECURITY ALERT: Forged cross-tenant performance approval event rejected! Event org {} does not match Review org {} for review ID {}",
                            eventOrgId, actualOrgId, reviewId);
                    return;
                }

                String beforeStatus = review.getStatus();
                if (status == ApprovalStatus.APPROVED) {
                    review.setStatus("APPROVED");
                    review.setApprovedAt(LocalDateTime.now());
                } else if (status == ApprovalStatus.REJECTED) {
                    review.setStatus("REJECTED");
                    review.setRejectedAt(LocalDateTime.now());
                    if (rejectionReason != null) {
                        review.setRejectionReason(rejectionReason);
                    }
                }
                review.setUpdatedAt(LocalDateTime.now());
                PerformanceReviewRecord saved = reviewRepository.save(review);

                // Audit
                PerformanceReviewAudit audit = new PerformanceReviewAudit(
                        saved.getOrganization(),
                        saved.getId(),
                        status == ApprovalStatus.APPROVED ? "APPROVE" : "REJECT",
                        null,
                        "Approval Workflow Engine",
                        beforeStatus,
                        saved.getStatus(),
                        saved.getFinalScore(),
                        saved.getFinalScore(),
                        status == ApprovalStatus.APPROVED ? "Multi-stage review workflow approved" : "Review workflow rejected",
                        saved.getSnapshotData()
                );
                auditRepository.save(audit);

                log.info("PerformanceReviewRecord ID {} status updated to {}", reviewId, saved.getStatus());
            }, () -> {
                log.warn("PerformanceReviewRecord ID {} not found in org context {}", reviewId, eventOrgId);
            });
        } catch (Exception e) {
            log.error("Failed to handle performance review workflow transition for refId {}: {}", refId, e.getMessage());
        }
    }
}
