package com.example.ems.goal.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.goal.domain.GoalApprovalPolicy;
import com.example.ems.goal.dto.GoalApprovalPolicyRequest;
import com.example.ems.goal.service.GoalApprovalPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@Tag(name = "Goal Approval Policy Management", description = "Configurable Approval Policies for Goal Module Actions")
public class GoalApprovalPolicyController {

    @Autowired
    private GoalApprovalPolicyService policyService;

    @Operation(summary = "List Goal Approval Policies", description = "Retrieves active approval policies for Goal Module")
    @GetMapping({"/api/v1/goal-approval-policies", "/api/v1/approval-policies"})
    @PreAuthorize("hasAuthority('GOAL_CONFIG_VIEW') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> getPolicies(@RequestParam(value = "module", required = false) String module) {
        List<GoalApprovalPolicy> policies = policyService.getPolicies();
        return ResponseEntity.ok(ApiResponse.success("Goal approval policies retrieved successfully", policies));
    }

    @Operation(summary = "Create Goal Approval Policy", description = "Creates a new goal approval policy")
    @PostMapping({"/api/v1/goal-approval-policies", "/api/v1/approval-policies"})
    @PreAuthorize("hasAuthority('GOAL_CONFIG_EDIT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> createPolicy(@Valid @RequestBody GoalApprovalPolicyRequest request) {
        GoalApprovalPolicy policy = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goal approval policy created successfully", policy));
    }

    @Operation(summary = "Update Goal Approval Policy", description = "Updates an existing goal approval policy")
    @PutMapping({"/api/v1/goal-approval-policies/{policyId}", "/api/v1/approval-policies/{policyId}"})
    @PreAuthorize("hasAuthority('GOAL_CONFIG_EDIT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> updatePolicy(
            @PathVariable("policyId") Long policyId,
            @RequestBody GoalApprovalPolicyRequest request) {
        GoalApprovalPolicy policy = policyService.updatePolicy(policyId, request);
        return ResponseEntity.ok(ApiResponse.success("Goal approval policy updated successfully", policy));
    }

    @Operation(summary = "Delete Goal Approval Policy", description = "Deactivates a goal approval policy")
    @DeleteMapping({"/api/v1/goal-approval-policies/{policyId}", "/api/v1/approval-policies/{policyId}"})
    @PreAuthorize("hasAuthority('GOAL_CONFIG_EDIT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deletePolicy(@PathVariable("policyId") Long policyId) {
        policyService.deletePolicy(policyId);
        return ResponseEntity.ok(ApiResponse.success("Goal approval policy deleted successfully", null));
    }
}
