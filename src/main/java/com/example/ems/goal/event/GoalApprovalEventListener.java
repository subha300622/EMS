package com.example.ems.goal.event;

import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.goal.domain.Goal;
import com.example.ems.goal.repository.GoalRepository;
import com.example.ems.goal.service.GoalActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GoalApprovalEventListener {

    @Autowired
    @Qualifier("enterpriseGoalRepository")
    private GoalRepository goalRepository;

    @Autowired
    private GoalActivityService activityService;

    @EventListener
    @Transactional
    public void handleApprovalCompleted(ApprovalWorkflowCompletedEvent event) {
        if (event == null || event.getWorkflowType() == null) return;

        WorkflowType type = event.getWorkflowType();
        if (type == WorkflowType.GOAL_APPROVAL || type == WorkflowType.GOAL_CREATION_APPROVAL || type == WorkflowType.GOAL_COMPLETION_APPROVAL) {
            String goalIdStr = event.getBusinessReferenceId();
            if (goalIdStr == null || goalIdStr.isBlank()) return;

            try {
                Long goalId = Long.parseLong(goalIdStr.trim());
                Goal goal = goalRepository.findById(goalId).orElse(null);
                if (goal == null) return;

                if (event.getStatus() == ApprovalStatus.APPROVED) {
                    if (type == WorkflowType.GOAL_CREATION_APPROVAL || type == WorkflowType.GOAL_APPROVAL) {
                        goal.setStatus("ACTIVE");
                        activityService.logActivity(goalId, null, "Approval Engine", "SYSTEM", "GOAL_APPROVED", "Goal creation approved", null);
                    } else if (type == WorkflowType.GOAL_COMPLETION_APPROVAL) {
                        goal.setStatus("COMPLETED");
                        goal.setProgress(100);
                        activityService.logActivity(goalId, null, "Approval Engine", "SYSTEM", "GOAL_COMPLETED", "Goal completion approved", null);
                    }
                } else if (event.getStatus() == ApprovalStatus.REJECTED) {
                    goal.setStatus("DRAFT");
                    activityService.logActivity(goalId, null, "Approval Engine", "SYSTEM", "GOAL_REJECTED", "Goal approval rejected", null);
                }
                goalRepository.save(goal);
            } catch (Exception ignored) {}
        }
    }
}
