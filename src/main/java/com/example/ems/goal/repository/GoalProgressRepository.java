package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalProgressRepository extends JpaRepository<GoalProgress, Long> {

    List<GoalProgress> findByOrganizationIdAndGoalIdOrderByUpdatedAtDesc(Long organizationId, Long goalId);

    Optional<GoalProgress> findTopByOrganizationIdAndGoalIdOrderByUpdatedAtDesc(Long organizationId, Long goalId);
}
