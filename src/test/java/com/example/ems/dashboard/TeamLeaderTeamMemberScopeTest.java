package com.example.ems.dashboard;

import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.dto.teamleader.TeamLeaveRecommendationRequest;
import com.example.ems.employee.dto.teamleader.TeamMemberRosterDto;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.Team;
import com.example.ems.employee.entity.TeamMember;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.TeamMemberRepository;
import com.example.ems.employee.repository.TeamRepository;
import com.example.ems.employee.service.impl.TeamLeaderDashboardServiceImpl;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TeamLeaderTeamMemberScopeTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private TeamLeaderDashboardServiceImpl service;

    private Employee tlA;
    private Employee empA1;
    private Employee empA2;

    private Employee tlB;
    private Employee empB1;
    private Employee empB2;

    private Team teamA;
    private Team teamB;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);

        tlA = new Employee();
        tlA.setId(10L);
        tlA.setEmail("tlA@company.com");
        tlA.setFullName("Team Leader A");

        empA1 = new Employee();
        empA1.setId(101L);
        empA1.setEmail("empA1@company.com");
        empA1.setFullName("Employee A1");

        empA2 = new Employee();
        empA2.setId(102L);
        empA2.setEmail("empA2@company.com");
        empA2.setFullName("Employee A2");

        tlB = new Employee();
        tlB.setId(20L);
        tlB.setEmail("tlB@company.com");
        tlB.setFullName("Team Leader B");

        empB1 = new Employee();
        empB1.setId(201L);
        empB1.setEmail("empB1@company.com");
        empB1.setFullName("Employee B1");

        empB2 = new Employee();
        empB2.setId(202L);
        empB2.setEmail("empB2@company.com");
        empB2.setFullName("Employee B2");

        teamA = new Team();
        teamA.setId(1001L);
        teamA.setTeamName("Team A");
        teamA.setTeamLead(tlA);

        teamB = new Team();
        teamB.setId(1002L);
        teamB.setTeamName("Team B");
        teamB.setTeamLead(tlB);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetTeamMembers_StrictTeamIsolation_ReturnsOnlyTeamAMembers() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tlA@company.com", "password", Collections.emptyList())
        );

        when(employeeRepository.findByEmailAndOrganizationId("tlA@company.com", 1L))
                .thenReturn(Optional.of(tlA));
        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(10L, 1L))
                .thenReturn(List.of(teamA));

        TeamMember tmA1 = new TeamMember(teamA, empA1, LocalDate.now(), false);
        TeamMember tmA2 = new TeamMember(teamA, empA2, LocalDate.now(), false);
        when(teamMemberRepository.findByTeamIdInAndStatus(List.of(1001L), "ACTIVE"))
                .thenReturn(List.of(tmA1, tmA2));

        when(attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(anyList(), any(LocalDate.class), eq(1L)))
                .thenReturn(Collections.emptyList());

        List<TeamMemberRosterDto> roster = service.getTeamMembers();

        assertEquals(2, roster.size());
        assertTrue(roster.stream().anyMatch(m -> m.getId().equals(101L) && "Employee A1".equals(m.getName())));
        assertTrue(roster.stream().anyMatch(m -> m.getId().equals(102L) && "Employee A2".equals(m.getName())));

        // Must strictly NOT return B1 or B2
        assertFalse(roster.stream().anyMatch(m -> m.getId().equals(201L)));
        assertFalse(roster.stream().anyMatch(m -> m.getId().equals(202L)));
    }

    @Test
    void testGetTeamMemberById_IDORBlocked_AttemptAccessB1Returns403() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tlA@company.com", "password", Collections.emptyList())
        );

        when(employeeRepository.findByEmailAndOrganizationId("tlA@company.com", 1L))
                .thenReturn(Optional.of(tlA));
        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(10L, 1L))
                .thenReturn(List.of(teamA));

        TeamMember tmA1 = new TeamMember(teamA, empA1, LocalDate.now(), false);
        TeamMember tmA2 = new TeamMember(teamA, empA2, LocalDate.now(), false);
        when(teamMemberRepository.findByTeamIdInAndStatus(List.of(1001L), "ACTIVE"))
                .thenReturn(List.of(tmA1, tmA2));

        // Team Leader A attempts to access B1's data (ID: 201)
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.getTeamMemberById(201L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(ex.getReason().contains("not a member of your team"));
    }

    @Test
    void testLeaveRecommendation_IDORBlocked_AttemptRecommendB1LeaveReturns403() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tlA@company.com", "password", Collections.emptyList())
        );

        when(employeeRepository.findByEmailAndOrganizationId("tlA@company.com", 1L))
                .thenReturn(Optional.of(tlA));
        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(10L, 1L))
                .thenReturn(List.of(teamA));

        TeamMember tmA1 = new TeamMember(teamA, empA1, LocalDate.now(), false);
        TeamMember tmA2 = new TeamMember(teamA, empA2, LocalDate.now(), false);
        when(teamMemberRepository.findByTeamIdInAndStatus(List.of(1001L), "ACTIVE"))
                .thenReturn(List.of(tmA1, tmA2));

        // B1's leave request
        Leave leaveB1 = new Leave();
        leaveB1.setId(888L);
        leaveB1.setEmployee(empB1);
        leaveB1.setStatus("PENDING_TL_RECOMMENDATION");
        leaveB1.setOrganization(new com.example.ems.organization.entity.Organization());
        leaveB1.getOrganization().setId(1L);

        when(leaveRepository.findById(888L)).thenReturn(Optional.of(leaveB1));

        TeamLeaveRecommendationRequest req = new TeamLeaveRecommendationRequest("RECOMMEND", "Unauthorized recommendation");

        // Attempt by TL A to recommend B1's leave must be DENIED (403) with no state change
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.recommendLeave(888L, req));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("PENDING_TL_RECOMMENDATION", leaveB1.getStatus(), "Leave status must not change");
        verify(leaveRepository, never()).save(any(Leave.class));
    }
}
