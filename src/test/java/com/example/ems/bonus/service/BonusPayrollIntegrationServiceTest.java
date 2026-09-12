package com.example.ems.bonus.service;

import com.example.ems.bonus.dto.BonusRecordResponse;
import com.example.ems.bonus.entity.BonusPayrollStatus;
import com.example.ems.bonus.entity.BonusRecord;
import com.example.ems.bonus.entity.BonusStatus;
import com.example.ems.bonus.repository.BonusRecordRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BonusPayrollIntegrationServiceTest {

    @Mock
    private BonusRecordRepository recordRepository;

    @InjectMocks
    private BonusPayrollIntegrationService payrollIntegrationService;

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
    void testGetEligibleBonusRecords() {
        BonusRecord rec = new BonusRecord();
        rec.setId(501L);
        rec.setApprovedAmount(BigDecimal.valueOf(12000.00));
        rec.setStatus(BonusStatus.APPROVED);
        rec.setPayrollStatus(BonusPayrollStatus.PENDING);

        when(recordRepository.findEligibleForPayroll(eq(orgId), eq(101L), any(), any()))
                .thenReturn(List.of(rec));

        List<BonusRecordResponse> eligible = payrollIntegrationService.getEligibleBonusRecords(
                101L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));

        assertNotNull(eligible);
        assertEquals(1, eligible.size());
        assertEquals(BigDecimal.valueOf(12000.00), eligible.get(0).getApprovedAmount());
    }

    @Test
    void testMarkBonusesProcessed() {
        BonusRecord rec = new BonusRecord();
        rec.setId(501L);
        rec.setStatus(BonusStatus.APPROVED);
        rec.setPayrollStatus(BonusPayrollStatus.PENDING);

        when(recordRepository.findEligibleForPayroll(eq(orgId), eq(101L), any(), any()))
                .thenReturn(List.of(rec));

        payrollIntegrationService.markBonusesProcessed(101L, orgId, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31), 999L);

        assertEquals(BonusPayrollStatus.POSTED, rec.getPayrollStatus());
        assertEquals(BonusStatus.POSTED_TO_PAYROLL, rec.getStatus());
        assertEquals(999L, rec.getPayrollRunId());
        assertNotNull(rec.getPayrollPostedAt());
        verify(recordRepository, times(1)).save(rec);
    }
}
