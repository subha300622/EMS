package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalEffort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalEffortRepository extends JpaRepository<GoalEffort, Long> {

    List<GoalEffort> findByOrganizationIdAndGoalIdOrderByWorkDateDesc(Long organizationId, Long goalId);

    Optional<GoalEffort> findByIdAndOrganizationId(Long id, Long organizationId);

    @Query("SELECT COALESCE(SUM(e.hours), 0.0) FROM GoalEffort e WHERE e.organizationId = :orgId AND e.goalId = :goalId")
    Double sumHoursByOrganizationIdAndGoalId(@Param("orgId") Long orgId, @Param("goalId") Long goalId);
}
