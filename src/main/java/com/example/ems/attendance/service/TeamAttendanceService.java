package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.TeamMemberAttendanceDto;
import com.example.ems.attendance.dto.TeamSummaryDto;
import com.example.ems.attendance.dto.TeamTrendDto;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamAttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    public List<TeamMemberAttendanceDto> getTeamAttendance(List<Employee> employees, LocalDate startDate, LocalDate endDate) {
        if (employees.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());
        List<Attendance> records = attendanceRepository.findByEmployeeIdInAndDateBetween(employeeIds, startDate, endDate);

        // Group records: employeeId -> List of attendance records
        Map<Long, List<Attendance>> employeeRecordsMap = records.stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        List<TeamMemberAttendanceDto> result = new ArrayList<>();
        for (Employee emp : employees) {
            List<Attendance> empRecords = employeeRecordsMap.getOrDefault(emp.getId(), Collections.emptyList());

            List<TeamMemberAttendanceDto.AttendanceRecordDto> recordDtos = empRecords.stream()
                    .map(r -> new TeamMemberAttendanceDto.AttendanceRecordDto(
                            r.getDate().format(dateFormatter),
                            r.getStatus().toUpperCase(),
                            r.getPunchInTime() != null ? r.getPunchInTime().format(timeFormatter) : null,
                            r.getPunchOutTime() != null ? r.getPunchOutTime().format(timeFormatter) : null,
                            r.getWorkingHours()
                    ))
                    .collect(Collectors.toList());

            result.add(new TeamMemberAttendanceDto(
                    emp.getId(),
                    emp.getFullName(),
                    emp.getDesignation() != null ? emp.getDesignation() : "Employee",
                    emp.getWorkMode() != null ? emp.getWorkMode().toUpperCase() : "OFFICE",
                    recordDtos
            ));
        }

        return result;
    }

    public TeamSummaryDto getTeamAttendanceSummary(List<Employee> employees, LocalDate date) {
        if (employees.isEmpty()) {
            return new TeamSummaryDto(date.toString(), 0, 0, 0, 0, 0);
        }

        List<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());
        List<Attendance> records = attendanceRepository.findByEmployeeIdInAndDateBetween(employeeIds, date, date);

        Map<Long, Attendance> recordMap = records.stream()
                .collect(Collectors.toMap(a -> a.getEmployee().getId(), a -> a));

        int present = 0;
        int absent = 0;
        int late = 0;
        int onLeave = 0;

        for (Long empId : employeeIds) {
            Attendance att = recordMap.get(empId);
            if (att == null) {
                absent++;
            } else {
                String status = att.getStatus() != null ? att.getStatus().toUpperCase() : "";
                if (status.contains("PRESENT")) {
                    present++;
                } else if (status.contains("LATE")) {
                    late++;
                } else if (status.contains("LEAVE")) {
                    onLeave++;
                } else if (status.contains("ABSENT")) {
                    absent++;
                } else {
                    present++; // fallback
                }
            }
        }

        return new TeamSummaryDto(
                date.toString(),
                employees.size(),
                present,
                absent,
                late,
                onLeave
        );
    }

    public TeamTrendDto getTeamAttendanceTrend(List<Employee> employees, LocalDate startDate, LocalDate endDate) {
        if (employees.isEmpty()) {
            return new TeamTrendDto(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        List<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());
        List<Object[]> stats = attendanceRepository.getTrendStats(employeeIds, startDate, endDate);

        List<String> labels = new ArrayList<>();
        List<Long> presentCount = new ArrayList<>();
        List<Long> absentCount = new ArrayList<>();
        List<Long> lateCount = new ArrayList<>();
        List<Long> onLeaveCount = new ArrayList<>();

        for (Object[] row : stats) {
            LocalDate date = (LocalDate) row[0];
            Long pres = (Long) row[1];
            Long abs = (Long) row[2];
            Long lat = (Long) row[3];
            Long leave = (Long) row[4];

            labels.add(date.toString());
            presentCount.add(pres);
            absentCount.add(abs);
            lateCount.add(lat);
            onLeaveCount.add(leave);
        }

        return new TeamTrendDto(
                labels,
                presentCount,
                absentCount,
                lateCount,
                onLeaveCount
        );
    }
}
