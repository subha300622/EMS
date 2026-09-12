package com.example.ems.leave;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.dto.LeaveRequest;
import com.example.ems.leave.entity.*;
import com.example.ems.leave.repository.*;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LeaveWorkflowIntegrationTest {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private LeaveRuleRepository leaveRuleRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization organization;
    private Employee employee;
    private Employee teamLead;
    private Employee hrManager;
    private LeaveType casualLeaveType;
    private LeavePolicy standardPolicy;

    @BeforeEach
    public void setUp() {
        // 1. Setup Organization
        organization = new Organization();
        organization.setName("Workflow Test Org");
        organization.setOrganizationCode("ORG_WORKFLOW_" + System.currentTimeMillis());
        organization = organizationRepository.save(organization);

        // 2. Setup Employees (TL, HR, Employee)
        teamLead = new Employee();
        teamLead.setEmployeeId("TL_001");
        teamLead.setFullName("Team Lead");
        teamLead.setEmail("tl@workflowtest.com");
        teamLead.setDepartment("Engineering");
        teamLead.setOrganization(organization);
        teamLead = employeeRepository.save(teamLead);

        hrManager = new Employee();
        hrManager.setEmployeeId("HR_001");
        hrManager.setFullName("HR Lead");
        hrManager.setEmail("hr@workflowtest.com");
        hrManager.setDepartment("HR");
        hrManager.setOrganization(organization);
        hrManager = employeeRepository.save(hrManager);

        employee = new Employee();
        employee.setEmployeeId("EMP_WF_001");
        employee.setFullName("Workflow Employee");
        employee.setEmail("emp@workflowtest.com");
        employee.setDepartment("Engineering");
        employee.setManager(teamLead);
        employee.setOrganization(organization);
        employee = employeeRepository.save(employee);

        // 3. Setup Leave Type
        casualLeaveType = new LeaveType();
        casualLeaveType.setName("Workflow Casual Leave");
        casualLeaveType.setDescription("Casual Leave for End-to-End Workflow Test");
        casualLeaveType.setDefaultDays(12);
        casualLeaveType.setActive(true);
        casualLeaveType.setOrganization(organization);
        casualLeaveType = leaveTypeRepository.save(casualLeaveType);

        // 4. Setup Leave Policy
        standardPolicy = new LeavePolicy();
        standardPolicy.setName("Standard Policy 2026");
        standardPolicy.setLeaveType(casualLeaveType);
        standardPolicy.setAccrualType("ANNUAL");
        standardPolicy.setStatus("ACTIVE");
        standardPolicy.setOrganization(organization);
        standardPolicy = leavePolicyRepository.save(standardPolicy);

        // 5. Setup Leave Rule
        LeaveRule noticeRule = new LeaveRule();
        noticeRule.setLeaveType(casualLeaveType);
        noticeRule.setNoticePeriodDays(1);
        noticeRule.setMaxConsecutiveDays(10);
        noticeRule.setAllowNegativeBalance(false);
        noticeRule.setAllowHalfDay(true);
        noticeRule.setIncludeWeekends(false);
        noticeRule.setIncludeHolidays(false);
        noticeRule.setOrganization(organization);
        leaveRuleRepository.save(noticeRule);

        // 6. Setup Initial Leave Balance (Entitlement = 12, Used = 0, Pending = 0)
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(employee);
        balance.setLeaveType(casualLeaveType);
        balance.setYear(2026);
        balance.setTotalEntitlement(12.0);
        balance.setUsedBalance(0.0);
        balance.setPendingBalance(0.0);
        balance.setOrganization(organization);
        leaveBalanceRepository.save(balance);
    }

    @Test
    @DisplayName("Phase 1: Leave Type creation and validation")
    public void testLeaveTypeLifecycle() {
        LeaveType type = new LeaveType();
        type.setName("Maternity Leave");
        type.setDescription("Maternity benefits");
        type.setDefaultDays(180);
        type.setActive(true);
        type.setOrganization(organization);

        LeaveType saved = leaveTypeRepository.save(type);
        assertNotNull(saved.getId());
        assertEquals("Maternity Leave", saved.getName());

        saved.setActive(false);
        leaveTypeRepository.save(saved);
        assertFalse(leaveTypeRepository.findById(saved.getId()).get().isActive());
    }

    @Test
    @DisplayName("Phase 3 & 4: Leave submission reserves pending balance and creates request")
    public void testLeaveSubmissionAndBalanceReservation() {
        LeaveRequest request = new LeaveRequest();
        request.setLeaveTypeId(casualLeaveType.getId());
        request.setStartDate(LocalDate.of(2026, 11, 10));
        request.setEndDate(LocalDate.of(2026, 11, 12));
        request.setReason("Vacation");

        Leave leave = leaveService.applyLeave(employee, request);

        assertNotNull(leave.getId());
        assertEquals("PENDING", leave.getStatus());
        assertEquals(3.0, leave.getDurationDays());

        // Verify Balance Reservation: Entitlement=12, Used=0, Pending=3, Available=9
        Optional<LeaveBalance> optBal = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), casualLeaveType.getId(), 2026
        );
        assertTrue(optBal.isPresent());
        LeaveBalance bal = optBal.get();
        assertEquals(3.0, bal.getPendingBalance());
        assertEquals(9.0, bal.getAvailableBalance());
    }

    @Test
    @DisplayName("Phase 4: Overlapping leave request is rejected")
    public void testOverlappingLeaveRejection() {
        LeaveRequest req1 = new LeaveRequest();
        req1.setLeaveTypeId(casualLeaveType.getId());
        req1.setStartDate(LocalDate.of(2026, 11, 10));
        req1.setEndDate(LocalDate.of(2026, 11, 12));
        req1.setReason("First Leave");
        leaveService.applyLeave(employee, req1);

        // Overlapping request
        LeaveRequest req2 = new LeaveRequest();
        req2.setLeaveTypeId(casualLeaveType.getId());
        req2.setStartDate(LocalDate.of(2026, 11, 11));
        req2.setEndDate(LocalDate.of(2026, 11, 15));
        req2.setReason("Overlapping Leave");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            leaveService.applyLeave(employee, req2);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("overlap"));
    }

    @Test
    @DisplayName("Phase 5: Approval updates leave to APPROVED, converts pending to used balance without double deduction")
    public void testLeaveApprovalWorkflow() {
        LeaveRequest request = new LeaveRequest();
        request.setLeaveTypeId(casualLeaveType.getId());
        request.setStartDate(LocalDate.of(2026, 12, 1));
        request.setEndDate(LocalDate.of(2026, 12, 3));
        request.setReason("Year-end break");
        Leave leave = leaveService.applyLeave(employee, request);

        // Manager / TL approves leave with comment
        leaveService.approveLeaveWithComment(leave.getId(), "Approved by Manager", teamLead);

        Leave updatedLeave = leaveRepository.findById(leave.getId()).get();
        assertEquals("APPROVED", updatedLeave.getStatus());
        assertNotNull(updatedLeave.getApprovedAt());
        assertEquals("Approved by Manager", updatedLeave.getManagerComment());

        // Balance check: Pending=0, Used=3, Available=9, Entitlement=12
        LeaveBalance bal = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), casualLeaveType.getId(), 2026
        ).get();
        assertEquals(0.0, bal.getPendingBalance());
        assertEquals(3.0, bal.getUsedBalance());
        assertEquals(9.0, bal.getAvailableBalance());
    }

    @Test
    @DisplayName("Phase 5: Rejection releases pending balance back to available")
    public void testLeaveRejectionWorkflow() {
        LeaveRequest request = new LeaveRequest();
        request.setLeaveTypeId(casualLeaveType.getId());
        request.setStartDate(LocalDate.of(2026, 12, 10));
        request.setEndDate(LocalDate.of(2026, 12, 12));
        request.setReason("Trip");
        Leave leave = leaveService.applyLeave(employee, request);

        // Reject
        leaveService.rejectLeaveWithComment(leave.getId(), "Business critical project timeline", teamLead);

        Leave updatedLeave = leaveRepository.findById(leave.getId()).get();
        assertEquals("REJECTED", updatedLeave.getStatus());

        // Balance restored: Pending=0, Used=0, Available=12
        LeaveBalance bal = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), casualLeaveType.getId(), 2026
        ).get();
        assertEquals(0.0, bal.getPendingBalance());
        assertEquals(0.0, bal.getUsedBalance());
        assertEquals(12.0, bal.getAvailableBalance());
    }

    @Test
    @DisplayName("Phase 7: Cancellation restores leave balance")
    public void testLeaveCancellationWorkflow() {
        LeaveRequest request = new LeaveRequest();
        request.setLeaveTypeId(casualLeaveType.getId());
        request.setStartDate(LocalDate.of(2026, 12, 20));
        request.setEndDate(LocalDate.of(2026, 12, 22));
        request.setReason("Personal");
        Leave leave = leaveService.applyLeave(employee, request);

        // Approve first
        leaveService.approveLeaveWithComment(leave.getId(), "Approved", teamLead);

        // Cancel approved leave
        leaveService.cancelLeave(leave.getId(), employee);

        Leave cancelledLeave = leaveRepository.findById(leave.getId()).get();
        assertEquals("CANCELLED", cancelledLeave.getStatus());

        // Used balance restored: Used=0, Available=12
        LeaveBalance bal = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), casualLeaveType.getId(), 2026
        ).get();
        assertEquals(0.0, bal.getUsedBalance());
        assertEquals(12.0, bal.getAvailableBalance());
    }

    @Test
    @DisplayName("Phase 8: Calendar event retrieval for employee")
    public void testLeaveCalendarRetrieval() {
        LeaveRequest request = new LeaveRequest();
        request.setLeaveTypeId(casualLeaveType.getId());
        request.setStartDate(LocalDate.of(2026, 9, 15));
        request.setEndDate(LocalDate.of(2026, 9, 17));
        request.setReason("Conference");
        leaveService.applyLeave(employee, request);

        List<Leave> calendar = leaveService.getEmployeeCalendar(
                employee.getId(),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
        );
        assertNotNull(calendar);
        assertFalse(calendar.isEmpty());
        assertEquals(LocalDate.of(2026, 9, 15), calendar.get(0).getStartDate());
    }
}
