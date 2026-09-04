package com.example.ems.approval.repository;

import com.example.ems.approval.entity.ApprovalWorkflowDefinition;
import com.example.ems.approval.entity.WorkflowType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalWorkflowDefinitionRepository extends JpaRepository<ApprovalWorkflowDefinition, Long> {

    @Query("SELECT d FROM ApprovalWorkflowDefinition d WHERE d.workflowType = :workflowType " +
           "AND d.status = 'ACTIVE' " +
           "AND (:orgId IS NULL OR d.organization.id = :orgId OR d.organization IS NULL) " +
           "ORDER BY d.organization.id DESC NULLS LAST, d.version DESC")
    Optional<ApprovalWorkflowDefinition> findActiveByWorkflowTypeAndOrganization(
            @Param("workflowType") WorkflowType workflowType,
            @Param("orgId") Long orgId
    );
}
