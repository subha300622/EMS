package com.example.ems.service;

import com.example.ems.dto.PermissionRequest;
import com.example.ems.entity.Permission;
import com.example.ems.repository.PermissionRepository;
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
}
