package com.example.ems.performance.repository;

import com.example.ems.performance.entity.PerformanceGoal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PerformanceGoalRepository extends JpaRepository<PerformanceGoal, Long> {
    List<PerformanceGoal> findByEmployeeId(Long employeeId);
    List<PerformanceGoal> findByCycleId(Long cycleId);
    List<PerformanceGoal> findByStatus(String status);
}
