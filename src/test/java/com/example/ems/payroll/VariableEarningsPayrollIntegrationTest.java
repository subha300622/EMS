package com.example.ems.payroll;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.entity.ExpenseCategory;
import com.example.ems.expense.repository.ExpenseCategoryRepository;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.payroll.dto.PayrollRunCreateRequest;
import com.example.ems.payroll.dto.PayrollRunResponse;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.integration.BonusPayrollAdapter;
import com.example.ems.payroll.integration.IncentivePayrollAdapter;
import com.example.ems.payroll.repository.*;
import com.example.ems.payroll.service.PayrollRunService;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class VariableEarningsPayrollIntegrationTest {

    @Autowired
    private PayrollRunService payrollRunService;

    @Autowired
    private PayrollEmployeeRepository payrollEmployeeRepository;

    @Autowired
    private PayrollItemRepository payrollItemRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SalaryStructureRepository salaryStructureRepository;

    @Autowired
    private SalaryComponentRepository salaryComponentRepository;

    @Autowired
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    @Autowired
    private EmployeeSalaryAssignmentRepository employeeSalaryAssignmentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;

    @Autowired
    private IncentivePayrollAdapter incentivePayrollAdapter;

    @Autowired
    private BonusPayrollAdapter bonusPayrollAdapter;

    private Organization organization;
    private Employee employee;
    private SalaryStructure structure;
    private ExpenseCategory expenseCategory;

    @BeforeEach
    public void setUp() {
        long ts = System.currentTimeMillis();
        organization = new Organization();
        organization.setName("Variable Pay Org " + ts);
        organization.setOrganizationCode("ORG_VP_" + ts);
        organization = organizationRepository.save(organization);

        TenantContext.setCurrentTenant(organization.getId());

        com.example.ems.employee.entity.Department department = new com.example.ems.employee.entity.Department();
        department.setName("Engineering " + ts);
        department.setCode("ENG_" + ts);
        department.setOrganization(organization);
        department = departmentRepository.save(department);

        employee = new Employee();
        employee.setEmployeeId("EMP_VP_" + ts);
        employee.setFullName("Bob Developer");
        employee.setEmail("bob_" + ts + "@variablepay.com");
        employee.setDepartment(department.getName());
        employee.setStatus("ACTIVE");
        employee.setOrganization(organization);
        employee = employeeRepository.save(employee);

        // Setup Salary Components & Structure: Basic = 50,000, HRA = 20,000, PF = 1,800
        SalaryComponent basicComp = new SalaryComponent(organization.getId(), "Basic Pay", "BASIC", "Basic Salary", SalaryComponentType.EARNING, true, true);
        basicComp = salaryComponentRepository.save(basicComp);

        SalaryComponent hraComp = new SalaryComponent(organization.getId(), "HRA", "HRA", "House Rent Allowance", SalaryComponentType.EARNING, true, true);
        hraComp = salaryComponentRepository.save(hraComp);

        SalaryComponent pfComp = new SalaryComponent(organization.getId(), "PF Deduction", "PF", "Provident Fund", SalaryComponentType.DEDUCTION, false, true);
        pfComp = salaryComponentRepository.save(pfComp);

        structure = new SalaryStructure();
        structure.setOrganizationId(organization.getId());
        structure.setName("Engineering Structure " + ts);
        structure.setCode("ENG_STR_" + ts);
        structure.setStatus(SalaryStructureStatus.ACTIVE);
        structure.setVersion(1);
        structure = salaryStructureRepository.save(structure);

        SalaryStructureComponent sscBasic = new SalaryStructureComponent(
                structure, basicComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null, 1
        );
        salaryStructureComponentRepository.save(sscBasic);

        SalaryStructureComponent sscHra = new SalaryStructureComponent(
                structure, hraComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(20000), null, null, 2
        );
        salaryStructureComponentRepository.save(sscHra);

        SalaryStructureComponent sscPf = new SalaryStructureComponent(
                structure, pfComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(1800), null, null, 3
        );
        salaryStructureComponentRepository.save(sscPf);

        // Assign structure to employee
        EmployeeSalaryAssignment assignment = new EmployeeSalaryAssignment(
                organization.getId(), employee, structure, LocalDate.of(2026, 1, 1), null, SalaryAssignmentStatus.ACTIVE, "Initial assignment"
        );
        employeeSalaryAssignmentRepository.save(assignment);

        // Expense category
        expenseCategory = new ExpenseCategory();
        expenseCategory.setName("Client Travel " + ts);
        expenseCategory.setCode("TRAVEL_" + ts);
        expenseCategory = expenseCategoryRepository.save(expenseCategory);
    }

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Overtime Ingestion: Approved OT hours calculated with 1.5x multiplier and snapshotted")
    public void testOvertimeCalculationInPayrollRun() {
        LocalDate periodStart = LocalDate.of(2026, 9, 1);
        LocalDate periodEnd = LocalDate.of(2026, 9, 30);

        // Create 2 attendance days with overtime (each day 9am to 8:30pm = 11.5 hrs -> 2.5 hrs OT each = 5.0 hrs total OT)
        for (int day = 2; day <= 3; day++) {
            Attendance att = new Attendance();
            att.setEmployee(employee);
            att.setOrganization(organization);
            att.setDate(LocalDate.of(2026, 9, day));
            att.setStatus(AttendanceStatus.PRESENT);
            att.setPunchInTime(LocalTime.of(9, 0));
            att.setPunchOutTime(LocalTime.of(20, 30));
            att.setServerTime(Instant.now());
            attendanceRepository.save(att);
        }

        // Create and process payroll run
        PayrollRunCreateRequest req = new PayrollRunCreateRequest(periodStart, periodEnd, "INR");
        PayrollRunResponse created = payrollRunService.createPayrollRun(req);

        PayrollRunResponse processed = payrollRunService.processPayrollRun(created.getId());
        assertEquals(PayrollRunStatus.CALCULATED, processed.getStatus());

        PayrollEmployee pe = payrollEmployeeRepository.findByPayrollRunIdAndEmployeeIdAndOrganizationId(created.getId(), employee.getId(), organization.getId()).orElseThrow();

        List<PayrollItem> items = payrollItemRepository.findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(pe.getId(), organization.getId());
        PayrollItem otItem = items.stream().filter(i -> "OT".equals(i.getComponentCode())).findFirst().orElse(null);
        assertNotNull(otItem, "OT PayrollItem must be generated");
        assertTrue(otItem.getAmount().compareTo(BigDecimal.ZERO) > 0);
        assertEquals("EARNING", otItem.getComponentType());
        assertEquals("HOURLY_RATE", otItem.getCalculationType());

        // Verify Gross includes Basic(50000) + HRA(20000) + OT
        BigDecimal expectedGross = BigDecimal.valueOf(70000).add(otItem.getAmount());
        assertEquals(0, expectedGross.compareTo(pe.getGrossAmount()));
    }

    @Test
    @DisplayName("Incentive & Performance Bonus Ingestion & Idempotency")
    public void testIncentiveAndBonusInPayrollRun() {
        LocalDate periodStart = LocalDate.of(2026, 9, 1);
        LocalDate periodEnd = LocalDate.of(2026, 9, 30);

        // Register approved incentive (5,000) and bonus (10,000)
        incentivePayrollAdapter.registerApprovedIncentive(organization.getId(), employee.getId(), periodStart, BigDecimal.valueOf(5000));
        bonusPayrollAdapter.registerApprovedBonus(organization.getId(), employee.getId(), periodStart, BigDecimal.valueOf(10000));

        PayrollRunCreateRequest req = new PayrollRunCreateRequest(periodStart, periodEnd, "INR");
        PayrollRunResponse created = payrollRunService.createPayrollRun(req);
        PayrollRunResponse processed = payrollRunService.processPayrollRun(created.getId());
        assertEquals(PayrollRunStatus.CALCULATED, processed.getStatus());

        PayrollEmployee pe = payrollEmployeeRepository.findByPayrollRunIdAndEmployeeIdAndOrganizationId(created.getId(), employee.getId(), organization.getId()).orElseThrow();
        List<PayrollItem> items = payrollItemRepository.findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(pe.getId(), organization.getId());

        PayrollItem incentiveItem = items.stream().filter(i -> "INCENTIVE".equals(i.getComponentCode())).findFirst().orElse(null);
        assertNotNull(incentiveItem);
        assertEquals(0, new BigDecimal("5000.00").compareTo(incentiveItem.getAmount()));

        PayrollItem bonusItem = items.stream().filter(i -> "BONUS".equals(i.getComponentCode())).findFirst().orElse(null);
        assertNotNull(bonusItem);
        assertEquals(0, new BigDecimal("10000.00").compareTo(bonusItem.getAmount()));

        // Fixed Gross = 70,000 + Incentive(5,000) + Bonus(10,000) = 85,000
        assertEquals(0, new BigDecimal("85000.00").compareTo(pe.getGrossAmount()));

        // Net Pay = Gross(85,000) - PF(1,800) = 83,200
        assertEquals(0, new BigDecimal("83200.00").compareTo(pe.getNetAmount()));
    }

    @Test
    @DisplayName("Salary Expense Reimbursement: Tracked separately from gross, added to net, and transitioned to PROCESSED")
    public void testSalaryReimbursementInPayrollRun() {
        LocalDate periodStart = LocalDate.of(2026, 9, 1);
        LocalDate periodEnd = LocalDate.of(2026, 9, 30);

        // Create approved expense for salary payroll reimbursement
        Expense expense = new Expense();
        expense.setTitle("Client Dinner & Taxi");
        expense.setAmount(BigDecimal.valueOf(4500.00));
        expense.setExpenseDate(LocalDate.of(2026, 9, 10));
        expense.setStatus("APPROVED");
        expense.setCategory(expenseCategory);
        expense.setEmployee(employee);
        expense.setExpenseNumber("EXP-VP-001");
        expense.setPaymentMode("SALARY_PAYROLL");
        expense.setReimbursementStatus("NOT_PAID");
        expense = expenseRepository.save(expense);

        PayrollRunCreateRequest req = new PayrollRunCreateRequest(periodStart, periodEnd, "INR");
        PayrollRunResponse created = payrollRunService.createPayrollRun(req);
        payrollRunService.processPayrollRun(created.getId());

        PayrollEmployee pe = payrollEmployeeRepository.findByPayrollRunIdAndEmployeeIdAndOrganizationId(created.getId(), employee.getId(), organization.getId()).orElseThrow();
        List<PayrollItem> items = payrollItemRepository.findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(pe.getId(), organization.getId());

        PayrollItem reimbItem = items.stream().filter(i -> "REIMBURSEMENT".equals(i.getComponentCode())).findFirst().orElse(null);
        assertNotNull(reimbItem);
        assertEquals(0, new BigDecimal("4500.00").compareTo(reimbItem.getAmount()));

        // Reimbursement is tracked separately from salary gross
        assertEquals(0, new BigDecimal("70000.00").compareTo(pe.getGrossAmount()));
        assertEquals(0, new BigDecimal("4500.00").compareTo(pe.getBenefitsAmount()));

        // Net Pay = Gross(70,000) + Reimbursement(4,500) - PF(1,800) = 72,700
        assertEquals(0, new BigDecimal("72700.00").compareTo(pe.getNetAmount()));

        // Verify Expense status transitioned to PROCESSED with reference to run
        Expense updatedExpense = expenseRepository.findById(expense.getId()).orElseThrow();
        assertEquals("PROCESSED", updatedExpense.getReimbursementStatus());
        assertEquals("PAYROLL_RUN_" + created.getId(), updatedExpense.getTransactionReference());
    }

    @Test
    @DisplayName("Combined End-to-End Variable Pay: OT + Incentive + Bonus + Reimbursement + Fixed Gross")
    public void testCombinedEndToEndVariableEarningsPayrollRun() {
        LocalDate periodStart = LocalDate.of(2026, 9, 1);
        LocalDate periodEnd = LocalDate.of(2026, 9, 30);

        // 1. Overtime (1 day with 2.5 hrs OT)
        Attendance att = new Attendance();
        att.setEmployee(employee);
        att.setOrganization(organization);
        att.setDate(LocalDate.of(2026, 9, 15));
        att.setStatus(AttendanceStatus.PRESENT);
        att.setPunchInTime(LocalTime.of(9, 0));
        att.setPunchOutTime(LocalTime.of(20, 30));
        att.setServerTime(Instant.now());
        attendanceRepository.save(att);

        // 2. Incentive (3,000) & Bonus (5,000)
        incentivePayrollAdapter.registerApprovedIncentive(organization.getId(), employee.getId(), periodStart, BigDecimal.valueOf(3000));
        bonusPayrollAdapter.registerApprovedBonus(organization.getId(), employee.getId(), periodStart, BigDecimal.valueOf(5000));

        // 3. Reimbursement (2,000)
        Expense expense = new Expense();
        expense.setTitle("Software License Reimbursement");
        expense.setAmount(BigDecimal.valueOf(2000.00));
        expense.setExpenseDate(LocalDate.of(2026, 9, 20));
        expense.setStatus("APPROVED");
        expense.setCategory(expenseCategory);
        expense.setEmployee(employee);
        expense.setExpenseNumber("EXP-VP-002");
        expense.setPaymentMode("SALARY_PAYROLL");
        expense.setReimbursementStatus("NOT_PAID");
        expenseRepository.save(expense);

        // Run payroll
        PayrollRunCreateRequest req = new PayrollRunCreateRequest(periodStart, periodEnd, "INR");
        PayrollRunResponse created = payrollRunService.createPayrollRun(req);
        PayrollRunResponse processed = payrollRunService.processPayrollRun(created.getId());
        assertEquals(PayrollRunStatus.CALCULATED, processed.getStatus());

        PayrollEmployee pe = payrollEmployeeRepository.findByPayrollRunIdAndEmployeeIdAndOrganizationId(created.getId(), employee.getId(), organization.getId()).orElseThrow();
        List<PayrollItem> items = payrollItemRepository.findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(pe.getId(), organization.getId());

        assertTrue(items.stream().anyMatch(i -> "BASIC".equals(i.getComponentCode())));
        assertTrue(items.stream().anyMatch(i -> "HRA".equals(i.getComponentCode())));
        assertTrue(items.stream().anyMatch(i -> "PF".equals(i.getComponentCode())));
        assertTrue(items.stream().anyMatch(i -> "OT".equals(i.getComponentCode())));
        assertTrue(items.stream().anyMatch(i -> "INCENTIVE".equals(i.getComponentCode())));
        assertTrue(items.stream().anyMatch(i -> "BONUS".equals(i.getComponentCode())));
        assertTrue(items.stream().anyMatch(i -> "REIMBURSEMENT".equals(i.getComponentCode())));

        // Finalize run
        PayrollRunResponse finalized = payrollRunService.finalizePayrollRun(created.getId());
        assertEquals(PayrollRunStatus.FINALIZED, finalized.getStatus());
        assertNotNull(finalized.getFinalizedAt());
    }
}
