package com.example.ems.payroll.integration;

import com.example.ems.incentive.entity.IncentivePayrollStatus;
import com.example.ems.incentive.entity.IncentiveRecord;
import com.example.ems.incentive.entity.IncentiveStatus;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import com.example.ems.payroll.dto.IncentivePeriodSummaryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IncentivePayrollAdapter {

    @Autowired(required = false)
    private IncentiveRecordRepository incentiveRecordRepository;

    @Autowired(required = false)
    private com.example.ems.organization.service.OrganizationCompensationConfigService compensationConfigService;

    // Idempotency consumption registry: tracks consumed incentives per (organizationId:employeeId:periodKey)
    private final Map<String, Boolean> consumedIncentiveRegistry = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> approvedIncentiveMockStore = new ConcurrentHashMap<>();

    /**
     * For testing/integration: registers an approved incentive for an employee and period.
     */
    public void registerApprovedIncentive(Long organizationId, Long employeeId, LocalDate periodStart, BigDecimal amount) {
        String key = buildPeriodKey(organizationId, employeeId, periodStart);
        approvedIncentiveMockStore.put(key, amount);
        consumedIncentiveRegistry.remove(key); // Reset consumption on fresh registration
    }

    public IncentivePeriodSummaryDto getIncentiveSummary(Long employeeId, Long organizationId,
                                                          LocalDate periodStart, LocalDate periodEnd) {
        // If incentive feature is disabled for the organization, return zero summary
        if (compensationConfigService != null && !compensationConfigService.isIncentiveEnabled(organizationId)) {
            return new IncentivePeriodSummaryDto(BigDecimal.ZERO, new ArrayList<>(), "Incentive module disabled for organization");
        }

        // 1. Check for modern approved IncentiveRecords first
        if (incentiveRecordRepository != null) {
            List<IncentiveRecord> approvedRecords = incentiveRecordRepository.findEligibleForPayroll(
                    organizationId, employeeId, periodStart, periodEnd
            );
            if (!approvedRecords.isEmpty()) {
                BigDecimal totalAmount = BigDecimal.ZERO;
                List<Long> recordIds = new ArrayList<>();

                for (IncentiveRecord rec : approvedRecords) {
                    BigDecimal amt = rec.getEffectiveAmount();
                    totalAmount = totalAmount.add(amt != null ? amt : BigDecimal.ZERO);
                    recordIds.add(rec.getId());
                }

                return new IncentivePeriodSummaryDto(totalAmount, recordIds, "Approved incentive records for period");
            }
        }

        // 2. Legacy fallback
        String key = buildPeriodKey(organizationId, employeeId, periodStart);

        // Check if already consumed by a prior completed payroll run
        if (Boolean.TRUE.equals(consumedIncentiveRegistry.get(key))) {
            return new IncentivePeriodSummaryDto(BigDecimal.ZERO, new ArrayList<>(), "Already consumed in prior payroll run");
        }

        BigDecimal approvedAmount = approvedIncentiveMockStore.getOrDefault(key, BigDecimal.ZERO);
        if (approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return new IncentivePeriodSummaryDto(BigDecimal.ZERO, new ArrayList<>(), null);
        }

        List<Long> ids = List.of(Math.abs((long) key.hashCode()));
        return new IncentivePeriodSummaryDto(approvedAmount, ids, "Approved performance incentive for period");
    }

    public void markIncentivesProcessed(Long employeeId, Long organizationId, LocalDate periodStart, Long payrollRunId) {
        if (incentiveRecordRepository != null) {
            LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);
            List<IncentiveRecord> approvedRecords = incentiveRecordRepository.findEligibleForPayroll(
                    organizationId, employeeId, periodStart, periodEnd
            );
            for (IncentiveRecord rec : approvedRecords) {
                rec.setPayrollStatus(IncentivePayrollStatus.POSTED);
                rec.setStatus(IncentiveStatus.POSTED_TO_PAYROLL);
                rec.setPayrollRunId(payrollRunId);
                incentiveRecordRepository.save(rec);
            }
        }
        String key = buildPeriodKey(organizationId, employeeId, periodStart);
        consumedIncentiveRegistry.put(key, true);
    }

    private String buildPeriodKey(Long organizationId, Long employeeId, LocalDate periodStart) {
        return organizationId + ":" + employeeId + ":" + periodStart.getYear() + "-" + String.format("%02d", periodStart.getMonthValue());
    }
}
