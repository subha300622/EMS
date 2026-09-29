package com.example.ems.payroll;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.dto.AssignLeavePolicyRequest;
import com.example.ems.leave.dto.CreateEncashmentRequest;
import com.example.ems.leave.dto.CreateLeaveRuleRequest;
import com.example.ems.leave.dto.LeaveRequest;
import com.example.ems.leave.entity.*;
import com.example.ems.leave.repository.*;
import com.example.ems.leave.service.LeaveEncashmentService;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.payroll.dto.SalaryStructureRequest;
import com.example.ems.payroll.entity.Payroll;
import com.example.ems.payroll.repository.PayrollRepository;
import com.example.ems.payroll.service.PayrollService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LeavePayrollIntegrationTest {

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private LeaveEncashmentService leaveEncashmentService;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveEncashmentRepository leaveEncashmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private com.example.ems.employee.repository.DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization organization;
    private com.example.ems.employee.entity.Department department;
    private Employee employee;
    private Employee manager;
    private LeaveType leaveType;
    private LeavePolicy leavePolicy;

    @BeforeEach
    public void setUp() {
        long ts = System.currentTimeMillis();
        organization = new Organization();
        organization.setName("Payroll Integration Test Org " + ts);
        organization.setOrganizationCode("ORG_PAY_" + ts);
        organization = organizationRepository.save(organization);

        department = new com.example.ems.employee.entity.Department();
        department.setName("PayrollTestDept_" + ts);
        department.setCode("DEPT_" + ts);
        department.setOrganization(organization);
        department = departmentRepository.save(department);

        manager = new Employee();
        manager.setEmployeeId("MGR_" + ts);
        manager.setFullName("Manager User");
        manager.setEmail("mgr_" + ts + "@payrolltest.com");
        manager.setDepartment(department.getName());
        manager.setStatus("ACTIVE");
        manager.setOrganization(organization);
        manager = employeeRepository.save(manager);

        employee = new Employee();
        employee.setEmployeeId("EMP_" + ts);
        employee.setFullName("Alice Engineer");
        employee.setEmail("alice_" + ts + "@payrolltest.com");
        employee.setDepartment(department.getName());
        employee.setStatus("ACTIVE");
        employee.setManager(manager);
        employee.setOrganization(organization);
        employee = employeeRepository.save(employee);

        // Configure salary structure: Basic = 50,000, HRA = 20,000, Allowances = 10,000 -> Gross = 80,000
        SalaryStructureRequest ssReq = new SalaryStructureRequest();
        ssReq.setEmployeeId(employee.getId());
        ssReq.setBasicSalary(BigDecimal.valueOf(50000));
        ssReq.setHra(BigDecimal.valueOf(20000));
        ssReq.setAllowances(BigDecimal.valueOf(10000));
        payrollService.saveSalaryStructure(ssReq);

        // Configure Leave Type and Policy
        leaveType = new LeaveType();
        leaveType.setName("Paid Vacation " + ts);
        leaveType.setDescription("Vacation Leave");
        leaveType.setDefaultDays(12);
        leaveType.setActive(true);
        leaveType.setOrganization(organization);
        leaveType = leaveTypeRepository.save(leaveType);

        leavePolicy = new LeavePolicy();
        leavePolicy.setName("Standard Vacation Policy " + ts);
        leavePolicy.setLeaveType(leaveType);
        leavePolicy.setCarryingLimit(12);
        leavePolicy.setAccrualType("ANNUAL");
        leavePolicy.setStatus("ACTIVE");
        leavePolicy.setOrganization(organization);
        leavePolicy = leavePolicyRepository.save(leavePolicy);
    }

    @Test
    @DisplayName("End-to-End: 5-Day Leave Request (2 Paid, 3 LOP) -> Approval -> Payroll LOP Deduction")
    public void testLopDeductionInPayrollRun() {
        // 1. Configure policy rule with allowLop = true
        CreateLeaveRuleRequest ruleReq = new CreateLeaveRuleRequest();
        ruleReq.setLeaveTypeId(leaveType.getId());
        ruleReq.setIncludeWeekends(false);
        ruleReq.setIncludeHolidays(false);
        ruleReq.setAllowLop(true);
        ruleReq.setAllowNegativeBalance(false);
        ruleReq.setAllowHalfDay(true);
        ruleReq.setMaxConsecutiveDays(10);
        leaveService.createLeaveRule(manager, ruleReq);

        // 2. Assign policy and initialize 2 available days
        AssignLeavePolicyRequest assignReq = new AssignLeavePolicyRequest(List.of(employee.getId()), null);
        leaveService.assignPolicy(leavePolicy.getId(), assignReq, manager);

        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), leaveType.getId(), LocalDate.now().getYear()).orElseThrow();
        balance.setTotalEntitlement(2.0);
        balance.setUsedBalance(0.0);
        balance.setPendingBalance(0.0);
        leaveBalanceRepository.save(balance);

        // 3. Employee requests 5 days leave in June 2026 (e.g. 2026-06-01 to 2026-06-05)
        LeaveRequest leaveReq = new LeaveRequest(
                leaveType.getId(),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 5),
                "FULL_DAY",
                "Family vacation"
        );
        Leave leave = leaveService.applyLeave(employee, leaveReq);
        assertEquals(5.0, leave.getDurationDays());
        assertEquals(2.0, leave.getPaidDays());
        assertEquals(3.0, leave.getLopDays());

        // 4. Manager approves the leave request
        leaveService.approveLeave(leave.getId(), manager);

        // 5. Run payroll for June 2026
        Map<String, Object> runResult = payrollService.processPayrollRun("2026-06", department.getId());
        assertEquals("SUCCESS", runResult.get("status"));

        // 6. Fetch payroll record and verify two-stage rounding calculations
        Payroll p = payrollRepository.findByEmployeeIdAndMonthAndYear(employee.getId(), 6, 2026).orElseThrow();

        LocalDate periodStart = LocalDate.of(2026, 6, 1);
        LocalDate periodEnd = LocalDate.of(2026, 6, 30);
        int expectedWorkingDays = payrollService.calculateWorkingDays(organization.getId(), periodStart, periodEnd);
        assertEquals(22, expectedWorkingDays);

        int expectedPaidDays = 22 - 3; // 19
        assertEquals(expectedPaidDays, p.getPaidDays());
        assertEquals(22, p.getWorkingDays());

        // Gross = 80,000. Daily Rate = 80,000 / 22 = 3636.36
        BigDecimal gross = BigDecimal.valueOf(80000);
        BigDecimal expectedDailyRate = gross.divide(BigDecimal.valueOf(22), 2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("3636.36"), expectedDailyRate);

        // LOP Deduction = 3636.36 * 3 = 10909.08
        BigDecimal expectedLopDeduction = expectedDailyRate.multiply(BigDecimal.valueOf(3)).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("10909.08"), expectedLopDeduction);

        // Total Deductions = PF(1800) + Tax(5000) + ESI(500) + LOP(10909.08) = 18209.08
        BigDecimal expectedDeductions = BigDecimal.valueOf(1800).add(BigDecimal.valueOf(5000)).add(BigDecimal.valueOf(500)).add(expectedLopDeduction);
        assertEquals(expectedDeductions, p.getDeductions());

        // Net Pay = 80,000 - 18,209.08 = 61,790.92
        BigDecimal expectedNetPay = gross.subtract(expectedDeductions);
        assertEquals(expectedNetPay, p.getNetPay());
    }

    @Test
    @DisplayName("End-to-End: Leave Encashment -> Approval -> Payroll Credit & Idempotency")
    public void testLeaveEncashmentInPayrollRun() {
        // 1. Configure policy rule with allowEncashment = true
        CreateLeaveRuleRequest ruleReq = new CreateLeaveRuleRequest();
        ruleReq.setLeaveTypeId(leaveType.getId());
        ruleReq.setAllowEncashment(true);
        ruleReq.setMaxEncashmentDays(5.0);
        ruleReq.setMinBalanceRetained(2.0);
        leaveService.createLeaveRule(manager, ruleReq);

        // 2. Assign policy and initialize 10 available days
        AssignLeavePolicyRequest assignReq = new AssignLeavePolicyRequest(List.of(employee.getId()), null);
        leaveService.assignPolicy(leavePolicy.getId(), assignReq, manager);

        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), leaveType.getId(), LocalDate.now().getYear()).orElseThrow();
        balance.setTotalEntitlement(10.0);
        balance.setUsedBalance(0.0);
        balance.setPendingBalance(0.0);
        leaveBalanceRepository.save(balance);

        // 3. Request 2 days leave encashment
        CreateEncashmentRequest encReq = new CreateEncashmentRequest(leaveType.getId(), 2.0, "Festival encashment");
        LeaveEncashment enc = leaveEncashmentService.requestEncashment(employee, encReq);
        assertEquals("PENDING", enc.getStatus());
        enc.setRequestedAt(java.time.LocalDateTime.of(2026, 6, 15, 10, 0));
        leaveEncashmentRepository.save(enc);

        // 4. Approve encashment
        leaveEncashmentService.approveEncashment(enc.getId());

        // 5. Run payroll for June 2026
        payrollService.processPayrollRun("2026-06", department.getId());

        // 6. Fetch payroll record and verify encashment addition
        Payroll p = payrollRepository.findByEmployeeIdAndMonthAndYear(employee.getId(), 6, 2026).orElseThrow();

        BigDecimal gross = BigDecimal.valueOf(80000);
        BigDecimal expectedDailyRate = gross.divide(BigDecimal.valueOf(22), 2, RoundingMode.HALF_UP);
        BigDecimal expectedEncashment = expectedDailyRate.multiply(BigDecimal.valueOf(2)).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("7272.72"), expectedEncashment);

        // Allowances includes HRA(20000) + Allowances(10000) + Encashment(7272.72) = 37272.72
        BigDecimal expectedAllowances = new BigDecimal("30000.00").add(expectedEncashment);
        assertEquals(0, expectedAllowances.compareTo(p.getAllowances()));

        // Net Pay = 80,000 + 7272.72 - (1800 + 5000 + 500) = 79,972.72
        BigDecimal standardDeductions = new BigDecimal("7300.00");
        BigDecimal expectedNetPay = gross.add(expectedEncashment).subtract(standardDeductions);
        assertEquals(0, expectedNetPay.compareTo(p.getNetPay()));

        // 7. Verify encashment is marked PROCESSED and will not be paid again in next month
        LeaveEncashment encUpdated = leaveEncashmentService.getEncashmentById(enc.getId());
        assertEquals("PROCESSED", encUpdated.getStatus());

        // Run next month's payroll
        payrollService.processPayrollRun("2026-07", department.getId());
        Payroll pJuly = payrollRepository.findByEmployeeIdAndMonthAndYear(employee.getId(), 7, 2026).orElseThrow();

        // In July, no additional encashment should be credited
        assertEquals(0, new BigDecimal("30000.00").compareTo(pJuly.getAllowances()));
        assertEquals(0, gross.subtract(standardDeductions).compareTo(pJuly.getNetPay()));
    }

    @Test
    @DisplayName("End-to-End: Zero LOP Standard Payroll Cycle")
    public void testZeroLopStandardPayrollCycle() {
        payrollService.processPayrollRun("2026-06", department.getId());
        Payroll p = payrollRepository.findByEmployeeIdAndMonthAndYear(employee.getId(), 6, 2026).orElseThrow();

        assertEquals(22, p.getWorkingDays());
        assertEquals(22, p.getPaidDays());
        assertEquals(0, new BigDecimal("7300.00").compareTo(p.getDeductions()));
        assertEquals(0, new BigDecimal("72700.00").compareTo(p.getNetPay()));
    }
}
