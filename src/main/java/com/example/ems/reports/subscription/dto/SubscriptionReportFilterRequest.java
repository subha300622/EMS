package com.example.ems.reports.subscription.dto;

import java.time.LocalDate;

public class SubscriptionReportFilterRequest {
    private String search;
    private String status;
    private String plan;
    private String billingCycle;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "organizationId";
    private String direction = "asc";

    // Getters and Setters
    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
}
