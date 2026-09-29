package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.*;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceBreak;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.exception.AttendanceNotFoundException;
import com.example.ems.attendance.repository.AttendanceBreakRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.Team;
import com.example.ems.employee.entity.TeamMember;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentAttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceBreakRepository attendanceBreakRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private Clock clock;

    @Transactional(readOnly = true)
    public DepartmentDailyAttendanceResponse getDepartmentDailyAttendance(Long departmentId, LocalDate date, AttendanceStatus statusFilter) {
        Long organizationId = TenantContext.requireOrganizationId();
        Department department = departmentRepository.findByIdAndOrganizationId(departmentId, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Department not found with ID: " + departmentId + " within the current organization."));

        LocalDate targetDate = (date != null) ? date : LocalDate.now(clock);
        LocalDate today = LocalDate.now(clock);

        List<Employee> departmentEmployees = employeeRepository.findByOrganizationIdAndDepartment(organizationId, department.getName());
        List<Team> departmentTeams = teamRepository.findByDepartmentIdAndOrganizationIdAndDeletedFalse(departmentId, organizationId);

        // Find active team memberships in these teams to map employee -> teamName
        Map<Long, String> employeeTeamNameMap = new HashMap<>();
        Map<Long, List<TeamMember>> teamMembersByTeamId = new HashMap<>();
        for (Team t : departmentTeams) {
            List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatus(t.getId(), "ACTIVE");
            teamMembersByTeamId.put(t.getId(), members);
            for (TeamMember tm : members) {
                employeeTeamNameMap.put(tm.getEmployee().getId(), t.getTeamName());
                if (departmentEmployees.stream().noneMatch(e -> e.getId().equals(tm.getEmployee().getId()))) {
                    departmentEmployees.add(tm.getEmployee());
                }
            }
        }

        DepartmentDailyAttendanceResponse response = new DepartmentDailyAttendanceResponse();
        response.setDepartmentId(department.getId());
        response.setDepartmentName(department.getName());
        response.setDepartmentCode(department.getCode());
        response.setDate(targetDate);
        response.setTotalEmployees(departmentEmployees.size());

        if (departmentEmployees.isEmpty()) {
            return response;
        }

        List<Long> employeeIds = departmentEmployees.stream()
                .map(Employee::getId)
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

        int deptPresent = 0;
        int deptAbsent = 0;
        int deptOnLeave = 0;
        int deptLate = 0;
        int deptNotCheckedIn = 0;

        List<DepartmentMemberDailyAttendanceDto> memberDtos = new ArrayList<>();
        Map<Long, DepartmentMemberDailyAttendanceDto> memberDtoMap = new HashMap<>();

        for (Employee emp : departmentEmployees) {
            DepartmentMemberDailyAttendanceDto dto = new DepartmentMemberDailyAttendanceDto();
            dto.setEmployeeId(emp.getId());
            dto.setEmployeeCode(emp.getEmployeeId());
            dto.setFullName(emp.getFullName());
            dto.setDesignation(emp.getDesignation() != null ? emp.getDesignation() : "Employee");
            dto.setTeamName(employeeTeamNameMap.getOrDefault(emp.getId(), "Unassigned"));

            Attendance att = attendanceByEmpId.get(emp.getId());
            if (att != null) {
                dto.setCheckInTime(att.getCheckInTime());
                dto.setCheckOutTime(att.getCheckOutTime());
                dto.setTotalWorkingMinutes(att.getTotalWorkingMinutes() != null ? att.getTotalWorkingMinutes() : 0);
                dto.setTotalBreakMinutes(att.getTotalBreakMinutes() != null ? att.getTotalBreakMinutes() : 0);
                dto.setIsLate(att.getIsLate() != null && att.getIsLate());
                dto.setLateBy(att.getLateBy() != null ? att.getLateBy() : "00:00");

                if (dto.getIsLate()) {
                    deptLate++;
                }

                String attStatus = att.getStatus() != null ? att.getStatus() : (att.getAttendanceStatus() != null ? att.getAttendanceStatus().name() : "PRESENT");
                dto.setStatus(attStatus);

                if ("ABSENT".equalsIgnoreCase(attStatus)) {
                    deptAbsent++;
                } else {
                    deptPresent++;
                }
            } else if (onLeaveEmpIds.contains(emp.getId())) {
                dto.setStatus("ON_LEAVE");
                deptOnLeave++;
            } else {
                if (targetDate.isBefore(today)) {
                    dto.setStatus("ABSENT");
                    deptAbsent++;
                } else {
                    dto.setStatus("NOT_CHECKED_IN");
                    deptNotCheckedIn++;
                }
            }

            memberDtoMap.put(emp.getId(), dto);

            if (statusFilter == null || dto.getStatus().equalsIgnoreCase(statusFilter.name())) {
                memberDtos.add(dto);
            }
        }

        // Build Team-level summaries
        List<DepartmentTeamSummaryDto> teamSummaries = new ArrayList<>();
        for (Team t : departmentTeams) {
            List<TeamMember> members = teamMembersByTeamId.getOrDefault(t.getId(), Collections.emptyList());
            DepartmentTeamSummaryDto teamDto = new DepartmentTeamSummaryDto();
            teamDto.setTeamId(t.getId());
            teamDto.setTeamName(t.getTeamName());
            teamDto.setTeamCode(t.getTeamCode());
            teamDto.setTotalEmployees(members.size());

            int tPres = 0;
            int tAbs = 0;
            int tLeave = 0;
            int tLate = 0;
            int tNotChecked = 0;

            for (TeamMember tm : members) {
                DepartmentMemberDailyAttendanceDto mDto = memberDtoMap.get(tm.getEmployee().getId());
                if (mDto != null) {
                    if (mDto.getIsLate()) tLate++;
                    if ("ON_LEAVE".equalsIgnoreCase(mDto.getStatus())) {
                        tLeave++;
                    } else if ("ABSENT".equalsIgnoreCase(mDto.getStatus())) {
                        tAbs++;
                    } else if ("NOT_CHECKED_IN".equalsIgnoreCase(mDto.getStatus())) {
                        tNotChecked++;
                    } else {
                        tPres++;
                    }
                }
            }

            teamDto.setPresentCount(tPres);
            teamDto.setAbsentCount(tAbs);
            teamDto.setOnLeaveCount(tLeave);
            teamDto.setLateCount(tLate);
            teamDto.setNotCheckedInCount(tNotChecked);
            teamSummaries.add(teamDto);
        }

        response.setPresentCount(deptPresent);
        response.setAbsentCount(deptAbsent);
        response.setOnLeaveCount(deptOnLeave);
        response.setLateCount(deptLate);
        response.setNotCheckedInCount(deptNotCheckedIn);
        response.setTeams(teamSummaries);
        response.setMembers(memberDtos);

        return response;
    }

    @Transactional(readOnly = true)
    public Page<TeamDepartmentAttendanceHistoryItemDto> getDepartmentAttendanceHistory(Long departmentId, DepartmentAttendanceHistoryQuery query) {
        Long organizationId = TenantContext.requireOrganizationId();
        Department department = departmentRepository.findByIdAndOrganizationId(departmentId, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Department not found with ID: " + departmentId + " within the current organization."));

        if (query == null) {
            query = new DepartmentAttendanceHistoryQuery();
        }

        if (query.getFromDate() != null && query.getToDate() != null && query.getFromDate().isAfter(query.getToDate())) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }

        List<Employee> departmentEmployees = employeeRepository.findByOrganizationIdAndDepartment(organizationId, department.getName());
        List<Team> departmentTeams = teamRepository.findByDepartmentIdAndOrganizationIdAndDeletedFalse(departmentId, organizationId);

        Map<Long, String> employeeTeamNameMap = new HashMap<>();
        for (Team t : departmentTeams) {
            List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatus(t.getId(), "ACTIVE");
            for (TeamMember tm : members) {
                employeeTeamNameMap.put(tm.getEmployee().getId(), t.getTeamName());
                if (departmentEmployees.stream().noneMatch(e -> e.getId().equals(tm.getEmployee().getId()))) {
                    departmentEmployees.add(tm.getEmployee());
                }
            }
        }

        if (departmentEmployees.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), query.toPageable(), 0);
        }

        List<Long> employeeIds = departmentEmployees.stream()
                .map(Employee::getId)
                .toList();

        Map<Long, Employee> employeeMap = departmentEmployees.stream()
                .collect(Collectors.toMap(Employee::getId, e -> e, (e1, e2) -> e1));

        Pageable pageable = query.toPageable();
        Page<Attendance> page = attendanceRepository.findHistoryForEmployees(
                employeeIds,
                organizationId,
                query.getFromDate(),
                query.getToDate(),
                query.getStatus(),
                pageable
        );

        return page.map(att -> {
            Long empId = att.getEmployee() != null ? att.getEmployee().getId() : null;
            String teamName = (empId != null) ? employeeTeamNameMap.getOrDefault(empId, "Unassigned") : "Unassigned";
            Employee emp = (empId != null) ? employeeMap.get(empId) : null;
            return mapToDepartmentHistoryItem(att, department.getName(), teamName, emp);
        });
    }

    private TeamDepartmentAttendanceHistoryItemDto mapToDepartmentHistoryItem(Attendance att, String departmentName, String teamName, Employee emp) {
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
        dto.setDepartmentName(departmentName);
        dto.setTeamName(teamName);

        if (emp != null) {
            dto.setEmployeeId(emp.getId());
            dto.setEmployeeCode(emp.getEmployeeId());
            dto.setEmployeeName(emp.getFullName());
            dto.setDesignation(emp.getDesignation());
        } else if (att.getEmployee() != null) {
            dto.setEmployeeId(att.getEmployee().getId());
            dto.setEmployeeCode(att.getEmployee().getEmployeeId());
            dto.setEmployeeName(att.getEmployee().getFullName());
            dto.setDesignation(att.getEmployee().getDesignation());
        }

        if (att.getBreaks() != null && !att.getBreaks().isEmpty()) {
            dto.setBreaks(att.getBreaks().stream().map(attendanceService::mapBreakToDto).toList());
        } else if (att.getId() != null) {
            List<AttendanceBreak> breakEntities = attendanceBreakRepository.findByAttendanceIdOrderByBreakStartTimeAsc(att.getId());
            dto.setBreaks(breakEntities.stream().map(attendanceService::mapBreakToDto).toList());
        }

        return dto;
    }
}
