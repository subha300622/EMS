package com.example.ems.incentive.service;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.employee.entity.Employee;
import com.example.ems.incentive.dto.IncentiveRecordResponse;
import com.example.ems.incentive.entity.*;
import com.example.ems.incentive.listener.IncentiveApprovalEventListener;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import com.example.ems.organization.entity.Organization;
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
public class IncentiveWorkflowServiceTest {

    @Mock
    private IncentiveRecordRepository recordRepository;

    @Mock
    private ApprovalFacade approvalFacade;

    @InjectMocks
    private IncentiveWorkflowService workflowService;

    private IncentiveApprovalEventListener eventListener;

    private IncentiveRecord record;
    private IncentivePolicy policy;
    private Employee employee;
    private final Long orgId = 1L;
    private final Long recordId = 100L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
        eventListener = new IncentiveApprovalEventListener(recordRepository);

        Organization org = new Organization();
        org.setId(orgId);

        employee = new Employee();
        employee.setId(10L);
        employee.setFullName("Jane Smith");

        policy = new IncentivePolicy();
        policy.setId(1L);
        policy.setApprovalRequired(true);

        record = new IncentiveRecord();
        record.setId(recordId);
        record.setOrganization(org);
        record.setEmployee(employee);
        record.setPolicy(policy);
        record.setIncentiveType(IncentiveType.PERFORMANCE);
        record.setCalculatedAmount(BigDecimal.valueOf(6000.00));
        record.setPeriodStart(LocalDate.of(2026, 9, 1));
        record.setPeriodEnd(LocalDate.of(2026, 9, 30));
        record.setStatus(IncentiveStatus.CALCULATED);
        record.setPayrollStatus(IncentivePayrollStatus.PENDING);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testSubmitWithApprovalRequiredTransitionsToPendingApproval() {
        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(IncentiveRecord.class))).thenAnswer(i -> i.getArgument(0));

        IncentiveRecordResponse resp = workflowService.submitIncentive(recordId);

        assertNotNull(resp);
        assertEquals(IncentiveStatus.PENDING_APPROVAL, resp.getStatus());
        assertNotNull(resp.getWorkflowInstanceId());
    }

    @Test
    void testSubmitWithAutoApprovalDirectlyApproves() {
        policy.setApprovalRequired(false);

        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(IncentiveRecord.class))).thenAnswer(i -> i.getArgument(0));

        IncentiveRecordResponse resp = workflowService.submitIncentive(recordId);

        assertNotNull(resp);
        assertEquals(IncentiveStatus.APPROVED, resp.getStatus());
        assertEquals(BigDecimal.valueOf(6000.00), resp.getApprovedAmount());
        assertEquals(IncentivePayrollStatus.PENDING, resp.getPayrollStatus());
    }

    @Test
    void testApprovalEventListenerHandlesApproval() {
        record.setStatus(IncentiveStatus.PENDING_APPROVAL);
        record.setAdjustedAmount(BigDecimal.valueOf(5000.00));

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(IncentiveRecord.class))).thenAnswer(i -> i.getArgument(0));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this, "WFI-123", WorkflowType.INCENTIVE_REQUEST, "INCENTIVE_RECORD",
                String.valueOf(recordId), orgId, ApprovalStatus.APPROVED);

        eventListener.onApprovalWorkflowCompleted(event);

        assertEquals(IncentiveStatus.APPROVED, record.getStatus());
        assertEquals(BigDecimal.valueOf(5000.00), record.getApprovedAmount());
        assertEquals(IncentivePayrollStatus.PENDING, record.getPayrollStatus());
    }

    @Test
    void testApprovalEventListenerHandlesRejection() {
        record.setStatus(IncentiveStatus.PENDING_APPROVAL);

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(IncentiveRecord.class))).thenAnswer(i -> i.getArgument(0));

        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this, "WFI-123", WorkflowType.INCENTIVE_REQUEST, "INCENTIVE_RECORD",
                String.valueOf(recordId), orgId, ApprovalStatus.REJECTED);

        eventListener.onApprovalWorkflowCompleted(event);

        assertEquals(IncentiveStatus.REJECTED, record.getStatus());
    }
}
