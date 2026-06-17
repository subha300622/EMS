package com.example.ems.performance.repository;

import com.example.ems.performance.entity.GoalProgressHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GoalProgressHistoryRepository extends JpaRepository<GoalProgressHistory, Long> {
    List<GoalProgressHistory> findByGoalIdOrderByUpdatedAtDesc(Long goalId);
}
