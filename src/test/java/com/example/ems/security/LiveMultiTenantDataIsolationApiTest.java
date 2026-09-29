package com.example.ems.security;

import com.example.ems.employee.dto.TeamDtos;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Team;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.TeamRepository;
import com.example.ems.employee.service.TeamService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.auth.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LiveMultiTenantDataIsolationApiTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamService teamService;

    private Organization orgA;
    private Organization orgB;
    private User userOrgA;
    private User userOrgB;
    private Team teamA;
    private Team teamB;
    private Department deptA;
    private Department deptB;

    @BeforeEach
    void setUp() {
        // 1. Create 2 distinct Organizations (Org A & Org B)
        orgA = new Organization();
        orgA.setName("Organization Alpha");
        orgA.setOrganizationCode("ORGA-" + System.currentTimeMillis());
        orgA.setStatus(OrganizationStatus.ACTIVE);
        orgA = organizationRepository.save(orgA);

        orgB = new Organization();
        orgB.setName("Organization Beta");
        orgB.setOrganizationCode("ORGB-" + System.currentTimeMillis());
        orgB.setStatus(OrganizationStatus.ACTIVE);
        orgB = organizationRepository.save(orgB);

        // 2. Create Departments for each Org
        deptA = new Department();
        deptA.setName("Org A Engineering");
        deptA.setCode("ENG-A");
        deptA.setOrganization(orgA);
        deptA.setStatus("ACTIVE");
        deptA = departmentRepository.save(deptA);

        deptB = new Department();
        deptB.setName("Org B Marketing");
        deptB.setCode("MKT-B");
        deptB.setOrganization(orgB);
        deptB.setStatus("ACTIVE");
        deptB = departmentRepository.save(deptB);

        // 3. Create Teams for each Org
        teamA = new Team();
        teamA.setTeamName("Alpha Devs");
        teamA.setTeamCode("ALPHA-DEV");
        teamA.setOrganization(orgA);
        teamA.setDepartment(deptA);
        teamA.setStatus("ACTIVE");
        teamA = teamRepository.save(teamA);

        teamB = new Team();
        teamB.setTeamName("Beta Marketers");
        teamB.setTeamCode("BETA-MKT");
        teamB.setOrganization(orgB);
        teamB.setDepartment(deptB);
        teamB.setStatus("ACTIVE");
        teamB = teamRepository.save(teamB);

        // 4. Create Users bound to Org A and Org B
        userOrgA = new User();
        userOrgA.setId(9001L);
        userOrgA.setWorkEmail("user@alpha.com");
        userOrgA.setOrganization(orgA);
        userOrgA.setOrganizationId(orgA.getId());

        userOrgB = new User();
        userOrgB.setId(9002L);
        userOrgB.setWorkEmail("user@beta.com");
        userOrgB.setOrganization(orgB);
        userOrgB.setOrganizationId(orgB.getId());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testDataIsolation_ListTeams_ReturnsOnlyRespectiveOrgData() {
        // Authenticate context for Org A
        TenantContext.setCurrentTenant(orgA.getId());
        List<Team> teamsA = teamRepository.findByOrganizationIdAndDeletedFalse(orgA.getId());
        assertEquals(1, teamsA.size());
        assertEquals("Alpha Devs", teamsA.get(0).getTeamName());
        assertEquals(orgA.getId(), teamsA.get(0).getOrganization().getId());

        // Authenticate context for Org B
        TenantContext.setCurrentTenant(orgB.getId());
        List<Team> teamsB = teamRepository.findByOrganizationIdAndDeletedFalse(orgB.getId());
        assertEquals(1, teamsB.size());
        assertEquals("Beta Marketers", teamsB.get(0).getTeamName());
        assertEquals(orgB.getId(), teamsB.get(0).getOrganization().getId());
    }

    @Test
    void testDataIsolation_GetSingleTeam_CrossTenantBlocked() {
        // Org A user trying to fetch Org B team
        TenantContext.setCurrentTenant(orgA.getId());
        assertThrows(IllegalArgumentException.class, () -> teamService.getTeam(teamB.getId(), userOrgA));

        // Org B user trying to fetch Org A team
        TenantContext.setCurrentTenant(orgB.getId());
        assertThrows(IllegalArgumentException.class, () -> teamService.getTeam(teamA.getId(), userOrgB));
    }

    @Test
    void testDataIsolation_CreateTeam_AssignsUserOrganizationId() {
        TenantContext.setCurrentTenant(orgA.getId());

        TeamDtos.TeamCreateRequest req = new TeamDtos.TeamCreateRequest();
        req.setTeamName("Alpha QA");
        req.setTeamCode("ALPHA-QA");
        req.setDepartmentId(deptA.getId());

        TeamDtos.TeamResponseDto created = teamService.createTeam(req, userOrgA);
        assertNotNull(created);
        assertEquals("Alpha QA", created.getTeamName());

        // Verify database entity is strictly assigned to Org A
        Team entity = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(created.getTeamId(), orgA.getId()).orElse(null);
        assertNotNull(entity);
        assertEquals(orgA.getId(), entity.getOrganization().getId());
    }

    @Test
    void testDataIsolation_CreateTeam_ReferencingOtherOrgDepartment_Fails() {
        TenantContext.setCurrentTenant(orgA.getId());

        TeamDtos.TeamCreateRequest req = new TeamDtos.TeamCreateRequest();
        req.setTeamName("Cross Tenant Team");
        req.setTeamCode("CROSS-1");
        req.setDepartmentId(deptB.getId()); // Referencing Org B department!

        // Must throw Exception because Dept B does not belong to Org A
        assertThrows(IllegalArgumentException.class, () -> teamService.createTeam(req, userOrgA));
    }

    @Test
    void testDataIsolation_UpdateTeam_CrossTenantUpdateBlocked() {
        TenantContext.setCurrentTenant(orgA.getId());

        TeamDtos.TeamUpdateRequest updateReq = new TeamDtos.TeamUpdateRequest();
        updateReq.setTeamName("Hacked Beta Team");

        // Org A attempting to update Org B team
        assertThrows(IllegalArgumentException.class, () -> teamService.updateTeam(teamB.getId(), updateReq, userOrgA));

        // Verify Org B team remains untouched in database
        Team originalTeamB = teamRepository.findById(teamB.getId()).orElse(null);
        assertNotNull(originalTeamB);
        assertEquals("Beta Marketers", originalTeamB.getTeamName());
    }

    @Test
    void testDataIsolation_DeleteTeam_CrossTenantDeleteBlocked() {
        TenantContext.setCurrentTenant(orgA.getId());

        // Org A attempting to delete Org B team
        assertThrows(IllegalArgumentException.class, () -> teamService.deleteTeam(teamB.getId(), userOrgA));

        // Verify Org B team remains in database
        Team originalTeamB = teamRepository.findById(teamB.getId()).orElse(null);
        assertNotNull(originalTeamB);
        assertFalse(Boolean.TRUE.equals(originalTeamB.getDeleted()));
    }
}
