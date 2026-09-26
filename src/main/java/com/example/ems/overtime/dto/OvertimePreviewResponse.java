package com.example.ems.overtime.dto;

import com.example.ems.overtime.entity.OvertimeAmountBasis;
import com.example.ems.overtime.entity.OvertimeDayType;
import com.example.ems.overtime.entity.OvertimeRoundingRule;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OvertimePreviewResponse {

    private Long attendanceId;
    private Long employeeId;
    private String employeeName;
    private LocalDate workDate;

    private Integer scheduledMinutes;
    private Integer workedMinutes;
    private Integer rawOtMinutes;
    private Integer calculatedOtMinutes;

    private OvertimeDayType dayType;
    private OvertimeAmountBasis amountBasis;
    private BigDecimal hourlyRate;
    private BigDecimal otMultiplier;
    private BigDecimal otRate;
    private BigDecimal calculatedAmount;

    private OvertimeRoundingRule roundingRule;
    private Long policyId;
    private String policyName;
    private Long policyVersion;
    private Boolean approvalRequired;

    public OvertimePreviewResponse() {}

    // Getters and Setters
    public Long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

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

    public OvertimeDayType getDayType() { return dayType; }
    public void setDayType(OvertimeDayType dayType) { this.dayType = dayType; }

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

    public OvertimeRoundingRule getRoundingRule() { return roundingRule; }
    public void setRoundingRule(OvertimeRoundingRule roundingRule) { this.roundingRule = roundingRule; }

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }

    public Long getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(Long policyVersion) { this.policyVersion = policyVersion; }

    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }
}
