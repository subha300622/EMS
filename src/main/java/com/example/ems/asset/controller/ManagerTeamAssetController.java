package com.example.ems.asset.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ems.asset.dto.TeamAssetDtos;
import com.example.ems.asset.service.ManagerTeamAssetService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manager/team-assets")
@CrossOrigin("*")
@Tag(name = "Manager Self Service - Team Assets")
public class ManagerTeamAssetController {

    @Autowired
    private ManagerTeamAssetService teamAssetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

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

    private Employee resolveEmployee(User user) {
        if (user == null) return null;
        return employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
    }

    private ResponseEntity<?> validateManagerAndPermission(String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = resolveEmployee(currentUser);
        if (manager == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        return ResponseEntity.ok(manager);
    }

    @Operation(summary = "Get Team Assets Dashboard Summary", description = "Retrieves count statistics for the manager's team assets.")
    @GetMapping("/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDashboard(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        
        ResponseEntity<?> validation = validateManagerAndPermission(authHeader);
        if (validation.getStatusCode() != HttpStatus.OK) {
            return (ResponseEntity) validation;
        }

        Employee manager = (Employee) validation.getBody();
        TeamAssetDtos.DashboardResponse response = teamAssetService.getTeamAssetsDashboard(manager);
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved successfully", response.data()));
    }

    @Operation(summary = "Get Team Assets Inventory List", description = "Retrieves a paginated list of assets assigned to the manager's team.")
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getTeamAssets(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        ResponseEntity<?> validation = validateManagerAndPermission(authHeader);
        if (validation.getStatusCode() != HttpStatus.OK) {
            return (ResponseEntity) validation;
        }

        Employee manager = (Employee) validation.getBody();
        Pageable pageable = PageRequest.of(page, size);
        
        String cleanStatus = (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) ? status.trim().toUpperCase() : null;
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : "";

        Page<TeamAssetDtos.InventoryItem> result = teamAssetService.getTeamAssets(manager, cleanStatus, employeeId, cleanSearch, pageable);
        return ResponseEntity.ok(ApiResponse.success("Team assets retrieved successfully", result));
    }

    @Operation(summary = "Get Team Asset Details", description = "Retrieves detailed information for a specific asset assigned to a team member.")
    @GetMapping("/{assetId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getTeamAssetDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long assetId) {
        
        ResponseEntity<?> validation = validateManagerAndPermission(authHeader);
        if (validation.getStatusCode() != HttpStatus.OK) {
            return (ResponseEntity) validation;
        }

        Employee manager = (Employee) validation.getBody();
        try {
            TeamAssetDtos.DetailResponse response = teamAssetService.getTeamAssetDetails(manager, assetId);
            return ResponseEntity.ok(ApiResponse.success("Asset details retrieved successfully", response));
        } catch (SecurityException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error(e.getMessage(), "AST_403"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.error(e.getMessage(), "AST_404"));
        }
    }

    @Operation(summary = "Get Team Asset Timeline", description = "Retrieves the lifecycle event history log for a specific team asset.")
    @GetMapping("/{assetId}/timeline")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getTeamAssetTimeline(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long assetId) {
        
        ResponseEntity<?> validation = validateManagerAndPermission(authHeader);
        if (validation.getStatusCode() != HttpStatus.OK) {
            return (ResponseEntity) validation;
        }

        Employee manager = (Employee) validation.getBody();
        try {
            List<TeamAssetDtos.TimelineEvent> response = teamAssetService.getTeamAssetTimeline(manager, assetId);
            return ResponseEntity.ok(ApiResponse.success("Timeline retrieved successfully", response));
        } catch (SecurityException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error(e.getMessage(), "AST_403"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.error(e.getMessage(), "AST_404"));
        }
    }

    @Operation(summary = "Get Pending Team Asset Requests", description = "Retrieves a paginated list of pending asset requests from the manager's team.")
    @GetMapping("/requests")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getPendingAssetRequests(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        ResponseEntity<?> validation = validateManagerAndPermission(authHeader);
        if (validation.getStatusCode() != HttpStatus.OK) {
            return (ResponseEntity) validation;
        }

        Employee manager = (Employee) validation.getBody();
        Pageable pageable = PageRequest.of(page, size);
        Page<TeamAssetDtos.RequestItem> result = teamAssetService.getPendingAssetRequests(manager, pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending asset requests retrieved successfully", result));
    }

    @Operation(summary = "Approve Asset Request", description = "Approves a team member's asset request.")
    @PutMapping("/requests/{requestId}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveAssetRequest(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestBody(required = false) TeamAssetDtos.ApprovalRequest request) {
        
        ResponseEntity<?> validation = validateManagerAndPermission(authHeader);
        if (validation.getStatusCode() != HttpStatus.OK) {
            return (ResponseEntity) validation;
        }

        Employee manager = (Employee) validation.getBody();
        String remarks = (request != null) ? request.managerRemarks() : null;

        try {
            teamAssetService.approveAssetRequest(manager, requestId, remarks);
            return ResponseEntity.ok(ApiResponse.success("Asset request approved"));
        } catch (SecurityException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error(e.getMessage(), "AST_403"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.error(e.getMessage(), "AST_404"));
        } catch (IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.error(e.getMessage(), "AST_400"));
        }
    }

    @Operation(summary = "Reject Asset Request", description = "Rejects a team member's asset request.")
    @PutMapping("/requests/{requestId}/reject")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectAssetRequest(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestBody(required = false) TeamAssetDtos.ApprovalRequest request) {
        
        ResponseEntity<?> validation = validateManagerAndPermission(authHeader);
        if (validation.getStatusCode() != HttpStatus.OK) {
            return (ResponseEntity) validation;
        }

        Employee manager = (Employee) validation.getBody();
        String remarks = (request != null) ? request.managerRemarks() : null;

        try {
            teamAssetService.rejectAssetRequest(manager, requestId, remarks);
            return ResponseEntity.ok(ApiResponse.success("Asset request rejected"));
        } catch (SecurityException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error(e.getMessage(), "AST_403"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.error(e.getMessage(), "AST_404"));
        } catch (IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.error(e.getMessage(), "AST_400"));
        }
    }

    @Operation(summary = "Get Pending Team Return Requests", description = "Retrieves a paginated list of pending asset returns from the manager's team.")
    @GetMapping("/returns")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getPendingReturnRequests(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        ResponseEntity<?> validation = validateManagerAndPermission(authHeader);
        if (validation.getStatusCode() != HttpStatus.OK) {
            return (ResponseEntity) validation;
        }

        Employee manager = (Employee) validation.getBody();
        Pageable pageable = PageRequest.of(page, size);
        Page<TeamAssetDtos.ReturnRequestItem> result = teamAssetService.getPendingReturnRequests(manager, pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending return requests retrieved successfully", result));
    }

    @Operation(summary = "Approve Asset Return", description = "Approves a team member's asset return request.")
    @PutMapping("/returns/{returnRequestId}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveReturnRequest(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long returnRequestId,
            @RequestBody(required = false) TeamAssetDtos.ReturnApprovalRequest request) {
        
        ResponseEntity<?> validation = validateManagerAndPermission(authHeader);
        if (validation.getStatusCode() != HttpStatus.OK) {
            return (ResponseEntity) validation;
        }

        Employee manager = (Employee) validation.getBody();
        String remarks = (request != null) ? request.remarks() : null;

        try {
            teamAssetService.approveReturnRequest(manager, returnRequestId, remarks);
            return ResponseEntity.ok(ApiResponse.success("Asset return approved"));
        } catch (SecurityException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error(e.getMessage(), "AST_403"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.error(e.getMessage(), "AST_404"));
        } catch (IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.error(e.getMessage(), "AST_400"));
        }
    }

    @Operation(summary = "Get Manager Team Asset Analytics", description = "Retrieves category inventory metrics and value metrics for the manager's team.")
    @GetMapping("/analytics")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAnalytics(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        
        ResponseEntity<?> validation = validateManagerAndPermission(authHeader);
        if (validation.getStatusCode() != HttpStatus.OK) {
            return (ResponseEntity) validation;
        }

        Employee manager = (Employee) validation.getBody();
        TeamAssetDtos.AnalyticsResponse response = teamAssetService.getTeamAssetsAnalytics(manager);
        return ResponseEntity.ok(ApiResponse.success("Analytics retrieved successfully", response.data()));
    }
}
