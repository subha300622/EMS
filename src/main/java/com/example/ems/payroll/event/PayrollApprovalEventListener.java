package com.example.ems.payroll.event;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalChangesRequestedEvent;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.event.ApprovalWorkflowRejectedEvent;
import com.example.ems.payroll.entity.PayrollRun;
import com.example.ems.payroll.entity.PayrollRunStatus;
import com.example.ems.payroll.repository.PayrollRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class PayrollApprovalEventListener {

    private static final Logger log = LoggerFactory.getLogger(PayrollApprovalEventListener.class);

    private final PayrollRunRepository payrollRunRepository;

    @Autowired
    public PayrollApprovalEventListener(PayrollRunRepository payrollRunRepository) {
        this.payrollRunRepository = payrollRunRepository;
    }

    @EventListener
    @Transactional
    public void onApprovalWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        if (!isPayrollWorkflow(event.getWorkflowType(), event.getBusinessReferenceType())) {
            return;
        }

        try {
            Long runId = Long.parseLong(event.getBusinessReferenceId());
            PayrollRun run = payrollRunRepository.findById(runId).orElse(null);
            if (run == null) {
                log.warn("Payroll run not found for approval completion event, runId: {}", runId);
                return;
            }

            if (!isMatchingTenantAndApprovalId(run, event.getOrganizationId(), event.getWorkflowInstanceId())) {
                return;
            }

            // Idempotent: If already in approved or subsequent state, do not re-mutate
            if (run.getStatus() == PayrollRunStatus.APPROVED ||
                run.getStatus() == PayrollRunStatus.LOCKED ||
                run.getStatus() == PayrollRunStatus.FINALIZED ||
                run.getStatus() == PayrollRunStatus.PAYMENT_PROCESSING ||
                run.getStatus() == PayrollRunStatus.PAID) {
                log.info("Payroll run {} already in terminal/approved status {}, ignoring duplicate event", runId, run.getStatus());
                return;
            }

            if (event.getStatus() == ApprovalStatus.APPROVED) {
                run.setStatus(PayrollRunStatus.APPROVED);
                run.setApprovedAt(LocalDateTime.now());
                run.setRejectionReason(null);
                payrollRunRepository.save(run);
                log.info("Payroll run {} successfully transitioned to APPROVED", runId);
            }
        } catch (Exception e) {
            log.error("Error handling ApprovalWorkflowCompletedEvent for payroll run", e);
        }
    }

    @EventListener
    @Transactional
    public void onApprovalWorkflowRejected(ApprovalWorkflowRejectedEvent event) {
        if (!isPayrollWorkflow(event.getWorkflowType(), event.getBusinessReferenceType())) {
            return;
        }

        try {
            Long runId = Long.parseLong(event.getBusinessReferenceId());
            PayrollRun run = payrollRunRepository.findById(runId).orElse(null);
            if (run == null) {
                log.warn("Payroll run not found for approval rejected event, runId: {}", runId);
                return;
            }

            if (!isMatchingTenantAndApprovalId(run, event.getOrganizationId(), event.getWorkflowInstanceId())) {
                return;
            }

            if (run.getStatus() == PayrollRunStatus.REJECTED) {
                log.info("Payroll run {} already REJECTED, ignoring duplicate event", runId);
                return;
            }

            run.setStatus(PayrollRunStatus.REJECTED);
            run.setRejectionReason(event.getReason());
            payrollRunRepository.save(run);
            log.info("Payroll run {} transitioned to REJECTED. Reason: {}", runId, event.getReason());
        } catch (Exception e) {
            log.error("Error handling ApprovalWorkflowRejectedEvent for payroll run", e);
        }
    }

    @EventListener
    @Transactional
    public void onApprovalChangesRequested(ApprovalChangesRequestedEvent event) {
        if (!isPayrollWorkflow(event.getWorkflowType(), event.getBusinessReferenceType())) {
            return;
        }

        try {
            Long runId = Long.parseLong(event.getBusinessReferenceId());
            PayrollRun run = payrollRunRepository.findById(runId).orElse(null);
            if (run == null) {
                log.warn("Payroll run not found for approval changes requested event, runId: {}", runId);
                return;
            }

            if (!isMatchingTenantAndApprovalId(run, event.getOrganizationId(), event.getWorkflowInstanceId())) {
                return;
            }

            if (run.getStatus() == PayrollRunStatus.CHANGES_REQUESTED) {
                log.info("Payroll run {} already in CHANGES_REQUESTED status, ignoring duplicate event", runId);
                return;
            }

            run.setStatus(PayrollRunStatus.CHANGES_REQUESTED);
            run.setRejectionReason(event.getComments());
            payrollRunRepository.save(run);
            log.info("Payroll run {} transitioned to CHANGES_REQUESTED. Comments: {}", runId, event.getComments());
        } catch (Exception e) {
            log.error("Error handling ApprovalChangesRequestedEvent for payroll run", e);
        }
    }

    private boolean isPayrollWorkflow(WorkflowType workflowType, String businessReferenceType) {
        return workflowType == WorkflowType.PAYROLL_APPROVAL ||
               "PAYROLL_APPROVAL".equalsIgnoreCase(businessReferenceType) ||
               "PAYROLL".equalsIgnoreCase(businessReferenceType);
    }

    private boolean isMatchingTenantAndApprovalId(PayrollRun run, Long organizationId, String workflowInstanceId) {
        if (organizationId != null && run.getOrganizationId() != null &&
            !organizationId.equals(run.getOrganizationId())) {
            log.warn("Organization mismatch on payroll approval event: expected {}, got {}",
                    run.getOrganizationId(), organizationId);
            return false;
        }

        if (workflowInstanceId != null && run.getApprovalInstanceId() != null &&
            !workflowInstanceId.equals(run.getApprovalInstanceId())) {
            log.warn("ApprovalInstanceId mismatch: run has {}, event has {}",
                    run.getApprovalInstanceId(), workflowInstanceId);
            return false;
        }

        return true;
    }
}
