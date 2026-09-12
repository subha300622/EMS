package com.example.ems.onboarding.service;

import com.example.ems.auth.entity.User;
import com.example.ems.onboarding.dto.policy.*;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.entity.OnboardingApprovalPolicy;
import com.example.ems.onboarding.repository.OnboardingApprovalPolicyRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.tenant.TenantSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OnboardingApprovalPolicyService {

    @Autowired
    private OnboardingApprovalPolicyRepository policyRepository;

    @Autowired
    private OnboardingSecurityValidator securityValidator;

    @Autowired
    private TenantSecurityService tenantSecurityService;

    @Autowired
    private OnboardingLifecycleService lifecycleService;

    public List<OnboardingStatusTransitionPolicyDto> getStatusTransitionPolicies() {
        return List.of(
                new OnboardingStatusTransitionPolicyDto("ONB-POL-001", "PENDING", "START", "IN_PROGRESS", "EMPLOYEE", true),
                new OnboardingStatusTransitionPolicyDto("ONB-POL-002", "IN_PROGRESS", "SUBMIT", "UNDER_REVIEW", "EMPLOYEE", true),
                new OnboardingStatusTransitionPolicyDto("ONB-POL-003", "UNDER_REVIEW", "REQUEST_CHANGES", "IN_PROGRESS", "CONFIGURED_ACTOR", true),
                new OnboardingStatusTransitionPolicyDto("ONB-POL-004", "UNDER_REVIEW", "VERIFY", "COMPLETED", "CONFIGURED_ACTOR", true),
                new OnboardingStatusTransitionPolicyDto("ONB-POL-005", "COMPLETED", "APPROVE", "APPROVED", "CONFIGURED_ACTOR", true)
        );
    }

    public List<OnboardingApprovalPolicy> getApprovalPoliciesForCurrentTenant() {
        Long orgId = TenantContext.getOrganizationId();
        if (orgId != null) {
            List<OnboardingApprovalPolicy> customPolicies = policyRepository.findByOrganizationIdAndActiveTrue(orgId);
            if (!customPolicies.isEmpty()) {
                return customPolicies;
            }
        }
        return policyRepository.findByOrganizationIdIsNullAndActiveTrue();
    }

    @Transactional
    public OnboardingApprovalPolicy createApprovalPolicy(OnboardingApprovalPolicyRequest request) {
        Long orgId = tenantSecurityService.getRequiredOrganizationId();

        OnboardingApprovalPolicy policy = new OnboardingApprovalPolicy();
        policy.setOrganizationId(orgId);
        policy.setPolicyId("ONB-POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        policy.setCurrentStatus(request.getCurrentStatus().toUpperCase().trim());
        policy.setAction(request.getAction().toUpperCase().trim());
        policy.setNextStatus(request.getNextStatus().toUpperCase().trim());
        policy.setActive(request.isActive());

        if (request.getApprover() != null) {
            String type = (String) request.getApprover().getOrDefault("type", "CONFIGURED_ROLE");
            policy.setApproverType(type);
            Number roleIdNum = (Number) request.getApprover().get("roleId");
            if (roleIdNum != null) {
                policy.setApproverRoleId(roleIdNum.longValue());
            }
        } else {
            policy.setApproverType("CONFIGURED_ROLE");
        }

        if (request.getConditions() != null) {
            policy.setConditions(String.join(",", request.getConditions()));
        }

        return policyRepository.save(policy);
    }

    @Transactional
    public OnboardingApprovalPolicy updateApprovalPolicy(Long policyId, OnboardingApprovalPolicyRequest request) {
        Long orgId = tenantSecurityService.getRequiredOrganizationId();
        OnboardingApprovalPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Approval policy not found with ID: " + policyId));

        if (policy.getOrganizationId() != null && !policy.getOrganizationId().equals(orgId)) {
            throw new IllegalArgumentException("Unauthorized access to organization approval policy.");
        }

        if (request.getCurrentStatus() != null) policy.setCurrentStatus(request.getCurrentStatus().toUpperCase().trim());
        if (request.getAction() != null) policy.setAction(request.getAction().toUpperCase().trim());
        if (request.getNextStatus() != null) policy.setNextStatus(request.getNextStatus().toUpperCase().trim());
        policy.setActive(request.isActive());

        if (request.getApprover() != null) {
            String type = (String) request.getApprover().getOrDefault("type", policy.getApproverType());
            policy.setApproverType(type);
            Number roleIdNum = (Number) request.getApprover().get("roleId");
            if (roleIdNum != null) {
                policy.setApproverRoleId(roleIdNum.longValue());
            }
        }

        if (request.getConditions() != null) {
            policy.setConditions(String.join(",", request.getConditions()));
        }

        policy.setUpdatedAt(LocalDateTime.now());
        return policyRepository.save(policy);
    }

    public OnboardingApprovalEligibilityResponse checkEligibility(Long onboardingId) {
        Onboarding onboarding = securityValidator.validateAndGetOnboarding(onboardingId);
        User currentUser = securityValidator.getAuthenticatedUser();

        Long orgId = TenantContext.getOrganizationId();
        String currentStatus = onboarding.getStatus();

        // Resolve active policy for current status and action APPROVE
        Optional<OnboardingApprovalPolicy> policyOpt = policyRepository
                .findByOrganizationIdAndCurrentStatusAndActionAndActiveTrue(orgId, currentStatus, "APPROVE");

        if (policyOpt.isEmpty()) {
            policyOpt = policyRepository.findByOrganizationIdIsNullAndCurrentStatusAndActionAndActiveTrue(currentStatus, "APPROVE");
        }

        OnboardingApprovalEligibilityResponse response = new OnboardingApprovalEligibilityResponse();
        response.setOnboardingId(onboardingId);
        response.setCurrentStatus(currentStatus);
        response.setAction("APPROVE");
        response.setNextStatus("APPROVED");

        Map<String, Object> ruleMap = new LinkedHashMap<>();
        if (policyOpt.isPresent()) {
            OnboardingApprovalPolicy policy = policyOpt.get();
            ruleMap.put("policyId", policy.getPolicyId());
            ruleMap.put("type", policy.getApproverType());
            if (policy.getApproverRoleId() != null) {
                ruleMap.put("roleId", policy.getApproverRoleId());
            }
            response.setApprovalRule(ruleMap);

            boolean isEligible = isUserEligibleForPolicy(currentUser, onboarding, policy);
            response.setEligible(isEligible);
        } else {
            ruleMap.put("type", "CONFIGURED_ACTOR");
            response.setApprovalRule(ruleMap);
            response.setEligible(true); // Default fallback allows authenticated org users
        }

        List<Map<String, Object>> approvers = new ArrayList<>();
        Map<String, Object> approverItem = new LinkedHashMap<>();
        approverItem.put("employeeId", currentUser.getId());
        approverItem.put("email", currentUser.getWorkEmail());
        approverItem.put("eligible", response.isEligible());
        approvers.add(approverItem);

        response.setEligibleApprovers(approvers);
        response.setPendingConditions(Collections.emptyList());

        return response;
    }

    private boolean isUserEligibleForPolicy(User currentUser, Onboarding onboarding, OnboardingApprovalPolicy policy) {
        String type = policy.getApproverType();
        if ("EMPLOYEE".equalsIgnoreCase(type)) {
            return onboarding.getEmployee() != null && onboarding.getEmployee().getEmail() != null
                    && onboarding.getEmployee().getEmail().equalsIgnoreCase(currentUser.getWorkEmail());
        } else if ("REPORTING_MANAGER".equalsIgnoreCase(type) || "MANAGER".equalsIgnoreCase(type)) {
            return onboarding.getManager() != null && onboarding.getManager().getEmail() != null
                    && onboarding.getManager().getEmail().equalsIgnoreCase(currentUser.getWorkEmail());
        } else if ("HR_OWNER".equalsIgnoreCase(type)) {
            return onboarding.getHrOwnerId() != null && onboarding.getHrOwnerId().equalsIgnoreCase(String.valueOf(currentUser.getId()));
        } else if ("CONFIGURED_ROLE".equalsIgnoreCase(type)) {
            if (policy.getApproverRoleId() != null && currentUser.getRole() != null) {
                return currentUser.getRole().getId() != null && currentUser.getRole().getId().equals(policy.getApproverRoleId());
            }
            return true;
        }
        return true;
    }

    @Transactional
    public Map<String, Object> approveOnboardingWithPolicy(Long onboardingId, String remarks) {
        User currentUser = securityValidator.getAuthenticatedUser();

        OnboardingApprovalEligibilityResponse eligibility = checkEligibility(onboardingId);
        if (!eligibility.isEligible()) {
            throw new IllegalArgumentException("User " + currentUser.getWorkEmail() + " is not authorized by the Organization Approval Policy to perform action APPROVE.");
        }

        lifecycleService.updateStatus(onboardingId, "APPROVED", remarks != null ? remarks : "Onboarding approved per Organization Approval Policy");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("onboardingId", onboardingId);
        result.put("status", "APPROVED");
        result.put("approvedBy", currentUser.getWorkEmail());
        result.put("remarks", remarks);
        result.put("approvedAt", LocalDateTime.now());
        return result;
    }
}
