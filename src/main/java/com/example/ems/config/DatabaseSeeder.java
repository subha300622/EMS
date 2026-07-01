package com.example.ems.config;

import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.offboarding.entity.Offboarding;
import com.example.ems.offboarding.repository.OffboardingRepository;
import com.example.ems.payroll.entity.FnfSettlement;
import com.example.ems.payroll.repository.FnfSettlementRepository;
import com.example.ems.payroll.repository.FnfSettlementAuditRepository;
import com.example.ems.asset.entity.MyAsset;
import com.example.ems.asset.repository.MyAssetRepository;
import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.entity.ExpenseCategory;
import com.example.ems.expense.entity.ExpenseStatus;
import com.example.ems.expense.entity.ExpenseAuditLog;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.expense.repository.ExpenseCategoryRepository;
import com.example.ems.expense.repository.MyExpenseTimelineEventRepository;
import com.example.ems.expense.repository.MyExpenseReceiptRepository;
import com.example.ems.expense.repository.MyExpenseApprovalStepRepository;
import com.example.ems.expense.repository.ExpenseAuditLogRepository;
import com.example.ems.recruitment.entity.Job;
import com.example.ems.recruitment.entity.Candidate;
import com.example.ems.recruitment.repository.JobRepository;
import com.example.ems.recruitment.repository.CandidateRepository;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.repository.LeaveTypeRepository;
import com.example.ems.payroll.entity.Payroll;
import com.example.ems.payroll.repository.PayrollRepository;
import com.example.ems.onboarding.entity.OnboardingTemplate;
import com.example.ems.onboarding.entity.OnboardingTemplateTask;
import com.example.ems.onboarding.repository.OnboardingTemplateRepository;
import com.example.ems.onboarding.repository.OnboardingTemplateTaskRepository;
import com.example.ems.settings.entity.SystemSetting;
import com.example.ems.settings.repository.SystemSettingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.ems.settings.service.SystemSettingService;
import com.example.ems.employee.service.MyDocumentService;
import com.example.ems.appraisal.service.AppraisalService;
import com.example.ems.payroll.service.PayrollService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Order(2)
public class DatabaseSeeder implements ApplicationRunner {

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
    private OnboardingTemplateRepository templateRepository;

    @Autowired
    private OnboardingTemplateTaskRepository templateTaskRepository;

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Autowired
    private com.example.ems.support.service.MySupportService mySupportService;

    @Autowired
    private com.example.ems.asset.service.MyAssetService myAssetService;

    @Autowired
    private FnfSettlementRepository fnfSettlementRepository;

    @Autowired
    private FnfSettlementAuditRepository fnfSettlementAuditRepository;

    @Autowired
    private OffboardingRepository offboardingRepository;

    @Autowired
    private MyAssetRepository myAssetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;

    @Autowired
    private MyExpenseTimelineEventRepository timelineEventRepository;

    @Autowired
    private MyExpenseReceiptRepository receiptRepository;

    @Autowired
    private MyExpenseApprovalStepRepository approvalStepRepository;

    @Autowired
    private ExpenseAuditLogRepository expenseAuditLogRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private com.example.ems.audit.service.AuditLogService auditLogService;

    @Autowired
    private com.example.ems.schedule.service.MyScheduleService myScheduleService;

    @Autowired
    private Environment environment;

    @Autowired
    private SystemSettingService systemSettingService;

    @Autowired
    private MyDocumentService myDocumentService;

    @Autowired
    private AppraisalService appraisalService;

    @Autowired
    private PayrollService payrollService;

    @Value("${app.seed.domain:company.com}")
    private String seedDomain;

    @Value("${app.seed.mock-data.enabled:true}")
    private boolean mockDataEnabled;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 1. Lock Check: Check if seeding is already complete using SystemSetting
        if (systemSettingRepository.findBySettingKey("seeder.completed").isPresent()) {
            System.out.println("DatabaseSeeder: Seeding has already been completed in a previous startup. Skipping.");
            return;
        }

        System.out.println("DatabaseSeeder: Starting database seeding...");

        // 2. Seed Core Data (runs for all environments)
        seedCoreData();

        // 3. Seed Mock Data (Dev/Test only)
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        boolean isProduction = activeProfiles.contains("prod") || activeProfiles.contains("production");
        
        if (mockDataEnabled && !isProduction) {
            System.out.println("DatabaseSeeder: Seeding mock development data...");
            seedMockData();
        } else {
            System.out.println("DatabaseSeeder: Mock data seeding bypassed (disabled or non-development environment).");
        }

        // 4. Mark seeding as complete
        systemSettingRepository.save(new SystemSetting("seeder.completed", "true", "system"));
        System.out.println("DatabaseSeeder: Seeding completed successfully.");
    }

    private void seedCoreData() {
        System.out.println("DatabaseSeeder: Seeding core system data...");
        // Roles & Permissions
        seedCoreAuthData();
        // Onboarding Templates
        seedOnboardingTemplate();
        // System Settings
        systemSettingService.initDefaultSettings();
        // Expense Categories
        seedExpenseCategories();
        // Document Categories & Types
        myDocumentService.seedCoreCategoriesAndTypes();
        // Appraisal Cycle & Policies
        appraisalService.seedCoreAppraisalData();
        // Payroll Settings, Salary Components, & Tax Slabs
        payrollService.seedCorePayrollData();
    }

    private void seedExpenseCategories() {
        if (expenseCategoryRepository.findByCode("TRAVEL").isEmpty()) {
            ExpenseCategory c = new ExpenseCategory();
            c.setName("Travel");
            c.setCode("TRAVEL");
            c.setMaxLimit(BigDecimal.valueOf(10000.00));
            c.setRequiresReceipt(true);
            expenseCategoryRepository.save(c);
        }
        if (expenseCategoryRepository.findByCode("MEALS").isEmpty()) {
            ExpenseCategory c = new ExpenseCategory();
            c.setName("Meals");
            c.setCode("MEALS");
            c.setMaxLimit(BigDecimal.valueOf(2000.00));
            c.setRequiresReceipt(true);
            expenseCategoryRepository.save(c);
        }
    }

    private void seedCoreAuthData() {
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
                emp.setPhone(superAdminUser.getMobileNumber() != null ? superAdminUser.getMobileNumber() : "1234567890");
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

    private void seedOnboardingTemplate() {
        if (templateRepository.count() > 0) {
            System.out.println("DatabaseSeeder: Onboarding template already seeded.");
            return;
        }

        System.out.println("DatabaseSeeder: Seeding default versioned onboarding template...");

        OnboardingTemplate template = new OnboardingTemplate();
        template.setName("Standard Employee Onboarding Template");
        template.setVersion(1);
        template.setActive(true);
        template.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        OnboardingTemplate savedTemplate = templateRepository.save(template);

        // Pre-Joining Phase Tasks (6 tasks)
        seedOnboardingTemplateTask(savedTemplate, "Submit Personal Documents", "Please upload your Government ID and Bank Account details.", "PRE_JOINING", "EMPLOYEE", "1 hr", -3, "HIGH", 24);
        seedOnboardingTemplateTask(savedTemplate, "Sign NDA and Offer Letter", "Review and sign employment agreement.", "PRE_JOINING", "EMPLOYEE", "30 mins", -5, "CRITICAL", 48);
        seedOnboardingTemplateTask(savedTemplate, "Background Verification", "HR triggers background screening check.", "PRE_JOINING", "HR", "15 mins", -2, "MEDIUM", 120);
        seedOnboardingTemplateTask(savedTemplate, "IT Asset Allocation Request", "Allocate laptop and standard work setup.", "PRE_JOINING", "IT", "20 mins", -4, "HIGH", 72);
        seedOnboardingTemplateTask(savedTemplate, "Corporate Email Setup", "Provision company corporate email address.", "PRE_JOINING", "IT", "15 mins", -1, "CRITICAL", 24);
        seedOnboardingTemplateTask(savedTemplate, "Bank Account Details Submission", "Submit details for salary credit.", "PRE_JOINING", "EMPLOYEE", "20 mins", -3, "MEDIUM", 72);

        // Day 1 Phase Tasks (7 tasks)
        seedOnboardingTemplateTask(savedTemplate, "Welcome 1:1 Meeting", "Initial manager welcome check-in.", "DAY_1", "MANAGER", "30 mins", 0, "HIGH", 8);
        seedOnboardingTemplateTask(savedTemplate, "Workspace Tour and Team Introduction", "Guided tour of the floor and team intro.", "DAY_1", "MANAGER", "45 mins", 0, "LOW", 8);
        seedOnboardingTemplateTask(savedTemplate, "Collect Access Badge", "Collect physical office entry badge.", "DAY_1", "ADMIN", "15 mins", 0, "MEDIUM", 4);
        seedOnboardingTemplateTask(savedTemplate, "Day 1 HR Briefing & Orientation", "HR orientation and portal registration.", "DAY_1", "HR", "2 hrs", 0, "HIGH", 8);
        seedOnboardingTemplateTask(savedTemplate, "Login Credential Activation", "Activate Active Directory and VPN access.", "DAY_1", "IT", "30 mins", 0, "CRITICAL", 4);
        seedOnboardingTemplateTask(savedTemplate, "Verify Personal Details in EMS Portal", "Confirm details in the system portal.", "DAY_1", "EMPLOYEE", "20 mins", 0, "MEDIUM", 12);
        seedOnboardingTemplateTask(savedTemplate, "Setup Developer Environment", "Clone repo, build code, install SDKs.", "DAY_1", "EMPLOYEE", "3 hrs", 0, "HIGH", 16);

        // Week 1 Phase Tasks (8 tasks)
        seedOnboardingTemplateTask(savedTemplate, "Complete Security Compliance Training", "Mandatory security awareness training.", "WEEK_1", "EMPLOYEE", "2 hrs", 7, "HIGH", 168);
        seedOnboardingTemplateTask(savedTemplate, "Review Team Goals & OKRs", "Align on performance deliverables.", "WEEK_1", "EMPLOYEE", "1 hr", 5, "MEDIUM", 120);
        seedOnboardingTemplateTask(savedTemplate, "Install Required Development Tools", "Get compiler licenses and tools.", "WEEK_1", "EMPLOYEE", "1 hr", 3, "LOW", 72);
        seedOnboardingTemplateTask(savedTemplate, "1:1 Sync with Onboarding Buddy", "Casual coffee chat and buddy check-in.", "WEEK_1", "EMPLOYEE", "30 mins", 4, "LOW", 96);
        seedOnboardingTemplateTask(savedTemplate, "EMS Time Logging Training", "Learn how to record weekly timecards.", "WEEK_1", "EMPLOYEE", "30 mins", 6, "MEDIUM", 144);
        seedOnboardingTemplateTask(savedTemplate, "Week 1 Manager Retrospective", "Feedback session on first week experience.", "WEEK_1", "MANAGER", "30 mins", 7, "HIGH", 48);
        seedOnboardingTemplateTask(savedTemplate, "Review Project Documentation", "Read documentation on architecture.", "WEEK_1", "EMPLOYEE", "4 hrs", 4, "LOW", 96);
        seedOnboardingTemplateTask(savedTemplate, "Understand Code Review Process", "Learn about code styling rules.", "WEEK_1", "EMPLOYEE", "1 hr", 5, "MEDIUM", 120);

        // Month 1 Phase Tasks (12 tasks)
        seedOnboardingTemplateTask(savedTemplate, "Complete Employee Code of Conduct", "Ethics and compliance verification.", "MONTH_1", "EMPLOYEE", "1 hr", 14, "HIGH", 336);
        seedOnboardingTemplateTask(savedTemplate, "First Project Contribution", "Submit first pull request in repository.", "MONTH_1", "EMPLOYEE", "8 hrs", 21, "HIGH", 504);
        seedOnboardingTemplateTask(savedTemplate, "Day 30 HR Check-in", "HR survey on onboarding feedback.", "MONTH_1", "HR", "30 mins", 30, "MEDIUM", 48);
        seedOnboardingTemplateTask(savedTemplate, "Day 30 Manager Performance Review", "Review progress against goals.", "MONTH_1", "MANAGER", "1 hr", 30, "HIGH", 48);
        seedOnboardingTemplateTask(savedTemplate, "Share Onboarding Process Feedback", "Complete checklist survey form.", "MONTH_1", "EMPLOYEE", "15 mins", 28, "LOW", 72);
        seedOnboardingTemplateTask(savedTemplate, "Attend Department Welcome Lunch", "Informal department team lunch.", "MONTH_1", "MANAGER", "1 hr", 15, "LOW", 168);
        seedOnboardingTemplateTask(savedTemplate, "Read Architecture Guidelines", "In-depth codebase guide.", "MONTH_1", "EMPLOYEE", "2 hrs", 12, "LOW", 288);
        seedOnboardingTemplateTask(savedTemplate, "Request Database Production Read Access", "Obtain read permissions for databases.", "MONTH_1", "IT", "15 mins", 10, "MEDIUM", 120);
        seedOnboardingTemplateTask(savedTemplate, "Setup 1:1s with Cross-functional Leads", "Meet with product and design leads.", "MONTH_1", "EMPLOYEE", "2 hrs", 20, "LOW", 480);
        seedOnboardingTemplateTask(savedTemplate, "Shadow Senior Engineer on Code Review", "Learn style guidelines in practice.", "MONTH_1", "EMPLOYEE", "2 hrs", 15, "MEDIUM", 360);
        seedOnboardingTemplateTask(savedTemplate, "Understand Security & PII Policies", "Training on handling personal data.", "MONTH_1", "EMPLOYEE", "1 hr", 18, "HIGH", 432);
        seedOnboardingTemplateTask(savedTemplate, "First Bug Fix Deployment to Sandbox", "Build and deploy patch fix.", "MONTH_1", "EMPLOYEE", "4 hrs", 25, "MEDIUM", 600);

        System.out.println("DatabaseSeeder: Default onboarding template tasks successfully seeded.");
    }

    private void seedOnboardingTemplateTask(OnboardingTemplate template, String title, String description, String phase, String owner, String estTime, int dueDays, String priority, int slaHours) {
        OnboardingTemplateTask task = new OnboardingTemplateTask();
        task.setTemplate(template);
        task.setTitle(title);
        task.setDescription(description);
        task.setPhase(phase);
        task.setOwner(owner);
        task.setEstimatedTime(estTime);
        task.setDueDaysAfterJoining(dueDays);
        task.setPriority(priority);
        task.setSlaHours(slaHours);
        templateTaskRepository.save(task);
    }

    private void seedMockData() {
        // 1. Seed non-admin default users
        List<String> mockRoles = Arrays.asList("ADMIN", "HR", "MANAGER", "FINANCE", "EMPLOYEE");
        for (String roleName : mockRoles) {
            Role role = roleRepository.findByName(roleName).orElse(null);
            if (role == null) {
                continue;
            }

            String roleCleanName = roleName.toLowerCase();
            String email = roleCleanName + "@" + seedDomain;
            String password = roleCleanName + "@" + role.getId();
            String displayName = roleName.charAt(0) + roleName.substring(1).toLowerCase().replace("_", " ");

            if (userRepository.findByWorkEmail(email).isEmpty()) {
                User user = new User();
                user.setFullName(displayName + " User");
                user.setWorkEmail(email);
                user.setMobileNumber("5550" + String.format("%03d", role.getId()));
                user.setDepartment(displayName);
                user.setRequestedRole(roleName);
                user.setRole(role);
                user.setPassword(passwordEncoder.encode(password));
                user.setLocation("Headquarters");
                userRepository.save(user);

                String userId = "EMP" + String.format("%03d", user.getId());
                user.setUserId(userId);
                userRepository.save(user);
                System.out.println("DatabaseSeeder: " + displayName + " User seeded: " + email + " with ID: " + userId);
            } else {
                User existingUser = userRepository.findByWorkEmail(email).get();
                existingUser.setRole(role);
                existingUser.setPassword(passwordEncoder.encode(password));
                userRepository.save(existingUser);
            }
        }

        // 2. Ensure all mock/non-admin users have fully-populated Employee records
        for (User u : userRepository.findAll()) {
            if ("super_admin@company.com".equalsIgnoreCase(u.getWorkEmail())) {
                continue;
            }
            Employee emp = employeeRepository.findByEmail(u.getWorkEmail())
                    .orElseGet(Employee::new);

            emp.setFullName(u.getFullName());
            emp.setEmail(u.getWorkEmail());
            emp.setEmployeeId(u.getUserId());
            if (emp.getPhone() == null)
                emp.setPhone(u.getMobileNumber() != null ? u.getMobileNumber() : "1234567890");
            if (emp.getGender() == null)
                emp.setGender("MALE");
            if (emp.getDob() == null)
                emp.setDob(LocalDate.of(1990, 1, 1));
            if (emp.getAddress() == null)
                emp.setAddress("123 Corporate Way");
            if (emp.getEmergencyContact() == null)
                emp.setEmergencyContact("9876543210");
            if (emp.getDepartment() == null)
                emp.setDepartment(u.getDepartment() != null ? u.getDepartment() : "Engineering");
            if (emp.getDesignation() == null)
                emp.setDesignation(u.getRole() != null ? u.getRole().getName() : "Software Engineer");
            if (emp.getAnnualSalary() == null)
                emp.setAnnualSalary(BigDecimal.valueOf(85000));
            if (emp.getJoiningDate() == null)
                emp.setJoiningDate(LocalDate.of(2026, 6, 10));
            if (emp.getLocation() == null)
                emp.setLocation(u.getLocation() != null ? u.getLocation() : "Headquarters");
            if (emp.getEmploymentType() == null)
                emp.setEmploymentType("FULL_TIME");
            if (emp.getStatus() == null || emp.getStatus().isBlank())
                emp.setStatus("ACTIVE");

            employeeRepository.save(emp);
        }

        // 3. Seed support data (categories, KB articles, sample tickets) for employee
        String employeeEmail = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "EMPLOYEE".equalsIgnoreCase(u.getRole().getName()))
                .map(User::getWorkEmail)
                .findFirst()
                .orElse("employee@" + seedDomain);
        mySupportService.seedSupportData(employeeEmail);

        // 4. Seed asset cost report data
        myAssetService.seedDatabase();

        // 5. Seed F&F settlements data
        seedSettlements();

        // 6. Seed Expense Approvals data
        seedExpenses();

        // 7. Seed Dashboard search items
        seedDashboardSearchEntities();

        // 8. Seed schedule data for all users
        userRepository.findAll().forEach(user -> {
            myScheduleService.seedScheduleData(user.getWorkEmail());
        });

        // 9. Seed mock employee documents
        myDocumentService.seedMockEmployeeDocuments();
    }

    private void seedSettlements() {
        if (fnfSettlementRepository.count() > 0) {
            return;
        }

        // 1. Seed Ravi Kumar
        Employee ravi = employeeRepository.findByEmail("ravi@company.com")
                .or(() -> employeeRepository.findByEmployeeId("EMP90101"))
                .orElseGet(() -> {
                    Employee e = new Employee();
                    e.setFullName("Ravi Kumar");
                    e.setEmail("ravi@company.com");
                    e.setEmployeeId("EMP90101");
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
                "Settlement created automatically during offboarding approval."));

        // Seed Assets for Ravi
        List<MyAsset> raviAssets = myAssetRepository.findByAssignedToId(ravi.getId());
        if (raviAssets.isEmpty()) {
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
                .or(() -> employeeRepository.findByEmployeeId("EMP90102"))
                .orElseGet(() -> {
                    Employee e = new Employee();
                    e.setFullName("Priya Sharma");
                    e.setEmail("priya@company.com");
                    e.setEmployeeId("EMP90102");
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
                "Settlement created automatically during offboarding approval."));
    }

    private void seedExpenses() {
        timelineEventRepository.deleteAll();
        approvalStepRepository.deleteAll();
        receiptRepository.deleteAll();
        expenseAuditLogRepository.deleteAll();
        expenseRepository.deleteAll();

        Employee robert = employeeRepository.findByEmail("robert@company.com")
                .or(() -> employeeRepository.findByEmployeeId("EMP90015"))
                .orElseGet(() -> {
                    Employee e = new Employee();
                    e.setFullName("Robert Chen");
                    e.setEmail("robert@company.com");
                    e.setEmployeeId("EMP90015");
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
        mainExp.setAttachmentData(new byte[254321]);
        mainExp = expenseRepository.save(mainExp);

        timelineEventRepository.save(
                new com.example.ems.expense.entity.MyExpenseTimelineEvent(mainExp, "SUBMITTED", robert.getFullName()));
        expenseAuditLogRepository
                .save(new ExpenseAuditLog(mainExp.getId(), ExpenseStatus.SUBMITTED, "Submitted", robert.getFullName()));

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

            timelineEventRepository.save(
                    new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "SUBMITTED", robert.getFullName()));
            expenseAuditLogRepository
                    .save(new ExpenseAuditLog(exp.getId(), ExpenseStatus.SUBMITTED, "Submitted", robert.getFullName()));
        }

        LocalDateTime baseApprovalTime = LocalDateTime.now().minusDays(1);
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

            timelineEventRepository.save(
                    new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "SUBMITTED", robert.getFullName()));
            timelineEventRepository
                    .save(new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "APPROVED", "Eran"));

            ExpenseAuditLog subLog = new ExpenseAuditLog(exp.getId(), ExpenseStatus.SUBMITTED, "Submitted",
                    robert.getFullName());
            subLog.setUpdatedAt(subTime);
            expenseAuditLogRepository.save(subLog);

            ExpenseAuditLog appLog = new ExpenseAuditLog(exp.getId(), ExpenseStatus.APPROVED, "Verified and approved",
                    "Eran");
            appLog.setUpdatedAt(baseApprovalTime);
            expenseAuditLogRepository.save(appLog);
        }

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

            timelineEventRepository.save(
                    new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "SUBMITTED", robert.getFullName()));
            timelineEventRepository.save(
                    new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "REJECTED", "Finance Officer"));

            ExpenseAuditLog subLog = new ExpenseAuditLog(exp.getId(), ExpenseStatus.SUBMITTED, "Submitted",
                    robert.getFullName());
            subLog.setUpdatedAt(subTime);
            expenseAuditLogRepository.save(subLog);

            ExpenseAuditLog rejLog = new ExpenseAuditLog(exp.getId(), ExpenseStatus.REJECTED, "Receipt is invalid",
                    "Eran");
            rejLog.setUpdatedAt(subTime.plusDays(1));
            expenseAuditLogRepository.save(rejLog);
        }

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

            timelineEventRepository.save(
                    new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "SUBMITTED", robert.getFullName()));
            timelineEventRepository.save(
                    new com.example.ems.expense.entity.MyExpenseTimelineEvent(exp, "SENT_BACK", "Finance Officer"));

            ExpenseAuditLog subLog = new ExpenseAuditLog(exp.getId(), ExpenseStatus.SUBMITTED, "Submitted",
                    robert.getFullName());
            subLog.setUpdatedAt(subTime);
            expenseAuditLogRepository.save(subLog);

            ExpenseAuditLog sbLog = new ExpenseAuditLog(exp.getId(), ExpenseStatus.SENT_BACK,
                    "Upload missing GST invoice", "Eran");
            sbLog.setUpdatedAt(subTime.plusDays(1));
            expenseAuditLogRepository.save(sbLog);
        }
    }

    private void seedDashboardSearchEntities() {
        if (departmentRepository.count() == 0) {
            Department eng = new Department(null, "Engineering", "ENG", "Engineering and Software Development");
            eng.setBudget(BigDecimal.valueOf(5000000));
            departmentRepository.save(eng);

            Department sales = new Department(null, "Sales", "SAL", "Sales and Business Development");
            sales.setBudget(BigDecimal.valueOf(3000000));
            departmentRepository.save(sales);

            Department hr = new Department(null, "HR", "HRD", "Human Resources and People Operations");
            hr.setBudget(BigDecimal.valueOf(1500000));
            departmentRepository.save(hr);

            Department fin = new Department(null, "Finance", "FIN", "Finance and Accounting");
            fin.setBudget(BigDecimal.valueOf(2500000));
            departmentRepository.save(fin);
        }

        if (jobRepository.count() == 0) {
            Job engJob = new Job();
            engJob.setTitle("Software Engineer");
            engJob.setDepartment("Engineering");
            engJob.setLocation("Remote");
            engJob.setDescription("Java Spring Boot Developer");
            engJob.setRequirements("Java, Spring Boot, SQL");
            engJob.setSalaryRange("80k-120k");
            engJob.setStatus("ACTIVE");
            jobRepository.save(engJob);

            Job salesJob = new Job();
            salesJob.setTitle("Sales Executive");
            salesJob.setDepartment("Sales");
            salesJob.setLocation("Headquarters");
            salesJob.setDescription("Enterprise B2B Sales");
            salesJob.setRequirements("B2B sales experience");
            salesJob.setSalaryRange("60k-90k");
            salesJob.setStatus("ACTIVE");
            jobRepository.save(salesJob);

            Job hrJob = new Job();
            hrJob.setTitle("HR Generalist");
            hrJob.setDepartment("HR");
            hrJob.setLocation("Headquarters");
            hrJob.setDescription("Talent acquisition and employee relations");
            hrJob.setRequirements("3+ years in HR");
            hrJob.setSalaryRange("50k-70k");
            hrJob.setStatus("ACTIVE");
            jobRepository.save(hrJob);
        }

        if (candidateRepository.count() == 0) {
            Job engJob = jobRepository.findAll().stream()
                    .filter(j -> "Software Engineer".equalsIgnoreCase(j.getTitle()))
                    .findFirst().orElse(null);
            if (engJob != null) {
                Candidate john = new Candidate();
                john.setFullName("John Doe");
                john.setEmail("john.doe@gmail.com");
                john.setPhone("5551234");
                john.setJob(engJob);
                john.setStatus("APPLIED");
                candidateRepository.save(john);
            }

            Job salesJob = jobRepository.findAll().stream()
                    .filter(j -> "Sales Executive".equalsIgnoreCase(j.getTitle()))
                    .findFirst().orElse(null);
            if (salesJob != null) {
                Candidate jane = new Candidate();
                jane.setFullName("Jane Smith");
                jane.setEmail("jane.smith@yahoo.com");
                jane.setPhone("5555678");
                jane.setJob(salesJob);
                jane.setStatus("INTERVIEWING");
                candidateRepository.save(jane);
            }
        }

        if (leaveTypeRepository.count() == 0) {
            LeaveType casual = new LeaveType(null, "Casual Leave", "Casual Leave", 12, true);
            leaveTypeRepository.save(casual);

            LeaveType sick = new LeaveType(null, "Sick Leave", "Sick / Medical Leave", 10, true);
            leaveTypeRepository.save(sick);

            LeaveType paid = new LeaveType(null, "Paid Leave", "Paid / Annual Leave", 15, true);
            leaveTypeRepository.save(paid);
        }

        if (leaveRepository.count() == 0) {
            Employee robert = employeeRepository.findByEmail("robert@company.com").orElse(null);
            LeaveType casual = leaveTypeRepository.findAll().stream()
                    .filter(lt -> "Casual Leave".equalsIgnoreCase(lt.getName()))
                    .findFirst().orElse(null);
            if (robert != null && casual != null) {
                Leave leave = new Leave();
                leave.setEmployee(robert);
                leave.setLeaveType(casual);
                leave.setStartDate(LocalDate.now().plusDays(5));
                leave.setEndDate(LocalDate.now().plusDays(7));
                leave.setReason("Family function");
                leave.setStatus("PENDING");
                leave.setAppliedAt(LocalDateTime.now());
                leave.setUpdatedAt(LocalDateTime.now());
                leaveRepository.save(leave);
            }
        }

        if (payrollRepository.count() == 0) {
            Employee robert = employeeRepository.findByEmail("robert@company.com").orElse(null);
            if (robert != null) {
                Payroll payroll = new Payroll();
                payroll.setEmployee(robert);
                payroll.setMonth(5);
                payroll.setYear(2026);
                payroll.setBasicSalary(BigDecimal.valueOf(80000));
                payroll.setAllowances(BigDecimal.valueOf(15000));
                payroll.setDeductions(BigDecimal.valueOf(5000));
                payroll.setNetPay(BigDecimal.valueOf(90000));
                payroll.setStatus("PAID");
                payroll.setGeneratedAt(LocalDateTime.now().minusDays(10));
                payroll.setProcessedAt(LocalDateTime.now().minusDays(5));
                payrollRepository.save(payroll);
            }
        }

        auditLogService.seedAuditLogs();
    }
}
