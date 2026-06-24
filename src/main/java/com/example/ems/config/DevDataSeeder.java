package com.example.ems.config;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
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
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.recruitment.entity.Job;
import com.example.ems.recruitment.repository.JobRepository;
import com.example.ems.recruitment.entity.Candidate;
import com.example.ems.recruitment.repository.CandidateRepository;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.repository.LeaveTypeRepository;
import com.example.ems.payroll.entity.Payroll;
import com.example.ems.payroll.repository.PayrollRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Order(2)
public class DevDataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

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

    @Value("${app.seed.domain:company.com}")
    private String seedDomain;

    @Value("${app.seed.mock-data.enabled:true}")
    private boolean mockDataEnabled;

    @Override
    public void run(String... args) throws Exception {
        // Double protection: Check enabled flag and ensure prod profile is not active
        if (!mockDataEnabled) {
            System.out.println("DevDataSeeder: Mock data seeding is disabled via configuration.");
            return;
        }

        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            System.out.println("DevDataSeeder: Production profile active. Bypassing mock data seeding.");
            return;
        }

        System.out.println("DevDataSeeder: Seeding mock development data...");

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
                System.out.println("DevDataSeeder: " + displayName + " User seeded: " + email + " with ID: " + userId);
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
                continue; // Super admin employee record is handled in DatabaseSeeder
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

        System.out.println("DevDataSeeder: Mock data seeding completed.");
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

        // 1. Create/Find Robert Chen (employee)
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
        mainExp.setAttachmentData(new byte[254321]);
        mainExp = expenseRepository.save(mainExp);

        timelineEventRepository.save(
                new com.example.ems.expense.entity.MyExpenseTimelineEvent(mainExp, "SUBMITTED", robert.getFullName()));
        expenseAuditLogRepository
                .save(new ExpenseAuditLog(mainExp.getId(), ExpenseStatus.SUBMITTED, "Submitted", robert.getFullName()));

        // 4. Seed other 35 pending expenses to make totalPending = 36 and pendingAmount = 154000
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

        // 5. Seed 84 approved expenses totaling 128400 with average approval days of 1.8
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

        // 6. Seed 12 rejected expenses totaling 38000
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
        // A. Seed Departments if empty
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

        // B. Seed Jobs if empty
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

        // C. Seed Candidates if empty
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

        // D. Seed LeaveTypes and Leaves if empty
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

        // E. Seed Payroll records if empty
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

        // F. Seed Audit Logs
        auditLogService.seedAuditLogs();
    }
}
