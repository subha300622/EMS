package com.example.ems.approval.controller;

import com.example.ems.approval.dto.*;
import com.example.ems.approval.entity.ApprovalStatus;
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

    @Operation(summary = "Get Approval Inbox", description = "Retrieves pending or historical approval tasks assigned to the logged-in approver.")
    @GetMapping("/inbox")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getInbox(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) WorkflowType workflowType,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            ApprovalInboxResponse resp = approvalWorkflowEngineService.getInbox(user, workflowType, status, page, size);
            return ResponseEntity.ok(ApiResponse.success("Approval inbox retrieved successfully", resp));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Get Approval Task Detail", description = "Retrieves single approval task details and available actions.")
    @GetMapping("/{approvalTaskId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getTaskDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalTaskId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            ApprovalTaskDetailDto dto = approvalWorkflowEngineService.getTaskDetail(user, approvalTaskId);
            return ResponseEntity.ok(ApiResponse.success("Approval task detail retrieved successfully", dto));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Approve Task", description = "Approves the assigned approval task and advances the workflow.")
    @PostMapping("/{approvalTaskId}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveTask(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalTaskId,
            @RequestBody(required = false) ApprovalActionRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String comment = request != null ? request.getComment() : null;

        try {
            ApprovalTaskDto dto = approvalWorkflowEngineService.approveTask(user, approvalTaskId, comment);
            return ResponseEntity.ok(ApiResponse.success("Task approved successfully", dto));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_002"));
        }
    }

    @Operation(summary = "Reject Task", description = "Rejects the assigned approval task and marks the workflow as rejected.")
    @PostMapping("/{approvalTaskId}/reject")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectTask(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalTaskId,
            @RequestBody(required = false) ApprovalActionRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String comment = request != null ? request.getComment() : null;

        try {
            ApprovalTaskDto dto = approvalWorkflowEngineService.rejectTask(user, approvalTaskId, comment);
            return ResponseEntity.ok(ApiResponse.success("Task rejected successfully", dto));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_002"));
        }
    }

    @Operation(summary = "Request Changes on Task", description = "Requests modifications from the requester for the assigned approval task.")
    @PostMapping("/{approvalTaskId}/request-changes")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> requestChanges(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String approvalTaskId,
            @RequestBody(required = false) ApprovalActionRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String comment = request != null ? request.getComment() : null;

        try {
            ApprovalTaskDto dto = approvalWorkflowEngineService.requestChanges(user, approvalTaskId, comment);
            return ResponseEntity.ok(ApiResponse.success("Changes requested successfully", dto));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_002"));
        }
    }
}
