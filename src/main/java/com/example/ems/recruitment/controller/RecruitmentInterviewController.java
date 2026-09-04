package com.example.ems.recruitment.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.recruitment.dto.*;
import com.example.ems.recruitment.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recruitment")
public class RecruitmentInterviewController {

    @Autowired
    private InterviewService interviewService;

    @PostMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<ApiResponse<InterviewResponse>> scheduleInterview(
            @PathVariable Long applicationId,
            @Valid @RequestBody InterviewScheduleRequest request) {
        InterviewResponse response = interviewService.scheduleInterview(applicationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interview scheduled successfully", response));
    }

    @PatchMapping("/interviews/{interviewId}/complete")
    public ResponseEntity<ApiResponse<InterviewResponse>> completeInterview(@PathVariable Long interviewId) {
        InterviewResponse response = interviewService.completeInterview(interviewId);
        return ResponseEntity.ok(ApiResponse.success("Interview completed successfully", response));
    }

    @PostMapping("/interviews/{interviewId}/feedback")
    public ResponseEntity<ApiResponse<InterviewResponse>> submitFeedback(
            @PathVariable Long interviewId,
            @Valid @RequestBody InterviewFeedbackRequest request) {
        InterviewResponse response = interviewService.submitFeedback(interviewId, request);
        return ResponseEntity.ok(ApiResponse.success("Interview feedback submitted successfully", response));
    }

    @GetMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getInterviewsForApplication(@PathVariable Long applicationId) {
        List<InterviewResponse> interviews = interviewService.getInterviewsForApplication(applicationId);
        return ResponseEntity.ok(ApiResponse.success("Fetched interviews successfully", interviews));
    }
}
