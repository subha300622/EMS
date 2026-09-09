package com.example.ems.increment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Employee Increment History Response")
public class EmployeeIncrementHistoryResponse {

    @Schema(description = "Employee ID", example = "1")
    private Long employeeId;
    @Schema(description = "List of historical increment items")
    private List<HistoryItemDto> history;

    public static class HistoryItemDto {
        private Long recommendationId;
        private LocalDate incrementDate;
        private BigDecimal previousSalary;
        private BigDecimal incrementPercentage;
        private BigDecimal incrementAmount;
        private BigDecimal revisedSalary;
        private String status;
        private String cycleName;

        public Long getRecommendationId() { return recommendationId; }
        public void setRecommendationId(Long recommendationId) { this.recommendationId = recommendationId; }

        public LocalDate getIncrementDate() { return incrementDate; }
        public void setIncrementDate(LocalDate incrementDate) { this.incrementDate = incrementDate; }

        public BigDecimal getPreviousSalary() { return previousSalary; }
        public void setPreviousSalary(BigDecimal previousSalary) { this.previousSalary = previousSalary; }

        public BigDecimal getIncrementPercentage() { return incrementPercentage; }
        public void setIncrementPercentage(BigDecimal incrementPercentage) { this.incrementPercentage = incrementPercentage; }

        public BigDecimal getIncrementAmount() { return incrementAmount; }
        public void setIncrementAmount(BigDecimal incrementAmount) { this.incrementAmount = incrementAmount; }

        public BigDecimal getRevisedSalary() { return revisedSalary; }
        public void setRevisedSalary(BigDecimal revisedSalary) { this.revisedSalary = revisedSalary; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCycleName() { return cycleName; }
        public void setCycleName(String cycleName) { this.cycleName = cycleName; }
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public List<HistoryItemDto> getHistory() { return history; }
    public void setHistory(List<HistoryItemDto> history) { this.history = history; }
}
