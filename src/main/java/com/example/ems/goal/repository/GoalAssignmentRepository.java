package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalAssignmentRepository extends JpaRepository<GoalAssignment, Long> {

    List<GoalAssignment> findByOrganizationIdAndGoalIdAndIsActiveTrue(Long organizationId, Long goalId);

    List<GoalAssignment> findByOrganizationIdAndAssignedToEmployeeIdAndIsActiveTrue(Long organizationId, Long assignedToEmployeeId);

    List<GoalAssignment> findByOrganizationIdAndDepartmentIdAndIsActiveTrue(Long organizationId, Long departmentId);

    List<GoalAssignment> findByOrganizationIdAndTeamIdAndIsActiveTrue(Long organizationId, Long teamId);

    Optional<GoalAssignment> findByIdAndOrganizationId(Long id, Long organizationId);
}
