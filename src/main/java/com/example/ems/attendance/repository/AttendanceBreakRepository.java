package com.example.ems.attendance.repository;

import com.example.ems.attendance.entity.AttendanceBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceBreakRepository extends JpaRepository<AttendanceBreak, Long> {

    Optional<AttendanceBreak> findByAttendanceIdAndBreakEndTimeIsNull(Long attendanceId);

    List<AttendanceBreak> findByAttendanceIdOrderByBreakStartTimeAsc(Long attendanceId);

    boolean existsByAttendanceIdAndBreakEndTimeIsNull(Long attendanceId);
}
