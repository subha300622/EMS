package com.example.ems.dashboard;

import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.dto.teamleader.TeamLeaveRecommendationRequest;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.Team;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.TeamMemberRepository;
import com.example.ems.employee.repository.TeamRepository;
import com.example.ems.employee.service.actioncenter.EmployeeActionCenterService;
import com.example.ems.employee.service.impl.EmployeeDashboardServiceImpl;
import com.example.ems.employee.service.impl.TeamLeaderDashboardServiceImpl;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveStatus;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.payroll.repository.EmployeeSalaryAssignmentRepository;
import com.example.ems.payroll.repository.PayrollRepository;
import com.example.ems.performance.repository.PerformanceReviewRecordRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DashboardTenantIsolationTest {

    private static final Long TENANT_A_ORG_ID = 100L;
    private static final Long TENANT_B_ORG_ID = 200L;

    // Team Leader Mocks
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private LeaveRepository leaveRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;
    @Mock
    private EmployeeSalaryAssignmentRepository salaryAssignmentRepository;
    @Mock
    private PayrollRepository payrollRepository;
    @Mock
    private PerformanceReviewRecordRepository performanceRecordRepository;
    @Mock
    private EmployeeActionCenterService actionCenterService;
    @Mock
    private LeaveService leaveService;

    @InjectMocks
    private TeamLeaderDashboardServiceImpl teamLeaderDashboardService;

    @InjectMocks
    private EmployeeDashboardServiceImpl employeeDashboardService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@company.com", "N/A", Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Tenant A Team Leader querying team members only fetches teams scoped to Tenant A org ID")
    void teamLeaderTeamMembers_strictlyScopedToTenantA() {
        TenantContext.setCurrentTenant(TENANT_A_ORG_ID);
        Long teamLeadId = 10L;

        Employee tl = new Employee();
        tl.setId(teamLeadId);
        tl.setEmail("user@company.com");
        Organization orgA = new Organization();
        orgA.setId(TENANT_A_ORG_ID);
        tl.setOrganization(orgA);

        when(employeeRepository.findByEmailAndOrganizationId("user@company.com", TENANT_A_ORG_ID))
                .thenReturn(Optional.of(tl));

        // Team Leader belongs to Tenant A
        Team teamA = new Team();
        teamA.setId(1L);
        teamA.setTeamName("Team Alpha");
        teamA.setOrganization(orgA);
        teamA.setTeamLead(tl);

        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(teamLeadId, TENANT_A_ORG_ID))
                .thenReturn(List.of(teamA));
        when(teamMemberRepository.findByTeamIdInAndStatus(List.of(1L), "ACTIVE"))
                .thenReturn(Collections.emptyList());

        var roster = teamLeaderDashboardService.getTeamMembers();

        assertNotNull(roster);
        verify(teamRepository).findByTeamLeadIdAndOrganizationIdAndDeletedFalse(teamLeadId, TENANT_A_ORG_ID);
        // Ensure no cross-tenant query for TENANT_B_ORG_ID is made
        verify(teamRepository, never()).findByTeamLeadIdAndOrganizationIdAndDeletedFalse(teamLeadId, TENANT_B_ORG_ID);
    }

    @Test
    @DisplayName("Tenant A Team Leader accessing Tenant B employee throws ResourceNotFound or AccessDenied")
    void teamLeaderCannotAccessTenantBEmployee() {
        TenantContext.setCurrentTenant(TENANT_A_ORG_ID);
        Long teamLeadId = 10L;
        Long tenantBEmployeeId = 999L;

        Employee tl = new Employee();
        tl.setId(teamLeadId);
        tl.setEmail("user@company.com");
        Organization orgA = new Organization();
        orgA.setId(TENANT_A_ORG_ID);
        tl.setOrganization(orgA);

        when(employeeRepository.findByEmailAndOrganizationId("user@company.com", TENANT_A_ORG_ID))
                .thenReturn(Optional.of(tl));

        Team teamA = new Team();
        teamA.setId(1L);
        teamA.setOrganization(orgA);
        teamA.setTeamLead(tl);

        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(teamLeadId, TENANT_A_ORG_ID))
                .thenReturn(List.of(teamA));
        // Employee 999 is NOT in Team A
        when(teamMemberRepository.existsByTeamIdInAndEmployeeIdAndStatus(
                List.of(1L), tenantBEmployeeId, "ACTIVE"
        )).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                teamLeaderDashboardService.getTeamMemberById(tenantBEmployeeId)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    @DisplayName("Tenant A Team Leader cannot recommend leave for Tenant B employee - state remains unchanged")
    void teamLeaderCannotRecommendLeaveForTenantBEmployee() {
        TenantContext.setCurrentTenant(TENANT_A_ORG_ID);
        Long teamLeadId = 10L;
        Long tenantBLeaveId = 555L;
        Long tenantBEmployeeId = 888L;

        Employee tl = new Employee();
        tl.setId(teamLeadId);
        tl.setEmail("user@company.com");
        Organization orgA = new Organization();
        orgA.setId(TENANT_A_ORG_ID);
        tl.setOrganization(orgA);

        when(employeeRepository.findByEmailAndOrganizationId("user@company.com", TENANT_A_ORG_ID))
                .thenReturn(Optional.of(tl));

        Employee empB = new Employee();
        empB.setId(tenantBEmployeeId);
        Organization orgB = new Organization();
        orgB.setId(TENANT_B_ORG_ID);
        empB.setOrganization(orgB);

        Leave tenantBLeave = new Leave();
        tenantBLeave.setId(tenantBLeaveId);
        tenantBLeave.setEmployee(empB);
        tenantBLeave.setOrganization(orgB); // Different tenant
        tenantBLeave.setStatus(LeaveStatus.PENDING);

        when(leaveRepository.findById(tenantBLeaveId)).thenReturn(Optional.of(tenantBLeave));

        Team teamA = new Team();
        teamA.setId(1L);
        teamA.setOrganization(orgA);
        teamA.setTeamLead(tl);

        when(teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(teamLeadId, TENANT_A_ORG_ID))
                .thenReturn(List.of(teamA));
        // Employee 888 does NOT belong to Team A
        when(teamMemberRepository.existsByTeamIdInAndEmployeeIdAndStatus(
                List.of(1L), tenantBEmployeeId, "ACTIVE"
        )).thenReturn(false);

        TeamLeaveRecommendationRequest request = new TeamLeaveRecommendationRequest();
        request.setDecision("RECOMMEND");
        request.setComment("Cross-tenant attempt");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                teamLeaderDashboardService.recommendLeave(tenantBLeaveId, request)
        );
        assertTrue(ex.getStatusCode() == HttpStatus.FORBIDDEN || ex.getStatusCode() == HttpStatus.NOT_FOUND);

        // Invariant: Leave status must NOT be modified
        assertEquals(LeaveStatus.PENDING, tenantBLeave.getStatus());
        verify(leaveRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tenant A Employee cannot view Tenant B Employee Dashboard data")
    void employeeDashboard_crossTenantAccessDenied() {
        TenantContext.setCurrentTenant(TENANT_A_ORG_ID);

        // User belongs to Tenant B, trying to access Tenant A
        when(employeeRepository.findByEmail("user@company.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                employeeDashboardService.getDashboard()
        );
    }

    @Test
    @DisplayName("Tenant A Employee cannot access Tenant B leave by ID")
    void employeeLeave_crossTenantAccessDenied() {
        TenantContext.setCurrentTenant(TENANT_A_ORG_ID);
        Long tenantBLeaveId = 777L;

        Employee empA = new Employee();
        empA.setId(101L);
        empA.setEmail("user@company.com");
        Organization orgA = new Organization();
        orgA.setId(TENANT_A_ORG_ID);
        empA.setOrganization(orgA);

        when(employeeRepository.findByEmail("user@company.com")).thenReturn(Optional.of(empA));

        Employee empB = new Employee();
        empB.setId(202L);
        Organization orgB = new Organization();
        orgB.setId(TENANT_B_ORG_ID);
        empB.setOrganization(orgB);

        Leave leaveB = new Leave();
        leaveB.setId(tenantBLeaveId);
        leaveB.setEmployee(empB);
        leaveB.setOrganization(orgB);

        when(leaveService.getLeaveById(tenantBLeaveId)).thenReturn(Optional.of(leaveB));

        assertThrows(AccessDeniedException.class, () ->
                employeeDashboardService.getMyLeaveById(tenantBLeaveId)
        );
    }
}
