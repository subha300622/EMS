package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalPerformanceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalPerformanceCategoryRepository extends JpaRepository<AppraisalPerformanceCategory, Long> {
    List<AppraisalPerformanceCategory> findByOrganizationIdOrderByMinRatingDesc(Long organizationId);
    List<AppraisalPerformanceCategory> findByOrganizationIdAndActiveTrueOrderByMinRatingDesc(Long organizationId);
    Optional<AppraisalPerformanceCategory> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<AppraisalPerformanceCategory> findByOrganizationIdAndName(Long organizationId, String name);
}
