package com.example.ems.appraisal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppraisalCurrentStageResponseDto {
    private Long appraisalId;
    private Long employeeId;
    private String employeeName;
    private Long cycleId;
    private String cycleName;
    private String appraisalStatus;
    private Integer currentStageOrder;
    private String currentStageName;
    private String requiredPermission;
    private boolean isRequired;
    private Double weightage;
    private boolean canReview;
    private boolean isCompleted;
    private List<ReviewStageDto> completedReviews;
}
