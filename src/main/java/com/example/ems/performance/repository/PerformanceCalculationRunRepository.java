package com.example.ems.performance.repository;

import com.example.ems.performance.entity.PerformanceCalculationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceCalculationRunRepository extends JpaRepository<PerformanceCalculationRun, Long> {

    Optional<PerformanceCalculationRun> findByIdAndOrganizationId(Long id, Long organizationId);

    List<PerformanceCalculationRun> findByOrganizationIdAndReviewIdOrderByCalculationVersionDesc(Long organizationId, Long reviewId);

    Optional<PerformanceCalculationRun> findByOrganizationIdAndReviewIdAndCalculationVersion(Long organizationId, Long reviewId, Integer calculationVersion);
}
