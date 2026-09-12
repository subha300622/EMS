package com.example.ems.offboarding.controller;

import com.example.ems.approval.dto.ApprovalActionRequest;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.ems.offboarding.dto.ExitRequestDecisionDto;

@RestController
@RequestMapping("/api/v1/exit-requests")
@CrossOrigin("*")
@Tag(name = "Exit Requests", description = "Offboarding Exit Request Approval APIs")
public class ExitRequestController {

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

    @Operation(summary = "Approve Exit Request", description = "Approves an employee resignation/exit request")
    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasAuthority('EXIT_APPROVE')")
    public ResponseEntity<ApiResponse<ExitRequestDecisionDto>> approveExitRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestBody(required = false) ApprovalActionRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));

        String comment = request != null ? request.getComment() : "Approved";
        ExitRequestDecisionDto response = new ExitRequestDecisionDto(requestId, "APPROVED", comment);
        return ResponseEntity.ok(ApiResponse.success("Exit request approved successfully", response));
    }

    @Operation(summary = "Reject Exit Request", description = "Rejects an employee resignation/exit request")
    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasAuthority('EXIT_REJECT')")
    public ResponseEntity<ApiResponse<ExitRequestDecisionDto>> rejectExitRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestBody(required = false) ApprovalActionRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));

        String comment = request != null ? request.getComment() : "Rejected";
        ExitRequestDecisionDto response = new ExitRequestDecisionDto(requestId, "REJECTED", comment);
        return ResponseEntity.ok(ApiResponse.success("Exit request rejected successfully", response));
    }

    @Operation(summary = "Send Back Exit Request", description = "Sends back an employee exit request for review")
    @PostMapping("/{requestId}/send-back")
    @PreAuthorize("hasAuthority('EXIT_APPROVE')")
    public ResponseEntity<ApiResponse<ExitRequestDecisionDto>> sendBackExitRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestBody(required = false) ApprovalActionRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));

        String comment = request != null ? request.getComment() : "Sent back";
        ExitRequestDecisionDto response = new ExitRequestDecisionDto(requestId, "NEEDS_REVISION", comment);
        return ResponseEntity.ok(ApiResponse.success("Exit request sent back successfully", response));
    }
}
