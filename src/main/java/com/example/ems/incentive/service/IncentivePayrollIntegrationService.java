package com.example.ems.incentive.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.incentive.dto.IncentiveRecordResponse;
import com.example.ems.incentive.entity.IncentivePayrollStatus;
import com.example.ems.incentive.entity.IncentiveRecord;
import com.example.ems.incentive.entity.IncentiveStatus;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import com.example.ems.security.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class IncentivePayrollIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(IncentivePayrollIntegrationService.class);

    private final IncentiveRecordRepository recordRepository;

    public IncentivePayrollIntegrationService(IncentiveRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    /**
     * Retrieves all APPROVED incentive records eligible to enter the specified payroll run period.
     */
    @Transactional(readOnly = true)
    public List<IncentiveRecordResponse> getEligibleIncentiveRecords(Long employeeId, LocalDate periodStart, LocalDate periodEnd) {
        Long orgId = TenantContext.requireOrganizationId();
        List<IncentiveRecord> records = recordRepository.findEligibleForPayroll(orgId, employeeId, periodStart, periodEnd);
        return records.stream().map(IncentiveRecordResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * Idempotently posts approved incentive records to a payroll run.
     * Guarantees: One approved incentive record can be posted to payroll only once.
     */
    public BigDecimal postIncentivesToPayrollRun(Long organizationId, Long employeeId,
                                                LocalDate periodStart, LocalDate periodEnd,
                                                Long payrollRunId) {
        if (payrollRunId == null) {
            throw new BadRequestException("payrollRunId is mandatory for posting incentive to payroll.");
        }

        List<IncentiveRecord> eligibleRecords = recordRepository.findEligibleForPayroll(
                organizationId, employeeId, periodStart, periodEnd
        );

        BigDecimal totalPostedAmount = BigDecimal.ZERO;

        for (IncentiveRecord record : eligibleRecords) {
            if (record.getStatus() == IncentiveStatus.APPROVED &&
                record.getPayrollStatus() == IncentivePayrollStatus.PENDING) {

                BigDecimal amount = record.getEffectiveAmount();

                record.setStatus(IncentiveStatus.POSTED_TO_PAYROLL);
                record.setPayrollStatus(IncentivePayrollStatus.POSTED);
                record.setPayrollRunId(payrollRunId);

                recordRepository.save(record);
                totalPostedAmount = totalPostedAmount.add(amount != null ? amount : BigDecimal.ZERO);

                log.info("Posted IncentiveRecord ID={} (Amount={}) to PayrollRun ID={}",
                        record.getId(), amount, payrollRunId);
            }
        }

        return totalPostedAmount;
    }
}
