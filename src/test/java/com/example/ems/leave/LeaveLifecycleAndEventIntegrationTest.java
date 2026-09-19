package com.example.ems.leave;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.dto.LeaveRequest;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveBalance;
import com.example.ems.leave.entity.LeavePolicy;
import com.example.ems.leave.entity.LeaveRule;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.event.LeaveApprovedEvent;
import com.example.ems.leave.event.LeaveCancelledEvent;
import com.example.ems.leave.event.LeaveRejectedEvent;
import com.example.ems.leave.event.LeaveRequestedEvent;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import com.example.ems.leave.repository.LeavePolicyRepository;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.repository.LeaveRuleRepository;
import com.example.ems.leave.repository.LeaveTypeRepository;
import com.example.ems.leave.service.LeaveApprovalEventListener;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LeaveLifecycleAndEventIntegrationTest {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private LeaveApprovalEventListener leaveApprovalEventListener;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private LeaveRuleRepository leaveRuleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TestLeaveEventCollector eventCollector;

    private Organization organization;
    private Employee manager;
    private Employee employee;
    private LeaveType annualLeaveType;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestLeaveEventCollector testLeaveEventCollector() {
            return new TestLeaveEventCollector();
        }
    }

    public static class TestLeaveEventCollector {
        public final List<LeaveRequestedEvent> requestedEvents = new CopyOnWriteArrayList<>();
        public final List<LeaveApprovedEvent> approvedEvents = new CopyOnWriteArrayList<>();
        public final List<LeaveRejectedEvent> rejectedEvents = new CopyOnWriteArrayList<>();
        public final List<LeaveCancelledEvent> cancelledEvents = new CopyOnWriteArrayList<>();

        public void clear() {
            requestedEvents.clear();
            approvedEvents.clear();
            rejectedEvents.clear();
            cancelledEvents.clear();
        }

        @EventListener
        public void onRequested(LeaveRequestedEvent event) {
            requestedEvents.add(event);
        }

        @EventListener
        public void onApproved(LeaveApprovedEvent event) {
            approvedEvents.add(event);
        }

        @EventListener
        public void onRejected(LeaveRejectedEvent event) {
            rejectedEvents.add(event);
        }

        @EventListener
        public void onCancelled(LeaveCancelledEvent event) {
            cancelledEvents.add(event);
        }
    }

    @BeforeEach
    public void setUp() {
        eventCollector.clear();

        organization = new Organization();
        organization.setName("Lifecycle Test Org " + System.currentTimeMillis());
        organization.setOrganizationCode("ORG_LC_" + System.currentTimeMillis());
        organization = organizationRepository.save(organization);

        manager = new Employee();
        manager.setEmployeeId("MGR_" + System.currentTimeMillis());
        manager.setFullName("Test Manager");
        manager.setEmail("mgr." + System.currentTimeMillis() + "@test.com");
        manager.setDepartment("Engineering");
        manager.setOrganization(organization);
        manager = employeeRepository.save(manager);

        employee = new Employee();
        employee.setEmployeeId("EMP_" + System.currentTimeMillis());
        employee.setFullName("Test Employee");
        employee.setEmail("emp." + System.currentTimeMillis() + "@test.com");
        employee.setDepartment("Engineering");
        employee.setManager(manager);
        employee.setOrganization(organization);
        employee = employeeRepository.save(employee);

        annualLeaveType = new LeaveType();
        annualLeaveType.setName("Annual Leave " + System.currentTimeMillis());
        annualLeaveType.setDescription("Annual Paid Leave");
        annualLeaveType.setDefaultDays(12);
        annualLeaveType.setActive(true);
        annualLeaveType.setOrganization(organization);
        annualLeaveType = leaveTypeRepository.save(annualLeaveType);

        LeavePolicy policy = new LeavePolicy();
        policy.setName("Standard Policy " + System.currentTimeMillis());
        policy.setLeaveType(annualLeaveType);
        policy.setAccrualType("ANNUAL");
        policy.setStatus("ACTIVE");
        policy.setOrganization(organization);
        leavePolicyRepository.save(policy);

        LeaveRule rule = new LeaveRule();
        rule.setLeaveType(annualLeaveType);
        rule.setNoticePeriodDays(0);
        rule.setMaxConsecutiveDays(30);
        rule.setAllowNegativeBalance(false);
        rule.setAllowHalfDay(true);
        rule.setIncludeWeekends(false);
        rule.setIncludeHolidays(false);
        rule.setOrganization(organization);
        leaveRuleRepository.save(rule);

        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(employee);
        balance.setLeaveType(annualLeaveType);
        balance.setYear(2026);
        balance.setTotalEntitlement(12.0);
        balance.setUsedBalance(0.0);
        balance.setPendingBalance(0.0);
        balance.setOrganization(organization);
        leaveBalanceRepository.save(balance);
    }

    @Test
    @DisplayName("1. Apply Leave reserves pending balance and emits LeaveRequestedEvent")
    public void testApplyLeaveLifecycle() {
        LeaveRequest request = new LeaveRequest();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.of(2026, 11, 2)); // Monday
        request.setEndDate(LocalDate.of(2026, 11, 3));   // Tuesday (2 days)
        request.setReason("Personal Break");

        Leave leave = leaveService.applyLeave(employee, request);

        assertNotNull(leave.getId());
        assertEquals("PENDING", leave.getStatus());
        assertEquals(2.0, leave.getDurationDays());

        // Verify balance
        LeaveBalance bal = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), annualLeaveType.getId(), 2026
        ).orElseThrow();
        assertEquals(2.0, bal.getPendingBalance());
        assertEquals(0.0, bal.getUsedBalance());
        assertEquals(10.0, bal.getAvailableBalance());

        // Verify LeaveRequestedEvent published
        assertEquals(1, eventCollector.requestedEvents.size());
        LeaveRequestedEvent event = eventCollector.requestedEvents.get(0);
        assertEquals(leave.getId(), event.leaveId());
        assertEquals(employee.getEmployeeId(), event.employeeCode());
        assertEquals(2.0, event.durationDays());
    }

    @Test
    @DisplayName("2. Approval via WorkflowEngine commits balance and emits LeaveApprovedEvent")
    public void testApprovalWorkflowCompletedEventApproved() {
        LeaveRequest request = new LeaveRequest();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.of(2026, 11, 2));
        request.setEndDate(LocalDate.of(2026, 11, 3));
        request.setReason("Personal Break");

        Leave leave = leaveService.applyLeave(employee, request);
        eventCollector.clear();

        // Simulate Workflow Engine completing with APPROVED
        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                leave.getApprovalWorkflowInstanceId() != null ? leave.getApprovalWorkflowInstanceId() : "WF-1001",
                WorkflowType.LEAVE_APPROVAL,
                "LEAVE_REQUEST",
                leave.getId().toString(),
                organization.getId(),
                ApprovalStatus.APPROVED
        );

        leaveApprovalEventListener.handleApprovalWorkflowCompleted(event);

        // Verify leave status
        Leave updated = leaveRepository.findById(leave.getId()).orElseThrow();
        assertEquals("APPROVED", updated.getStatus());

        // Verify balance: pending released to used
        LeaveBalance bal = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), annualLeaveType.getId(), 2026
        ).orElseThrow();
        assertEquals(0.0, bal.getPendingBalance());
        assertEquals(2.0, bal.getUsedBalance());
        assertEquals(10.0, bal.getAvailableBalance());

        // Verify event
        assertEquals(1, eventCollector.approvedEvents.size());
        LeaveApprovedEvent approvedEvent = eventCollector.approvedEvents.get(0);
        assertEquals(leave.getId(), approvedEvent.leaveId());
        assertEquals(employee.getEmployeeId(), approvedEvent.employeeCode());
    }

    @Test
    @DisplayName("3. Duplicate APPROVED event is idempotent and does not mutate balance twice")
    public void testDuplicateApprovalIsIdempotent() {
        LeaveRequest request = new LeaveRequest();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.of(2026, 11, 2));
        request.setEndDate(LocalDate.of(2026, 11, 3));
        request.setReason("Personal Break");

        Leave leave = leaveService.applyLeave(employee, request);
        eventCollector.clear();

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "WF-1002",
                WorkflowType.LEAVE_APPROVAL,
                "LEAVE_REQUEST",
                leave.getId().toString(),
                organization.getId(),
                ApprovalStatus.APPROVED
        );

        // First delivery
        leaveApprovalEventListener.handleApprovalWorkflowCompleted(event);

        LeaveBalance balAfterFirst = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), annualLeaveType.getId(), 2026
        ).orElseThrow();
        assertEquals(0.0, balAfterFirst.getPendingBalance());
        assertEquals(2.0, balAfterFirst.getUsedBalance());

        // Second duplicate delivery
        leaveApprovalEventListener.handleApprovalWorkflowCompleted(event);

        LeaveBalance balAfterSecond = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), annualLeaveType.getId(), 2026
        ).orElseThrow();
        assertEquals(0.0, balAfterSecond.getPendingBalance());
        assertEquals(2.0, balAfterSecond.getUsedBalance()); // MUST NOT BE 4.0!
        assertEquals(10.0, balAfterSecond.getAvailableBalance());
    }

    @Test
    @DisplayName("4. Rejection via WorkflowEngine releases pending balance and emits LeaveRejectedEvent")
    public void testRejectionWorkflowCompletedEvent() {
        LeaveRequest request = new LeaveRequest();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.of(2026, 11, 2));
        request.setEndDate(LocalDate.of(2026, 11, 4)); // 3 days
        request.setReason("Trip");

        Leave leave = leaveService.applyLeave(employee, request);
        eventCollector.clear();

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "WF-1003",
                WorkflowType.LEAVE_APPROVAL,
                "LEAVE_REQUEST",
                leave.getId().toString(),
                organization.getId(),
                ApprovalStatus.REJECTED
        );

        leaveApprovalEventListener.handleApprovalWorkflowCompleted(event);

        Leave updated = leaveRepository.findById(leave.getId()).orElseThrow();
        assertEquals("REJECTED", updated.getStatus());

        LeaveBalance bal = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), annualLeaveType.getId(), 2026
        ).orElseThrow();
        assertEquals(0.0, bal.getPendingBalance());
        assertEquals(0.0, bal.getUsedBalance());
        assertEquals(12.0, bal.getAvailableBalance());

        assertEquals(1, eventCollector.rejectedEvents.size());
        LeaveRejectedEvent rejectedEvent = eventCollector.rejectedEvents.get(0);
        assertEquals(leave.getId(), rejectedEvent.leaveId());
    }

    @Test
    @DisplayName("5. Duplicate REJECTED event is idempotent and does not mutate balance twice")
    public void testDuplicateRejectionIsIdempotent() {
        LeaveRequest request = new LeaveRequest();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.of(2026, 11, 2));
        request.setEndDate(LocalDate.of(2026, 11, 4));
        request.setReason("Trip");

        Leave leave = leaveService.applyLeave(employee, request);
        eventCollector.clear();

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "WF-1004",
                WorkflowType.LEAVE_APPROVAL,
                "LEAVE_REQUEST",
                leave.getId().toString(),
                organization.getId(),
                ApprovalStatus.REJECTED
        );

        // First delivery
        leaveApprovalEventListener.handleApprovalWorkflowCompleted(event);

        // Duplicate delivery
        leaveApprovalEventListener.handleApprovalWorkflowCompleted(event);

        LeaveBalance bal = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), annualLeaveType.getId(), 2026
        ).orElseThrow();
        assertEquals(0.0, bal.getPendingBalance());
        assertEquals(0.0, bal.getUsedBalance());
        assertEquals(12.0, bal.getAvailableBalance());
    }

    @Test
    @DisplayName("6. Cancel pending leave releases pending balance and emits LeaveCancelledEvent")
    public void testCancelPendingLeave() {
        LeaveRequest request = new LeaveRequest();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.of(2026, 11, 2));
        request.setEndDate(LocalDate.of(2026, 11, 3));
        request.setReason("Cancel me");

        Leave leave = leaveService.applyLeave(employee, request);
        eventCollector.clear();

        Leave cancelled = leaveService.cancelLeave(leave.getId(), employee);
        assertEquals("CANCELLED", cancelled.getStatus());

        LeaveBalance bal = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), annualLeaveType.getId(), 2026
        ).orElseThrow();
        assertEquals(0.0, bal.getPendingBalance());
        assertEquals(0.0, bal.getUsedBalance());
        assertEquals(12.0, bal.getAvailableBalance());

        assertEquals(1, eventCollector.cancelledEvents.size());
        assertEquals(leave.getId(), eventCollector.cancelledEvents.get(0).leaveId());
    }

    @Test
    @DisplayName("7. Cancel approved leave refunds used balance and emits LeaveCancelledEvent")
    public void testCancelApprovedLeave() {
        LeaveRequest request = new LeaveRequest();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.of(2026, 11, 2));
        request.setEndDate(LocalDate.of(2026, 11, 3));
        request.setReason("Approve then cancel");

        Leave leave = leaveService.applyLeave(employee, request);
        leaveService.approveLeave(leave.getId(), manager);

        LeaveBalance balApproved = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), annualLeaveType.getId(), 2026
        ).orElseThrow();
        assertEquals(2.0, balApproved.getUsedBalance());
        assertEquals(0.0, balApproved.getPendingBalance());

        eventCollector.clear();

        Leave cancelled = leaveService.cancelLeave(leave.getId(), employee);
        assertEquals("CANCELLED", cancelled.getStatus());

        LeaveBalance balCancelled = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), annualLeaveType.getId(), 2026
        ).orElseThrow();
        assertEquals(0.0, balCancelled.getUsedBalance());
        assertEquals(0.0, balCancelled.getPendingBalance());
        assertEquals(12.0, balCancelled.getAvailableBalance());

        assertEquals(1, eventCollector.cancelledEvents.size());
        assertEquals(leave.getId(), eventCollector.cancelledEvents.get(0).leaveId());
    }
}
