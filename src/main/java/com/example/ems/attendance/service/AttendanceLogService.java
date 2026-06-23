package com.example.ems.attendance.service;

import com.example.ems.attendance.entity.AttendanceLog;
import com.example.ems.attendance.repository.AttendanceLogRepository;
import com.example.ems.employee.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceLogService {

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    @Transactional
    public AttendanceLog logSwipe(Employee employee, String type, String location) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<AttendanceLog> dailyLogs = attendanceLogRepository.findByEmployeeIdAndDateOrderByTimeAsc(employee.getId(), today);
        
        Optional<AttendanceLog> lastLogOpt = dailyLogs.stream()
                .filter(log -> log.getType().equalsIgnoreCase(type))
                .reduce((first, second) -> second);

        if (lastLogOpt.isPresent()) {
            Duration duration = Duration.between(lastLogOpt.get().getTime(), now);
            if (duration.getSeconds() >= 0 && duration.getSeconds() < 5) {
                throw new IllegalArgumentException("Duplicate swipe detected. Please wait 5 seconds.");
            }
        }

        AttendanceLog log = new AttendanceLog(employee, today, now, type.toUpperCase(), location);
        return attendanceLogRepository.save(log);
    }

    public Page<AttendanceLog> getDailyLogs(Long employeeId, LocalDate date, Pageable pageable) {
        return attendanceLogRepository.findByEmployeeIdAndDate(employeeId, date, pageable);
    }

    public List<AttendanceLog> getDailyLogsList(Long employeeId, LocalDate date) {
        return attendanceLogRepository.findByEmployeeIdAndDateOrderByTimeAsc(employeeId, date);
    }
}
