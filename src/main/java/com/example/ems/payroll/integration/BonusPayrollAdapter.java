package com.example.ems.payroll.integration;

import com.example.ems.payroll.dto.BonusPeriodSummaryDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BonusPayrollAdapter {

    // Idempotency consumption registry: tracks consumed bonuses per (organizationId:employeeId:periodKey)
    private final Map<String, Boolean> consumedBonusRegistry = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> approvedBonusMockStore = new ConcurrentHashMap<>();

    /**
     * For testing/integration: registers an approved bonus for an employee and period.
     */
    public void registerApprovedBonus(Long organizationId, Long employeeId, LocalDate periodStart, BigDecimal amount) {
        String key = buildPeriodKey(organizationId, employeeId, periodStart);
        approvedBonusMockStore.put(key, amount);
        consumedBonusRegistry.remove(key); // Reset consumption on fresh registration
    }

    public BonusPeriodSummaryDto getBonusSummary(Long employeeId, Long organizationId,
                                                  LocalDate periodStart, LocalDate periodEnd) {
        String key = buildPeriodKey(organizationId, employeeId, periodStart);

        // Check if already consumed by a prior completed payroll run
        if (Boolean.TRUE.equals(consumedBonusRegistry.get(key))) {
            return new BonusPeriodSummaryDto(BigDecimal.ZERO, new ArrayList<>(), "Already consumed in prior payroll run");
        }

        BigDecimal approvedAmount = approvedBonusMockStore.getOrDefault(key, BigDecimal.ZERO);
        if (approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return new BonusPeriodSummaryDto(BigDecimal.ZERO, new ArrayList<>(), null);
        }

        List<Long> ids = List.of(Math.abs((long) key.hashCode()));
        return new BonusPeriodSummaryDto(approvedAmount, ids, "Approved appraisal/annual performance bonus");
    }

    public void markBonusesProcessed(Long employeeId, Long organizationId, LocalDate periodStart, Long payrollRunId) {
        String key = buildPeriodKey(organizationId, employeeId, periodStart);
        consumedBonusRegistry.put(key, true);
    }

    private String buildPeriodKey(Long organizationId, Long employeeId, LocalDate periodStart) {
        return organizationId + ":" + employeeId + ":" + periodStart.getYear() + "-" + String.format("%02d", periodStart.getMonthValue());
    }
}
