package com.example.ems.performance.repository;

import com.example.ems.performance.entity.PerformanceReviewRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceReviewRecordRepository extends JpaRepository<PerformanceReviewRecord, Long> {

    Optional<PerformanceReviewRecord> findByIdAndOrganizationId(Long id, Long organizationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM PerformanceReviewRecord r WHERE r.id = :id AND r.organization.id = :orgId")
    Optional<PerformanceReviewRecord> findWithLockByIdAndOrganizationId(@Param("id") Long id, @Param("orgId") Long orgId);

    Optional<PerformanceReviewRecord> findByOrganizationIdAndCycleIdAndEmployeeId(Long organizationId, Long cycleId, Long employeeId);

    Page<PerformanceReviewRecord> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<PerformanceReviewRecord> findByOrganizationIdAndCycleId(Long organizationId, Long cycleId, Pageable pageable);

    List<PerformanceReviewRecord> findByOrganizationIdAndCycleId(Long organizationId, Long cycleId);

    Page<PerformanceReviewRecord> findByOrganizationIdAndStatus(Long organizationId, String status, Pageable pageable);

    List<PerformanceReviewRecord> findByOrganizationIdAndEmployeeId(Long organizationId, Long employeeId);

    List<PerformanceReviewRecord> findByOrganizationIdAndReviewerId(Long organizationId, Long reviewerId);

    long countByOrganizationIdAndCycleId(Long organizationId, Long cycleId);

    long countByOrganizationIdAndCycleIdAndStatus(Long organizationId, Long cycleId, String status);
}
