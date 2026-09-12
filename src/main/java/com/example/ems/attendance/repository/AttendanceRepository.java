package com.example.ems.attendance.repository;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {
    List<Attendance> findByEmployeeId(Long employeeId);
    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);

    // ── Tenant & Employee Scoped Isolation Queries ─────────────────────────
    Optional<Attendance> findByEmployeeIdAndDateAndOrganizationId(Long employeeId, LocalDate date, Long organizationId);
    boolean existsByEmployeeIdAndDateAndOrganizationId(Long employeeId, LocalDate date, Long organizationId);
    Optional<Attendance> findByIdAndEmployeeIdAndOrganizationId(Long id, Long employeeId, Long organizationId);
    Optional<Attendance> findByIdAndOrganizationId(Long id, Long organizationId);

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.employee.id = :employeeId
          AND a.organization.id = :organizationId
          AND (:fromDate IS NULL OR a.date >= :fromDate)
          AND (:toDate IS NULL OR a.date <= :toDate)
          AND (:status IS NULL OR a.status = :status)
    """)
    Page<Attendance> findHistory(
        @Param("employeeId") Long employeeId,
        @Param("organizationId") Long organizationId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("status") AttendanceStatus status,
        Pageable pageable
    );

    List<Attendance> findByEmployeeIdAndDateBetweenAndOrganizationId(Long employeeId, LocalDate startDate, LocalDate endDate, Long organizationId);

    List<Attendance> findByEmployeeIdInAndDateAndOrganizationId(List<Long> employeeIds, LocalDate date, Long organizationId);

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.employee.id IN :employeeIds
          AND a.organization.id = :organizationId
          AND (:fromDate IS NULL OR a.date >= :fromDate)
          AND (:toDate IS NULL OR a.date <= :toDate)
          AND (:status IS NULL OR a.status = :status)
    """)
    Page<Attendance> findHistoryForEmployees(
        @Param("employeeIds") List<Long> employeeIds,
        @Param("organizationId") Long organizationId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("status") AttendanceStatus status,
        Pageable pageable
    );

    List<Attendance> findByDate(LocalDate date);

    List<Attendance> findByEmployeeIdInAndDateBetween(List<Long> employeeIds, LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT a.date,
               SUM(CASE WHEN UPPER(a.status) = 'PRESENT' THEN 1 ELSE 0 END),
               SUM(CASE WHEN UPPER(a.status) = 'ABSENT' THEN 1 ELSE 0 END),
               SUM(CASE WHEN UPPER(a.status) = 'LATE' THEN 1 ELSE 0 END),
               SUM(CASE WHEN UPPER(a.status) = 'LEAVE' OR UPPER(a.status) = 'ON LEAVE' OR UPPER(a.status) = 'ON_LEAVE' THEN 1 ELSE 0 END)
        FROM Attendance a
        WHERE a.employee.id IN :employeeIds
          AND a.date BETWEEN :startDate AND :endDate
        GROUP BY a.date
        ORDER BY a.date ASC
    """)
    List<Object[]> getTrendStats(
        @Param("employeeIds") List<Long> employeeIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.organization.id = :organizationId
          AND a.isLate = true
          AND (:employeeIds IS NULL OR a.employee.id IN :employeeIds)
          AND (:employeeId IS NULL OR a.employee.id = :employeeId)
          AND (:date IS NULL OR a.date = :date)
          AND (:fromDate IS NULL OR a.date >= :fromDate)
          AND (:toDate IS NULL OR a.date <= :toDate)
    """)
    Page<Attendance> findLateAttendance(
        @Param("organizationId") Long organizationId,
        @Param("employeeIds") List<Long> employeeIds,
        @Param("employeeId") Long employeeId,
        @Param("date") LocalDate date,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.organization.id = :organizationId
          AND a.isEarlyCheckout = true
          AND (:employeeIds IS NULL OR a.employee.id IN :employeeIds)
          AND (:employeeId IS NULL OR a.employee.id = :employeeId)
          AND (:date IS NULL OR a.date = :date)
          AND (:fromDate IS NULL OR a.date >= :fromDate)
          AND (:toDate IS NULL OR a.date <= :toDate)
    """)
    Page<Attendance> findEarlyCheckoutAttendance(
        @Param("organizationId") Long organizationId,
        @Param("employeeIds") List<Long> employeeIds,
        @Param("employeeId") Long employeeId,
        @Param("date") LocalDate date,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );
}
