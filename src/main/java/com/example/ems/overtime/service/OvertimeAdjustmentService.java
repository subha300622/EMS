package com.example.ems.overtime.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.overtime.dto.OvertimeAdjustmentRequest;
import com.example.ems.overtime.dto.OvertimeRecordResponse;
import com.example.ems.overtime.entity.OvertimeRecord;
import com.example.ems.overtime.entity.OvertimeStatus;
import com.example.ems.overtime.repository.OvertimeRecordRepository;
import com.example.ems.security.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Transactional
public class OvertimeAdjustmentService {

    private static final Logger log = LoggerFactory.getLogger(OvertimeAdjustmentService.class);

    private final OvertimeRecordRepository recordRepository;

    public OvertimeAdjustmentService(OvertimeRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    /**
     * Adjusts an existing calculated overtime record while strictly preserving the initial
     * attendance-derived calculation for permanent auditability.
     */
    public OvertimeRecordResponse adjustOvertime(Long id, OvertimeAdjustmentRequest request) {
        Long orgId = TenantContext.requireOrganizationId();

        if (request == null || request.getAdjustedOtMinutes() == null) {
            throw new BadRequestException("adjustedOtMinutes is mandatory.");
        }
        if (request.getAdjustedOtMinutes() < 0) {
            throw new BadRequestException("adjustedOtMinutes cannot be negative (" + request.getAdjustedOtMinutes() + ").");
        }
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BadRequestException("Adjustment reason is mandatory.");
        }

        OvertimeRecord record = recordRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime record not found with ID: " + id));

        // Invariant: Forbidden to adjust after approval or payroll posting
        if (record.getStatus() == OvertimeStatus.APPROVED ||
            record.getStatus() == OvertimeStatus.POSTED_TO_PAYROLL ||
            record.getStatus() == OvertimeStatus.PAID) {
            throw new BadRequestException("Cannot adjust overtime record in status " + record.getStatus() +
                    ". Only unapproved records (CALCULATED, ADJUSTED, SUBMITTED, PENDING_APPROVAL) can be adjusted.");
        }

        // Validate policy maximum cap if policy is attached
        if (record.getPolicy() != null && record.getPolicy().getMaximumOtMinutes() != null) {
            int maxCap = record.getPolicy().getMaximumOtMinutes();
            if (request.getAdjustedOtMinutes() > maxCap) {
                throw new BadRequestException("adjustedOtMinutes (" + request.getAdjustedOtMinutes() +
                        ") exceeds the maximum policy limit of " + maxCap + " minutes.");
            }
        }

        // Recalculate adjustedAmount using the ORIGINAL captured otRate (never mutate original calculated values)
        BigDecimal otRate = record.getOtRate() != null ? record.getOtRate() : BigDecimal.ZERO;
        BigDecimal adjustedHours = BigDecimal.valueOf(request.getAdjustedOtMinutes())
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal adjustedAmount = adjustedHours.multiply(otRate).setScale(2, RoundingMode.HALF_UP);

        String currentUser = resolveCurrentUser();

        record.setAdjustedOtMinutes(request.getAdjustedOtMinutes());
        record.setAdjustedAmount(adjustedAmount);
        record.setAdjustmentReason(request.getReason().trim());
        record.setAdjustedBy(currentUser);
        record.setAdjustedAt(LocalDateTime.now());
        record.setStatus(OvertimeStatus.ADJUSTED);

        record = recordRepository.save(record);
        log.info("Adjusted OvertimeRecord ID={} for Emp ID={}: OriginalCalcMins={}, AdjustedMins={}, AdjustedAmount={}, AdjustedBy={}",
                record.getId(), record.getEmployee().getId(), record.getCalculatedOtMinutes(),
                record.getAdjustedOtMinutes(), record.getAdjustedAmount(), currentUser);

        return OvertimeRecordResponse.fromEntity(record);
    }

    private String resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            return auth.getName();
        }
        return "SYSTEM";
    }
}
