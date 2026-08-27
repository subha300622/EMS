package com.example.ems.approval.repository;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.ApprovalTask;
import com.example.ems.approval.entity.WorkflowType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalTaskRepository extends JpaRepository<ApprovalTask, Long>, JpaSpecificationExecutor<ApprovalTask> {

    Optional<ApprovalTask> findByApprovalTaskId(String approvalTaskId);

    List<ApprovalTask> findByWorkflowInstanceIdAndStepOrder(Long workflowInstanceId, Integer stepOrder);

    @Query("SELECT t FROM ApprovalTask t WHERE t.approver.id = :approverId " +
           "AND t.workflowInstance.organization.id = :orgId " +
           "AND (:workflowType IS NULL OR t.workflowType = :workflowType) " +
           "AND (:status IS NULL OR t.status = :status)")
    Page<ApprovalTask> findInboxTasks(
            @Param("approverId") Long approverId,
            @Param("orgId") Long orgId,
            @Param("workflowType") WorkflowType workflowType,
            @Param("status") ApprovalStatus status,
            Pageable pageable
    );
}
