package com.example.ems.attendance.repository;

import com.example.ems.attendance.entity.AttendancePermission;
import com.example.ems.attendance.entity.AttendancePermissionStatus;
import com.example.ems.attendance.entity.AttendancePermissionType;
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
public interface AttendancePermissionRepository extends JpaRepository<AttendancePermission, Long> {

    Optional<AttendancePermission> findByIdAndOrganizationId(Long id, Long organizationId);

    Page<AttendancePermission> findByOrganizationIdAndEmployeeIdOrderByAttendanceDateDesc(
            Long organizationId, Long employeeId, Pageable pageable
    );

    Page<AttendancePermission> findByOrganizationIdAndStatusOrderByCreatedAtDesc(
            Long organizationId, AttendancePermissionStatus status, Pageable pageable
    );

    @Query("SELECT p FROM AttendancePermission p " +
           "WHERE p.organization.id = :orgId " +
           "AND p.employee.id = :employeeId " +
           "AND p.attendanceDate = :date " +
           "AND p.permissionType = :type " +
           "AND p.status IN ('APPROVED', 'APPLIED')")
    List<AttendancePermission> findApprovedPermissions(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date,
            @Param("type") AttendancePermissionType type
    );

    @Query("SELECT p FROM AttendancePermission p " +
           "WHERE p.organization.id = :orgId " +
           "AND p.employee.id = :employeeId " +
           "AND p.attendanceDate = :date " +
           "AND p.status IN ('APPROVED', 'APPLIED')")
    List<AttendancePermission> findAllApprovedPermissionsForDate(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date
    );

    @Query("SELECT COUNT(p) FROM AttendancePermission p " +
           "WHERE p.organization.id = :orgId " +
           "AND p.employee.id = :employeeId " +
           "AND p.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND p.status NOT IN ('REJECTED', 'CANCELLED')")
    long countPermissionsInPeriod(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(p.requestedMinutes), 0) FROM AttendancePermission p " +
           "WHERE p.organization.id = :orgId " +
           "AND p.employee.id = :employeeId " +
           "AND p.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND p.status NOT IN ('REJECTED', 'CANCELLED')")
    int sumMinutesInPeriod(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(p.requestedMinutes), 0) FROM AttendancePermission p " +
           "WHERE p.organization.id = :orgId " +
           "AND p.employee.id = :employeeId " +
           "AND p.attendanceDate = :date " +
           "AND p.status NOT IN ('REJECTED', 'CANCELLED')")
    int sumMinutesForDate(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date
    );

    @Query("SELECT p FROM AttendancePermission p " +
           "WHERE p.organization.id = :orgId " +
           "AND (:employeeId IS NULL OR p.employee.id = :employeeId) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:fromDate IS NULL OR p.attendanceDate >= :fromDate) " +
           "AND (:toDate IS NULL OR p.attendanceDate <= :toDate)")
    Page<AttendancePermission> findPermissionsFiltered(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("status") AttendancePermissionStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );
}
