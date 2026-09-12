package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalAssignmentRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalAssignmentRuleRepository extends JpaRepository<GoalAssignmentRule, Long> {

    List<GoalAssignmentRule> findByOrganizationId(Long organizationId);
}
