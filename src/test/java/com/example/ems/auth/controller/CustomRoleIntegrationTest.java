package com.example.ems.auth.controller;

import com.example.ems.auth.dto.RoleRequest;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.PermissionGroup;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionGroupRepository;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.config.DatabaseSeeder;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CustomRoleIntegrationTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private DatabaseSeeder databaseSeeder;

    private Organization tenantA;
    private Organization tenantB;
    private PermissionGroup leaveSelfGroup;
    private PermissionGroup attendanceManagerGroup;

    @BeforeEach
    void setUp() {
        databaseSeeder.seedCoreAuthData();

        tenantA = new Organization();
        tenantA.setName("Tenant A - " + System.currentTimeMillis());
        tenantA.setOrganizationCode("ORG_A_" + System.currentTimeMillis());
        tenantA = organizationRepository.save(tenantA);

        tenantB = new Organization();
        tenantB.setName("Tenant B - " + System.currentTimeMillis());
        tenantB.setOrganizationCode("ORG_B_" + System.currentTimeMillis());
        tenantB = organizationRepository.save(tenantB);

        leaveSelfGroup = permissionGroupRepository.findByCode("LEAVE_SELF_SERVICE")
                .orElseThrow(() -> new IllegalStateException("LEAVE_SELF_SERVICE group not seeded"));
        attendanceManagerGroup = permissionGroupRepository.findByCode("ATTENDANCE_MANAGER")
                .orElseThrow(() -> new IllegalStateException("ATTENDANCE_MANAGER group not seeded"));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Tenant A creates a custom role with tiered permission groups and direct permissions")
    void testCreateCustomRoleWithTieredGroupsAndDirectPermissions() {
        TenantContext.setCurrentTenant(tenantA.getId());

        Permission directPerm = permissionRepository.findByName("recruitment.manage")
                .orElseThrow(() -> new IllegalStateException("recruitment.manage not found"));

        RoleRequest req = new RoleRequest();
        req.setName("SENIOR_TEAM_LEAD");
        req.setDescription("Custom lead role for Tenant A");
        req.setPermissionGroupIds(List.of(leaveSelfGroup.getId(), attendanceManagerGroup.getId()));
        req.setPermissionIds(List.of(directPerm.getId()));

        Role created = roleService.createTenantRole(req);

        assertNotNull(created.getId());
        assertEquals("SENIOR_TEAM_LEAD", created.getName());
        assertEquals(tenantA.getId(), created.getOrganization().getId());
        assertFalse(created.isPlatformTemplate());
        assertFalse(created.isSystemRole());

        // Verify effective permissions combine groups + direct permission
        Set<String> permNames = created.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        assertTrue(permNames.contains("leave.self.read"));
        assertTrue(permNames.contains("employee.leave.create"));
        assertTrue(permNames.contains("attendance.team.read"));
        assertTrue(permNames.contains("attendance.permission.approve"));
        assertTrue(permNames.contains("recruitment.manage"));

        // Crucial verification: does NOT have admin or manager leave permissions
        assertFalse(permNames.contains("leave.approve"));
        assertFalse(permNames.contains("leave.manage"));
        assertFalse(permNames.contains("attendance.manage"));
    }

    @Test
    @DisplayName("Tenant A clones from platform template role (MANAGER)")
    void testCloneFromPlatformTemplate() {
        TenantContext.setCurrentTenant(tenantA.getId());

        Role template = roleRepository.findByNameAndIsPlatformTemplateTrue("MANAGER")
                .orElseThrow(() -> new IllegalStateException("MANAGER template not found"));

        Role cloned = roleService.cloneFromTemplate(template.getId(), "OPERATIONS_MANAGER", "Custom Ops Manager");

        assertNotNull(cloned.getId());
        assertEquals("OPERATIONS_MANAGER", cloned.getName());
        assertEquals("Custom Ops Manager", cloned.getDescription());
        assertEquals(tenantA.getId(), cloned.getOrganization().getId());
        assertFalse(cloned.isPlatformTemplate());
        assertFalse(cloned.isSystemRole());

        // Verify cloned permissions match template
        Set<String> templatePerms = template.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());
        Set<String> clonedPerms = cloned.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());
        assertEquals(templatePerms, clonedPerms);
    }

    @Test
    @DisplayName("Tenant B cannot access or modify Tenant A's custom role (Tenant Isolation)")
    void testTenantIsolationCrossTenantAccessBlocked() {
        TenantContext.setCurrentTenant(tenantA.getId());

        RoleRequest req = new RoleRequest();
        req.setName("CONFIDENTIAL_ROLE");
        req.setDescription("Owned exclusively by Tenant A");
        req.setPermissionGroupIds(List.of(leaveSelfGroup.getId()));
        Role roleA = roleService.createTenantRole(req);

        // Switch context to Tenant B
        TenantContext.setCurrentTenant(tenantB.getId());

        // Attempting to read Tenant A's role fails with AccessDeniedException
        AccessDeniedException readEx = assertThrows(AccessDeniedException.class, () ->
                roleService.requireRoleOwnedByCurrentTenant(roleA.getId()));
        assertTrue(readEx.getMessage().contains("ROLE_TENANT_ACCESS_DENIED"));

        // Attempting to mutate Tenant A's role fails with AccessDeniedException
        AccessDeniedException mutateEx = assertThrows(AccessDeniedException.class, () ->
                roleService.requireTenantMutableRole(roleA.getId()));
        assertTrue(mutateEx.getMessage().contains("ROLE_TENANT_ACCESS_DENIED"));

        // Attempting to delete Tenant A's role fails with AccessDeniedException
        AccessDeniedException delEx = assertThrows(AccessDeniedException.class, () ->
                roleService.deleteTenantRole(roleA.getId()));
        assertTrue(delEx.getMessage().contains("ROLE_TENANT_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("Protected system roles and platform templates cannot be deleted or mutated by tenants")
    void testProtectedSystemRolesCannotBeDeleted() {
        TenantContext.setCurrentTenant(tenantA.getId());

        Role employeeRole = roleRepository.findByNameAndIsPlatformTemplateTrue("EMPLOYEE")
                .orElseThrow(() -> new IllegalStateException("EMPLOYEE template not found"));

        // Mutating a platform template is rejected
        ConflictException mutateEx = assertThrows(ConflictException.class, () ->
                roleService.requireTenantMutableRole(employeeRole.getId()));
        assertEquals("ROLE_SYSTEM_PROTECTED", mutateEx.getErrorCode());

        // Deleting a platform template or system role is rejected
        ConflictException delEx = assertThrows(ConflictException.class, () ->
                roleService.deleteTenantRole(employeeRole.getId()));
        assertEquals("ROLE_SYSTEM_PROTECTED", delEx.getErrorCode());
    }

    @Test
    @DisplayName("Deleting a custom role assigned to active users is rejected with ROLE_ASSIGNED_TO_ACTIVE_USERS")
    void testCannotDeleteRoleAssignedToActiveUsers() {
        TenantContext.setCurrentTenant(tenantA.getId());

        RoleRequest req = new RoleRequest();
        req.setName("ASSIGNED_CUSTOM_ROLE");
        req.setDescription("Role assigned to user");
        req.setPermissionGroupIds(List.of(leaveSelfGroup.getId()));
        Role customRole = roleService.createTenantRole(req);

        // Assign to an active user in Tenant A
        User user = new User();
        user.setUserId("USR_TENANT_A_" + System.currentTimeMillis());
        user.setWorkEmail("worker_" + System.currentTimeMillis() + "@tenanta.com");
        user.setOrganization(tenantA);
        user.setOrganizationId(tenantA.getId());
        user.setRole(customRole);
        user.setRoleId(customRole.getId());
        userRepository.save(user);

        // Attempt to delete role
        ConflictException ex = assertThrows(ConflictException.class, () ->
                roleService.deleteTenantRole(customRole.getId()));

        assertEquals("ROLE_ASSIGNED_TO_ACTIVE_USERS", ex.getErrorCode());
    }

    @Test
    @DisplayName("Duplicate role name within same tenant is rejected with ROLE_NAME_ALREADY_EXISTS")
    void testDuplicateRoleNameRejected() {
        TenantContext.setCurrentTenant(tenantA.getId());

        RoleRequest req1 = new RoleRequest();
        req1.setName("DUPLICATE_NAME_ROLE");
        req1.setDescription("First instance");
        roleService.createTenantRole(req1);

        RoleRequest req2 = new RoleRequest();
        req2.setName("DUPLICATE_NAME_ROLE");
        req2.setDescription("Second instance");

        ConflictException ex = assertThrows(ConflictException.class, () ->
                roleService.createTenantRole(req2));

        assertEquals("ROLE_NAME_ALREADY_EXISTS", ex.getErrorCode());
    }
}
