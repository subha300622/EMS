package com.example.ems.bonus.service;

import com.example.ems.bonus.dto.BonusRecordResponse;
import com.example.ems.bonus.entity.BonusPayrollStatus;
import com.example.ems.bonus.entity.BonusRecord;
import com.example.ems.bonus.entity.BonusStatus;
import com.example.ems.bonus.repository.BonusRecordRepository;
import com.example.ems.security.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BonusPayrollIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(BonusPayrollIntegrationService.class);

    private final BonusRecordRepository recordRepository;

    public BonusPayrollIntegrationService(BonusRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Transactional(readOnly = true)
    public List<BonusRecordResponse> getEligibleBonusRecords(Long employeeId, LocalDate periodStart, LocalDate periodEnd) {
        return getEligibleBonusRecords(employeeId, periodStart, periodEnd, null);
    }

    @Transactional(readOnly = true)
    public List<BonusRecordResponse> getEligibleBonusRecords(Long employeeId, LocalDate periodStart, LocalDate periodEnd, BonusPayrollStatus payrollStatus) {
        Long orgId = TenantContext.requireOrganizationId();
        List<BonusRecord> records = (payrollStatus != null)
                ? recordRepository.findEligibleForPayrollWithStatus(orgId, employeeId, payrollStatus, periodStart, periodEnd)
                : recordRepository.findEligibleForPayroll(orgId, employeeId, periodStart, periodEnd);
        return records.stream().map(BonusRecordResponse::fromEntity).toList();
    }

    public void markBonusesProcessed(Long employeeId, Long organizationId, LocalDate periodStart, LocalDate periodEnd, Long payrollRunId) {
        List<BonusRecord> eligibleRecords = recordRepository.findEligibleForPayroll(organizationId, employeeId, periodStart, periodEnd);
        for (BonusRecord record : eligibleRecords) {
            record.setPayrollStatus(BonusPayrollStatus.POSTED);
            record.setStatus(BonusStatus.POSTED_TO_PAYROLL);
            record.setPayrollRunId(payrollRunId);
            record.setPayrollPostedAt(LocalDateTime.now());
            recordRepository.save(record);
            log.info("Marked BonusRecord ID={} as POSTED to payroll run ID={}", record.getId(), payrollRunId);
        }
    }
}
