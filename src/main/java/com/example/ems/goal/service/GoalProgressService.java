package com.example.ems.goal.service;

import com.example.ems.goal.domain.Goal;
import com.example.ems.goal.domain.GoalProgress;
import com.example.ems.goal.dto.GoalProgressRequest;
import com.example.ems.goal.event.GoalProgressUpdatedEvent;
import com.example.ems.goal.repository.GoalProgressRepository;
import com.example.ems.goal.repository.GoalRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoalProgressService {

    @Autowired
    @Qualifier("enterpriseGoalRepository")
    private GoalRepository goalRepository;

    @Autowired
    private GoalProgressRepository progressRepository;

    @Autowired
    private GoalCascadeService cascadeService;

    @Autowired
    private GoalActivityService activityService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public GoalProgress addProgressUpdate(Long goalId, GoalProgressRequest request, Long updatedById, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        Goal goal = goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(goalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));

        if ("CANCELLED".equalsIgnoreCase(goal.getStatus())) {
            throw new IllegalStateException("Cannot update progress on a CANCELLED goal.");
        }
        if ("COMPLETED".equalsIgnoreCase(goal.getStatus())) {
            throw new IllegalStateException("Cannot update progress on a COMPLETED goal unless reopened.");
        }

        int previousProgress = goal.getProgress() != null ? goal.getProgress() : 0;
        int newProgress = request.getPercentage();

        GoalProgress entry = new GoalProgress();
        entry.setOrganizationId(orgId);
        entry.setGoalId(goalId);
        entry.setPercentage(newProgress);
        entry.setTargetAchieved(request.getTargetAchieved());
        entry.setMilestoneCompleted(request.getMilestoneCompleted());
        entry.setTasksCompleted(request.getTasksCompleted());
        entry.setUpdateComment(request.getUpdateComment());
        entry.setEvidenceDocument(request.getEvidenceDocument());
        entry.setUpdatedBy(updatedById);

        GoalProgress saved = progressRepository.save(entry);

        // Update goal's latest progress value
        goal.setProgress(newProgress);
        if (request.getTargetAchieved() != null) {
            goal.setCurrentValue(request.getTargetAchieved());
        }
        goalRepository.save(goal);

        // Perform synchronous parent goal progress roll-up
        if (goal.getParentGoalId() != null) {
            cascadeService.recalculateParentProgress(goal.getParentGoalId());
        }

        // Publish domain event
        eventPublisher.publishEvent(new GoalProgressUpdatedEvent(goalId, orgId, previousProgress, newProgress, updatedById));

        // Audit log
        activityService.logActivity(
                goalId,
                updatedById,
                actorName,
                actorRole,
                "PROGRESS_UPDATED",
                "Progress updated from " + previousProgress + "% to " + newProgress + "%",
                "{" + "\"previousProgress\":" + previousProgress + ",\"newProgress\":" + newProgress + "}"
        );

        return saved;
    }

    public List<GoalProgress> getProgressHistory(Long goalId) {
        Long orgId = TenantContext.requireOrganizationId();
        return progressRepository.findByOrganizationIdAndGoalIdOrderByUpdatedAtDesc(orgId, goalId);
    }
}
