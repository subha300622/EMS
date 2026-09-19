package com.example.ems.increment.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.increment.dto.*;
import com.example.ems.increment.entity.IncrementRecommendationStatus;
import com.example.ems.increment.service.IncrementApprovalService;
import com.example.ems.increment.service.IncrementImplementationService;
import com.example.ems.increment.service.IncrementLetterService;
import com.example.ems.increment.service.IncrementRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/increment/recommendations")
@CrossOrigin("*")
@Tag(name = "Increment Recommendation APIs")
public class IncrementRecommendationController {

    @Autowired
    private IncrementRecommendationService recommendationService;

    @Autowired
    private IncrementApprovalService approvalService;

    @Autowired
    private IncrementImplementationService implementationService;

    @Autowired
    private IncrementLetterService letterService;

    @Operation(summary = "Create Increment Recommendation")
    @PostMapping
    public ResponseEntity<ApiResponse<IncrementRecommendationResponse>> createRecommendation(@Valid @RequestBody CreateRecommendationRequest request) {
        IncrementRecommendationResponse response = recommendationService.createRecommendation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Increment recommendation created successfully", response));
    }

    @Operation(summary = "List and Filter Increment Recommendations")
    @GetMapping
    public ResponseEntity<ApiResponse<List<IncrementRecommendationResponse>>> searchRecommendations(
            @RequestParam(required = false) Long cycleId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) IncrementRecommendationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDateTo) {
        List<IncrementRecommendationResponse> responses = recommendationService.searchRecommendations(
                cycleId, employeeId, status, effectiveDateFrom, effectiveDateTo
        );
        return ResponseEntity.ok(ApiResponse.success("Increment recommendations retrieved successfully", responses));
    }

    @Operation(summary = "Get Increment Recommendation by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncrementRecommendationResponse>> getRecommendationById(@PathVariable Long id) {
        IncrementRecommendationResponse response = recommendationService.getRecommendationById(id);
        return ResponseEntity.ok(ApiResponse.success("Increment recommendation retrieved successfully", response));
    }

    @Operation(summary = "Revise Increment Recommendation")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<IncrementRecommendationResponse>> updateRecommendation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecommendationRequest request) {
        IncrementRecommendationResponse response = recommendationService.updateRecommendation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Increment recommendation updated successfully", response));
    }

    @Operation(summary = "Submit Increment Recommendation for Approval")
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<SubmitApprovalResponse>> submitForApproval(@PathVariable Long id) {
        SubmitApprovalResponse response = approvalService.submitForApproval(id);
        return ResponseEntity.ok(ApiResponse.success("Increment recommendation submitted for approval successfully", response));
    }

    @Operation(summary = "Implement Approved Increment and Apply Salary Revision")
    @PostMapping("/{id}/implement")
    public ResponseEntity<ApiResponse<IncrementRecommendationResponse>> implementIncrement(@PathVariable Long id) {
        IncrementRecommendationResponse response = implementationService.implementIncrement(id);
        return ResponseEntity.ok(ApiResponse.success("Salary revision implemented successfully", response));
    }

    @Operation(summary = "Generate Increment Letter")
    @PostMapping("/{id}/letter")
    public ResponseEntity<ApiResponse<IncrementLetterResponse>> generateLetter(@PathVariable Long id) {
        IncrementLetterResponse response = letterService.generateLetter(id);
        return ResponseEntity.ok(ApiResponse.success("Increment letter generated successfully", response));
    }
}
