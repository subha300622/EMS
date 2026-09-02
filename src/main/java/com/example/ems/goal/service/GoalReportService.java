package com.example.ems.goal.service;

import com.example.ems.goal.domain.Goal;
import com.example.ems.goal.dto.GoalResponse;
import com.example.ems.goal.repository.GoalRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalReportService {

    @Autowired
    @Qualifier("enterpriseGoalRepository")
    private GoalRepository goalRepository;

    public List<GoalResponse> getAchievementReport(String category, String priority, String status) {
        Long orgId = TenantContext.requireOrganizationId();
        List<Goal> goals = goalRepository.findByOrganizationIdAndOwnerIdAndIsDeletedFalse(orgId, null);
        if (goals.isEmpty()) {
            goals = goalRepository.findAll().stream()
                    .filter(g -> orgId.equals(g.getOrganizationId()) && Boolean.FALSE.equals(g.getIsDeleted()))
                    .collect(Collectors.toList());
        }

        return goals.stream()
                .filter(g -> (category == null || category.equalsIgnoreCase(g.getCategory())))
                .filter(g -> (priority == null || priority.equalsIgnoreCase(g.getPriority())))
                .filter(g -> (status == null || status.equalsIgnoreCase(g.getStatus())))
                .map(GoalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<GoalResponse> getOverdueReport() {
        Long orgId = TenantContext.requireOrganizationId();
        List<Goal> goals = goalRepository.findAll().stream()
                .filter(g -> orgId.equals(g.getOrganizationId()) && Boolean.FALSE.equals(g.getIsDeleted()))
                .filter(g -> !"COMPLETED".equalsIgnoreCase(g.getStatus()) && !"CANCELLED".equalsIgnoreCase(g.getStatus()))
                .filter(g -> g.getEndDate() != null && java.time.LocalDate.now().isAfter(g.getEndDate()))
                .collect(Collectors.toList());

        return goals.stream().map(GoalResponse::fromEntity).collect(Collectors.toList());
    }

    public List<GoalResponse> getProgressReport() {
        Long orgId = TenantContext.requireOrganizationId();
        return goalRepository.findAll().stream()
                .filter(g -> orgId.equals(g.getOrganizationId()) && Boolean.FALSE.equals(g.getIsDeleted()))
                .map(GoalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<GoalResponse> getEmployeePerformanceReport(Long employeeId) {
        Long orgId = TenantContext.requireOrganizationId();
        return goalRepository.findAll().stream()
                .filter(g -> orgId.equals(g.getOrganizationId()) && Boolean.FALSE.equals(g.getIsDeleted()))
                .filter(g -> employeeId == null || employeeId.equals(g.getOwnerId()))
                .map(GoalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<GoalResponse> getTeamPerformanceReport(Long teamId) {
        Long orgId = TenantContext.requireOrganizationId();
        return goalRepository.findAll().stream()
                .filter(g -> orgId.equals(g.getOrganizationId()) && Boolean.FALSE.equals(g.getIsDeleted()))
                .map(GoalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<GoalResponse> getDepartmentPerformanceReport(Long departmentId) {
        Long orgId = TenantContext.requireOrganizationId();
        return goalRepository.findAll().stream()
                .filter(g -> orgId.equals(g.getOrganizationId()) && Boolean.FALSE.equals(g.getIsDeleted()))
                .map(GoalResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
