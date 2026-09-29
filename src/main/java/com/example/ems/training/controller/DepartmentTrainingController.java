package com.example.ems.training.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.dto.DepartmentProgressResponse;
import com.example.ems.training.dto.TrainingAssignmentOptionsRequest;
import com.example.ems.training.dto.TrainingEmployeeSummaryResponse;
import com.example.ems.training.dto.TrainingUnifiedAssignmentRequest;
import com.example.ems.training.entity.AssignmentTargetType;
import com.example.ems.training.entity.TrainingParticipant;
import com.example.ems.training.service.TrainingManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/training/departments")
@CrossOrigin("*")
@Tag(name = "Department Training Operations")
public class DepartmentTrainingController {

    @Autowired
    private TrainingManagementService trainingService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

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

    @Operation(summary = "Get all departments with training summary")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department summaries retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DepartmentProgressResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<?> getDepartmentsWithSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Long orgId = trainingService.resolveOrganizationId(user);
        List<Department> departments = departmentRepository.findAll().stream()
                .filter(d -> d.getOrganization() != null && d.getOrganization().getId().equals(orgId))
                .toList();

        List<DepartmentProgressResponse> summaries = departments.stream()
                .map(d -> trainingService.getDepartmentProgress(d.getId(), user))
                .toList();

        return ResponseEntity.ok(summaries);
    }

    @Operation(summary = "Get department training details")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department details retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartmentProgressResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{departmentId}")
    public ResponseEntity<?> getDepartmentDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long departmentId) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            DepartmentProgressResponse details = trainingService.getDepartmentProgress(departmentId, user);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_DEPT_001"));
        }
    }

    @Operation(summary = "Get department trainings")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department trainings retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartmentProgressResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{departmentId}/trainings")
    public ResponseEntity<?> getDepartmentTrainings(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long departmentId) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            DepartmentProgressResponse progress = trainingService.getDepartmentProgress(departmentId, user);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_DEPT_002"));
        }
    }

    @Operation(summary = "Assign training to department")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Training assigned successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TrainingParticipant.class)))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{departmentId}/trainings/{trainingId}")
    public ResponseEntity<?> assignTrainingToDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long departmentId,
            @PathVariable Long trainingId,
            @RequestBody(required = false) @Valid TrainingAssignmentOptionsRequest body) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            TrainingUnifiedAssignmentRequest req = new TrainingUnifiedAssignmentRequest();
            req.setAssignmentType(AssignmentTargetType.DEPARTMENT);
            req.setTargetIds(List.of(departmentId.toString()));
            req.setMandatory(body != null ? body.isMandatory() : true);

            List<TrainingParticipant> assigned = trainingService.assignUnified(trainingId, req, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(assigned);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_DEPT_003"));
        }
    }

    @Operation(summary = "Remove department training assignment")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Training assignment removed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Department scope removed and employee coverage re-evaluated successfully\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{departmentId}/trainings/{trainingId}")
    public ResponseEntity<?> removeDepartmentTrainingAssignment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long departmentId,
            @PathVariable Long trainingId) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            trainingService.deleteAssignmentScope(trainingId, AssignmentTargetType.DEPARTMENT, departmentId.toString(),
                    user);
            return ResponseEntity
                    .ok(Map.of("message", "Department scope removed and employee coverage re-evaluated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_DEPT_004"));
        }
    }

    @Operation(summary = "Get employees in department for training")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department employees retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TrainingEmployeeSummaryResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{departmentId}/employees")
    public ResponseEntity<?> getDepartmentEmployees(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long departmentId) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Long orgId = trainingService.resolveOrganizationId(user);
        Department dept = departmentRepository.findById(departmentId).orElse(null);
        String deptName = dept != null ? dept.getName() : "";

        List<TrainingEmployeeSummaryResponse> employees = employeeRepository.findByOrganizationId(orgId).stream()
                .filter(e -> e.getDepartment() != null && e.getDepartment().equalsIgnoreCase(deptName))
                .map(e -> new TrainingEmployeeSummaryResponse(
                        e.getId(),
                        e.getEmployeeId() != null ? e.getEmployeeId() : "",
                        e.getFullName() != null ? e.getFullName() : "",
                        e.getEmail() != null ? e.getEmail() : ""))
                .toList();

        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Get department training progress")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department progress retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartmentProgressResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{departmentId}/progress")
    public ResponseEntity<?> getDepartmentProgress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long departmentId) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            DepartmentProgressResponse progress = trainingService.getDepartmentProgress(departmentId, user);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_DEPT_005"));
        }
    }
}
