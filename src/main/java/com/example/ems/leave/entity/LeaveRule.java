package com.example.ems.leave.entity;

import com.example.ems.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_rules")
public class LeaveRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id")
    private LeaveType leaveType;

    private Integer minServiceDays = 0;
    private Integer maxConsecutiveDays = 14;
    private boolean includeWeekends = false;
    private boolean includeHolidays = false;
    private boolean allowHalfDay = true;
    private Integer noticePeriodDays = 0;
    private boolean allowNegativeBalance = false;
    private Integer maxCarryForwardDays = 5;

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public LeaveRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }

    public Integer getMinServiceDays() { return minServiceDays; }
    public void setMinServiceDays(Integer minServiceDays) { this.minServiceDays = minServiceDays; }

    public Integer getMaxConsecutiveDays() { return maxConsecutiveDays; }
    public void setMaxConsecutiveDays(Integer maxConsecutiveDays) { this.maxConsecutiveDays = maxConsecutiveDays; }

    public boolean isIncludeWeekends() { return includeWeekends; }
    public void setIncludeWeekends(boolean includeWeekends) { this.includeWeekends = includeWeekends; }

    public boolean isIncludeHolidays() { return includeHolidays; }
    public void setIncludeHolidays(boolean includeHolidays) { this.includeHolidays = includeHolidays; }

    public boolean isAllowHalfDay() { return allowHalfDay; }
    public void setAllowHalfDay(boolean allowHalfDay) { this.allowHalfDay = allowHalfDay; }

    public Integer getNoticePeriodDays() { return noticePeriodDays; }
    public void setNoticePeriodDays(Integer noticePeriodDays) { this.noticePeriodDays = noticePeriodDays; }

    public boolean isAllowNegativeBalance() { return allowNegativeBalance; }
    public void setAllowNegativeBalance(boolean allowNegativeBalance) { this.allowNegativeBalance = allowNegativeBalance; }

    public Integer getMaxCarryForwardDays() { return maxCarryForwardDays; }
    public void setMaxCarryForwardDays(Integer maxCarryForwardDays) { this.maxCarryForwardDays = maxCarryForwardDays; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
