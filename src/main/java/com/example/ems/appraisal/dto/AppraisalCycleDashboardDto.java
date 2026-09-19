package com.example.ems.appraisal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppraisalCycleDashboardDto {
    private Long cycleId;
    private String cycleName;
    private String cycleStatus;
    private int totalAppraisals;
    private int completedCount;
    private int publishedCount;
    private double completionPercentage;
    private Double averageRating;
    private Map<String, Long> statusBreakdown;
    private Map<String, Long> stageBreakdown;
    private Map<String, Long> categoryDistribution;
}
