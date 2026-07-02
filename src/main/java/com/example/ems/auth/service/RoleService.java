package com.example.ems.auth.service;

import com.example.ems.auth.dto.RoleRequest;
import com.example.ems.auth.dto.TemplateDiffResponse;
import com.example.ems.auth.dto.RoleStatsResponse;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private CacheManager cacheManager;

    @Transactional(readOnly = true)
    @Cacheable(value = "userPermissions", key = "#userId")
    public List<String> getPermissionsForUserId(String userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Optional<User> optUser = userRepository.findByUserId(userId);
        if (optUser.isEmpty()) {
            return Collections.emptyList();
        }
        return getEffectivePermissions(optUser.get()).stream()
                .map(Permission::getName)
                .collect(Collectors.toList());
    }

    public void evictUserPermissionsCache(String userId) {
        if (userId != null && cacheManager != null) {
            try {
                Cache permCache = cacheManager.getCache("userPermissions");
                if (permCache != null) {
                    permCache.evict(userId);
                }
            } catch (Exception e) {
                // Log and ignore to prevent crashes when Redis is down
            }
            try {
                Cache bootCache = cacheManager.getCache("userBootstrap");
                if (bootCache != null) {
                    bootCache.evict(userId);
                }
            } catch (Exception e) {
                // Log and ignore
            }
        }
    }

    public void evictRolePermissionsCache(Long roleId) {
        if (roleId != null) {
            List<User> users = userRepository.findByRoleId(roleId);
            for (User u : users) {
                evictUserPermissionsCache(u.getUserId());
            }
        }
    }

    public Set<Permission> getEffectivePermissions(User user) {
        if (user == null || user.getRole() == null) {
            return new HashSet<>();
        }
        Set<Permission> perms = user.getRole().getPermissions();
        if (perms == null || perms.isEmpty()) {
            // Try to find the default tenant EMPLOYEE role first, then fallback to template
            Long orgId = user.getOrganization() != null ? user.getOrganization().getId() : null;
            Optional<Role> fallbackRole = Optional.empty();
            if (orgId != null) {
                fallbackRole = roleRepository.findByOrganizationIdAndName(orgId, "EMPLOYEE");
            }
            if (fallbackRole.isEmpty()) {
                fallbackRole = roleRepository.findByNameAndIsPlatformTemplateTrue("EMPLOYEE");
            }
            return fallbackRole.map(Role::getPermissions).orElse(new HashSet<>());
        }
        return perms;
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(String email, String permissionName) {
        if (email == null || email.trim().isEmpty() || permissionName == null || permissionName.trim().isEmpty()) {
            return false;
        }
        Optional<User> optUser = userRepository.findByWorkEmail(email);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();
        List<String> permissions = getPermissionsForUserId(user.getUserId());

        // SUPER_ADMIN bypass: if user has system.manage, allow everything
        boolean isSuperAdmin = permissions.contains("system.manage");
        if (isSuperAdmin) {
            return true;
        }

        return permissions.stream().anyMatch(perm -> perm.equalsIgnoreCase(permissionName));
    }

    public boolean hasRoleManagementPermission(String email) {
        return hasPermission(email, "role.manage");
    }

    public boolean isSuperAdmin(String email) {
        return hasPermission(email, "system.manage");
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    // ── Platform Template CRUD ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Role> getPlatformTemplates() {
        return roleRepository.findByIsPlatformTemplateTrue();
    }

    @Transactional
    public Role createPlatformTemplate(RoleRequest request) {
        if (roleRepository.existsByOrganizationIdAndName(null, request.getName())) {
            throw new IllegalArgumentException("Platform role template with name '" + request.getName() + "' already exists");
        }
        Role template = new Role();
        template.setName(request.getName().trim());
        template.setDescription(request.getDescription());
        template.setPlatformTemplate(true);
        template.setOrganization(null);
        template.setSystemRole(false);
        template.setVersion(1);
        return roleRepository.save(template);
    }

    @Transactional
    public Role updatePlatformTemplate(Long id, RoleRequest request) {
        Role template = roleRepository.findById(id)
                .filter(Role::isPlatformTemplate)
                .orElseThrow(() -> new IllegalArgumentException("Platform template not found with ID: " + id));

        if (template.isSystemRole() && !template.getName().equalsIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Cannot rename system roles");
        }

        if (!template.getName().equalsIgnoreCase(request.getName()) &&
                roleRepository.existsByOrganizationIdAndName(null, request.getName())) {
            throw new IllegalArgumentException("Platform role template with name '" + request.getName() + "' already exists");
        }

        template.setName(request.getName().trim());
        template.setDescription(request.getDescription());
        Role saved = roleRepository.save(template);
        evictRolePermissionsCache(id);
        return saved;
    }

    @Transactional
    public void deletePlatformTemplate(Long id) {
        Role template = roleRepository.findById(id)
                .filter(Role::isPlatformTemplate)
                .orElseThrow(() -> new IllegalArgumentException("Platform template not found with ID: " + id));

        if (template.isSystemRole()) {
            throw new IllegalArgumentException("Cannot delete core system roles");
        }

        evictRolePermissionsCache(id);
        roleRepository.delete(template);
    }

    // ── Tenant/Organization Role CRUD ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Role> getTenantRoles(Long organizationId) {
        return roleRepository.findByOrganizationId(organizationId);
    }

    @Transactional
    public Role createTenantRole(Long organizationId, RoleRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + organizationId));

        if (roleRepository.existsByOrganizationIdAndName(organizationId, request.getName())) {
            throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists in organization");
        }

        Role role = new Role();
        role.setName(request.getName().trim());
        role.setDescription(request.getDescription());
        role.setOrganization(organization);
        role.setPlatformTemplate(false);
        role.setSystemRole(false);
        role.setVersion(1);
        return roleRepository.save(role);
    }

    @Transactional
    public Role updateTenantRole(Long id, Long organizationId, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .filter(r -> organizationId.equals(r.getOrganization() != null ? r.getOrganization().getId() : null))
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + id + " for organization ID: " + organizationId));

        if (role.isSystemRole() && !role.getName().equalsIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Cannot rename system roles");
        }

        if (!role.getName().equalsIgnoreCase(request.getName()) &&
                roleRepository.existsByOrganizationIdAndName(organizationId, request.getName())) {
            throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists in organization");
        }

        role.setName(request.getName().trim());
        role.setDescription(request.getDescription());
        Role saved = roleRepository.save(role);
        evictRolePermissionsCache(id);
        return saved;
    }

    @Transactional
    public void deleteTenantRole(Long id, Long organizationId) {
        Role role = roleRepository.findById(id)
                .filter(r -> organizationId.equals(r.getOrganization() != null ? r.getOrganization().getId() : null))
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + id + " for organization ID: " + organizationId));

        if (role.isSystemRole()) {
            throw new IllegalArgumentException("Cannot delete core system roles");
        }

        // Check if users are assigned to this role
        List<User> assignedUsers = userRepository.findByRoleId(id);
        if (!assignedUsers.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete role because it is currently assigned to " + assignedUsers.size() + " user(s).");
        }

        evictRolePermissionsCache(id);
        roleRepository.delete(role);
    }

    // ── Legacy Compatibility / Shared Mappings CRUD ─────────────────────────────

    public Role createRole(RoleRequest request) {
        return createPlatformTemplate(request);
    }

    public Optional<Role> updateRole(Long id, RoleRequest request) {
        return Optional.of(updatePlatformTemplate(id, request));
    }

    public boolean deleteRole(Long id) {
        try {
            deletePlatformTemplate(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Role Assignment ─────────────────────────────────────────────────────────

    @Transactional
    public boolean assignRole(Long userId, String roleName) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();
        Long orgId = user.getOrganization() != null ? user.getOrganization().getId() : null;

        // Resolve the role within organization or fallback to template
        Optional<Role> optRole = Optional.empty();
        if (orgId != null) {
            optRole = roleRepository.findByOrganizationIdAndName(orgId, roleName.trim());
        }
        if (optRole.isEmpty()) {
            optRole = roleRepository.findByNameAndIsPlatformTemplateTrue(roleName.trim());
        }

        if (optRole.isEmpty()) {
            throw new IllegalArgumentException("Role '" + roleName + "' does not exist.");
        }

        user.setRole(optRole.get());
        user.setRequestedRole(roleName);
        userRepository.save(user);
        evictUserPermissionsCache(user.getUserId());
        return true;
    }

    @Transactional
    public boolean assignRoleById(Long userId, Long roleId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();

        Optional<Role> optRole = roleRepository.findById(roleId);
        if (optRole.isEmpty()) {
            throw new IllegalArgumentException("Role with ID '" + roleId + "' does not exist.");
        }

        // Validate tenant isolation
        Role role = optRole.get();
        if (!role.isPlatformTemplate()) {
            Long userOrgId = user.getOrganization() != null ? user.getOrganization().getId() : null;
            Long roleOrgId = role.getOrganization() != null ? role.getOrganization().getId() : null;
            if (userOrgId == null || !userOrgId.equals(roleOrgId)) {
                throw new IllegalArgumentException("Role with ID '" + roleId + "' does not belong to your organization.");
            }
        }

        user.setRole(role);
        user.setRequestedRole(role.getName());
        userRepository.save(user);
        evictUserPermissionsCache(user.getUserId());
        return true;
    }

    @Transactional
    public boolean assignRoleById(Long userId, Long roleId, Long organizationId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();
        Long userOrgId = user.getOrganization() != null ? user.getOrganization().getId() : null;
        if (userOrgId == null || !userOrgId.equals(organizationId)) {
            throw new IllegalArgumentException("User does not belong to organization ID: " + organizationId);
        }

        return assignRoleById(userId, roleId);
    }

    // ── Permission Management ───────────────────────────────────────────────────

    @Transactional
    public boolean assignPermissionsToRole(Long roleId, List<String> permissionNames) {
        Optional<Role> optRole = roleRepository.findById(roleId);
        if (optRole.isEmpty()) {
            return false;
        }
        Role role = optRole.get();

        Set<Permission> permissionSet = new HashSet<>();
        for (String name : permissionNames) {
            Permission permission = permissionRepository.findByName(name.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Permission '" + name + "' does not exist"));
            permissionSet.add(permission);
        }

        role.setPermissions(permissionSet);
        roleRepository.save(role);
        evictRolePermissionsCache(roleId);
        return true;
    }

    @Transactional
    public boolean assignPermissionIdsToRole(Long roleId, List<Long> permissionIds) {
        Optional<Role> optRole = roleRepository.findById(roleId);
        if (optRole.isEmpty()) {
            return false;
        }
        Role role = optRole.get();

        Set<Permission> permissionSet = new HashSet<>();
        for (Long id : permissionIds) {
            Permission permission = permissionRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Permission with ID '" + id + "' does not exist"));
            permissionSet.add(permission);
        }

        role.setPermissions(permissionSet);
        roleRepository.save(role);
        evictRolePermissionsCache(roleId);
        return true;
    }

    @Transactional
    public boolean revokePermissionFromRole(Long roleId, Long permissionId) {
        Optional<Role> optRole = roleRepository.findById(roleId);
        if (optRole.isEmpty()) {
            return false;
        }
        Role role = optRole.get();
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));

        boolean removed = role.getPermissions().remove(permission);
        if (removed) {
            roleRepository.save(role);
            evictRolePermissionsCache(roleId);
            return true;
        }
        return false;
    }

    public Long getRoleIdByName(String roleName) {
        if (roleName == null) return null;
        return roleRepository.findByName(roleName).map(Role::getId).orElse(null);
    }

    public boolean hasRole(User user, String roleName) {
        if (user == null || user.getRole() == null || roleName == null) {
            return false;
        }
        // Match name directly as IDs are now organization-specific and dynamic
        return roleName.equalsIgnoreCase(user.getRole().getName());
    }

    public boolean hasRoleOrGreater(User user, String targetRoleName) {
        if (user == null || user.getRole() == null || targetRoleName == null) {
            return false;
        }
        // Super admin has maximum clearance
        if ("SUPER_ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            return true;
        }
        // Hierarchy resolution: SUPER_ADMIN > ADMIN > HR/MANAGER > FINANCE > EMPLOYEE
        Map<String, Integer> hierarchy = new HashMap<>();
        hierarchy.put("SUPER_ADMIN", 100);
        hierarchy.put("ADMIN", 80);
        hierarchy.put("HR", 60);
        hierarchy.put("MANAGER", 60);
        hierarchy.put("FINANCE", 40);
        hierarchy.put("EMPLOYEE", 20);

        int userLevel = hierarchy.getOrDefault(user.getRole().getName().toUpperCase(), 0);
        int targetLevel = hierarchy.getOrDefault(targetRoleName.toUpperCase(), 0);

        return userLevel >= targetLevel;
    }

    @Transactional
    public Optional<Role> patchRole(Long id, Map<String, Object> updates) {
        return roleRepository.findById(id).map(role -> {
            if (updates.containsKey("name")) {
                String newName = (String) updates.get("name");
                if (newName == null || newName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Role name cannot be empty");
                }
                if (role.isSystemRole() && !role.getName().equalsIgnoreCase(newName)) {
                    throw new IllegalArgumentException("Cannot rename system roles");
                }
                Long orgId = role.getOrganization() != null ? role.getOrganization().getId() : null;
                if (!role.getName().equalsIgnoreCase(newName) && roleRepository.existsByOrganizationIdAndName(orgId, newName)) {
                    throw new IllegalArgumentException("Role with name '" + newName + "' already exists");
                }
                role.setName(newName.trim());
            }
            if (updates.containsKey("description")) {
                role.setDescription((String) updates.get("description"));
            }
            Role saved = roleRepository.save(role);
            evictRolePermissionsCache(id);
            return saved;
        });
    }

    @Transactional(readOnly = true)
    public TemplateDiffResponse compareRoleToTemplate(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));
        if (role.isPlatformTemplate()) {
            throw new IllegalArgumentException("Role ID: " + roleId + " is already a platform template");
        }
        Role template = roleRepository.findByNameAndIsPlatformTemplateTrue(role.getName())
                .orElseThrow(() -> new IllegalArgumentException("No matching platform template found for role name: " + role.getName()));

        Set<String> rolePerms = role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());
        Set<String> templatePerms = template.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());

        List<String> added = rolePerms.stream().filter(p -> !templatePerms.contains(p)).sorted().collect(Collectors.toList());
        List<String> removed = templatePerms.stream().filter(p -> !rolePerms.contains(p)).sorted().collect(Collectors.toList());

        return new TemplateDiffResponse(added, removed);
    }

    @Transactional
    public void syncRoleWithTemplate(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));
        if (role.isPlatformTemplate()) {
            throw new IllegalArgumentException("Role ID: " + roleId + " is already a platform template");
        }
        Role template = roleRepository.findByNameAndIsPlatformTemplateTrue(role.getName())
                .orElseThrow(() -> new IllegalArgumentException("No matching platform template found for role name: " + role.getName()));

        role.setPermissions(new HashSet<>(template.getPermissions()));
        roleRepository.save(role);
        evictRolePermissionsCache(roleId);
    }

    @Transactional(readOnly = true)
    public List<RoleStatsResponse> getRoleStats(Long organizationId) {
        List<Role> roles = roleRepository.findByOrganizationId(organizationId);
        List<RoleStatsResponse> stats = new ArrayList<>();
        for (Role role : roles) {
            long usersCount = userRepository.findByRoleId(role.getId()).size();
            long permissionsCount = role.getPermissions().size();
            boolean customized = false;
            Optional<Role> templateOpt = roleRepository.findByNameAndIsPlatformTemplateTrue(role.getName());
            if (templateOpt.isPresent()) {
                Set<String> rolePerms = role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());
                Set<String> templatePerms = templateOpt.get().getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());
                customized = !rolePerms.equals(templatePerms);
            } else {
                customized = true;
            }
            stats.add(new RoleStatsResponse(role.getName(), usersCount, permissionsCount, customized));
        }
        return stats;
    }

    @Transactional(readOnly = true)
    public List<User> getRoleUsers(Long roleId) {
        return userRepository.findByRoleId(roleId);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getPermissionStats(Long organizationId) {
        List<Role> roles = roleRepository.findByOrganizationId(organizationId);
        Map<String, Long> stats = new TreeMap<>(); // Sorted keys for stability in tests
        for (Role role : roles) {
            for (Permission perm : role.getPermissions()) {
                stats.put(perm.getName(), stats.getOrDefault(perm.getName(), 0L) + 1);
            }
        }
        return stats;
    }

    @Transactional(readOnly = true)
    public List<RoleStatsResponse> getCustomizations(Long organizationId) {
        return getRoleStats(organizationId).stream()
                .filter(RoleStatsResponse::isCustomized)
                .collect(Collectors.toList());
    }
}
