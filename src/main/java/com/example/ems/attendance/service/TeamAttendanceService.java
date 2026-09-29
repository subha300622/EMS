package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.*;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceBreak;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.exception.AttendanceNotFoundException;
import com.example.ems.attendance.repository.AttendanceBreakRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.Team;
import com.example.ems.employee.entity.TeamMember;
import com.example.ems.employee.repository.TeamMemberRepository;
import com.example.ems.employee.repository.TeamRepository;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamAttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceBreakRepository attendanceBreakRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private Clock clock;

    // ── Canonical Team Attendance APIs ──────────────────────────────────────

    @Transactional(readOnly = true)
    public TeamDailyAttendanceResponse getTeamDailyAttendance(Long teamId, LocalDate date, AttendanceStatus statusFilter) {
        Long organizationId = TenantContext.requireOrganizationId();
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Team not found with ID: " + teamId + " within the current organization."));

        LocalDate targetDate = (date != null) ? date : LocalDate.now(clock);
        LocalDate today = LocalDate.now(clock);

        List<TeamMember> activeMembers = teamMemberRepository.findByTeamIdAndStatus(teamId, "ACTIVE");
        TeamDailyAttendanceResponse response = new TeamDailyAttendanceResponse();
        response.setTeamId(team.getId());
        response.setTeamName(team.getTeamName());
        response.setTeamCode(team.getTeamCode());
        response.setDate(targetDate);
        response.setTotalEmployees(activeMembers.size());

        if (activeMembers.isEmpty()) {
            return response;
        }

        List<Long> employeeIds = activeMembers.stream()
                .map(m -> m.getEmployee().getId())
                .toList();

        List<Attendance> attendanceRecords = attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(
                employeeIds, targetDate, organizationId);
        Map<Long, Attendance> attendanceByEmpId = attendanceRecords.stream()
                .collect(Collectors.toMap(a -> a.getEmployee().getId(), a -> a, (a1, a2) -> a1));

        List<Leave> leaves = leaveRepository.findByEmployeeIdInAndStatus(employeeIds, "APPROVED");
        Set<Long> onLeaveEmpIds = leaves.stream()
                .filter(l -> !targetDate.isBefore(l.getStartDate()) && !targetDate.isAfter(l.getEndDate()))
                .map(l -> l.getEmployee().getId())
                .collect(Collectors.toSet());

        int presentCount = 0;
        int absentCount = 0;
        int onLeaveCount = 0;
        int lateCount = 0;
        int notCheckedInCount = 0;

        List<TeamMemberDailyAttendanceDto> memberDtos = new ArrayList<>();

        for (TeamMember member : activeMembers) {
            Employee emp = member.getEmployee();
            TeamMemberDailyAttendanceDto dto = new TeamMemberDailyAttendanceDto();
            dto.setEmployeeId(emp.getId());
            dto.setEmployeeCode(emp.getEmployeeId());
            dto.setFullName(emp.getFullName());
            dto.setDesignation(emp.getDesignation() != null ? emp.getDesignation() : "Employee");
            dto.setIsTeamLead(member.getIsTeamLead() != null && member.getIsTeamLead());

            Attendance att = attendanceByEmpId.get(emp.getId());
            if (att != null) {
                dto.setCheckInTime(att.getCheckInTime());
                dto.setCheckOutTime(att.getCheckOutTime());
                dto.setTotalWorkingMinutes(att.getTotalWorkingMinutes() != null ? att.getTotalWorkingMinutes() : 0);
                dto.setTotalBreakMinutes(att.getTotalBreakMinutes() != null ? att.getTotalBreakMinutes() : 0);
                dto.setIsLate(att.getIsLate() != null && att.getIsLate());
                dto.setLateBy(att.getLateBy() != null ? att.getLateBy() : "00:00");

                if (dto.getIsLate()) {
                    lateCount++;
                }

                String attStatus = att.getStatus() != null ? att.getStatus() : (att.getAttendanceStatus() != null ? att.getAttendanceStatus().name() : "PRESENT");
                dto.setStatus(attStatus);

                if ("ABSENT".equalsIgnoreCase(attStatus)) {
                    absentCount++;
                } else {
                    presentCount++;
                }
            } else if (onLeaveEmpIds.contains(emp.getId())) {
                dto.setStatus("ON_LEAVE");
                onLeaveCount++;
            } else {
                if (targetDate.isBefore(today)) {
                    dto.setStatus("ABSENT");
                    absentCount++;
                } else {
                    dto.setStatus("NOT_CHECKED_IN");
                    notCheckedInCount++;
                }
            }

            if (statusFilter == null || dto.getStatus().equalsIgnoreCase(statusFilter.name())) {
                memberDtos.add(dto);
            }
        }

        response.setPresentCount(presentCount);
        response.setAbsentCount(absentCount);
        response.setOnLeaveCount(onLeaveCount);
        response.setLateCount(lateCount);
        response.setNotCheckedInCount(notCheckedInCount);
        response.setMembers(memberDtos);

        return response;
    }

    @Transactional(readOnly = true)
    public Page<TeamDepartmentAttendanceHistoryItemDto> getTeamAttendanceHistory(Long teamId, TeamAttendanceHistoryQuery query) {
        Long organizationId = TenantContext.requireOrganizationId();
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Team not found with ID: " + teamId + " within the current organization."));

        if (query == null) {
            query = new TeamAttendanceHistoryQuery();
        }

        if (query.getFromDate() != null && query.getToDate() != null && query.getFromDate().isAfter(query.getToDate())) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }

        List<TeamMember> activeMembers = teamMemberRepository.findByTeamIdAndStatus(teamId, "ACTIVE");
        if (activeMembers.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), query.toPageable(), 0);
        }

        List<Long> employeeIds = activeMembers.stream()
                .map(m -> m.getEmployee().getId())
                .toList();

        Map<Long, Employee> employeeMap = activeMembers.stream()
                .collect(Collectors.toMap(m -> m.getEmployee().getId(), TeamMember::getEmployee, (e1, e2) -> e1));

        Pageable pageable = query.toPageable();
        Page<Attendance> page = attendanceRepository.findHistoryForEmployees(
                employeeIds,
                organizationId,
                query.getFromDate(),
                query.getToDate(),
                query.getStatus(),
                pageable
        );

        return page.map(att -> mapToTeamHistoryItem(att, team.getTeamName(), employeeMap.get(att.getEmployee().getId())));
    }

    private TeamDepartmentAttendanceHistoryItemDto mapToTeamHistoryItem(Attendance att, String teamName, Employee emp) {
        TeamDepartmentAttendanceHistoryItemDto dto = new TeamDepartmentAttendanceHistoryItemDto();
        dto.setAttendanceId(att.getId());
        dto.setAttendanceDate(att.getDate());
        dto.setStatus(att.getStatus() != null ? att.getStatus() : (att.getAttendanceStatus() != null ? att.getAttendanceStatus().name() : null));
        dto.setCheckInTime(att.getCheckInTime());
        dto.setCheckOutTime(att.getCheckOutTime());
        dto.setTotalBreakMinutes(att.getTotalBreakMinutes() != null ? att.getTotalBreakMinutes() : 0);
        dto.setTotalWorkingMinutes(att.getTotalWorkingMinutes() != null ? att.getTotalWorkingMinutes() : 0);
        dto.setIsLate(att.getIsLate());
        dto.setLateBy(att.getLateBy());
        dto.setTeamName(teamName);

        if (emp != null) {
            dto.setEmployeeId(emp.getId());
            dto.setEmployeeCode(emp.getEmployeeId());
            dto.setEmployeeName(emp.getFullName());
            dto.setDesignation(emp.getDesignation());
            dto.setDepartmentName(emp.getDepartment());
        } else if (att.getEmployee() != null) {
            dto.setEmployeeId(att.getEmployee().getId());
            dto.setEmployeeCode(att.getEmployee().getEmployeeId());
            dto.setEmployeeName(att.getEmployee().getFullName());
            dto.setDesignation(att.getEmployee().getDesignation());
            dto.setDepartmentName(att.getEmployee().getDepartment());
        }

        if (att.getBreaks() != null && !att.getBreaks().isEmpty()) {
            dto.setBreaks(att.getBreaks().stream().map(attendanceService::mapBreakToDto).toList());
        } else if (att.getId() != null) {
            List<AttendanceBreak> breakEntities = attendanceBreakRepository.findByAttendanceIdOrderByBreakStartTimeAsc(att.getId());
            dto.setBreaks(breakEntities.stream().map(attendanceService::mapBreakToDto).toList());
        }

        return dto;
    }

    // ── Legacy Methods (Preserved for compatibility) ─────────────────────────

    public List<TeamMemberAttendanceDto> getTeamAttendance(List<Employee> employees, LocalDate startDate, LocalDate endDate) {
        if (employees.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());
        List<Attendance> records = attendanceRepository.findByEmployeeIdInAndDateBetween(employeeIds, startDate, endDate);

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
                .collect(Collectors.toMap(a -> a.getEmployee().getId(), a -> a, (a1, a2) -> a1));

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
                    present++;
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
