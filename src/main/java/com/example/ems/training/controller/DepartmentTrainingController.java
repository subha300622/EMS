package com.example.ems.training.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.dto.DepartmentProgressResponse;
import com.example.ems.training.dto.TrainingUnifiedAssignmentRequest;
import com.example.ems.training.entity.AssignmentTargetType;
import com.example.ems.training.entity.TrainingParticipant;
import com.example.ems.training.service.TrainingManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

    @PostMapping("/{departmentId}/trainings/{trainingId}")
    public ResponseEntity<?> assignTrainingToDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long departmentId,
            @PathVariable Long trainingId,
            @RequestBody(required = false) Map<String, Object> body) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            TrainingUnifiedAssignmentRequest req = new TrainingUnifiedAssignmentRequest();
            req.setAssignmentType(AssignmentTargetType.DEPARTMENT);
            req.setTargetIds(List.of(departmentId.toString()));
            req.setMandatory(
                    body != null && body.containsKey("mandatory") ? Boolean.TRUE.equals(body.get("mandatory")) : true);

            List<TrainingParticipant> assigned = trainingService.assignUnified(trainingId, req, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(assigned);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_DEPT_003"));
        }
    }

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

        List<Map<String, Object>> employees = employeeRepository.findByOrganizationId(orgId).stream()
                .filter(e -> e.getDepartment() != null && e.getDepartment().equalsIgnoreCase(deptName))
                .map(e -> Map.of(
                        "id", (Object) e.getId(),
                        "employeeId", e.getEmployeeId() != null ? e.getEmployeeId() : "",
                        "fullName", e.getFullName() != null ? e.getFullName() : "",
                        "email", e.getEmail() != null ? e.getEmail() : ""))
                .toList();

        return ResponseEntity.ok(employees);
    }

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
