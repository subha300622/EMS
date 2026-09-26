package com.example.ems.overtime.dto;

import com.example.ems.overtime.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OvertimeRecordResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private Long attendanceId;
    private Long policyId;
    private String policyName;
    private Long policyVersion;
    private LocalDate workDate;

    private Integer scheduledMinutes;
    private Integer workedMinutes;
    private Integer rawOtMinutes;
    private Integer calculatedOtMinutes;
    private Integer adjustedOtMinutes;
    private Integer approvedOtMinutes;
    private Integer effectiveOtMinutes;

    private OvertimeAmountBasis amountBasis;
    private BigDecimal hourlyRate;
    private BigDecimal otMultiplier;
    private BigDecimal otRate;

    private BigDecimal calculatedAmount;
    private BigDecimal adjustedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal effectiveAmount;

    private OvertimeDayType dayType;
    private OvertimeStatus status;

    private String adjustmentReason;
    private String adjustedBy;
    private LocalDateTime adjustedAt;

    private String approvedBy;
    private LocalDateTime approvedAt;

    private String rejectionReason;
    private String rejectedBy;
    private LocalDateTime rejectedAt;

    private String workflowInstanceId;
    private Long payrollRunId;
    private OvertimePayrollStatus payrollStatus;

    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OvertimeRecordResponse() {}

    public static OvertimeRecordResponse fromEntity(OvertimeRecord record) {
        if (record == null) return null;
        OvertimeRecordResponse res = new OvertimeRecordResponse();
        res.setId(record.getId());
        if (record.getEmployee() != null) {
            res.setEmployeeId(record.getEmployee().getId());
            res.setEmployeeName(record.getEmployee().getFullName());
            res.setEmployeeCode(record.getEmployee().getEmployeeId());
        }
        if (record.getAttendance() != null) {
            res.setAttendanceId(record.getAttendance().getId());
        }
        if (record.getPolicy() != null) {
            res.setPolicyId(record.getPolicy().getId());
            res.setPolicyName(record.getPolicy().getName());
        }
        res.setPolicyVersion(record.getPolicyVersion());
        res.setWorkDate(record.getWorkDate());
        res.setScheduledMinutes(record.getScheduledMinutes());
        res.setWorkedMinutes(record.getWorkedMinutes());
        res.setRawOtMinutes(record.getRawOtMinutes());
        res.setCalculatedOtMinutes(record.getCalculatedOtMinutes());
        res.setAdjustedOtMinutes(record.getAdjustedOtMinutes());
        res.setApprovedOtMinutes(record.getApprovedOtMinutes());
        res.setEffectiveOtMinutes(record.getEffectiveOtMinutes());

        res.setAmountBasis(record.getAmountBasis());
        res.setHourlyRate(record.getHourlyRate());
        res.setOtMultiplier(record.getOtMultiplier());
        res.setOtRate(record.getOtRate());

        res.setCalculatedAmount(record.getCalculatedAmount());
        res.setAdjustedAmount(record.getAdjustedAmount());
        res.setApprovedAmount(record.getApprovedAmount());
        res.setEffectiveAmount(record.getEffectiveAmount());

        res.setDayType(record.getDayType());
        res.setStatus(record.getStatus());

        res.setAdjustmentReason(record.getAdjustmentReason());
        res.setAdjustedBy(record.getAdjustedBy());
        res.setAdjustedAt(record.getAdjustedAt());

        res.setApprovedBy(record.getApprovedBy());
        res.setApprovedAt(record.getApprovedAt());

        res.setRejectionReason(record.getRejectionReason());
        res.setRejectedBy(record.getRejectedBy());
        res.setRejectedAt(record.getRejectedAt());

        res.setWorkflowInstanceId(record.getWorkflowInstanceId());
        res.setPayrollRunId(record.getPayrollRunId());
        res.setPayrollStatus(record.getPayrollStatus());

        res.setVersion(record.getVersion());
        res.setCreatedAt(record.getCreatedAt());
        res.setUpdatedAt(record.getUpdatedAt());
        return res;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public Long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }

    public Long getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(Long policyVersion) { this.policyVersion = policyVersion; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public Integer getScheduledMinutes() { return scheduledMinutes; }
    public void setScheduledMinutes(Integer scheduledMinutes) { this.scheduledMinutes = scheduledMinutes; }

    public Integer getWorkedMinutes() { return workedMinutes; }
    public void setWorkedMinutes(Integer workedMinutes) { this.workedMinutes = workedMinutes; }

    public Integer getRawOtMinutes() { return rawOtMinutes; }
    public void setRawOtMinutes(Integer rawOtMinutes) { this.rawOtMinutes = rawOtMinutes; }

    public Integer getCalculatedOtMinutes() { return calculatedOtMinutes; }
    public void setCalculatedOtMinutes(Integer calculatedOtMinutes) { this.calculatedOtMinutes = calculatedOtMinutes; }

    public Integer getAdjustedOtMinutes() { return adjustedOtMinutes; }
    public void setAdjustedOtMinutes(Integer adjustedOtMinutes) { this.adjustedOtMinutes = adjustedOtMinutes; }

    public Integer getApprovedOtMinutes() { return approvedOtMinutes; }
    public void setApprovedOtMinutes(Integer approvedOtMinutes) { this.approvedOtMinutes = approvedOtMinutes; }

    public Integer getEffectiveOtMinutes() { return effectiveOtMinutes; }
    public void setEffectiveOtMinutes(Integer effectiveOtMinutes) { this.effectiveOtMinutes = effectiveOtMinutes; }

    public OvertimeAmountBasis getAmountBasis() { return amountBasis; }
    public void setAmountBasis(OvertimeAmountBasis amountBasis) { this.amountBasis = amountBasis; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public BigDecimal getOtMultiplier() { return otMultiplier; }
    public void setOtMultiplier(BigDecimal otMultiplier) { this.otMultiplier = otMultiplier; }

    public BigDecimal getOtRate() { return otRate; }
    public void setOtRate(BigDecimal otRate) { this.otRate = otRate; }

    public BigDecimal getCalculatedAmount() { return calculatedAmount; }
    public void setCalculatedAmount(BigDecimal calculatedAmount) { this.calculatedAmount = calculatedAmount; }

    public BigDecimal getAdjustedAmount() { return adjustedAmount; }
    public void setAdjustedAmount(BigDecimal adjustedAmount) { this.adjustedAmount = adjustedAmount; }

    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }

    public BigDecimal getEffectiveAmount() { return effectiveAmount; }
    public void setEffectiveAmount(BigDecimal effectiveAmount) { this.effectiveAmount = effectiveAmount; }

    public OvertimeDayType getDayType() { return dayType; }
    public void setDayType(OvertimeDayType dayType) { this.dayType = dayType; }

    public OvertimeStatus getStatus() { return status; }
    public void setStatus(OvertimeStatus status) { this.status = status; }

    public String getAdjustmentReason() { return adjustmentReason; }
    public void setAdjustmentReason(String adjustmentReason) { this.adjustmentReason = adjustmentReason; }

    public String getAdjustedBy() { return adjustedBy; }
    public void setAdjustedBy(String adjustedBy) { this.adjustedBy = adjustedBy; }

    public LocalDateTime getAdjustedAt() { return adjustedAt; }
    public void setAdjustedAt(LocalDateTime adjustedAt) { this.adjustedAt = adjustedAt; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }

    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }

    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public void setWorkflowInstanceId(String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }

    public Long getPayrollRunId() { return payrollRunId; }
    public void setPayrollRunId(Long payrollRunId) { this.payrollRunId = payrollRunId; }

    public OvertimePayrollStatus getPayrollStatus() { return payrollStatus; }
    public void setPayrollStatus(OvertimePayrollStatus payrollStatus) { this.payrollStatus = payrollStatus; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
