package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingRecurrenceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingRecurrenceConfigRepository extends JpaRepository<TrainingRecurrenceConfig, Long> {
    Optional<TrainingRecurrenceConfig> findByTrainingId(Long trainingId);
    void deleteByTrainingId(Long trainingId);
}
