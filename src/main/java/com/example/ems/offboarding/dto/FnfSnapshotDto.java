package com.example.ems.offboarding.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FnfSnapshotDto {

    public static class SalarySnapshot {
        private Long assignmentId;
        private BigDecimal annualSalary;
        private BigDecimal monthlyBase;
        private Integer workedDays;
        private Integer daysInMonth;
        private BigDecimal dailyRate;
        private BigDecimal proRatedSalary;

        public SalarySnapshot() {}

        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
        public BigDecimal getAnnualSalary() { return annualSalary; }
        public void setAnnualSalary(BigDecimal annualSalary) { this.annualSalary = annualSalary; }
        public BigDecimal getMonthlyBase() { return monthlyBase; }
        public void setMonthlyBase(BigDecimal monthlyBase) { this.monthlyBase = monthlyBase; }
        public Integer getWorkedDays() { return workedDays; }
        public void setWorkedDays(Integer workedDays) { this.workedDays = workedDays; }
        public Integer getDaysInMonth() { return daysInMonth; }
        public void setDaysInMonth(Integer daysInMonth) { this.daysInMonth = daysInMonth; }
        public BigDecimal getDailyRate() { return dailyRate; }
        public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
        public BigDecimal getProRatedSalary() { return proRatedSalary; }
        public void setProRatedSalary(BigDecimal proRatedSalary) { this.proRatedSalary = proRatedSalary; }
    }

    public static class LeaveSnapshot {
        private Double encashableDays;
        private BigDecimal dailyRate;
        private BigDecimal leaveEncashmentAmount;

        public LeaveSnapshot() {}

        public Double getEncashableDays() { return encashableDays; }
        public void setEncashableDays(Double encashableDays) { this.encashableDays = encashableDays; }
        public BigDecimal getDailyRate() { return dailyRate; }
        public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
        public BigDecimal getLeaveEncashmentAmount() { return leaveEncashmentAmount; }
        public void setLeaveEncashmentAmount(BigDecimal leaveEncashmentAmount) { this.leaveEncashmentAmount = leaveEncashmentAmount; }
    }

    public static class AssetSnapshot {
        private BigDecimal deductionAmount;
        private Integer unreturnedCount;
        private String clearanceReference;

        public AssetSnapshot() {}

        public BigDecimal getDeductionAmount() { return deductionAmount; }
        public void setDeductionAmount(BigDecimal deductionAmount) { this.deductionAmount = deductionAmount; }
        public Integer getUnreturnedCount() { return unreturnedCount; }
        public void setUnreturnedCount(Integer unreturnedCount) { this.unreturnedCount = unreturnedCount; }
        public String getClearanceReference() { return clearanceReference; }
        public void setClearanceReference(String clearanceReference) { this.clearanceReference = clearanceReference; }
    }

    public static class NoticeSnapshot {
        private Integer requiredNoticeDays;
        private Integer servedNoticeDays;
        private Integer shortfallDays;
        private BigDecimal noticeRecoveryAmount;

        public NoticeSnapshot() {}

        public Integer getRequiredNoticeDays() { return requiredNoticeDays; }
        public void setRequiredNoticeDays(Integer requiredNoticeDays) { this.requiredNoticeDays = requiredNoticeDays; }
        public Integer getServedNoticeDays() { return servedNoticeDays; }
        public void setServedNoticeDays(Integer servedNoticeDays) { this.servedNoticeDays = servedNoticeDays; }
        public Integer getShortfallDays() { return shortfallDays; }
        public void setShortfallDays(Integer shortfallDays) { this.shortfallDays = shortfallDays; }
        public BigDecimal getNoticeRecoveryAmount() { return noticeRecoveryAmount; }
        public void setNoticeRecoveryAmount(BigDecimal noticeRecoveryAmount) { this.noticeRecoveryAmount = noticeRecoveryAmount; }
    }

    public static class VariablePaySnapshot {
        private BigDecimal bonus;
        private BigDecimal incentives;
        private BigDecimal overtime;

        public VariablePaySnapshot() {}

        public BigDecimal getBonus() { return bonus; }
        public void setBonus(BigDecimal bonus) { this.bonus = bonus; }
        public BigDecimal getIncentives() { return incentives; }
        public void setIncentives(BigDecimal incentives) { this.incentives = incentives; }
        public BigDecimal getOvertime() { return overtime; }
        public void setOvertime(BigDecimal overtime) { this.overtime = overtime; }
    }

    public static class ExpenseSnapshot {
        private BigDecimal pendingReimbursements;

        public ExpenseSnapshot() {}

        public BigDecimal getPendingReimbursements() { return pendingReimbursements; }
        public void setPendingReimbursements(BigDecimal pendingReimbursements) { this.pendingReimbursements = pendingReimbursements; }
    }

    public static class DeductionSnapshot {
        private BigDecimal loanRecovery;
        private BigDecimal taxDeduction;
        private BigDecimal otherDeductions;

        public DeductionSnapshot() {}

        public BigDecimal getLoanRecovery() { return loanRecovery; }
        public void setLoanRecovery(BigDecimal loanRecovery) { this.loanRecovery = loanRecovery; }
        public BigDecimal getTaxDeduction() { return taxDeduction; }
        public void setTaxDeduction(BigDecimal taxDeduction) { this.taxDeduction = taxDeduction; }
        public BigDecimal getOtherDeductions() { return otherDeductions; }
        public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }
    }

    private SalarySnapshot salary;
    private LeaveSnapshot leave;
    private AssetSnapshot assets;
    private NoticeSnapshot notice;
    private VariablePaySnapshot variablePay;
    private ExpenseSnapshot expenses;
    private DeductionSnapshot deductions;
    private Map<String, Object> sourceReferences;

    public FnfSnapshotDto() {}

    public SalarySnapshot getSalary() { return salary; }
    public void setSalary(SalarySnapshot salary) { this.salary = salary; }
    public LeaveSnapshot getLeave() { return leave; }
    public void setLeave(LeaveSnapshot leave) { this.leave = leave; }
    public AssetSnapshot getAssets() { return assets; }
    public void setAssets(AssetSnapshot assets) { this.assets = assets; }
    public NoticeSnapshot getNotice() { return notice; }
    public void setNotice(NoticeSnapshot notice) { this.notice = notice; }
    public VariablePaySnapshot getVariablePay() { return variablePay; }
    public void setVariablePay(VariablePaySnapshot variablePay) { this.variablePay = variablePay; }
    public ExpenseSnapshot getExpenses() { return expenses; }
    public void setExpenses(ExpenseSnapshot expenses) { this.expenses = expenses; }
    public DeductionSnapshot getDeductions() { return deductions; }
    public void setDeductions(DeductionSnapshot deductions) { this.deductions = deductions; }
    public Map<String, Object> getSourceReferences() { return sourceReferences; }
    public void setSourceReferences(Map<String, Object> sourceReferences) { this.sourceReferences = sourceReferences; }
}
