package com.example.ems.goal.service;

import com.example.ems.goal.domain.GoalActivity;
import com.example.ems.goal.repository.GoalActivityRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoalActivityService {

    @Autowired
    private GoalActivityRepository activityRepository;

    @Transactional
    public GoalActivity logActivity(Long goalId, Long actorId, String actorName, String actorRole, String action, String description, String metadata) {
        Long orgId = TenantContext.requireOrganizationId();
        GoalActivity activity = new GoalActivity();
        activity.setOrganizationId(orgId);
        activity.setGoalId(goalId);
        activity.setActorEmployeeId(actorId);
        activity.setActorName(actorName);
        activity.setActorRole(actorRole);
        activity.setAction(action);
        activity.setDescription(description);
        activity.setMetadata(metadata);
        return activityRepository.save(activity);
    }

    public List<GoalActivity> getGoalActivities(Long goalId) {
        Long orgId = TenantContext.requireOrganizationId();
        return activityRepository.findByOrganizationIdAndGoalIdOrderByCreatedAtDesc(orgId, goalId);
    }
}
