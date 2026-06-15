package com.example.ems.config;


import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;




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
    private EmployeeRepository employeeRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Permissions
        List<String> permissionNames = Arrays.asList(
                // User Management
                "user.create", "user.read", "user.update", "user.delete", "user.manage",
                // Employee Management
                "employee.create", "employee.read", "employee.update", "employee.delete", "employee.team.read",
                // Attendance
                "attendance.read", "attendance.manage", "attendance.team.read", "attendance.self.read",
                // Leave Management
                "leave.create", "leave.read", "leave.approve", "leave.manage", "leave.team.approve", "leave.self.read",
                // Payroll
                "payroll.read", "payroll.manage", "salary.manage", "payslip.read", "payslip.self.read",
                // Reports
                "reports.view", "reports.hr", "reports.finance", "reports.manager",
                // System
                "system.manage", "role.manage", "permission.manage",
                // Additional
                "recruitment.manage", "task.assign", "performance.review", "expense.manage",
                "profile.read", "profile.update",
                // Onboarding Self-Service
                "onboarding.self.read", "onboarding.self.update", "onboarding.document.upload", 
                "onboarding.document.read.self", "onboarding.self.submit", "employee.onboarding.read.self",
                // New Self Service Permissions
                "document.self.read", "expense.self.read", "performance.self.read", "goal.self.read", "asset.self.read",
                // Enterprise Self-Service Permissions
                "employee.dashboard.read",
                "employee.profile.read", "employee.profile.update",
                "employee.onboarding.read", "employee.onboarding.update", "employee.onboarding.document.upload", "employee.onboarding.document.read", "employee.onboarding.submit",
                "employee.attendance.read", "employee.attendance.create",
                "employee.leave.create", "employee.leave.read", "employee.leave.cancel",
                "employee.payslip.read", "employee.payslip.download",
                "employee.document.read", "employee.document.upload", "employee.document.delete",
                "employee.asset.read", "employee.asset.request",
                "employee.expense.create", "employee.expense.read", "employee.expense.update",
                "employee.performance.read", "employee.performance.self-review.submit",
                "employee.training.read", "employee.training.complete",
                "employee.notification.read", "employee.notification.update",
                "employee.support-ticket.create", "employee.support-ticket.read", "employee.support-ticket.update",
                "employee.goal.read", "employee.goal.update",
                "employee.schedule.read",
                "employee.announcement.read");

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
                "system.manage", "role.manage", "permission.manage", "user.manage",
                "user.create", "user.read", "user.update", "user.delete",
                "employee.create", "employee.read", "employee.update", "employee.delete", "employee.team.read",
                "attendance.read", "attendance.manage", "attendance.team.read", "attendance.self.read",
                "leave.create", "leave.read", "leave.approve", "leave.manage", "leave.team.approve", "leave.self.read",
                "payroll.read", "payroll.manage", "salary.manage", "payslip.read", "payslip.self.read",
                "reports.view", "reports.hr", "reports.finance", "reports.manager",
                "recruitment.manage", "task.assign", "performance.review", "expense.manage",
                "profile.read", "profile.update",
                "onboarding.self.read", "onboarding.self.update", "onboarding.document.upload", 
                "onboarding.document.read.self", "onboarding.self.submit", "employee.onboarding.read.self",
                "document.self.read", "expense.self.read", "performance.self.read", "goal.self.read", "asset.self.read",
                "employee.dashboard.read",
                "employee.profile.read", "employee.profile.update",
                "employee.onboarding.read", "employee.onboarding.update", "employee.onboarding.document.upload", "employee.onboarding.document.read", "employee.onboarding.submit",
                "employee.attendance.read", "employee.attendance.create",
                "employee.leave.create", "employee.leave.read", "employee.leave.cancel",
                "employee.payslip.read", "employee.payslip.download",
                "employee.document.read", "employee.document.upload", "employee.document.delete",
                "employee.asset.read", "employee.asset.request",
                "employee.expense.create", "employee.expense.read", "employee.expense.update",
                "employee.performance.read", "employee.performance.self-review.submit",
                "employee.training.read", "employee.training.complete",
                "employee.notification.read", "employee.notification.update",
                "employee.support-ticket.create", "employee.support-ticket.read", "employee.support-ticket.update",
                "employee.goal.read", "employee.goal.update",
                "employee.schedule.read",
                "employee.announcement.read"));

        rolePermissionsMap.put("ADMIN", Arrays.asList(
                "user.manage",
                "user.create", "user.read", "user.update",
                "employee.create", "employee.read", "employee.update",
                "attendance.manage", "leave.manage", "reports.view"));

        rolePermissionsMap.put("HR", Arrays.asList(
                "employee.create", "employee.read", "employee.update",
                "attendance.read", "leave.approve", "leave.read",
                "recruitment.manage", "reports.hr"));

        rolePermissionsMap.put("MANAGER", Arrays.asList(
                "employee.team.read", "attendance.team.read", "leave.team.approve",
                "task.assign", "performance.review"));

        rolePermissionsMap.put("FINANCE", Arrays.asList(
                "payroll.read", "payroll.manage", "expense.manage",
                "salary.manage", "reports.finance"));

        rolePermissionsMap.put("EMPLOYEE", Arrays.asList(
                "profile.read", "profile.update", "attendance.self.read",
                "leave.create", "leave.self.read", "payslip.read", "payslip.self.read",
                "onboarding.self.read", "onboarding.self.update", "onboarding.document.upload", 
                "onboarding.document.read.self", "onboarding.self.submit", "employee.onboarding.read.self",
                "document.self.read", "expense.self.read", "performance.self.read", "goal.self.read", "asset.self.read",
                "employee.dashboard.read",
                "employee.profile.read", "employee.profile.update",
                "employee.onboarding.read", "employee.onboarding.update", "employee.onboarding.document.upload", "employee.onboarding.document.read", "employee.onboarding.submit",
                "employee.attendance.read", "employee.attendance.create",
                "employee.leave.create", "employee.leave.read", "employee.leave.cancel",
                "employee.payslip.read", "employee.payslip.download",
                "employee.document.read", "employee.document.upload", "employee.document.delete",
                "employee.asset.read", "employee.asset.request",
                "employee.expense.create", "employee.expense.read", "employee.expense.update",
                "employee.performance.read", "employee.performance.self-review.submit",
                "employee.training.read", "employee.training.complete",
                "employee.notification.read", "employee.notification.update",
                "employee.support-ticket.create", "employee.support-ticket.read", "employee.support-ticket.update",
                "employee.goal.read", "employee.goal.update",
                "employee.schedule.read",
                "employee.announcement.read"));

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

        // 3. Seed Default Users dynamically based on roles (except super admin which uses emssuperadmin@gmail.com / Admin@123)
        for (Role role : roleMap.values()) {
            String email;
            String password;
            String displayName;
            
            if ("SUPER_ADMIN".equalsIgnoreCase(role.getName())) {
                email = "emssuperadmin@gmail.com";
                password = "Admin@123";
                displayName = "Super Admin";
            } else {
                String roleCleanName = role.getName().toLowerCase();
                email = roleCleanName + "@company.com";
                password = roleCleanName + "@" + role.getId();
                displayName = role.getName().charAt(0) + role.getName().substring(1).toLowerCase().replace("_", " ");
            }
            
            if (userRepository.findByWorkEmail(email).isEmpty()) {
                User user = new User();
                user.setFullName(displayName + ("SUPER_ADMIN".equalsIgnoreCase(role.getName()) ? "" : " User"));
                user.setWorkEmail(email);
                user.setMobileNumber("SUPER_ADMIN".equalsIgnoreCase(role.getName()) ? "1234567890" : "5550" + String.format("%03d", role.getId()));
                user.setDepartment("SUPER_ADMIN".equalsIgnoreCase(role.getName()) ? "IT" : displayName);
                user.setRequestedRole(role.getName());
                user.setRole(role);
                user.setPassword(passwordEncoder.encode(password));
                user.setLocation("Headquarters");
                userRepository.save(user);

                String userId = "EMP" + String.format("%03d", user.getId());
                user.setUserId(userId);
                userRepository.save(user);
                System.out.println(displayName + " User seeded: " + email + " with ID: " + userId);
            } else {
                User existingUser = userRepository.findByWorkEmail(email).get();
                existingUser.setRole(role);
                existingUser.setPassword(passwordEncoder.encode(password));
                userRepository.save(existingUser);
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

        // 5. Ensure all users have fully-populated Employee records (no null values)
        for (User u : userRepository.findAll()) {
            Employee emp = employeeRepository.findByEmail(u.getWorkEmail())
                    .orElseGet(Employee::new);
            
            emp.setFullName(u.getFullName());
            emp.setEmail(u.getWorkEmail());
            emp.setEmployeeId(u.getUserId());
            if (emp.getPhone() == null) emp.setPhone(u.getMobileNumber() != null ? u.getMobileNumber() : "1234567890");
            if (emp.getGender() == null) emp.setGender("MALE");
            if (emp.getDob() == null) emp.setDob(LocalDate.of(1990, 1, 1));
            if (emp.getAddress() == null) emp.setAddress("123 Corporate Way");
            if (emp.getEmergencyContact() == null) emp.setEmergencyContact("9876543210");
            if (emp.getDepartment() == null) emp.setDepartment(u.getDepartment() != null ? u.getDepartment() : "Engineering");
            if (emp.getDesignation() == null) emp.setDesignation(u.getRole() != null ? u.getRole().getName() : "Software Engineer");
            if (emp.getAnnualSalary() == null) emp.setAnnualSalary(BigDecimal.valueOf(85000));
            if (emp.getJoiningDate() == null) emp.setJoiningDate(LocalDate.of(2026, 6, 10));
            if (emp.getLocation() == null) emp.setLocation(u.getLocation() != null ? u.getLocation() : "Headquarters");
            if (emp.getEmploymentType() == null) emp.setEmploymentType("FULL_TIME");
            if (emp.getStatus() == null || emp.getStatus().isBlank()) emp.setStatus("ACTIVE");
            
            employeeRepository.save(emp);
        }
    }
}
