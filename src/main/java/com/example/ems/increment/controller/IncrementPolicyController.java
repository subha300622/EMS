package com.example.ems.increment.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.increment.dto.CreateIncrementPolicyRequest;
import com.example.ems.increment.dto.IncrementPolicyResponse;
import com.example.ems.increment.dto.UpdateIncrementPolicyRequest;
import com.example.ems.increment.service.IncrementPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/increment/policies")
@CrossOrigin("*")
@Tag(name = "Increment Policy APIs")
public class IncrementPolicyController {

    @Autowired
    private IncrementPolicyService policyService;

    @Operation(summary = "Create Increment Policy")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Increment policy created successfully",
                    content = @Content(schema = @Schema(implementation = IncrementPolicyResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> createPolicy(@Valid @RequestBody CreateIncrementPolicyRequest request) {
        IncrementPolicyResponse response = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Increment policy created successfully", response));
    }

    @Operation(summary = "Update Increment Policy")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Increment policy updated successfully",
                    content = @Content(schema = @Schema(implementation = IncrementPolicyResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Increment policy not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIncrementPolicyRequest request) {
        IncrementPolicyResponse response = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Increment policy updated successfully", response));
    }

    @Operation(summary = "Get Current Active Increment Policy")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current active increment policy retrieved successfully",
                    content = @Content(schema = @Schema(implementation = IncrementPolicyResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No active increment policy is configured",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentPolicy() {
        try {
            IncrementPolicyResponse response = policyService.getCurrentActivePolicy();
            return ResponseEntity.ok(ApiResponse.success("Current active increment policy retrieved successfully", response));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("No active increment policy is configured.", "INCREMENT_POLICY_NOT_CONFIGURED"));
        }
    }

    @Operation(summary = "Get All Increment Policies")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Increment policies retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = IncrementPolicyResponse.class))))
    })
    @GetMapping
    public ResponseEntity<?> getAllPolicies() {
        List<IncrementPolicyResponse> responses = policyService.getAllPolicies();
        return ResponseEntity.ok(ApiResponse.success("Increment policies retrieved successfully", responses));
    }
}
