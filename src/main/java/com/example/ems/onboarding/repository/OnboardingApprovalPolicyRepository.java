package com.example.ems.onboarding.repository;

import com.example.ems.onboarding.entity.OnboardingApprovalPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingApprovalPolicyRepository extends JpaRepository<OnboardingApprovalPolicy, Long> {

    List<OnboardingApprovalPolicy> findByOrganizationIdAndActiveTrue(Long organizationId);

    List<OnboardingApprovalPolicy> findByOrganizationIdIsNullAndActiveTrue();

    Optional<OnboardingApprovalPolicy> findByOrganizationIdAndCurrentStatusAndActionAndActiveTrue(Long organizationId, String currentStatus, String action);

    Optional<OnboardingApprovalPolicy> findByOrganizationIdIsNullAndCurrentStatusAndActionAndActiveTrue(String currentStatus, String action);

    Optional<OnboardingApprovalPolicy> findByPolicyId(String policyId);
}
