package com.example.ems.appraisal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateAppraisalCycleDto {

    @NotBlank(message = "name is required")
    private String name;

    private String type = "ANNUAL";

    @NotNull(message = "startDate is required")
    private LocalDate startDate;

    @NotNull(message = "endDate is required")
    private LocalDate endDate;

    private CycleEligibilityCriteriaDto eligibleEmployeeCriteria;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public CycleEligibilityCriteriaDto getEligibleEmployeeCriteria() { return eligibleEmployeeCriteria; }
    public void setEligibleEmployeeCriteria(CycleEligibilityCriteriaDto eligibleEmployeeCriteria) { this.eligibleEmployeeCriteria = eligibleEmployeeCriteria; }
}
