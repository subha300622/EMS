package com.example.ems.approval.controller;

import com.example.ems.approval.dto.*;
import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.ApprovalWorkflowDefinition;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.ApprovalWorkflowStep;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("workflowApprovalController")
@RequestMapping("/api/v1/approvals")
@CrossOrigin("*")
@Tag(name = "Approval Workflow Engine", description = "Generic Approval Workflow Engine APIs")
public class ApprovalController {

    @Autowired
    private ApprovalWorkflowEngineService approvalWorkflowEngineService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email).orElse(null);
            }
        }
        return null;
    }

    // ── 1. WORKFLOW CONFIGURATION APIS ──────────────────────────────────────────

    @Operation(summary = "Create Approval Workflow")
    @PostMapping("/workflows")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateApprovalWorkflowRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowDefinition def = approvalWorkflowEngineService.createWorkflow(user, request);
        return ResponseEntity.ok(ApiResponse.success("Approval workflow created successfully", def));
    }

    @Operation(summary = "List Approval Workflows")
    @GetMapping("/workflows")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getWorkflows(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<ApprovalWorkflowDefinition> list = approvalWorkflowEngineService.getWorkflows(user);
        return ResponseEntity.ok(ApiResponse.success("Approval workflows retrieved successfully", list));
    }

    @Operation(summary = "Get Approval Workflow Details")
    @GetMapping("/workflows/{workflowId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long workflowId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowDefinition def = approvalWorkflowEngineService.getWorkflow(user, workflowId);
        return ResponseEntity.ok(ApiResponse.success("Approval workflow retrieved successfully", def));
    }

    @Operation(summary = "Update Approval Workflow")
    @PutMapping("/workflows/{workflowId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long workflowId,
            @RequestBody CreateApprovalWorkflowRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowDefinition def = approvalWorkflowEngineService.updateWorkflow(user, workflowId, request);
        return ResponseEntity.ok(ApiResponse.success("Approval workflow updated successfully", def));
    }

    @Operation(summary = "Disable/Delete Approval Workflow")
    @DeleteMapping("/workflows/{workflowId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long workflowId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        approvalWorkflowEngineService.deleteWorkflow(user, workflowId);
        return ResponseEntity.ok(ApiResponse.success("Approval workflow disabled successfully", null));
    }

    // ── 2. WORKFLOW STEP APIS ──────────────────────────────────────────────────

    @Operation(summary = "Add Approval Step")
    @PostMapping("/workflows/{workflowId}/steps")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> addStep(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long workflowId,
            @RequestBody CreateApprovalWorkflowRequest.StepRequest stepRequest) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowStep step = approvalWorkflowEngineService.addWorkflowStep(user, workflowId, stepRequest);
        return ResponseEntity.ok(ApiResponse.success("Approval step added successfully", step));
    }

    @Operation(summary = "Get Workflow Steps")
    @GetMapping("/workflows/{workflowId}/steps")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getWorkflowSteps(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long workflowId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowDefinition def = approvalWorkflowEngineService.getWorkflow(user, workflowId);
        return ResponseEntity.ok(ApiResponse.success("Workflow steps retrieved successfully", def.getSteps()));
    }

    // ── 3. APPROVAL INSTANCE APIS ──────────────────────────────────────────────

    @Operation(summary = "Start Approval Workflow Instance")
    @PostMapping("/instances")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> startInstance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody StartApprovalInstanceRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowInstance instance = approvalWorkflowEngineService.startWorkflowInstance(user, request);
        return ResponseEntity.ok(ApiResponse.success("Approval workflow instance started successfully", instance));
    }

    @Operation(summary = "List Approval Instances")
    @GetMapping("/instances")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getInstances(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<ApprovalWorkflowInstance> list = approvalWorkflowEngineService.getInstances(user);
        return ResponseEntity.ok(ApiResponse.success("Approval instances retrieved successfully", list));
    }

    @Operation(summary = "Get Approval Instance Details")
    @GetMapping("/instances/{approvalId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getInstance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowInstance instance = approvalWorkflowEngineService.getInstance(user, approvalId);
        return ResponseEntity.ok(ApiResponse.success("Approval instance details retrieved successfully", instance));
    }

    @Operation(summary = "Get Approval Instance Steps")
    @GetMapping("/instances/{approvalId}/steps")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getInstanceSteps(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowInstance instance = approvalWorkflowEngineService.getInstance(user, approvalId);
        return ResponseEntity.ok(ApiResponse.success("Approval instance steps retrieved successfully", instance.getWorkflowDefinition() != null ? instance.getWorkflowDefinition().getSteps() : List.of()));
    }

    @Operation(summary = "Approve Instance")
    @PostMapping("/instances/{approvalId}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveInstance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalId,
            @RequestBody(required = false) ApprovalActionRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        String comment = request != null ? request.getComment() : "Approved";
        ApprovalTaskDto dto = approvalWorkflowEngineService.approveInstanceTask(user, approvalId, comment);
        return ResponseEntity.ok(ApiResponse.success("Task approved successfully", dto));
    }

    @Operation(summary = "Reject Instance")
    @PostMapping("/instances/{approvalId}/reject")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectInstance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalId,
            @RequestBody(required = false) ApprovalActionRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        String comment = request != null ? request.getComment() : "Rejected";
        ApprovalTaskDto dto = approvalWorkflowEngineService.rejectInstanceTask(user, approvalId, comment);
        return ResponseEntity.ok(ApiResponse.success("Task rejected successfully", dto));
    }

    // ── 4. MY APPROVAL INBOX APIS ───────────────────────────────────────────────

    @Operation(summary = "My Pending Approvals", description = "Retrieves pending approval tasks assigned to the logged-in user.")
    @GetMapping({"/my/pending", "/inbox"})
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getMyPendingApprovals(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) WorkflowType workflowType,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalStatus effectiveStatus = status != null ? status : ApprovalStatus.PENDING;
        ApprovalInboxResponse resp = approvalWorkflowEngineService.getInbox(user, workflowType, effectiveStatus, page, size);
        return ResponseEntity.ok(ApiResponse.success("My pending approvals retrieved successfully", resp));
    }

    @Operation(summary = "Get Task Detail", description = "Retrieves single task details")
    @GetMapping("/{approvalTaskId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getTaskDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalTaskId) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalTaskDetailDto dto = approvalWorkflowEngineService.getTaskDetail(user, approvalTaskId);
        return ResponseEntity.ok(ApiResponse.success("Approval task detail retrieved successfully", dto));
    }

    @Operation(summary = "Approve Task by Task ID")
    @PostMapping("/{approvalTaskId}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveTask(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalTaskId,
            @RequestBody(required = false) ApprovalActionRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        String comment = request != null ? request.getComment() : null;
        ApprovalTaskDto dto = approvalWorkflowEngineService.approveTask(user, approvalTaskId, comment);
        return ResponseEntity.ok(ApiResponse.success("Task approved successfully", dto));
    }

    @Operation(summary = "Reject Task by Task ID")
    @PostMapping("/{approvalTaskId}/reject")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectTask(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalTaskId,
            @RequestBody(required = false) ApprovalActionRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        String comment = request != null ? request.getComment() : null;
        ApprovalTaskDto dto = approvalWorkflowEngineService.rejectTask(user, approvalTaskId, comment);
        return ResponseEntity.ok(ApiResponse.success("Task rejected successfully", dto));
    }
}
