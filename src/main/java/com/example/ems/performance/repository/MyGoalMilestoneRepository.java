package com.example.ems.performance.repository;

import com.example.ems.performance.entity.MyGoalMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyGoalMilestoneRepository extends JpaRepository<MyGoalMilestone, Long> {
}
