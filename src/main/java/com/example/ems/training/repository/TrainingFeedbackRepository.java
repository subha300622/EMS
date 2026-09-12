package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingFeedbackRepository extends JpaRepository<TrainingFeedback, Long> {
    List<TrainingFeedback> findByTrainingId(Long trainingId);
    Optional<TrainingFeedback> findByTrainingIdAndEmployeeId(Long trainingId, Long employeeId);
    boolean existsByTrainingIdAndEmployeeId(Long trainingId, Long employeeId);
}
