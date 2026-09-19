package com.example.ems.performance.repository;

import com.example.ems.performance.entity.PerformanceReviewKpiScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceReviewKpiScoreRepository extends JpaRepository<PerformanceReviewKpiScore, Long> {

    Optional<PerformanceReviewKpiScore> findByIdAndOrganizationId(Long id, Long organizationId);

    List<PerformanceReviewKpiScore> findByOrganizationIdAndReviewId(Long organizationId, Long reviewId);

    Optional<PerformanceReviewKpiScore> findByOrganizationIdAndReviewIdAndKpiId(Long organizationId, Long reviewId, Long kpiId);

    void deleteByOrganizationIdAndReviewId(Long organizationId, Long reviewId);
}
