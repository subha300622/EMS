package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingAssessmentSubmission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingAssessmentSubmissionRepository extends JpaRepository<TrainingAssessmentSubmission, Long> {
    List<TrainingAssessmentSubmission> findByEnrollmentId(Long enrollmentId);
}
