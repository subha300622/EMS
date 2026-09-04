package com.example.ems.employee.service;

import com.example.ems.auth.entity.User;
import com.example.ems.employee.dto.TeamDtos;
import com.example.ems.employee.entity.*;
import com.example.ems.employee.repository.*;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private TeamAuditLogRepository teamAuditLogRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private TeamService teamService;

    private User user;
    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setId(1L);
        organization.setName("Test Org");

        user = new User();
        user.setId(10L);
        user.setWorkEmail("admin@test.com");
        user.setOrganization(organization);
        user.setOrganizationId(1L);
    }

    @Test
    void testCreateTeam_Success() {
        TeamDtos.TeamCreateRequest req = new TeamDtos.TeamCreateRequest();
        req.setTeamName("Backend Team");
        req.setTeamCode("BACKEND");
        req.setDescription("Backend dev team");

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(teamRepository.existsByTeamNameAndOrganizationIdAndDeletedFalse("Backend Team", 1L)).thenReturn(false);
        when(teamRepository.existsByTeamCodeAndOrganizationIdAndDeletedFalse("BACKEND", 1L)).thenReturn(false);

        Team savedTeam = new Team();
        savedTeam.setId(101L);
        savedTeam.setTeamName("Backend Team");
        savedTeam.setTeamCode("BACKEND");
        savedTeam.setOrganization(organization);
        savedTeam.setStatus("ACTIVE");

        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);

        TeamDtos.TeamResponseDto result = teamService.createTeam(req, user);

        assertNotNull(result);
        assertEquals(101L, result.getTeamId());
        assertEquals("Backend Team", result.getTeamName());
        assertEquals("BACKEND", result.getTeamCode());
        verify(teamAuditLogRepository, times(1)).save(any(TeamAuditLog.class));
    }

    @Test
    void testCreateTeam_DuplicateName_ThrowsException() {
        TeamDtos.TeamCreateRequest req = new TeamDtos.TeamCreateRequest();
        req.setTeamName("Backend Team");
        req.setTeamCode("BACKEND");

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(teamRepository.existsByTeamNameAndOrganizationIdAndDeletedFalse("Backend Team", 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> teamService.createTeam(req, user));
    }

    @Test
    void testDeleteTeam_WithActiveMembers_ThrowsActiveMembersExistException() {
        Team team = new Team();
        team.setId(101L);
        team.setOrganization(organization);

        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(101L, 1L)).thenReturn(Optional.of(team));
        when(teamMemberRepository.countByTeamIdAndStatus(101L, "ACTIVE")).thenReturn(2L);

        TeamService.ActiveMembersExistException ex = assertThrows(
                TeamService.ActiveMembersExistException.class,
                () -> teamService.deleteTeam(101L, user)
        );

        assertEquals("TEAM_HAS_ACTIVE_MEMBERS", ex.getCode());
    }

    @Test
    void testAssignDepartment_Mismatch_ThrowsDepartmentMismatchException() {
        Team team = new Team();
        team.setId(101L);
        team.setOrganization(organization);

        Department targetDept = new Department(10L, "Engineering", "ENG", "Eng Dept");
        targetDept.setStatus("ACTIVE");

        Employee employee = new Employee();
        employee.setId(153L);
        employee.setFullName("Jane Smith");
        employee.setDepartment("Sales"); // Incompatible department!

        TeamMember member = new TeamMember(team, employee, LocalDate.now(), false);

        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(101L, 1L)).thenReturn(Optional.of(team));
        when(departmentRepository.findByIdAndOrganizationId(10L, 1L)).thenReturn(Optional.of(targetDept));
        when(teamMemberRepository.findByTeamIdAndStatus(101L, "ACTIVE")).thenReturn(List.of(member));

        TeamService.DepartmentMismatchException ex = assertThrows(
                TeamService.DepartmentMismatchException.class,
                () -> teamService.assignDepartment(101L, 10L, user)
        );

        assertEquals("TEAM_MEMBER_DEPARTMENT_MISMATCH", ex.getCode());
        assertFalse(ex.getDetails().isEmpty());
    }

    @Test
    void testBulkAddMembers_PartialSuccess() {
        Team team = new Team();
        team.setId(101L);
        team.setOrganization(organization);
        team.setStatus("ACTIVE");

        Employee emp1 = new Employee();
        emp1.setId(150L);
        emp1.setStatus("ACTIVE");
        emp1.setOrganization(organization);

        Employee emp2 = new Employee();
        emp2.setId(151L);
        emp2.setStatus("INACTIVE"); // Inactive employee!
        emp2.setOrganization(organization);

        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(101L, 1L)).thenReturn(Optional.of(team));
        when(employeeRepository.findByIdAndOrganizationId(150L, 1L)).thenReturn(Optional.of(emp1));
        when(employeeRepository.findByIdAndOrganizationId(151L, 1L)).thenReturn(Optional.of(emp2));
        when(teamMemberRepository.existsByTeamIdAndEmployeeIdAndStatus(101L, 150L, "ACTIVE")).thenReturn(false);

        TeamDtos.TeamMemberBulkAddResponse response = teamService.bulkAddMembers(101L, List.of(150L, 151L), user);

        assertNotNull(response);
        assertEquals(1, response.getSuccessCount());
        assertEquals(1, response.getFailedCount());
        assertEquals(2, response.getResults().size());
        assertEquals("ADDED", response.getResults().get(0).getStatus());
        assertEquals("FAILED", response.getResults().get(1).getStatus());
        assertEquals("EMPLOYEE_INACTIVE", response.getResults().get(1).getReason());
    }
}
