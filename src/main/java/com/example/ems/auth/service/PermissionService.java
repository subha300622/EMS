package com.example.ems.auth.service;

import com.example.ems.auth.dto.PermissionRequest;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.repository.PermissionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public java.util.Optional<Permission> getPermissionById(Long id) {
        return permissionRepository.findById(id);
    }

    public java.util.Optional<Permission> updatePermission(Long id, PermissionRequest request) {
        return permissionRepository.findById(id).map(permission -> {
            if (!permission.getName().equalsIgnoreCase(request.getName()) && permissionRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Permission with name '" + request.getName() + "' already exists");
            }
            permission.setName(request.getName());
            permission.setDescription(request.getDescription());
            return permissionRepository.save(permission);
        });
    }

    public boolean deletePermission(Long id) {
        if (permissionRepository.existsById(id)) {
            permissionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
