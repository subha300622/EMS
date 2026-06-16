package com.example.ems.asset.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MyAssetsDashboardResponse {

    private EmployeeInfo employee;
    private SummaryInfo summary;
    private LocalDateTime lastUpdatedAt;

    public MyAssetsDashboardResponse() {}

    public MyAssetsDashboardResponse(EmployeeInfo employee, SummaryInfo summary, LocalDateTime lastUpdatedAt) {
        this.employee = employee;
        this.summary = summary;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public EmployeeInfo getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeInfo employee) {
        this.employee = employee;
    }

    public SummaryInfo getSummary() {
        return summary;
    }

    public void setSummary(SummaryInfo summary) {
        this.summary = summary;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public static class EmployeeInfo {
        private Long employeeId;
        private String employeeCode;
        private String name;
        private String department;

        public EmployeeInfo() {}

        public EmployeeInfo(Long employeeId, String employeeCode, String name, String department) {
            this.employeeId = employeeId;
            this.employeeCode = employeeCode;
            this.name = name;
            this.department = department;
        }

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeCode() {
            return employeeCode;
        }

        public void setEmployeeCode(String employeeCode) {
            this.employeeCode = employeeCode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }
    }

    public static class SummaryInfo {
        private int assignedAssets;
        private int activeAssets;
        private BigDecimal totalAssetValue;
        private String currency;
        private int upcomingReturns;
        private int openIssueTickets;

        public SummaryInfo() {}

        public SummaryInfo(int assignedAssets, int activeAssets, BigDecimal totalAssetValue, String currency, int upcomingReturns, int openIssueTickets) {
            this.assignedAssets = assignedAssets;
            this.activeAssets = activeAssets;
            this.totalAssetValue = totalAssetValue;
            this.currency = currency;
            this.upcomingReturns = upcomingReturns;
            this.openIssueTickets = openIssueTickets;
        }

        public int getAssignedAssets() {
            return assignedAssets;
        }

        public void setAssignedAssets(int assignedAssets) {
            this.assignedAssets = assignedAssets;
        }

        public int getActiveAssets() {
            return activeAssets;
        }

        public void setActiveAssets(int activeAssets) {
            this.activeAssets = activeAssets;
        }

        public BigDecimal getTotalAssetValue() {
            return totalAssetValue;
        }

        public void setTotalAssetValue(BigDecimal totalAssetValue) {
            this.totalAssetValue = totalAssetValue;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public int getUpcomingReturns() {
            return upcomingReturns;
        }

        public void setUpcomingReturns(int upcomingReturns) {
            this.upcomingReturns = upcomingReturns;
        }

        public int getOpenIssueTickets() {
            return openIssueTickets;
        }

        public void setOpenIssueTickets(int openIssueTickets) {
            this.openIssueTickets = openIssueTickets;
        }
    }
}
