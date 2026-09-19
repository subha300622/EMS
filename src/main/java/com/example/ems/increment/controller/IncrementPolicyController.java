package com.example.ems.increment.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.increment.dto.CreateIncrementPolicyRequest;
import com.example.ems.increment.dto.IncrementPolicyResponse;
import com.example.ems.increment.dto.UpdateIncrementPolicyRequest;
import com.example.ems.increment.service.IncrementPolicyService;
import io.swagger.v3.oas.annotations.Operation;
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
    @PostMapping
    public ResponseEntity<?> createPolicy(@Valid @RequestBody CreateIncrementPolicyRequest request) {
        IncrementPolicyResponse response = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Increment policy created successfully", response));
    }

    @Operation(summary = "Update Increment Policy")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIncrementPolicyRequest request) {
        IncrementPolicyResponse response = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Increment policy updated successfully", response));
    }

    @Operation(summary = "Get Current Active Increment Policy")
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
    @GetMapping
    public ResponseEntity<?> getAllPolicies() {
        List<IncrementPolicyResponse> responses = policyService.getAllPolicies();
        return ResponseEntity.ok(ApiResponse.success("Increment policies retrieved successfully", responses));
    }
}
