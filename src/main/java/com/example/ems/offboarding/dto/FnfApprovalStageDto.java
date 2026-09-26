package com.example.ems.offboarding.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Single stage in F&F approval workflow")
public class FnfApprovalStageDto {

    @Schema(description = "Stage sequence number in approval workflow", example = "1")
    private Integer sequence;

    @Schema(description = "Required approver role: FINANCE, HR, SUPER_ADMIN", example = "FINANCE")
    private String role;

    @Schema(description = "Human-readable name of approval step", example = "Finance Team Verification")
    private String stepName;

    @Schema(description = "User ID of designated approver", example = "1002")
    private Long approverId;

    @Schema(description = "Full name of designated approver", example = "Robert Chen")
    private String approverName;

    @Schema(description = "Approval stage state: PENDING, APPROVED, REJECTED, HOLD", example = "APPROVED")
    private String status; // PENDING, APPROVED, REJECTED, HOLD

    @Schema(description = "Approver commentary / sign-off notes", example = "All ledger and deduction items audited successfully.")
    private String remarks;

    @Schema(description = "Timestamp when approval action was executed", example = "2026-09-18T11:20:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime actedAt;

    public FnfApprovalStageDto() {}

    public FnfApprovalStageDto(Integer sequence, String role, String stepName, Long approverId, String approverName, String status, String remarks, LocalDateTime actedAt) {
        this.sequence = sequence;
        this.role = role;
        this.stepName = stepName;
        this.approverId = approverId;
        this.approverName = approverName;
        this.status = status;
        this.remarks = remarks;
        this.actedAt = actedAt;
    }

    public Integer getSequence() { return sequence; }
    public void setSequence(Integer sequence) { this.sequence = sequence; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getActedAt() { return actedAt; }
    public void setActedAt(LocalDateTime actedAt) { this.actedAt = actedAt; }
}

