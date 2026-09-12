package com.example.ems.overtime.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.overtime.dto.OvertimeRecordResponse;
import com.example.ems.overtime.entity.OvertimePayrollStatus;
import com.example.ems.overtime.entity.OvertimeRecord;
import com.example.ems.overtime.entity.OvertimeStatus;
import com.example.ems.overtime.repository.OvertimeRecordRepository;
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
public class OvertimePayrollIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(OvertimePayrollIntegrationService.class);

    private final OvertimeRecordRepository recordRepository;

    public OvertimePayrollIntegrationService(OvertimeRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    /**
     * Retrieves all APPROVED overtime records eligible to enter the specified payroll run period.
     */
    @Transactional(readOnly = true)
    public List<OvertimeRecordResponse> getEligibleOvertimeRecords(Long employeeId, LocalDate periodStart, LocalDate periodEnd) {
        Long orgId = TenantContext.requireOrganizationId();
        List<OvertimeRecord> records = recordRepository.findEligibleForPayroll(orgId, employeeId, periodStart, periodEnd);
        return records.stream().map(OvertimeRecordResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * Idempotently posts approved overtime records to a payroll run.
     * Guarantees: One approved OT record can be posted to payroll only once.
     */
    public BigDecimal postOvertimeToPayrollRun(Long organizationId, Long employeeId,
                                              LocalDate periodStart, LocalDate periodEnd,
                                              Long payrollRunId) {
        if (payrollRunId == null) {
            throw new BadRequestException("payrollRunId is mandatory for posting overtime to payroll.");
        }

        List<OvertimeRecord> eligibleRecords = recordRepository.findEligibleForPayroll(
                organizationId, employeeId, periodStart, periodEnd
        );

        BigDecimal totalPostedAmount = BigDecimal.ZERO;

        for (OvertimeRecord record : eligibleRecords) {
            if (record.getStatus() == OvertimeStatus.APPROVED &&
                record.getPayrollStatus() == OvertimePayrollStatus.PENDING) {

                BigDecimal amount = record.getApprovedAmount() != null
                        ? record.getApprovedAmount()
                        : (record.getAdjustedAmount() != null ? record.getAdjustedAmount() : record.getCalculatedAmount());

                record.setStatus(OvertimeStatus.POSTED_TO_PAYROLL);
                record.setPayrollStatus(OvertimePayrollStatus.POSTED);
                record.setPayrollRunId(payrollRunId);

                recordRepository.save(record);
                totalPostedAmount = totalPostedAmount.add(amount);

                log.info("Posted OvertimeRecord ID={} (Amount={}) to PayrollRun ID={}",
                        record.getId(), amount, payrollRunId);
            }
        }

        return totalPostedAmount;
    }
}
