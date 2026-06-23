package com.example.ems.attendance.repository;

import com.example.ems.attendance.entity.AttendanceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {
    Page<AttendanceLog> findByEmployeeIdAndDate(Long employeeId, LocalDate date, Pageable pageable);
    List<AttendanceLog> findByEmployeeIdAndDateOrderByTimeAsc(Long employeeId, LocalDate date);
}
