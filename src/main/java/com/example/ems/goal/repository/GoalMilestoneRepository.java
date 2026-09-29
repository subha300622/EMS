package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalMilestoneRepository extends JpaRepository<GoalMilestone, Long> {

    List<GoalMilestone> findByOrganizationIdAndGoalIdOrderByTargetDateAsc(Long organizationId, Long goalId);

    Optional<GoalMilestone> findByIdAndOrganizationId(Long id, Long organizationId);
}
