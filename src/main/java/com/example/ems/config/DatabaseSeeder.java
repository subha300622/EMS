package com.example.ems.config;

import com.example.ems.entity.Permission;
import com.example.ems.entity.Role;
import com.example.ems.entity.User;
import com.example.ems.repository.PermissionRepository;
import com.example.ems.repository.RoleRepository;
import com.example.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Permissions
        List<String> permissionNames = Arrays.asList(
            // User Management
            "user.create", "user.read", "user.update", "user.delete",
            // Employee Management
            "employee.create", "employee.read", "employee.update", "employee.delete", "employee.team.read",
            // Attendance
            "attendance.read", "attendance.manage", "attendance.team.read", "attendance.self.read",
            // Leave Management
            "leave.create", "leave.read", "leave.approve", "leave.manage", "leave.team.approve", "leave.self.read",
            // Payroll
            "payroll.read", "payroll.manage", "salary.manage", "payslip.read",
            // Reports
            "reports.view", "reports.hr", "reports.finance", "reports.manager",
            // System
            "system.manage", "role.manage", "permission.manage",
            // Additional
            "recruitment.manage", "task.assign", "performance.review", "expense.manage",
            "profile.read", "profile.update"
        );

        Map<String, Permission> permissionMap = new HashMap<>();
        for (String permName : permissionNames) {
            Permission permission = permissionRepository.findByName(permName)
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setName(permName);
                    p.setDescription("Permission for " + permName);
                    return permissionRepository.save(p);
                });
            permissionMap.put(permName, permission);
        }

        // 2. Seed Roles and map Permissions
        Map<String, List<String>> rolePermissionsMap = new HashMap<>();
        
        rolePermissionsMap.put("SUPER_ADMIN", Arrays.asList(
            "system.manage", "role.manage", "permission.manage",
            "user.create", "user.read", "user.update", "user.delete",
            "employee.create", "employee.read", "employee.update", "employee.delete",
            "attendance.manage", "leave.manage", "payroll.manage", "reports.view"
        ));

        rolePermissionsMap.put("ADMIN", Arrays.asList(
            "user.create", "user.read", "user.update",
            "employee.create", "employee.read", "employee.update",
            "attendance.manage", "leave.manage", "reports.view"
        ));

        rolePermissionsMap.put("HR", Arrays.asList(
            "employee.create", "employee.read", "employee.update",
            "attendance.read", "leave.approve", "leave.read",
            "recruitment.manage", "reports.hr"
        ));

        rolePermissionsMap.put("MANAGER", Arrays.asList(
            "employee.team.read", "attendance.team.read", "leave.team.approve",
            "task.assign", "performance.review"
        ));

        rolePermissionsMap.put("FINANCE", Arrays.asList(
            "payroll.read", "payroll.manage", "expense.manage",
            "salary.manage", "reports.finance"
        ));

        rolePermissionsMap.put("EMPLOYEE", Arrays.asList(
            "profile.read", "profile.update", "attendance.self.read",
            "leave.create", "leave.self.read", "payslip.read"
        ));

        Map<String, Role> roleMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : rolePermissionsMap.entrySet()) {
            String roleName = entry.getKey();
            List<String> perms = entry.getValue();

            Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(roleName);
                    r.setDescription("Role for " + roleName);
                    return roleRepository.save(r);
                });

            // Set permissions
            Set<Permission> rolePerms = new HashSet<>();
            for (String pName : perms) {
                if (permissionMap.containsKey(pName)) {
                    rolePerms.add(permissionMap.get(pName));
                }
            }
            role.setPermissions(rolePerms);
            roleRepository.save(role);
            roleMap.put(roleName, role);
        }

        // 3. Seed Default Super Admin User (emssuperadmin@gmail.com)
        String adminEmail = "emssuperadmin@gmail.com";
        Role superAdminRole = roleMap.get("SUPER_ADMIN");
        
        Optional<User> optAdmin = userRepository.findByWorkEmail(adminEmail);
        User adminUser;
        if (optAdmin.isEmpty()) {
            adminUser = new User();
            adminUser.setFullName("Super Admin");
            adminUser.setWorkEmail(adminEmail);
            adminUser.setMobileNumber("1234567890");
            adminUser.setDepartment("IT");
            adminUser.setRequestedRole("SUPER_ADMIN");
            adminUser.setRole(superAdminRole);
            adminUser.setPassword(passwordEncoder.encode("Admin@123"));
            adminUser.setLocation("Headquarters");
            userRepository.save(adminUser);

            String userId = "EMP" + String.format("%03d", adminUser.getId());
            adminUser.setUserId(userId);
            userRepository.save(adminUser);
            System.out.println("Super Admin seeded successfully: " + adminEmail + " with ID: " + userId);
        } else {
            adminUser = optAdmin.get();
            if (adminUser.getRole() == null) {
                adminUser.setRole(superAdminRole);
                userRepository.save(adminUser);
            }
        }

        // 4. Migrate existing users with legacy role strings to database roles
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (user.getRole() == null && user.getRequestedRole() != null) {
                String reqRole = user.getRequestedRole().trim().toUpperCase().replace(" ", "_");
                Role targetRole = roleMap.get(reqRole);
                if (targetRole != null) {
                    user.setRole(targetRole);
                    userRepository.save(user);
                    System.out.println("Migrated user " + user.getWorkEmail() + " to role " + targetRole.getName());
                } else {
                    // Try exact mapping fallback
                    if (reqRole.equals("SUPERADMIN")) {
                        user.setRole(roleMap.get("SUPER_ADMIN"));
                        userRepository.save(user);
                    }
                }
            }
        }
    }
}
