package com.example.ems.increment.service;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalChangesRequestedEvent;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.event.ApprovalWorkflowRejectedEvent;
import com.example.ems.increment.entity.IncrementRecommendation;
import com.example.ems.increment.entity.IncrementRecommendationStatus;
import com.example.ems.increment.repository.IncrementRecommendationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class IncrementApprovalEventListener {

    private static final Logger log = LoggerFactory.getLogger(IncrementApprovalEventListener.class);

    @Autowired
    private IncrementRecommendationRepository recommendationRepository;

    @EventListener
    @Transactional
    public void onApprovalWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (!isIncrementWorkflow(event.getWorkflowType(), event.getBusinessReferenceType())) {
            return;
        }

        try {
            Long recId = Long.parseLong(event.getBusinessReferenceId());
            IncrementRecommendation rec = recommendationRepository.findById(recId).orElse(null);
            if (rec == null) {
                log.warn("Increment recommendation not found for ID: {}", recId);
                return;
            }

            if (!isMatchingTenantAndApprovalId(rec, event.getOrganizationId(), event.getWorkflowInstanceId())) {
                return;
            }

            if (event.getStatus() == ApprovalStatus.APPROVED) {
                rec.setStatus(IncrementRecommendationStatus.APPROVED);
                recommendationRepository.save(rec);
                log.info("Increment recommendation {} transitioned to APPROVED", recId);
            }
        } catch (Exception e) {
            log.error("Error processing ApprovalWorkflowCompletedEvent for increment", e);
        }
    }

    @EventListener
    @Transactional
    public void onApprovalWorkflowRejected(ApprovalWorkflowRejectedEvent event) {
        if (!isIncrementWorkflow(event.getWorkflowType(), event.getBusinessReferenceType())) {
            return;
        }

        try {
            Long recId = Long.parseLong(event.getBusinessReferenceId());
            IncrementRecommendation rec = recommendationRepository.findById(recId).orElse(null);
            if (rec == null) {
                log.warn("Increment recommendation not found for ID: {}", recId);
                return;
            }

            if (!isMatchingTenantAndApprovalId(rec, event.getOrganizationId(), event.getWorkflowInstanceId())) {
                return;
            }

            rec.setStatus(IncrementRecommendationStatus.REJECTED);
            rec.setRejectionReason(event.getReason());
            recommendationRepository.save(rec);
            log.info("Increment recommendation {} transitioned to REJECTED", recId);
        } catch (Exception e) {
            log.error("Error processing ApprovalWorkflowRejectedEvent for increment", e);
        }
    }

    @EventListener
    @Transactional
    public void onApprovalChangesRequested(ApprovalChangesRequestedEvent event) {
        if (!isIncrementWorkflow(event.getWorkflowType(), event.getBusinessReferenceType())) {
            return;
        }

        try {
            Long recId = Long.parseLong(event.getBusinessReferenceId());
            IncrementRecommendation rec = recommendationRepository.findById(recId).orElse(null);
            if (rec == null) {
                log.warn("Increment recommendation not found for ID: {}", recId);
                return;
            }

            if (!isMatchingTenantAndApprovalId(rec, event.getOrganizationId(), event.getWorkflowInstanceId())) {
                return;
            }

            rec.setStatus(IncrementRecommendationStatus.SENT_BACK);
            rec.setRejectionReason(event.getComments());
            recommendationRepository.save(rec);
            log.info("Increment recommendation {} transitioned to SENT_BACK", recId);
        } catch (Exception e) {
            log.error("Error processing ApprovalChangesRequestedEvent for increment", e);
        }
    }

    private boolean isIncrementWorkflow(WorkflowType workflowType, String businessReferenceType) {
        return workflowType == WorkflowType.INCREMENT_RECOMMENDATION ||
               "INCREMENT_RECOMMENDATION".equalsIgnoreCase(businessReferenceType) ||
               "INCREMENT".equalsIgnoreCase(businessReferenceType);
    }

    private boolean isMatchingTenantAndApprovalId(IncrementRecommendation rec, Long organizationId, String workflowInstanceId) {
        if (organizationId != null && rec.getOrganization() != null &&
            !organizationId.equals(rec.getOrganization().getId())) {
            log.warn("Organization mismatch on increment approval event: expected {}, got {}",
                    rec.getOrganization().getId(), organizationId);
            return false;
        }

        if (workflowInstanceId != null && rec.getApprovalRequestId() != null) {
            try {
                Long wfId = Long.parseLong(workflowInstanceId);
                if (!wfId.equals(rec.getApprovalRequestId())) {
                    log.warn("ApprovalRequestId mismatch: rec has {}, event has {}",
                            rec.getApprovalRequestId(), wfId);
                    return false;
                }
            } catch (NumberFormatException ignored) {}
        }

        return true;
    }
}
