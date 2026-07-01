package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.service.AppraisalService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/api/v1/performance/appraisals")
@CrossOrigin("*")
@Tag(name = "Team Appraisal Management")
public class TeamAppraisalController {

    @Autowired
    private AppraisalService appraisalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

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

    private boolean isManagerHrOrAdmin(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        String role = user.getRole().getName().toUpperCase();
        return "MANAGER".equals(role) || "HR".equals(role) || "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }

    private boolean isFinanceOrAdmin(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        String role = user.getRole().getName().toUpperCase();
        return "FINANCE".equals(role) || "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }

    @Operation(summary = "Get Dashboard Summary Metrics for Team")
    @GetMapping("/team/summary")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTeamSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManagerHrOrAdmin(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager, HR or Admin role.", "AUTH_002"));
        }

        TeamAppraisalSummaryDto summary = appraisalService.getTeamSummary(user.getWorkEmail(), cycleId);
        return ResponseEntity.ok(ApiResponse.success("Team appraisal summary retrieved successfully", summary));
    }

    @Operation(summary = "Get Paginated List of Direct Reports' Appraisals")
    @GetMapping("/team")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTeamList(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long cycleId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AppraisalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeName,asc") String sort) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManagerHrOrAdmin(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager, HR or Admin role.", "AUTH_002"));
        }

        String[] sortParts = sort.split(",");
        String sortProp = sortParts[0];
        Sort.Direction sortDir = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortProp));

        PageResponse<TeamAppraisalListItemDto> result = appraisalService.getTeamAppraisals(
                user.getWorkEmail(), cycleId, search, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Team appraisals retrieved successfully", result));
    }

    @Operation(summary = "Get Details of a Specific Direct Report's Appraisal")
    @GetMapping("/{appraisalId}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getAppraisalDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManagerHrOrAdmin(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager, HR or Admin role.", "AUTH_002"));
        }

        try {
            TeamAppraisalDetailDto detail = appraisalService.getAppraisalDetail(user.getWorkEmail(), appraisalId);
            return ResponseEntity.ok(ApiResponse.success("Appraisal details retrieved successfully", detail));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        }
    }

    @Operation(summary = "Save or Update Manager Rating and Comments")
    @PutMapping("/{appraisalId}/rating")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> saveTeamRating(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId,
            @Valid @RequestBody TeamAppraisalRatingRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManagerHrOrAdmin(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager, HR or Admin role.", "AUTH_002"));
        }

        try {
            AppraisalResponse response = appraisalService.saveTeamRating(user.getWorkEmail(), appraisalId, request);
            return ResponseEntity.ok(ApiResponse.success("Manager rating and comments saved successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        }
    }

    @Operation(summary = "Justify Low Attendance with Audit Trail")
    @PostMapping("/{appraisalId}/attendance-justification")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> justifyAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId,
            @Valid @RequestBody AttendanceJustificationRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManagerHrOrAdmin(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager, HR or Admin role.", "AUTH_002"));
        }

        try {
            AppraisalResponse response = appraisalService.justifyAttendance(user.getWorkEmail(), appraisalId, request);
            return ResponseEntity.ok(ApiResponse.success("Attendance justification submitted successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        }
    }

    @Operation(summary = "Submit Draft Appraisal (employee finishes draft and sends into workflow)")
    @PostMapping("/{appraisalId}/submit")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> submitAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        try {
            AppraisalResponse response = appraisalService.submitAppraisal(appraisalId, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Appraisal submitted successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_003"));
        }
    }

    @Operation(summary = "Submit Appraisal & Recommended Increment to Finance (HR/Manager)")
    @PostMapping("/{appraisalId}/submit-to-finance")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> submitToFinance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManagerHrOrAdmin(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager, HR or Admin role.", "AUTH_002"));
        }
        try {
            AppraisalResponse response = appraisalService.submitToFinance(user.getWorkEmail(), appraisalId);
            return ResponseEntity.ok(ApiResponse.success("Appraisal submitted to finance successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_003"));
        }
    }

    @Operation(summary = "Reopen Appraisal (after rejection or correction)")
    @PatchMapping("/{appraisalId}/reopen")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> reopenAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        try {
            AppraisalResponse response = appraisalService.reopenAppraisal(appraisalId, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Appraisal reopened successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_003"));
        }
    }

    @Operation(summary = "Request Revision (manager ↔ employee loop)")
    @PostMapping("/{appraisalId}/request-revision")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> requestRevision(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId,
            @RequestBody Map<String, String> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        String instructions = body.getOrDefault("instructions", "Revision requested.");
        try {
            AppraisalResponse response = appraisalService.requestRevision(appraisalId, instructions, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Revision requested successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_003"));
        }
    }

    @Operation(summary = "Save Draft Appraisal")
    @PatchMapping("/{appraisalId}/draft")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> saveDraft(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId,
            @Valid @RequestBody AppraisalSelfReviewRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        try {
            AppraisalResponse response = appraisalService.saveDraft(appraisalId, request, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Appraisal draft saved successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_003"));
        }
    }

    @Operation(summary = "Lock Appraisal (prevent edits after submission)")
    @PatchMapping("/{appraisalId}/lock")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> lockAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        try {
            AppraisalResponse response = appraisalService.lockAppraisal(appraisalId, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Appraisal locked successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_003"));
        }
    }

    @Operation(summary = "Compensation Simulation API")
    @PostMapping("/{appraisalId}/simulate-increment")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> simulateIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        try {
            Map<String, Object> sim = appraisalService.simulateIncrement(appraisalId);
            return ResponseEntity.ok(ApiResponse.success("Compensation simulation calculated successfully", sim));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_003"));
        }
    }

    @Operation(summary = "Freeze Appraisal Cycle")
    @PostMapping("/cycles/{cycleId}/freeze")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> freezeCycle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        try {
            AppraisalCycleResponse response = appraisalService.freezeCycle(cycleId, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Appraisal cycle frozen successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_003"));
        }
    }

    @Operation(summary = "Reopen Appraisal Cycle")
    @PostMapping("/cycles/{cycleId}/reopen")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> reopenCycle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        try {
            AppraisalCycleResponse response = appraisalService.reopenCycle(cycleId, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Appraisal cycle reopened successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_003"));
        }
    }

    @Operation(summary = "Close Appraisal Cycle Permanently")
    @PostMapping("/cycles/{cycleId}/close")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> closeCycle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        try {
            AppraisalCycleResponse response = appraisalService.closeCycle(cycleId, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Appraisal cycle closed successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_003"));
        }
    }

    @Operation(summary = "Compensation Freeze API")
    @PostMapping("/{appraisalId}/compensation-freeze")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> compensationFreeze(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        try {
            AppraisalResponse response = appraisalService.compensationFreeze(appraisalId, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Compensation details frozen successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_003"));
        }
    }

    @Operation(summary = "Get Paginated List of Appraisals Pending Finance Decision")
    @GetMapping("/finance")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getFinanceQueue(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long cycleId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AppraisalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeName,asc") String sort) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isFinanceOrAdmin(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Finance or Admin role.", "AUTH_002"));
        }

        String[] sortParts = sort.split(",");
        String sortProp = sortParts[0];
        Sort.Direction sortDir = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortProp));

        AppraisalStatus queryStatus = status != null ? status : AppraisalStatus.PENDING_FINANCE;

        PageResponse<TeamAppraisalListItemDto> result = appraisalService.getTeamAppraisals(
                user.getWorkEmail(), cycleId, search, queryStatus, pageable);
        return ResponseEntity.ok(ApiResponse.success("Finance appraisal queue retrieved successfully", result));
    }

    @Operation(summary = "Finance Approve Increment")
    @PostMapping("/{appraisalId}/finance/approve")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> financeApprove(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId,
            @Valid @RequestBody FinanceDecisionRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isFinanceOrAdmin(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Finance or Admin role.", "AUTH_002"));
        }

        try {
            AppraisalResponse response = appraisalService.financeApproveHardened(appraisalId, request, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Appraisal increment approved by finance successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "APP_004"));
        }
    }

    @Operation(summary = "Finance Reject Increment")
    @PostMapping("/{appraisalId}/finance/reject")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> financeReject(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId,
            @Valid @RequestBody FinanceDecisionRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isFinanceOrAdmin(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Finance or Admin role.", "AUTH_002"));
        }

        try {
            AppraisalResponse response = appraisalService.financeRejectHardened(appraisalId, request, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Appraisal increment rejected by finance successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "APP_005"));
        }
    }

    @Operation(summary = "Finance Send Back for Revision")
    @PostMapping("/{appraisalId}/finance/send-back")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> financeSendBack(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isFinanceOrAdmin(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Finance or Admin role.", "AUTH_002"));
        }

        try {
            AppraisalResponse response = appraisalService.financeSendBack(appraisalId, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Appraisal sent back successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "APP_006"));
        }
    }

    @Operation(summary = "Bulk Approve Appraisals (Manager)")
    @PostMapping("/bulk/approve")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> bulkApprove(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, List<Long>> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManagerHrOrAdmin(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager, HR or Admin role.", "AUTH_002"));
        }
        List<Long> ids = body.getOrDefault("ids", Collections.emptyList());
        Map<String, Object> result = appraisalService.bulkApprove(ids, user.getWorkEmail());
        return ResponseEntity.ok(ApiResponse.success("Bulk approvals processed successfully", result));
    }

    @Operation(summary = "Bulk Reject Appraisals (Manager)")
    @PostMapping("/bulk/reject")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> bulkReject(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, List<Long>> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManagerHrOrAdmin(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager, HR or Admin role.", "AUTH_002"));
        }
        List<Long> ids = body.getOrDefault("ids", Collections.emptyList());
        Map<String, Object> result = appraisalService.bulkReject(ids, user.getWorkEmail());
        return ResponseEntity.ok(ApiResponse.success("Bulk rejections processed successfully", result));
    }

    @Operation(summary = "Get Appraisal Timeline Tracking")
    @GetMapping("/{id}/timeline")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTimeline(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        List<Map<String, Object>> list = appraisalService.getTimeline(id);
        return ResponseEntity.ok(ApiResponse.success("Appraisal timeline retrieved successfully", list));
    }

    @Operation(summary = "Get Appraisal Comments Thread")
    @GetMapping("/{id}/comments")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getComments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        List<Map<String, Object>> list = appraisalService.getComments(id);
        return ResponseEntity.ok(ApiResponse.success("Appraisal comments thread retrieved successfully", list));
    }

    @Operation(summary = "Add Comment to Appraisal Discussion")
    @PostMapping("/{id}/comments")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> addComment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        String commentText = body.get("comment");
        if (commentText == null || commentText.trim().isEmpty()) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("Comment cannot be empty.", "VAL_001"));
        }
        Map<String, Object> comment = appraisalService.addComment(id, commentText, user.getWorkEmail());
        return ResponseEntity.ok(ApiResponse.success("Comment added successfully", comment));
    }

    @Operation(summary = "Rating Distribution Analytics")
    @GetMapping("/analytics/rating-distribution")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getRatingDistribution(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Map<String, Long> dist = appraisalService.getRatingDistribution();
        return ResponseEntity.ok(ApiResponse.success("Rating distribution analytics retrieved successfully", dist));
    }

    @Operation(summary = "Department-wise Appraisal Summary")
    @GetMapping("/analytics/department-summary")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getDepartmentSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        List<Map<String, Object>> summary = appraisalService.getDepartmentSummary();
        return ResponseEntity.ok(ApiResponse.success("Department-wise appraisal summary retrieved successfully", summary));
    }

    @Operation(summary = "Increment Recommendation vs Approved Comparison")
    @GetMapping("/analytics/increment-gap")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getIncrementGap(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Map<String, Object> gap = appraisalService.getIncrementGap();
        return ResponseEntity.ok(ApiResponse.success("Increment recommendation vs approved comparison retrieved successfully", gap));
    }

    @Operation(summary = "Cycle Completion Metrics")
    @GetMapping("/analytics/cycle-progress")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getCycleProgress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Map<String, Object> prog = appraisalService.getCycleProgress(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Cycle completion metrics retrieved successfully", prog));
    }

    @Operation(summary = "Get Appraisal Audit Trail")
    @GetMapping("/{id}/audit-logs")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getAuditLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        List<Map<String, Object>> logs = appraisalService.getAuditLogs(id);
        return ResponseEntity.ok(ApiResponse.success("Appraisal audit logs retrieved successfully", logs));
    }

    @Operation(summary = "Get Change History Diff View")
    @GetMapping("/{id}/history")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        List<Map<String, Object>> history = appraisalService.getHistory(id);
        return ResponseEntity.ok(ApiResponse.success("Appraisal change history retrieved successfully", history));
    }
}
