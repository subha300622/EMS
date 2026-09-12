package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("enterpriseGoalCommentRepository")
public interface GoalCommentRepository extends JpaRepository<GoalComment, Long> {

    List<GoalComment> findByOrganizationIdAndGoalIdAndIsDeletedFalseOrderByCreatedAtAsc(Long organizationId, Long goalId);

    Optional<GoalComment> findByIdAndOrganizationIdAndIsDeletedFalse(Long id, Long organizationId);
}
