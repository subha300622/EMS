package com.example.ems.auth.service;

import com.example.ems.auth.dto.RoleRequest;
import com.example.ems.auth.entity.PermissionGroup;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionGroupRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.config.DatabaseSeeder;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.dto.AuthAuthenticationToken;
import com.example.ems.security.dto.AuthPrincipal;
import com.example.ems.security.service.PermissionCheckService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PermissionEnforcementIntegrationTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionCheckService permissionCheckService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private DatabaseSeeder databaseSeeder;

    private Organization testOrg;
    private User testUser;
    private Role roleWithLeave;
    private Role roleWithoutLeave;

    @BeforeEach
    void setUp() {
        databaseSeeder.seedCoreAuthData();

        testOrg = new Organization();
        testOrg.setName("Enforcement Test Org " + System.currentTimeMillis());
        testOrg.setOrganizationCode("ORG_ENF_" + System.currentTimeMillis());
        testOrg = organizationRepository.save(testOrg);

        TenantContext.setCurrentTenant(testOrg.getId());

        PermissionGroup leaveSelf = permissionGroupRepository.findByCode("LEAVE_SELF_SERVICE")
                .orElseThrow(() -> new IllegalStateException("LEAVE_SELF_SERVICE not found"));
        PermissionGroup attendanceSelf = permissionGroupRepository.findByCode("ATTENDANCE_SELF_SERVICE")
                .orElseThrow(() -> new IllegalStateException("ATTENDANCE_SELF_SERVICE not found"));

        // Role 1: Has Leave self-service
        RoleRequest req1 = new RoleRequest();
        req1.setName("ROLE_WITH_LEAVE_" + System.currentTimeMillis());
        req1.setPermissionGroupIds(List.of(leaveSelf.getId()));
        roleWithLeave = roleService.createTenantRole(req1);

        // Role 2: Attendance only (NO leave permissions)
        RoleRequest req2 = new RoleRequest();
        req2.setName("ROLE_NO_LEAVE_" + System.currentTimeMillis());
        req2.setPermissionGroupIds(List.of(attendanceSelf.getId()));
        roleWithoutLeave = roleService.createTenantRole(req2);

        // User setup
        testUser = new User();
        testUser.setUserId("ENF_USER_" + System.currentTimeMillis());
        testUser.setWorkEmail("enf_user_" + System.currentTimeMillis() + "@test.com");
        testUser.setOrganization(testOrg);
        testUser.setOrganizationId(testOrg.getId());
        testUser.setRole(roleWithLeave);
        testUser.setRoleId(roleWithLeave.getId());
        testUser.setRequestedRole(roleWithLeave.getName());
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    private void establishSecurityContext(User user) {
        AuthPrincipal principal = new AuthPrincipal(
                user.getUserId(),
                "SESS_" + user.getUserId(),
                1,
                1L,
                user.getWorkEmail(),
                user.getRole() != null ? user.getRole().getName() : "ROLE_USER"
        );

        // Hydrate authorities via roleService (identical to SessionAuthenticationProvider)
        List<String> perms = roleService.getPermissionsForUserId(user.getUserId());
        List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + principal.getRole()));
        for (String p : perms) {
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(p));
        }

        AuthAuthenticationToken token = new AuthAuthenticationToken(principal, authorities);
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test
    @DisplayName("Verify Spring Security authority hydration contains both Role and granular permissions")
    void testSecurityAuthorityHydration() {
        establishSecurityContext(testUser);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());

        Set<String> authorityNames = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Role authority present
        assertTrue(authorityNames.contains("ROLE_" + roleWithLeave.getName()));

        // Granular permission authorities present
        assertTrue(authorityNames.contains("leave.self.read"));
        assertTrue(authorityNames.contains("employee.leave.create"));
        assertFalse(authorityNames.contains("leave.approve"));
    }

    @Test
    @DisplayName("Live cache invalidation under active session: 200 -> Role modified -> 403 -> Role restored -> 200")
    void testLiveCacheInvalidationUnderActiveSession() {
        TenantContext.setCurrentTenant(testOrg.getId());

        // 1. User starts with roleWithLeave
        establishSecurityContext(testUser);

        // User can access leave.self.read
        assertTrue(permissionCheckService.hasPermission("leave.self.read"));
        assertDoesNotThrow(() -> permissionCheckService.requirePermission("leave.self.read"));

        // 2. Admin mutates user's role to roleWithoutLeave (cache evicted)
        roleService.assignRoleById(testUser.getId(), roleWithoutLeave.getId(), testOrg.getId());

        // Under the active session, permissions are dynamically checked against the evicted cache
        assertFalse(permissionCheckService.hasPermission("leave.self.read"));
        AccessDeniedException denied = assertThrows(AccessDeniedException.class, () ->
                permissionCheckService.requirePermission("leave.self.read"));
        assertTrue(denied.getMessage().contains("Requires 'leave.self.read' permission"));

        // But user now has attendance.self.read
        assertTrue(permissionCheckService.hasPermission("attendance.self.read"));

        // 3. Admin re-assigns roleWithLeave back to user
        roleService.assignRoleById(testUser.getId(), roleWithLeave.getId(), testOrg.getId());

        // Immediately restored without re-login
        assertTrue(permissionCheckService.hasPermission("leave.self.read"));
        assertDoesNotThrow(() -> permissionCheckService.requirePermission("leave.self.read"));
    }

    @Test
    @DisplayName("Super Admin bypasses all individual permission checks")
    void testSuperAdminBypass() {
        Role superAdminRole = roleRepository.findByNameAndIsPlatformTemplateTrue("SUPER_ADMIN")
                .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN template not found"));

        User superUser = new User();
        superUser.setUserId("SUPER_ADMIN_" + System.currentTimeMillis());
        superUser.setWorkEmail("superadmin_" + System.currentTimeMillis() + "@test.com");
        superUser.setOrganization(testOrg);
        superUser.setRole(superAdminRole);
        superUser.setRequestedRole("SUPER_ADMIN");
        superUser = userRepository.save(superUser);

        establishSecurityContext(superUser);

        // Super Admin has access to any permission
        assertTrue(permissionCheckService.hasPermission("leave.manage"));
        assertTrue(permissionCheckService.hasPermission("payroll.disburse"));
        assertTrue(permissionCheckService.hasPermission("arbitrary.permission.check"));
    }
}
