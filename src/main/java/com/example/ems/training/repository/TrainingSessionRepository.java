package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingSession;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    List<TrainingSession> findByCourseId(Long courseId);
}
