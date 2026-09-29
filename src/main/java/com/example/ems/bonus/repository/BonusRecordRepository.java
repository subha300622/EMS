package com.example.ems.bonus.repository;

import com.example.ems.bonus.entity.BonusPayrollStatus;
import com.example.ems.bonus.entity.BonusRecord;
import com.example.ems.bonus.entity.BonusStatus;
import com.example.ems.bonus.entity.BonusType;
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
public interface BonusRecordRepository extends JpaRepository<BonusRecord, Long> {

    Optional<BonusRecord> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<BonusRecord> findByWorkflowInstanceIdAndOrganizationId(String workflowInstanceId, Long organizationId);

    Optional<BonusRecord> findByWorkflowInstanceId(String workflowInstanceId);

    boolean existsByOrganizationIdAndEmployeeIdAndPolicyIdAndPeriodStartAndPeriodEnd(
            Long orgId, Long employeeId, Long policyId, LocalDate periodStart, LocalDate periodEnd);

    Optional<BonusRecord> findByOrganizationIdAndEmployeeIdAndPolicyIdAndPeriodStartAndPeriodEnd(
            Long orgId, Long employeeId, Long policyId, LocalDate periodStart, LocalDate periodEnd);

    Page<BonusRecord> findByOrganizationIdAndEmployeeId(Long orgId, Long employeeId, Pageable pageable);

    @Query("SELECT r FROM BonusRecord r WHERE r.organization.id = :orgId " +
           "AND (:employeeId IS NULL OR r.employee.id = :employeeId) " +
           "AND (:policyId IS NULL OR r.policy.id = :policyId) " +
           "AND (:bonusType IS NULL OR r.bonusType = :bonusType) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:payrollStatus IS NULL OR r.payrollStatus = :payrollStatus) " +
           "AND (:periodStart IS NULL OR r.periodEnd >= :periodStart) " +
           "AND (:periodEnd IS NULL OR r.periodStart <= :periodEnd)")
    Page<BonusRecord> findFiltered(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("policyId") Long policyId,
            @Param("bonusType") BonusType bonusType,
            @Param("status") BonusStatus status,
            @Param("payrollStatus") BonusPayrollStatus payrollStatus,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd,
            Pageable pageable);

    @Query("SELECT r FROM BonusRecord r WHERE r.organization.id = :orgId " +
           "AND (:employeeId IS NULL OR r.employee.id = :employeeId) " +
           "AND r.status = com.example.ems.bonus.entity.BonusStatus.APPROVED " +
           "AND r.payrollStatus = com.example.ems.bonus.entity.BonusPayrollStatus.PENDING " +
           "AND r.periodStart <= :periodEnd " +
           "AND r.periodEnd >= :periodStart")
    List<BonusRecord> findEligibleForPayroll(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);

    @Query("SELECT r FROM BonusRecord r WHERE r.organization.id = :orgId " +
           "AND (:employeeId IS NULL OR r.employee.id = :employeeId) " +
           "AND (:payrollStatus IS NULL OR r.payrollStatus = :payrollStatus) " +
           "AND r.periodStart <= :periodEnd " +
           "AND r.periodEnd >= :periodStart")
    List<BonusRecord> findEligibleForPayrollWithStatus(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("payrollStatus") BonusPayrollStatus payrollStatus,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);
}
