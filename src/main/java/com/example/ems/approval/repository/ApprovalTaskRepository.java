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

    @Query("SELECT t FROM ApprovalTask t LEFT JOIN FETCH t.approver LEFT JOIN FETCH t.workflowInstance LEFT JOIN FETCH t.step WHERE t.approvalTaskId = :approvalTaskId")
    Optional<ApprovalTask> findByApprovalTaskId(@Param("approvalTaskId") String approvalTaskId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM ApprovalTask t LEFT JOIN FETCH t.approver LEFT JOIN FETCH t.workflowInstance LEFT JOIN FETCH t.step WHERE t.approvalTaskId = :approvalTaskId")
    Optional<ApprovalTask> findByApprovalTaskIdWithLock(@Param("approvalTaskId") String approvalTaskId);

    List<ApprovalTask> findByWorkflowInstanceIdAndStepOrder(Long workflowInstanceId, Integer stepOrder);

    @Query(value = "SELECT t FROM ApprovalTask t LEFT JOIN FETCH t.approver LEFT JOIN FETCH t.workflowInstance LEFT JOIN FETCH t.step WHERE t.approver.id = :approverId " +
           "AND (:orgId IS NULL OR t.workflowInstance.organization.id = :orgId) " +
           "AND (:workflowType IS NULL OR t.workflowType = :workflowType) " +
           "AND (:status IS NULL OR t.status = :status)",
           countQuery = "SELECT count(t) FROM ApprovalTask t WHERE t.approver.id = :approverId " +
           "AND (:orgId IS NULL OR t.workflowInstance.organization.id = :orgId) " +
           "AND (:workflowType IS NULL OR t.workflowType = :workflowType) " +
           "AND (:status IS NULL OR t.status = :status)")
    Page<ApprovalTask> findInboxTasks(
            @Param("approverId") Long approverId,
            @Param("orgId") Long orgId,
            @Param("workflowType") WorkflowType workflowType,
            @Param("status") ApprovalStatus status,
            Pageable pageable
    );

    @Query("SELECT t FROM ApprovalTask t WHERE t.workflowType = :workflowType " +
           "AND t.businessReferenceType = :businessReferenceType " +
           "AND t.businessReferenceId = :businessReferenceId " +
           "AND t.status = com.example.ems.approval.entity.ApprovalStatus.PENDING")
    List<ApprovalTask> findActiveTasksForBusinessRef(
            @Param("workflowType") WorkflowType workflowType,
            @Param("businessReferenceType") String businessReferenceType,
            @Param("businessReferenceId") String businessReferenceId
    );
}
