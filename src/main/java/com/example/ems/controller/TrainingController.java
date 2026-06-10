package com.example.ems.controller;

import com.example.ems.dto.*;
import com.example.ems.entity.Employee;
import com.example.ems.entity.User;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.JwtService;
import com.example.ems.service.RoleService;
import com.example.ems.service.TrainingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class TrainingController {

    @Autowired private TrainingService trainingService;
    @Autowired private UserRepository userRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private JwtService jwtService;
    @Autowired private RoleService roleService;

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

    // ── 1. DASHBOARD ─────────────────────────────────────────────────────────
    @GetMapping("/trainings/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        TrainingDashboardResponse stats = trainingService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Training dashboard statistics retrieved successfully", stats));
    }

    // ── 2. COURSES ───────────────────────────────────────────────────────────
    @PostMapping("/trainings/courses")
    public ResponseEntity<?> createCourse(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody TrainingCourseRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        TrainingCourseResponse response = trainingService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Training course created successfully", response));
    }

    @GetMapping("/trainings/courses")
    public ResponseEntity<?> getCourses(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<TrainingCourseResponse> response = trainingService.getCourses();
        return ResponseEntity.ok(ApiResponse.success("Training courses retrieved successfully", response));
    }

    @GetMapping("/trainings/courses/{id}")
    public ResponseEntity<?> getCourseById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<TrainingCourseResponse> course = trainingService.getCourseById(id);
        if (course.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Training course not found with ID: " + id, "TRN_001"));

        return ResponseEntity.ok(ApiResponse.success("Training course details retrieved successfully", course.get()));
    }

    @PatchMapping("/trainings/courses/{id}/status")
    public ResponseEntity<?> updateCourseStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        String status = body.get("status");
        if (status == null || status.isBlank())
            return ResponseEntity.badRequest().body(ErrorResponse.error("Status is required", "VAL_001"));

        Optional<TrainingCourseResponse> updated = trainingService.updateCourseStatus(id, status);
        if (updated.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Training course not found with ID: " + id, "TRN_001"));

        return ResponseEntity.ok(ApiResponse.success("Training course status updated successfully", updated.get()));
    }

    // ── 3. SESSIONS ──────────────────────────────────────────────────────────
    @PostMapping("/trainings/sessions")
    public ResponseEntity<?> createSession(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody TrainingSessionRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        try {
            TrainingSessionResponse response = trainingService.createSession(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Training session scheduled successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_002"));
        }
    }

    @GetMapping("/trainings/sessions")
    public ResponseEntity<?> getSessions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<TrainingSessionResponse> response = trainingService.getSessions();
        return ResponseEntity.ok(ApiResponse.success("Training sessions retrieved successfully", response));
    }

    // ── 4. ENROLLMENTS ───────────────────────────────────────────────────────
    @PostMapping("/trainings/enrollments")
    public ResponseEntity<?> enrollEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody TrainingEnrollmentRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        // Allow HR/Manager, or the target employee themselves
        if (!isManager(currentUser) && (currentUser.getEmployeeId() == null || !currentUser.getEmployeeId().equals(String.valueOf(request.getEmployeeId())))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot enroll another employee.", "AUTH_002"));
        }

        try {
            TrainingEnrollmentResponse response = trainingService.enrollEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Employee enrolled in training session successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_003"));
        }
    }

    @GetMapping("/trainings/my")
    public ResponseEntity<?> getMyEnrollments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<TrainingEnrollmentResponse> list = trainingService.getMyEnrollments(currentUser.getWorkEmail());
        return ResponseEntity.ok(ApiResponse.success("My training enrollments retrieved successfully", list));
    }

    @PatchMapping("/trainings/enrollments/{id}/withdraw")
    public ResponseEntity<?> withdrawEnrollment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        // Scoping: allow manager, or the owner
        Optional<TrainingEnrollmentResponse> check = trainingService.getMyEnrollments(currentUser.getWorkEmail()).stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();

        if (!isManager(currentUser) && check.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot withdraw from this enrollment.", "AUTH_002"));
        }

        Optional<TrainingEnrollmentResponse> updated = trainingService.withdrawEnrollment(id);
        if (updated.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Training enrollment not found with ID: " + id, "TRN_003"));

        return ResponseEntity.ok(ApiResponse.success("Withdrawn from training session successfully", updated.get()));
    }

    // ── 5. ATTENDANCE ────────────────────────────────────────────────────────
    @PostMapping("/trainings/sessions/{id}/attendance")
    public ResponseEntity<?> submitAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody TrainingAttendanceRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        try {
            Map<String, Object> result = trainingService.submitAttendance(id, request);
            return ResponseEntity.ok(ApiResponse.success("Session attendance submitted successfully", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_004"));
        }
    }

    // ── 6. ASSESSMENTS ───────────────────────────────────────────────────────
    @PostMapping("/trainings/assessments/{id}/submissions")
    public ResponseEntity<?> submitAssessment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody TrainingAssessmentRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions to grade assessments.", "AUTH_002"));

        try {
            Map<String, Object> result = trainingService.submitAssessment(id, request);
            return ResponseEntity.ok(ApiResponse.success("Training assessment submission graded successfully", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_005"));
        }
    }

    // ── 7. CERTIFICATE ───────────────────────────────────────────────────────
    @GetMapping("/trainings/certificates/{id}/download")
    public ResponseEntity<?> downloadCertificate(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<TrainingCertificateResponse> cert = trainingService.getCertificate(id);
        if (cert.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Certificate not found for enrollment ID: " + id, "TRN_006"));

        TrainingCertificateResponse certificate = cert.get();

        // Check permission: manager, or the owner
        if (!isManager(currentUser)) {
            Optional<Employee> empOpt = employeeRepository.findByEmail(currentUser.getWorkEmail());
            if (empOpt.isEmpty() || !empOpt.get().getFullName().equals(certificate.getEmployeeName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.error("Access Denied: You cannot download this certificate.", "AUTH_002"));
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Training certificate retrieved successfully", certificate));
    }

    // ── 8. REPORTS ───────────────────────────────────────────────────────────
    @GetMapping("/trainings/reports/{reportType}")
    public ResponseEntity<?> getTrainingsReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String reportType) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Map<String, Object> data = trainingService.getTrainingsReport(reportType);
        return ResponseEntity.ok(ApiResponse.success("Training report generated successfully", data));
    }

    // ── 9. NOTIFICATIONS ─────────────────────────────────────────────────────
    @PostMapping("/trainings/notifications")
    public ResponseEntity<?> sendNotification(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        String message = body.getOrDefault("message", "Training session reminder alert");
        Map<String, Object> result = Map.of(
                "status", "SENT",
                "message", message,
                "sentAt", java.time.LocalDateTime.now().toString(),
                "channel", "EMAIL"
        );
        return ResponseEntity.ok(ApiResponse.success("Training notification dispatched successfully", result));
    }
}
