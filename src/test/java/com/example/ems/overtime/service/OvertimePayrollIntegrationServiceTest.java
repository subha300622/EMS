package com.example.ems.overtime.service;

import com.example.ems.overtime.entity.*;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OvertimePayrollIntegrationServiceTest {

    @Mock
    private OvertimeRecordRepository recordRepository;

    @InjectMocks
    private OvertimePayrollIntegrationService payrollIntegrationService;

    private final Long orgId = 1L;
    private final Long empId = 10L;
    private final Long payrollRunId = 500L;
    private final LocalDate periodStart = LocalDate.of(2026, 9, 1);
    private final LocalDate periodEnd = LocalDate.of(2026, 9, 30);

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testPostOvertimeToPayrollRun_PostsApprovedPendingRecords() {
        OvertimeRecord rec1 = new OvertimeRecord();
        rec1.setId(101L);
        rec1.setStatus(OvertimeStatus.APPROVED);
        rec1.setPayrollStatus(OvertimePayrollStatus.PENDING);
        rec1.setApprovedAmount(new BigDecimal("375.00"));
        rec1.setWorkDate(LocalDate.of(2026, 9, 10));

        OvertimeRecord rec2 = new OvertimeRecord();
        rec2.setId(102L);
        rec2.setStatus(OvertimeStatus.APPROVED);
        rec2.setPayrollStatus(OvertimePayrollStatus.PENDING);
        rec2.setApprovedAmount(new BigDecimal("432.70"));
        rec2.setWorkDate(LocalDate.of(2026, 9, 15));

        when(recordRepository.findEligibleForPayroll(orgId, empId, periodStart, periodEnd))
                .thenReturn(List.of(rec1, rec2));
        when(recordRepository.save(any(OvertimeRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        BigDecimal totalPosted = payrollIntegrationService.postOvertimeToPayrollRun(
                orgId, empId, periodStart, periodEnd, payrollRunId
        );

        // Total = 375.00 + 432.70 = 807.70
        assertEquals(new BigDecimal("807.70"), totalPosted);

        assertEquals(OvertimeStatus.POSTED_TO_PAYROLL, rec1.getStatus());
        assertEquals(OvertimePayrollStatus.POSTED, rec1.getPayrollStatus());
        assertEquals(payrollRunId, rec1.getPayrollRunId());

        assertEquals(OvertimeStatus.POSTED_TO_PAYROLL, rec2.getStatus());
        assertEquals(OvertimePayrollStatus.POSTED, rec2.getPayrollStatus());
        assertEquals(payrollRunId, rec2.getPayrollRunId());

        verify(recordRepository, times(2)).save(any(OvertimeRecord.class));
    }

    @Test
    void testPostOvertimeToPayrollRun_Idempotency_SecondRunPostsZero() {
        // First run posts records
        OvertimeRecord rec = new OvertimeRecord();
        rec.setId(101L);
        rec.setStatus(OvertimeStatus.POSTED_TO_PAYROLL);
        rec.setPayrollStatus(OvertimePayrollStatus.POSTED);
        rec.setPayrollRunId(payrollRunId);

        // Since it's already POSTED, findEligibleForPayroll returns empty list
        when(recordRepository.findEligibleForPayroll(orgId, empId, periodStart, periodEnd))
                .thenReturn(Collections.emptyList());

        BigDecimal totalPosted = payrollIntegrationService.postOvertimeToPayrollRun(
                orgId, empId, periodStart, periodEnd, payrollRunId
        );

        assertEquals(BigDecimal.ZERO, totalPosted);
        verify(recordRepository, never()).save(any());
    }
}
