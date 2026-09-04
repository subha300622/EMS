package com.example.ems.approval.repository;

import com.example.ems.approval.entity.ApprovalAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalActionRepository extends JpaRepository<ApprovalAction, Long> {

    List<ApprovalAction> findByApprovalTaskIdOrderByCreatedAtAsc(Long approvalTaskId);
}
