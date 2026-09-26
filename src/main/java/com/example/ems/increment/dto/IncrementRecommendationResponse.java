package com.example.ems.increment.dto;

import com.example.ems.increment.entity.IncrementRecommendationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Increment Recommendation Response")
public class IncrementRecommendationResponse {

    @Schema(description = "Recommendation ID", example = "1")
    private Long id;
    @Schema(description = "Employee ID", example = "1")
    private Long employeeId;
    @Schema(description = "Employee full name", example = "John Doe")
    private String employeeName;
    @Schema(description = "Employee code", example = "EMP001")
    private String employeeCode;
    @Schema(description = "Department name", example = "Engineering")
    private String department;
    @Schema(description = "Designation", example = "Senior Software Engineer")
    private String designation;
    @Schema(description = "Appraisal ID", example = "5")
    private Long appraisalId;
    @Schema(description = "Increment cycle ID", example = "2")
    private Long cycleId;
    @Schema(description = "Increment cycle name", example = "FY 2026-27 Annual Increment Cycle")
    private String cycleName;
    @Schema(description = "Current salary", example = "100000.00")
    private BigDecimal currentSalary;
    @Schema(description = "Increment percentage", example = "12.5")
    private BigDecimal incrementPercentage;
    @Schema(description = "Calculated increment amount", example = "12500.00")
    private BigDecimal incrementAmount;
    @Schema(description = "Recommended new salary", example = "112500.00")
    private BigDecimal recommendedSalary;
    @Schema(description = "Effective date", example = "2026-07-01")
    private LocalDate effectiveDate;
    @Schema(description = "Recommendation comments", example = "High performance rating and key achievements")
    private String comments;
    @Schema(description = "Recommendation status", example = "RECOMMENDED")
    private IncrementRecommendationStatus status;
    @Schema(description = "Approval request ID", example = "10")
    private Long approvalRequestId;
    @Schema(description = "Rejection reason if rejected")
    private String rejectionReason;
    @Schema(description = "Employee reference details")
    private EmployeeReference employee;
    @Schema(description = "Approval reference details")
    private ApprovalReference approval;
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public static class EmployeeReference {
        private Long employeeId;
        private String name;

        public EmployeeReference() {}
        public EmployeeReference(Long employeeId, String name) {
            this.employeeId = employeeId;
            this.name = name;
        }
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class ApprovalReference {
        private String status;

        public ApprovalReference() {}
        public ApprovalReference(String status) {
            this.status = status;
        }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public BigDecimal getCurrentSalary() { return currentSalary; }
    public void setCurrentSalary(BigDecimal currentSalary) { this.currentSalary = currentSalary; }

    public BigDecimal getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(BigDecimal incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public BigDecimal getIncrementAmount() { return incrementAmount; }
    public void setIncrementAmount(BigDecimal incrementAmount) { this.incrementAmount = incrementAmount; }

    public BigDecimal getRecommendedSalary() { return recommendedSalary; }
    public void setRecommendedSalary(BigDecimal recommendedSalary) { this.recommendedSalary = recommendedSalary; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public IncrementRecommendationStatus getStatus() { return status; }
    public void setStatus(IncrementRecommendationStatus status) { this.status = status; }

    public Long getApprovalRequestId() { return approvalRequestId; }
    public void setApprovalRequestId(Long approvalRequestId) { this.approvalRequestId = approvalRequestId; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public EmployeeReference getEmployee() { return employee; }
    public void setEmployee(EmployeeReference employee) { this.employee = employee; }

    public ApprovalReference getApproval() { return approval; }
    public void setApproval(ApprovalReference approval) { this.approval = approval; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
