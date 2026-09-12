package com.example.ems.leave.dto;

public class CreateLeaveRuleRequest {
    private Long leaveTypeId;
    private Integer minServiceDays = 0;
    private Integer maxConsecutiveDays = 14;
    private boolean includeWeekends = false;
    private boolean includeHolidays = false;
    private boolean allowHalfDay = true;
    private Integer noticePeriodDays = 0;
    private boolean allowNegativeBalance = false;
    private Integer maxCarryForwardDays = 5;
    private Boolean allowLop = true;
    private Boolean allowEncashment = false;
    private Double maxEncashmentDays = 0.0;
    private Double minBalanceRetained = 0.0;

    public Long getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(Long leaveTypeId) { this.leaveTypeId = leaveTypeId; }

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

    public Boolean getAllowLop() { return allowLop; }
    public void setAllowLop(Boolean allowLop) { this.allowLop = allowLop; }

    public Boolean getAllowEncashment() { return allowEncashment; }
    public void setAllowEncashment(Boolean allowEncashment) { this.allowEncashment = allowEncashment; }

    public Double getMaxEncashmentDays() { return maxEncashmentDays; }
    public void setMaxEncashmentDays(Double maxEncashmentDays) { this.maxEncashmentDays = maxEncashmentDays; }

    public Double getMinBalanceRetained() { return minBalanceRetained; }
    public void setMinBalanceRetained(Double minBalanceRetained) { this.minBalanceRetained = minBalanceRetained; }
}
