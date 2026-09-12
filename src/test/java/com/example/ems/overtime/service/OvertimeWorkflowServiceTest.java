package com.example.ems.overtime.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.overtime.dto.OvertimeRecordResponse;
import com.example.ems.overtime.entity.*;
import com.example.ems.overtime.listener.OvertimeApprovalEventListener;
import com.example.ems.overtime.repository.OvertimeRecordRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OvertimeWorkflowServiceTest {

    @Mock
    private OvertimeRecordRepository recordRepository;

    @Mock
    private ApprovalFacade approvalFacade;

    @InjectMocks
    private OvertimeWorkflowService workflowService;

    private OvertimeApprovalEventListener approvalEventListener;

    private OvertimeRecord record;
    private OvertimePolicy policy;
    private final Long orgId = 1L;
    private final Long recordId = 100L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);

        approvalEventListener = new OvertimeApprovalEventListener(recordRepository);

        Employee employee = new Employee();
        employee.setId(10L);
        employee.setFullName("Alice Walker");

        policy = new OvertimePolicy();
        policy.setId(5L);
        policy.setApprovalRequired(true);

        record = new OvertimeRecord();
        record.setId(recordId);
        record.setEmployee(employee);
        record.setPolicy(policy);
        record.setWorkDate(LocalDate.of(2026, 9, 9));
        record.setCalculatedOtMinutes(120);
        record.setCalculatedAmount(new BigDecimal("300.00"));
        record.setStatus(OvertimeStatus.CALCULATED);
        record.setPayrollStatus(OvertimePayrollStatus.PENDING);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testSubmit_WhenApprovalNotRequired_AutoApproves() {
        policy.setApprovalRequired(false);

        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(OvertimeRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        OvertimeRecordResponse resp = workflowService.submitOvertime(recordId);

        assertNotNull(resp);
        assertEquals(OvertimeStatus.APPROVED, resp.getStatus());
        assertEquals(120, resp.getApprovedOtMinutes());
        assertEquals(new BigDecimal("300.00"), resp.getApprovedAmount());
        assertEquals(OvertimePayrollStatus.PENDING, resp.getPayrollStatus());
        assertEquals("AUTO_POLICY", resp.getApprovedBy());

        verify(approvalFacade, never()).startApproval(any());
        verify(recordRepository, times(1)).save(record);
    }

    @Test
    void testSubmit_WhenApprovalRequired_InitiatesCentralApprovalWorkflow() {
        policy.setApprovalRequired(true);

        ApprovalWorkflowInstance mockInstance = new ApprovalWorkflowInstance();
        mockInstance.setWorkflowInstanceId("WF-OT-9988");

        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));
        when(approvalFacade.startApproval(any(ApprovalContext.class))).thenReturn(mockInstance);
        when(recordRepository.save(any(OvertimeRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        OvertimeRecordResponse resp = workflowService.submitOvertime(recordId);

        assertNotNull(resp);
        assertEquals(OvertimeStatus.PENDING_APPROVAL, resp.getStatus());
        assertEquals("WF-OT-9988", resp.getWorkflowInstanceId());

        verify(approvalFacade, times(1)).startApproval(any(ApprovalContext.class));
        verify(recordRepository, times(1)).save(record);
    }

    @Test
    void testSubmit_InvalidStatus_ThrowsBadRequest() {
        record.setStatus(OvertimeStatus.APPROVED);
        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));

        assertThrows(BadRequestException.class, () -> workflowService.submitOvertime(recordId));
    }

    @Test
    void testApprovalEventListener_OnApprovedEvent() {
        record.setStatus(OvertimeStatus.PENDING_APPROVAL);
        record.setAdjustedOtMinutes(90);
        record.setAdjustedAmount(new BigDecimal("225.00"));

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(OvertimeRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this, "WF-OT-9988", WorkflowType.OVERTIME_REQUEST, "OVERTIME_RECORD",
                String.valueOf(recordId), orgId, ApprovalStatus.APPROVED
        );

        approvalEventListener.onApprovalWorkflowCompleted(event);

        assertEquals(OvertimeStatus.APPROVED, record.getStatus());
        // Uses adjusted values because adjustment exists
        assertEquals(90, record.getApprovedOtMinutes());
        assertEquals(new BigDecimal("225.00"), record.getApprovedAmount());
        assertEquals(OvertimePayrollStatus.PENDING, record.getPayrollStatus());
        assertNotNull(record.getApprovedAt());
        verify(recordRepository, times(1)).save(record);
    }

    @Test
    void testApprovalEventListener_OnRejectedEvent() {
        record.setStatus(OvertimeStatus.PENDING_APPROVAL);

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(OvertimeRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this, "WF-OT-9988", WorkflowType.OVERTIME_REQUEST, "OVERTIME_RECORD",
                String.valueOf(recordId), orgId, ApprovalStatus.REJECTED
        );

        approvalEventListener.onApprovalWorkflowCompleted(event);

        assertEquals(OvertimeStatus.REJECTED, record.getStatus());
        assertNotNull(record.getRejectionReason());
        assertNotNull(record.getRejectedAt());
        verify(recordRepository, times(1)).save(record);
    }
}
