package com.example.ems.performance.repository;

import com.example.ems.performance.entity.GoalComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GoalCommentRepository extends JpaRepository<GoalComment, Long> {
    List<GoalComment> findByGoalId(Long goalId);
}
