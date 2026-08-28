package com.example.ems.schedule.listener;

import com.example.ems.leave.event.LeaveApprovedEvent;
import com.example.ems.leave.event.LeaveCancelledEvent;
import com.example.ems.schedule.entity.ScheduleException;
import com.example.ems.schedule.repository.ScheduleExceptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class LeaveScheduleEventListener {

    private static final Logger log = LoggerFactory.getLogger(LeaveScheduleEventListener.class);

    @Autowired
    private ScheduleExceptionRepository scheduleExceptionRepository;

    @EventListener
    @Transactional
    public void handleLeaveApproved(LeaveApprovedEvent event) {
        log.info("Processing LeaveApprovedEvent: leaveRequestId={}, employeeId={}", event.getLeaveRequestId(), event.getEmployeeId());

        Optional<ScheduleException> optException = scheduleExceptionRepository.findByLeaveRequestIdAndExceptionType(
                event.getLeaveRequestId(), "LEAVE");

        if (optException.isPresent()) {
            ScheduleException existing = optException.get();
            existing.setStatus("ACTIVE");
            existing.setStartDate(event.getStartDate());
            existing.setEndDate(event.getEndDate());
            scheduleExceptionRepository.save(existing);
            log.info("Updated existing ScheduleException to ACTIVE for leaveRequestId={}", event.getLeaveRequestId());
        } else {
            ScheduleException exception = new ScheduleException(
                    event.getEmployeeId(),
                    "LEAVE",
                    event.getStartDate(),
                    event.getEndDate(),
                    event.getLeaveRequestId(),
                    "ACTIVE"
            );
            scheduleExceptionRepository.save(exception);
            log.info("Created new ScheduleException ACTIVE for leaveRequestId={}", event.getLeaveRequestId());
        }
    }

    @EventListener
    @Transactional
    public void handleLeaveCancelled(LeaveCancelledEvent event) {
        log.info("Processing LeaveCancelledEvent: leaveRequestId={}, employeeId={}", event.getLeaveRequestId(), event.getEmployeeId());

        Optional<ScheduleException> optException = scheduleExceptionRepository.findByLeaveRequestIdAndExceptionType(
                event.getLeaveRequestId(), "LEAVE");

        if (optException.isPresent()) {
            ScheduleException existing = optException.get();
            existing.setStatus("CANCELLED");
            scheduleExceptionRepository.save(existing);
            log.info("Deactivated ScheduleException (status=CANCELLED) for leaveRequestId={}", event.getLeaveRequestId());
        } else {
            log.warn("No active ScheduleException found to cancel for leaveRequestId={}", event.getLeaveRequestId());
        }
    }
}
