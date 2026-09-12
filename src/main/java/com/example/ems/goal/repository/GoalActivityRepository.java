package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalActivityRepository extends JpaRepository<GoalActivity, Long> {

    List<GoalActivity> findByOrganizationIdAndGoalIdOrderByCreatedAtDesc(Long organizationId, Long goalId);
}
