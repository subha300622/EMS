package com.example.ems.appraisal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleEligibilityPreviewResponseDto {

    private Long cycleId;
    private String cycleName;
    private int totalEmployees;
    private int eligibleCount;
    private int ineligibleCount;
    private List<EmployeeEligibilityDetailDto> eligibleEmployees = new ArrayList<>();
    private List<EmployeeEligibilityDetailDto> ineligibleEmployees = new ArrayList<>();
    private List<EmployeeEligibilityDetailDto> employees = new ArrayList<>();
}
