package com.example.ems.overtime.repository;

import com.example.ems.overtime.entity.OvertimePayrollStatus;
import com.example.ems.overtime.entity.OvertimeRecord;
import com.example.ems.overtime.entity.OvertimeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OvertimeRecordRepository extends JpaRepository<OvertimeRecord, Long>, JpaSpecificationExecutor<OvertimeRecord> {

    Optional<OvertimeRecord> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<OvertimeRecord> findByOrganizationIdAndEmployeeIdAndAttendanceId(Long organizationId, Long employeeId, Long attendanceId);

    boolean existsByOrganizationIdAndEmployeeIdAndAttendanceId(Long organizationId, Long employeeId, Long attendanceId);

    Optional<OvertimeRecord> findByWorkflowInstanceId(String workflowInstanceId);

    Page<OvertimeRecord> findByOrganizationIdAndEmployeeId(Long organizationId, Long employeeId, Pageable pageable);

    Page<OvertimeRecord> findByOrganizationIdAndStatus(Long organizationId, OvertimeStatus status, Pageable pageable);

    @Query("""
        SELECT r FROM OvertimeRecord r
        WHERE r.organization.id = :organizationId
          AND (:employeeId IS NULL OR r.employee.id = :employeeId)
          AND (:status IS NULL OR r.status = :status)
          AND (:payrollStatus IS NULL OR r.payrollStatus = :payrollStatus)
          AND (:fromDate IS NULL OR r.workDate >= :fromDate)
          AND (:toDate IS NULL OR r.workDate <= :toDate)
    """)
    Page<OvertimeRecord> findFiltered(
            @Param("organizationId") Long organizationId,
            @Param("employeeId") Long employeeId,
            @Param("status") OvertimeStatus status,
            @Param("payrollStatus") OvertimePayrollStatus payrollStatus,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    @Query("""
        SELECT r FROM OvertimeRecord r
        WHERE r.organization.id = :organizationId
          AND (:employeeId IS NULL OR r.employee.id = :employeeId)
          AND r.workDate BETWEEN :periodStart AND :periodEnd
          AND r.status = 'APPROVED'
          AND r.payrollStatus = 'PENDING'
        ORDER BY r.workDate ASC
    """)
    List<OvertimeRecord> findEligibleForPayroll(
            @Param("organizationId") Long organizationId,
            @Param("employeeId") Long employeeId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );
}
