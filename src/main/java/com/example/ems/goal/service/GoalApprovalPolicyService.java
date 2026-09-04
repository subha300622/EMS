package com.example.ems.goal.service;

import com.example.ems.goal.domain.Goal;
import com.example.ems.goal.domain.GoalApprovalPolicy;
import com.example.ems.goal.dto.GoalApprovalPolicyRequest;
import com.example.ems.goal.repository.GoalApprovalPolicyRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GoalApprovalPolicyService {

    @Autowired
    private GoalApprovalPolicyRepository policyRepository;

    @Transactional
    public GoalApprovalPolicy createPolicy(GoalApprovalPolicyRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        GoalApprovalPolicy policy = new GoalApprovalPolicy();
        policy.setOrganizationId(orgId);
        policy.setPolicyName(request.getPolicyName());
        policy.setModule("GOAL");
        policy.setAction(request.getAction().toUpperCase());
        policy.setApprovalRequired(request.getApprovalRequired() != null ? request.getApprovalRequired() : true);
        policy.setApprovalType(request.getApprovalType());
        policy.setPriority(request.getPriority());
        policy.setGoalType(request.getGoalType());
        policy.setWeightageThreshold(request.getWeightageThreshold() != null ? request.getWeightageThreshold() : 0);
        policy.setEstimatedHoursThreshold(request.getEstimatedHoursThreshold() != null ? request.getEstimatedHoursThreshold() : 0.0);
        policy.setDepartmentId(request.getDepartmentId());
        policy.setApproverRole(request.getApproverRole());
        policy.setApprovalLevels(request.getApprovalLevels() != null ? request.getApprovalLevels() : 1);
        policy.setAutoApproval(request.getAutoApproval() != null ? request.getAutoApproval() : false);
        policy.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return policyRepository.save(policy);
    }

    @Transactional
    public GoalApprovalPolicy updatePolicy(Long policyId, GoalApprovalPolicyRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        GoalApprovalPolicy policy = policyRepository.findByIdAndOrganizationId(policyId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Approval policy not found with ID: " + policyId));

        if (request.getPolicyName() != null) policy.setPolicyName(request.getPolicyName());
        if (request.getAction() != null) policy.setAction(request.getAction().toUpperCase());
        if (request.getApprovalRequired() != null) policy.setApprovalRequired(request.getApprovalRequired());
        if (request.getApprovalType() != null) policy.setApprovalType(request.getApprovalType());
        if (request.getPriority() != null) policy.setPriority(request.getPriority());
        if (request.getGoalType() != null) policy.setGoalType(request.getGoalType());
        if (request.getWeightageThreshold() != null) policy.setWeightageThreshold(request.getWeightageThreshold());
        if (request.getEstimatedHoursThreshold() != null) policy.setEstimatedHoursThreshold(request.getEstimatedHoursThreshold());
        if (request.getDepartmentId() != null) policy.setDepartmentId(request.getDepartmentId());
        if (request.getApproverRole() != null) policy.setApproverRole(request.getApproverRole());
        if (request.getApprovalLevels() != null) policy.setApprovalLevels(request.getApprovalLevels());
        if (request.getAutoApproval() != null) policy.setAutoApproval(request.getAutoApproval());
        if (request.getIsActive() != null) policy.setIsActive(request.getIsActive());

        return policyRepository.save(policy);
    }

    public List<GoalApprovalPolicy> getPolicies() {
        Long orgId = TenantContext.requireOrganizationId();
        return policyRepository.findByOrganizationIdAndIsActiveTrue(orgId);
    }

    @Transactional
    public void deletePolicy(Long policyId) {
        Long orgId = TenantContext.requireOrganizationId();
        GoalApprovalPolicy policy = policyRepository.findByIdAndOrganizationId(policyId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Approval policy not found with ID: " + policyId));
        policy.setIsActive(false);
        policyRepository.save(policy);
    }

    /**
     * Evaluates applicable approval policy for a goal & action (CREATE / COMPLETE).
     * Policy Engine decides whether approval is required and who approves.
     */
    public Optional<GoalApprovalPolicy> evaluateApprovalPolicy(Goal goal, String action) {
        Long orgId = goal.getOrganizationId();
        List<GoalApprovalPolicy> policies = policyRepository.findByOrganizationIdAndActionAndIsActiveTrue(orgId, action.toUpperCase());

        for (GoalApprovalPolicy policy : policies) {
            // Check condition matches
            if (policy.getGoalType() != null && !"ALL".equalsIgnoreCase(policy.getGoalType())) {
                if (!policy.getGoalType().equalsIgnoreCase(goal.getType())) {
                    continue;
                }
            }
            if (policy.getPriority() != null && !"ALL".equalsIgnoreCase(policy.getPriority())) {
                if (!policy.getPriority().equalsIgnoreCase(goal.getPriority())) {
                    continue;
                }
            }
            if (policy.getWeightageThreshold() != null && policy.getWeightageThreshold() > 0) {
                int weightage = goal.getWeightage() != null ? goal.getWeightage() : 0;
                if (weightage < policy.getWeightageThreshold()) {
                    continue;
                }
            }
            if (policy.getEstimatedHoursThreshold() != null && policy.getEstimatedHoursThreshold() > 0) {
                double hours = goal.getEstimatedHours() != null ? goal.getEstimatedHours() : 0.0;
                if (hours < policy.getEstimatedHoursThreshold()) {
                    continue;
                }
            }

            // Matching policy found!
            return Optional.of(policy);
        }

        return Optional.empty();
    }
}
