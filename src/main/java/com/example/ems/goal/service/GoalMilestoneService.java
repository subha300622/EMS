package com.example.ems.goal.service;

import com.example.ems.goal.domain.GoalMilestone;
import com.example.ems.goal.dto.GoalMilestoneRequest;
import com.example.ems.goal.repository.GoalMilestoneRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoalMilestoneService {

    @Autowired
    private GoalMilestoneRepository milestoneRepository;

    @Autowired
    private GoalActivityService activityService;

    @Transactional
    public GoalMilestone addMilestone(Long goalId, GoalMilestoneRequest request, Long actorId, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        GoalMilestone milestone = new GoalMilestone();
        milestone.setOrganizationId(orgId);
        milestone.setGoalId(goalId);
        milestone.setName(request.getName());
        milestone.setDescription(request.getDescription());
        milestone.setTargetDate(request.getTargetDate());
        milestone.setWeightage(request.getWeightage() != null ? request.getWeightage() : 1);
        milestone.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");

        GoalMilestone saved = milestoneRepository.save(milestone);

        activityService.logActivity(
                goalId,
                actorId,
                actorName,
                actorRole,
                "MILESTONE_CREATED",
                "Created milestone: " + request.getName(),
                "{\"milestoneId\":" + saved.getId() + "}"
        );

        return saved;
    }

    @Transactional
    public GoalMilestone completeMilestone(Long milestoneId, Long actorId, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        GoalMilestone milestone = milestoneRepository.findByIdAndOrganizationId(milestoneId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found with ID: " + milestoneId));

        milestone.setStatus("COMPLETED");
        milestone.setCompletedAt(LocalDateTime.now());
        milestone.setCompletedBy(actorId);

        GoalMilestone saved = milestoneRepository.save(milestone);

        activityService.logActivity(
                milestone.getGoalId(),
                actorId,
                actorName,
                actorRole,
                "MILESTONE_COMPLETED",
                "Completed milestone: " + milestone.getName(),
                "{\"milestoneId\":" + milestoneId + "}"
        );

        return saved;
    }

    public List<GoalMilestone> getMilestones(Long goalId) {
        Long orgId = TenantContext.requireOrganizationId();
        return milestoneRepository.findByOrganizationIdAndGoalIdOrderByTargetDateAsc(orgId, goalId);
    }

    public GoalMilestone getMilestoneById(Long milestoneId) {
        Long orgId = TenantContext.requireOrganizationId();
        return milestoneRepository.findByIdAndOrganizationId(milestoneId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found with ID: " + milestoneId));
    }

    @Transactional
    public GoalMilestone updateMilestone(Long milestoneId, GoalMilestoneRequest request, Long actorId, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        GoalMilestone milestone = milestoneRepository.findByIdAndOrganizationId(milestoneId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found with ID: " + milestoneId));
        if (request.getName() != null) milestone.setName(request.getName());
        if (request.getDescription() != null) milestone.setDescription(request.getDescription());
        if (request.getTargetDate() != null) milestone.setTargetDate(request.getTargetDate());
        if (request.getWeightage() != null) milestone.setWeightage(request.getWeightage());
        if (request.getStatus() != null) milestone.setStatus(request.getStatus());
        return milestoneRepository.save(milestone);
    }

    @Transactional
    public void deleteMilestone(Long milestoneId, Long actorId, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        GoalMilestone milestone = milestoneRepository.findByIdAndOrganizationId(milestoneId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found with ID: " + milestoneId));
        milestoneRepository.delete(milestone);
    }
}
