package com.example.ems.overtime.entity;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "overtime_records",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_ot_record_org_emp_att", columnNames = {"organization_id", "employee_id", "attendance_id"})
    },
    indexes = {
        @Index(name = "idx_ot_record_org_emp_date", columnList = "organization_id, employee_id, work_date"),
        @Index(name = "idx_ot_record_org_status", columnList = "organization_id, status"),
        @Index(name = "idx_ot_record_org_payroll", columnList = "organization_id, payroll_status, work_date"),
        @Index(name = "idx_ot_record_workflow", columnList = "workflow_instance_id")
    }
)
public class OvertimeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id")
    private Attendance attendance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private OvertimePolicy policy;

    @Column(name = "policy_version", nullable = false)
    private Long policyVersion = 0L;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "scheduled_minutes", nullable = false)
    private Integer scheduledMinutes = 480;

    @Column(name = "worked_minutes", nullable = false)
    private Integer workedMinutes = 0;

    @Column(name = "raw_ot_minutes", nullable = false)
    private Integer rawOtMinutes = 0;

    @Column(name = "calculated_ot_minutes", nullable = false)
    private Integer calculatedOtMinutes = 0;

    @Column(name = "adjusted_ot_minutes")
    private Integer adjustedOtMinutes;

    @Column(name = "approved_ot_minutes")
    private Integer approvedOtMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "amount_basis", nullable = false, length = 30)
    private OvertimeAmountBasis amountBasis;

    @Column(name = "hourly_rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    @Column(name = "ot_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal otMultiplier = BigDecimal.valueOf(1.50);

    @Column(name = "ot_rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal otRate = BigDecimal.ZERO;

    @Column(name = "calculated_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal calculatedAmount = BigDecimal.ZERO;

    @Column(name = "adjusted_amount", precision = 15, scale = 2)
    private BigDecimal adjustedAmount;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 30)
    private OvertimeDayType dayType = OvertimeDayType.NORMAL_DAY;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OvertimeStatus status = OvertimeStatus.CALCULATED;

    @Column(name = "adjustment_reason", columnDefinition = "TEXT")
    private String adjustmentReason;

    @Column(name = "adjusted_by", length = 100)
    private String adjustedBy;

    @Column(name = "adjusted_at")
    private LocalDateTime adjustedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "rejected_by", length = 100)
    private String rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "workflow_instance_id", length = 100)
    private String workflowInstanceId;

    @Column(name = "payroll_run_id")
    private Long payrollRunId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payroll_status", nullable = false, length = 30)
    private OvertimePayrollStatus payrollStatus = OvertimePayrollStatus.PENDING;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OvertimeRecord() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.version == null) this.version = 0L;
        if (this.status == null) this.status = OvertimeStatus.CALCULATED;
        if (this.payrollStatus == null) this.payrollStatus = OvertimePayrollStatus.PENDING;
        if (this.dayType == null) this.dayType = OvertimeDayType.NORMAL_DAY;
        if (this.hourlyRate == null) this.hourlyRate = BigDecimal.ZERO;
        if (this.otMultiplier == null) this.otMultiplier = BigDecimal.valueOf(1.50);
        if (this.otRate == null) this.otRate = BigDecimal.ZERO;
        if (this.calculatedAmount == null) this.calculatedAmount = BigDecimal.ZERO;
        if (this.rawOtMinutes == null) this.rawOtMinutes = 0;
        if (this.calculatedOtMinutes == null) this.calculatedOtMinutes = 0;
        if (this.scheduledMinutes == null) this.scheduledMinutes = 480;
        if (this.workedMinutes == null) this.workedMinutes = 0;
        if (this.policyVersion == null) this.policyVersion = 0L;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Effective payable minutes & amount getters
    public Integer getEffectiveOtMinutes() {
        if (approvedOtMinutes != null) return approvedOtMinutes;
        if (adjustedOtMinutes != null) return adjustedOtMinutes;
        return calculatedOtMinutes;
    }

    public BigDecimal getEffectiveAmount() {
        if (approvedAmount != null) return approvedAmount;
        if (adjustedAmount != null) return adjustedAmount;
        return calculatedAmount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Attendance getAttendance() { return attendance; }
    public void setAttendance(Attendance attendance) { this.attendance = attendance; }

    public OvertimePolicy getPolicy() { return policy; }
    public void setPolicy(OvertimePolicy policy) { this.policy = policy; }

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
