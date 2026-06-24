package com.example.ems.performance.manager.repository;

import com.example.ems.performance.manager.entity.PerformanceGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("managerPerformanceGoalRepository")
public interface PerformanceGoalRepository extends JpaRepository<PerformanceGoal, Long> {
    List<PerformanceGoal> findByReviewId(Long reviewId);
}
