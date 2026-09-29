package com.example.ems.offboarding.service;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.event.ApprovalWorkflowRejectedEvent;
import com.example.ems.offboarding.entity.EmployeeExit;
import com.example.ems.offboarding.repository.EmployeeExitRepository;
import com.example.ems.offboarding.repository.ExitFnfSettlementRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.rls.PostgresRlsSessionBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class ExitWorkflowEventListener {

    private static final Logger log = LoggerFactory.getLogger(ExitWorkflowEventListener.class);

    @Autowired
    private EmployeeExitRepository exitRepository;

    @Autowired
    private ExitFnfSettlementRepository fnfRepository;

    @Autowired(required = false)
    private PostgresRlsSessionBinder rlsSessionBinder;

    private void bindRls() {
        if (rlsSessionBinder != null) {
            try {
                rlsSessionBinder.bindCurrentTenant();
            } catch (Exception ignored) {
            }
        }
    }

    @EventListener
    @Transactional
    public void onApprovalWorkflowCompleted(ApprovalWorkflowCompletedEvent event) {
        log.info("ExitWorkflowEventListener received completed event: workflowType={}, refType={}, refId={}, org={}, status={}",
                event.getWorkflowType(), event.getBusinessReferenceType(), event.getBusinessReferenceId(),
                event.getOrganizationId(), event.getStatus());

        Long prevTenant = TenantContext.getCurrentTenant();
        try {
            if (event.getOrganizationId() != null) {
                TenantContext.setCurrentTenant(event.getOrganizationId());
                bindRls();
            }

            if (event.getWorkflowType() == WorkflowType.EMPLOYEE_EXIT) {
                handleExitWorkflow(event.getBusinessReferenceId(), event.getOrganizationId(), event.getStatus());
            } else if (event.getWorkflowType() == WorkflowType.FNF_APPROVAL) {
                handleFnfWorkflow(event.getBusinessReferenceId(), event.getOrganizationId(), event.getStatus());
            }
        } finally {
            if (prevTenant != null) {
                TenantContext.setCurrentTenant(prevTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    @EventListener
    @Transactional
    public void onApprovalWorkflowRejected(ApprovalWorkflowRejectedEvent event) {
        log.info("ExitWorkflowEventListener received rejected event: workflowType={}, refType={}, refId={}, org={}",
                event.getWorkflowType(), event.getBusinessReferenceType(), event.getBusinessReferenceId(),
                event.getOrganizationId());

        Long prevTenant = TenantContext.getCurrentTenant();
        try {
            if (event.getOrganizationId() != null) {
                TenantContext.setCurrentTenant(event.getOrganizationId());
                bindRls();
            }

            if (event.getWorkflowType() == WorkflowType.EMPLOYEE_EXIT) {
                handleExitWorkflow(event.getBusinessReferenceId(), event.getOrganizationId(), ApprovalStatus.REJECTED);
            } else if (event.getWorkflowType() == WorkflowType.FNF_APPROVAL) {
                handleFnfWorkflow(event.getBusinessReferenceId(), event.getOrganizationId(), ApprovalStatus.REJECTED);
            }
        } finally {
            if (prevTenant != null) {
                TenantContext.setCurrentTenant(prevTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    private void handleExitWorkflow(String refId, Long eventOrgId, ApprovalStatus status) {
        try {
            Long exitId = Long.parseLong(refId);

            // Anti-Forgery Guard: Entity must exist within the exact event organization context
            exitRepository.findById(exitId).ifPresentOrElse(exit -> {
                Long actualOrgId = exit.getOrganization() != null ? exit.getOrganization().getId() : null;
                if (eventOrgId != null && actualOrgId != null && !eventOrgId.equals(actualOrgId)) {
                    log.error("SECURITY ALERT: Forged cross-tenant approval event rejected! Event org {} does not match Exit org {} for exit ID {}",
                            eventOrgId, actualOrgId, exitId);
                    return; // REJECT MUTATION
                }

                if (status == ApprovalStatus.APPROVED) {
                    exit.setStatus("HR_OFFBOARDING_PENDING");
                } else if (status == ApprovalStatus.REJECTED) {
                    exit.setStatus("REJECTED");
                }
                exit.setUpdatedAt(LocalDateTime.now());
                exitRepository.save(exit);
                log.info("Updated EmployeeExit ID {} status to {}", exitId, exit.getStatus());
            }, () -> {
                log.warn("EmployeeExit ID {} not found in organization context {}", exitId, eventOrgId);
            });
        } catch (Exception e) {
            log.error("Failed to handle EmployeeExit workflow status change for refId {}: {}", refId, e.getMessage());
        }
    }

    private void handleFnfWorkflow(String refId, Long eventOrgId, ApprovalStatus status) {
        try {
            Long fnfId = Long.parseLong(refId);

            // Anti-Forgery Guard: Settlement must exist within the exact event organization context
            fnfRepository.findById(fnfId).ifPresentOrElse(fnf -> {
                Long actualOrgId = fnf.getOrganization() != null ? fnf.getOrganization().getId() : null;
                if (eventOrgId != null && actualOrgId != null && !eventOrgId.equals(actualOrgId)) {
                    log.error("SECURITY ALERT: Forged cross-tenant F&F approval event rejected! Event org {} does not match Settlement org {} for settlement ID {}",
                            eventOrgId, actualOrgId, fnfId);
                    return; // REJECT MUTATION
                }

                if (status == ApprovalStatus.APPROVED) {
                    fnf.setStatus("SETTLEMENT_APPROVED");
                } else if (status == ApprovalStatus.REJECTED) {
                    fnf.setStatus("REJECTED");
                }
                fnf.setUpdatedAt(LocalDateTime.now());
                fnfRepository.save(fnf);

                if (fnf.getExit() != null) {
                    EmployeeExit exit = fnf.getExit();
                    if (status == ApprovalStatus.APPROVED) {
                        exit.setStatus("SETTLEMENT_APPROVED");
                    } else if (status == ApprovalStatus.REJECTED) {
                        exit.setStatus("REJECTED");
                    }
                    exit.setUpdatedAt(LocalDateTime.now());
                    exitRepository.save(exit);
                }
                log.info("Updated FnfSettlement ID {} status to {}", fnfId, fnf.getStatus());
            }, () -> {
                log.warn("FnfSettlement ID {} not found in organization context {}", fnfId, eventOrgId);
            });
        } catch (Exception e) {
            log.error("Failed to handle FnfSettlement workflow status change for refId {}: {}", refId, e.getMessage());
        }
    }
}
