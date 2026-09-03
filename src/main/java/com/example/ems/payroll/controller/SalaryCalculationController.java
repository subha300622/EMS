package com.example.ems.payroll.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.payroll.dto.SalaryCalculationPreviewRequest;
import com.example.ems.payroll.dto.SalaryCalculationResponse;
import com.example.ems.payroll.service.SalaryCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}/salary-calculations")
@CrossOrigin("*")
@Tag(name = "Salary Calculation Engine", description = "Dynamic Employee Salary Evaluation, Previews and What-If Simulations")
public class SalaryCalculationController {

    @Autowired
    private SalaryCalculationService salaryCalculationService;

    @Operation(summary = "Calculate Current Salary", description = "Calculates the employee's current salary breakdown based on their active assignment as of today.")
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<SalaryCalculationResponse>> getCurrentSalary(
            @PathVariable Long employeeId) {
        SalaryCalculationResponse response = salaryCalculationService.calculateCurrentSalary(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Current salary calculated successfully", response));
    }

    @Operation(summary = "Preview Salary Calculation", description = "Simulates an employee's salary breakdown on any effective date with optional ad-hoc component value overrides without mutating state.")
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<SalaryCalculationResponse>> previewSalaryCalculation(
            @PathVariable Long employeeId,
            @RequestBody(required = false) SalaryCalculationPreviewRequest request) {
        SalaryCalculationResponse response = salaryCalculationService.previewSalaryCalculation(employeeId, request);
        return ResponseEntity.ok(ApiResponse.success("Salary calculation preview generated successfully", response));
    }
}
