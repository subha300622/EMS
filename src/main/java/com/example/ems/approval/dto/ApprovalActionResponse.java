package com.example.ems.approval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalActionResponse {
    private String resourceId;
    private String approvalId;
    private String status;
    private String action;
    private String actedBy;
    private LocalDateTime actedAt;
    private String nextStep;
}
