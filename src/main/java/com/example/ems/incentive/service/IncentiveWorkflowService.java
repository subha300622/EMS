package com.example.ems.incentive.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.incentive.dto.IncentiveRecordResponse;
import com.example.ems.incentive.entity.IncentivePayrollStatus;
import com.example.ems.incentive.entity.IncentiveRecord;
import com.example.ems.incentive.entity.IncentiveStatus;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
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
public class IncentiveWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(IncentiveWorkflowService.class);

    private final IncentiveRecordRepository recordRepository;
    private final ApprovalFacade approvalFacade;
    private final com.example.ems.organization.service.OrganizationCompensationConfigService compensationConfigService;

    @Autowired
    public IncentiveWorkflowService(IncentiveRecordRepository recordRepository,
                                    @Autowired(required = false) ApprovalFacade approvalFacade,
                                    @Autowired(required = false) com.example.ems.organization.service.OrganizationCompensationConfigService compensationConfigService) {
        this.recordRepository = recordRepository;
        this.approvalFacade = approvalFacade;
        this.compensationConfigService = compensationConfigService;
    }

    public IncentiveWorkflowService(IncentiveRecordRepository recordRepository,
                                    @Autowired(required = false) ApprovalFacade approvalFacade) {
        this(recordRepository, approvalFacade, null);
    }

    public IncentiveRecordResponse submitForApproval(Long id) {
        return submitIncentive(id);
    }

    public IncentiveRecordResponse submitIncentive(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null) {
            compensationConfigService.requireIncentiveEnabled(orgId);
        }
        IncentiveRecord record = recordRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Incentive record not found with ID: " + id));

        if (record.getStatus() != IncentiveStatus.CALCULATED && record.getStatus() != IncentiveStatus.ADJUSTED) {
            throw new BadRequestException("Only CALCULATED or ADJUSTED incentive records can be submitted for approval.");
        }

        boolean approvalRequired = record.getPolicy() == null || Boolean.TRUE.equals(record.getPolicy().getApprovalRequired());

        if (!approvalRequired) {
            // Auto-approval path
            BigDecimal finalAmount = record.getAdjustedAmount() != null ? record.getAdjustedAmount() : record.getCalculatedAmount();
            record.setApprovedAmount(finalAmount);
            record.setStatus(IncentiveStatus.APPROVED);
            record.setPayrollStatus(IncentivePayrollStatus.PENDING);
            record.setApprovedBy("SYSTEM_AUTO_APPROVAL");
            record.setApprovedAt(LocalDateTime.now());

            IncentiveRecord saved = recordRepository.save(record);
            log.info("IncentiveRecord ID={} auto-approved (approvalRequired=false). ApprovedAmount={}", saved.getId(), finalAmount);
            return IncentiveRecordResponse.fromEntity(saved);
        }

        // Central Approval Platform path
        if (approvalFacade != null) {
            try {
                ApprovalContext context = new ApprovalContext(
                        orgId,
                        "INCENTIVE_REQUEST",
                        "INCENTIVE_RECORD",
                        String.valueOf(record.getId()),
                        String.valueOf(record.getEmployee().getId())
                );
                BigDecimal effectiveAmount = record.getEffectiveAmount();
                context.setAmount(effectiveAmount);

                ApprovalWorkflowInstance instance = approvalFacade.startApproval(context);
                if (instance != null) {
                    record.setWorkflowInstanceId(instance.getWorkflowInstanceId());
                } else {
                    record.setWorkflowInstanceId("WFI-INC-" + record.getId());
                }
            } catch (Exception e) {
                log.warn("Central approval workflow initiation failed for IncentiveRecord ID={}: {}. Falling back.",
                        record.getId(), e.getMessage());
                record.setWorkflowInstanceId("WFI-INC-" + record.getId());
            }
        } else {
            record.setWorkflowInstanceId("WFI-INC-" + record.getId());
        }

        record.setStatus(IncentiveStatus.PENDING_APPROVAL);
        IncentiveRecord saved = recordRepository.save(record);
        log.info("IncentiveRecord ID={} transitioned to PENDING_APPROVAL with workflowInstanceId={}",
                saved.getId(), saved.getWorkflowInstanceId());

        return IncentiveRecordResponse.fromEntity(saved);
    }

    public IncentiveRecordResponse cancelIncentive(Long id, String reason) {
        Long orgId = TenantContext.requireOrganizationId();
        IncentiveRecord record = recordRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Incentive record not found with ID: " + id));

        if (record.getStatus() == IncentiveStatus.POSTED_TO_PAYROLL) {
            throw new BadRequestException("Cannot cancel an incentive record that has already been posted to payroll.");
        }

        if (record.getStatus() == IncentiveStatus.PENDING_APPROVAL && approvalFacade != null) {
            try {
                approvalFacade.cancel(WorkflowType.INCENTIVE_REQUEST, "INCENTIVE_RECORD", String.valueOf(record.getId()), reason);
            } catch (Exception e) {
                log.warn("Could not cancel central approval workflow for IncentiveRecord ID={}: {}", id, e.getMessage());
            }
        }

        record.setStatus(IncentiveStatus.CANCELLED);
        record.setRejectionReason(reason != null ? reason : "Cancelled by user");
        IncentiveRecord saved = recordRepository.save(record);
        log.info("IncentiveRecord ID={} marked as CANCELLED", saved.getId());
        return IncentiveRecordResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public IncentiveRecordResponse getRecordById(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        IncentiveRecord record = recordRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Incentive record not found with ID: " + id));
        return IncentiveRecordResponse.fromEntity(record);
    }

    @Transactional(readOnly = true)
    public Page<IncentiveRecordResponse> searchRecords(Long employeeId, IncentiveStatus status,
                                                      IncentivePayrollStatus payrollStatus,
                                                      LocalDate periodStart, LocalDate periodEnd,
                                                      Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        Page<IncentiveRecord> page = recordRepository.findFiltered(orgId, employeeId, status, payrollStatus, periodStart, periodEnd, pageable);
        return page.map(IncentiveRecordResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<IncentiveRecordResponse> searchRecords(Long employeeId, IncentiveStatus status,
                                                      LocalDate periodStart, LocalDate periodEnd,
                                                      Pageable pageable) {
        return searchRecords(employeeId, status, null, periodStart, periodEnd, pageable);
    }
}