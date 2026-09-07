package com.example.ems.appraisal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppraisalOrganizationDashboardDto {
    private int totalCycles;
    private int openCycles;
    private Long activeCycleId;
    private String activeCycleName;
    private int totalAppraisals;
    private Map<String, Long> statusBreakdown;
    private Double averageRating;
    private Map<String, Long> performanceCategoryDistribution;
    private int totalIncrementsApproved;
    private int totalIncrementsApplied;
}
