package com.example.ems.appraisal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppraisalEmployeeDashboardDto {
    private Long employeeId;
    private String employeeName;
    private Long currentAppraisalId;
    private String currentAppraisalStatus;
    private Integer currentStageOrder;
    private String currentStageName;
    private Double selfRating;
    private Double finalRating;
    private String performanceCategory;
    private LocalDateTime publishedAt;
    private Double approvedIncrementPercentage;
    private Double approvedBonusPercentage;
    private String incrementStatus;
    private int totalHistoricalAppraisals;
}
