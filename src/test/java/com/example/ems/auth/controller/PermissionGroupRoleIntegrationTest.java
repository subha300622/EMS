package com.example.ems.auth.controller;

import com.example.ems.auth.dto.PermissionCatalogResponseDto;
import com.example.ems.auth.dto.RoleRequest;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.PermissionGroup;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionGroupRepository;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.PermissionService;
import com.example.ems.auth.service.RoleService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PermissionGroupRoleIntegrationTest {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.ems.config.DatabaseSeeder databaseSeeder;

    private Organization orgA;
    private Organization orgB;

    @BeforeEach
    void setUp() {
        databaseSeeder.seedCoreAuthData();

        orgA = organizationRepository.findAll().stream()
                .filter(o -> "Org Alpha".equalsIgnoreCase(o.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Organization o = new Organization();
                    o.setName("Org Alpha");
                    o.setOrganizationCode("ORG_ALPHA_" + System.currentTimeMillis());
                    return organizationRepository.save(o);
                });

        orgB = organizationRepository.findAll().stream()
                .filter(o -> "Org Beta".equalsIgnoreCase(o.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Organization o = new Organization();
                    o.setName("Org Beta");
                    o.setOrganizationCode("ORG_BETA_" + System.currentTimeMillis());
                    return organizationRepository.save(o);
                });
    }

    @Test
    @DisplayName("1. Verify Permission Catalog API Structure")
    void testPermissionCatalogStructure() {
        PermissionCatalogResponseDto catalog = permissionService.getPermissionCatalog();

        assertNotNull(catalog);
        assertNotNull(catalog.getGroups());
        assertFalse(catalog.getGroups().isEmpty(), "Permission groups master catalog should not be empty");

        boolean hasEmployeeManagement = catalog.getGroups().stream()
                .anyMatch(g -> "EMPLOYEE_MANAGEMENT".equalsIgnoreCase(g.getCode()));
        assertTrue(hasEmployeeManagement, "Catalog must include EMPLOYEE_MANAGEMENT permission group");
    }

    @Test
    @DisplayName("2. Create Custom Role with Group and Direct Permissions (Effective Union Calculation)")
    void testCreateCustomRoleWithGroupsAndDirectPermissions() {
        PermissionGroup empGroup = permissionGroupRepository.findByCode("EMPLOYEE_MANAGEMENT")
                .orElseThrow(() -> new IllegalStateException("EMPLOYEE_MANAGEMENT group missing"));

        Permission directPerm = permissionRepository.findByName("leave.team.approve")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setName("leave.team.approve");
                    p.setDescription("Test leave team approve permission");
                    return permissionRepository.save(p);
                });

        RoleRequest req = new RoleRequest();
        req.setName("Operations Lead");
        req.setDescription("Custom role combining Employee Management group and direct leave approval permission");
        req.setPermissionGroupIds(List.of(empGroup.getId()));
        req.setPermissionIds(List.of(directPerm.getId()));

        Role role = roleService.createTenantRole(orgA.getId(), req);

        assertNotNull(role);
        assertNotNull(role.getId());
        assertEquals("Operations Lead", role.getName());
        assertEquals(orgA.getId(), role.getOrganization().getId());

        // Check group association
        assertEquals(1, role.getPermissionGroups().size());
        assertTrue(role.getPermissionGroups().contains(empGroup));

        // Check direct permissions association
        assertEquals(1, role.getDirectPermissions().size());
        assertTrue(role.getDirectPermissions().contains(directPerm));

        // Verify effective permissions = Union(group permissions) + direct permissions
        Set<String> effectiveNames = role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        for (Permission p : empGroup.getPermissions()) {
            assertTrue(effectiveNames.contains(p.getName()),
                    "Effective permissions must contain group permission: " + p.getName());
        }
        assertTrue(effectiveNames.contains("leave.team.approve"),
                "Effective permissions must contain direct permission: leave.team.approve");
    }

    @Test
    @DisplayName("3. Verify Tenant Isolation for Roles across Organizations")
    void testTenantRoleIsolation() {
        RoleRequest reqA = new RoleRequest();
        reqA.setName("Regional Manager");
        reqA.setPermissionNames(List.of("employee.read", "employee.update"));
        Role roleA = roleService.createTenantRole(orgA.getId(), reqA);

        RoleRequest reqB = new RoleRequest();
        reqB.setName("Regional Manager");
        reqB.setPermissionNames(List.of("payroll.read", "reports.view"));
        Role roleB = roleService.createTenantRole(orgB.getId(), reqB);

        assertNotEquals(roleA.getId(), roleB.getId());
        assertEquals(orgA.getId(), roleA.getOrganization().getId());
        assertEquals(orgB.getId(), roleB.getOrganization().getId());

        List<Role> orgARoles = roleService.getTenantRoles(orgA.getId());
        assertTrue(orgARoles.stream().anyMatch(r -> r.getId().equals(roleA.getId())));
        assertFalse(orgARoles.stream().anyMatch(r -> r.getId().equals(roleB.getId())));
    }

    @Test
    @DisplayName("4. Dynamic Role Authorization Verification")
    void testRoleAuthorizationHasPermission() {
        PermissionGroup attGroup = permissionGroupRepository.findByCode("ATTENDANCE")
                .orElseThrow(() -> new IllegalStateException("ATTENDANCE group missing"));

        RoleRequest req = new RoleRequest();
        req.setName("Shift Supervisor");
        req.setPermissionGroupIds(List.of(attGroup.getId()));

        Role role = roleService.createTenantRole(orgA.getId(), req);

        User user = new User();
        user.setWorkEmail("supervisor-" + System.currentTimeMillis() + "@orga.com");
        user.setUserId("EMP_SUP_" + System.currentTimeMillis());
        user.setFirstName("Shift");
        user.setLastName("Supervisor");
        user.setRole(role);
        user.setOrganization(orgA);
        user = userRepository.save(user);

        boolean canReadAttendance = roleService.hasPermission(user.getWorkEmail(), "attendance.read");
        boolean canManagePayroll = roleService.hasPermission(user.getWorkEmail(), "payroll.manage");

        assertTrue(canReadAttendance, "User with ATTENDANCE group should have attendance.read permission");
        assertFalse(canManagePayroll, "User without PAYROLL group should not have payroll.manage permission");
    }
}
