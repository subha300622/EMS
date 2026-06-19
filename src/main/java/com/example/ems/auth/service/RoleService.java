package com.example.ems.auth.service;

import com.example.ems.auth.dto.RoleRequest;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * Resolves the effective permissions for a user. If the user's role has no permissions
     * mapped, it falls back to the standard 'EMPLOYEE' permissions.
     */
    public Set<Permission> getEffectivePermissions(User user) {
        if (user == null || user.getRole() == null) {
            return new HashSet<>();
        }
        Set<Permission> perms = user.getRole().getPermissions();
        if (perms == null || perms.isEmpty()) {
            return roleRepository.findByName("EMPLOYEE")
                    .map(Role::getPermissions)
                    .orElse(new HashSet<>());
        }
        return perms;
    }

    /**
     * Checks if a user has a specific permission in the database.
     * SUPER_ADMIN users (those with system.manage) bypass all permission checks.
     */
    public boolean hasPermission(String email, String permissionName) {
        if (email == null || email.trim().isEmpty() || permissionName == null || permissionName.trim().isEmpty()) {
            return false;
        }
        Optional<User> optUser = userRepository.findByWorkEmail(email);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();
        Set<Permission> perms = getEffectivePermissions(user);

        // SUPER_ADMIN bypass: if user has system.manage, allow everything
        boolean isSuperAdmin = perms.stream().anyMatch(p -> "system.manage".equalsIgnoreCase(p.getName()));
        if (isSuperAdmin) {
            return true;
        }

        return perms.stream().anyMatch(permission -> permission.getName().equalsIgnoreCase(permissionName));
    }

    /**
     * Checks if the user with the given email is authorized to manage roles.
     * Permitted if the user has "role.manage" permission.
     */
    public boolean hasRoleManagementPermission(String email) {
        return hasPermission(email, "role.manage");
    }

    /**
     * Checks if the user with the given email is a Super Admin.
     * Permitted if the user has "system.manage" permission.
     */
    public boolean isSuperAdmin(String email) {
        return hasPermission(email, "system.manage");
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    public Role createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists");
        }
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        return roleRepository.save(role);
    }

    public Optional<Role> updateRole(Long id, RoleRequest request) {
        return roleRepository.findById(id).map(role -> {
            if (!role.getName().equalsIgnoreCase(request.getName()) && roleRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists");
            }
            role.setName(request.getName());
            role.setDescription(request.getDescription());
            return roleRepository.save(role);
        });
    }

    public Optional<Role> patchRole(Long id, java.util.Map<String, Object> updates) {
        return roleRepository.findById(id).map(role -> {
            if (updates.containsKey("name")) {
                String newName = (String) updates.get("name");
                if (newName == null || newName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Role name cannot be empty");
                }
                if (!role.getName().equalsIgnoreCase(newName) && roleRepository.existsByName(newName)) {
                    throw new IllegalArgumentException("Role with name '" + newName + "' already exists");
                }
                role.setName(newName);
            }
            if (updates.containsKey("description")) {
                role.setDescription((String) updates.get("description"));
            }
            return roleRepository.save(role);
        });
    }


    public boolean deleteRole(Long id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean assignRole(Long userId, String roleName) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();

        // Resolve the role entity by name and assign the FK
        Optional<Role> optRole = roleRepository.findByName(roleName.trim().toUpperCase().replace(" ", "_"));
        if (optRole.isEmpty()) {
            optRole = roleRepository.findByName(roleName.trim());
        }
        if (optRole.isEmpty()) {
            throw new IllegalArgumentException("Role '" + roleName + "' does not exist. Use GET /api/v1/roles to find valid role names.");
        }
        user.setRole(optRole.get());
        user.setRequestedRole(roleName);
        userRepository.save(user);
        return true;
    }

    /**
     * Assigns a role to a user by its numeric Role ID.
     */
    public boolean assignRoleById(Long userId, Long roleId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();

        Optional<Role> optRole = roleRepository.findById(roleId);
        if (optRole.isEmpty()) {
            throw new IllegalArgumentException("Role with ID '" + roleId + "' does not exist. Use GET /api/v1/roles to find valid role IDs.");
        }
        user.setRole(optRole.get());
        user.setRequestedRole(optRole.get().getName());
        userRepository.save(user);
        return true;
    }

    public boolean assignPermissionsToRole(Long roleId, List<String> permissionNames) {
        Optional<Role> optRole = roleRepository.findById(roleId);
        if (optRole.isEmpty()) {
            return false;
        }
        Role role = optRole.get();

        Set<Permission> permissionSet = new HashSet<>();
        for (String name : permissionNames) {
            Permission permission = permissionRepository.findByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("Permission '" + name + "' does not exist"));
            permissionSet.add(permission);
        }

        role.setPermissions(permissionSet);
        roleRepository.save(role);
        return true;
    }

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
        return true;
    }

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
            return true;
        }
        return false;
    }

    /**
     * Resolves a role ID by its name dynamically to avoid hardcoding role IDs.
     */
    public Long getRoleIdByName(String roleName) {
        if (roleName == null) return null;
        return roleRepository.findByName(roleName).map(Role::getId).orElse(null);
    }

    /**
     * Checks if user has a role matching the resolved role ID.
     */
    public boolean hasRole(User user, String roleName) {
        if (user == null || user.getRole() == null || roleName == null) {
            return false;
        }
        Long targetRoleId = getRoleIdByName(roleName);
        return targetRoleId != null && targetRoleId.equals(user.getRole().getId());
    }

    /**
     * Checks if the user's role authority (hierarchical level) is greater than or equal to the target role.
     * Greater authority level corresponds to a smaller or equal role ID in the database.
     */
    public boolean hasRoleOrGreater(User user, String targetRoleName) {
        if (user == null || user.getRole() == null || targetRoleName == null) {
            return false;
        }
        Long userRoleId = user.getRole().getId();
        Long targetRoleId = getRoleIdByName(targetRoleName);
        if (userRoleId == null || targetRoleId == null) {
            return false;
        }
        return userRoleId <= targetRoleId;
    }
}

