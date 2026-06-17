package com.example.ems.performance.repository;

import com.example.ems.performance.entity.GoalKeyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalKeyResultRepository extends JpaRepository<GoalKeyResult, Long> {
}
