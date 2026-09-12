package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.early.EarlyCheckoutQuery;
import com.example.ems.attendance.dto.early.EarlyCheckoutReportDto;
import com.example.ems.attendance.dto.late.LateAttendanceQuery;
import com.example.ems.attendance.dto.late.LateAttendanceReportDto;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.Team;
import com.example.ems.employee.entity.TeamMember;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.TeamMemberRepository;
import com.example.ems.employee.repository.TeamRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class AttendanceLateEarlyService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendancePolicyService attendancePolicyService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public Page<LateAttendanceReportDto> getLateAttendanceReport(LateAttendanceQuery query) {
        Long organizationId = TenantContext.requireOrganizationId();
        if (query == null) {
            query = new LateAttendanceQuery();
        }

        validateDateRange(query.getFromDate(), query.getToDate());

        List<Long> scopedEmployeeIds = resolveScopeEmployeeIds(organizationId, query.getDepartmentId(), query.getTeamId());
        if (scopedEmployeeIds != null && scopedEmployeeIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), query.toPageable(), 0);
        }

        Pageable pageable = query.toPageable();
        Page<Attendance> page = attendanceRepository.findLateAttendance(
                organizationId,
                scopedEmployeeIds,
                query.getEmployeeId(),
                query.getDate(),
                query.getFromDate(),
                query.getToDate(),
                pageable
        );

        AttendancePolicy policy = attendancePolicyService.getActivePolicy(organizationId);
        return page.map(att -> mapToLateDto(att, policy));
    }

    @Transactional(readOnly = true)
    public Page<EarlyCheckoutReportDto> getEarlyCheckoutReport(EarlyCheckoutQuery query) {
        Long organizationId = TenantContext.requireOrganizationId();
        if (query == null) {
            query = new EarlyCheckoutQuery();
        }

        validateDateRange(query.getFromDate(), query.getToDate());

        List<Long> scopedEmployeeIds = resolveScopeEmployeeIds(organizationId, query.getDepartmentId(), query.getTeamId());
        if (scopedEmployeeIds != null && scopedEmployeeIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), query.toPageable(), 0);
        }

        Pageable pageable = query.toPageable();
        Page<Attendance> page = attendanceRepository.findEarlyCheckoutAttendance(
                organizationId,
                scopedEmployeeIds,
                query.getEmployeeId(),
                query.getDate(),
                query.getFromDate(),
                query.getToDate(),
                pageable
        );

        AttendancePolicy policy = attendancePolicyService.getActivePolicy(organizationId);
        return page.map(att -> mapToEarlyDto(att, policy));
    }

    private List<Long> resolveScopeEmployeeIds(Long organizationId, Long departmentId, Long teamId) {
        if (departmentId == null && teamId == null) {
            return null; // No department/team filter restriction
        }

        Set<Long> employeeIds = new HashSet<>();

        if (departmentId != null) {
            Optional<Department> deptOpt = departmentRepository.findByIdAndOrganizationId(departmentId, organizationId);
            if (deptOpt.isPresent()) {
                List<Employee> deptEmps = employeeRepository.findByOrganizationIdAndDepartment(organizationId, deptOpt.get().getName());
                deptEmps.forEach(e -> employeeIds.add(e.getId()));

                List<Team> deptTeams = teamRepository.findByDepartmentIdAndOrganizationIdAndDeletedFalse(departmentId, organizationId);
                for (Team t : deptTeams) {
                    List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatus(t.getId(), "ACTIVE");
                    members.forEach(m -> employeeIds.add(m.getEmployee().getId()));
                }
            }
        }

        if (teamId != null) {
            Optional<Team> teamOpt = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, organizationId);
            if (teamOpt.isPresent()) {
                List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatus(teamId, "ACTIVE");
                Set<Long> teamEmpIds = new HashSet<>();
                members.forEach(m -> teamEmpIds.add(m.getEmployee().getId()));
                if (departmentId != null) {
                    employeeIds.retainAll(teamEmpIds);
                } else {
                    employeeIds.addAll(teamEmpIds);
                }
            } else {
                return Collections.emptyList();
            }
        }

        return new ArrayList<>(employeeIds);
    }

    private LateAttendanceReportDto mapToLateDto(Attendance att, AttendancePolicy policy) {
        LateAttendanceReportDto dto = new LateAttendanceReportDto();
        dto.setAttendanceId(att.getId());
        dto.setDate(att.getDate());
        dto.setCheckInTime(att.getCheckInTime());
        dto.setStatus(att.getStatus() != null ? att.getStatus() : (att.getAttendanceStatus() != null ? att.getAttendanceStatus().name() : null));
        dto.setLateBy(att.getLateBy() != null ? att.getLateBy() : "00:00");
        dto.setLateByMinutes(att.getLateByMinutes() != null ? att.getLateByMinutes() : 0);

        if (policy != null) {
            dto.setExpectedStartTime(policy.getOfficeStartTime());
            dto.setGracePeriodMinutes(policy.getGracePeriodMinutes());
        }

        if (att.getEmployee() != null) {
            dto.setEmployeeId(att.getEmployee().getId());
            dto.setEmployeeName(att.getEmployee().getFullName());
            dto.setEmployeeCode(att.getEmployee().getEmployeeId());
            dto.setDepartment(att.getEmployee().getDepartment());
            if (att.getEmployee().getTeam() != null) {
                dto.setTeam(att.getEmployee().getTeam().getTeamName());
            }
        }
        return dto;
    }

    private EarlyCheckoutReportDto mapToEarlyDto(Attendance att, AttendancePolicy policy) {
        EarlyCheckoutReportDto dto = new EarlyCheckoutReportDto();
        dto.setAttendanceId(att.getId());
        dto.setDate(att.getDate());
        dto.setCheckOutTime(att.getCheckOutTime());
        dto.setStatus(att.getStatus() != null ? att.getStatus() : (att.getAttendanceStatus() != null ? att.getAttendanceStatus().name() : null));
        dto.setEarlyBy(att.getEarlyBy() != null ? att.getEarlyBy() : "00:00");
        dto.setEarlyByMinutes(att.getEarlyByMinutes() != null ? att.getEarlyByMinutes() : 0);

        if (policy != null) {
            dto.setExpectedEndTime(policy.getOfficeEndTime());
            dto.setEarlyCheckoutThresholdMinutes(policy.getEarlyCheckoutThreshold());
        }

        if (att.getEmployee() != null) {
            dto.setEmployeeId(att.getEmployee().getId());
            dto.setEmployeeName(att.getEmployee().getFullName());
            dto.setEmployeeCode(att.getEmployee().getEmployeeId());
            dto.setDepartment(att.getEmployee().getDepartment());
            if (att.getEmployee().getTeam() != null) {
                dto.setTeam(att.getEmployee().getTeam().getTeamName());
            }
        }
        return dto;
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }
    }
}
