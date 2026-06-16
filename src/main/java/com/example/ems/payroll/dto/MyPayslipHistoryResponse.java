package com.example.ems.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MyPayslipHistoryResponse {

    private List<PayslipItem> data;
    private PaginationInfo pagination;

    public MyPayslipHistoryResponse() {}

    public MyPayslipHistoryResponse(List<PayslipItem> data, PaginationInfo pagination) {
        this.data = data;
        this.pagination = pagination;
    }

    public List<PayslipItem> getData() { return data; }
    public void setData(List<PayslipItem> data) { this.data = data; }

    public PaginationInfo getPagination() { return pagination; }
    public void setPagination(PaginationInfo pagination) { this.pagination = pagination; }

    public static class PayslipItem {
        private Long payslipId;
        private String payslipNumber;
        private String payPeriod;
        private BigDecimal grossSalary;
        private BigDecimal deductions;
        private BigDecimal netSalary;
        private LocalDate paymentDate;
        private String status;
        private ActionInfo actions;

        public PayslipItem() {}

        public PayslipItem(Long payslipId, String payslipNumber, String payPeriod, BigDecimal grossSalary, BigDecimal deductions, BigDecimal netSalary, LocalDate paymentDate, String status, ActionInfo actions) {
            this.payslipId = payslipId;
            this.payslipNumber = payslipNumber;
            this.payPeriod = payPeriod;
            this.grossSalary = grossSalary;
            this.deductions = deductions;
            this.netSalary = netSalary;
            this.paymentDate = paymentDate;
            this.status = status;
            this.actions = actions;
        }

        public Long getPayslipId() { return payslipId; }
        public void setPayslipId(Long payslipId) { this.payslipId = payslipId; }

        public String getPayslipNumber() { return payslipNumber; }
        public void setPayslipNumber(String payslipNumber) { this.payslipNumber = payslipNumber; }

        public String getPayPeriod() { return payPeriod; }
        public void setPayPeriod(String payPeriod) { this.payPeriod = payPeriod; }

        public BigDecimal getGrossSalary() { return grossSalary; }
        public void setGrossSalary(BigDecimal grossSalary) { this.grossSalary = grossSalary; }

        public BigDecimal getDeductions() { return deductions; }
        public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }

        public BigDecimal getNetSalary() { return netSalary; }
        public void setNetSalary(BigDecimal netSalary) { this.netSalary = netSalary; }

        public LocalDate getPaymentDate() { return paymentDate; }
        public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public ActionInfo getActions() { return actions; }
        public void setActions(ActionInfo actions) { this.actions = actions; }
    }

    public static class ActionInfo {
        private boolean canView;
        private boolean canDownload;

        public ActionInfo() {}

        public ActionInfo(boolean canView, boolean canDownload) {
            this.canView = canView;
            this.canDownload = canDownload;
        }

        public boolean isCanView() { return canView; }
        public void setCanView(boolean canView) { this.canView = canView; }

        public boolean isCanDownload() { return canDownload; }
        public void setCanDownload(boolean canDownload) { this.canDownload = canDownload; }
    }

    public static class PaginationInfo {
        private int page;
        private int size;
        private long totalRecords;
        private int totalPages;

        public PaginationInfo() {}

        public PaginationInfo(int page, int size, long totalRecords, int totalPages) {
            this.page = page;
            this.size = size;
            this.totalRecords = totalRecords;
            this.totalPages = totalPages;
        }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public long getTotalRecords() { return totalRecords; }
        public void setTotalRecords(long totalRecords) { this.totalRecords = totalRecords; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
}
