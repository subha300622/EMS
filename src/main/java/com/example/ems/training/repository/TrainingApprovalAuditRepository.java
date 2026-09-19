package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingApprovalAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingApprovalAuditRepository extends JpaRepository<TrainingApprovalAudit, Long> {
    List<TrainingApprovalAudit> findByTrainingIdOrderByCreatedAtDesc(Long trainingId);
}
