package com.example.ems.audit.integration;

import com.example.ems.audit.dto.AuditLogEvent;
import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.enums.AuditModule;
import com.example.ems.audit.enums.AuditStatus;
import com.example.ems.audit.repository.AuditLogRepository;
import com.example.ems.audit.service.AuditLogService;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.PermissionGroup;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionGroupRepository;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.dto.AuthPrincipal;
import com.example.ems.security.service.PermissionCheckService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PermissionAuditSecurityIntegrationTest {

    @Autowired
    private PermissionCheckService permissionCheckService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private com.example.ems.organization.repository.OrganizationRepository organizationRepository;

    private Long testCompanyId;
    private com.example.ems.organization.entity.Organization testOrganization;

    @BeforeEach
    public void setUp() {
        testOrganization = new com.example.ems.organization.entity.Organization();
        testOrganization.setName("Audit Security Test Org");
        testOrganization.setOrganizationCode("AUD_" + System.currentTimeMillis());
        testOrganization.setStatus(com.example.ems.organization.entity.OrganizationStatus.ACTIVE);
        testOrganization = organizationRepository.save(testOrganization);
        testCompanyId = testOrganization.getId();
        TenantContext.setCurrentTenant(testCompanyId);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
        try {
            List<AuditLog> logs = auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(testCompanyId);
            auditLogRepository.deleteAll(logs);
            List<User> users = userRepository.findByOrganizationId(testCompanyId);
            userRepository.deleteAll(users);
            if (testOrganization != null && testOrganization.getId() != null) {
                organizationRepository.deleteById(testOrganization.getId());
            }
        } catch (Exception ignored) {}
    }

    private void authenticateUser(String email, String userId, String roleName) {
        AuthPrincipal principal = new AuthPrincipal(userId, "session-test", 1, System.currentTimeMillis(), email, roleName);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + roleName))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Permission getOrCreatePermission(String name) {
        return permissionRepository.findByName(name).orElseGet(() -> {
            Permission p = new Permission();
            p.setName(name);
            p.setDescription("Test perm " + name);
            p.setActive(true);
            return permissionRepository.save(p);
        });
    }

    @Test
    @DisplayName("Test 1 — Dynamic role: Two custom roles with candidate.create both succeed with permission recorded and zero role hardcoding")
    public void test1_DynamicRolesAudit() {
        Permission candidateCreate = getOrCreatePermission("candidate.create");

        // Role A
        Role roleA = new Role();
        roleA.setName("CUSTOM_ROLE_RECRUITER_A_" + System.currentTimeMillis());
        roleA.setOrganization(testOrganization);
        roleA.setPlatformTemplate(false);
        roleA.setPermissions(new HashSet<>(Set.of(candidateCreate)));
        roleA = roleRepository.save(roleA);

        User userA = new User();
        userA.setUserId("USR_ROLA");
        userA.setWorkEmail("userA@dynamic.test");
        userA.setRole(roleA);
        userA.setOrganizationId(testCompanyId);
        userRepository.save(userA);

        // Role B
        Role roleB = new Role();
        roleB.setName("CUSTOM_ROLE_RECRUITER_B_" + System.currentTimeMillis());
        roleB.setOrganization(testOrganization);
        roleB.setPlatformTemplate(false);
        roleB.setPermissions(new HashSet<>(Set.of(candidateCreate)));
        roleB = roleRepository.save(roleB);

        User userB = new User();
        userB.setUserId("USR_ROLB");
        userB.setWorkEmail("userB@dynamic.test");
        userB.setRole(roleB);
        userB.setOrganizationId(testCompanyId);
        userRepository.save(userB);

        // User A performs action
        authenticateUser(userA.getWorkEmail(), userA.getUserId(), roleA.getName());
        permissionCheckService.requirePermission("candidate.create");
        AuditLog logA = auditLogService.success(AuditLogEvent.builder()
                .companyId(testCompanyId)
                .module(AuditModule.RECRUITMENT)
                .action(AuditAction.CREATE)
                .entityType("Candidate")
                .recordId("CAND-001")
                .permission("candidate.create")
                .details("Created candidate by Role A user")
                .build());

        // User B performs action
        authenticateUser(userB.getWorkEmail(), userB.getUserId(), roleB.getName());
        permissionCheckService.requirePermission("candidate.create");
        AuditLog logB = auditLogService.success(AuditLogEvent.builder()
                .companyId(testCompanyId)
                .module(AuditModule.RECRUITMENT)
                .action(AuditAction.CREATE)
                .entityType("Candidate")
                .recordId("CAND-002")
                .permission("candidate.create")
                .details("Created candidate by Role B user")
                .build());

        assertNotNull(logA);
        assertNotNull(logB);
        assertEquals("candidate.create", logA.getPermission());
        assertEquals("candidate.create", logB.getPermission());
        assertEquals(AuditStatus.SUCCESS.name(), logA.getStatus());
        assertEquals(AuditStatus.SUCCESS.name(), logB.getStatus());
    }

    @Test
    @DisplayName("Test 2 — Direct permission: User granted direct permission without group succeeds and audit records effective permission")
    public void test2_DirectPermissionAudit() {
        Permission candidateCreate = getOrCreatePermission("candidate.create");

        Role directRole = new Role();
        directRole.setName("DIRECT_PERM_ROLE_" + System.currentTimeMillis());
        directRole.setOrganization(testOrganization);
        directRole.setPlatformTemplate(false);
        directRole.setDirectPermissions(new HashSet<>(Set.of(candidateCreate)));
        directRole.setPermissions(new HashSet<>(Set.of(candidateCreate)));
        directRole = roleRepository.save(directRole);

        User directUser = new User();
        directUser.setUserId("USR_DIRECT");
        directUser.setWorkEmail("direct@dynamic.test");
        directUser.setRole(directRole);
        directUser.setOrganizationId(testCompanyId);
        userRepository.save(directUser);

        authenticateUser(directUser.getWorkEmail(), directUser.getUserId(), directRole.getName());
        assertTrue(permissionCheckService.hasPermission("candidate.create"));
        permissionCheckService.requirePermission("candidate.create");

        AuditLog log = auditLogService.success(AuditLogEvent.builder()
                .companyId(testCompanyId)
                .module(AuditModule.RECRUITMENT)
                .action(AuditAction.CREATE)
                .entityType("Candidate")
                .recordId("CAND-DIRECT")
                .permission("candidate.create")
                .details("Created candidate using direct permission")
                .build());

        assertNotNull(log);
        assertEquals("candidate.create", log.getPermission());
        assertEquals(AuditStatus.SUCCESS.name(), log.getStatus());
    }

    @Test
    @DisplayName("Test 3 — Group permission: Custom role with Recruitment Group inherits candidate.create and audit logs effective permission")
    public void test3_GroupPermissionAudit() {
        Permission candidateCreate = getOrCreatePermission("candidate.create");

        PermissionGroup group = new PermissionGroup();
        group.setCode("RECRUITMENT_TEST_GRP_" + System.currentTimeMillis());
        group.setName("Recruitment Group Test");
        group.setPermissions(new HashSet<>(Set.of(candidateCreate)));
        group = permissionGroupRepository.save(group);

        Role groupRole = new Role();
        groupRole.setName("GROUP_TEST_ROLE_" + System.currentTimeMillis());
        groupRole.setOrganization(testOrganization);
        groupRole.setPlatformTemplate(false);
        groupRole.setPermissionGroups(new HashSet<>(Set.of(group)));
        groupRole.setPermissions(new HashSet<>(Set.of(candidateCreate)));
        groupRole = roleRepository.save(groupRole);

        User groupUser = new User();
        groupUser.setUserId("USR_GRP");
        groupUser.setWorkEmail("groupuser@dynamic.test");
        groupUser.setRole(groupRole);
        groupUser.setOrganizationId(testCompanyId);
        userRepository.save(groupUser);

        authenticateUser(groupUser.getWorkEmail(), groupUser.getUserId(), groupRole.getName());
        assertTrue(permissionCheckService.hasPermission("candidate.create"));
        permissionCheckService.requirePermission("candidate.create");

        AuditLog log = auditLogService.success(AuditLogEvent.builder()
                .companyId(testCompanyId)
                .module(AuditModule.RECRUITMENT)
                .action(AuditAction.CREATE)
                .entityType("Candidate")
                .recordId("CAND-GRP")
                .permission("candidate.create")
                .details("Created candidate using group-inherited permission")
                .build());

        assertNotNull(log);
        assertEquals("candidate.create", log.getPermission());
        assertEquals(AuditStatus.SUCCESS.name(), log.getStatus());
    }

    @Test
    @DisplayName("Test 4 — Denied permission: AccessDeniedException rolled back outer transaction, but DENIED audit row PERSISTS via REQUIRES_NEW")
    public void test4_DeniedPermissionAuditPersistsAfterRollback() {
        Permission candidateRead = getOrCreatePermission("candidate.read");

        Role readOnlyRole = new Role();
        readOnlyRole.setName("READONLY_CANDIDATE_ROLE_" + System.currentTimeMillis());
        readOnlyRole.setOrganization(testOrganization);
        readOnlyRole.setPlatformTemplate(false);
        readOnlyRole.setPermissions(new HashSet<>(Set.of(candidateRead)));
        readOnlyRole = roleRepository.save(readOnlyRole);

        User readOnlyUser = new User();
        readOnlyUser.setUserId("USR_READONLY");
        readOnlyUser.setWorkEmail("readonly@dynamic.test");
        readOnlyUser.setRole(readOnlyRole);
        readOnlyUser.setOrganizationId(testCompanyId);
        userRepository.save(readOnlyUser);

        authenticateUser(readOnlyUser.getWorkEmail(), readOnlyUser.getUserId(), readOnlyRole.getName());

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // Run in transaction that rolls back due to AccessDeniedException
        assertThrows(AccessDeniedException.class, () -> {
            txTemplate.execute(status -> {
                // User has read, but attempts candidate.delete
                permissionCheckService.requirePermission("candidate.delete");
                return null;
            });
        });

        // Verify that the audit row WAS saved and committed despite outer transaction failure
        List<AuditLog> logs = auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(testCompanyId);
        AuditLog deniedLog = logs.stream()
                .filter(l -> "candidate.delete".equals(l.getPermission()))
                .findFirst()
                .orElse(null);

        assertNotNull(deniedLog, "Denied audit row MUST exist even though outer transaction failed!");
        assertEquals(AuditStatus.DENIED.name(), deniedLog.getStatus());
        assertEquals("candidate.delete", deniedLog.getPermission());
        assertTrue(deniedLog.getFailureReason().contains("INSUFFICIENT_PERMISSION"));
    }

    @Test
    @DisplayName("Test 5 — Change authorization: Allowed initially, then denied after permission revocation with DENIED audit generated")
    public void test5_DynamicAuthorizationChangeAudit() {
        Permission candidateCreate = getOrCreatePermission("candidate.create");

        Role dynamicRole = new Role();
        dynamicRole.setName("CHANGING_ROLE_" + System.currentTimeMillis());
        dynamicRole.setPermissions(new HashSet<>(Set.of(candidateCreate)));
        dynamicRole = roleRepository.save(dynamicRole);

        User dynamicUser = new User();
        dynamicUser.setUserId("USR_DYNAMIC");
        dynamicUser.setWorkEmail("dynamic@dynamic.test");
        dynamicUser.setRole(dynamicRole);
        dynamicUser.setOrganizationId(testCompanyId);
        userRepository.save(dynamicUser);

        authenticateUser(dynamicUser.getWorkEmail(), dynamicUser.getUserId(), dynamicRole.getName());

        // Step 1: Initial request succeeds
        permissionCheckService.requirePermission("candidate.create");
        AuditLog initialSuccess = auditLogService.success(AuditLogEvent.builder()
                .companyId(testCompanyId)
                .module(AuditModule.RECRUITMENT)
                .action(AuditAction.CREATE)
                .entityType("Candidate")
                .recordId("CAND-DYN-1")
                .permission("candidate.create")
                .details("Allowed initially")
                .build());
        assertEquals(AuditStatus.SUCCESS.name(), initialSuccess.getStatus());

        // Step 2: Revoke permission from role and evict cache
        dynamicRole.setPermissions(new HashSet<>(Collections.emptySet()));
        roleRepository.save(dynamicRole);
        roleService.evictRolePermissionsCache(dynamicRole.getId());
        roleService.evictUserPermissionsCache(dynamicUser.getUserId());

        // Step 3: Request again -> throws AccessDeniedException and persists DENIED audit
        assertThrows(AccessDeniedException.class, () -> {
            permissionCheckService.requirePermission("candidate.create");
        });

        List<AuditLog> logs = auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(testCompanyId);
        AuditLog deniedLog = logs.stream()
                .filter(l -> "candidate.create".equals(l.getPermission()) && AuditStatus.DENIED.name().equals(l.getStatus()))
                .findFirst()
                .orElse(null);

        assertNotNull(deniedLog, "DENIED audit log must be recorded after authorization removal");
        assertEquals(AuditStatus.DENIED.name(), deniedLog.getStatus());
        assertEquals("candidate.create", deniedLog.getPermission());
    }
}
