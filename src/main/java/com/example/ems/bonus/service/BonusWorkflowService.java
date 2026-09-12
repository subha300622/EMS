package com.example.ems.bonus.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.bonus.dto.BonusRecordResponse;
import com.example.ems.bonus.entity.BonusPayrollStatus;
import com.example.ems.bonus.entity.BonusRecord;
import com.example.ems.bonus.entity.BonusStatus;
import com.example.ems.bonus.entity.BonusType;
import com.example.ems.bonus.repository.BonusRecordRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.security.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class BonusWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(BonusWorkflowService.class);

    private final BonusRecordRepository recordRepository;
    private final ApprovalFacade approvalFacade;
    private final com.example.ems.organization.service.OrganizationCompensationConfigService compensationConfigService;

    @Autowired
    public BonusWorkflowService(BonusRecordRepository recordRepository,
                                @Autowired(required = false) ApprovalFacade approvalFacade,
                                @Autowired(required = false) com.example.ems.organization.service.OrganizationCompensationConfigService compensationConfigService) {
        this.recordRepository = recordRepository;
        this.approvalFacade = approvalFacade;
        this.compensationConfigService = compensationConfigService;
    }

    public BonusWorkflowService(BonusRecordRepository recordRepository,
                                @Autowired(required = false) ApprovalFacade approvalFacade) {
        this(recordRepository, approvalFacade, null);
    }

    public BonusRecordResponse submitForApproval(Long id) {
        return submitBonus(id);
    }

    public BonusRecordResponse submitBonus(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null) {
            compensationConfigService.requireBonusEnabled(orgId);
        }
        BonusRecord record = recordRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus record not found with ID: " + id));

        if (record.getStatus() != BonusStatus.CALCULATED && record.getStatus() != BonusStatus.ADJUSTED) {
            throw new BadRequestException("Only CALCULATED or ADJUSTED bonus records can be submitted for approval.");
        }

        boolean approvalRequired = record.getPolicy() == null || Boolean.TRUE.equals(record.getPolicy().getApprovalRequired());

        if (!approvalRequired) {
            // Auto-approval path
            BigDecimal finalAmount = record.getAdjustedAmount() != null ? record.getAdjustedAmount() : record.getCalculatedAmount();
            record.setApprovedAmount(finalAmount);
            record.setStatus(BonusStatus.APPROVED);
            record.setPayrollStatus(BonusPayrollStatus.PENDING);
            record.setApprovedBy("SYSTEM_AUTO_APPROVAL");
            record.setApprovedAt(LocalDateTime.now());

            BonusRecord saved = recordRepository.save(record);
            log.info("BonusRecord ID={} auto-approved (approvalRequired=false). ApprovedAmount={}", saved.getId(), finalAmount);
            return BonusRecordResponse.fromEntity(saved);
        }

        // Central Approval Platform path
        if (approvalFacade != null) {
            try {
                ApprovalContext context = new ApprovalContext(
                        orgId,
                        "BONUS_REQUEST",
                        "BONUS_RECORD",
                        String.valueOf(record.getId()),
                        String.valueOf(record.getEmployee().getId())
                );
                BigDecimal effectiveAmount = record.getEffectiveAmount();
                context.setAmount(effectiveAmount);

                ApprovalWorkflowInstance instance = approvalFacade.startApproval(context);
                if (instance != null) {
                    record.setWorkflowInstanceId(instance.getWorkflowInstanceId());
                } else {
                    record.setWorkflowInstanceId("WFI-BONUS-" + record.getId());
                }
            } catch (Exception e) {
                log.warn("Central approval workflow initiation failed for BonusRecord ID={}: {}. Falling back to default instance ID.",
                        record.getId(), e.getMessage());
                record.setWorkflowInstanceId("WFI-BONUS-" + record.getId());
            }
        } else {
            record.setWorkflowInstanceId("WFI-BONUS-" + record.getId());
        }

        record.setStatus(BonusStatus.PENDING_APPROVAL);
        BonusRecord saved = recordRepository.save(record);
        log.info("BonusRecord ID={} transitioned to PENDING_APPROVAL with workflowInstanceId={}",
                saved.getId(), saved.getWorkflowInstanceId());

        return BonusRecordResponse.fromEntity(saved);
    }

    public BonusRecordResponse cancelBonus(Long id, String reason) {
        Long orgId = TenantContext.requireOrganizationId();
        BonusRecord record = recordRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus record not found with ID: " + id));

        if (record.getStatus() == BonusStatus.POSTED_TO_PAYROLL || record.getStatus() == BonusStatus.PAID) {
            throw new BadRequestException("Cannot cancel a bonus record that has already been processed in payroll.");
        }

        if (record.getStatus() == BonusStatus.PENDING_APPROVAL && approvalFacade != null) {
            try {
                approvalFacade.cancel(WorkflowType.BONUS_REQUEST, "BONUS_RECORD", String.valueOf(record.getId()), reason);
            } catch (Exception e) {
                log.warn("Could not cancel central approval workflow for BonusRecord ID={}: {}", id, e.getMessage());
            }
        }

        record.setStatus(BonusStatus.CANCELLED);
        record.setRejectionReason(reason != null ? reason : "Cancelled by user");
        BonusRecord saved = recordRepository.save(record);
        log.info("BonusRecord ID={} marked as CANCELLED", saved.getId());
        return BonusRecordResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public BonusRecordResponse getRecordById(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        BonusRecord record = recordRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus record not found with ID: " + id));
        return BonusRecordResponse.fromEntity(record);
    }

    @Transactional(readOnly = true)
    public Page<BonusRecordResponse> searchRecords(Long employeeId, Long policyId, BonusType bonusType,
                                                  BonusStatus status, BonusPayrollStatus payrollStatus,
                                                  LocalDate periodStart, LocalDate periodEnd,
                                                  Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        Page<BonusRecord> page = recordRepository.findFiltered(
                orgId, employeeId, policyId, bonusType, status, payrollStatus, periodStart, periodEnd, pageable);
        return page.map(BonusRecordResponse::fromEntity);
    }
}
