package com.example.ems.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public class SettlementReviewResponse {
    private Long settlementId;
    private EmployeeInfo employee;
    private List<LineItem> earnings;
    private List<LineItem> deductions;
    private BigDecimal grossAmount;
    private BigDecimal deductionAmount;
    private BigDecimal netAmount;
    private String status;

    public static class EmployeeInfo {
        private Long id;
        private String name;
        private String department;

        public EmployeeInfo() {}

        public EmployeeInfo(Long id, String name, String department) {
            this.id = id;
            this.name = name;
            this.department = department;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
    }

    public SettlementReviewResponse() {}

    public SettlementReviewResponse(Long settlementId, EmployeeInfo employee, List<LineItem> earnings, List<LineItem> deductions, BigDecimal grossAmount, BigDecimal deductionAmount, BigDecimal netAmount, String status) {
        this.settlementId = settlementId;
        this.employee = employee;
        this.earnings = earnings;
        this.deductions = deductions;
        this.grossAmount = grossAmount;
        this.deductionAmount = deductionAmount;
        this.netAmount = netAmount;
        this.status = status;
    }

    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long settlementId) { this.settlementId = settlementId; }

    public EmployeeInfo getEmployee() { return employee; }
    public void setEmployee(EmployeeInfo employee) { this.employee = employee; }

    public List<LineItem> getEarnings() { return earnings; }
    public void setEarnings(List<LineItem> earnings) { this.earnings = earnings; }

    public List<LineItem> getDeductions() { return deductions; }
    public void setDeductions(List<LineItem> deductions) { this.deductions = deductions; }

    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }

    public BigDecimal getDeductionAmount() { return deductionAmount; }
    public void setDeductionAmount(BigDecimal deductionAmount) { this.deductionAmount = deductionAmount; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
