package com.example.ems.appraisal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AppraisalCycleResponseDto {
    private Long id;
    private String name;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private CycleEligibilityCriteriaDto eligibleEmployeeCriteria;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public CycleEligibilityCriteriaDto getEligibleEmployeeCriteria() { return eligibleEmployeeCriteria; }
    public void setEligibleEmployeeCriteria(CycleEligibilityCriteriaDto eligibleEmployeeCriteria) { this.eligibleEmployeeCriteria = eligibleEmployeeCriteria; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
