package com.example.ems.increment.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.increment.dto.EmployeeIncrementHistoryResponse;
import com.example.ems.increment.service.IncrementRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@CrossOrigin("*")
@Tag(name = "Employee Increment History APIs")
public class EmployeeIncrementHistoryController {

    private final IncrementRecommendationService recommendationService;

    @Autowired
    public EmployeeIncrementHistoryController(IncrementRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "Get Employee Increment History")
    @GetMapping("/{employeeId}/increment-history")
    public ResponseEntity<ApiResponse<EmployeeIncrementHistoryResponse>> getEmployeeIncrementHistory(@PathVariable Long employeeId) {
        EmployeeIncrementHistoryResponse response = recommendationService.getEmployeeIncrementHistory(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee increment history retrieved successfully", response));
    }
}
