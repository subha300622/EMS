package com.example.ems.approval.repository;

import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalWorkflowInstanceRepository extends JpaRepository<ApprovalWorkflowInstance, Long> {

    Optional<ApprovalWorkflowInstance> findByWorkflowInstanceIdAndOrganizationId(String workflowInstanceId, Long organizationId);

    Optional<ApprovalWorkflowInstance> findByWorkflowInstanceId(String workflowInstanceId);

    Optional<ApprovalWorkflowInstance> findByBusinessReferenceTypeAndBusinessReferenceIdAndOrganizationId(
            String businessReferenceType, String businessReferenceId, Long organizationId
    );
}
