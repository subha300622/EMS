package com.example.ems.appraisal.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppraisalCycleDto {

    @Size(min = 2, max = 150, message = "Appraisal cycle name must be between 2 and 150 characters")
    private String name;

    private String type; // e.g., ANNUAL, PROBATION, MID_YEAR

    private LocalDate startDate;

    private LocalDate endDate;

    private CycleEligibilityCriteriaDto eligibleEmployeeCriteria;
}
