package com.example.ems.employee;

import com.example.ems.auth.entity.User;
import com.example.ems.employee.dto.TeamDtos;
import com.example.ems.employee.entity.*;
import com.example.ems.employee.repository.*;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeAndTeamCrossTenantSecurityTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private TeamService teamService;

    private Organization orgA;
    private Organization orgB;
    private User userOrgA;

    @BeforeEach
    void setUp() {
        orgA = new Organization();
        orgA.setId(1001L);
        orgA.setName("Organization A");

        orgB = new Organization();
        orgB.setId(1002L);
        orgB.setName("Organization B");

        userOrgA = new User();
        userOrgA.setId(10L);
        userOrgA.setWorkEmail("userA@orgA.com");
        userOrgA.setOrganization(orgA);
        userOrgA.setOrganizationId(1001L);

        TenantContext.setCurrentTenant(1001L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testGetTeam_CrossTenantAccess_Fails() {
        // Team 200 belongs to Org B (1002L)
        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(200L, 1001L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> teamService.getTeam(200L, userOrgA));
    }

    @Test
    void testCreateTeam_CrossTenantDepartment_Fails() {
        TeamDtos.TeamCreateRequest req = new TeamDtos.TeamCreateRequest();
        req.setTeamName("Security Team");
        req.setTeamCode("SEC");
        req.setDepartmentId(999L); // Belongs to Org B

        when(organizationRepository.findById(1001L)).thenReturn(Optional.of(orgA));
        when(teamRepository.existsByTeamNameAndOrganizationIdAndDeletedFalse("Security Team", 1001L)).thenReturn(false);
        when(teamRepository.existsByTeamCodeAndOrganizationIdAndDeletedFalse("SEC", 1001L)).thenReturn(false);
        when(departmentRepository.findByIdAndOrganizationId(999L, 1001L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> teamService.createTeam(req, userOrgA));
    }

    @Test
    void testAddMember_CrossTenantEmployee_Fails() {
        Team teamOrgA = new Team();
        teamOrgA.setId(50L);
        teamOrgA.setOrganization(orgA);
        teamOrgA.setStatus("ACTIVE");

        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(50L, 1001L)).thenReturn(Optional.of(teamOrgA));
        when(employeeRepository.findByIdAndOrganizationId(888L, 1001L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> teamService.addMember(50L, 888L, userOrgA));
    }

    @Test
    void testAssignDepartment_CrossTenantDepartment_Fails() {
        Team teamOrgA = new Team();
        teamOrgA.setId(50L);
        teamOrgA.setOrganization(orgA);

        when(teamRepository.findByIdAndOrganizationIdAndDeletedFalse(50L, 1001L)).thenReturn(Optional.of(teamOrgA));
        when(departmentRepository.findByIdAndOrganizationId(777L, 1001L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> teamService.assignDepartment(50L, 777L, userOrgA));
    }
}
