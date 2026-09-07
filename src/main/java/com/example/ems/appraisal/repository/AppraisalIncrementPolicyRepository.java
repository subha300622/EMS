package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalIncrementPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalIncrementPolicyRepository extends JpaRepository<AppraisalIncrementPolicy, Long> {
    List<AppraisalIncrementPolicy> findByOrganizationId(Long organizationId);
    Optional<AppraisalIncrementPolicy> findByOrganizationIdAndActiveTrue(Long organizationId);
    Optional<AppraisalIncrementPolicy> findByIdAndOrganizationId(Long id, Long organizationId);
}
