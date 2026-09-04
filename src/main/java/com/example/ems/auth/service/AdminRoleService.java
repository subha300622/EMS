package com.example.ems.auth.service;

import com.example.ems.auth.dto.AdminRoleDtos;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.util.RoleIdResolver;
import com.example.ems.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminRoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Transactional(readOnly = true)
    public AdminRoleDtos.RolePageResponse listRoles(int page, int size, String search, String status) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;

        List<Role> allRoles = roleRepository.findAll();

        final String searchFilter = (search != null && !search.isBlank()) ? search.trim().toLowerCase() : null;
        final String statusFilter = (status != null && !status.isBlank()) ? status.trim().toUpperCase() : null;

        List<Role> filtered = allRoles.stream()
                .filter(role -> {
                    if (searchFilter != null) {
                        boolean matchName = role.getName() != null && role.getName().toLowerCase().contains(searchFilter);
                        boolean matchDesc = role.getDescription() != null && role.getDescription().toLowerCase().contains(searchFilter);
                        if (!matchName && !matchDesc) {
                            return false;
                        }
                    }
                    if (statusFilter != null) {
                        String currentStatus = role.getStatus() != null ? role.getStatus().toUpperCase() : "ACTIVE";
                        if (!currentStatus.equals(statusFilter)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0) totalPages = 1;

        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<Role> pagedList = filtered.subList(fromIndex, toIndex);

        List<AdminRoleDtos.RoleResponse> content = pagedList.stream()
                .map(this::mapToFullResponse)
                .collect(Collectors.toList());

        return AdminRoleDtos.RolePageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    @Transactional
    public AdminRoleDtos.RoleResponse createRole(AdminRoleDtos.CreateRoleRequest request) {
        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new IllegalArgumentException("Role name cannot be blank");
        }
        String trimmedName = request.getRole().trim();

        if (roleRepository.findAll().stream().anyMatch(r -> r.getName().equalsIgnoreCase(trimmedName))) {
            throw new IllegalArgumentException("Role with name '" + trimmedName + "' already exists");
        }

        Role role = new Role();
        role.setName(trimmedName);
        role.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        role.setStatus("ACTIVE");
        role.setSystemRole(false);

        Role saved = roleRepository.save(role);

        return AdminRoleDtos.RoleResponse.builder()
                .roleId(RoleIdResolver.formatRoleId(saved.getId()))
                .role(saved.getName())
                .description(saved.getDescription())
                .status(saved.getStatus())
                .systemRole(saved.isSystemRole())
                .createdAt(saved.getCreatedAt() != null ? ISO_FORMATTER.format(saved.getCreatedAt()) : null)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminRoleDtos.RoleResponse getRole(String roleIdStr) {
        Long id = RoleIdResolver.parseRoleId(roleIdStr);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleIdStr));

        return mapToFullResponse(role);
    }

    @Transactional
    public AdminRoleDtos.RoleResponse editRole(String roleIdStr, AdminRoleDtos.EditRoleRequest request) {
        Long id = RoleIdResolver.parseRoleId(roleIdStr);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleIdStr));

        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new IllegalArgumentException("Role name cannot be blank");
        }
        String trimmedName = request.getRole().trim();

        if (role.isSystemRole() && !role.getName().equalsIgnoreCase(trimmedName)) {
            throw new IllegalArgumentException("Cannot rename system roles");
        }

        if (!role.getName().equalsIgnoreCase(trimmedName) &&
                roleRepository.findAll().stream().anyMatch(r -> !r.getId().equals(id) && r.getName().equalsIgnoreCase(trimmedName))) {
            throw new IllegalArgumentException("Role with name '" + trimmedName + "' already exists");
        }

        role.setName(trimmedName);
        role.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Role saved = roleRepository.save(role);

        return AdminRoleDtos.RoleResponse.builder()
                .roleId(RoleIdResolver.formatRoleId(saved.getId()))
                .role(saved.getName())
                .description(saved.getDescription())
                .status(saved.getStatus())
                .systemRole(saved.isSystemRole())
                .updatedAt(saved.getUpdatedAt() != null ? ISO_FORMATTER.format(saved.getUpdatedAt()) : null)
                .build();
    }

    @Transactional
    public AdminRoleDtos.RoleResponse duplicateRole(String roleIdStr, AdminRoleDtos.CreateRoleRequest request) {
        Long id = RoleIdResolver.parseRoleId(roleIdStr);
        Role sourceRole = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleIdStr));

        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new IllegalArgumentException("Role name cannot be blank");
        }
        String trimmedName = request.getRole().trim();

        if (roleRepository.findAll().stream().anyMatch(r -> r.getName().equalsIgnoreCase(trimmedName))) {
            throw new IllegalArgumentException("Role with name '" + trimmedName + "' already exists");
        }

        Role newRole = new Role();
        newRole.setName(trimmedName);
        newRole.setDescription(request.getDescription() != null ? request.getDescription().trim() : sourceRole.getDescription());
        newRole.setStatus("ACTIVE");
        newRole.setSystemRole(false);

        Role saved = roleRepository.save(newRole);

        return AdminRoleDtos.RoleResponse.builder()
                .roleId(RoleIdResolver.formatRoleId(saved.getId()))
                .role(saved.getName())
                .description(saved.getDescription())
                .status(saved.getStatus())
                .systemRole(saved.isSystemRole())
                .userCount(0)
                .createdAt(saved.getCreatedAt() != null ? ISO_FORMATTER.format(saved.getCreatedAt()) : null)
                .build();
    }

    @Transactional
    public AdminRoleDtos.RoleResponse updateRoleStatus(String roleIdStr, AdminRoleDtos.RoleStatusRequest request) {
        Long id = RoleIdResolver.parseRoleId(roleIdStr);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleIdStr));

        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new IllegalArgumentException("Status cannot be blank");
        }
        String upperStatus = request.getStatus().trim().toUpperCase();
        if (!"ACTIVE".equals(upperStatus) && !"INACTIVE".equals(upperStatus)) {
            throw new IllegalArgumentException("Invalid status: " + request.getStatus() + ". Valid values are ACTIVE, INACTIVE");
        }

        if (role.isSystemRole() && "INACTIVE".equals(upperStatus)) {
            throw new IllegalArgumentException("System roles cannot be deactivated");
        }

        role.setStatus(upperStatus);
        Role saved = roleRepository.save(role);

        return AdminRoleDtos.RoleResponse.builder()
                .roleId(RoleIdResolver.formatRoleId(saved.getId()))
                .role(saved.getName())
                .status(saved.getStatus())
                .updatedAt(saved.getUpdatedAt() != null ? ISO_FORMATTER.format(saved.getUpdatedAt()) : null)
                .build();
    }

    @Transactional
    public AdminRoleDtos.RoleDeleteResponse deleteRole(String roleIdStr) {
        Long id = RoleIdResolver.parseRoleId(roleIdStr);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleIdStr));

        if (role.isSystemRole()) {
            throw new IllegalStateException("Role cannot be deleted because it is a system role");
        }

        int assignedUsers = userRepository.findByRoleId(id).size();
        if (assignedUsers > 0) {
            throw new IllegalStateException("Role cannot be deleted because it is assigned to users");
        }

        roleRepository.delete(role);

        return AdminRoleDtos.RoleDeleteResponse.builder()
                .message("Role deleted successfully")
                .roleId(RoleIdResolver.formatRoleId(id))
                .build();
    }

    private AdminRoleDtos.RoleResponse mapToFullResponse(Role role) {
        int userCount = userRepository.findByRoleId(role.getId()).size();
        return AdminRoleDtos.RoleResponse.builder()
                .roleId(RoleIdResolver.formatRoleId(role.getId()))
                .role(role.getName())
                .description(role.getDescription())
                .status(role.getStatus() != null ? role.getStatus() : "ACTIVE")
                .systemRole(role.isSystemRole())
                .userCount(userCount)
                .createdAt(role.getCreatedAt() != null ? ISO_FORMATTER.format(role.getCreatedAt()) : null)
                .updatedAt(role.getUpdatedAt() != null ? ISO_FORMATTER.format(role.getUpdatedAt()) : (role.getCreatedAt() != null ? ISO_FORMATTER.format(role.getCreatedAt()) : null))
                .build();
    }
}
