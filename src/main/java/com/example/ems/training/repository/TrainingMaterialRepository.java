package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingMaterialRepository extends JpaRepository<TrainingMaterial, Long> {
    List<TrainingMaterial> findByTrainingId(Long trainingId);
}
