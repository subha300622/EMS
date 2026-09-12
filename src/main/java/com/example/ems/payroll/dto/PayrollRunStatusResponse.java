package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.PayrollRun;
import com.example.ems.payroll.entity.PayrollRunStatus;

import java.time.LocalDateTime;

public class PayrollRunStatusResponse {

    private Long runId;
    private PayrollRunStatus status;
    private String approvalInstanceId;
    private Integer totalEmployees;
    private Integer processedEmployees;
    private LocalDateTime finalizedAt;
    private LocalDateTime updatedAt;

    public PayrollRunStatusResponse() {}

    public PayrollRunStatusResponse(Long runId, PayrollRunStatus status, String approvalInstanceId,
                                    Integer totalEmployees, Integer processedEmployees,
                                    LocalDateTime finalizedAt, LocalDateTime updatedAt) {
        this.runId = runId;
        this.status = status;
        this.approvalInstanceId = approvalInstanceId;
        this.totalEmployees = totalEmployees;
        this.processedEmployees = processedEmployees;
        this.finalizedAt = finalizedAt;
        this.updatedAt = updatedAt;
    }

    public static PayrollRunStatusResponse fromEntity(PayrollRun run) {
        if (run == null) return null;
        return new PayrollRunStatusResponse(
                run.getId(),
                run.getStatus(),
                run.getApprovalInstanceId(),
                run.getTotalEmployees(),
                run.getProcessedEmployees(),
                run.getFinalizedAt(),
                run.getUpdatedAt()
        );
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public PayrollRunStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollRunStatus status) {
        this.status = status;
    }

    public String getApprovalInstanceId() {
        return approvalInstanceId;
    }

    public void setApprovalInstanceId(String approvalInstanceId) {
        this.approvalInstanceId = approvalInstanceId;
    }

    public Integer getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(Integer totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public Integer getProcessedEmployees() {
        return processedEmployees;
    }

    public void setProcessedEmployees(Integer processedEmployees) {
        this.processedEmployees = processedEmployees;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
