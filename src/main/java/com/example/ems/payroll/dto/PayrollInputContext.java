package com.example.ems.payroll.dto;

import com.example.ems.leave.dto.LeavePeriodSummaryDto;

import java.time.LocalDate;

/**
 * Unified context consolidating all period inputs across upstream modules
 * (Salary DAG, Attendance & Working Days, Leave LOP & Encashment, Overtime, Incentives, Bonuses, Reimbursements).
 */
public class PayrollInputContext {

    private Long organizationId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private int workingDays;

    private SalaryCalculationResponse salaryCalculation;
    private LeavePeriodSummaryDto leaveSummary;
    private OvertimePeriodSummaryDto overtimeSummary;
    private IncentivePeriodSummaryDto incentiveSummary;
    private BonusPeriodSummaryDto bonusSummary;
    private ReimbursementPeriodSummaryDto reimbursementSummary;

    public PayrollInputContext() {}

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public int getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(int workingDays) {
        this.workingDays = workingDays;
    }

    public SalaryCalculationResponse getSalaryCalculation() {
        return salaryCalculation;
    }

    public void setSalaryCalculation(SalaryCalculationResponse salaryCalculation) {
        this.salaryCalculation = salaryCalculation;
    }

    public LeavePeriodSummaryDto getLeaveSummary() {
        return leaveSummary;
    }

    public void setLeaveSummary(LeavePeriodSummaryDto leaveSummary) {
        this.leaveSummary = leaveSummary;
    }

    public OvertimePeriodSummaryDto getOvertimeSummary() {
        return overtimeSummary;
    }

    public void setOvertimeSummary(OvertimePeriodSummaryDto overtimeSummary) {
        this.overtimeSummary = overtimeSummary;
    }

    public IncentivePeriodSummaryDto getIncentiveSummary() {
        return incentiveSummary;
    }

    public void setIncentiveSummary(IncentivePeriodSummaryDto incentiveSummary) {
        this.incentiveSummary = incentiveSummary;
    }

    public BonusPeriodSummaryDto getBonusSummary() {
        return bonusSummary;
    }

    public void setBonusSummary(BonusPeriodSummaryDto bonusSummary) {
        this.bonusSummary = bonusSummary;
    }

    public ReimbursementPeriodSummaryDto getReimbursementSummary() {
        return reimbursementSummary;
    }

    public void setReimbursementSummary(ReimbursementPeriodSummaryDto reimbursementSummary) {
        this.reimbursementSummary = reimbursementSummary;
    }
}
