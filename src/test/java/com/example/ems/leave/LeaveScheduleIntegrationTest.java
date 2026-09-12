package com.example.ems.leave;

import com.example.ems.leave.event.LeaveApprovedEvent;
import com.example.ems.leave.event.LeaveCancelledEvent;
import com.example.ems.schedule.entity.ScheduleException;
import com.example.ems.schedule.listener.LeaveScheduleEventListener;
import com.example.ems.schedule.repository.ScheduleExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
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
public class LeaveScheduleIntegrationTest {

    @Autowired
    private LeaveScheduleEventListener listener;

    @Autowired
    private ScheduleExceptionRepository scheduleExceptionRepository;

    @BeforeEach
    public void setUp() {
        scheduleExceptionRepository.deleteAll();
    }

    @Test
    public void testLeaveApprovedEventCreatesActiveScheduleException() {
        Long leaveRequestId = 10025L;
        String employeeId = "EMP-1001";
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 9, 3);

        LeaveApprovedEvent event = new LeaveApprovedEvent(leaveRequestId, employeeId, startDate, endDate, "ANNUAL");
        listener.handleLeaveApproved(event);

        Optional<ScheduleException> opt = scheduleExceptionRepository.findByLeaveRequestIdAndExceptionType(leaveRequestId, "LEAVE");
        assertTrue(opt.isPresent(), "ScheduleException should be created for approved leave");

        ScheduleException exception = opt.get();
        assertEquals("EMP-1001", exception.getEmployeeId());
        assertEquals("LEAVE", exception.getExceptionType());
        assertEquals(startDate, exception.getStartDate());
        assertEquals(endDate, exception.getEndDate());
        assertEquals("ACTIVE", exception.getStatus());
    }

    @Test
    public void testIdempotentDuplicateLeaveApprovedEvent() {
        Long leaveRequestId = 10026L;
        String employeeId = "EMP-1002";
        LocalDate startDate = LocalDate.of(2026, 9, 10);
        LocalDate endDate = LocalDate.of(2026, 9, 12);

        LeaveApprovedEvent event1 = new LeaveApprovedEvent(leaveRequestId, employeeId, startDate, endDate, "SICK");
        listener.handleLeaveApproved(event1);

        // Duplicate event handling
        LeaveApprovedEvent event2 = new LeaveApprovedEvent(leaveRequestId, employeeId, startDate, endDate, "SICK");
        listener.handleLeaveApproved(event2);

        List<ScheduleException> all = scheduleExceptionRepository.findAll();
        assertEquals(1, all.size(), "Idempotent event processing must not create duplicate exceptions");
        assertEquals("ACTIVE", all.get(0).getStatus());
    }

    @Test
    public void testApprovedLeaveCancelledDeactivatesScheduleException() {
        Long leaveRequestId = 10027L;
        String employeeId = "EMP-1003";
        LocalDate startDate = LocalDate.of(2026, 9, 15);
        LocalDate endDate = LocalDate.of(2026, 9, 17);

        // 1. Approve
        LeaveApprovedEvent approveEvent = new LeaveApprovedEvent(leaveRequestId, employeeId, startDate, endDate, "CASUAL");
        listener.handleLeaveApproved(approveEvent);

        Optional<ScheduleException> activeOpt = scheduleExceptionRepository.findByLeaveRequestIdAndExceptionType(leaveRequestId, "LEAVE");
        assertTrue(activeOpt.isPresent());
        assertEquals("ACTIVE", activeOpt.get().getStatus());

        // 2. Cancel
        LeaveCancelledEvent cancelEvent = new LeaveCancelledEvent(leaveRequestId, employeeId, startDate, endDate);
        listener.handleLeaveCancelled(cancelEvent);

        Optional<ScheduleException> cancelledOpt = scheduleExceptionRepository.findByLeaveRequestIdAndExceptionType(leaveRequestId, "LEAVE");
        assertTrue(cancelledOpt.isPresent());
        assertEquals("CANCELLED", cancelledOpt.get().getStatus());

        // 3. Verify no active exception for target date
        List<ScheduleException> activeOnDate = scheduleExceptionRepository.findActiveExceptionsOnDate(employeeId, LocalDate.of(2026, 9, 16));
        assertTrue(activeOnDate.isEmpty(), "Cancelled leave must leave employee available on schedule");
    }

    @Test
    public void testDateBoundaryAvailability() {
        Long leaveRequestId = 10028L;
        String employeeId = "EMP-1004";
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 9, 3);

        LeaveApprovedEvent event = new LeaveApprovedEvent(leaveRequestId, employeeId, startDate, endDate, "ANNUAL");
        listener.handleLeaveApproved(event);

        // Sep 1, 2, 3 should be unavailable
        assertFalse(scheduleExceptionRepository.findActiveExceptionsOnDate(employeeId, LocalDate.of(2026, 9, 1)).isEmpty());
        assertFalse(scheduleExceptionRepository.findActiveExceptionsOnDate(employeeId, LocalDate.of(2026, 9, 2)).isEmpty());
        assertFalse(scheduleExceptionRepository.findActiveExceptionsOnDate(employeeId, LocalDate.of(2026, 9, 3)).isEmpty());

        // Sep 4 should be available
        assertTrue(scheduleExceptionRepository.findActiveExceptionsOnDate(employeeId, LocalDate.of(2026, 9, 4)).isEmpty());
    }

    @Test
    public void testPendingLeaveCancellationDoesNotCreateScheduleException() {
        Long leaveRequestId = 10029L;
        String employeeId = "EMP-1005";
        LocalDate startDate = LocalDate.of(2026, 9, 5);
        LocalDate endDate = LocalDate.of(2026, 9, 7);

        // A pending leave cancelled without prior approval receives no event or finds no exception to alter
        LeaveCancelledEvent cancelEvent = new LeaveCancelledEvent(leaveRequestId, employeeId, startDate, endDate);
        listener.handleLeaveCancelled(cancelEvent);

        Optional<ScheduleException> opt = scheduleExceptionRepository.findByLeaveRequestIdAndExceptionType(leaveRequestId, "LEAVE");
        assertFalse(opt.isPresent(), "Pending leave cancellation should not leave any ScheduleException in the database");
    }

    @Test
    public void testDateRangeOverlapQueryForMultiDaySchedule() {
        Long leaveRequestId = 10030L;
        String employeeId = "EMP-1006";
        LocalDate leaveStart = LocalDate.of(2026, 9, 3);
        LocalDate leaveEnd = LocalDate.of(2026, 9, 4);

        LeaveApprovedEvent event = new LeaveApprovedEvent(leaveRequestId, employeeId, leaveStart, leaveEnd, "ANNUAL");
        listener.handleLeaveApproved(event);

        // Schedule range Sep 2 -> Sep 5 overlaps with Leave Sep 3 -> Sep 4
        List<ScheduleException> overlaps = scheduleExceptionRepository.findActiveExceptionsInDateRange(
                employeeId, LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 5));
        assertFalse(overlaps.isEmpty(), "Schedule range Sep 2-5 must detect overlap with leave Sep 3-4");

        // Schedule range Sep 5 -> Sep 7 does not overlap with Leave Sep 3 -> Sep 4
        List<ScheduleException> noOverlaps = scheduleExceptionRepository.findActiveExceptionsInDateRange(
                employeeId, LocalDate.of(2026, 9, 5), LocalDate.of(2026, 9, 7));
        assertTrue(noOverlaps.isEmpty(), "Schedule range Sep 5-7 must not overlap with leave Sep 3-4");
    }
}
