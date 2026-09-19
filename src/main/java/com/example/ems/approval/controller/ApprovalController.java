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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("workflowApprovalController")
@RequestMapping(value = "/api/v1/approvals", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Approval Workflow Engine", description = "Approval Engine Infrastructure & Inbox APIs")
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

    @Operation(summary = "Create Approval Workflow", description = "Creates a new approval workflow definition.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval workflow created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApprovalWorkflowDefinition.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/workflows", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateApprovalWorkflowRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowDefinition def = approvalWorkflowEngineService.createWorkflow(user, request);
        return ResponseEntity.ok(ApiResponse.success("Approval workflow created successfully", def));
    }

    @Operation(summary = "List Approval Workflows", description = "Lists all configured approval workflow definitions for the organization.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval workflows retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ApprovalWorkflowDefinition.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/workflows")
    public ResponseEntity<?> getWorkflows(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<ApprovalWorkflowDefinition> list = approvalWorkflowEngineService.getWorkflows(user);
        return ResponseEntity.ok(ApiResponse.success("Approval workflows retrieved successfully", list));
    }

    @Operation(summary = "Get Approval Workflow Details", description = "Retrieves an approval workflow definition by its ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval workflow retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApprovalWorkflowDefinition.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/workflows/{workflowId}")
    public ResponseEntity<?> getWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long workflowId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowDefinition def = approvalWorkflowEngineService.getWorkflow(user, workflowId);
        return ResponseEntity.ok(ApiResponse.success("Approval workflow retrieved successfully", def));
    }

    @Operation(summary = "Update Approval Workflow", description = "Updates an existing approval workflow definition.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval workflow updated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApprovalWorkflowDefinition.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/workflows/{workflowId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long workflowId,
            @RequestBody CreateApprovalWorkflowRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowDefinition def = approvalWorkflowEngineService.updateWorkflow(user, workflowId, request);
        return ResponseEntity.ok(ApiResponse.success("Approval workflow updated successfully", def));
    }

    @Operation(summary = "Disable/Delete Approval Workflow", description = "Deletes or disables an approval workflow definition.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval workflow disabled successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/workflows/{workflowId}")
    public ResponseEntity<?> deleteWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long workflowId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        approvalWorkflowEngineService.deleteWorkflow(user, workflowId);
        return ResponseEntity.ok(ApiResponse.success("Approval workflow disabled successfully", null));
    }

    @Operation(summary = "Activate Approval Workflow", description = "Transitions an approval workflow definition to ACTIVE status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval workflow activated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApprovalWorkflowDefinition.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/workflows/{workflowId}/activate")
    public ResponseEntity<?> activateWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long workflowId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowDefinition def = approvalWorkflowEngineService.getWorkflow(user, workflowId);
        if (def != null) {
            def.setStatus("ACTIVE");
        }
        return ResponseEntity.ok(ApiResponse.success("Approval workflow activated successfully", def));
    }

    // ── 2. WORKFLOW STEP APIS ──────────────────────────────────────────────────

    @Operation(summary = "Add Approval Step", description = "Appends an approval step to a workflow definition.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval step added successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApprovalWorkflowStep.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/workflows/{workflowId}/steps", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addStep(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long workflowId,
            @RequestBody CreateApprovalWorkflowRequest.StepRequest stepRequest) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowStep step = approvalWorkflowEngineService.addWorkflowStep(user, workflowId, stepRequest);
        return ResponseEntity.ok(ApiResponse.success("Approval step added successfully", step));
    }

    @Operation(summary = "Get Workflow Steps", description = "Lists all steps configured in an approval workflow.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow steps retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ApprovalWorkflowStep.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/workflows/{workflowId}/steps")
    public ResponseEntity<?> getWorkflowSteps(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long workflowId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowDefinition def = approvalWorkflowEngineService.getWorkflow(user, workflowId);
        return ResponseEntity.ok(ApiResponse.success("Workflow steps retrieved successfully", def != null ? def.getSteps() : List.of()));
    }

    // ── 3. APPROVAL INSTANCE APIS ──────────────────────────────────────────────

    @Operation(summary = "Start Approval Workflow Instance", description = "Initiates a new instance of an approval workflow.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval workflow instance started successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApprovalWorkflowInstance.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/instances", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> startInstance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody StartApprovalInstanceRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowInstance instance = approvalWorkflowEngineService.startWorkflowInstance(user, request);
        return ResponseEntity.ok(ApiResponse.success("Approval workflow instance started successfully", instance));
    }

    @Operation(summary = "List Approval Instances", description = "Lists all workflow instances initiated for the organization.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval instances retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ApprovalWorkflowInstance.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/instances")
    public ResponseEntity<?> getInstances(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<ApprovalWorkflowInstance> list = approvalWorkflowEngineService.getInstances(user);
        return ResponseEntity.ok(ApiResponse.success("Approval instances retrieved successfully", list));
    }

    @Operation(summary = "Get Approval Instance Details", description = "Retrieves an approval workflow instance by ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval instance details retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApprovalWorkflowInstance.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/instances/{approvalId}")
    public ResponseEntity<?> getInstance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowInstance instance = approvalWorkflowEngineService.getInstance(user, approvalId);
        return ResponseEntity.ok(ApiResponse.success("Approval instance details retrieved successfully", instance));
    }

    @Operation(summary = "Get Approval Instance Steps", description = "Retrieves the execution steps of an approval instance.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval instance steps retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ApprovalWorkflowStep.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/instances/{approvalId}/steps")
    public ResponseEntity<?> getInstanceSteps(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowInstance instance = approvalWorkflowEngineService.getInstance(user, approvalId);
        return ResponseEntity.ok(ApiResponse.success("Approval instance steps retrieved successfully", instance != null && instance.getWorkflowDefinition() != null ? instance.getWorkflowDefinition().getSteps() : List.of()));
    }

    @Operation(summary = "Approval Request History", description = "Retrieves execution history for an approval request.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval history retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApprovalWorkflowInstance.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/instances/{approvalId}/history")
    public ResponseEntity<?> getApprovalHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalWorkflowInstance instance = approvalWorkflowEngineService.getInstance(user, approvalId);
        return ResponseEntity.ok(ApiResponse.success("Approval history retrieved successfully", instance));
    }

    // ── 4. APPROVAL INBOX & TASK DETAILS APIS ───────────────────────────────────

    @Operation(summary = "My Pending Approvals", description = "Retrieves pending approval tasks assigned to the logged-in user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "My pending approvals retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApprovalInboxResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/inbox")
    public ResponseEntity<?> getInbox(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) WorkflowType workflowType,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalStatus effectiveStatus = status != null ? status : ApprovalStatus.PENDING;
        ApprovalInboxResponse resp = approvalWorkflowEngineService.getInbox(user, workflowType, effectiveStatus, page, size);
        return ResponseEntity.ok(ApiResponse.success("My pending approvals retrieved successfully", resp));
    }

    @Operation(summary = "Get Task Detail", description = "Retrieves single task details.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval task detail retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApprovalTaskDetailDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getTaskDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String taskId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        ApprovalTaskDetailDto dto = approvalWorkflowEngineService.getTaskDetail(user, taskId);
        return ResponseEntity.ok(ApiResponse.success("Approval task detail retrieved successfully", dto));
    }

    @Operation(summary = "Approve Task", description = "Approves a pending approval task with comments.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task approved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApprovalTaskDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping({"/{taskId}/approve", "/tasks/{taskId}/approve"})
    public ResponseEntity<?> approveTask(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String taskId,
            @RequestBody(required = false) ApprovalActionRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        String comment = request != null ? request.getComment() : "Approved";
        ApprovalTaskDto resp = approvalWorkflowEngineService.approveTask(user, taskId, comment);
        return ResponseEntity.ok(ApiResponse.success("Task approved successfully", resp));
    }

    @Operation(summary = "Reject Task", description = "Rejects a pending approval task with comments.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task rejected successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApprovalTaskDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping({"/{taskId}/reject", "/tasks/{taskId}/reject"})
    public ResponseEntity<?> rejectTask(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String taskId,
            @RequestBody(required = false) ApprovalActionRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        String comment = request != null ? request.getComment() : "Rejected";
        ApprovalTaskDto resp = approvalWorkflowEngineService.rejectTask(user, taskId, comment);
        return ResponseEntity.ok(ApiResponse.success("Task rejected successfully", resp));
    }

    @Operation(summary = "Execute Generic Approval Action", description = "Executes an action (APPROVE, REJECT, HOLD) on an approval task or workflow instance.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval action executed successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApprovalTaskDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or action", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{approvalId}/action", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handleApprovalAction(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalId,
            @RequestBody ApprovalGenericActionRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        if (request == null || request.getAction() == null || request.getAction().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Action is required in request payload (e.g. APPROVE, REJECT, HOLD)", "VAL_001"));
        }

        try {
            ApprovalTaskDto resp = approvalWorkflowEngineService.executeApprovalAction(user, approvalId, request.getAction(), request.getRemarks());
            return ResponseEntity.ok(ApiResponse.success("Approval action executed successfully", resp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_001"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_002"));
        }
    }
}
