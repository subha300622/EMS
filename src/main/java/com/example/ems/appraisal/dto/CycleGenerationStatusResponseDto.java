package com.example.ems.appraisal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleGenerationStatusResponseDto {
    private Long cycleId;
    private String cycleName;
    private String cycleStatus;
    private int totalEligibleEmployees;
    private int totalGeneratedAppraisals;
    private int totalPendingGeneration;
    private boolean isGenerationComplete;
    private LocalDateTime checkedAt;
}
