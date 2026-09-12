package com.example.ems.leave.dto;

public class CreateAccrualRuleRequest {
    private Long leaveTypeId;
    private Integer annualQuota = 12;
    private String accrualFrequency = "MONTHLY"; // MONTHLY, QUARTERLY, ANNUAL
    private Double creditAmount = 1.0;

    public Long getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(Long leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    public Integer getAnnualQuota() { return annualQuota; }
    public void setAnnualQuota(Integer annualQuota) { this.annualQuota = annualQuota; }

    public String getAccrualFrequency() { return accrualFrequency; }
    public void setAccrualFrequency(String accrualFrequency) { this.accrualFrequency = accrualFrequency; }

    public Double getCreditAmount() { return creditAmount; }
    public void setCreditAmount(Double creditAmount) { this.creditAmount = creditAmount; }
}
