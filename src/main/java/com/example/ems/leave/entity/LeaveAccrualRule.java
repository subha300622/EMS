package com.example.ems.leave.entity;

import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_accrual_rules")
public class LeaveAccrualRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @JsonIgnore
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer annualQuota = 12;

    @Column(nullable = false)
    private String accrualFrequency = "MONTHLY"; // MONTHLY, QUARTERLY, ANNUAL

    @Column(nullable = false)
    private Double creditAmount = 1.0;

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public LeaveAccrualRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }

    public Integer getAnnualQuota() { return annualQuota; }
    public void setAnnualQuota(Integer annualQuota) { this.annualQuota = annualQuota; }

    public String getAccrualFrequency() { return accrualFrequency; }
    public void setAccrualFrequency(String accrualFrequency) { this.accrualFrequency = accrualFrequency; }

    public Double getCreditAmount() { return creditAmount; }
    public void setCreditAmount(Double creditAmount) { this.creditAmount = creditAmount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
