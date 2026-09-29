package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppraisalAssessmentRepository extends JpaRepository<AppraisalAssessment, Long> {
    Optional<AppraisalAssessment> findByAppraisalId(Long appraisalId);
}
