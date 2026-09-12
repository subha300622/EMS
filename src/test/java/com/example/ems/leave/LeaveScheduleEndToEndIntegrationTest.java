package com.example.ems.leave;

import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.holiday.dto.HolidayCheckResponse;
import com.example.ems.holiday.dto.HolidayCreateRequest;
import com.example.ems.holiday.dto.HolidayResponseDto;
import com.example.ems.holiday.repository.HolidayRepository;
import com.example.ems.holiday.service.HolidayService;
import com.example.ems.leave.dto.*;
import com.example.ems.leave.entity.*;
import com.example.ems.leave.event.LeaveApprovedEvent;
import com.example.ems.leave.repository.*;
import com.example.ems.leave.service.LeaveBalanceService;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.schedule.dto.EmployeeAvailabilityDto;
import com.example.ems.schedule.dto.ScheduleCreateRequest;
import com.example.ems.schedule.dto.ScheduleDto;
import com.example.ems.schedule.entity.ScheduleException;
import com.example.ems.schedule.listener.LeaveScheduleEventListener;
import com.example.ems.schedule.repository.ScheduleExceptionRepository;
import com.example.ems.schedule.service.ScheduleManagementService;
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
public class LeaveScheduleEndToEndIntegrationTest {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private ScheduleManagementService scheduleManagementService;

    @Autowired
    private ScheduleExceptionRepository scheduleExceptionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private LeaveScheduleEventListener leaveScheduleEventListener;

    private User currentUser;
    private Organization testOrg;
    private Employee testEmp;

    @BeforeEach
    public void setUp() {
        scheduleExceptionRepository.deleteAll();
        leaveRepository.deleteAll();
        leaveBalanceRepository.deleteAll();
        leavePolicyRepository.deleteAll();
        leaveTypeRepository.deleteAll();
        holidayRepository.deleteAll();

        // Setup Organization & Employee
        testOrg = new Organization();
        testOrg.setName("Acme Hospital E2E");
        testOrg.setOrganizationCode("ORG-ACME-HOSP");
        testOrg = organizationRepository.save(testOrg);

        testEmp = new Employee();
        testEmp.setEmployeeId("EMP-1001");
        testEmp.setFirstName("John");
        testEmp.setLastName("Doe");
        testEmp.setEmail("john.doe@acme.com");
        testEmp.setOrganization(testOrg);
        testEmp = employeeRepository.save(testEmp);

        currentUser = new User();
        currentUser.setWorkEmail("john.doe@acme.com");
        currentUser.setOrganization(testOrg);
    }

    @Test
    @DisplayName("Exact 19-Step Verification: Holiday -> Leave -> Approval -> Schedule Integration Flow")
    public void testExact19StepHolidayLeaveApprovalScheduleFlow() {
        int year = 2026;

        // TEST 1 — Create Holiday (Sep 3, 2026: Hospital Foundation Day)
        HolidayCreateRequest holReq = new HolidayCreateRequest();
        holReq.setName("Hospital Foundation Day");
        holReq.setHolidayDate(LocalDate.of(2026, 9, 3));
        HolidayResponseDto createdHoliday = holidayService.createHoliday(currentUser, holReq);
        assertNotNull(createdHoliday);
        assertEquals("Hospital Foundation Day", createdHoliday.getName());
        assertEquals(LocalDate.of(2026, 9, 3), createdHoliday.getHolidayDate());

        // TEST 2 — Verify Holiday
        HolidayCheckResponse holCheck = holidayService.checkHoliday(currentUser, LocalDate.of(2026, 9, 3));
        assertTrue(holCheck.getIsHoliday());
        assertEquals("Hospital Foundation Day", holCheck.getHolidayName());

        // TEST 3 — Create Leave Type
        LeaveTypeRequest typeReq = new LeaveTypeRequest();
        typeReq.setName("Annual Leave E2E");
        typeReq.setDescription("Paid annual leave");
        typeReq.setDefaultDays(10);
        LeaveType annualLeaveType = leaveService.createLeaveType(testEmp, typeReq);
        assertNotNull(annualLeaveType);
        assertTrue(annualLeaveType.isActive());

        // TEST 4 — Create Policy
        LeavePolicyRequest policyReq = new LeavePolicyRequest();
        policyReq.setName("Standard Leave Policy");
        policyReq.setLeaveTypeId(annualLeaveType.getId());
        LeavePolicy annualPolicy = leaveService.createLeavePolicy(testEmp, policyReq);
        assertNotNull(annualPolicy);

        // TEST 5 — Assign Policy / Initialize Balance
        leaveBalanceService.getOrCreateBalance(testEmp, annualLeaveType, year);

        // TEST 6 — Check Initial Balance (Available = 10, Reserved = 0, Used = 0)
        List<LeaveBalance> initialBalances = leaveBalanceService.getEmployeeBalances(testEmp.getId(), year);
        assertFalse(initialBalances.isEmpty());
        LeaveBalance initialBal = initialBalances.get(0);
        assertEquals(10.0, initialBal.getAvailableBalance());
        assertEquals(0.0, initialBal.getPendingBalance());
        assertEquals(0.0, initialBal.getUsedBalance());

        // TEST 7 — Apply Leave Including Holiday (2026-09-01 -> 2026-09-05: 5 calendar days)
        // Sep 01 (Tue) = Working (1), Sep 02 (Wed) = Working (1), Sep 03 (Thu) = Holiday (Excluded), Sep 04 (Fri) = Working (1), Sep 05 (Sat) = Weekend (Excluded)
        // Net leave days = 3 days!
        LeaveRequest applyReq = new LeaveRequest();
        applyReq.setLeaveTypeId(annualLeaveType.getId());
        applyReq.setStartDate(LocalDate.of(2026, 9, 1));
        applyReq.setEndDate(LocalDate.of(2026, 9, 5));
        applyReq.setReason("Personal Vacation");

        Leave leaveReq = leaveService.applyLeave(testEmp, applyReq);
        assertEquals("PENDING", leaveReq.getStatus());
        assertEquals(3.0, leaveReq.getDurationDays(), "Holiday (Sep 3) and Weekend (Sep 5) MUST be excluded from net leave days");
        Long leaveRequestId = leaveReq.getId();

        // TEST 8 — Verify Balance after PENDING application (Available = 7, Reserved = 3, Used = 0)
        LeaveBalance pendingBal = leaveBalanceService.getEmployeeBalances(testEmp.getId(), year).get(0);
        assertEquals(7.0, pendingBal.getAvailableBalance());
        assertEquals(3.0, pendingBal.getPendingBalance(), "Reserved balance MUST be 3 (excluding holiday & weekend), NOT 5!");
        assertEquals(0.0, pendingBal.getUsedBalance());

        // TEST 9 — CRITICAL TEST: Check Schedule Availability BEFORE approval (PENDING leave does NOT block Schedule)
        EmployeeAvailabilityDto pendingAvailability = scheduleManagementService.getEmployeeAvailability(
                currentUser, "EMP-1001", LocalDate.of(2026, 9, 2));
        assertTrue(pendingAvailability.isAvailable(), "CRITICAL PROOF: PENDING leave MUST NOT block Schedule!");
        assertEquals("NONE", pendingAvailability.getReason());
        assertNull(pendingAvailability.getLeaveRequestId());

        // TEST 10 — Approve Leave
        Leave approvedLeave = leaveService.approveLeave(leaveRequestId, testEmp);
        assertEquals("APPROVED", approvedLeave.getStatus());

        // Verify Balance after APPROVAL (Available = 7, Reserved = 0, Used = 3) -- NO double deduction!
        LeaveBalance approvedBal = leaveBalanceService.getEmployeeBalances(testEmp.getId(), year).get(0);
        assertEquals(7.0, approvedBal.getAvailableBalance());
        assertEquals(0.0, approvedBal.getPendingBalance());
        assertEquals(3.0, approvedBal.getUsedBalance());

        // TEST 11 — Verify Schedule Exception in Database
        Optional<ScheduleException> excOpt = scheduleExceptionRepository.findByLeaveRequestIdAndExceptionType(leaveRequestId, "LEAVE");
        assertTrue(excOpt.isPresent(), "ScheduleException record must exist for approved leave");
        ScheduleException scheduleException = excOpt.get();
        assertEquals("EMP-1001", scheduleException.getEmployeeId());
        assertEquals("LEAVE", scheduleException.getExceptionType());
        assertEquals(LocalDate.of(2026, 9, 1), scheduleException.getStartDate());
        assertEquals(LocalDate.of(2026, 9, 5), scheduleException.getEndDate());
        assertEquals("ACTIVE", scheduleException.getStatus());

        // TEST 12 — Verify Schedule Availability on Working Leave Date (Sep 2)
        EmployeeAvailabilityDto approvedAvailability = scheduleManagementService.getEmployeeAvailability(
                currentUser, "EMP-1001", LocalDate.of(2026, 9, 2));
        assertFalse(approvedAvailability.isAvailable(), "APPROVED leave MUST render employee unavailable!");
        assertEquals("LEAVE", approvedAvailability.getReason());
        assertEquals(leaveRequestId, approvedAvailability.getLeaveRequestId());

        // TEST 13 — Verify Holiday Availability Priority on Sep 3 (Holiday Priority!)
        EmployeeAvailabilityDto holAvailability = scheduleManagementService.getEmployeeAvailability(
                currentUser, "EMP-1001", LocalDate.of(2026, 9, 3));
        assertFalse(holAvailability.isAvailable());
        assertEquals("HOLIDAY", holAvailability.getReason(), "Holiday date MUST take priority with reason = HOLIDAY");

        // TEST 14 — Try Creating Shift During Leave (Sep 2) -> Must fail with EMPLOYEE_ON_LEAVE
        ScheduleCreateRequest shiftReq = new ScheduleCreateRequest();
        shiftReq.setEmployeeId("EMP-1001");
        shiftReq.setDate("2026-09-02");
        shiftReq.setStartTime("09:00");
        shiftReq.setEndTime("18:00");

        IllegalStateException leaveConflict = assertThrows(IllegalStateException.class, () -> {
            scheduleManagementService.createSchedule(currentUser, shiftReq);
        });
        assertTrue(leaveConflict.getMessage().contains("EMPLOYEE_ON_LEAVE"), "Shift creation during approved leave must be rejected");

        // TEST 15 — Try Creating Shift on Holiday (Sep 3) -> Must fail with EMPLOYEE_UNAVAILABLE_HOLIDAY
        ScheduleCreateRequest holShiftReq = new ScheduleCreateRequest();
        holShiftReq.setEmployeeId("EMP-1001");
        holShiftReq.setDate("2026-09-03");
        holShiftReq.setStartTime("09:00");
        holShiftReq.setEndTime("18:00");

        IllegalStateException holConflict = assertThrows(IllegalStateException.class, () -> {
            scheduleManagementService.createSchedule(currentUser, holShiftReq);
        });
        assertTrue(holConflict.getMessage().contains("EMPLOYEE_UNAVAILABLE_HOLIDAY"), "Shift creation on holiday must be rejected");

        // TEST 16 — Cancel Approved Leave
        Leave cancelledLeave = leaveService.cancelLeave(leaveRequestId, testEmp);
        assertEquals("CANCELLED", cancelledLeave.getStatus());

        // Verify Balance restored (Available = 10, Reserved = 0, Used = 0)
        LeaveBalance cancelledBal = leaveBalanceService.getEmployeeBalances(testEmp.getId(), year).get(0);
        assertEquals(10.0, cancelledBal.getAvailableBalance());
        assertEquals(0.0, cancelledBal.getPendingBalance());
        assertEquals(0.0, cancelledBal.getUsedBalance());

        // TEST 17 — Verify Schedule Exception in DB is status = CANCELLED (Historical preservation)
        Optional<ScheduleException> cancelledExcOpt = scheduleExceptionRepository.findByLeaveRequestIdAndExceptionType(leaveRequestId, "LEAVE");
        assertTrue(cancelledExcOpt.isPresent());
        assertEquals("CANCELLED", cancelledExcOpt.get().getStatus());

        // TEST 18 — Verify Schedule Availability After Cancellation
        EmployeeAvailabilityDto postCancelAvailability = scheduleManagementService.getEmployeeAvailability(
                currentUser, "EMP-1001", LocalDate.of(2026, 9, 2));
        assertTrue(postCancelAvailability.isAvailable(), "CANCELLED approved leave MUST restore schedule availability!");
        assertEquals("NONE", postCancelAvailability.getReason());

        // Creating shift on Sep 2 now SUCCEEDS!
        ScheduleDto createdSchedule = scheduleManagementService.createSchedule(currentUser, shiftReq);
        assertNotNull(createdSchedule);
        assertEquals("EMP-1001", createdSchedule.getEmployeeId());
        assertEquals("2026-09-02", createdSchedule.getDate());

        // TEST 19 — Rejection Flow (10-Sep -> 12-Sep)
        LeaveRequest rejReq = new LeaveRequest();
        rejReq.setLeaveTypeId(annualLeaveType.getId());
        rejReq.setStartDate(LocalDate.of(2026, 9, 10));
        rejReq.setEndDate(LocalDate.of(2026, 9, 12));
        rejReq.setReason("Doctor Appointment");

        Leave leaveToReject = leaveService.applyLeave(testEmp, rejReq);
        Long rejId = leaveToReject.getId();
        assertEquals("PENDING", leaveToReject.getStatus());

        // Reject leave
        Leave rejectedLeave = leaveService.rejectLeave(rejId, testEmp);
        assertEquals("REJECTED", rejectedLeave.getStatus());

        // Availability on Sep 11 = true, reason = NONE
        EmployeeAvailabilityDto rejAvailability = scheduleManagementService.getEmployeeAvailability(
                currentUser, "EMP-1001", LocalDate.of(2026, 9, 11));
        assertTrue(rejAvailability.isAvailable(), "REJECTED leave must keep employee available");
        assertEquals("NONE", rejAvailability.getReason());

        // Verify 0 ScheduleExceptions created for rejected leave
        Optional<ScheduleException> rejExcOpt = scheduleExceptionRepository.findByLeaveRequestIdAndExceptionType(rejId, "LEAVE");
        assertFalse(rejExcOpt.isPresent(), "REJECTED leave must NEVER produce a ScheduleException record");

        // Duplicate Event Protection (Idempotency)
        LeaveApprovedEvent dupEvent = new LeaveApprovedEvent(leaveRequestId, "EMP-1001", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5), "Annual Leave E2E");
        leaveScheduleEventListener.handleLeaveApproved(dupEvent);
        leaveScheduleEventListener.handleLeaveApproved(dupEvent);

        List<ScheduleException> exceptions = scheduleExceptionRepository.findAll();
        long excCountForLeave = exceptions.stream().filter(e -> leaveRequestId.equals(e.getLeaveRequestId())).count();
        assertEquals(1, excCountForLeave, "Idempotence check: Duplicate events must produce exactly 1 ScheduleException record");
    }
}
