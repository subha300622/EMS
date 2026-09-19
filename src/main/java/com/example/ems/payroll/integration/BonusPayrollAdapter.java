package com.example.ems.payroll.integration;

import com.example.ems.bonus.entity.BonusPayrollStatus;
import com.example.ems.bonus.entity.BonusRecord;
import com.example.ems.bonus.entity.BonusStatus;
import com.example.ems.bonus.repository.BonusRecordRepository;
import com.example.ems.payroll.dto.BonusPeriodSummaryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BonusPayrollAdapter {

    @Autowired(required = false)
    private BonusRecordRepository bonusRecordRepository;

    @Autowired(required = false)
    private com.example.ems.organization.service.OrganizationCompensationConfigService compensationConfigService;

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
        // If bonus feature is disabled for the organization, return zero summary
        if (compensationConfigService != null && !compensationConfigService.isBonusEnabled(organizationId)) {
            return new BonusPeriodSummaryDto(BigDecimal.ZERO, new ArrayList<>(), "Bonus module disabled for organization");
        }

        // 1. Check for modern approved BonusRecords in DB first
        if (bonusRecordRepository != null) {
            List<BonusRecord> approvedRecords = bonusRecordRepository.findEligibleForPayroll(
                    organizationId, employeeId, periodStart, periodEnd
            );
            if (!approvedRecords.isEmpty()) {
                BigDecimal totalAmount = BigDecimal.ZERO;
                List<Long> recordIds = new ArrayList<>();

                for (BonusRecord rec : approvedRecords) {
                    BigDecimal amt = rec.getEffectiveAmount();
                    totalAmount = totalAmount.add(amt != null ? amt : BigDecimal.ZERO);
                    recordIds.add(rec.getId());
                }

                return new BonusPeriodSummaryDto(totalAmount, recordIds, "Approved bonus records for period");
            }
        }

        // 2. Mock / testing fallback
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
        // 1. Process persisted database records
        if (bonusRecordRepository != null) {
            LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);
            List<BonusRecord> approvedRecords = bonusRecordRepository.findEligibleForPayroll(
                    organizationId, employeeId, periodStart, periodEnd
            );
            for (BonusRecord rec : approvedRecords) {
                rec.setPayrollStatus(BonusPayrollStatus.POSTED);
                rec.setStatus(BonusStatus.POSTED_TO_PAYROLL);
                rec.setPayrollRunId(payrollRunId);
                rec.setPayrollPostedAt(LocalDateTime.now());
                bonusRecordRepository.save(rec);
            }
        }

        // 2. Mark in-memory registry
        String key = buildPeriodKey(organizationId, employeeId, periodStart);
        consumedBonusRegistry.put(key, true);
    }

    private String buildPeriodKey(Long organizationId, Long employeeId, LocalDate periodStart) {
        return organizationId + ":" + employeeId + ":" + periodStart.getYear() + "-" + String.format("%02d", periodStart.getMonthValue());
    }
}
