package com.example.ems.goal.service;

import com.example.ems.goal.domain.Goal;
import com.example.ems.goal.domain.GoalAssignment;
import com.example.ems.goal.domain.GoalAssignmentHistory;
import com.example.ems.goal.dto.GoalAssignRequest;
import com.example.ems.goal.repository.GoalAssignmentHistoryRepository;
import com.example.ems.goal.repository.GoalAssignmentRepository;
import com.example.ems.goal.repository.GoalRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoalAssignmentService {

    @Autowired
    @Qualifier("enterpriseGoalRepository")
    private GoalRepository goalRepository;

    @Autowired
    private GoalAssignmentRepository assignmentRepository;

    @Autowired
    private GoalAssignmentHistoryRepository assignmentHistoryRepository;

    @Autowired
    private GoalActivityService activityService;

    @Transactional
    public GoalAssignment assignGoal(Long goalId, GoalAssignRequest request, Long assignedById, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        Goal goal = goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(goalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));

        GoalAssignment assignment = new GoalAssignment();
        assignment.setOrganizationId(orgId);
        assignment.setGoalId(goalId);
        assignment.setAssignedToEmployeeId(request.getAssignedToEmployeeId());
        assignment.setDepartmentId(request.getDepartmentId());
        assignment.setTeamId(request.getTeamId());
        assignment.setBranchId(request.getBranchId());
        assignment.setRoleId(request.getRoleId());
        assignment.setAssignmentLevel(request.getAssignmentLevel());
        assignment.setAssignmentType(request.getAssignmentType());
        assignment.setAssignedBy(assignedById);
        assignment.setIsActive(true);

        GoalAssignment saved = assignmentRepository.save(assignment);

        // Update owner if employee assignment
        if (request.getAssignedToEmployeeId() != null) {
            goal.setOwnerId(request.getAssignedToEmployeeId());
            goalRepository.save(goal);
        }

        // Log assignment history
        GoalAssignmentHistory history = new GoalAssignmentHistory();
        history.setOrganizationId(orgId);
        history.setGoalId(goalId);
        history.setAssignmentId(saved.getId());
        history.setAssignedTo(request.getAssignedToEmployeeId());
        history.setAssignedBy(assignedById);
        history.setDepartmentId(request.getDepartmentId());
        history.setTeamId(request.getTeamId());
        history.setRoleId(request.getRoleId());
        history.setAssignmentLevel(request.getAssignmentLevel());
        history.setAssignmentType(request.getAssignmentType());
        history.setAssignmentReason(request.getReason());
        assignmentHistoryRepository.save(history);

        // Activity log
        activityService.logActivity(
                goalId,
                assignedById,
                actorName,
                actorRole,
                "GOAL_ASSIGNED",
                "Goal assigned to level " + request.getAssignmentLevel() + " (Type: " + request.getAssignmentType() + ")",
                "{" + "\"assignmentLevel\":\"" + request.getAssignmentLevel() + "\",\"assignmentType\":\"" + request.getAssignmentType() + "\"}"
        );

        return saved;
    }

    @Transactional
    public GoalAssignment reassignGoal(Long goalId, GoalAssignRequest request, Long assignedById, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        Goal goal = goalRepository.findByIdAndOrganizationIdAndIsDeletedFalse(goalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));

        Long previousOwner = goal.getOwnerId();

        // Deactivate existing assignments
        List<GoalAssignment> existing = assignmentRepository.findByOrganizationIdAndGoalIdAndIsActiveTrue(orgId, goalId);
        for (GoalAssignment active : existing) {
            active.setIsActive(false);
            assignmentRepository.save(active);
        }

        // Create new assignment
        GoalAssignRequest req = new GoalAssignRequest();
        req.setAssignedToEmployeeId(request.getAssignedToEmployeeId());
        req.setDepartmentId(request.getDepartmentId());
        req.setTeamId(request.getTeamId());
        req.setBranchId(request.getBranchId());
        req.setRoleId(request.getRoleId());
        req.setAssignmentLevel(request.getAssignmentLevel());
        req.setAssignmentType("REASSIGNMENT");
        req.setReason(request.getReason());

        GoalAssignment newAssign = assignGoal(goalId, req, assignedById, actorName, actorRole);

        // Log history unassignment
        GoalAssignmentHistory history = new GoalAssignmentHistory();
        history.setOrganizationId(orgId);
        history.setGoalId(goalId);
        history.setAssignmentId(newAssign.getId());
        history.setAssignedTo(request.getAssignedToEmployeeId());
        history.setAssignedBy(assignedById);
        history.setPreviousOwnerId(previousOwner);
        history.setDepartmentId(request.getDepartmentId());
        history.setTeamId(request.getTeamId());
        history.setRoleId(request.getRoleId());
        history.setAssignmentLevel(request.getAssignmentLevel());
        history.setAssignmentType("REASSIGNMENT");
        history.setAssignmentReason(request.getReason());
        history.setUnassignedAt(LocalDateTime.now());
        assignmentHistoryRepository.save(history);

        return newAssign;
    }

    public List<GoalAssignment> getActiveAssignments(Long goalId) {
        Long orgId = TenantContext.requireOrganizationId();
        return assignmentRepository.findByOrganizationIdAndGoalIdAndIsActiveTrue(orgId, goalId);
    }

    public List<GoalAssignmentHistory> getAssignmentHistory(Long goalId) {
        Long orgId = TenantContext.requireOrganizationId();
        return assignmentHistoryRepository.findByOrganizationIdAndGoalIdOrderByAssignedAtDesc(orgId, goalId);
    }
}
