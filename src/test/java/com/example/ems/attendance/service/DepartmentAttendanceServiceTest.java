package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.DepartmentAttendanceHistoryQuery;
import com.example.ems.attendance.dto.DepartmentDailyAttendanceResponse;
import com.example.ems.attendance.dto.TeamDepartmentAttendanceHistoryItemDto;
import com.example.ems.attendance.entity.Attendance;
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
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.organization.entity.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentAttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceBreakRepository attendanceBreakRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private Clock clock;

    @InjectMocks
    private DepartmentAttendanceService departmentAttendanceService;

    private Organization organization;
    private Department department;
    private Team team1;
    private Employee emp1;
    private Employee emp2;
    private final ZoneId zoneId = ZoneId.of("UTC");
    private final Instant fixedNow = Instant.parse("2026-09-10T12:00:00Z");

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        TenantContext.setCurrentTenant(100L);

        organization = new Organization();
        organization.setId(100L);
        organization.setName("Acme Corp");

        department = new Department(5L, "Engineering", "ENG", "Engineering Dept");
        department.setOrganization(organization);

        team1 = new Team();
        team1.setId(12L);
        team1.setTeamName("Backend Core");
        team1.setTeamCode("BE-CORE");
        team1.setDepartment(department);
        team1.setOrganization(organization);

        emp1 = new Employee();
        emp1.setId(1L);
        emp1.setEmployeeId("EMP-001");
        emp1.setFullName("Alice Bob");
        emp1.setDepartment("Engineering");
        emp1.setOrganization(organization);

        emp2 = new Employee();
        emp2.setId(2L);
        emp2.setEmployeeId("EMP-002");
        emp2.setFullName("Charlie Dave");
        emp2.setDepartment("Engineering");
        emp2.setOrganization(organization);

        lenient().when(clock.instant()).thenReturn(fixedNow);
        lenient().when(clock.getZone()).thenReturn(zoneId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Department: Daily Attendance returns department rollup, team distribution, and members")
    void testGetDepartmentDailyAttendance_Success() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        when(departmentRepository.findByIdAndOrganizationId(5L, 100L))
                .thenReturn(Optional.of(department));

        when(employeeRepository.findByOrganizationIdAndDepartment(100L, "Engineering"))
                .thenReturn(new ArrayList<>(List.of(emp1, emp2)));

        when(teamRepository.findByDepartmentIdAndOrganizationIdAndDeletedFalse(5L, 100L))
                .thenReturn(List.of(team1));

        TeamMember tm1 = new TeamMember(team1, emp1, LocalDate.of(2026, 1, 1), true);
        when(teamMemberRepository.findByTeamIdAndStatus(12L, "ACTIVE"))
                .thenReturn(List.of(tm1));

        Attendance att1 = new Attendance();
        att1.setId(101L);
        att1.setEmployee(emp1);
        att1.setDate(date);
        att1.setStatus(AttendanceStatus.COMPLETED);
        att1.setCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        att1.setCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));
        att1.setTotalWorkingMinutes(480);
        att1.setTotalBreakMinutes(45);

        when(attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(List.of(1L, 2L), date, 100L))
                .thenReturn(List.of(att1));

        when(leaveRepository.findByEmployeeIdInAndStatus(List.of(1L, 2L), "APPROVED"))
                .thenReturn(Collections.emptyList());

        DepartmentDailyAttendanceResponse response = departmentAttendanceService.getDepartmentDailyAttendance(5L, date, null);

        assertNotNull(response);
        assertEquals(5L, response.getDepartmentId());
        assertEquals("Engineering", response.getDepartmentName());
        assertEquals(2, response.getTotalEmployees());
        assertEquals(1, response.getPresentCount());
        assertEquals(1, response.getNotCheckedInCount());
        assertEquals(1, response.getTeams().size());
        assertEquals("Backend Core", response.getTeams().get(0).getTeamName());
        assertEquals(1, response.getTeams().get(0).getPresentCount());
        assertEquals(2, response.getMembers().size());
    }

    @Test
    @DisplayName("Department: Daily Attendance throws AttendanceNotFoundException for cross-tenant or missing department")
    void testGetDepartmentDailyAttendance_NotFoundOrCrossTenant() {
        when(departmentRepository.findByIdAndOrganizationId(999L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () ->
                departmentAttendanceService.getDepartmentDailyAttendance(999L, LocalDate.now(), null));
    }

    @Test
    @DisplayName("Department: History retrieves paginated department member records with date filtering")
    void testGetDepartmentAttendanceHistory_Success() {
        when(departmentRepository.findByIdAndOrganizationId(5L, 100L))
                .thenReturn(Optional.of(department));

        when(employeeRepository.findByOrganizationIdAndDepartment(100L, "Engineering"))
                .thenReturn(new ArrayList<>(List.of(emp1)));
        when(teamRepository.findByDepartmentIdAndOrganizationIdAndDeletedFalse(5L, 100L))
                .thenReturn(Collections.emptyList());

        Attendance att1 = new Attendance();
        att1.setId(101L);
        att1.setEmployee(emp1);
        att1.setDate(LocalDate.of(2026, 9, 8));
        att1.setStatus(AttendanceStatus.COMPLETED);
        att1.setTotalWorkingMinutes(480);

        Page<Attendance> page = new PageImpl<>(List.of(att1), PageRequest.of(0, 20), 1);
        when(attendanceRepository.findHistoryForEmployees(
                eq(List.of(1L)), eq(100L), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        DepartmentAttendanceHistoryQuery query = new DepartmentAttendanceHistoryQuery();
        query.setFromDate(LocalDate.of(2026, 9, 1));
        query.setToDate(LocalDate.of(2026, 9, 10));

        Page<TeamDepartmentAttendanceHistoryItemDto> result = departmentAttendanceService.getDepartmentAttendanceHistory(5L, query);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        TeamDepartmentAttendanceHistoryItemDto item = result.getContent().get(0);
        assertEquals(101L, item.getAttendanceId());
        assertEquals("EMP-001", item.getEmployeeCode());
        assertEquals("Alice Bob", item.getEmployeeName());
        assertEquals("Engineering", item.getDepartmentName());
    }

    @Test
    @DisplayName("Department: Status filter returns only matching members on daily attendance")
    void testGetDepartmentDailyAttendance_StatusFilter() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        when(departmentRepository.findByIdAndOrganizationId(5L, 100L))
                .thenReturn(Optional.of(department));

        when(employeeRepository.findByOrganizationIdAndDepartment(100L, "Engineering"))
                .thenReturn(new ArrayList<>(List.of(emp1, emp2)));

        when(teamRepository.findByDepartmentIdAndOrganizationIdAndDeletedFalse(5L, 100L))
                .thenReturn(Collections.emptyList());

        Attendance att1 = new Attendance();
        att1.setId(101L);
        att1.setEmployee(emp1);
        att1.setDate(date);
        att1.setStatus(AttendanceStatus.COMPLETED);

        when(attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(List.of(1L, 2L), date, 100L))
                .thenReturn(List.of(att1));

        when(leaveRepository.findByEmployeeIdInAndStatus(List.of(1L, 2L), "APPROVED"))
                .thenReturn(Collections.emptyList());

        DepartmentDailyAttendanceResponse response = departmentAttendanceService.getDepartmentDailyAttendance(5L, date, AttendanceStatus.COMPLETED);

        assertNotNull(response);
        assertEquals(2, response.getTotalEmployees());
        assertEquals(1, response.getPresentCount());
        assertEquals(1, response.getMembers().size());
        assertEquals("COMPLETED", response.getMembers().get(0).getStatus());
    }

    @Test
    @DisplayName("Department: History rejects unwhitelisted sort fields")
    void testGetDepartmentAttendanceHistory_UnsafeSortField_ThrowsException() {
        when(departmentRepository.findByIdAndOrganizationId(5L, 100L))
                .thenReturn(Optional.of(department));

        DepartmentAttendanceHistoryQuery query = new DepartmentAttendanceHistoryQuery();
        query.setSortBy("organizationId");

        assertThrows(IllegalArgumentException.class, () ->
                departmentAttendanceService.getDepartmentAttendanceHistory(5L, query));
    }
}
