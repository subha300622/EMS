package com.example.ems.bonus.service;

import com.example.ems.bonus.dto.BonusAdjustmentRequest;
import com.example.ems.bonus.dto.BonusRecordResponse;
import com.example.ems.bonus.entity.BonusRecord;
import com.example.ems.bonus.entity.BonusStatus;
import com.example.ems.bonus.repository.BonusRecordRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.security.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class BonusAdjustmentService {

    private static final Logger log = LoggerFactory.getLogger(BonusAdjustmentService.class);

    private final BonusRecordRepository recordRepository;

    public BonusAdjustmentService(BonusRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public BonusRecordResponse adjustBonus(Long id, BonusAdjustmentRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        BonusRecord record = recordRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus record not found with ID: " + id));

        if (record.getStatus() == BonusStatus.POSTED_TO_PAYROLL || record.getStatus() == BonusStatus.PAID) {
            throw new BadRequestException("Cannot adjust a bonus record that has already been processed in payroll.");
        }

        if (record.getStatus() == BonusStatus.APPROVED) {
            throw new BadRequestException("Cannot adjust an already APPROVED bonus record directly. Re-open workflow or re-calculate.");
        }

        if (record.getStatus() == BonusStatus.CANCELLED) {
            throw new BadRequestException("Cannot adjust a CANCELLED bonus record.");
        }

        if (request.getAdjustmentReason() == null || request.getAdjustmentReason().trim().isEmpty()) {
            throw new BadRequestException("A valid adjustment reason is mandatory.");
        }

        if (request.getAdjustedAmount() == null) {
            throw new BadRequestException("Adjusted amount cannot be null.");
        }

        String currentUser = resolveCurrentUser();

        // Preserve calculatedAmount, set adjustedAmount
        record.setAdjustedAmount(request.getAdjustedAmount());
        record.setAdjustmentReason(request.getAdjustmentReason().trim());
        record.setAdjustedBy(currentUser);
        record.setAdjustedAt(LocalDateTime.now());
        record.setStatus(BonusStatus.ADJUSTED);

        BonusRecord saved = recordRepository.save(record);
        log.info("Adjusted BonusRecord ID={}: original calculatedAmount={}, new adjustedAmount={}, adjustedBy={}",
                saved.getId(), saved.getCalculatedAmount(), saved.getAdjustedAmount(), currentUser);

        return BonusRecordResponse.fromEntity(saved);
    }

    private String resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            return auth.getName();
        }
        return "SYSTEM";
    }
}
