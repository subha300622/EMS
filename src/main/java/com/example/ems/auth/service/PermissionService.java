package com.example.ems.auth.service;

import com.example.ems.auth.dto.PermissionRequest;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.repository.PermissionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import com.example.ems.auth.dto.PermissionCatalogResponseDto;
import com.example.ems.auth.dto.PermissionGroupDto;
import com.example.ems.auth.dto.PermissionResponse;
import com.example.ems.auth.entity.PermissionGroup;
import com.example.ems.auth.repository.PermissionGroupRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public Permission createPermission(PermissionRequest request) {
        if (permissionRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Permission with name '" + request.getName() + "' already exists");
        }
        Permission permission = new Permission();
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        return permissionRepository.save(permission);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Optional<Permission> getPermissionById(Long id) {
        return permissionRepository.findById(id);
    }

    public Optional<Permission> updatePermission(Long id, PermissionRequest request) {
        return permissionRepository.findById(id).map(permission -> {
            if (!permission.getName().equalsIgnoreCase(request.getName()) && permissionRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Permission with name '" + request.getName() + "' already exists");
            }
            permission.setName(request.getName());
            permission.setDescription(request.getDescription());
            return permissionRepository.save(permission);
        });
    }

    public Permission savePermission(Permission permission) {
        return permissionRepository.save(permission);
    }

    public boolean deletePermission(Long id) {
        if (permissionRepository.existsById(id)) {
            permissionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    public PermissionCatalogResponseDto getPermissionCatalog() {
        List<PermissionGroup> groups = permissionGroupRepository.findAll();
        List<PermissionGroupDto> groupDtos = new ArrayList<>();
        Set<Long> groupedPermissionIds = new HashSet<>();

        for (PermissionGroup group : groups) {
            List<PermissionResponse> permDtos = new ArrayList<>();
            for (Permission p : group.getPermissions()) {
                groupedPermissionIds.add(p.getId());
                permDtos.add(new PermissionResponse(p.getId(), p.getName(), p.getDescription()));
            }
            groupDtos.add(new PermissionGroupDto(
                group.getId(),
                group.getCode(),
                group.getName(),
                group.getDescription(),
                permDtos
            ));
        }

        List<PermissionResponse> standalonePerms = new ArrayList<>();
        for (Permission p : permissionRepository.findAll()) {
            if (!groupedPermissionIds.contains(p.getId())) {
                standalonePerms.add(new PermissionResponse(p.getId(), p.getName(), p.getDescription()));
            }
        }

        return new PermissionCatalogResponseDto(groupDtos, standalonePerms);
    }
}
