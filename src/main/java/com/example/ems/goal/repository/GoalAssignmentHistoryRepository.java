package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalAssignmentHistoryRepository extends JpaRepository<GoalAssignmentHistory, Long> {

    List<GoalAssignmentHistory> findByOrganizationIdAndGoalIdOrderByAssignedAtDesc(Long organizationId, Long goalId);
}
