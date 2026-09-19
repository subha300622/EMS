package com.example.ems.onboarding.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.onboarding.dto.OnboardingDocumentResponse;
import com.example.ems.onboarding.dto.selfservice.OnboardingProgressResponse;
import com.example.ems.onboarding.dto.selfservice.OnboardingSelfServiceResponse;
import com.example.ems.onboarding.dto.task.OnboardingTaskListResponse;
import com.example.ems.onboarding.service.OnboardingSelfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/onboarding/me")
@CrossOrigin("*")
@Tag(name = "Employee Self Service - Onboarding")
public class OnboardingSelfServiceController {

    @Autowired
    private OnboardingSelfService selfService;

    @GetMapping
    @Operation(summary = "Get Authenticated Candidate Onboarding Profile")
    public ResponseEntity<ApiResponse<OnboardingSelfServiceResponse>> getMyOnboarding() {
        OnboardingSelfServiceResponse response = selfService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success("Self-service onboarding profile retrieved successfully", response));
    }

    @GetMapping("/tasks")
    @Operation(summary = "Get Authenticated Candidate Onboarding Tasks")
    public ResponseEntity<ApiResponse<OnboardingTaskListResponse>> getMyTasks() {
        OnboardingTaskListResponse response = selfService.getMyTasks();
        return ResponseEntity.ok(ApiResponse.success("Candidate tasks retrieved successfully", response));
    }

    @GetMapping("/documents")
    @Operation(summary = "Get Authenticated Candidate Onboarding Documents")
    public ResponseEntity<ApiResponse<List<OnboardingDocumentResponse>>> getMyDocuments() {
        List<OnboardingDocumentResponse> response = selfService.getMyDocuments();
        return ResponseEntity.ok(ApiResponse.success("Candidate documents retrieved successfully", response));
    }

    @GetMapping("/progress")
    @Operation(summary = "Get Authenticated Candidate Onboarding Progress")
    public ResponseEntity<ApiResponse<OnboardingProgressResponse>> getMyProgress() {
        OnboardingProgressResponse response = selfService.getMyProgress();
        return ResponseEntity.ok(ApiResponse.success("Candidate progress retrieved successfully", response));
    }
}
