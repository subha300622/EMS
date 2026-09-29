package com.example.ems.incentive.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.incentive.dto.IncentiveRecordResponse;
import com.example.ems.incentive.entity.*;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncentivePayrollIntegrationServiceTest {

    @Mock
    private IncentiveRecordRepository recordRepository;

    @InjectMocks
    private IncentivePayrollIntegrationService payrollIntegrationService;

    private final Long orgId = 1L;
    private final Long employeeId = 100L;
    private final LocalDate periodStart = LocalDate.of(2026, 3, 1);
    private final LocalDate periodEnd = LocalDate.of(2026, 3, 31);

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getEligibleIncentiveRecords_returnsMappedResponses() {
        IncentivePolicy policy = IncentivePolicy.builder()
                .id(10L)
                .policyCode("INC-TEST")
                .policyName("Test Policy")
                .incentiveType(IncentiveType.PERFORMANCE)
                .calculationMethod(IncentiveCalculationMethod.FIXED_AMOUNT)
                .build();

        IncentiveRecord record = IncentiveRecord.builder()
                .id(50L)
                .organizationId(orgId)
                .employeeId(employeeId)
                .policy(policy)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .calculatedAmount(new BigDecimal("5000.00"))
                .status(IncentiveStatus.APPROVED)
                .payrollStatus(IncentivePayrollStatus.PENDING)
                .build();

        when(recordRepository.findEligibleForPayroll(orgId, employeeId, periodStart, periodEnd))
                .thenReturn(List.of(record));

        List<IncentiveRecordResponse> responses = payrollIntegrationService.getEligibleIncentiveRecords(employeeId, periodStart, periodEnd);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(50L);
        assertThat(responses.get(0).getCalculatedAmount()).isEqualByComparingTo("5000.00");
    }

    @Test
    void postIncentivesToPayrollRun_nullPayrollRunId_throwsBadRequest() {
        assertThatThrownBy(() -> payrollIntegrationService.postIncentivesToPayrollRun(
                orgId, employeeId, periodStart, periodEnd, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("payrollRunId is mandatory");
    }

    @Test
    void postIncentivesToPayrollRun_updatesStatusAndReturnsTotalAmount() {
        IncentivePolicy policy = IncentivePolicy.builder()
                .id(10L)
                .policyCode("INC-TEST")
                .build();

        IncentiveRecord record1 = IncentiveRecord.builder()
                .id(51L)
                .organizationId(orgId)
                .employeeId(employeeId)
                .policy(policy)
                .calculatedAmount(new BigDecimal("3000.00"))
                .status(IncentiveStatus.APPROVED)
                .payrollStatus(IncentivePayrollStatus.PENDING)
                .build();

        IncentiveRecord record2 = IncentiveRecord.builder()
                .id(52L)
                .organizationId(orgId)
                .employeeId(employeeId)
                .policy(policy)
                .calculatedAmount(new BigDecimal("4000.00"))
                .adjustedAmount(new BigDecimal("3500.00"))
                .status(IncentiveStatus.APPROVED)
                .payrollStatus(IncentivePayrollStatus.PENDING)
                .build();

        when(recordRepository.findEligibleForPayroll(orgId, employeeId, periodStart, periodEnd))
                .thenReturn(List.of(record1, record2));

        BigDecimal total = payrollIntegrationService.postIncentivesToPayrollRun(
                orgId, employeeId, periodStart, periodEnd, 999L
        );

        assertThat(total).isEqualByComparingTo("6500.00"); // 3000 + 3500

        assertThat(record1.getStatus()).isEqualTo(IncentiveStatus.POSTED_TO_PAYROLL);
        assertThat(record1.getPayrollStatus()).isEqualTo(IncentivePayrollStatus.POSTED);
        assertThat(record1.getPayrollRunId()).isEqualTo(999L);

        assertThat(record2.getStatus()).isEqualTo(IncentiveStatus.POSTED_TO_PAYROLL);
        assertThat(record2.getPayrollStatus()).isEqualTo(IncentivePayrollStatus.POSTED);
        assertThat(record2.getPayrollRunId()).isEqualTo(999L);

        verify(recordRepository, times(2)).save(any(IncentiveRecord.class));
    }
}
