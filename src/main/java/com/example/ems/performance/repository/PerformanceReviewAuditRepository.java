package com.example.ems.performance.repository;

import com.example.ems.performance.entity.PerformanceReviewAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceReviewAuditRepository extends JpaRepository<PerformanceReviewAudit, Long> {

    List<PerformanceReviewAudit> findByOrganizationIdAndReviewIdOrderByCreatedAtDesc(Long organizationId, Long reviewId);

    Page<PerformanceReviewAudit> findByOrganizationIdAndReviewId(Long organizationId, Long reviewId, Pageable pageable);
}
