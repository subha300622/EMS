package com.example.ems.attendance.event;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceRegularization;
import com.example.ems.attendance.entity.AttendanceRegularizationStatus;
import com.example.ems.attendance.repository.AttendanceRegularizationRepository;
import com.example.ems.attendance.service.AttendanceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceRegularizationApprovalEventListenerTest {

    @Mock
    private AttendanceRegularizationRepository regularizationRepository;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceRegularizationApprovalEventListener listener;

    @Test
    @DisplayName("EventListener: Final APPROVED event updates regularization and applies Attendance correction")
    void testHandleWorkflowCompleted_Approved() {
        Attendance attendance = new Attendance();
        attendance.setId(101L);

        AttendanceRegularization reg = new AttendanceRegularization();
        reg.setId(501L);
        reg.setAttendance(attendance);
        reg.setStatus(AttendanceRegularizationStatus.PENDING);
        reg.setRequestedCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        reg.setRequestedCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));

        when(regularizationRepository.findById(501L)).thenReturn(Optional.of(reg));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "wf-100",
                WorkflowType.ATTENDANCE_REGULARIZATION,
                "ATTENDANCE_REGULARIZATION",
                "501",
                100L,
                ApprovalStatus.APPROVED
        );

        listener.handleWorkflowCompleted(event);

        assertEquals(AttendanceRegularizationStatus.APPROVED, reg.getStatus());
        verify(regularizationRepository).save(reg);
        verify(attendanceService).applyRegularizationCorrection(101L, reg.getRequestedCheckInTime(), reg.getRequestedCheckOutTime());
    }

    @Test
    @DisplayName("EventListener: Final REJECTED event marks regularization REJECTED and leaves Attendance untouched")
    void testHandleWorkflowCompleted_Rejected() {
        Attendance attendance = new Attendance();
        attendance.setId(101L);

        AttendanceRegularization reg = new AttendanceRegularization();
        reg.setId(501L);
        reg.setAttendance(attendance);
        reg.setStatus(AttendanceRegularizationStatus.PENDING);

        when(regularizationRepository.findById(501L)).thenReturn(Optional.of(reg));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "wf-100",
                WorkflowType.ATTENDANCE_REGULARIZATION,
                "ATTENDANCE_REGULARIZATION",
                "501",
                100L,
                ApprovalStatus.REJECTED
        );

        listener.handleWorkflowCompleted(event);

        assertEquals(AttendanceRegularizationStatus.REJECTED, reg.getStatus());
        verify(regularizationRepository).save(reg);
        verify(attendanceService, never()).applyRegularizationCorrection(any(), any(), any());
    }

    @Test
    @DisplayName("EventListener: Ignores unrelated workflow events")
    void testHandleWorkflowCompleted_UnrelatedType() {
        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "wf-200",
                WorkflowType.LEAVE_APPROVAL,
                "LEAVE",
                "1001",
                100L,
                ApprovalStatus.APPROVED
        );

        listener.handleWorkflowCompleted(event);

        verify(regularizationRepository, never()).findById(any());
        verify(attendanceService, never()).applyRegularizationCorrection(any(), any(), any());
    }
}
