package com.example.ems.incentive.repository;

import com.example.ems.incentive.entity.IncentivePayrollStatus;
import com.example.ems.incentive.entity.IncentiveRecord;
import com.example.ems.incentive.entity.IncentiveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncentiveRecordRepository extends JpaRepository<IncentiveRecord, Long> {

    Optional<IncentiveRecord> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<IncentiveRecord> findByWorkflowInstanceIdAndOrganizationId(String workflowInstanceId, Long organizationId);

    Optional<IncentiveRecord> findByWorkflowInstanceId(String workflowInstanceId);

    boolean existsByOrganizationIdAndEmployeeIdAndPolicyIdAndPeriodStartAndPeriodEnd(
            Long orgId, Long employeeId, Long policyId, LocalDate periodStart, LocalDate periodEnd);

    Optional<IncentiveRecord> findByOrganizationIdAndEmployeeIdAndPolicyIdAndPeriodStartAndPeriodEnd(
            Long orgId, Long employeeId, Long policyId, LocalDate periodStart, LocalDate periodEnd);

    Page<IncentiveRecord> findByOrganizationIdAndEmployeeId(Long orgId, Long employeeId, Pageable pageable);

    @Query("SELECT r FROM IncentiveRecord r WHERE r.organization.id = :orgId " +
           "AND (:employeeId IS NULL OR r.employee.id = :employeeId) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:payrollStatus IS NULL OR r.payrollStatus = :payrollStatus) " +
           "AND (:periodStart IS NULL OR r.periodEnd >= :periodStart) " +
           "AND (:periodEnd IS NULL OR r.periodStart <= :periodEnd)")
    Page<IncentiveRecord> findFiltered(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("status") IncentiveStatus status,
            @Param("payrollStatus") IncentivePayrollStatus payrollStatus,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd,
            Pageable pageable);

    @Query("SELECT r FROM IncentiveRecord r WHERE r.organization.id = :orgId " +
           "AND (:employeeId IS NULL OR r.employee.id = :employeeId) " +
           "AND r.status = com.example.ems.incentive.entity.IncentiveStatus.APPROVED " +
           "AND r.payrollStatus = com.example.ems.incentive.entity.IncentivePayrollStatus.PENDING " +
           "AND r.periodStart <= :periodEnd " +
           "AND r.periodEnd >= :periodStart")
    List<IncentiveRecord> findEligibleForPayroll(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);
}
