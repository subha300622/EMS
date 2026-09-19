package com.example.ems.increment.dto;

import com.example.ems.increment.entity.IncrementCycleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Increment Cycle Response")
public class IncrementCycleResponse {

    @Schema(description = "Cycle ID", example = "1")
    private Long id;
    @Schema(description = "Cycle Name", example = "FY 2026-27 Annual Increment Cycle")
    private String name;
    @Schema(description = "Financial Year", example = "2026-2027")
    private String financialYear;
    @Schema(description = "Start Date", example = "2026-04-01")
    private LocalDate startDate;
    @Schema(description = "End Date", example = "2027-03-31")
    private LocalDate endDate;
    @Schema(description = "Effective Date", example = "2026-07-01")
    private LocalDate effectiveDate;
    @Schema(description = "Policy ID", example = "1")
    private Long policyId;
    @Schema(description = "Policy Name", example = "Standard Annual Increment Policy")
    private String policyName;
    @Schema(description = "Policy Version", example = "1")
    private Integer policyVersion;
    @Schema(description = "Budget Limit", example = "5000000.00")
    private BigDecimal budgetLimit;
    @Schema(description = "Allocated Budget", example = "3500000.00")
    private BigDecimal allocatedBudget;
    @Schema(description = "Cycle Status", example = "OPEN")
    private IncrementCycleStatus status;
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFinancialYear() { return financialYear; }
    public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }

    public Integer getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(Integer policyVersion) { this.policyVersion = policyVersion; }

    public BigDecimal getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(BigDecimal budgetLimit) { this.budgetLimit = budgetLimit; }

    public BigDecimal getAllocatedBudget() { return allocatedBudget; }
    public void setAllocatedBudget(BigDecimal allocatedBudget) { this.allocatedBudget = allocatedBudget; }

    public IncrementCycleStatus getStatus() { return status; }
    public void setStatus(IncrementCycleStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
