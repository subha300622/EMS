package com.example.ems.incentive.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.incentive.dto.IncentiveAdjustmentRequest;
import com.example.ems.incentive.dto.IncentiveRecordResponse;
import com.example.ems.incentive.entity.IncentiveRecord;
import com.example.ems.incentive.entity.IncentiveStatus;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import com.example.ems.security.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class IncentiveAdjustmentService {

    private static final Logger log = LoggerFactory.getLogger(IncentiveAdjustmentService.class);

    private final IncentiveRecordRepository recordRepository;

    public IncentiveAdjustmentService(IncentiveRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public IncentiveRecordResponse adjustIncentive(Long id, IncentiveAdjustmentRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        IncentiveRecord record = recordRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Incentive record not found with ID: " + id));

        if (record.getStatus() == IncentiveStatus.APPROVED ||
            record.getStatus() == IncentiveStatus.POSTED_TO_PAYROLL ||
            record.getStatus() == IncentiveStatus.PAID) {
            throw new BadRequestException("Cannot adjust an incentive record in " + record.getStatus() + " status.");
        }

        if (request.getAdjustedAmount() == null || request.getAdjustedAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Adjusted amount must be non-negative.");
        }

        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BadRequestException("Adjustment reason is mandatory.");
        }

        if (record.getPolicy() != null && record.getPolicy().getMaximumAmount() != null) {
            if (request.getAdjustedAmount().compareTo(record.getPolicy().getMaximumAmount()) > 0) {
                throw new BadRequestException(String.format("Adjusted amount (%.2f) exceeds policy maximum cap (%.2f).",
                        request.getAdjustedAmount(), record.getPolicy().getMaximumAmount()));
            }
        }

        // Preserve original calculation permanently
        record.setAdjustedAmount(request.getAdjustedAmount().setScale(2, java.math.RoundingMode.HALF_UP));
        record.setAdjustmentReason(request.getReason().trim());
        record.setAdjustedBy(resolveCurrentUsername());
        record.setAdjustedAt(LocalDateTime.now());
        record.setStatus(IncentiveStatus.ADJUSTED);

        IncentiveRecord saved = recordRepository.save(record);
        log.info("Adjusted IncentiveRecord ID={} for Emp ID={}: OriginalCalcAmount={}, AdjustedAmount={}, AdjustedBy={}",
                saved.getId(), saved.getEmployee().getId(), saved.getCalculatedAmount(), saved.getAdjustedAmount(), saved.getAdjustedBy());

        return IncentiveRecordResponse.fromEntity(saved);
    }

    private String resolveCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "system";
    }
}
