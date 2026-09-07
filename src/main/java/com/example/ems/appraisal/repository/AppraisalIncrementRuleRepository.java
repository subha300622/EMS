package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalIncrementRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalIncrementRuleRepository extends JpaRepository<AppraisalIncrementRule, Long> {
    List<AppraisalIncrementRule> findByPolicyIdOrderByMinRatingDesc(Long policyId);
    List<AppraisalIncrementRule> findByOrganizationIdAndActiveTrueOrderByMinRatingDesc(Long organizationId);
    Optional<AppraisalIncrementRule> findByIdAndOrganizationId(Long id, Long organizationId);
}
