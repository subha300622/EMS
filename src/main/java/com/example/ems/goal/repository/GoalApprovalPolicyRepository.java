package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalApprovalPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalApprovalPolicyRepository extends JpaRepository<GoalApprovalPolicy, Long> {

    List<GoalApprovalPolicy> findByOrganizationIdAndIsActiveTrue(Long organizationId);

    List<GoalApprovalPolicy> findByOrganizationIdAndActionAndIsActiveTrue(Long organizationId, String action);

    Optional<GoalApprovalPolicy> findByIdAndOrganizationId(Long id, Long organizationId);
}
