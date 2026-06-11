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
     * Checks if a user has a specific permission in the database.
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
        if (user.getRole() == null || user.getRole().getPermissions() == null) {
            return false;
        }
        return user.getRole().getPermissions().stream()
                .anyMatch(permission -> permission.getName().equalsIgnoreCase(permissionName));
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
        user.setRequestedRole(roleName);
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
}
