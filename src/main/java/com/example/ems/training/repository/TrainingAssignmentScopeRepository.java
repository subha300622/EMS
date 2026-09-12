package com.example.ems.training.repository;

import com.example.ems.training.entity.AssignmentTargetType;
import com.example.ems.training.entity.TrainingAssignmentScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingAssignmentScopeRepository extends JpaRepository<TrainingAssignmentScope, Long> {

    List<TrainingAssignmentScope> findByTrainingIdAndStatus(Long trainingId, String status);

    List<TrainingAssignmentScope> findByOrganizationIdAndTrainingIdAndStatus(Long organizationId, Long trainingId, String status);

    Optional<TrainingAssignmentScope> findByTrainingIdAndAssignmentTypeAndTargetIdAndStatus(
            Long trainingId, AssignmentTargetType assignmentType, String targetId, String status);

    List<TrainingAssignmentScope> findByOrganizationIdAndAssignmentTypeAndTargetIdAndStatus(
            Long organizationId, AssignmentTargetType assignmentType, String targetId, String status);

    List<TrainingAssignmentScope> findByOrganizationIdAndStatus(Long organizationId, String status);
}
