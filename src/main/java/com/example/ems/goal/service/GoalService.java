package com.example.ems.goal.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.goal.domain.Goal;
import com.example.ems.goal.domain.GoalApprovalPolicy;
import com.example.ems.goal.domain.GoalConfig;
import com.example.ems.goal.dto.CreateGoalRequest;
import com.example.ems.goal.dto.GoalResponse;
import com.example.ems.goal.dto.UpdateGoalRequest;
import com.example.ems.goal.repository.GoalRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service("enterpriseGoalService")
public class GoalService {

    @Autowired
    @Qualifier("enterpriseGoalRepository")
    private GoalRepository goalRepository;

    @Autowired
    private GoalConfigService configService;

    @Autowired
    private GoalApprovalPolicyService approvalPolicyService;

    @Autowired
    private GoalActivityService activityService;

    @Autowired
    private ApprovalFacade approvalFacade;

    @Transactional
    public GoalResponse createGoal(CreateGoalRequest request, Long createdById, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        GoalConfig config = configService.getOrCreateConfig();

        if (request.getStartDate() != null && request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Start date must be before or equal to End date.");
        }

        if (request.getWeightage() != null) {
            if (request.getWeightage() < config.getMinWeightage() || request.getWeightage() > config.getMaxWeightage()) {
                throw new IllegalArgumentException("Weightage must be between " + config.getMinWeightage() + " and " + config.getMaxWeightage());
            }
        }

        Goal goal = new Goal();
        goal.setOrganizationId(orgId);
        goal.setGoalNumber(configService.generateNextGoalNumber());
        goal.setGoalName(request.getGoalName());
        goal.setDescription(request.getDescription());
        goal.setCategory(request.getCategory());
        goal.setType(request.getType());
        goal.setPriority(request.getPriority());
        goal.setWeightage(request.getWeightage() != null ? request.getWeightage() : 1);
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setTargetValue(request.getTargetValue());
        goal.setCurrentValue(0.0);
        goal.setUnitOfMeasurement(request.getUnitOfMeasurement());
        goal.setOwnerId(request.getOwnerId() != null ? request.getOwnerId() : createdById);
        goal.setEstimatedHours(request.getEstimatedHours() != null ? request.getEstimatedHours() : 0.0);
        goal.setVisibility(request.getVisibility() != null ? request.getVisibility() : "PUBLIC");
        goal.setParentGoalId(request.getParentGoalId());
        goal.setProjectId(request.getProjectId());
        goal.setCreatedBy(createdById);
        goal.setIsDeleted(false);

        // Evaluate Policy Engine for Goal Creation
        Optional<GoalApprovalPolicy> policyOpt = approvalPolicyService.evaluateApprovalPolicy(goal, "CREATE");
        boolean approvalRequired = policyOpt.map(GoalApprovalPolicy::getApprovalRequired).orElse(Boolean.TRUE.equals(config.getRequireApprovalForCreate()));

        if (approvalRequired) {
            goal.setStatus("PENDING_APPROVAL");
        } else {
            goal.setStatus("ACTIVE");
        }

        Goal saved = goalRepository.save(goal);

        // Audit log
        activityService.logActivity(saved.getId(), createdById, actorName, actorRole, "GOAL_CREATED", "Goal created: " + saved.getGoalNumber(), null);

        // Delegate to Approval Engine if required
        if (approvalRequired) {
            try {
                ApprovalContext ctx = new ApprovalContext();
                ctx.setModule("GOAL_CREATION");
                ctx.setResourceId(saved.getId().toString());
                if (saved.getOwnerId() != null) {
                    ctx.setEmployeeId(saved.getOwnerId().toString());
                }
                ctx.setOrganizationId(orgId);

                // Pass policy metadata if matched
                if (policyOpt.isPresent()) {
                    GoalApprovalPolicy p = policyOpt.get();
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("approvalType", p.getApprovalType());
                    meta.put("approverRole", p.getApproverRole());
                    meta.put("approvalLevels", p.getApprovalLevels());
                    ctx.setMetadata(meta);
                }

                approvalFacade.startApproval(ctx);
            } catch (Exception ignored) {}
        }

        return GoalResponse.fromEntity(saved);
    }

    @Transactional
    public GoalResponse updateGoal(Long goalId, UpdateGoalRequest request, Long updatedById, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        Goal goal = goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(goalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));

        if ("COMPLETED".equalsIgnoreCase(goal.getStatus()) || "CANCELLED".equalsIgnoreCase(goal.getStatus())) {
            throw new IllegalStateException("Cannot edit a " + goal.getStatus() + " goal.");
        }

        if (request.getGoalName() != null) goal.setGoalName(request.getGoalName());
        if (request.getDescription() != null) goal.setDescription(request.getDescription());
        if (request.getCategory() != null) goal.setCategory(request.getCategory());
        if (request.getType() != null) goal.setType(request.getType());
        if (request.getPriority() != null) goal.setPriority(request.getPriority());
        if (request.getWeightage() != null) goal.setWeightage(request.getWeightage());
        if (request.getStartDate() != null) goal.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) goal.setEndDate(request.getEndDate());
        if (request.getTargetValue() != null) goal.setTargetValue(request.getTargetValue());
        if (request.getUnitOfMeasurement() != null) goal.setUnitOfMeasurement(request.getUnitOfMeasurement());
        if (request.getOwnerId() != null) goal.setOwnerId(request.getOwnerId());
        if (request.getEstimatedHours() != null) goal.setEstimatedHours(request.getEstimatedHours());
        if (request.getVisibility() != null) goal.setVisibility(request.getVisibility());
        if (request.getParentGoalId() != null) goal.setParentGoalId(request.getParentGoalId());
        if (request.getProjectId() != null) goal.setProjectId(request.getProjectId());

        Goal updated = goalRepository.save(goal);
        activityService.logActivity(goalId, updatedById, actorName, actorRole, "GOAL_UPDATED", "Goal updated", null);

        return GoalResponse.fromEntity(updated);
    }

    public GoalResponse getGoalById(Long goalId) {
        Long orgId = TenantContext.requireOrganizationId();
        Goal goal = goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(goalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));
        return GoalResponse.fromEntity(goal);
    }

    public Page<GoalResponse> getAllGoals(Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        return goalRepository.findByOrganizationIdAndIsDeletedFalse(orgId, pageable)
                .map(GoalResponse::fromEntity);
    }

    public Page<GoalResponse> getMyGoals(Long employeeId, Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        return goalRepository.findByOrganizationIdAndOwnerIdAndIsDeletedFalse(orgId, employeeId, pageable)
                .map(GoalResponse::fromEntity);
    }

    @Transactional
    public void deleteGoal(Long goalId, Long deletedById, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        Goal goal = goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(goalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));

        goal.setIsDeleted(true);
        goalRepository.save(goal);

        activityService.logActivity(goalId, deletedById, actorName, actorRole, "GOAL_DELETED", "Goal soft deleted", null);
    }

    // --- Command State Machine Transitions ---

    @Transactional
    public GoalResponse activateGoal(Long goalId, Long actorId, String actorName, String actorRole) {
        Goal goal = getGoalEntity(goalId);
        goal.setStatus("ACTIVE");
        Goal saved = goalRepository.save(goal);
        activityService.logActivity(goalId, actorId, actorName, actorRole, "GOAL_ACTIVATED", "Goal activated", null);
        return GoalResponse.fromEntity(saved);
    }

    @Transactional
    public GoalResponse holdGoal(Long goalId, Long actorId, String actorName, String actorRole) {
        Goal goal = getGoalEntity(goalId);
        goal.setStatus("ON_HOLD");
        Goal saved = goalRepository.save(goal);
        activityService.logActivity(goalId, actorId, actorName, actorRole, "GOAL_HELD", "Goal put on hold", null);
        return GoalResponse.fromEntity(saved);
    }

    @Transactional
    public GoalResponse resumeGoal(Long goalId, Long actorId, String actorName, String actorRole) {
        Goal goal = getGoalEntity(goalId);
        goal.setStatus("ACTIVE");
        Goal saved = goalRepository.save(goal);
        activityService.logActivity(goalId, actorId, actorName, actorRole, "GOAL_RESUMED", "Goal resumed", null);
        return GoalResponse.fromEntity(saved);
    }

    @Transactional
    public GoalResponse completeGoal(Long goalId, Long actorId, String actorName, String actorRole) {
        Goal goal = getGoalEntity(goalId);
        GoalConfig config = configService.getOrCreateConfig();

        // Evaluate Policy Engine for Goal Completion
        Optional<GoalApprovalPolicy> policyOpt = approvalPolicyService.evaluateApprovalPolicy(goal, "COMPLETE");
        boolean approvalRequired = policyOpt.map(GoalApprovalPolicy::getApprovalRequired).orElse(Boolean.TRUE.equals(config.getRequireApprovalForComplete()));

        if (approvalRequired) {
            goal.setStatus("PENDING_APPROVAL");
            Goal saved = goalRepository.save(goal);
            try {
                ApprovalContext ctx = new ApprovalContext();
                ctx.setModule("GOAL_COMPLETION");
                ctx.setResourceId(saved.getId().toString());
                if (saved.getOwnerId() != null) {
                    ctx.setEmployeeId(saved.getOwnerId().toString());
                }
                ctx.setOrganizationId(saved.getOrganizationId());

                if (policyOpt.isPresent()) {
                    GoalApprovalPolicy p = policyOpt.get();
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("approvalType", p.getApprovalType());
                    meta.put("approverRole", p.getApproverRole());
                    meta.put("approvalLevels", p.getApprovalLevels());
                    ctx.setMetadata(meta);
                }

                approvalFacade.startApproval(ctx);
            } catch (Exception ignored) {}
            return GoalResponse.fromEntity(saved);
        } else {
            goal.setStatus("COMPLETED");
            goal.setProgress(100);
            Goal saved = goalRepository.save(goal);
            activityService.logActivity(goalId, actorId, actorName, actorRole, "GOAL_COMPLETED", "Goal completed", null);
            return GoalResponse.fromEntity(saved);
        }
    }

    @Transactional
    public GoalResponse cancelGoal(Long goalId, Long actorId, String actorName, String actorRole) {
        Goal goal = getGoalEntity(goalId);
        goal.setStatus("CANCELLED");
        Goal saved = goalRepository.save(goal);
        activityService.logActivity(goalId, actorId, actorName, actorRole, "GOAL_CANCELLED", "Goal cancelled", null);
        return GoalResponse.fromEntity(saved);
    }

    @Transactional
    public GoalResponse reopenGoal(Long goalId, Long actorId, String actorName, String actorRole) {
        Goal goal = getGoalEntity(goalId);
        goal.setStatus("ACTIVE");
        Goal saved = goalRepository.save(goal);
        activityService.logActivity(goalId, actorId, actorName, actorRole, "GOAL_REOPENED", "Goal reopened to active state", null);
        return GoalResponse.fromEntity(saved);
    }

    private Goal getGoalEntity(Long goalId) {
        Long orgId = TenantContext.requireOrganizationId();
        return goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(goalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));
    }
}
