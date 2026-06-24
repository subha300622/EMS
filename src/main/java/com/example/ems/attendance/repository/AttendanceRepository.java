package com.example.ems.attendance.repository;

import com.example.ems.attendance.entity.Attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployeeId(Long employeeId);
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<Attendance> findByDate(LocalDate date);

    List<Attendance> findByEmployeeIdInAndDateBetween(List<Long> employeeIds, LocalDate startDate, LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("""
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
        @org.springframework.data.repository.query.Param("employeeIds") List<Long> employeeIds,
        @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
        @org.springframework.data.repository.query.Param("endDate") LocalDate endDate
    );
}
