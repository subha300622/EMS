package com.example.ems.schedule.swap.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import com.example.ems.schedule.swap.dto.*;
import com.example.ems.schedule.swap.service.ScheduleSwapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/schedule-swap-requests")
@CrossOrigin("*")
@Tag(name = "Schedule Swap Requests", description = "Schedule Swap Request Management APIs")
public class ScheduleSwapController {

    @Autowired
    private ScheduleSwapService scheduleSwapService;

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

    @Operation(summary = "Create Swap Request", description = "Submits a request to swap scheduled shifts with another employee and initiates the approval workflow.")
    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createSwapRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ScheduleSwapCreateRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            ScheduleSwapResponseDto dto = scheduleSwapService.createSwapRequest(user, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Schedule swap request created successfully", dto));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Source and target schedules belong to the same employee")) {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.error(msg, "SWAP_002"));
            }
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(msg, "VAL_001"));
        } catch (IllegalStateException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("An active swap request already exists")) {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ErrorResponse.error(msg, "SWAP_003"));
            }
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.error(msg, "VAL_002"));
        }
    }

    @Operation(summary = "Get All Swap Requests", description = "Retrieves paginated list of schedule swap requests in the organization.")
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getSwapRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        ScheduleSwapListResponse resp = scheduleSwapService.getSwapRequests(user, page, size);
        return ResponseEntity.ok(ApiResponse.success("Swap requests retrieved successfully", resp));
    }

    @Operation(summary = "Get My Swap Requests", description = "Retrieves paginated list of swap requests requested by or involving the logged-in employee.")
    @GetMapping("/my")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getMySwapRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        ScheduleSwapListResponse resp = scheduleSwapService.getMySwapRequests(user, page, size);
        return ResponseEntity.ok(ApiResponse.success("My swap requests retrieved successfully", resp));
    }

    @Operation(summary = "Get Swap Request Detail", description = "Retrieves single swap request details by ID.")
    @GetMapping("/{requestId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getSwapRequestById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String requestId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            ScheduleSwapResponseDto dto = scheduleSwapService.getSwapRequestById(user, requestId);
            return ResponseEntity.ok(ApiResponse.success("Swap request retrieved successfully", dto));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Cancel Swap Request", description = "Cancels an active schedule swap request.")
    @PostMapping("/{requestId}/cancel")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> cancelSwapRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String requestId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            ScheduleSwapResponseDto dto = scheduleSwapService.cancelSwapRequest(user, requestId);
            return ResponseEntity.ok(ApiResponse.success("Swap request cancelled successfully", dto));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_002"));
        }
    }
}
