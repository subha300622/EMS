package com.example.ems.config;


import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.entity.Offboarding;
import com.example.ems.payroll.entity.FnfSettlement;
import com.example.ems.asset.entity.MyAsset;
import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.entity.ExpenseCategory;
import com.example.ems.expense.entity.ExpenseStatus;
import com.example.ems.expense.entity.ExpenseAuditLog;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;




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

    @Autowired
    private com.example.ems.support.service.MySupportService mySupportService;

    @Autowired
    private com.example.ems.asset.service.MyAssetService myAssetService;

    @Autowired
    private com.example.ems.payroll.repository.FnfSettlementRepository fnfSettlementRepository;

    @Autowired
    private com.example.ems.payroll.repository.FnfSettlementAuditRepository fnfSettlementAuditRepository;

    @Autowired
    private com.example.ems.offboarding.repository.OffboardingRepository offboardingRepository;

    @Autowired
    private com.example.ems.asset.repository.MyAssetRepository myAssetRepository;

    @Autowired
    private com.example.ems.expense.repository.ExpenseRepository expenseRepository;

    @Autowired
    private com.example.ems.expense.repository.ExpenseCategoryRepository expenseCategoryRepository;

    @Autowired
    private com.example.ems.expense.repository.MyExpenseTimelineEventRepository timelineEventRepository;

    @Autowired
    private com.example.ems.expense.repository.MyExpenseReceiptRepository receiptRepository;

    @Autowired
    private com.example.ems.expense.repository.MyExpenseApprovalStepRepository approvalStepRepository;

    @Autowired
    private com.example.ems.expense.repository.ExpenseAuditLogRepository expenseAuditLogRepository;

    @org.springframework.beans.factory.annotation.Value("${app.seed.domain:company.com}")
    private String seedDomain;

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
                "employee.announcement.read",
                // My Performance Permissions
                "performance.self.goal.update", "performance.self.assessment.submit", 
                "performance.self.feedback.read", "performance.self.history.read",
                "schedule.self.read", "schedule.self.change.create", "schedule.self.availability.update",
                "schedule.self.notification.read", "schedule.self.timeline.read",
                "employee.directory.read", "employee.message.create", "employee.contact.read",
                "employee.team.hierarchy.read", "employee.directory.manage", "employee.report.read",
                // Support Ticket Permissions
                "support.self.create", "support.self.read", "support.self.comment.create", "support.self.close",
                // Goals Module Permissions
                "goal.create", "goal.read", "goal.update", "goal.delete", "goal.self.update", "goal.submit", "goal.approve", "goal.reject", "goal.analytics.read",
                // Settings Module Permissions
                "settings.self.read", "settings.security.read", "settings.security.update", 
                "settings.privacy.read", "settings.privacy.update", "settings.notifications.read", 
                "settings.notifications.update", "settings.appearance.read", "settings.appearance.update", 
                "settings.language.read", "settings.language.update", "settings.devices.read", 
                "settings.devices.remove", "settings.data.export", "settings.support.create", 
                "settings.support.read",
                // Enterprise Module Permissions
                "audit.read", "audit.export", "settings.manage", "team.read", "asset.manage",
                "fnf.manage", "payroll-settings.manage", "announcement.manage");

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
                "employee.announcement.read",
                "performance.self.goal.update", "performance.self.assessment.submit", 
                "performance.self.feedback.read", "performance.self.history.read",
                "support.self.create", "support.self.read", "support.self.comment.create", "support.self.close",
                "goal.create", "goal.read", "goal.update", "goal.delete", "goal.self.update", "goal.submit", "goal.approve", "goal.reject", "goal.analytics.read",
                "audit.read", "audit.export", "settings.manage", "team.read", "asset.manage", "fnf.manage", "payroll-settings.manage", "announcement.manage"));

        rolePermissionsMap.put("ADMIN", Arrays.asList(
                "user.manage",
                "user.create", "user.read", "user.update", "user.role.assign",
                "employee.create", "employee.read", "employee.update",
                "attendance.manage", "leave.manage", "reports.view",
                "payroll.read", "payroll.manage", "salary.manage", "payslip.read",
                "expense.manage",
                "goal.create", "goal.read", "goal.update", "goal.delete", "goal.approve", "goal.reject", "goal.analytics.read",
                "performance.review",
                "role.manage", "permission.manage",
                "audit.read", "audit.export", "settings.manage", "team.read", "asset.manage", "fnf.manage", "payroll-settings.manage", "announcement.manage"));

        rolePermissionsMap.put("HR", Arrays.asList(
                "employee.create", "employee.read", "employee.update",
                "attendance.read", "leave.approve", "leave.read",
                "recruitment.manage", "reports.hr",
                "goal.create", "goal.read", "goal.update", "goal.delete", "goal.self.read", "goal.self.update", "goal.submit", "goal.approve", "goal.reject", "goal.analytics.read",
                "team.read", "asset.manage", "fnf.manage", "announcement.manage"));

        rolePermissionsMap.put("MANAGER", Arrays.asList(
                "employee.read", "employee.team.read", "attendance.read", "attendance.team.read", "leave.team.approve",
                "task.assign", "performance.review",
                "employee.directory.read", "employee.profile.read", "employee.message.create",
                "employee.contact.read", "employee.team.hierarchy.read",
                "goal.create", "goal.read", "goal.update", "goal.self.read", "goal.self.update", "goal.submit", "goal.approve", "goal.reject", "goal.analytics.read",
                "team.read"));

        rolePermissionsMap.put("FINANCE", Arrays.asList(
                "payroll.read", "payroll.manage", "expense.manage",
                "salary.manage", "reports.finance",
                "payslip.read", "employee.payslip.read", "employee.payslip.download",
                "payslip.self.read", "payslip.self.preview", "payslip.self.download",
                "fnf.manage", "payroll-settings.manage", "team.read"));

        rolePermissionsMap.put("EMPLOYEE", Arrays.asList(
                "attendance.self.read",
                "leave.create", "leave.self.read", "payslip.self.read",
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
                "employee.announcement.read",
                "performance.self.goal.update", "performance.self.assessment.submit", 
                "performance.self.feedback.read", "performance.self.history.read",
                "schedule.self.read", "schedule.self.change.create", "schedule.self.availability.update",
                "schedule.self.notification.read", "schedule.self.timeline.read",
                "employee.directory.read", "employee.profile.read", "employee.message.create",
                "employee.contact.read",
                "support.self.create", "support.self.read", "support.self.comment.create", "support.self.close",
                "goal.self.update", "goal.submit"));

        List<String> settingsPerms = Arrays.asList(
                "settings.self.read", "settings.security.read", "settings.security.update", 
                "settings.privacy.read", "settings.privacy.update", "settings.notifications.read", 
                "settings.notifications.update", "settings.appearance.read", "settings.appearance.update", 
                "settings.language.read", "settings.language.update", "settings.devices.read", 
                "settings.devices.remove", "settings.data.export", "settings.support.create", 
                "settings.support.read");

        Map<String, Role> roleMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : rolePermissionsMap.entrySet()) {
            String roleName = entry.getKey();
            List<String> perms = new ArrayList<>(entry.getValue());
            perms.addAll(settingsPerms);

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

        // 3. Seed Default Users
        for (Role role : roleMap.values()) {
            String roleCleanName = role.getName().toLowerCase();
            String email = roleCleanName + "@" + seedDomain;
            String password = roleCleanName + "@" + role.getId();
            String displayName = role.getName().charAt(0) + role.getName().substring(1).toLowerCase().replace("_", " ");
            
            if ("SUPER_ADMIN".equalsIgnoreCase(role.getName())) {
                displayName = "Super Admin";
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

        // 4. Migrate existing users
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

        // 5. Ensure all users have fully-populated Employee records
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

        // 6. Seed support data (categories, KB articles, sample tickets) for employee
        String employeeEmail = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "EMPLOYEE".equalsIgnoreCase(u.getRole().getName()))
                .map(User::getWorkEmail)
                .findFirst()
                .orElse("employee@" + seedDomain);
        mySupportService.seedSupportData(employeeEmail);

        // 7. Seed asset cost report data
        myAssetService.seedDatabase();

        // 8. Seed F&F settlements data
        seedSettlements();

        // 9. Seed Expense Approvals data
        seedExpenses();
    }

    private void seedSettlements() {
        if (fnfSettlementRepository.count() > 0) {
            return;
        }

        // 1. Seed Ravi Kumar
        Employee ravi = employeeRepository.findByEmail("ravi@company.com")
                .orElseGet(() -> {
                    Employee e = new Employee();
                    e.setFullName("Ravi Kumar");
                    e.setEmail("ravi@company.com");
                    e.setEmployeeId("EMP101");
                    e.setPhone("1234567890");
                    e.setGender("MALE");
                    e.setDob(LocalDate.of(1990, 1, 1));
                    e.setAddress("123 Corporate Way");
                    e.setEmergencyContact("9876543210");
                    e.setDepartment("Marketing");
                    e.setDesignation("Marketing Lead");
                    e.setAnnualSalary(BigDecimal.valueOf(1400000));
                    e.setJoiningDate(LocalDate.of(2023, 5, 15));
                    e.setStatus("ACTIVE");
                    return employeeRepository.save(e);
                });

        // Seed Offboarding for Ravi
        if (offboardingRepository.findByEmployeeId(ravi.getId()).isEmpty()) {
            Offboarding ob = new Offboarding();
            ob.setEmployee(ravi);
            ob.setStatus("APPROVED");
            ob.setExitDate(LocalDate.of(2026, 5, 15));
            ob.setRequestedLastWorkingDay(LocalDate.of(2026, 5, 15));
            offboardingRepository.save(ob);
        }

        // Seed FnfSettlement for Ravi
        FnfSettlement raviSett = new FnfSettlement();
        raviSett.setEmployeeId(ravi.getId());
        raviSett.setUnpaidSalary(BigDecimal.valueOf(120000));
        raviSett.setGratuity(BigDecimal.valueOf(58000));
        raviSett.setNoticePay(BigDecimal.valueOf(44000));
        raviSett.setOtherDeductions(BigDecimal.valueOf(15000));
        raviSett.setNetAmount(BigDecimal.valueOf(225000));
        raviSett.setStatus(com.example.ems.payroll.entity.FnfSettlementStatus.PENDING);
        raviSett.setNotes("Full & Final Settlement for Ravi Kumar");
        raviSett = fnfSettlementRepository.save(raviSett);

        // Seed Audit log for Ravi
        fnfSettlementAuditRepository.save(new com.example.ems.payroll.entity.FnfSettlementAudit(
                raviSett.getId(),
                com.example.ems.payroll.entity.FnfSettlementStatus.PENDING,
                "HR Admin",
                "Settlement created automatically during offboarding approval."
        ));

        // Seed Assets for Ravi
        List<MyAsset> raviAssets = myAssetRepository.findByAssignedToId(ravi.getId());
        if (raviAssets.isEmpty()) {
            // Dell Latitude (assigned, recovery 10000)
            MyAsset laptop = new MyAsset();
            laptop.setAssetCode("AST-RAVI-01");
            laptop.setAssetName("Dell Latitude");
            laptop.setCategory("LAPTOP");
            laptop.setBrand("Dell");
            laptop.setModel("Latitude 5400");
            laptop.setSerialNumber("CN-RAVI-5400");
            laptop.setPurchaseDate(LocalDate.now().minusYears(2));
            laptop.setPurchasePrice(BigDecimal.valueOf(80000));
            laptop.setCurrentValue(BigDecimal.valueOf(40000));
            laptop.setAssetRecoveryAmount(BigDecimal.valueOf(10000));
            laptop.setAssignedTo(ravi);
            laptop.setAssignedDate(LocalDate.now().minusYears(2));
            laptop.setStatus("ASSIGNED");
            myAssetRepository.save(laptop);

            // Seed 3 returned assets for Ravi
            for (int i = 1; i <= 3; i++) {
                MyAsset retAsset = new MyAsset();
                retAsset.setAssetCode("AST-RAVI-RET-" + i);
                retAsset.setAssetName("Mock Accessory " + i);
                retAsset.setCategory("ACCESSORIES");
                retAsset.setBrand("Logitech");
                retAsset.setModel("MX Keyboard");
                retAsset.setSerialNumber("CN-RAVI-RET-" + i);
                retAsset.setPurchaseDate(LocalDate.now().minusYears(1));
                retAsset.setPurchasePrice(BigDecimal.valueOf(10000));
                retAsset.setCurrentValue(BigDecimal.valueOf(8000));
                retAsset.setAssetRecoveryAmount(BigDecimal.ZERO);
                retAsset.setAssignedTo(ravi);
                retAsset.setStatus("RETURNED");
                myAssetRepository.save(retAsset);
            }
        }

        // 2. Seed Priya Sharma
        Employee priya = employeeRepository.findByEmail("priya@company.com")
                .orElseGet(() -> {
                    Employee e = new Employee();
                    e.setFullName("Priya Sharma");
                    e.setEmail("priya@company.com");
                    e.setEmployeeId("EMP102");
                    e.setPhone("0987654321");
                    e.setGender("FEMALE");
                    e.setDob(LocalDate.of(1992, 5, 10));
                    e.setAddress("456 Corporate Road");
                    e.setEmergencyContact("9876543211");
                    e.setDepartment("HR");
                    e.setDesignation("HR Specialist");
                    e.setAnnualSalary(BigDecimal.valueOf(800000));
                    e.setJoiningDate(LocalDate.of(2024, 8, 1));
                    e.setStatus("ACTIVE");
                    return employeeRepository.save(e);
                });

        // Seed Offboarding for Priya
        if (offboardingRepository.findByEmployeeId(priya.getId()).isEmpty()) {
            Offboarding ob = new Offboarding();
            ob.setEmployee(priya);
            ob.setStatus("PENDING");
            ob.setExitDate(LocalDate.of(2026, 6, 30));
            ob.setRequestedLastWorkingDay(LocalDate.of(2026, 6, 30));
            offboardingRepository.save(ob);
        }

        // Seed FnfSettlement for Priya
        FnfSettlement priyaSett = new FnfSettlement();
        priyaSett.setEmployeeId(priya.getId());
        priyaSett.setUnpaidSalary(BigDecimal.valueOf(80000));
        priyaSett.setGratuity(BigDecimal.valueOf(25000));
        priyaSett.setNoticePay(BigDecimal.valueOf(0));
        priyaSett.setOtherDeductions(BigDecimal.valueOf(5000));
        priyaSett.setNetAmount(BigDecimal.valueOf(100000));
        priyaSett.setStatus(com.example.ems.payroll.entity.FnfSettlementStatus.PENDING);
        priyaSett.setNotes("Full & Final Settlement for Priya Sharma");
        priyaSett = fnfSettlementRepository.save(priyaSett);

        fnfSettlementAuditRepository.save(new com.example.ems.payroll.entity.FnfSettlementAudit(
                priyaSett.getId(),
                com.example.ems.payroll.entity.FnfSettlementStatus.PENDING,
                "HR Admin",
                "Settlement created automatically during offboarding approval."
        ));
    }

    private void seedExpenses() {
        timelineEventRepository.deleteAll();
        approvalStepRepository.deleteAll();
        receiptRepository.deleteAll();
        expenseAuditLogRepository.deleteAll();
        expenseRepository.deleteAll();

        // 1. Create/Find Robert Chen (employee)
        Employee robert = employeeRepository.findByEmail("robert@company.com")
                .orElseGet(() -> {
                    Employee e = new Employee();
                    e.setFullName("Robert Chen");
                    e.setEmail("robert@company.com");
                    e.setEmployeeId("EMP015");
                    e.setPhone("5550015");
                    e.setGender("MALE");
                    e.setDob(LocalDate.of(1992, 4, 3));
                    e.setAddress("Delhi client office road");
                    e.setEmergencyContact("9876543212");
                    e.setDepartment("Engineering");
                    e.setDesignation("Software Engineer");
                    e.setAnnualSalary(BigDecimal.valueOf(1200000));
                    e.setJoiningDate(LocalDate.of(2025, 4, 3));
                    e.setStatus("ACTIVE");
                    return employeeRepository.save(e);
                });

        // 2. Categories
        ExpenseCategory travelCat = expenseCategoryRepository.findByCode("TRAVEL")
                .orElseGet(() -> {
                    ExpenseCategory c = new ExpenseCategory();
                    c.setName("Travel");
                    c.setCode("TRAVEL");
                    c.setMaxLimit(BigDecimal.valueOf(10000.00));
                    c.setRequiresReceipt(true);
                    return expenseCategoryRepository.save(c);
                });

        ExpenseCategory mealsCat = expenseCategoryRepository.findByCode("MEALS")
                .orElseGet(() -> {
                    ExpenseCategory c = new ExpenseCategory();
                    c.setName("Meals");
                    c.setCode("MEALS");
                    c.setMaxLimit(BigDecimal.valueOf(2000.00));
                    c.setRequiresReceipt(true);
                    return expenseCategoryRepository.save(c);
                });

        // 3. Seed Robert Chen's main pending expense (amount: 4200.00, receipt size: 254321)
        Expense mainExp = new Expense();
        mainExp.setEmployee(robert);
        mainExp.setTitle("Flight to Delhi - Client Meeting");
        mainExp.setDescription("Flight to Delhi");
        mainExp.setBusinessPurpose("Client Meeting");
        mainExp.setAmount(BigDecimal.valueOf(4200.00));
        mainExp.setExpenseDate(LocalDate.of(2026, 4, 3));
        LocalDateTime mainSub = LocalDateTime.of(2026, 4, 3, 9, 0, 0);
        mainExp.setSubmittedAt(mainSub);
        mainExp.setCreatedAt(mainSub);
        mainExp.setUpdatedAt(mainSub);
        mainExp.setExpenseStatus(ExpenseStatus.PENDING);
        mainExp.setCurrency("INR");
        mainExp.setReimbursementStatus("NOT_PAID");
        mainExp.setCategory(travelCat);
        mainExp.setExpenseNumber("EXP-2026-0101");
        mainExp.setAttachmentName("flight-ticket.pdf");
        mainExp.setAttachmentType("application/pdf");
        mainExp.setAttachmentUrl("/api/v1/files/receipts/flight-ticket.pdf");
        // Creating mock PDF data of exactly 254321 bytes
        mainExp.setAttachmentData(new byte[254321]);
        mainExp = expenseRepository.save(mainExp);

        timelineEventRepository.save(new com.example.ems.expense.entity.MyExpenseTimelineEvent(mainExp, "SUBMITTED", robert.getFullName()));
        expenseAuditLogRepository.save(new ExpenseAuditLog(mainExp.getId(), ExpenseStatus.SUBMITTED, "Submitted", robert.getFullName()));

        // 4. Seed other 35 pending expenses to make totalPending = 36 and pendingAmount = 154000
        // Robert's main pending = 4200.00. Remaining = 154000 - 4200 = 149800.
        // We seed 35 pending expenses with 4280.00 each (35 * 4280 = 149800).
        for (int i = 1; i <= 35; i++) {
            Expense exp = new Expense();
            exp.setEmployee(robert);
            exp.setTitle("Pending Expense " + i);
            exp.setDescription("Simulated Broadband/Meals allowance " + i);
            exp.setBusinessPurpose("Internet Allowance");
            exp.setAmount(BigDecimal.valueOf(4280.00));
            exp.setExpenseDate(LocalDate.now().minusDays(i));
            LocalDateTime subTime = LocalDateTime.now().minusDays(i);
            exp.setSubmittedAt(subTime);
            exp.setCreatedAt(subTime);
            exp.setUpdatedAt(subTime);
            exp.setExpenseStatus(ExpenseStatus.PENDING);
            exp.setCurrency("INR");
            exp.setReimbursementStatus("NOT_PAID");
            exp.setCategory(mealsCat);
            exp.setExpenseNumber("EXP-PEND-" + i);
            exp = expenseRepository.save(exp);

            timelineEventRepository.save(new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "SUBMITTED", robert.getFullName()));
            expenseAuditLogRepository.save(new ExpenseAuditLog(exp.getId(), ExpenseStatus.SUBMITTED, "Submitted", robert.getFullName()));
        }

        // 5. Seed 84 approved expenses totaling 128400 with average approval days of 1.8
        // Sum = 83 * 1500 + 3900 = 124500 + 3900 = 128400.
        // Duration: 1.8 days is 43 hours and 12 minutes.
        LocalDateTime baseApprovalTime = LocalDateTime.now().minusDays(1); // approved yesterday, so it's this month
        for (int i = 1; i <= 84; i++) {
            BigDecimal amt = (i == 84) ? BigDecimal.valueOf(3900.00) : BigDecimal.valueOf(1500.00);
            Expense exp = new Expense();
            exp.setEmployee(robert);
            exp.setTitle("Approved Expense " + i);
            exp.setDescription("Travel/Client Dinner " + i);
            exp.setBusinessPurpose("Business Dinner");
            exp.setAmount(amt);
            exp.setExpenseDate(LocalDate.now().minusDays(3));
            LocalDateTime subTime = baseApprovalTime.minusHours(43).minusMinutes(12);
            exp.setSubmittedAt(subTime);
            exp.setCreatedAt(subTime);
            exp.setUpdatedAt(baseApprovalTime);
            exp.setExpenseStatus(ExpenseStatus.APPROVED);
            exp.setCurrency("INR");
            exp.setReimbursementStatus("NOT_PAID");
            exp.setCategory(travelCat);
            exp.setExpenseNumber("EXP-APP-" + i);
            exp = expenseRepository.save(exp);

            // Timeline Events
            timelineEventRepository.save(new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "SUBMITTED", robert.getFullName()));
            timelineEventRepository.save(new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "APPROVED", "Eran"));

            // Audit logs
            ExpenseAuditLog subLog = new ExpenseAuditLog(exp.getId(), ExpenseStatus.SUBMITTED, "Submitted", robert.getFullName());
            subLog.setUpdatedAt(subTime);
            expenseAuditLogRepository.save(subLog);

            ExpenseAuditLog appLog = new ExpenseAuditLog(exp.getId(), ExpenseStatus.APPROVED, "Verified and approved", "Eran");
            appLog.setUpdatedAt(baseApprovalTime);
            expenseAuditLogRepository.save(appLog);
        }

        // 6. Seed 12 rejected expenses totaling 38000
        // Sum = 11 * 3160 + 3240 = 34760 + 3240 = 38000.
        for (int i = 1; i <= 12; i++) {
            BigDecimal amt = (i == 12) ? BigDecimal.valueOf(3240.00) : BigDecimal.valueOf(3160.00);
            Expense exp = new Expense();
            exp.setEmployee(robert);
            exp.setTitle("Rejected Expense " + i);
            exp.setDescription("Duplicate Broadband receipt " + i);
            exp.setBusinessPurpose("Broadband");
            exp.setAmount(amt);
            exp.setExpenseDate(LocalDate.now().minusDays(i + 5));
            LocalDateTime subTime = LocalDateTime.now().minusDays(i + 5);
            exp.setSubmittedAt(subTime);
            exp.setCreatedAt(subTime);
            exp.setUpdatedAt(subTime.plusDays(1));
            exp.setExpenseStatus(ExpenseStatus.REJECTED);
            exp.setRejectionReason("Receipt missing");
            exp.setCurrency("INR");
            exp.setCategory(mealsCat);
            exp.setExpenseNumber("EXP-REJ-" + i);
            exp = expenseRepository.save(exp);

            timelineEventRepository.save(new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "SUBMITTED", robert.getFullName()));
            timelineEventRepository.save(new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "REJECTED", "Finance Officer"));

            ExpenseAuditLog subLog = new ExpenseAuditLog(exp.getId(), ExpenseStatus.SUBMITTED, "Submitted", robert.getFullName());
            subLog.setUpdatedAt(subTime);
            expenseAuditLogRepository.save(subLog);

            ExpenseAuditLog rejLog = new ExpenseAuditLog(exp.getId(), ExpenseStatus.REJECTED, "Receipt is invalid", "Eran");
            rejLog.setUpdatedAt(subTime.plusDays(1));
            expenseAuditLogRepository.save(rejLog);
        }

        // 7. Seed 5 sent back expenses
        for (int i = 1; i <= 5; i++) {
            Expense exp = new Expense();
            exp.setEmployee(robert);
            exp.setTitle("Sent Back Expense " + i);
            exp.setDescription("Incomplete documentation " + i);
            exp.setBusinessPurpose("Travel");
            exp.setAmount(BigDecimal.valueOf(2500.00));
            exp.setExpenseDate(LocalDate.now().minusDays(i + 2));
            LocalDateTime subTime = LocalDateTime.now().minusDays(i + 2);
            exp.setSubmittedAt(subTime);
            exp.setCreatedAt(subTime);
            exp.setUpdatedAt(subTime.plusDays(1));
            exp.setExpenseStatus(ExpenseStatus.SENT_BACK);
            exp.setSendBackReason("Upload original invoice copy");
            exp.setCurrency("INR");
            exp.setCategory(travelCat);
            exp.setExpenseNumber("EXP-SB-" + i);
            exp = expenseRepository.save(exp);

            timelineEventRepository.save(new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "SUBMITTED", robert.getFullName()));
            timelineEventRepository.save(new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "SENT_BACK", "Finance Officer"));

            ExpenseAuditLog subLog = new ExpenseAuditLog(exp.getId(), ExpenseStatus.SUBMITTED, "Submitted", robert.getFullName());
            subLog.setUpdatedAt(subTime);
            expenseAuditLogRepository.save(subLog);

            ExpenseAuditLog sbLog = new ExpenseAuditLog(exp.getId(), ExpenseStatus.SENT_BACK, "Upload missing GST invoice", "Eran");
            sbLog.setUpdatedAt(subTime.plusDays(1));
            expenseAuditLogRepository.save(sbLog);
        }
    }
}
