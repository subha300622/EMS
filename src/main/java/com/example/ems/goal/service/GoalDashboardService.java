package com.example.ems.goal.service;

import com.example.ems.goal.domain.Goal;
import com.example.ems.goal.dto.GoalDashboardResponse;
import com.example.ems.goal.repository.GoalRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoalDashboardService {

    @Autowired
    @Qualifier("enterpriseGoalRepository")
    private GoalRepository goalRepository;

    public GoalDashboardResponse getOrganizationDashboard() {
        Long orgId = TenantContext.requireOrganizationId();
        List<Goal> goals = goalRepository.findByOrganizationIdAndParentGoalIdAndIsDeletedFalse(orgId, null);
        if (goals.isEmpty()) {
            goals = goalRepository.findByOrganizationIdAndOwnerIdAndIsDeletedFalse(orgId, null);
        }
        return computeDashboard(goals);
    }

    public GoalDashboardResponse getEmployeeDashboard(Long employeeId) {
        Long orgId = TenantContext.requireOrganizationId();
        List<Goal> goals = goalRepository.findByOrganizationIdAndOwnerIdAndIsDeletedFalse(orgId, employeeId);
        return computeDashboard(goals);
    }

    private GoalDashboardResponse computeDashboard(List<Goal> goals) {
        GoalDashboardResponse dto = new GoalDashboardResponse();
        dto.setTotalGoals(goals.size());

        long active = 0, completed = 0, overdue = 0, onTrack = 0, atRisk = 0;
        double sumProgress = 0.0, totalEst = 0.0, totalAct = 0.0;

        for (Goal g : goals) {
            String status = g.getStatus() != null ? g.getStatus().toUpperCase() : "DRAFT";
            if ("ACTIVE".equals(status)) active++;
            if ("COMPLETED".equals(status)) completed++;

            sumProgress += (g.getProgress() != null ? g.getProgress() : 0);
            totalEst += (g.getEstimatedHours() != null ? g.getEstimatedHours() : 0.0);
            totalAct += (g.getActualHours() != null ? g.getActualHours() : 0.0);

            // Compute health status
            var resp = com.example.ems.goal.dto.GoalResponse.fromEntity(g);
            String health = resp.getHealthIndicator();
            if ("OVERDUE".equals(health)) overdue++;
            else if ("AT_RISK".equals(health)) atRisk++;
            else if ("ON_TRACK".equals(health)) onTrack++;
        }

        dto.setActiveGoals(active);
        dto.setCompletedGoals(completed);
        dto.setOverdueGoals(overdue);
        dto.setOnTrackGoals(onTrack);
        dto.setAtRiskGoals(atRisk);
        dto.setAverageProgress(goals.size() > 0 ? sumProgress / goals.size() : 0.0);
        dto.setCompletionRate(goals.size() > 0 ? ((double) completed / goals.size()) * 100.0 : 0.0);
        dto.setTotalEstimatedHours(totalEst);
        dto.setTotalActualHours(totalAct);
        dto.setHoursVariance(totalAct - totalEst);

        return dto;
    }
}
