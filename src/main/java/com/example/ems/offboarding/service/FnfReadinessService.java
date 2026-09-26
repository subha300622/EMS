package com.example.ems.offboarding.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.offboarding.dto.FnfBlockingItemDto;
import com.example.ems.offboarding.dto.FnfReadinessDto;
import com.example.ems.offboarding.entity.EmployeeExit;
import com.example.ems.offboarding.entity.ExitClearance;
import com.example.ems.offboarding.entity.FnfSettlement;
import com.example.ems.offboarding.repository.ExitClearanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class FnfReadinessService {

    @Autowired
    private ExitClearanceRepository clearanceRepository;

    public FnfReadinessDto checkClearanceReadiness(EmployeeExit exit) {
        List<FnfBlockingItemDto> blockingItems = new ArrayList<>();
        if (exit != null) {
            List<ExitClearance> clearances = clearanceRepository.findByExitId(exit.getId());
            if (clearances.isEmpty()) {
                blockingItems.add(new FnfBlockingItemDto(
                        "CLEARANCE", "EXIT-" + exit.getId(), "OFFBOARDING",
                        "Department clearances have not been initialized.", "BLOCKING"));
            } else {
                for (ExitClearance c : clearances) {
                    if (!"COMPLETED".equalsIgnoreCase(c.getStatus()) && !"CLEARED".equalsIgnoreCase(c.getStatus())) {
                        blockingItems.add(new FnfBlockingItemDto(
                                "CLEARANCE", "CLR-" + c.getId(), c.getDepartment(),
                                c.getDepartment() + " clearance is in status '" + c.getStatus() + "': "
                                        + (c.getClearanceReason() != null ? c.getClearanceReason() : "Pending review"),
                                "BLOCKING"));
                    }
                }
            }
        }
        boolean isReady = blockingItems.isEmpty();
        String message = isReady
                ? "All clearances completed. Ready for F&F settlement processing."
                : blockingItems.size() + " clearance item(s) currently blocking settlement.";
        return new FnfReadinessDto(isReady, exit != null ? exit.getStatus() : "UNKNOWN", "CLEARANCE_VERIFICATION",
                blockingItems, message);
    }

    public FnfReadinessDto checkClearanceReadiness(FnfSettlement settlement) {
        EmployeeExit exit = settlement != null ? settlement.getExit() : null;
        FnfReadinessDto dto = checkClearanceReadiness(exit);
        dto.setCurrentStatus(settlement != null ? settlement.getStatus() : dto.getCurrentStatus());
        return dto;
    }

    public FnfReadinessDto checkApprovalEligibility(FnfSettlement settlement) {
        List<FnfBlockingItemDto> blockingItems = new ArrayList<>();

        // 1. Clearances must be clean
        FnfReadinessDto clearanceReadiness = checkClearanceReadiness(settlement);
        blockingItems.addAll(clearanceReadiness.getBlockingItems());

        // 2. Compensation valid
        Employee emp = settlement.getExit() != null ? settlement.getExit().getEmployee() : null;
        if (emp == null || emp.getAnnualSalary() == null || emp.getAnnualSalary().compareTo(BigDecimal.ZERO) <= 0) {
            blockingItems.add(new FnfBlockingItemDto(
                    "COMPENSATION", emp != null ? "EMP-" + emp.getId() : "UNKNOWN", "PAYROLL",
                    "Employee annual compensation is unconfigured or zero.", "BLOCKING"));
        }

        // 3. Status must be CALCULATED or DRAFT
        String status = settlement.getStatus();
        if (!"CALCULATED".equalsIgnoreCase(status) && !"DRAFT".equalsIgnoreCase(status)) {
            blockingItems.add(new FnfBlockingItemDto(
                    "LIFECYCLE", "SETTLEMENT-" + settlement.getId(), "FINANCE",
                    "Settlement is in status '" + status
                            + "'. Only DRAFT or CALCULATED settlements can be submitted for approval.",
                    "BLOCKING"));
        }

        boolean isReady = blockingItems.isEmpty();

        String message = isReady
                ? "Settlement is eligible for submission into approval workflow."
                : "Settlement cannot be submitted: " + blockingItems.size() + " blocking issue(s) detected.";

        return new FnfReadinessDto(isReady, settlement.getStatus(), "APPROVAL_SUBMISSION", blockingItems, message);
    }

    public FnfReadinessDto checkPaymentReadiness(FnfSettlement settlement) {
        List<FnfBlockingItemDto> blockingItems = new ArrayList<>();

        // 1. Must be SETTLEMENT_APPROVED or PAYMENT_PENDING
        String status = settlement.getStatus();
        boolean validStatus = "PAYMENT_PENDING".equalsIgnoreCase(status) || "SETTLEMENT_APPROVED".equalsIgnoreCase(status);
        if (!validStatus) {
            blockingItems.add(new FnfBlockingItemDto(
                    "APPROVAL", "SETTLEMENT-" + settlement.getId(), "APPROVAL_ENGINE",
                    "Settlement status is '" + status + "'. Approvals must be complete and status must be PAYMENT_PENDING or SETTLEMENT_APPROVED.", "BLOCKING"));
        }

        // 2. Amount must be positive
                            
                    
        if (settlement.getNetSettlement() == null || settlement.getNetSettlement().compareTo(BigDecimal.ZERO) < 0) {
            blockingItems.add(new FnfBlockingItemDto(
                    "PAYMENT", "SETTLEMENT-" + settlement.getId(), "FINANCE",
                    "Net settlement amount cannot be negative.", "BLOCKING"));
        }

        // 3. Clearances must be fully cleared
        FnfReadinessDto clr = checkClearanceReadiness(settlement);
        blockingItems.addAll(clr.getBlockingItems());

        boolean isReady = blockingItems.isEmpty();
        String message = isReady
                ? "Settlement is fully cleared and verified for payment release."
                : "Payment disbursement blocked: " + blockingItems.size() + " requirement(s) pending.";

        return new FnfReadinessDto(isReady, settlement.getStatus(), "PAYMENT_DISBURSEMENT", blockingItems, message);
    }

    public FnfReadinessDto checkFinalizationReadiness(FnfSettlement settlement) {
        List<FnfBlockingItemDto> blockingItems = new ArrayList<>();

        // 1. Must be PAYMENT_RELEASED
        if (!"PAYMENT_RELEASED".equalsIgnoreCase(settlement.getStatus())) {
            blockingItems.add(new FnfBlockingItemDto(
                    "PAYMENT", "SETTLEMENT-" + settlement.getId(), "FINANCE",
                    "Payment has not been confirmed released. Current status: " + settlement.getStatus(), "BLOCKING"));
        }

        // 2. Payment reference must exist
        if (settlement.getPaymentReference() == null || settlement.getPaymentReference().isBlank()) {
            blockingItems.add(new FnfBlockingItemDto(
                    "AUDIT", "SETTLEMENT-" + settlement.getId(), "FINANCE",
                    "Payment reference number is missing.", "BLOCKING"));
        }

        // 3. Employee must be EXITED
        Employee emp = settlement.getExit() != null ? settlement.getExit().getEmployee() : null;
        if (emp != null && !"EXITED".equalsIgnoreCase(emp.getStatus())) {
            blockingItems.add(new FnfBlockingItemDto(
                    "EMPLOYEE", "EMP-" + emp.getId(), "HR",
                    "Employee status is '" + emp.getStatus() + "' instead of EXITED.", "BLOCKING"));
        }

        boolean isReady = blockingItems.isEmpty();
        String message = isReady
                ? "Settlement meets all terminal finalization criteria."
                : "Finalization blocked by " + blockingItems.size() + " pending item(s).";

        return new FnfReadinessDto(isReady, settlement.getStatus(), "FINALIZATION", blockingItems, message);
    }
}
