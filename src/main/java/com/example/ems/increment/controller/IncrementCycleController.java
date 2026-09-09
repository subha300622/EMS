package com.example.ems.increment.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.increment.dto.*;
import com.example.ems.increment.service.IncrementCycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/increment/cycles")
@CrossOrigin("*")
@Tag(name = "Increment Cycle APIs")
public class IncrementCycleController {

    @Autowired
    private IncrementCycleService cycleService;

    @Operation(summary = "Create Increment Cycle")
    @PostMapping
    public ResponseEntity<ApiResponse<IncrementCycleResponse>> createCycle(@Valid @RequestBody CreateIncrementCycleRequest request) {
        IncrementCycleResponse response = cycleService.createCycle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Increment cycle created successfully", response));
    }

    @Operation(summary = "Get All Increment Cycles")
    @GetMapping
    public ResponseEntity<ApiResponse<List<IncrementCycleResponse>>> getAllCycles() {
        List<IncrementCycleResponse> responses = cycleService.getAllCycles();
        return ResponseEntity.ok(ApiResponse.success("Increment cycles retrieved successfully", responses));
    }

    @Operation(summary = "Get Increment Cycle Details")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncrementCycleResponse>> getCycleById(@PathVariable Long id) {
        IncrementCycleResponse response = cycleService.getCycleById(id);
        return ResponseEntity.ok(ApiResponse.success("Increment cycle details retrieved successfully", response));
    }

    @Operation(summary = "Open Increment Cycle")
    @PostMapping("/{cycleId}/open")
    public ResponseEntity<ApiResponse<IncrementCycleResponse>> openCycle(@PathVariable Long cycleId) {
        IncrementCycleResponse response = cycleService.openCycle(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Increment cycle opened successfully", response));
    }

    @Operation(summary = "Import Completed Appraisals into Increment Cycle")
    @PostMapping("/{cycleId}/import-appraisals")
    public ResponseEntity<ApiResponse<ImportAppraisalsResponse>> importAppraisals(
            @PathVariable Long cycleId,
            @Valid @RequestBody ImportAppraisalsRequest request) {
        ImportAppraisalsResponse response = cycleService.importCompletedAppraisals(cycleId, request);
        return ResponseEntity.ok(ApiResponse.success("Completed appraisals imported successfully", response));
    }

    @Operation(summary = "Check and Calculate Employee Increment Eligibility")
    @PostMapping("/{cycleId}/employees/{employeeId}/eligibility")
    public ResponseEntity<ApiResponse<EligibilityEvaluationResponse>> checkEligibility(
            @PathVariable Long cycleId,
            @PathVariable Long employeeId,
            @Valid @RequestBody CheckEligibilityRequest request) {
        EligibilityEvaluationResponse response = cycleService.calculateEmployeeEligibility(cycleId, employeeId, request);
        return ResponseEntity.ok(ApiResponse.success("Eligibility calculation completed successfully", response));
    }
}
