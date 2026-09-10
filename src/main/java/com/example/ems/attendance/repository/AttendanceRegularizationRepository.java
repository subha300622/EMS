package com.example.ems.attendance.repository;

import com.example.ems.attendance.entity.AttendanceRegularization;
import com.example.ems.attendance.entity.AttendanceRegularizationStatus;
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
public interface AttendanceRegularizationRepository extends JpaRepository<AttendanceRegularization, Long> {

    // ── Double-Scoped Tenant and Employee Queries ─────────────────────────
    Page<AttendanceRegularization> findByEmployeeIdAndOrganizationId(Long employeeId, Long organizationId, Pageable pageable);

    Page<AttendanceRegularization> findByEmployeeIdAndOrganizationIdAndStatus(Long employeeId, Long organizationId, AttendanceRegularizationStatus status, Pageable pageable);

    Optional<AttendanceRegularization> findByIdAndEmployeeIdAndOrganizationId(Long id, Long employeeId, Long organizationId);

    Optional<AttendanceRegularization> findByIdAndOrganizationId(Long id, Long organizationId);

    boolean existsByAttendanceIdAndStatus(Long attendanceId, AttendanceRegularizationStatus status);

    Optional<AttendanceRegularization> findByWorkflowInstanceId(String workflowInstanceId);

    @Query("""
        SELECT r FROM AttendanceRegularization r
        WHERE r.employee.id = :employeeId
          AND r.organization.id = :organizationId
          AND (:status IS NULL OR r.status = :status)
          AND (:fromDate IS NULL OR r.date >= :fromDate)
          AND (:toDate IS NULL OR r.date <= :toDate)
    """)
    Page<AttendanceRegularization> findMyRegularizations(
        @Param("employeeId") Long employeeId,
        @Param("organizationId") Long organizationId,
        @Param("status") AttendanceRegularizationStatus status,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    // ── Backward Compatibility Queries ────────────────────────────────────
    List<AttendanceRegularization> findByEmployeeId(Long employeeId);
    List<AttendanceRegularization> findByEmployeeIdAndStatus(Long employeeId, String status);
    List<AttendanceRegularization> findByStatus(String status);
}
