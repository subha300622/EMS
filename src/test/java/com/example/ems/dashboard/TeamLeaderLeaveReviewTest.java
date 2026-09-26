package com.example.ems.dashboard;

import com.example.ems.employee.dto.teamleader.TeamLeaveRecommendationRequest;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TeamLeaderLeaveReviewTest {

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private TeamLeaderDashboardServiceImpl service;

    private Employee tl;
    private Employee member;
    private Team team;
    private Leave leave;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tl@company.com", "password", Collections.emptyList())
        );

        tl = new Employee();
        tl.setId(10L);
        tl.setEmail("tl@company.com");

        member = new Employee();
        member.setId(101L);
        member.setEmail("member@company.com");

        team = new Team();
        team.setId(500L);
        team.setTeamLead(tl);

        leave = new Leave();
        leave.setId(901L);
        leave.setEmployee(member);
        leave.setStatus("PENDING_TL_RECOMMENDATION");
        leave.setOrganization(new com.example.ems.organization.entity.Organization());
        leave.getOrganization().setId(1L);

        when(employeeRepository.findByEmailAndOrganizationId("tl@company.com", 1L))
                .thenReturn(Optional.of(tl));
        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(10L, 1L))
                .thenReturn(List.of(team));
        when(teamMemberRepository.findByTeamIdInAndStatus(List.of(500L), "ACTIVE"))
                .thenReturn(List.of(new TeamMember(team, member, LocalDate.now(), false)));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testLeaveReview_ValidDecisionRecommend() {
        when(leaveRepository.findById(901L)).thenReturn(Optional.of(leave));

        TeamLeaveRecommendationRequest req = new TeamLeaveRecommendationRequest("RECOMMEND", "Workload is manageable.");
        service.recommendLeave(901L, req);

        assertEquals("TL_RECOMMENDED", leave.getStatus());
        assertEquals("Workload is manageable.", leave.getManagerComment());
        verify(leaveRepository, times(1)).save(leave);
    }

    @Test
    void testLeaveReview_ValidDecisionNotRecommend() {
        when(leaveRepository.findById(901L)).thenReturn(Optional.of(leave));

        TeamLeaveRecommendationRequest req = new TeamLeaveRecommendationRequest("NOT_RECOMMEND", "Sprint delivery deadline.");
        service.recommendLeave(901L, req);

        assertEquals("TL_NOT_RECOMMENDED", leave.getStatus());
        assertEquals("Sprint delivery deadline.", leave.getManagerComment());
        verify(leaveRepository, times(1)).save(leave);
    }

    @Test
    void testLeaveReview_InvalidDecisionThrows400() {
        TeamLeaveRecommendationRequest req = new TeamLeaveRecommendationRequest("APPROVE", "Approved directly");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.recommendLeave(901L, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Invalid decision"));
        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void testLeaveReview_DuplicateRecommendationThrows409Conflict() {
        // Leave is already recommended
        leave.setStatus("TL_RECOMMENDED");
        when(leaveRepository.findById(901L)).thenReturn(Optional.of(leave));

        TeamLeaveRecommendationRequest req = new TeamLeaveRecommendationRequest("RECOMMEND", "Second recommendation");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.recommendLeave(901L, req));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("already been processed"));
        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void testLeaveReview_LeaveNotFoundThrows404() {
        when(leaveRepository.findById(9999L)).thenReturn(Optional.empty());

        TeamLeaveRecommendationRequest req = new TeamLeaveRecommendationRequest("RECOMMEND", "Valid comment");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.recommendLeave(9999L, req));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
