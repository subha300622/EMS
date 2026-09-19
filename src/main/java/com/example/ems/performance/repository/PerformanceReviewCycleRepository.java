package com.example.ems.performance.repository;

import com.example.ems.performance.entity.PerformanceReviewCycle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceReviewCycleRepository extends JpaRepository<PerformanceReviewCycle, Long> {

    Optional<PerformanceReviewCycle> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<PerformanceReviewCycle> findByOrganizationIdAndCode(Long organizationId, String code);

    List<PerformanceReviewCycle> findByOrganizationId(Long organizationId);

    Page<PerformanceReviewCycle> findByOrganizationId(Long organizationId, Pageable pageable);

    List<PerformanceReviewCycle> findByOrganizationIdAndStatus(Long organizationId, String status);

    boolean existsByOrganizationIdAndCode(Long organizationId, String code);
}
