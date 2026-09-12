package com.example.ems.onboarding.service;

import com.example.ems.auth.entity.User;
import com.example.ems.onboarding.dto.approval.OnboardingApprovalActionRequest;
import com.example.ems.onboarding.dto.approval.OnboardingApprovalListResponse;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.entity.OnboardingApproval;
import com.example.ems.onboarding.repository.OnboardingApprovalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OnboardingApprovalService {

    @Autowired
    private OnboardingApprovalRepository approvalRepository;

    @Autowired
    private OnboardingLifecycleService lifecycleService;

    @Autowired
    private OnboardingSecurityValidator securityValidator;

    @Autowired
    private OnboardingAuditLogService auditLogService;

    public OnboardingApprovalListResponse getApprovals(Long onboardingId) {
        Onboarding onboarding = securityValidator.validateAndGetOnboarding(onboardingId);
        List<OnboardingApproval> records = approvalRepository.findByOnboardingIdOrderByLevelAsc(onboardingId);

        OnboardingApprovalListResponse response = new OnboardingApprovalListResponse();
        response.setOnboardingId(onboardingId);
        response.setStatus(onboarding.getStatus());

        List<OnboardingApprovalListResponse.ApprovalItem> items = records.stream().map(a -> {
            OnboardingApprovalListResponse.ApprovalItem item = new OnboardingApprovalListResponse.ApprovalItem();
            item.setApprovalId(a.getId());
            item.setLevel(a.getLevel());
            item.setApproverEmployeeId(a.getApprover().getEmployeeId() != null ? a.getApprover().getEmployeeId() : "USER-" + a.getApprover().getId());
            item.setApproverName(a.getApprover().getWorkEmail());
            item.setStatus(a.getStatus());
            item.setRemarks(a.getRemarks());
            item.setApprovedAt(a.getApprovedAt());
            return item;
        }).collect(Collectors.toList());

        response.setApprovals(items);
        return response;
    }

    @Transactional
    public OnboardingApprovalListResponse processApprovalAction(Long onboardingId, OnboardingApprovalActionRequest request) {
        Onboarding onboarding = securityValidator.validateAndGetOnboarding(onboardingId);
        User currentUser = securityValidator.getAuthenticatedUser();

        String action = request.getAction().toUpperCase().trim();
        if (!"APPROVE".equals(action) && !"REJECT".equals(action)) {
            throw new IllegalArgumentException("Action must be either APPROVE or REJECT");
        }

        OnboardingApproval record = new OnboardingApproval();
        record.setOnboarding(onboarding);
        record.setLevel(1);
        record.setApprover(currentUser);
        record.setRemarks(request.getRemarks());

        if ("APPROVE".equals(action)) {
            record.setStatus("APPROVED");
            record.setApprovedAt(LocalDateTime.now());

            // Single status mutation engine
            lifecycleService.updateStatus(onboardingId, "APPROVED", request.getRemarks());
            auditLogService.logAction(onboarding, "ONBOARDING_APPROVED", "ONBOARDING_APPROVAL", onboardingId, request.getRemarks());
        } else {
            record.setStatus("REJECTED");
            lifecycleService.updateStatus(onboardingId, "CANCELLED", request.getRemarks());
            auditLogService.logAction(onboarding, "ONBOARDING_REJECTED", "ONBOARDING_APPROVAL", onboardingId, request.getRemarks());
        }

        approvalRepository.save(record);
        return getApprovals(onboardingId);
    }
}
