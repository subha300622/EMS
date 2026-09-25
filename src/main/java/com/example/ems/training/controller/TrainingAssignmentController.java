package com.example.ems.training.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.dto.EmployeeTrainingDetailResponse;
import com.example.ems.training.dto.TeamRiskResponse;
import com.example.ems.training.dto.TeamSummaryResponse;
import com.example.ems.training.dto.TrainingAssignRequest;
import com.example.ems.training.dto.TrainingCatalogRequest;
import com.example.ems.training.dto.TrainingProgressUpdateRequest;
import com.example.ems.training.entity.TrainingCourse;
import com.example.ems.training.service.TrainingAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.ems.training.dto.CompleteTrainingRequest;
import com.example.ems.training.dto.TrainingAssignmentItemResponse;
import com.example.ems.training.dto.MyTrainingItemResponse;
import com.example.ems.training.dto.TrainingAssignmentDetailsResponse;
import com.example.ems.training.dto.TeamProgressListResponse;

@RestController
@RequestMapping("/api/v1/training")
@CrossOrigin("*")
@Tag(name = "Training Management")
public class TrainingAssignmentController {

    @Autowired
    private TrainingAssignmentService assignmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    // ── Auth helpers ─────────────────────────────────────────────────────────
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

    private boolean isManager(User user) {
        return roleService.hasPermission(user.getWorkEmail(), "employee.update")
                || roleService.hasPermission(user.getWorkEmail(), "employee.delete")
                || roleService.hasPermission(user.getWorkEmail(), "recruitment.manage");
    }

    private Long getEmployeeDbId(User user) {
        if (user == null || user.getEmployeeId() == null)
            return null;
        return employeeRepository.findByEmployeeId(user.getEmployeeId())
                .map(Employee::getId)
                .orElse(null);
    }

    // ── A. Catalog APIs (HR) ─────────────────────────────────────────────────
    @Operation(summary = "Create Course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Training course created successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"id\": 1, \"message\": \"Training course created successfully\", \"status\": \"ACTIVE\"}"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/catalog")
public ResponseEntity<?> createCourse(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody TrainingCatalogRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManager(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        TrainingCourse course = assignmentService.createCourse(request);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", course.getId());
        resp.put("message", "Training course created successfully");
        resp.put("status", course.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Operation(summary = "Get Catalog")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Courses retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TrainingCourse.class))))
    })
    @GetMapping("/catalog")
    public ResponseEntity<?> getCatalog() {
        List<TrainingCourse> courses = assignmentService.getCourses();
        return ResponseEntity.ok(courses);
    }

    @Operation(summary = "Get Course by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Course retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingCourse.class))),
        @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/catalog/{id}")
public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        Optional<TrainingCourse> course = assignmentService.getCourseById(id);
        if (course.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Course not found with ID: " + id, "TRN_001"));
        }
        return ResponseEntity.ok(course.get());
    }

    // ── B. Assignment APIs (Manager/HR) ──────────────────────────────────────
    @Operation(summary = "Assign Training")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Training assigned successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"assignmentId\": 1, \"message\": \"Training assigned successfully\", \"assignedCount\": 5, \"status\": \"ASSIGNED\", \"assignedOn\": \"2026-09-25\"}"))),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/assign")
public ResponseEntity<?> assignTraining(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody TrainingAssignRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManager(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        try {
            if (request.getAssignedBy() == null) {
                Long managerDbId = getEmployeeDbId(currentUser);
                request.setAssignedBy(managerDbId);
            }
            Map<String, Object> resp = assignmentService.assignTraining(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_002"));
        }
    }

    @Operation(summary = "Get Assignments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assignments retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TrainingAssignmentItemResponse.class))))
    })
    @GetMapping("/assignments")
    public ResponseEntity<?> getAssignments() {
        List<TrainingAssignmentItemResponse> list = assignmentService.getAssignments().stream().map(a ->
            new TrainingAssignmentItemResponse(
                a.getId(),
                a.getCourse().getTitle(),
                a.getDueDate() != null ? a.getDueDate().toString() : null,
                a.getPriority(),
                a.getProgressList().size()
            )
        ).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Get Assignment Details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assignment details retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingAssignmentDetailsResponse.class))),
        @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/assignments/{id}")
public ResponseEntity<?> getAssignmentDetails(@PathVariable Long id) {
        Optional<Map<String, Object>> details = assignmentService.getAssignmentDetails(id);
        if (details.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Assignment not found with ID: " + id, "TRN_002"));
        }
        return ResponseEntity.ok(details.get());
    }

    @Operation(summary = "Get Employee Training Details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee detail retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeTrainingDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/employee/{id}")
public ResponseEntity<?> getEmployeeDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManager(currentUser)) {
            // Check if employee is viewing their own details
            Long selfDbId = getEmployeeDbId(currentUser);
            if (selfDbId == null || !selfDbId.equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.error("Access Denied", "AUTH_002"));
            }
        }

        try {
            EmployeeTrainingDetailResponse detail = assignmentService.getEmployeeDetail(id);
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "TRN_003"));
        }
    }

    // ── C. Employee Training APIs ────────────────────────────────────────────
    @Operation(summary = "Get My Trainings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "My trainings retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MyTrainingItemResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my")
public ResponseEntity<?> getMyTrainings(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<Map<String, Object>> list = assignmentService.getMyTrainings(currentUser.getWorkEmail());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Update Training Progress")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Progress updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"message\": \"Progress updated successfully\", \"currentProgress\": 75, \"status\": \"IN_PROGRESS\"}"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PutMapping("/{assignmentId}/progress")
public ResponseEntity<?> updateProgress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long assignmentId,
            @Valid @RequestBody TrainingProgressUpdateRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        // Validate requester is updating their own progress, or is a manager
        Long selfDbId = getEmployeeDbId(currentUser);
        if (selfDbId == null || (!selfDbId.equals(request.getEmployeeId()) && !isManager(currentUser))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied", "AUTH_002"));
        }

        try {
            Map<String, Object> resp = assignmentService.updateProgress(assignmentId, request);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "TRN_004"));
        }
    }

    @Operation(summary = "Complete Training")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Training completed successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"message\": \"Training marked as completed successfully\", \"status\": \"COMPLETED\", \"progress\": 100}"))),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PutMapping("/{assignmentId}/complete")
public ResponseEntity<?> completeTraining(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long assignmentId,
            @RequestBody @Valid CompleteTrainingRequest body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Long employeeId = body != null ? body.employeeId() : null;
        if (employeeId == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Employee ID is required", "VAL_001"));
        }

        // Validate requester is completing their own training, or is a manager
        Long selfDbId = getEmployeeDbId(currentUser);
        if (selfDbId == null || (!selfDbId.equals(employeeId) && !isManager(currentUser))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied", "AUTH_002"));
        }

        try {
            Map<String, Object> resp = assignmentService.completeTraining(assignmentId, employeeId);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "TRN_005"));
        }
    }

    // ── D. Team Dashboard APIs (Manager UI) ──────────────────────────────────
    @Operation(summary = "Get Team Summary")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Team summary retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamSummaryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/team/summary")
public ResponseEntity<?> getTeamSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long managerId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManager(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied", "AUTH_002"));
        }

        Long resolvedManagerId = managerId;
        if (resolvedManagerId == null) {
            resolvedManagerId = getEmployeeDbId(currentUser);
        }

        if (resolvedManagerId == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Manager ID is required", "VAL_001"));
        }

        TeamSummaryResponse summary = assignmentService.getTeamSummary(resolvedManagerId);
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Get Team Progress")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Team progress retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamProgressListResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/team")
public ResponseEntity<?> getTeamProgress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long managerId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManager(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied", "AUTH_002"));
        }

        Long resolvedManagerId = managerId;
        if (resolvedManagerId == null) {
            resolvedManagerId = getEmployeeDbId(currentUser);
        }

        if (resolvedManagerId == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Manager ID is required", "VAL_001"));
        }

        Map<String, Object> progress = assignmentService.getTeamProgressList(resolvedManagerId);
        return ResponseEntity.ok(progress);
    }

    @Operation(summary = "Get Team Risk")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Team risk retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TeamRiskResponse.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/team/risk")
public ResponseEntity<?> getTeamRisk(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long managerId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isManager(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied", "AUTH_002"));
        }

        Long resolvedManagerId = managerId;
        if (resolvedManagerId == null) {
            resolvedManagerId = getEmployeeDbId(currentUser);
        }

        if (resolvedManagerId == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Manager ID is required", "VAL_001"));
        }

        List<TeamRiskResponse> risk = assignmentService.getTeamRiskList(resolvedManagerId);
        return ResponseEntity.ok(risk);
    }
}
