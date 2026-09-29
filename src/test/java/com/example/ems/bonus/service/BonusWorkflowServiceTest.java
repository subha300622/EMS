package com.example.ems.bonus.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.bonus.dto.BonusRecordResponse;
import com.example.ems.bonus.entity.BonusPayrollStatus;
import com.example.ems.bonus.entity.BonusPolicy;
import com.example.ems.bonus.entity.BonusRecord;
import com.example.ems.bonus.entity.BonusStatus;
import com.example.ems.bonus.repository.BonusRecordRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BonusWorkflowServiceTest {

    @Mock
    private BonusRecordRepository recordRepository;

    @Mock
    private ApprovalFacade approvalFacade;

    @InjectMocks
    private BonusWorkflowService workflowService;

    private final Long orgId = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testSubmitBonus_AutoApprovalWhenApprovalNotRequired() {
        BonusPolicy policy = new BonusPolicy();
        policy.setApprovalRequired(false);

        BonusRecord record = new BonusRecord();
        record.setId(501L);
        record.setPolicy(policy);
        record.setCalculatedAmount(BigDecimal.valueOf(15000.00));
        record.setStatus(BonusStatus.CALCULATED);

        when(recordRepository.findByIdAndOrganizationId(501L, orgId)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(BonusRecord.class))).thenAnswer(i -> i.getArgument(0));

        BonusRecordResponse resp = workflowService.submitBonus(501L);

        assertNotNull(resp);
        assertEquals(BonusStatus.APPROVED, resp.getStatus());
        assertEquals(BonusPayrollStatus.PENDING, resp.getPayrollStatus());
        assertEquals(BigDecimal.valueOf(15000.00), resp.getApprovedAmount());
        assertEquals("SYSTEM_AUTO_APPROVAL", resp.getApprovedBy());
    }

    @Test
    void testSubmitBonus_CentralApprovalPlatform() {
        BonusPolicy policy = new BonusPolicy();
        policy.setApprovalRequired(true);

        Employee employee = new Employee();
        employee.setId(101L);

        BonusRecord record = new BonusRecord();
        record.setId(501L);
        record.setEmployee(employee);
        record.setPolicy(policy);
        record.setCalculatedAmount(BigDecimal.valueOf(15000.00));
        record.setStatus(BonusStatus.CALCULATED);

        ApprovalWorkflowInstance instance = new ApprovalWorkflowInstance();
        instance.setWorkflowInstanceId("WFI-BONUS-999");

        when(recordRepository.findByIdAndOrganizationId(501L, orgId)).thenReturn(Optional.of(record));
        when(approvalFacade.startApproval(any(ApprovalContext.class))).thenReturn(instance);
        when(recordRepository.save(any(BonusRecord.class))).thenAnswer(i -> i.getArgument(0));

        BonusRecordResponse resp = workflowService.submitBonus(501L);

        assertNotNull(resp);
        assertEquals(BonusStatus.PENDING_APPROVAL, resp.getStatus());
        assertEquals("WFI-BONUS-999", resp.getWorkflowInstanceId());
    }
}
