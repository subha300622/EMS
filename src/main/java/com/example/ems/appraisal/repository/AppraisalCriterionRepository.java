package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalCriterionRepository extends JpaRepository<AppraisalCriterion, Long> {
    List<AppraisalCriterion> findByOrganizationId(Long organizationId);
    List<AppraisalCriterion> findByOrganizationIdAndActiveTrue(Long organizationId);
    Optional<AppraisalCriterion> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<AppraisalCriterion> findByOrganizationIdAndName(Long organizationId, String name);
}
