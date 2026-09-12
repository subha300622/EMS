package com.example.ems.goal.service;

import com.example.ems.goal.domain.Goal;
import com.example.ems.goal.repository.GoalRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoalCascadeService {

    @Autowired
    @Qualifier("enterpriseGoalRepository")
    private GoalRepository goalRepository;

    @Transactional
    public void recalculateParentProgress(Long parentGoalId) {
        if (parentGoalId == null) return;

        Long orgId = TenantContext.requireOrganizationId();
        Goal parentGoal = goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(parentGoalId, orgId).orElse(null);
        if (parentGoal == null) return;

        List<Goal> children = goalRepository.findByOrganizationIdAndParentGoalIdAndIsDeletedFalse(orgId, parentGoalId);
        if (children.isEmpty()) return;

        long totalWeightedProgress = 0;
        long totalWeightage = 0;

        for (Goal child : children) {
            int weightage = (child.getWeightage() != null && child.getWeightage() > 0) ? child.getWeightage() : 1;
            int childProgress = (child.getProgress() != null) ? child.getProgress() : 0;
            totalWeightedProgress += ((long) childProgress * weightage);
            totalWeightage += weightage;
        }

        if (totalWeightage > 0) {
            int calculatedProgress = (int) Math.round((double) totalWeightedProgress / totalWeightage);
            parentGoal.setProgress(Math.min(100, Math.max(0, calculatedProgress)));
            goalRepository.save(parentGoal);

            // Recurse up the hierarchy if parent also has a parent
            if (parentGoal.getParentGoalId() != null) {
                recalculateParentProgress(parentGoal.getParentGoalId());
            }
        }
    }
}
