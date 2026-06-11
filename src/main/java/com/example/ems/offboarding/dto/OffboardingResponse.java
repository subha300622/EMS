package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.entity.ExitInterview;
import com.example.ems.offboarding.entity.Offboarding;
import com.example.ems.offboarding.entity.OffboardingAssetReturn;
import com.example.ems.offboarding.entity.OffboardingHandover;
import com.example.ems.offboarding.entity.OffboardingSettlement;
import com.example.ems.offboarding.entity.OffboardingTask;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OffboardingResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private String status;
    private String reason;
    private LocalDate exitDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<OffboardingTaskResponse> tasks;
    private List<AssetReturnResponse> assetReturns;
    private List<SettlementResponse> settlements;
    private List<HandoverResponse> handovers;
    private List<ExitInterviewResponse> exitInterviews;

    public OffboardingResponse() {}

    public OffboardingResponse(Offboarding offboarding,
                              List<OffboardingTask> tasks,
                              List<OffboardingAssetReturn> assetReturns,
                              List<OffboardingSettlement> settlements,
                              List<OffboardingHandover> handovers,
                              List<ExitInterview> exitInterviews) {
        this.id = offboarding.getId();
        if (offboarding.getEmployee() != null) {
            this.employeeId = offboarding.getEmployee().getId();
            this.employeeName = offboarding.getEmployee().getFullName();
            this.employeeEmail = offboarding.getEmployee().getEmail();
        }
        this.status = offboarding.getStatus();
        this.reason = offboarding.getReason();
        this.exitDate = offboarding.getExitDate();
        this.createdAt = offboarding.getCreatedAt();
        this.updatedAt = offboarding.getUpdatedAt();

        if (tasks != null) {
            this.tasks = tasks.stream().map(OffboardingTaskResponse::new).collect(Collectors.toList());
        }
        if (assetReturns != null) {
            this.assetReturns = assetReturns.stream().map(AssetReturnResponse::new).collect(Collectors.toList());
        }
        if (settlements != null) {
            this.settlements = settlements.stream().map(SettlementResponse::new).collect(Collectors.toList());
        }
        if (handovers != null) {
            this.handovers = handovers.stream().map(HandoverResponse::new).collect(Collectors.toList());
        }
        if (exitInterviews != null) {
            this.exitInterviews = exitInterviews.stream().map(ExitInterviewResponse::new).collect(Collectors.toList());
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getExitDate() { return exitDate; }
    public void setExitDate(LocalDate exitDate) { this.exitDate = exitDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OffboardingTaskResponse> getTasks() { return tasks; }
    public void setTasks(List<OffboardingTaskResponse> tasks) { this.tasks = tasks; }

    public List<AssetReturnResponse> getAssetReturns() { return assetReturns; }
    public void setAssetReturns(List<AssetReturnResponse> assetReturns) { this.assetReturns = assetReturns; }

    public List<SettlementResponse> getSettlements() { return settlements; }
    public void setSettlements(List<SettlementResponse> settlements) { this.settlements = settlements; }

    public List<HandoverResponse> getHandovers() { return handovers; }
    public void setHandovers(List<HandoverResponse> handovers) { this.handovers = handovers; }

    public List<ExitInterviewResponse> getExitInterviews() { return exitInterviews; }
    public void setExitInterviews(List<ExitInterviewResponse> exitInterviews) { this.exitInterviews = exitInterviews; }

    // Nested DTO classes
    public static class AssetReturnResponse {
        private Long id;
        private String assetName;
        private String serialNumber;
        private String returnStatus;
        private LocalDateTime returnedAt;

        public AssetReturnResponse(OffboardingAssetReturn asset) {
            this.id = asset.getId();
            this.assetName = asset.getAssetName();
            this.serialNumber = asset.getSerialNumber();
            this.returnStatus = asset.getReturnStatus();
            this.returnedAt = asset.getReturnedAt();
        }

        public Long getId() { return id; }
        public String getAssetName() { return assetName; }
        public String getSerialNumber() { return serialNumber; }
        public String getReturnStatus() { return returnStatus; }
        public LocalDateTime getReturnedAt() { return returnedAt; }
    }

    public static class SettlementResponse {
        private Long id;
        private BigDecimal gratuity;
        private BigDecimal severance;
        private BigDecimal pendingSalary;
        private BigDecimal deductions;
        private BigDecimal totalSettlementAmount;
        private String paymentStatus;
        private LocalDateTime processedAt;

        public SettlementResponse(OffboardingSettlement s) {
            this.id = s.getId();
            this.gratuity = s.getGratuity();
            this.severance = s.getSeverance();
            this.pendingSalary = s.getPendingSalary();
            this.deductions = s.getDeductions();
            this.totalSettlementAmount = s.getTotalSettlementAmount();
            this.paymentStatus = s.getPaymentStatus();
            this.processedAt = s.getProcessedAt();
        }

        public Long getId() { return id; }
        public BigDecimal getGratuity() { return gratuity; }
        public BigDecimal getSeverance() { return severance; }
        public BigDecimal getPendingSalary() { return pendingSalary; }
        public BigDecimal getDeductions() { return deductions; }
        public BigDecimal getTotalSettlementAmount() { return totalSettlementAmount; }
        public String getPaymentStatus() { return paymentStatus; }
        public LocalDateTime getProcessedAt() { return processedAt; }
    }

    public static class HandoverResponse {
        private Long id;
        private String taskName;
        private Long recipientId;
        private String recipientName;
        private String status;
        private LocalDateTime completedAt;

        public HandoverResponse(OffboardingHandover h) {
            this.id = h.getId();
            this.taskName = h.getTaskName();
            this.status = h.getStatus();
            this.completedAt = h.getCompletedAt();
            if (h.getRecipientEmployee() != null) {
                this.recipientId = h.getRecipientEmployee().getId();
                this.recipientName = h.getRecipientEmployee().getFullName();
            }
        }

        public Long getId() { return id; }
        public String getTaskName() { return taskName; }
        public Long getRecipientId() { return recipientId; }
        public String getRecipientName() { return recipientName; }
        public String getStatus() { return status; }
        public LocalDateTime getCompletedAt() { return completedAt; }
    }

    public static class ExitInterviewResponse {
        private Long id;
        private LocalDate interviewDate;
        private String interviewerName;
        private String status;
        private String feedback;
        private String reasonsForLeaving;
        private Integer rating;

        public ExitInterviewResponse(ExitInterview e) {
            this.id = e.getId();
            this.interviewDate = e.getInterviewDate();
            this.interviewerName = e.getInterviewerName();
            this.status = e.getStatus();
            this.feedback = e.getFeedback();
            this.reasonsForLeaving = e.getReasonsForLeaving();
            this.rating = e.getRating();
        }

        public Long getId() { return id; }
        public LocalDate getInterviewDate() { return interviewDate; }
        public String getInterviewerName() { return interviewerName; }
        public String getStatus() { return status; }
        public String getFeedback() { return feedback; }
        public String getReasonsForLeaving() { return reasonsForLeaving; }
        public Integer getRating() { return rating; }
    }
}
