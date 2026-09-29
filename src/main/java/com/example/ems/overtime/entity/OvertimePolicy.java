package com.example.ems.overtime.entity;

import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "overtime_policies",
    indexes = {
        @Index(name = "idx_ot_policy_org_status", columnList = "organization_id, status"),
        @Index(name = "idx_ot_policy_org_effective", columnList = "organization_id, effective_from, effective_to")
    }
)
public class OvertimePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OvertimePolicyStatus status = OvertimePolicyStatus.DRAFT;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "normal_working_hours", nullable = false)
    private Integer normalWorkingHours = 8;

    @Column(name = "minimum_ot_minutes", nullable = false)
    private Integer minimumOtMinutes = 30;

    @Column(name = "maximum_ot_minutes", nullable = false)
    private Integer maximumOtMinutes = 240;

    @Enumerated(EnumType.STRING)
    @Column(name = "amount_basis", nullable = false, length = 30)
    private OvertimeAmountBasis amountBasis = OvertimeAmountBasis.BASIC_SALARY;

    @Column(name = "fixed_hourly_rate", precision = 15, scale = 2)
    private BigDecimal fixedHourlyRate;

    @Column(name = "working_days_per_month", nullable = false)
    private Integer workingDaysPerMonth = 26;

    @Column(name = "working_hours_per_day", nullable = false)
    private Integer workingHoursPerDay = 8;

    @Column(name = "normal_day_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal normalDayMultiplier = BigDecimal.valueOf(1.50);

    @Column(name = "weekend_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal weekendMultiplier = BigDecimal.valueOf(2.00);

    @Column(name = "holiday_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal holidayMultiplier = BigDecimal.valueOf(2.00);

    @Enumerated(EnumType.STRING)
    @Column(name = "rounding_rule", nullable = false, length = 30)
    private OvertimeRoundingRule roundingRule = OvertimeRoundingRule.EXACT;

    @Column(name = "approval_required", nullable = false)
    private Boolean approvalRequired = true;

    @Column(name = "department_id", length = 50)
    private String departmentId;

    @Column(name = "designation_id", length = 50)
    private String designationId;

    @Column(name = "employee_type", length = 50)
    private String employeeType;

    @Column(name = "branch_id", length = 50)
    private String branchId;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OvertimePolicy() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.version == null) this.version = 0L;
        if (this.normalWorkingHours == null) this.normalWorkingHours = 8;
        if (this.minimumOtMinutes == null) this.minimumOtMinutes = 30;
        if (this.maximumOtMinutes == null) this.maximumOtMinutes = 240;
        if (this.workingDaysPerMonth == null) this.workingDaysPerMonth = 26;
        if (this.workingHoursPerDay == null) this.workingHoursPerDay = 8;
        if (this.normalDayMultiplier == null) this.normalDayMultiplier = BigDecimal.valueOf(1.50);
        if (this.weekendMultiplier == null) this.weekendMultiplier = BigDecimal.valueOf(2.00);
        if (this.holidayMultiplier == null) this.holidayMultiplier = BigDecimal.valueOf(2.00);
        if (this.roundingRule == null) this.roundingRule = OvertimeRoundingRule.EXACT;
        if (this.approvalRequired == null) this.approvalRequired = true;
        if (this.status == null) this.status = OvertimePolicyStatus.DRAFT;
        if (this.amountBasis == null) this.amountBasis = OvertimeAmountBasis.BASIC_SALARY;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

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
