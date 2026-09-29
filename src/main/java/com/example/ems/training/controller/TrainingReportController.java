package com.example.ems.training.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.dto.AttendanceReportResponse;
import com.example.ems.training.dto.DepartmentProgressResponse;
import com.example.ems.training.dto.EmployeeReportResponse;
import com.example.ems.training.dto.ParticipationReportResponse;
import com.example.ems.training.dto.TeamProgressResponse;
import com.example.ems.training.dto.TrainingReportSummaryResponse;
import com.example.ems.training.service.TrainingManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/training/reports", "/api/v1/training-reports"})
@CrossOrigin("*")
@Tag(name = "Training Analytics & Reports")
public class TrainingReportController {

    @Autowired
    private TrainingManagementService trainingService;

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
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            String principal = SecurityContextHolder.getContext().getAuthentication().getName();
            if (principal != null && !principal.isBlank()) {
                return userRepository.findByWorkEmail(principal).orElse(null);
            }
        }
        return null;
    }

    @Operation(summary = "Get high-level training analytics summary")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Training summary report retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingReportSummaryResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/summary")
    public ResponseEntity<?> getSummaryReport(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        TrainingReportSummaryResponse summary = trainingService.getReportSummary(user);
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Get training participation analytics breakdown")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Participation report retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ParticipationReportResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/participation")
    public ResponseEntity<?> getParticipationReport(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        List<ParticipationReportResponse> report = trainingService.getParticipationReport(user);
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Get training attendance analytics report")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Attendance report retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AttendanceReportResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/attendance")
    public ResponseEntity<?> getAttendanceReport(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        List<AttendanceReportResponse> report = trainingService.getAttendanceReport(user);
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Export training attendance report as CSV")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV export generated successfully",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/export")
    public ResponseEntity<?> exportReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "csv") String format) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<AttendanceReportResponse> report = trainingService.getAttendanceReport(user);
        StringBuilder csv = new StringBuilder();
        csv.append("Training/Department,Assigned,Attended,Absent,CompletionPercentage\n");
        for (AttendanceReportResponse row : report) {
            csv.append(String.format("\"%s\",%d,%d,%d,%.1f%%\n",
                    row.getDepartmentName(), row.getTotalAssigned(), row.getTotalAttended(), row.getTotalAbsent(), row.getCompletionPercentage()));
        }

        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"training_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    // ── Department, Team & Employee Reports ─────────────────────────────────
    @Operation(summary = "Get department training report")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department report retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartmentProgressResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<?> getDepartmentReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long departmentId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            return ResponseEntity.ok(trainingService.getDepartmentProgress(departmentId, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_REP_001"));
        }
    }

    @Operation(summary = "Get department completion report")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department completion report retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartmentProgressResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/department/{departmentId}/completion")
    public ResponseEntity<?> getDepartmentCompletionReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long departmentId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            return ResponseEntity.ok(trainingService.getDepartmentProgress(departmentId, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_REP_002"));
        }
    }

    @Operation(summary = "Get team training report")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Team report retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamProgressResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/team/{teamId}")
    public ResponseEntity<?> getTeamReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long teamId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            return ResponseEntity.ok(trainingService.getTeamProgress(teamId, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_REP_003"));
        }
    }

    @Operation(summary = "Get team completion report")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Team completion report retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamProgressResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/team/{teamId}/completion")
    public ResponseEntity<?> getTeamCompletionReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long teamId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            return ResponseEntity.ok(trainingService.getTeamProgress(teamId, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_REP_004"));
        }
    }

    @Operation(summary = "Get employee training report")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee report retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeReportResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            return ResponseEntity.ok(trainingService.getEmployeeReport(employeeId, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_REP_005"));
        }
    }
}
