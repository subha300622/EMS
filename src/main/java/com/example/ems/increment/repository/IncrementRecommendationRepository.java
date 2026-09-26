package com.example.ems.increment.repository;

import com.example.ems.increment.entity.IncrementRecommendation;
import com.example.ems.increment.entity.IncrementRecommendationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncrementRecommendationRepository extends JpaRepository<IncrementRecommendation, Long> {

    @Query("SELECT r FROM IncrementRecommendation r WHERE r.id = :id AND r.organization.id = :orgId")
    Optional<IncrementRecommendation> findByIdAndOrgId(@Param("id") Long id, @Param("orgId") Long orgId);

    @Query("SELECT r FROM IncrementRecommendation r WHERE r.cycle.id = :cycleId AND r.employee.id = :employeeId AND (:appraisalId IS NULL OR r.appraisalId = :appraisalId) AND r.status NOT IN ('REJECTED')")
    Optional<IncrementRecommendation> findActiveRecommendation(
            @Param("cycleId") Long cycleId,
            @Param("employeeId") Long employeeId,
            @Param("appraisalId") Long appraisalId
    );

    @Query("SELECT r FROM IncrementRecommendation r WHERE r.employee.id = :employeeId AND r.organization.id = :orgId ORDER BY r.effectiveDate DESC")
    List<IncrementRecommendation> findHistoryByEmployeeIdAndOrgId(@Param("employeeId") Long employeeId, @Param("orgId") Long orgId);

    @Query("SELECT r FROM IncrementRecommendation r WHERE r.employee.id = :employeeId AND r.status = 'IMPLEMENTED' AND r.organization.id = :orgId ORDER BY r.effectiveDate DESC")
    List<IncrementRecommendation> findImplementedHistoryByEmployeeIdAndOrgId(@Param("employeeId") Long employeeId, @Param("orgId") Long orgId);

    @Query("SELECT COALESCE(SUM(r.incrementAmount), 0) FROM IncrementRecommendation r WHERE r.cycle.id = :cycleId AND r.status NOT IN ('REJECTED')")
    BigDecimal sumAllocatedAmountByCycleId(@Param("cycleId") Long cycleId);

    @Query("SELECT r FROM IncrementRecommendation r WHERE r.organization.id = :orgId " +
           "AND (:cycleId IS NULL OR r.cycle.id = :cycleId) " +
           "AND (:employeeId IS NULL OR r.employee.id = :employeeId) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:fromDate IS NULL OR r.effectiveDate >= :fromDate) " +
           "AND (:toDate IS NULL OR r.effectiveDate <= :toDate) " +
           "ORDER BY r.id DESC")
    List<IncrementRecommendation> searchRecommendations(
            @Param("orgId") Long orgId,
            @Param("cycleId") Long cycleId,
            @Param("employeeId") Long employeeId,
            @Param("status") IncrementRecommendationStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
