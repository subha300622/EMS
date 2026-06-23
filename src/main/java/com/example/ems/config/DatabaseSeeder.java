package com.example.ems.config;

import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
@Order(1)
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

    @org.springframework.beans.factory.annotation.Value("${app.seed.domain:company.com}")
    private String seedDomain;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DatabaseSeeder: Seeding permissions, roles, and core configuration...");

        // 1. Seed Permissions from PermissionRegistry
        Map<String, Permission> permissionMap = new HashMap<>();
        for (String permName : PermissionRegistry.ALL_PERMISSIONS) {
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
        rolePermissionsMap.put("SUPER_ADMIN", PermissionRegistry.SUPER_ADMIN_PERMS);
        rolePermissionsMap.put("ADMIN", PermissionRegistry.ADMIN_PERMS);
        rolePermissionsMap.put("HR", PermissionRegistry.HR_PERMS);
        rolePermissionsMap.put("MANAGER", PermissionRegistry.MANAGER_PERMS);
        rolePermissionsMap.put("FINANCE", PermissionRegistry.FINANCE_PERMS);
        rolePermissionsMap.put("EMPLOYEE", PermissionRegistry.EMPLOYEE_SELF_PERMS);

        Map<String, Role> roleMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : rolePermissionsMap.entrySet()) {
            String roleName = entry.getKey();
            List<String> perms = new ArrayList<>(entry.getValue());
            perms.addAll(PermissionRegistry.COMMON_SETTINGS_PERMS);

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

        // 3. Seed SUPER_ADMIN default user
        Role superAdminRole = roleMap.get("SUPER_ADMIN");
        if (superAdminRole != null) {
            String roleCleanName = "super_admin";
            String email = roleCleanName + "@" + seedDomain;
            String password = roleCleanName + "@" + superAdminRole.getId();
            String displayName = "Super Admin";

            User superAdminUser;
            if (userRepository.findByWorkEmail(email).isEmpty()) {
                superAdminUser = new User();
                superAdminUser.setFullName(displayName);
                superAdminUser.setWorkEmail(email);
                superAdminUser.setMobileNumber("1234567890");
                superAdminUser.setDepartment("IT");
                superAdminUser.setRequestedRole("SUPER_ADMIN");
                superAdminUser.setRole(superAdminRole);
                superAdminUser.setPassword(passwordEncoder.encode(password));
                superAdminUser.setLocation("Headquarters");
                userRepository.save(superAdminUser);

                String userId = "EMP" + String.format("%03d", superAdminUser.getId());
                superAdminUser.setUserId(userId);
                userRepository.save(superAdminUser);
                System.out.println("DatabaseSeeder: " + displayName + " User seeded: " + email + " with ID: " + userId);
            } else {
                superAdminUser = userRepository.findByWorkEmail(email).get();
                superAdminUser.setRole(superAdminRole);
                superAdminUser.setPassword(passwordEncoder.encode(password));
                userRepository.save(superAdminUser);
            }

            // Ensure Super Admin has fully-populated Employee record
            Employee emp = employeeRepository.findByEmail(superAdminUser.getWorkEmail())
                    .orElseGet(Employee::new);

            emp.setFullName(superAdminUser.getFullName());
            emp.setEmail(superAdminUser.getWorkEmail());
            emp.setEmployeeId(superAdminUser.getUserId());
            if (emp.getPhone() == null)
                emp.setPhone(
                        superAdminUser.getMobileNumber() != null ? superAdminUser.getMobileNumber() : "1234567890");
            if (emp.getGender() == null)
                emp.setGender("MALE");
            if (emp.getDob() == null)
                emp.setDob(LocalDate.of(1990, 1, 1));
            if (emp.getAddress() == null)
                emp.setAddress("123 Corporate Way");
            if (emp.getEmergencyContact() == null)
                emp.setEmergencyContact("9876543210");
            if (emp.getDepartment() == null)
                emp.setDepartment(superAdminUser.getDepartment() != null ? superAdminUser.getDepartment() : "IT");
            if (emp.getDesignation() == null)
                emp.setDesignation("SUPER_ADMIN");
            if (emp.getAnnualSalary() == null)
                emp.setAnnualSalary(BigDecimal.valueOf(150000));
            if (emp.getJoiningDate() == null)
                emp.setJoiningDate(LocalDate.of(2026, 6, 10));
            if (emp.getLocation() == null)
                emp.setLocation(superAdminUser.getLocation() != null ? superAdminUser.getLocation() : "Headquarters");
            if (emp.getEmploymentType() == null)
                emp.setEmploymentType("FULL_TIME");
            if (emp.getStatus() == null || emp.getStatus().isBlank())
                emp.setStatus("ACTIVE");

            employeeRepository.save(emp);
        }

        // 4. Migrate existing users (production mapping)
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (user.getRole() == null && user.getRequestedRole() != null) {
                String reqRole = user.getRequestedRole().trim().toUpperCase().replace(" ", "_");
                Role targetRole = roleMap.get(reqRole);
                if (targetRole != null) {
                    user.setRole(targetRole);
                    userRepository.save(user);
                }
            }
        }
    }
}
