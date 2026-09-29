package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingParticipant;
import com.example.ems.training.entity.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingParticipantRepository extends JpaRepository<TrainingParticipant, Long> {
    List<TrainingParticipant> findByTrainingId(Long trainingId);
    List<TrainingParticipant> findByEmployeeId(Long employeeId);
    List<TrainingParticipant> findByEmployeeIdAndParticipationStatus(Long employeeId, ParticipationStatus participationStatus);
    Optional<TrainingParticipant> findByTrainingIdAndEmployeeId(Long trainingId, Long employeeId);
    boolean existsByTrainingIdAndEmployeeId(Long trainingId, Long employeeId);
    void deleteByTrainingIdAndEmployeeId(Long trainingId, Long employeeId);
    long countByTrainingId(Long trainingId);
    long countByTrainingIdAndParticipationStatus(Long trainingId, ParticipationStatus participationStatus);
}
