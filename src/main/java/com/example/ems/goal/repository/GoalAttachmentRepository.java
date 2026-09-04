package com.example.ems.goal.repository;


import com.example.ems.goal.domain.GoalAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalAttachmentRepository extends JpaRepository<GoalAttachment, Long> {

    List<GoalAttachment> findByOrganizationIdAndGoalIdOrderByUploadedAtDesc(Long organizationId, Long goalId);

    Optional<GoalAttachment> findByIdAndOrganizationId(Long id, Long organizationId);
}
