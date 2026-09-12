package com.example.ems.attendance.repository;

import com.example.ems.attendance.entity.AttendanceAdjustment;
import com.example.ems.attendance.entity.AttendanceAdjustmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AttendanceAdjustmentRepository extends JpaRepository<AttendanceAdjustment, Long> {

    Optional<AttendanceAdjustment> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<AttendanceAdjustment> findByIdAndEmployeeIdAndOrganizationId(Long id, Long employeeId, Long organizationId);

    boolean existsByAttendanceIdAndStatus(Long attendanceId, AttendanceAdjustmentStatus status);

    @Query("SELECT a FROM AttendanceAdjustment a WHERE a.organization.id = :orgId " +
           "AND (:employeeId IS NULL OR a.employee.id = :employeeId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:fromDate IS NULL OR a.attendance.date >= :fromDate) " +
           "AND (:toDate IS NULL OR a.attendance.date <= :toDate)")
    Page<AttendanceAdjustment> findAdjustments(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("status") AttendanceAdjustmentStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );
}
