package com.example.ems.payroll.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.payroll.dto.*;
import com.example.ems.payroll.service.EmployeeSalaryAssignmentService;
import com.example.ems.payroll.service.EmployeeSalaryComponentValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}/salary-assignments")
@CrossOrigin("*")
@Tag(name = "Employee Salary Assignments", description = "Management of Employee Structure Assignments, Timeline Transitions and Component Overrides")
public class EmployeeSalaryAssignmentController {

    @Autowired
    private EmployeeSalaryAssignmentService employeeSalaryAssignmentService;

    @Autowired
    private EmployeeSalaryComponentValueService employeeSalaryComponentValueService;

    // ── SALARY STRUCTURE ASSIGNMENT ──────────────────────────────────────────

    @Operation(summary = "Assign Salary Structure to Employee", description = "Assigns an ACTIVE salary structure to an employee with effective dates, auto-closing previous open assignment.")
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeSalaryAssignmentResponse>> createAssignment(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeSalaryAssignmentCreateRequest request) {
        EmployeeSalaryAssignmentResponse response = employeeSalaryAssignmentService.createAssignment(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Salary structure assigned to employee successfully", response));
    }

    @Operation(summary = "Get Current Salary Assignment", description = "Retrieves the active salary structure assignment for an employee as of today.")
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<EmployeeSalaryAssignmentResponse>> getCurrentAssignment(
            @PathVariable Long employeeId) {
        EmployeeSalaryAssignmentResponse response = employeeSalaryAssignmentService.getCurrentAssignment(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Current salary assignment retrieved successfully", response));
    }

    @Operation(summary = "Get Salary Assignment History", description = "Retrieves all historical salary structure assignments for an employee ordered by effective date desc.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeSalaryAssignmentResponse>>> getAssignmentHistory(
            @PathVariable Long employeeId) {
        List<EmployeeSalaryAssignmentResponse> history = employeeSalaryAssignmentService.getAssignmentHistory(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Salary assignment history retrieved successfully", history));
    }

    @Operation(summary = "Get Salary Assignment by ID", description = "Retrieves details of a specific salary assignment by ID.")
    @GetMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse<EmployeeSalaryAssignmentResponse>> getAssignmentById(
            @PathVariable Long employeeId,
            @PathVariable Long assignmentId) {
        EmployeeSalaryAssignmentResponse response = employeeSalaryAssignmentService.getAssignmentById(employeeId, assignmentId);
        return ResponseEntity.ok(ApiResponse.success("Salary assignment details retrieved successfully", response));
    }

    // ── EMPLOYEE-SPECIFIC COMPONENT VALUES & OVERRIDES ────────────────────────

    @Operation(summary = "Add Employee Component Value / Override", description = "Adds an employee-specific value or override for a component in the assigned structure.")
    @PostMapping("/{assignmentId}/components")
    public ResponseEntity<ApiResponse<EmployeeSalaryComponentValueResponse>> addComponentValue(
            @PathVariable Long employeeId,
            @PathVariable Long assignmentId,
            @Valid @RequestBody EmployeeSalaryComponentValueRequest request) {
        EmployeeSalaryComponentValueResponse response = employeeSalaryComponentValueService.addComponentValue(employeeId, assignmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee salary component value added successfully", response));
    }

    @Operation(summary = "Get Employee Component Values", description = "Retrieves all component values and overrides configured for a salary assignment.")
    @GetMapping("/{assignmentId}/components")
    public ResponseEntity<ApiResponse<List<EmployeeSalaryComponentValueResponse>>> getComponentValues(
            @PathVariable Long employeeId,
            @PathVariable Long assignmentId) {
        List<EmployeeSalaryComponentValueResponse> list = employeeSalaryComponentValueService.getComponentValues(employeeId, assignmentId);
        return ResponseEntity.ok(ApiResponse.success("Employee salary component values retrieved successfully", list));
    }

    @Operation(summary = "Update Employee Component Value / Override", description = "Updates an employee component value or override amount/percentage.")
    @PutMapping("/{assignmentId}/components/{valueId}")
    public ResponseEntity<ApiResponse<EmployeeSalaryComponentValueResponse>> updateComponentValue(
            @PathVariable Long employeeId,
            @PathVariable Long assignmentId,
            @PathVariable Long valueId,
            @Valid @RequestBody EmployeeSalaryComponentValueRequest request) {
        EmployeeSalaryComponentValueResponse response = employeeSalaryComponentValueService.updateComponentValue(employeeId, assignmentId, valueId, request);
        return ResponseEntity.ok(ApiResponse.success("Employee salary component value updated successfully", response));
    }

    @Operation(summary = "Remove Employee Component Override", description = "Deletes an employee-specific component override from a salary assignment.")
    @DeleteMapping("/{assignmentId}/components/{valueId}")
    public ResponseEntity<ApiResponse<Void>> removeComponentValue(
            @PathVariable Long employeeId,
            @PathVariable Long assignmentId,
            @PathVariable Long valueId) {
        employeeSalaryComponentValueService.removeComponentValue(employeeId, assignmentId, valueId);
        return ResponseEntity.ok(ApiResponse.success("Employee salary component override removed successfully", null));
    }
}
