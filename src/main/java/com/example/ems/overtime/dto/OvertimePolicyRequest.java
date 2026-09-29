package com.example.ems.overtime.dto;

import com.example.ems.overtime.entity.OvertimeAmountBasis;
import com.example.ems.overtime.entity.OvertimeRoundingRule;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OvertimePolicyRequest {

    @NotBlank(message = "Policy name is mandatory")
    @Size(max = 150, message = "Policy name cannot exceed 150 characters")
    private String name;

    private String description;

    @NotNull(message = "effectiveFrom is mandatory")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @NotNull(message = "normalWorkingHours is mandatory")
    @Min(value = 1, message = "normalWorkingHours must be at least 1")
    @Max(value = 24, message = "normalWorkingHours cannot exceed 24")
    private Integer normalWorkingHours = 8;

    @NotNull(message = "minimumOtMinutes is mandatory")
    @Min(value = 0, message = "minimumOtMinutes cannot be negative")
    private Integer minimumOtMinutes = 30;

    @NotNull(message = "maximumOtMinutes is mandatory")
    @Min(value = 1, message = "maximumOtMinutes must be at least 1")
    private Integer maximumOtMinutes = 240;

    @NotNull(message = "amountBasis is mandatory")
    private OvertimeAmountBasis amountBasis = OvertimeAmountBasis.BASIC_SALARY;

    @DecimalMin(value = "0.0", inclusive = false, message = "fixedHourlyRate must be greater than 0")
    private BigDecimal fixedHourlyRate;

    @NotNull(message = "workingDaysPerMonth is mandatory")
    @Min(value = 1, message = "workingDaysPerMonth must be at least 1")
    @Max(value = 31, message = "workingDaysPerMonth cannot exceed 31")
    private Integer workingDaysPerMonth = 26;

    @NotNull(message = "workingHoursPerDay is mandatory")
    @Min(value = 1, message = "workingHoursPerDay must be at least 1")
    @Max(value = 24, message = "workingHoursPerDay cannot exceed 24")
    private Integer workingHoursPerDay = 8;

    @NotNull(message = "normalDayMultiplier is mandatory")
    @DecimalMin(value = "1.0", message = "normalDayMultiplier must be at least 1.0")
    private BigDecimal normalDayMultiplier = BigDecimal.valueOf(1.50);

    @NotNull(message = "weekendMultiplier is mandatory")
    @DecimalMin(value = "1.0", message = "weekendMultiplier must be at least 1.0")
    private BigDecimal weekendMultiplier = BigDecimal.valueOf(2.00);

    @NotNull(message = "holidayMultiplier is mandatory")
    @DecimalMin(value = "1.0", message = "holidayMultiplier must be at least 1.0")
    private BigDecimal holidayMultiplier = BigDecimal.valueOf(2.00);

    @NotNull(message = "roundingRule is mandatory")
    private OvertimeRoundingRule roundingRule = OvertimeRoundingRule.EXACT;

    @NotNull(message = "approvalRequired is mandatory")
    private Boolean approvalRequired = true;

    private String departmentId;
    private String designationId;
    private String employeeType;
    private String branchId;

    public OvertimePolicyRequest() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

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
}
