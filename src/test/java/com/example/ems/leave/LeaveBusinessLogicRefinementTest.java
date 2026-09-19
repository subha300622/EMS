package com.example.ems.leave;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.dto.AssignLeavePolicyRequest;
import com.example.ems.leave.dto.CreateEncashmentRequest;
import com.example.ems.leave.dto.CreateLeaveRuleRequest;
import com.example.ems.leave.dto.LeaveRequest;
import com.example.ems.leave.entity.*;
import com.example.ems.leave.repository.*;
import com.example.ems.leave.service.LeaveAccrualService;
import com.example.ems.leave.service.LeaveBalanceService;
import com.example.ems.leave.service.LeaveEncashmentService;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LeaveBusinessLogicRefinementTest {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private LeaveAccrualService leaveAccrualService;

    @Autowired
    private LeaveEncashmentService leaveEncashmentService;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private LeaveRuleRepository leaveRuleRepository;

    @Autowired
    private LeaveAccrualRuleRepository accrualRuleRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private EmployeeLeavePolicyRepository employeeLeavePolicyRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization organization;
    private Employee employee;
    private Employee admin;
    private LeaveType casualLeaveType;
    private LeavePolicy casualPolicy;

    @BeforeEach
    public void setUp() {
        organization = new Organization();
        organization.setName("Refinement Test Org");
        organization.setOrganizationCode("ORG_REF_" + System.currentTimeMillis());
        organization = organizationRepository.save(organization);

        admin = new Employee();
        admin.setEmployeeId("ADM_" + System.currentTimeMillis());
        admin.setFullName("Admin User");
        admin.setEmail("admin_" + System.currentTimeMillis() + "@refinement.com");
        admin.setDepartment("HR");
        admin.setOrganization(organization);
        admin = employeeRepository.save(admin);

        employee = new Employee();
        employee.setEmployeeId("EMP_" + System.currentTimeMillis());
        employee.setFullName("John Doe");
        employee.setEmail("john_" + System.currentTimeMillis() + "@refinement.com");
        employee.setDepartment("Engineering");
        employee.setOrganization(organization);
        employee = employeeRepository.save(employee);

        long ts = System.currentTimeMillis();
        casualLeaveType = new LeaveType();
        casualLeaveType.setName("Casual Leave " + ts);
        casualLeaveType.setDescription("Casual Paid Leave");
        casualLeaveType.setDefaultDays(12);
        casualLeaveType.setActive(true);
        casualLeaveType.setOrganization(organization);
        casualLeaveType = leaveTypeRepository.save(casualLeaveType);

        casualPolicy = new LeavePolicy();
        casualPolicy.setName("Casual Leave Policy " + ts);
        casualPolicy.setLeaveType(casualLeaveType);
        casualPolicy.setCarryingLimit(12);
        casualPolicy.setAccrualType("MONTHLY");
        casualPolicy.setStatus("ACTIVE");
        casualPolicy.setOrganization(organization);
        casualPolicy = leavePolicyRepository.save(casualPolicy);
    }

    @Test
    @DisplayName("Gap 1: Policy Assignment provisions EmployeeLeavePolicy and LeaveBalance")
    public void testPolicyAssignmentProvisionsBalance() {
        AssignLeavePolicyRequest request = new AssignLeavePolicyRequest(List.of(employee.getId()), null);
        leaveService.assignPolicy(casualPolicy.getId(), request, admin);

        List<EmployeeLeavePolicy> assignments = employeeLeavePolicyRepository.findByEmployeeIdAndActiveTrue(employee.getId());
        assertEquals(1, assignments.size());
        assertEquals(casualPolicy.getId(), assignments.get(0).getLeavePolicy().getId());

        int currentYear = LocalDate.now().getYear();
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employee.getId(), casualLeaveType.getId(), currentYear).orElse(null);
        assertNotNull(balance);
        assertEquals(12.0, balance.getTotalEntitlement());
    }

    @Test
    @DisplayName("Gap 2: Accrual execution is strictly idempotent per period")
    public void testAccrualIdempotency() {
        LeaveAccrualRule accrualRule = new LeaveAccrualRule();
        accrualRule.setLeaveType(casualLeaveType);
        accrualRule.setOrganization(organization);
        accrualRule.setAnnualQuota(12);
        accrualRule.setCreditAmount(2.0);
        accrualRule.setAccrualFrequency("MONTHLY");
        accrualRule.setActive(true);
        accrualRuleRepository.save(accrualRule);

        // Run accrual 1st time
        List<LeaveAccrualTransaction> txns1 = leaveAccrualService.runAccrualsForOrganization(organization.getId());
        assertFalse(txns1.isEmpty());

        int currentYear = LocalDate.now().getYear();
        LeaveBalance balanceAfterFirstRun = leaveBalanceService.getOrCreateBalance(employee, casualLeaveType, currentYear);
        double entitlementAfterFirstRun = balanceAfterFirstRun.getTotalEntitlement();

        // Run accrual 2nd time in same period -> must skip without extra credit
        List<LeaveAccrualTransaction> txns2 = leaveAccrualService.runAccrualsForOrganization(organization.getId());
        assertTrue(txns2.isEmpty());

        LeaveBalance balanceAfterSecondRun = leaveBalanceService.getOrCreateBalance(employee, casualLeaveType, currentYear);
        assertEquals(entitlementAfterFirstRun, balanceAfterSecondRun.getTotalEntitlement(),
                "Entitlement must not be increased on duplicate accrual run in same period");
    }

    @Test
    @DisplayName("Gap 3: Leave application with LOP enabled splits into paidDays and lopDays, reserving only paidDays")
    public void testLeaveRequestLopCalculationAndReservation() {
        int currentYear = 2026;
        LeaveBalance balance = leaveBalanceService.getOrCreateBalance(employee, casualLeaveType, currentYear);
        balance.setTotalEntitlement(2.0);
        balance.setUsedBalance(0.0);
        balance.setPendingBalance(0.0);
        leaveBalanceService.saveBalance(balance);

        CreateLeaveRuleRequest ruleReq = new CreateLeaveRuleRequest();
        ruleReq.setLeaveTypeId(casualLeaveType.getId());
        ruleReq.setIncludeWeekends(true);
        ruleReq.setIncludeHolidays(true);
        ruleReq.setAllowLop(true);
        ruleReq.setAllowNegativeBalance(false);
        leaveService.createLeaveRule(admin, ruleReq);

        // Apply for 5 days (Monday to Friday)
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 5);
        LeaveRequest req = new LeaveRequest(casualLeaveType.getId(), start, end, "FULL_DAY", "Vacation");

        Leave leave = leaveService.applyLeave(employee, req);
        assertNotNull(leave.getId());
        assertEquals(5.0, leave.getDurationDays());
        assertEquals(2.0, leave.getPaidDays(), "Paid days should equal available balance (2.0)");
        assertEquals(3.0, leave.getLopDays(), "LOP days should equal remainder (3.0)");

        LeaveBalance balanceAfterApply = leaveBalanceService.getOrCreateBalance(employee, casualLeaveType, currentYear);
        assertEquals(2.0, balanceAfterApply.getPendingBalance(), "Only paid days (2.0) should be reserved, NOT lop days");

        // Approve leave
        leaveService.approveLeave(leave.getId(), admin);
        LeaveBalance balanceAfterApprove = leaveBalanceService.getOrCreateBalance(employee, casualLeaveType, currentYear);
        assertEquals(0.0, balanceAfterApprove.getPendingBalance());
        assertEquals(2.0, balanceAfterApprove.getUsedBalance(), "Only paid days (2.0) should be committed to used balance");
    }

    @Test
    @DisplayName("Gap 3: Leave application with LOP disabled rejects when balance is insufficient")
    public void testLeaveRequestLopDisabledRejection() {
        int currentYear = 2026;
        LeaveBalance balance = leaveBalanceService.getOrCreateBalance(employee, casualLeaveType, currentYear);
        balance.setTotalEntitlement(2.0);
        balance.setUsedBalance(0.0);
        balance.setPendingBalance(0.0);
        leaveBalanceService.saveBalance(balance);

        CreateLeaveRuleRequest ruleReq = new CreateLeaveRuleRequest();
        ruleReq.setLeaveTypeId(casualLeaveType.getId());
        ruleReq.setIncludeWeekends(true);
        ruleReq.setIncludeHolidays(true);
        ruleReq.setAllowLop(false);
        ruleReq.setAllowNegativeBalance(false);
        leaveService.createLeaveRule(admin, ruleReq);

        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 5);
        LeaveRequest req = new LeaveRequest(casualLeaveType.getId(), start, end, "FULL_DAY", "Vacation");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> leaveService.applyLeave(employee, req));
        assertTrue(ex.getMessage().contains("Insufficient leave balance"));
        assertTrue(ex.getMessage().contains("Loss of Pay (LOP) is not permitted"));
    }

    @Test
    @DisplayName("Gap 4: Encashment enforces allowEncashment, maxEncashmentDays, and minBalanceRetained")
    public void testEncashmentConstraints() {
        int currentYear = LocalDate.now().getYear();
        LeaveBalance balance = leaveBalanceService.getOrCreateBalance(employee, casualLeaveType, currentYear);
        balance.setTotalEntitlement(20.0);
        balance.setUsedBalance(0.0);
        balance.setPendingBalance(0.0);
        leaveBalanceService.saveBalance(balance);

        CreateLeaveRuleRequest ruleReq = new CreateLeaveRuleRequest();
        ruleReq.setLeaveTypeId(casualLeaveType.getId());
        ruleReq.setAllowEncashment(false);
        LeaveRule rule = leaveService.createLeaveRule(admin, ruleReq);

        // 1. Encashment not allowed
        CreateEncashmentRequest req1 = new CreateEncashmentRequest(casualLeaveType.getId(), 5.0, "Bonus");
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> leaveEncashmentService.requestEncashment(employee, req1));
        assertTrue(ex1.getMessage().contains("not permitted"));

        // 2. Encashment allowed, max days limit = 10, min retained = 5
        rule.setAllowEncashment(true);
        rule.setMaxEncashmentDays(10.0);
        rule.setMinBalanceRetained(5.0);
        leaveRuleRepository.save(rule);

        // Exceeds max allowable limit of 10
        CreateEncashmentRequest reqExceedMax = new CreateEncashmentRequest(casualLeaveType.getId(), 12.0, "Bonus");
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> leaveEncashmentService.requestEncashment(employee, reqExceedMax));
        assertTrue(ex2.getMessage().contains("exceeds the maximum allowable limit"));

        // Violates min balance retained (Balance = 20, Request = 16 -> Remaining = 4 < 5)
        CreateEncashmentRequest reqViolateMin = new CreateEncashmentRequest(casualLeaveType.getId(), 16.0, "Bonus");
        rule.setMaxEncashmentDays(20.0); // bump max to test min retained check
        leaveRuleRepository.save(rule);
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
                () -> leaveEncashmentService.requestEncashment(employee, reqViolateMin));
        assertTrue(ex3.getMessage().contains("violate the minimum balance retention requirement"));

        // Valid encashment (Request 8 days -> remaining 12 >= 5, and 8 <= 20)
        CreateEncashmentRequest validReq = new CreateEncashmentRequest(casualLeaveType.getId(), 8.0, "Bonus");
        LeaveEncashment enc = leaveEncashmentService.requestEncashment(employee, validReq);
        assertNotNull(enc.getId());
        assertEquals("PENDING", enc.getStatus());
        assertEquals(8.0, enc.getDaysEncashed());

        // Approve encashment
        leaveEncashmentService.approveEncashment(enc.getId());
        LeaveBalance balanceAfterApprove = leaveBalanceService.getOrCreateBalance(employee, casualLeaveType, currentYear);
        assertEquals(8.0, balanceAfterApprove.getUsedBalance());
        assertEquals(12.0, balanceAfterApprove.getAvailableBalance());
    }
}
