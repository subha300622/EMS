package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.DepartmentCalendarSummaryDto;
import com.example.ems.attendance.dto.EmployeeCalendarDto;
import com.example.ems.attendance.dto.TeamCalendarGridDto;
import com.example.ems.attendance.dto.TeamHeatmapDto;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeamAttendanceCalendarService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    public TeamCalendarGridDto getTeamMonthlyCalendar(
            List<Employee> employees, LocalDate startOfMonth, LocalDate endOfMonth, Long departmentId, Long managerId, String view) {

        String monthStr = startOfMonth.toString().substring(0, 7);
        List<TeamCalendarGridDto.CalendarDayDto> calendarDays = new ArrayList<>();

        List<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());
        List<Attendance> records = employeeIds.isEmpty() ? Collections.emptyList() :
                attendanceRepository.findByEmployeeIdInAndDateBetween(employeeIds, startOfMonth, endOfMonth);

        Map<LocalDate, Map<Long, Attendance>> attendanceByDateAndEmployee = new HashMap<>();
        for (Attendance r : records) {
            attendanceByDateAndEmployee
                    .computeIfAbsent(r.getDate(), k -> new HashMap<>())
                    .put(r.getEmployee().getId(), r);
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            Map<Long, Attendance> dayRecords = attendanceByDateAndEmployee.getOrDefault(date, Collections.emptyMap());

            int present = 0;
            int absent = 0;
            int onLeave = 0;
            int holiday = 0;

            List<TeamCalendarGridDto.EmployeeDayDto> employeeDays = new ArrayList<>();

            for (Employee emp : employees) {
                Attendance r = dayRecords.get(emp.getId());
                String status = "ABSENT";
                String checkIn = null;
                String checkOut = null;

                if (r != null) {
                    status = r.getStatus() != null ? r.getStatus().toUpperCase() : "PRESENT";
                    if (status.contains("PRESENT") || status.contains("LATE")) {
                        present++;
                    } else if (status.contains("LEAVE")) {
                        onLeave++;
                    } else if (status.contains("ABSENT")) {
                        absent++;
                    } else {
                        present++;
                    }
                    checkIn = r.getPunchInTime() != null ? r.getPunchInTime().format(timeFormatter) : null;
                    checkOut = r.getPunchOutTime() != null ? r.getPunchOutTime().format(timeFormatter) : null;
                } else {
                    absent++;
                }

                if (!"summary".equalsIgnoreCase(view)) {
                    employeeDays.add(new TeamCalendarGridDto.EmployeeDayDto(
                            emp.getId(),
                            emp.getFullName(),
                            status,
                            checkIn,
                            checkOut
                    ));
                }
            }

            TeamCalendarGridDto.DaySummaryDto summaryDto = new TeamCalendarGridDto.DaySummaryDto(present, absent, onLeave, holiday);
            calendarDays.add(new TeamCalendarGridDto.CalendarDayDto(
                    date.toString(),
                    date.getDayOfWeek().name(),
                    summaryDto,
                    "summary".equalsIgnoreCase(view) ? null : employeeDays
            ));
        }

        return new TeamCalendarGridDto(monthStr, departmentId, managerId, calendarDays);
    }

    public TeamHeatmapDto getTeamHeatmap(List<Employee> employees, LocalDate startOfMonth, LocalDate endOfMonth) {
        String monthStr = startOfMonth.toString().substring(0, 7);
        List<TeamHeatmapDto.HeatmapDayDto> heatmapDays = new ArrayList<>();

        if (employees.isEmpty()) {
            for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
                heatmapDays.add(new TeamHeatmapDto.HeatmapDayDto(date.toString(), 0, "RED"));
            }
            return new TeamHeatmapDto(monthStr, heatmapDays);
        }

        List<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());
        List<Attendance> records = attendanceRepository.findByEmployeeIdInAndDateBetween(employeeIds, startOfMonth, endOfMonth);

        Map<LocalDate, Map<Long, Attendance>> attendanceByDateAndEmployee = new HashMap<>();
        for (Attendance r : records) {
            attendanceByDateAndEmployee
                    .computeIfAbsent(r.getDate(), k -> new HashMap<>())
                    .put(r.getEmployee().getId(), r);
        }

        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            Map<Long, Attendance> dayRecords = attendanceByDateAndEmployee.getOrDefault(date, Collections.emptyMap());

            int present = 0;
            for (Employee emp : employees) {
                Attendance r = dayRecords.get(emp.getId());
                if (r != null) {
                    String status = r.getStatus() != null ? r.getStatus().toUpperCase() : "PRESENT";
                    if (status.contains("PRESENT") || status.contains("LATE")) {
                        present++;
                    }
                }
            }

            int presentPercent = (present * 100) / employees.size();
            String color = "RED";
            if (presentPercent >= 80) {
                color = "GREEN";
            } else if (presentPercent >= 50) {
                color = "YELLOW";
            }

            heatmapDays.add(new TeamHeatmapDto.HeatmapDayDto(date.toString(), presentPercent, color));
        }

        return new TeamHeatmapDto(monthStr, heatmapDays);
    }

    public EmployeeCalendarDto getEmployeeMonthlyCalendar(Employee employee, LocalDate startOfMonth, LocalDate endOfMonth) {
        String monthStr = startOfMonth.toString().substring(0, 7);
        List<EmployeeCalendarDto.EmployeeDayRecordDto> calendarDays = new ArrayList<>();

        List<Attendance> records = attendanceRepository.findByEmployeeIdInAndDateBetween(
                List.of(employee.getId()), startOfMonth, endOfMonth);

        Map<LocalDate, Attendance> recordMap = records.stream()
                .collect(Collectors.toMap(Attendance::getDate, r -> r, (r1, r2) -> r1));

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            Attendance r = recordMap.get(date);
            String status = "ABSENT";
            String checkIn = null;
            String checkOut = null;

            if (r != null) {
                status = r.getStatus() != null ? r.getStatus().toUpperCase() : "PRESENT";
                checkIn = r.getPunchInTime() != null ? r.getPunchInTime().format(timeFormatter) : null;
                checkOut = r.getPunchOutTime() != null ? r.getPunchOutTime().format(timeFormatter) : null;
            }

            calendarDays.add(new EmployeeCalendarDto.EmployeeDayRecordDto(
                    date.toString(),
                    status,
                    checkIn,
                    checkOut
            ));
        }

        return new EmployeeCalendarDto(employee.getId(), monthStr, calendarDays);
    }

    public DepartmentCalendarSummaryDto getDepartmentCalendarSummary(
            List<Employee> employees, LocalDate startOfMonth, LocalDate endOfMonth, Long departmentId, Long managerId) {

        String monthStr = startOfMonth.toString().substring(0, 7);

        int workingDays = 0;
        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            int dayVal = date.getDayOfWeek().getValue();
            if (dayVal >= 1 && dayVal <= 5) {
                workingDays++;
            }
        }

        if (employees.isEmpty() || workingDays == 0) {
            return new DepartmentCalendarSummaryDto(
                    departmentId,
                    managerId,
                    monthStr,
                    new DepartmentCalendarSummaryDto.TotalsDto(workingDays, 0.0, employees.size()),
                    new DepartmentCalendarSummaryDto.TrendDto(null, null)
            );
        }

        List<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());
        List<Attendance> records = attendanceRepository.findByEmployeeIdInAndDateBetween(employeeIds, startOfMonth, endOfMonth);

        Map<LocalDate, Map<Long, Attendance>> attendanceByDateAndEmployee = new HashMap<>();
        for (Attendance r : records) {
            attendanceByDateAndEmployee
                    .computeIfAbsent(r.getDate(), k -> new HashMap<>())
                    .put(r.getEmployee().getId(), r);
        }

        double totalPct = 0.0;
        int weekdayCount = 0;
        LocalDate bestDay = null;
        int maxPresent = -1;
        LocalDate worstDay = null;
        int minPresent = Integer.MAX_VALUE;

        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            int dayVal = date.getDayOfWeek().getValue();
            if (dayVal >= 1 && dayVal <= 5) {
                weekdayCount++;
                Map<Long, Attendance> dayRecords = attendanceByDateAndEmployee.getOrDefault(date, Collections.emptyMap());

                int present = 0;
                for (Employee emp : employees) {
                    Attendance r = dayRecords.get(emp.getId());
                    if (r != null) {
                        String status = r.getStatus() != null ? r.getStatus().toUpperCase() : "PRESENT";
                        if (status.contains("PRESENT") || status.contains("LATE")) {
                            present++;
                        }
                    }
                }

                double presencePct = (present * 100.0) / employees.size();
                totalPct += presencePct;

                if (present > maxPresent) {
                    maxPresent = present;
                    bestDay = date;
                }
                if (present < minPresent) {
                    minPresent = present;
                    worstDay = date;
                }
            }
        }

        double avgAttendance = weekdayCount == 0 ? 0.0 : totalPct / weekdayCount;
        avgAttendance = Math.round(avgAttendance * 10.0) / 10.0;

        return new DepartmentCalendarSummaryDto(
                departmentId,
                managerId,
                monthStr,
                new DepartmentCalendarSummaryDto.TotalsDto(workingDays, avgAttendance, employees.size()),
                new DepartmentCalendarSummaryDto.TrendDto(
                        bestDay != null ? bestDay.toString() : null,
                        worstDay != null ? worstDay.toString() : null
                )
        );
    }
}
