package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.TeamAttendanceHistoryQuery;
import com.example.ems.attendance.dto.TeamDailyAttendanceResponse;
import com.example.ems.attendance.dto.TeamDepartmentAttendanceHistoryItemDto;
import com.example.ems.attendance.entity.Attendance;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamAttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceBreakRepository attendanceBreakRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private Clock clock;

    @InjectMocks
    private TeamAttendanceService teamAttendanceService;

    private Organization organization;
    private Team team;
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

        team = new Team();
        team.setId(12L);
        team.setTeamName("Backend Core");
        team.setTeamCode("BE-CORE");
        team.setOrganization(organization);

        emp1 = new Employee();
        emp1.setId(1L);
        emp1.setEmployeeId("EMP-001");
        emp1.setFullName("Alice Bob");
        emp1.setDesignation("Lead Engineer");
        emp1.setOrganization(organization);

        emp2 = new Employee();
        emp2.setId(2L);
        emp2.setEmployeeId("EMP-002");
        emp2.setFullName("Charlie Dave");
        emp2.setDesignation("Software Engineer");
        emp2.setOrganization(organization);

        lenient().when(clock.instant()).thenReturn(fixedNow);
        lenient().when(clock.getZone()).thenReturn(zoneId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Team: Daily Attendance returns correct rollup and member breakdown")
    void testGetTeamDailyAttendance_Success() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(12L, 100L))
                .thenReturn(Optional.of(team));

        TeamMember tm1 = new TeamMember(team, emp1, LocalDate.of(2026, 1, 1), true);
        TeamMember tm2 = new TeamMember(team, emp2, LocalDate.of(2026, 1, 1), false);
        when(teamMemberRepository.findByTeamIdAndStatus(12L, "ACTIVE"))
                .thenReturn(List.of(tm1, tm2));

        Attendance att1 = new Attendance();
        att1.setId(101L);
        att1.setEmployee(emp1);
        att1.setDate(date);
        att1.setStatus(AttendanceStatus.COMPLETED);
        att1.setCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        att1.setCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));
        att1.setTotalWorkingMinutes(480);
        att1.setTotalBreakMinutes(60);
        att1.setIsLate(true);
        att1.setLateBy("00:30");

        when(attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(List.of(1L, 2L), date, 100L))
                .thenReturn(List.of(att1));

        Leave leave = new Leave();
        leave.setId(10L);
        leave.setEmployee(emp2);
        leave.setStartDate(date);
        leave.setEndDate(date);
        leave.setStatus("APPROVED");

        when(leaveRepository.findByEmployeeIdInAndStatus(List.of(1L, 2L), "APPROVED"))
                .thenReturn(List.of(leave));

        TeamDailyAttendanceResponse response = teamAttendanceService.getTeamDailyAttendance(12L, date, null);

        assertNotNull(response);
        assertEquals(12L, response.getTeamId());
        assertEquals("Backend Core", response.getTeamName());
        assertEquals(2, response.getTotalEmployees());
        assertEquals(1, response.getPresentCount());
        assertEquals(1, response.getOnLeaveCount());
        assertEquals(1, response.getLateCount());
        assertEquals(0, response.getAbsentCount());
        assertEquals(2, response.getMembers().size());

        assertEquals("COMPLETED", response.getMembers().get(0).getStatus());
        assertTrue(response.getMembers().get(0).getIsTeamLead());
        assertTrue(response.getMembers().get(0).getIsLate());
        assertEquals("00:30", response.getMembers().get(0).getLateBy());

        assertEquals("ON_LEAVE", response.getMembers().get(1).getStatus());
        assertFalse(response.getMembers().get(1).getIsTeamLead());
    }

    @Test
    @DisplayName("Team: Daily Attendance throws AttendanceNotFoundException for cross-tenant or missing team")
    void testGetTeamDailyAttendance_NotFoundOrCrossTenant() {
        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(999L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () ->
                teamAttendanceService.getTeamDailyAttendance(999L, LocalDate.now(), null));
    }

    @Test
    @DisplayName("Team: History retrieves paginated member attendance with date filtering")
    void testGetTeamAttendanceHistory_Success() {
        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(12L, 100L))
                .thenReturn(Optional.of(team));

        TeamMember tm1 = new TeamMember(team, emp1, LocalDate.of(2026, 1, 1), true);
        when(teamMemberRepository.findByTeamIdAndStatus(12L, "ACTIVE"))
                .thenReturn(List.of(tm1));

        Attendance att1 = new Attendance();
        att1.setId(101L);
        att1.setEmployee(emp1);
        att1.setDate(LocalDate.of(2026, 9, 5));
        att1.setStatus(AttendanceStatus.COMPLETED);
        att1.setTotalWorkingMinutes(480);

        Page<Attendance> attPage = new PageImpl<>(List.of(att1), PageRequest.of(0, 20), 1);
        when(attendanceRepository.findHistoryForEmployees(
                eq(List.of(1L)), eq(100L), any(), any(), any(), any(Pageable.class)))
                .thenReturn(attPage);

        TeamAttendanceHistoryQuery query = new TeamAttendanceHistoryQuery();
        query.setFromDate(LocalDate.of(2026, 9, 1));
        query.setToDate(LocalDate.of(2026, 9, 10));

        Page<TeamDepartmentAttendanceHistoryItemDto> result = teamAttendanceService.getTeamAttendanceHistory(12L, query);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        TeamDepartmentAttendanceHistoryItemDto item = result.getContent().get(0);
        assertEquals(101L, item.getAttendanceId());
        assertEquals("EMP-001", item.getEmployeeCode());
        assertEquals("Alice Bob", item.getEmployeeName());
        assertEquals("Backend Core", item.getTeamName());
        assertEquals(480, item.getTotalWorkingMinutes());
    }

    @Test
    @DisplayName("Team: Status filter returns only matching members on daily attendance")
    void testGetTeamDailyAttendance_StatusFilter() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(12L, 100L))
                .thenReturn(Optional.of(team));

        TeamMember tm1 = new TeamMember(team, emp1, LocalDate.of(2026, 1, 1), true);
        TeamMember tm2 = new TeamMember(team, emp2, LocalDate.of(2026, 1, 1), false);
        when(teamMemberRepository.findByTeamIdAndStatus(12L, "ACTIVE"))
                .thenReturn(List.of(tm1, tm2));

        Attendance att1 = new Attendance();
        att1.setId(101L);
        att1.setEmployee(emp1);
        att1.setDate(date);
        att1.setStatus(AttendanceStatus.COMPLETED);

        when(attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(List.of(1L, 2L), date, 100L))
                .thenReturn(List.of(att1));

        when(leaveRepository.findByEmployeeIdInAndStatus(List.of(1L, 2L), "APPROVED"))
                .thenReturn(Collections.emptyList());

        TeamDailyAttendanceResponse response = teamAttendanceService.getTeamDailyAttendance(12L, date, AttendanceStatus.COMPLETED);

        assertNotNull(response);
        assertEquals(2, response.getTotalEmployees());
        assertEquals(1, response.getPresentCount());
        assertEquals(1, response.getMembers().size());
        assertEquals("COMPLETED", response.getMembers().get(0).getStatus());
    }

    @Test
    @DisplayName("Team: History rejects unwhitelisted sort fields")
    void testGetTeamAttendanceHistory_UnsafeSortField_ThrowsException() {
        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(12L, 100L))
                .thenReturn(Optional.of(team));

        TeamAttendanceHistoryQuery query = new TeamAttendanceHistoryQuery();
        query.setSortBy("employeeName");

        assertThrows(IllegalArgumentException.class, () ->
                teamAttendanceService.getTeamAttendanceHistory(12L, query));
    }
}
