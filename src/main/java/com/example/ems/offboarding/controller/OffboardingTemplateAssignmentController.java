package com.example.ems.offboarding.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.offboarding.dto.EmployeeTemplateAssignmentRequest;
import com.example.ems.offboarding.dto.EmployeeTemplateAssignmentResponse;
import com.example.ems.offboarding.service.OffboardingTemplateAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/offboarding/template-assignments", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Offboarding Template Assignments", description = "APIs for individual employee offboarding template assignments and overrides")
public class OffboardingTemplateAssignmentController {

    private final OffboardingTemplateAssignmentService assignmentService;

    @Autowired
    public OffboardingTemplateAssignmentController(OffboardingTemplateAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @Operation(summary = "Assign Template to Employee", description = "Assigns an individual offboarding template override for a specific employee and exit type.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template assigned successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EmployeeTemplateAssignmentResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or inactive template",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee or template not found in organization",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/employees/{employeeId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('offboarding.template.manage')")
    public ResponseEntity<ApiResponse<EmployeeTemplateAssignmentResponse>> assignTemplate(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeTemplateAssignmentRequest request) {
        EmployeeTemplateAssignmentResponse response = assignmentService.assignTemplateToEmployee(employeeId, request);
        return ResponseEntity.ok(ApiResponse.success("Template assigned to employee successfully", response));
    }

    @Operation(summary = "Get Employee Template Assignments", description = "Retrieves all individual template assignments configured for the employee.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assignments retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = EmployeeTemplateAssignmentResponse.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/employees/{employeeId}")
    @PreAuthorize("hasAuthority('offboarding.template.manage')")
    public ResponseEntity<ApiResponse<List<EmployeeTemplateAssignmentResponse>>> getAssignments(
            @PathVariable Long employeeId) {
        List<EmployeeTemplateAssignmentResponse> response = assignmentService.getAssignments(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee template assignments retrieved successfully", response));
    }

    @Operation(summary = "Remove Employee Template Assignment", description = "Removes individual template assignment override, reverting employee to automatic matching.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assignment removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/employees/{employeeId}")
    @PreAuthorize("hasAuthority('offboarding.template.manage')")
    public ResponseEntity<ApiResponse<Void>> removeAssignment(
            @PathVariable Long employeeId,
            @Parameter(description = "Optional exit type to remove (e.g. RESIGNATION). If omitted, all assignments for employee are removed.")
            @RequestParam(required = false) String exitType) {
        assignmentService.removeAssignment(employeeId, exitType);
        return ResponseEntity.ok(ApiResponse.success("Template assignment removed successfully", null));
    }
}
