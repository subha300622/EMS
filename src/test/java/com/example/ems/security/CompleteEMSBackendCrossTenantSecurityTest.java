package com.example.ems.security;

import com.example.ems.approval.repository.ApprovalTaskRepository;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.User;
import com.example.ems.employee.dto.TeamDtos;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.Team;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.TeamMemberRepository;
import com.example.ems.employee.repository.TeamRepository;
import com.example.ems.employee.service.TeamService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CompleteEMSBackendCrossTenantSecurityTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ApprovalTaskRepository approvalTaskRepository;

    @InjectMocks
    private TeamService teamService;

    @InjectMocks
    private ApprovalWorkflowEngineService approvalWorkflowEngineService;

    private Organization orgA;
    private Organization orgB;
    private User userOrgA;

    @BeforeEach
    void setUp() {
        orgA = new Organization();
        orgA.setId(9631L);
        orgA.setName("Organization A (9631)");

        orgB = new Organization();
        orgB.setId(4567L);
        orgB.setName("Organization B (4567)");

        userOrgA = new User();
        userOrgA.setId(101L);
        userOrgA.setWorkEmail("userA@orga.com");
        userOrgA.setOrganization(orgA);
        userOrgA.setOrganizationId(9631L);

        TenantContext.setCurrentTenant(9631L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testListTeams_ReturnsOnlySameOrgRecords() {
        Team teamOrgA = new Team();
        teamOrgA.setId(100L);
        teamOrgA.setTeamName("Org A Team");
        teamOrgA.setOrganization(orgA);

        when(teamRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.jpa.domain.Specification.class), org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(teamOrgA)));

        org.springframework.data.domain.Page<TeamDtos.TeamResponseDto> page = teamService.listTeams(null, null, null, 0, 10, userOrgA);
        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        assertEquals("Org A Team", page.getContent().get(0).getTeamName());
    }

    @Test
    void testOrgAReadsOrgAResource_Success() {
        Team team = new Team();
        team.setId(100L);
        team.setTeamName("Core Team");
        team.setOrganization(orgA);

        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(100L, 9631L))
                .thenReturn(Optional.of(team));
        when(teamMemberRepository.countByTeamIdAndStatus(100L, "ACTIVE")).thenReturn(0L);

        TeamDtos.TeamResponseDto result = teamService.getTeam(100L, userOrgA);
        assertNotNull(result);
        assertEquals("Core Team", result.getTeamName());
    }

    @Test
    void testOrgAReadsOrgBResource_ThrowsNotFoundOrForbidden() {
        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(200L, 9631L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> teamService.getTeam(200L, userOrgA));
    }

    @Test
    void testOrgAUpdatesOrgBResource_ThrowsNotFoundOrForbidden() {
        TeamDtos.TeamUpdateRequest updateReq = new TeamDtos.TeamUpdateRequest();
        updateReq.setTeamName("Hacked Team");

        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(200L, 9631L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> teamService.updateTeam(200L, updateReq, userOrgA));
    }

    @Test
    void testOrgADeletesOrgBResource_ThrowsNotFoundOrForbidden() {
        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(200L, 9631L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> teamService.deleteTeam(200L, userOrgA));
    }

    @Test
    void testOrgACreatesResourceReferencingOrgBDepartment_Fails() {
        TeamDtos.TeamCreateRequest createReq = new TeamDtos.TeamCreateRequest();
        createReq.setTeamName("Org A Team");
        createReq.setTeamCode("ORGA");
        createReq.setDepartmentId(999L); // Dept 999 belongs to Org B

        when(organizationRepository.findById(9631L)).thenReturn(Optional.of(orgA));
        when(teamRepository.existsByTeamNameAndOrganizationIdAndDeletedFalse("Org A Team", 9631L)).thenReturn(false);
        when(teamRepository.existsByTeamCodeAndOrganizationIdAndDeletedFalse("ORGA", 9631L)).thenReturn(false);
        when(departmentRepository.findByIdAndOrganizationId(999L, 9631L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> teamService.createTeam(createReq, userOrgA));
    }

    @Test
    void testOrgAApprovesOrgBApprovalTask_Fails() {
        Employee emp = new Employee();
        emp.setId(101L);
        emp.setEmail("userA@orga.com");
        emp.setOrganization(orgA);

        when(employeeRepository.findByEmail("userA@orga.com")).thenReturn(Optional.of(emp));
        when(approvalTaskRepository.findByApprovalTaskId("500L")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> approvalWorkflowEngineService.approveTask(userOrgA, "500L", "Approved"));
    }
}
