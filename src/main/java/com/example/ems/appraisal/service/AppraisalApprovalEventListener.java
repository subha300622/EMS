package com.example.ems.appraisal.service;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalRequestStatus;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.appraisal.repository.AppraisalRequestRepository;
import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.event.ApprovalWorkflowRejectedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class AppraisalApprovalEventListener {

    @Autowired
    private AppraisalRequestRepository requestRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private AppraisalHistoryService historyService;

    @EventListener
    @Transactional
    public void onApprovalWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (event.getWorkflowType() != WorkflowType.APPRAISAL_REQUEST) {
            return;
        }

        try {
            Long requestId = Long.parseLong(event.getBusinessReferenceId());
            requestRepository.findById(requestId).ifPresent(request -> {
                if (event.getStatus() == ApprovalStatus.APPROVED) {
                    request.setStatus(AppraisalRequestStatus.APPROVED);
                    request.setApprovedAt(LocalDateTime.now());
                    request.setUpdatedAt(LocalDateTime.now());

                    // Create Appraisal record on approved request
                    Appraisal appraisal = new Appraisal();
                    appraisal.setOrganization(request.getOrganization());
                    appraisal.setEmployee(request.getEmployee());
                    appraisal.setRequest(request);
                    appraisal.setStatus(AppraisalStatus.CREATED);
                    appraisal.setCurrentStageOrder(1);
                    appraisal.setCreatedAt(LocalDateTime.now());
                    appraisal.setUpdatedAt(LocalDateTime.now());

                    Appraisal savedAppraisal = appraisalRepository.save(appraisal);
                    request.setAppraisalId(savedAppraisal.getId());
                    request.setStatus(AppraisalRequestStatus.APPRAISAL_CREATED);
                    requestRepository.save(request);

                    historyService.recordHistory(
                            savedAppraisal,
                            request.getEmployee(),
                            "APPRAISAL_CREATED_FROM_REQUEST",
                            null,
                            AppraisalStatus.CREATED.name(),
                            null,
                            "Appraisal automatically created after special request approval"
                    );
                }
            });
        } catch (Exception ignored) {}
    }

    @EventListener
    @Transactional
    public void onApprovalWorkflowRejected(ApprovalWorkflowRejectedEvent event) {
        if (event.getWorkflowType() != WorkflowType.APPRAISAL_REQUEST) {
            return;
        }

        try {
            Long requestId = Long.parseLong(event.getBusinessReferenceId());
            requestRepository.findById(requestId).ifPresent(request -> {
                request.setStatus(AppraisalRequestStatus.REJECTED);
                request.setRejectedAt(LocalDateTime.now());
                request.setUpdatedAt(LocalDateTime.now());
                requestRepository.save(request);
            });
        } catch (Exception ignored) {}
    }
}
