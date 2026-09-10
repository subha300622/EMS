package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.AttendanceBreakDto;
import com.example.ems.attendance.dto.AttendanceCalendarDayDto;
import com.example.ems.attendance.dto.MonthlyAttendanceCalendarResponse;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceBreak;
import com.example.ems.attendance.repository.AttendanceBreakRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.employee.entity.Employee;
import com.example.ems.holiday.entity.Holiday;
import com.example.ems.holiday.entity.HolidayStatus;
import com.example.ems.holiday.repository.HolidayRepository;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceCalendarService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceBreakRepository attendanceBreakRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private Clock clock;

    @Transactional(readOnly = true)
    public MonthlyAttendanceCalendarResponse getMonthlyCalendar(int year, int month) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month + ". Month must be between 1 and 12.");
        }

        Employee employee = attendanceService.resolveCurrentEmployee();
        Long organizationId = employee.getOrganization() != null ? employee.getOrganization().getId() : TenantContext.requireOrganizationId();

        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
        LocalDate today = LocalDate.now(clock);

        // 1. Fetch holidays for tenant in month
        List<Holiday> holidays = holidayRepository.findByOrganizationIdAndStatusAndHolidayDateBetweenOrderByHolidayDateAsc(
                organizationId, HolidayStatus.ACTIVE, startOfMonth, endOfMonth);
        Map<LocalDate, Holiday> holidayMap = holidays.stream()
                .collect(Collectors.toMap(Holiday::getHolidayDate, h -> h, (h1, h2) -> h1));

        // 2. Fetch approved leaves for employee in month
        List<Leave> leaves = leaveRepository.findOverlappingLeaves(employee.getId(), startOfMonth, endOfMonth);
        List<Leave> approvedLeaves = leaves.stream()
                .filter(l -> "APPROVED".equalsIgnoreCase(l.getStatus()))
                .toList();

        // 3. Fetch attendance records for employee in month
        List<Attendance> attendanceList = attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(
                employee.getId(), startOfMonth, endOfMonth, organizationId);
        Map<LocalDate, Attendance> attendanceMap = attendanceList.stream()
                .collect(Collectors.toMap(Attendance::getDate, a -> a, (a1, a2) -> a1));

        List<AttendanceCalendarDayDto> dayDtos = new ArrayList<>();
        int presentCount = 0;
        int absentCount = 0;
        int halfDayCount = 0;
        int leaveCount = 0;
        int holidayCount = 0;
        int weekOffCount = 0;
        int notCheckedInCount = 0;
        int totalWorkingMinutesAccum = 0;
        int totalBreakMinutesAccum = 0;

        for (LocalDate d = startOfMonth; !d.isAfter(endOfMonth); d = d.plusDays(1)) {
            AttendanceCalendarDayDto dayDto = new AttendanceCalendarDayDto();
            dayDto.setDate(d);
            dayDto.setDayOfWeek(d.getDayOfWeek().name());

            // If an attendance record exists for this date, retain working minutes & check-in details
            Attendance att = attendanceMap.get(d);
            if (att != null) {
                dayDto.setCheckInTime(att.getCheckInTime());
                dayDto.setCheckOutTime(att.getCheckOutTime());
                int workMins = att.getTotalWorkingMinutes() != null ? att.getTotalWorkingMinutes() : 0;
                int breakMins = att.getTotalBreakMinutes() != null ? att.getTotalBreakMinutes() : 0;
                dayDto.setTotalWorkingMinutes(workMins);
                dayDto.setTotalBreakMinutes(breakMins);
                dayDto.setIsLate(att.getIsLate() != null ? att.getIsLate() : false);
                dayDto.setLateBy(att.getLateBy() != null ? att.getLateBy() : "00:00");

                totalWorkingMinutesAccum += workMins;
                totalBreakMinutesAccum += breakMins;
            }

            LocalDate currentDate = d;
            Optional<Leave> matchingLeave = approvedLeaves.stream()
                    .filter(l -> !currentDate.isBefore(l.getStartDate()) && !currentDate.isAfter(l.getEndDate()))
                    .findFirst();

            // Status Priority Rule:
            // 1. HOLIDAY -> 2. WEEK_OFF -> 3. ON_LEAVE -> 4. ATTENDANCE -> 5. NOT_CHECKED_IN / ABSENT
            if (holidayMap.containsKey(d)) {
                Holiday h = holidayMap.get(d);
                dayDto.setStatus("HOLIDAY");
                dayDto.setHolidayName(h.getName());
                holidayCount++;
            } else if (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
                dayDto.setStatus("WEEK_OFF");
                weekOffCount++;
            } else if (matchingLeave.isPresent()) {
                Leave l = matchingLeave.get();
                dayDto.setStatus("ON_LEAVE");
                dayDto.setLeaveType(l.getLeaveType() != null ? l.getLeaveType().getName() : "LEAVE");
                leaveCount++;
            } else if (att != null) {
                String attStatus = att.getStatus() != null ? att.getStatus() : (att.getAttendanceStatus() != null ? att.getAttendanceStatus().name() : "");
                if ("HALF_DAY".equalsIgnoreCase(attStatus)) {
                    dayDto.setStatus("HALF_DAY");
                    halfDayCount++;
                } else if ("ABSENT".equalsIgnoreCase(attStatus)) {
                    dayDto.setStatus("ABSENT");
                    absentCount++;
                } else {
                    dayDto.setStatus("PRESENT");
                    presentCount++;
                }
            } else {
                if (d.isBefore(today)) {
                    dayDto.setStatus("ABSENT");
                    absentCount++;
                } else {
                    dayDto.setStatus("NOT_CHECKED_IN");
                    notCheckedInCount++;
                }
            }

            dayDtos.add(dayDto);
        }

        MonthlyAttendanceCalendarResponse response = new MonthlyAttendanceCalendarResponse();
        response.setYear(year);
        response.setMonth(month);
        int totalDays = endOfMonth.getDayOfMonth();
        response.setTotalDays(totalDays);
        int workingDays = Math.max(0, totalDays - weekOffCount - holidayCount);
        response.setWorkingDays(workingDays);
        response.setPresentDays(presentCount);
        response.setAbsentDays(absentCount);
        response.setHalfDays(halfDayCount);
        response.setLeaveDays(leaveCount);
        response.setHolidayDays(holidayCount);
        response.setWeekOffDays(weekOffCount);
        response.setNotCheckedInDays(notCheckedInCount);
        response.setTotalWorkingMinutes(totalWorkingMinutesAccum);
        response.setTotalBreakMinutes(totalBreakMinutesAccum);

        int attendedDays = presentCount + halfDayCount;
        double avgWorkingMins = attendedDays > 0 ? (double) totalWorkingMinutesAccum / attendedDays : 0.0;
        response.setAverageWorkingMinutes(Math.round(avgWorkingMins * 10.0) / 10.0);
        response.setDays(dayDtos);

        return response;
    }

    @Transactional(readOnly = true)
    public AttendanceCalendarDayDto getDayDetail(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date parameter cannot be null");
        }

        Employee employee = attendanceService.resolveCurrentEmployee();
        Long organizationId = employee.getOrganization() != null ? employee.getOrganization().getId() : TenantContext.requireOrganizationId();
        LocalDate today = LocalDate.now(clock);

        AttendanceCalendarDayDto dayDto = new AttendanceCalendarDayDto();
        dayDto.setDate(date);
        dayDto.setDayOfWeek(date.getDayOfWeek().name());

        // Check if attendance record exists for this date and populate timestamps/breaks
        Optional<Attendance> attendanceOpt = attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(
                employee.getId(), date, organizationId);
        if (attendanceOpt.isPresent()) {
            Attendance att = attendanceOpt.get();
            dayDto.setCheckInTime(att.getCheckInTime());
            dayDto.setCheckOutTime(att.getCheckOutTime());
            dayDto.setTotalWorkingMinutes(att.getTotalWorkingMinutes() != null ? att.getTotalWorkingMinutes() : 0);
            dayDto.setTotalBreakMinutes(att.getTotalBreakMinutes() != null ? att.getTotalBreakMinutes() : 0);
            dayDto.setIsLate(att.getIsLate() != null ? att.getIsLate() : false);
            dayDto.setLateBy(att.getLateBy() != null ? att.getLateBy() : "00:00");

            if (att.getId() != null) {
                List<AttendanceBreak> breakEntities = attendanceBreakRepository.findByAttendanceIdOrderByBreakStartTimeAsc(att.getId());
                List<AttendanceBreakDto> breakDtos = breakEntities.stream()
                        .map(attendanceService::mapBreakToDto)
                        .toList();
                dayDto.setBreaks(breakDtos);
            }
        }

        // 1. Holiday check
        Optional<Holiday> holidayOpt = holidayRepository.findByOrganizationIdAndHolidayDateAndStatus(
                organizationId, date, HolidayStatus.ACTIVE);
        if (holidayOpt.isPresent()) {
            dayDto.setStatus("HOLIDAY");
            dayDto.setHolidayName(holidayOpt.get().getName());
            return dayDto;
        }

        // 2. Week off check
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dayDto.setStatus("WEEK_OFF");
            return dayDto;
        }

        // 3. Leave check (approved leave)
        List<Leave> leaves = leaveRepository.findOverlappingLeaves(employee.getId(), date, date);
        Optional<Leave> approvedLeave = leaves.stream()
                .filter(l -> "APPROVED".equalsIgnoreCase(l.getStatus()))
                .findFirst();
        if (approvedLeave.isPresent()) {
            Leave l = approvedLeave.get();
            dayDto.setStatus("ON_LEAVE");
            dayDto.setLeaveType(l.getLeaveType() != null ? l.getLeaveType().getName() : "LEAVE");
            return dayDto;
        }

        // 4. Attendance record status
        if (attendanceOpt.isPresent()) {
            Attendance att = attendanceOpt.get();
            String attStatus = att.getStatus() != null ? att.getStatus() : (att.getAttendanceStatus() != null ? att.getAttendanceStatus().name() : "");
            if ("HALF_DAY".equalsIgnoreCase(attStatus)) {
                dayDto.setStatus("HALF_DAY");
            } else if ("ABSENT".equalsIgnoreCase(attStatus)) {
                dayDto.setStatus("ABSENT");
            } else {
                dayDto.setStatus("PRESENT");
            }
            return dayDto;
        }

        // 5. Default fallback
        if (date.isBefore(today)) {
            dayDto.setStatus("ABSENT");
        } else {
            dayDto.setStatus("NOT_CHECKED_IN");
        }
        return dayDto;
    }
}
