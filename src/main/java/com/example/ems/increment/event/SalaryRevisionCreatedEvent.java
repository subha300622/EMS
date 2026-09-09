package com.example.ems.increment.event;

import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalaryRevisionCreatedEvent extends ApplicationEvent {

    private final Long organizationId;
    private final Long employeeId;
    private final Long recommendationId;
    private final Long salaryRevisionId;
    private final BigDecimal previousSalary;
    private final BigDecimal newSalary;
    private final BigDecimal incrementPercentage;
    private final BigDecimal incrementAmount;
    private final LocalDate effectiveDate;

    public SalaryRevisionCreatedEvent(
            Object source,
            Long organizationId,
            Long employeeId,
            Long recommendationId,
            Long salaryRevisionId,
            BigDecimal previousSalary,
            BigDecimal newSalary,
            BigDecimal incrementPercentage,
            BigDecimal incrementAmount,
            LocalDate effectiveDate
    ) {
        super(source);
        this.organizationId = organizationId;
        this.employeeId = employeeId;
        this.recommendationId = recommendationId;
        this.salaryRevisionId = salaryRevisionId;
        this.previousSalary = previousSalary;
        this.newSalary = newSalary;
        this.incrementPercentage = incrementPercentage;
        this.incrementAmount = incrementAmount;
        this.effectiveDate = effectiveDate;
    }

    public Long getOrganizationId() { return organizationId; }
    public Long getEmployeeId() { return employeeId; }
    public Long getRecommendationId() { return recommendationId; }
    public Long getSalaryRevisionId() { return salaryRevisionId; }
    public BigDecimal getPreviousSalary() { return previousSalary; }
    public BigDecimal getNewSalary() { return newSalary; }
    public BigDecimal getIncrementPercentage() { return incrementPercentage; }
    public BigDecimal getIncrementAmount() { return incrementAmount; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
}
