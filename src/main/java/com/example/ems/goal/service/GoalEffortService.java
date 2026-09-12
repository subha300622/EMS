package com.example.ems.goal.service;

import com.example.ems.goal.domain.Goal;
import com.example.ems.goal.domain.GoalEffort;
import com.example.ems.goal.dto.GoalEffortRequest;
import com.example.ems.goal.repository.GoalEffortRepository;
import com.example.ems.goal.repository.GoalRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoalEffortService {

    @Autowired
    @Qualifier("enterpriseGoalRepository")
    private GoalRepository goalRepository;

    @Autowired
    private GoalEffortRepository effortRepository;

    @Autowired
    private GoalActivityService activityService;

    @Transactional
    public GoalEffort logEffort(Long goalId, GoalEffortRequest request, Long employeeId, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        Goal goal = goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(goalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));

        GoalEffort entry = new GoalEffort();
        entry.setOrganizationId(orgId);
        entry.setGoalId(goalId);
        entry.setEmployeeId(employeeId);
        entry.setWorkDate(request.getWorkDate());
        entry.setHours(request.getHours());
        entry.setDescription(request.getDescription());

        GoalEffort saved = effortRepository.save(entry);

        // Recalculate total actual hours for goal
        Double totalActual = effortRepository.sumHoursByOrganizationIdAndGoalId(orgId, goalId);
        goal.setActualHours(totalActual != null ? totalActual : 0.0);
        goalRepository.save(goal);

        // Activity log
        activityService.logActivity(
                goalId,
                employeeId,
                actorName,
                actorRole,
                "EFFORT_LOGGED",
                "Logged " + request.getHours() + " hours for work date " + request.getWorkDate(),
                "{\"hours\":" + request.getHours() + ",\"totalActualHours\":" + goal.getActualHours() + "}"
        );

        return saved;
    }

    public List<GoalEffort> getEffortEntries(Long goalId) {
        Long orgId = TenantContext.requireOrganizationId();
        return effortRepository.findByOrganizationIdAndGoalIdOrderByWorkDateDesc(orgId, goalId);
    }

    public GoalEffort getEffortById(Long effortId) {
        Long orgId = TenantContext.requireOrganizationId();
        return effortRepository.findByIdAndOrganizationId(effortId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Effort entry not found with ID: " + effortId));
    }

    @Transactional
    public GoalEffort updateEffort(Long effortId, GoalEffortRequest request, Long employeeId, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        GoalEffort entry = effortRepository.findByIdAndOrganizationId(effortId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Effort entry not found with ID: " + effortId));
        if (request.getHours() != null) entry.setHours(request.getHours());
        if (request.getWorkDate() != null) entry.setWorkDate(request.getWorkDate());
        if (request.getDescription() != null) entry.setDescription(request.getDescription());
        GoalEffort saved = effortRepository.save(entry);

        Double totalActual = effortRepository.sumHoursByOrganizationIdAndGoalId(orgId, entry.getGoalId());
        Goal goal = goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(entry.getGoalId(), orgId).orElse(null);
        if (goal != null) {
            goal.setActualHours(totalActual != null ? totalActual : 0.0);
            goalRepository.save(goal);
        }
        return saved;
    }

    @Transactional
    public void deleteEffort(Long effortId, Long employeeId, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        GoalEffort entry = effortRepository.findByIdAndOrganizationId(effortId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Effort entry not found with ID: " + effortId));
        Long goalId = entry.getGoalId();
        effortRepository.delete(entry);

        Double totalActual = effortRepository.sumHoursByOrganizationIdAndGoalId(orgId, goalId);
        Goal goal = goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(goalId, orgId).orElse(null);
        if (goal != null) {
            goal.setActualHours(totalActual != null ? totalActual : 0.0);
            goalRepository.save(goal);
        }
    }
}
