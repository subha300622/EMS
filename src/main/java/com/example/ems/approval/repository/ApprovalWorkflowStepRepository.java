package com.example.ems.approval.repository;

import com.example.ems.approval.entity.ApprovalWorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalWorkflowStepRepository extends JpaRepository<ApprovalWorkflowStep, Long> {
    List<ApprovalWorkflowStep> findByWorkflowDefinitionIdOrderByStepOrderAsc(Long workflowDefinitionId);
}
