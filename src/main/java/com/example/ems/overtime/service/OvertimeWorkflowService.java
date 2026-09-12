package com.example.ems.overtime.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.overtime.dto.OvertimeRecordResponse;
import com.example.ems.overtime.entity.OvertimePayrollStatus;
import com.example.ems.overtime.entity.OvertimePolicy;
import com.example.ems.overtime.entity.OvertimeRecord;
import com.example.ems.overtime.entity.OvertimeStatus;
import com.example.ems.overtime.repository.OvertimeRecordRepository;
import com.example.ems.security.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class OvertimeWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(OvertimeWorkflowService.class);

    private final OvertimeRecordRepository recordRepository;
    private final ApprovalFacade approvalFacade;
    private final com.example.ems.organization.service.OrganizationCompensationConfigService compensationConfigService;

    @Autowired
    public OvertimeWorkflowService(OvertimeRecordRepository recordRepository,
                                   @Autowired(required = false) ApprovalFacade approvalFacade,
                                   @Autowired(required = false) com.example.ems.organization.service.OrganizationCompensationConfigService compensationConfigService) {
        this.recordRepository = recordRepository;
        this.approvalFacade = approvalFacade;
        this.compensationConfigService = compensationConfigService;
    }

    public OvertimeWorkflowService(OvertimeRecordRepository recordRepository,
                                   @Autowired(required = false) ApprovalFacade approvalFacade) {
        this(recordRepository, approvalFacade, null);
    }

    /**
     * Submits an overtime record for approval or auto-approves if the applicable policy
     * specifies approvalRequired = false.
     */
    public OvertimeRecordResponse submitOvertime(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null) {
            compensationConfigService.requireOvertimeEnabled(orgId);
        }

        OvertimeRecord record = recordRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime record not found with ID: " + id));

        if (record.getStatus() != OvertimeStatus.CALCULATED && record.getStatus() != OvertimeStatus.ADJUSTED) {
            throw new BadRequestException("Only records in CALCULATED or ADJUSTED status can be submitted for approval (current: " +
                    record.getStatus() + ").");
        }

        OvertimePolicy policy = record.getPolicy();
        boolean approvalRequired = policy == null || Boolean.TRUE.equals(policy.getApprovalRequired());

        if (!approvalRequired) {
            // Direct auto-approval without Central Approval Engine workflow
            int effectiveMins = record.getAdjustedOtMinutes() != null ? record.getAdjustedOtMinutes() : record.getCalculatedOtMinutes();
            BigDecimal effectiveAmount = record.getAdjustedAmount() != null ? record.getAdjustedAmount() : record.getCalculatedAmount();

            record.setApprovedOtMinutes(effectiveMins);
            record.setApprovedAmount(effectiveAmount);
            record.setStatus(OvertimeStatus.APPROVED);
            record.setApprovedBy("AUTO_POLICY");
            record.setApprovedAt(LocalDateTime.now());
            record.setPayrollStatus(OvertimePayrollStatus.PENDING);

            record = recordRepository.save(record);
            log.info("OvertimeRecord ID={} auto-approved (approvalRequired=false). ApprovedAmount={}", record.getId(), effectiveAmount);
            return OvertimeRecordResponse.fromEntity(record);
        }

        // Delegate to Central Approval Engine
        if (approvalFacade != null) {
            try {
                ApprovalContext context = new ApprovalContext(
                        orgId,
                        "OVERTIME_REQUEST",
                        "OVERTIME_REQUEST",
                        String.valueOf(record.getId()),
                        String.valueOf(record.getEmployee().getId())
                );
                BigDecimal effectiveAmount = record.getAdjustedAmount() != null ? record.getAdjustedAmount() : record.getCalculatedAmount();
                context.setAmount(effectiveAmount);

                ApprovalWorkflowInstance instance = approvalFacade.startApproval(context);
                if (instance != null) {
                    record.setWorkflowInstanceId(instance.getWorkflowInstanceId());
                }
            } catch (Exception e) {
                log.warn("Central approval workflow initiation failed for OvertimeRecord ID={}: {}. Falling back to PENDING_APPROVAL.",
                        record.getId(), e.getMessage());
            }
        }

        record.setStatus(OvertimeStatus.PENDING_APPROVAL);
        record = recordRepository.save(record);
        log.info("OvertimeRecord ID={} transitioned to PENDING_APPROVAL with workflowInstanceId={}",
                record.getId(), record.getWorkflowInstanceId());

        return OvertimeRecordResponse.fromEntity(record);
    }
}
