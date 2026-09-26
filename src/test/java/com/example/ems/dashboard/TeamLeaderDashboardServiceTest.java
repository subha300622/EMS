package com.example.ems.dashboard;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.dto.teamleader.*;
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
public class TeamLeaderDashboardServiceTest {

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

    @Mock
    private AppraisalRepository appraisalRepository;

    @InjectMocks
    private TeamLeaderDashboardServiceImpl service;

    private Employee tlEmployee;
    private Team team1;
    private List<Employee> directReports;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tl@company.com", "password", Collections.emptyList())
        );

        tlEmployee = new Employee();
        tlEmployee.setId(10L);
        tlEmployee.setEmail("tl@company.com");
        tlEmployee.setFullName("Terry Lead");

        team1 = new Team();
        team1.setId(100L);
        team1.setTeamName("Core Team");
        team1.setTeamLead(tlEmployee);

        directReports = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Employee emp = new Employee();
            emp.setId((long) (100 + i));
            emp.setEmployeeId("EMP-00" + i);
            emp.setFullName("Developer " + i);
            emp.setEmail("dev" + i + "@company.com");
            emp.setDepartment("Engineering");
            emp.setDesignation("Software Engineer");
            emp.setStatus("ACTIVE");
            directReports.add(emp);
        }
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetDashboard_SuccessWithDirectReports() {
        when(employeeRepository.findByEmailAndOrganizationId("tl@company.com", 1L))
                .thenReturn(Optional.of(tlEmployee));
        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(10L, 1L))
                .thenReturn(List.of(team1));

        List<TeamMember> teamMembers = directReports.stream()
                .map(e -> new TeamMember(team1, e, LocalDate.now(), false))
                .toList();
        when(teamMemberRepository.findByTeamIdInAndStatus(List.of(100L), "ACTIVE"))
                .thenReturn(teamMembers);

        // 4 present out of 5
        List<Attendance> attendances = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Attendance att = new Attendance();
            att.setEmployee(directReports.get(i));
            att.setStatus(AttendanceStatus.PRESENT);
            attendances.add(att);
        }
        when(attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(anyList(), any(LocalDate.class), eq(1L)))
                .thenReturn(attendances);

        // 2 pending leaves
        Leave l1 = new Leave();
        l1.setId(501L);
        l1.setEmployee(directReports.get(0));
        l1.setStatus("PENDING_TL_RECOMMENDATION");
        Leave l2 = new Leave();
        l2.setId(502L);
        l2.setEmployee(directReports.get(1));
        l2.setStatus("PENDING");
        when(leaveRepository.findByEmployeeIdInAndStatusIn(anyList(), anyList()))
                .thenReturn(List.of(l1, l2));

        // Performance appraisal: average 4.5
        for (Employee emp : directReports) {
            Appraisal a = new Appraisal();
            a.setFinalRating(4.5);
            when(appraisalRepository.findByOrganizationIdAndEmployeeId(1L, emp.getId()))
                    .thenReturn(List.of(a));
        }

        TeamLeaderDashboardResponse response = service.getDashboard();

        assertNotNull(response);
        assertEquals(5, response.getSummary().getTeamMembers());
        assertEquals(5, response.getSummary().getActiveDirectReports());
        assertEquals(4, response.getSummary().getPresentToday());
        assertEquals(80, response.getSummary().getAttendancePercentage());
        assertEquals(2, response.getSummary().getPendingLeaveReviews());
        assertEquals(4.5, response.getSummary().getTeamPerformance().getRating());
        assertEquals(5.0, response.getSummary().getTeamPerformance().getMaxRating());
        assertEquals(5, response.getTeamRoster().size());
        assertEquals(2, response.getPendingLeaveReviews().size());
    }

    @Test
    void testGetDashboard_ZeroStateWhenNoDirectReports() {
        when(employeeRepository.findByEmailAndOrganizationId("tl@company.com", 1L))
                .thenReturn(Optional.of(tlEmployee));
        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(10L, 1L))
                .thenReturn(Collections.emptyList());
        when(teamRepository.findByTeamLeadIdAndDeletedFalse(10L))
                .thenReturn(Collections.emptyList());

        TeamLeaderDashboardResponse response = service.getDashboard();

        assertNotNull(response);
        assertEquals(0, response.getSummary().getTeamMembers());
        assertEquals(0, response.getSummary().getPresentToday());
        assertEquals(0, response.getSummary().getAttendancePercentage());
        assertEquals(0, response.getSummary().getPendingLeaveReviews());
        assertTrue(response.getTeamRoster().isEmpty());
        assertTrue(response.getPendingLeaveReviews().isEmpty());
    }

    @Test
    void testGetAttendanceToday_CorrectStatusBreakdown() {
        when(employeeRepository.findByEmailAndOrganizationId("tl@company.com", 1L))
                .thenReturn(Optional.of(tlEmployee));
        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(10L, 1L))
                .thenReturn(List.of(team1));

        List<TeamMember> teamMembers = directReports.stream()
                .map(e -> new TeamMember(team1, e, LocalDate.now(), false))
                .toList();
        when(teamMemberRepository.findByTeamIdInAndStatus(List.of(100L), "ACTIVE"))
                .thenReturn(teamMembers);

        // 1: PRESENT, 2: PRESENT, 3: REMOTE, 4: LEAVE, 5: ABSENT
        Attendance a1 = new Attendance();
        a1.setEmployee(directReports.get(0));
        a1.setStatus(AttendanceStatus.PRESENT);

        Attendance a2 = new Attendance();
        a2.setEmployee(directReports.get(1));
        a2.setStatus(AttendanceStatus.PRESENT);

        Attendance a3 = new Attendance();
        a3.setEmployee(directReports.get(2));
        a3.setStatus(AttendanceStatus.PRESENT);
        a3.setLocation("REMOTE");

        Attendance a4 = new Attendance();
        a4.setEmployee(directReports.get(3));
        a4.setStatus(AttendanceStatus.LEAVE);

        Attendance a5 = new Attendance();
        a5.setEmployee(directReports.get(4));
        a5.setStatus(AttendanceStatus.ABSENT);

        when(attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(anyList(), any(LocalDate.class), eq(1L)))
                .thenReturn(List.of(a1, a2, a3, a4, a5));

        TeamLeaderAttendanceTodayResponse response = service.getAttendanceToday();

        assertNotNull(response);
        assertEquals(5, response.getTotalMembers());
        assertEquals(2, response.getPresent());
        assertEquals(1, response.getRemote());
        assertEquals(1, response.getOnLeave());
        assertEquals(1, response.getAbsent());
        assertEquals(40, response.getAttendancePercentage());
    }

    @Test
    void testGetTeamMemberById_SuccessAndForbiddenIDOR() {
        when(employeeRepository.findByEmailAndOrganizationId("tl@company.com", 1L))
                .thenReturn(Optional.of(tlEmployee));
        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(10L, 1L))
                .thenReturn(List.of(team1));

        List<TeamMember> teamMembers = directReports.stream()
                .map(e -> new TeamMember(team1, e, LocalDate.now(), false))
                .toList();
        when(teamMemberRepository.findByTeamIdInAndStatus(List.of(100L), "ACTIVE"))
                .thenReturn(teamMembers);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(101L, LocalDate.now(), 1L))
                .thenReturn(Optional.empty());

        // Valid member
        TeamMemberRosterDto member = service.getTeamMemberById(101L);
        assertNotNull(member);
        assertEquals(101L, member.getId());

        // IDOR: Requesting an employee not in TL's team (e.g. 999L) -> 403 Forbidden
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.getTeamMemberById(999L));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void testRecommendLeave_SuccessRecommendAndNotRecommend() {
        when(employeeRepository.findByEmailAndOrganizationId("tl@company.com", 1L))
                .thenReturn(Optional.of(tlEmployee));
        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(10L, 1L))
                .thenReturn(List.of(team1));

        List<TeamMember> teamMembers = directReports.stream()
                .map(e -> new TeamMember(team1, e, LocalDate.now(), false))
                .toList();
        when(teamMemberRepository.findByTeamIdInAndStatus(List.of(100L), "ACTIVE"))
                .thenReturn(teamMembers);

        Leave pendingLeave = new Leave();
        pendingLeave.setId(501L);
        pendingLeave.setEmployee(directReports.get(0));
        pendingLeave.setStatus("PENDING_TL_RECOMMENDATION");
        pendingLeave.setOrganization(new com.example.ems.organization.entity.Organization());
        pendingLeave.getOrganization().setId(1L);

        when(leaveRepository.findById(501L)).thenReturn(Optional.of(pendingLeave));

        // RECOMMEND
        TeamLeaveRecommendationRequest req1 = new TeamLeaveRecommendationRequest("RECOMMEND", "Team permits");
        service.recommendLeave(501L, req1);
        assertEquals("TL_RECOMMENDED", pendingLeave.getStatus());
        assertEquals("Team permits", pendingLeave.getManagerComment());

        // NOT_RECOMMEND
        pendingLeave.setStatus("PENDING_TL_RECOMMENDATION");
        TeamLeaveRecommendationRequest req2 = new TeamLeaveRecommendationRequest("NOT_RECOMMEND", "Critical sprint delivery");
        service.recommendLeave(501L, req2);
        assertEquals("TL_NOT_RECOMMENDED", pendingLeave.getStatus());
        assertEquals("Critical sprint delivery", pendingLeave.getManagerComment());
    }

    @Test
    void testRecommendLeave_ValidationAndConflictErrors() {
        // Invalid decision (e.g. "APPROVE") -> 400 Bad Request
        TeamLeaveRecommendationRequest invalidReq = new TeamLeaveRecommendationRequest("APPROVE", "Approved");
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class, () ->
                service.recommendLeave(501L, invalidReq));
        assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatusCode());

        when(employeeRepository.findByEmailAndOrganizationId("tl@company.com", 1L))
                .thenReturn(Optional.of(tlEmployee));
        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(10L, 1L))
                .thenReturn(List.of(team1));

        List<TeamMember> teamMembers = directReports.stream()
                .map(e -> new TeamMember(team1, e, LocalDate.now(), false))
                .toList();
        when(teamMemberRepository.findByTeamIdInAndStatus(List.of(100L), "ACTIVE"))
                .thenReturn(teamMembers);

        // Already processed leave -> 409 Conflict
        Leave processedLeave = new Leave();
        processedLeave.setId(501L);
        processedLeave.setEmployee(directReports.get(0));
        processedLeave.setStatus("TL_RECOMMENDED");
        processedLeave.setOrganization(new com.example.ems.organization.entity.Organization());
        processedLeave.getOrganization().setId(1L);

        when(leaveRepository.findById(501L)).thenReturn(Optional.of(processedLeave));

        TeamLeaveRecommendationRequest req = new TeamLeaveRecommendationRequest("RECOMMEND", "Permits");
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () ->
                service.recommendLeave(501L, req));
        assertEquals(HttpStatus.CONFLICT, ex2.getStatusCode());

        // Cross-team leave -> 403 Forbidden
        Employee outsideEmp = new Employee();
        outsideEmp.setId(999L);
        Leave outsideLeave = new Leave();
        outsideLeave.setId(502L);
        outsideLeave.setEmployee(outsideEmp);
        outsideLeave.setStatus("PENDING_TL_RECOMMENDATION");
        outsideLeave.setOrganization(new com.example.ems.organization.entity.Organization());
        outsideLeave.getOrganization().setId(1L);

        when(leaveRepository.findById(502L)).thenReturn(Optional.of(outsideLeave));

        ResponseStatusException ex3 = assertThrows(ResponseStatusException.class, () ->
                service.recommendLeave(502L, req));
        assertEquals(HttpStatus.FORBIDDEN, ex3.getStatusCode());
    }

    @Test
    void testGetPerformance_CalculatesAverageCorrectly() {
        when(employeeRepository.findByEmailAndOrganizationId("tl@company.com", 1L))
                .thenReturn(Optional.of(tlEmployee));
        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(10L, 1L))
                .thenReturn(List.of(team1));

        List<TeamMember> teamMembers = directReports.stream()
                .map(e -> new TeamMember(team1, e, LocalDate.now(), false))
                .toList();
        when(teamMemberRepository.findByTeamIdInAndStatus(List.of(100L), "ACTIVE"))
                .thenReturn(teamMembers);

        // Ratings: 4.0, 5.0, 4.5, 4.5, 4.5 -> avg 4.5
        double[] ratings = {4.0, 5.0, 4.5, 4.5, 4.5};
        for (int i = 0; i < 5; i++) {
            Appraisal a = new Appraisal();
            a.setFinalRating(ratings[i]);
            when(appraisalRepository.findByOrganizationIdAndEmployeeId(1L, directReports.get(i).getId()))
                    .thenReturn(List.of(a));
        }

        TeamLeaderPerformanceResponse perf = service.getPerformance();

        assertNotNull(perf);
        assertEquals(4.5, perf.getRating());
        assertEquals(5.0, perf.getMaxRating());
        assertEquals(4.5, perf.getSprintDeliveryIndex());
        assertEquals(5, perf.getMemberRatings().size());
    }
}
