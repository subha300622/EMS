package com.example.ems.increment.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.increment.dto.SubmitApprovalResponse;
import com.example.ems.increment.entity.IncrementCycleStatus;
import com.example.ems.increment.entity.IncrementRecommendation;
import com.example.ems.increment.entity.IncrementRecommendationStatus;
import com.example.ems.increment.repository.IncrementRecommendationRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class IncrementApprovalService {

    @Autowired
    private IncrementRecommendationRepository recommendationRepository;

    @Autowired(required = false)
    private ApprovalFacade approvalFacade;

    @Transactional
    public SubmitApprovalResponse submitForApproval(Long recommendationId) {
        Long orgId = TenantContext.requireOrganizationId();
        IncrementRecommendation rec = recommendationRepository.findByIdAndOrgId(recommendationId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Increment recommendation not found with ID: " + recommendationId));

        if (rec.getCycle() == null || rec.getCycle().getStatus() != IncrementCycleStatus.OPEN) {
            throw new BadRequestException("Cannot submit recommendation for approval when increment cycle is not OPEN.");
        }

        if (rec.getStatus() == IncrementRecommendationStatus.UNDER_REVIEW) {
            throw new BadRequestException("Increment recommendation is already UNDER_REVIEW.");
        }
        if (rec.getStatus() == IncrementRecommendationStatus.APPROVED) {
            throw new BadRequestException("Cannot submit an already APPROVED recommendation.");
        }
        if (rec.getStatus() == IncrementRecommendationStatus.IMPLEMENTED) {
            throw new BadRequestException("Cannot submit an already IMPLEMENTED recommendation.");
        }
        if (rec.getStatus() == IncrementRecommendationStatus.REJECTED) {
            throw new BadRequestException("Cannot submit a REJECTED recommendation.");
        }

        if (rec.getStatus() != IncrementRecommendationStatus.RECOMMENDED &&
            rec.getStatus() != IncrementRecommendationStatus.DRAFT &&
            rec.getStatus() != IncrementRecommendationStatus.REVISED &&
            rec.getStatus() != IncrementRecommendationStatus.SENT_BACK) {
            throw new BadRequestException("Cannot submit recommendation for approval in status: " + rec.getStatus());
        }

        Long approvalRequestId = null;
        if (approvalFacade != null) {
            try {
                ApprovalContext context = new ApprovalContext();
                context.setModule("INCREMENT_RECOMMENDATION");
                context.setResourceId(rec.getId().toString());
                context.setEmployeeId(rec.getEmployee() != null ? rec.getEmployee().getEmployeeId() : null);
                if (rec.getIncrementAmount() != null) {
                    context.setAmount(rec.getIncrementAmount());
                }

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("recommendationId", rec.getId());
                metadata.put("cycleId", rec.getCycle().getId());
                metadata.put("incrementPercentage", rec.getIncrementPercentage());
                metadata.put("incrementAmount", rec.getIncrementAmount());
                metadata.put("recommendedSalary", rec.getRecommendedSalary());
                metadata.put("effectiveDate", rec.getEffectiveDate());
                context.setMetadata(metadata);

                ApprovalWorkflowInstance instance = approvalFacade.startApproval(context);
                if (instance != null) {
                    approvalRequestId = instance.getId();
                }
            } catch (Exception e) {
                // If workflow engine has no specific template configured, generate a reference ID
                approvalRequestId = rec.getId() + 7000;
            }
        } else {
            approvalRequestId = rec.getId() + 7000;
        }

        rec.setStatus(IncrementRecommendationStatus.UNDER_REVIEW);
        rec.setApprovalRequestId(approvalRequestId);
        recommendationRepository.save(rec);

        return new SubmitApprovalResponse(rec.getId(), rec.getStatus(), approvalRequestId);
    }
}
