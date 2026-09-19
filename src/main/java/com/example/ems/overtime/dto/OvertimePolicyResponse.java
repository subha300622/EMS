package com.example.ems.overtime.dto;

import com.example.ems.overtime.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OvertimePolicyResponse {

    private Long id;
    private String name;
    private String description;
    private OvertimePolicyStatus status;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Integer normalWorkingHours;
    private Integer minimumOtMinutes;
    private Integer maximumOtMinutes;
    private OvertimeAmountBasis amountBasis;
    private BigDecimal fixedHourlyRate;
    private Integer workingDaysPerMonth;
    private Integer workingHoursPerDay;
    private BigDecimal normalDayMultiplier;
    private BigDecimal weekendMultiplier;
    private BigDecimal holidayMultiplier;
    private OvertimeRoundingRule roundingRule;
    private Boolean approvalRequired;
    private String departmentId;
    private String designationId;
    private String employeeType;
    private String branchId;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OvertimePolicyResponse() {}

    public static OvertimePolicyResponse fromEntity(OvertimePolicy policy) {
        if (policy == null) return null;
        OvertimePolicyResponse res = new OvertimePolicyResponse();
        res.setId(policy.getId());
        res.setName(policy.getName());
        res.setDescription(policy.getDescription());
        res.setStatus(policy.getStatus());
        res.setEffectiveFrom(policy.getEffectiveFrom());
        res.setEffectiveTo(policy.getEffectiveTo());
        res.setNormalWorkingHours(policy.getNormalWorkingHours());
        res.setMinimumOtMinutes(policy.getMinimumOtMinutes());
        res.setMaximumOtMinutes(policy.getMaximumOtMinutes());
        res.setAmountBasis(policy.getAmountBasis());
        res.setFixedHourlyRate(policy.getFixedHourlyRate());
        res.setWorkingDaysPerMonth(policy.getWorkingDaysPerMonth());
        res.setWorkingHoursPerDay(policy.getWorkingHoursPerDay());
        res.setNormalDayMultiplier(policy.getNormalDayMultiplier());
        res.setWeekendMultiplier(policy.getWeekendMultiplier());
        res.setHolidayMultiplier(policy.getHolidayMultiplier());
        res.setRoundingRule(policy.getRoundingRule());
        res.setApprovalRequired(policy.getApprovalRequired());
        res.setDepartmentId(policy.getDepartmentId());
        res.setDesignationId(policy.getDesignationId());
        res.setEmployeeType(policy.getEmployeeType());
        res.setBranchId(policy.getBranchId());
        res.setVersion(policy.getVersion());
        res.setCreatedAt(policy.getCreatedAt());
        res.setUpdatedAt(policy.getUpdatedAt());
        return res;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OvertimePolicyStatus getStatus() { return status; }
    public void setStatus(OvertimePolicyStatus status) { this.status = status; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }

    public Integer getNormalWorkingHours() { return normalWorkingHours; }
    public void setNormalWorkingHours(Integer normalWorkingHours) { this.normalWorkingHours = normalWorkingHours; }

    public Integer getMinimumOtMinutes() { return minimumOtMinutes; }
    public void setMinimumOtMinutes(Integer minimumOtMinutes) { this.minimumOtMinutes = minimumOtMinutes; }

    public Integer getMaximumOtMinutes() { return maximumOtMinutes; }
    public void setMaximumOtMinutes(Integer maximumOtMinutes) { this.maximumOtMinutes = maximumOtMinutes; }

    public OvertimeAmountBasis getAmountBasis() { return amountBasis; }
    public void setAmountBasis(OvertimeAmountBasis amountBasis) { this.amountBasis = amountBasis; }

    public BigDecimal getFixedHourlyRate() { return fixedHourlyRate; }
    public void setFixedHourlyRate(BigDecimal fixedHourlyRate) { this.fixedHourlyRate = fixedHourlyRate; }

    public Integer getWorkingDaysPerMonth() { return workingDaysPerMonth; }
    public void setWorkingDaysPerMonth(Integer workingDaysPerMonth) { this.workingDaysPerMonth = workingDaysPerMonth; }

    public Integer getWorkingHoursPerDay() { return workingHoursPerDay; }
    public void setWorkingHoursPerDay(Integer workingHoursPerDay) { this.workingHoursPerDay = workingHoursPerDay; }

    public BigDecimal getNormalDayMultiplier() { return normalDayMultiplier; }
    public void setNormalDayMultiplier(BigDecimal normalDayMultiplier) { this.normalDayMultiplier = normalDayMultiplier; }

    public BigDecimal getWeekendMultiplier() { return weekendMultiplier; }
    public void setWeekendMultiplier(BigDecimal weekendMultiplier) { this.weekendMultiplier = weekendMultiplier; }

    public BigDecimal getHolidayMultiplier() { return holidayMultiplier; }
    public void setHolidayMultiplier(BigDecimal holidayMultiplier) { this.holidayMultiplier = holidayMultiplier; }

    public OvertimeRoundingRule getRoundingRule() { return roundingRule; }
    public void setRoundingRule(OvertimeRoundingRule roundingRule) { this.roundingRule = roundingRule; }

    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public String getDesignationId() { return designationId; }
    public void setDesignationId(String designationId) { this.designationId = designationId; }

    public String getEmployeeType() { return employeeType; }
    public void setEmployeeType(String employeeType) { this.employeeType = employeeType; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
