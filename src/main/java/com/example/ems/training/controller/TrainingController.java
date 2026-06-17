package com.example.ems.training.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.dto.TrainingAssessmentRequest;
import com.example.ems.training.dto.TrainingAttendanceRequest;
import com.example.ems.training.dto.TrainingCertificateResponse;
import com.example.ems.training.dto.TrainingCourseRequest;
import com.example.ems.training.dto.TrainingCourseResponse;
import com.example.ems.training.dto.TrainingDashboardResponse;
import com.example.ems.training.dto.TrainingEnrollmentRequest;
import com.example.ems.training.dto.TrainingEnrollmentResponse;
import com.example.ems.training.dto.TrainingSessionRequest;
import com.example.ems.training.dto.TrainingSessionResponse;
import com.example.ems.training.service.TrainingService;
import com.example.ems.training.repository.TrainingEnrollmentRepository;
import com.example.ems.training.entity.TrainingEnrollment;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Training")
public class TrainingController {

    @Autowired private TrainingService trainingService;
    @Autowired private UserRepository userRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private JwtService jwtService;
    @Autowired private RoleService roleService;
    @Autowired private TrainingEnrollmentRepository trainingEnrollmentRepository;

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

    // ── Helper formatters to match client API requirements ────────────────────
    private String formatUtc(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.atZone(java.time.ZoneId.systemDefault())
                .withZoneSameInstant(java.time.ZoneOffset.UTC)
                .toInstant()
                .toString();
    }

    private Map<String, Object> formatCourseCreated(TrainingCourseResponse course) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("courseId", course.getId());
        m.put("title", course.getTitle());
        m.put("category", course.getCategory() != null ? course.getCategory().toUpperCase() : null);
        m.put("durationHours", course.getDurationHours());
        m.put("status", course.getStatus());
        m.put("createdAt", formatUtc(course.getCreatedAt()));
        return m;
    }

    private Map<String, Object> formatCourseSummary(TrainingCourseResponse course) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("courseId", course.getId());
        m.put("title", course.getTitle());
        m.put("category", course.getCategory() != null ? course.getCategory().toUpperCase() : null);
        m.put("durationHours", course.getDurationHours());
        m.put("status", course.getStatus());
        return m;
    }

    private Map<String, Object> formatCourseDetail(TrainingCourseResponse course) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("courseId", course.getId());
        m.put("title", course.getTitle());
        m.put("description", course.getDescription());
        m.put("category", course.getCategory() != null ? course.getCategory().toUpperCase() : null);
        m.put("durationHours", course.getDurationHours());
        m.put("status", course.getStatus());
        m.put("createdAt", formatUtc(course.getCreatedAt()));
        m.put("updatedAt", formatUtc(course.getUpdatedAt()));
        return m;
    }

    private Map<String, Object> formatCourseUpdate(TrainingCourseResponse course) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("courseId", course.getId());
        m.put("title", course.getTitle());
        m.put("status", course.getStatus());
        m.put("updatedAt", formatUtc(course.getUpdatedAt()));
        return m;
    }

    private Map<String, Object> formatSession(TrainingSessionResponse session) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("sessionId", session.getId());
        m.put("courseId", session.getCourseId());
        m.put("courseTitle", session.getCourseTitle());
        m.put("trainerName", session.getTrainerName());
        m.put("scheduleDate", session.getScheduleDate().toString());
        m.put("capacity", session.getCapacity());
        m.put("enrolledCount", session.getEnrolledCount());
        return m;
    }

    private Map<String, Object> formatEnrollment(TrainingEnrollmentResponse enrollment) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("enrollmentId", enrollment.getId());
        
        String empBusinessId = null;
        if (enrollment.getEmployeeEmail() != null) {
            empBusinessId = employeeRepository.findByEmail(enrollment.getEmployeeEmail())
                    .map(Employee::getEmployeeId)
                    .orElse(null);
        }
        m.put("employeeId", empBusinessId);
        m.put("sessionId", enrollment.getSessionId());
        m.put("status", enrollment.getStatus());
        m.put("enrollmentDate", formatUtc(enrollment.getEnrollmentDate()));
        return m;
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
                .body(ApiResponse.success("Training course created successfully", formatCourseCreated(response)));
    }

    @GetMapping("/trainings/courses")
    public ResponseEntity<?> getCourses(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<TrainingCourseResponse> list = trainingService.getCourses();
        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (TrainingCourseResponse course : list) {
            formattedList.add(formatCourseSummary(course));
        }
        return ResponseEntity.ok(ApiResponse.success("Training courses retrieved successfully", formattedList));
    }

    @GetMapping("/trainings/courses/{courseId}")
    public ResponseEntity<?> getCourseById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long courseId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<TrainingCourseResponse> course = trainingService.getCourseById(courseId);
        if (course.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Training course not found with ID: " + courseId, "TRN_001"));

        return ResponseEntity.ok(ApiResponse.success("Training course details retrieved successfully", formatCourseDetail(course.get())));
    }

    @PutMapping("/trainings/courses/{courseId}")
    public ResponseEntity<?> updateCourse(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long courseId,
            @Valid @RequestBody TrainingCourseRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Optional<TrainingCourseResponse> response = trainingService.updateCourse(courseId, request);
        if (response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Training course not found with ID: " + courseId, "TRN_001"));
        }
        return ResponseEntity.ok(ApiResponse.success("Training course updated successfully", formatCourseUpdate(response.get())));
    }

    @DeleteMapping("/trainings/courses/{courseId}")
    public ResponseEntity<?> deleteCourse(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long courseId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        boolean deleted = trainingService.deleteCourse(courseId);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Training course not found with ID: " + courseId, "TRN_001"));
        }
        
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("courseId", courseId);
        data.put("deleted", true);
        return ResponseEntity.ok(ApiResponse.success("Training course deleted successfully", data));
    }

    @PatchMapping("/trainings/courses/{courseId}/status")
    public ResponseEntity<?> updateCourseStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long courseId,
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

        Optional<TrainingCourseResponse> updated = trainingService.updateCourseStatus(courseId, status);
        if (updated.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Training course not found with ID: " + courseId, "TRN_001"));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("courseId", courseId);
        data.put("status", status.toUpperCase());
        return ResponseEntity.ok(ApiResponse.success("Course status updated successfully", data));
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
                    .body(ApiResponse.success("Training session scheduled successfully", formatSession(response)));
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

        List<TrainingSessionResponse> list = trainingService.getSessions();
        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (TrainingSessionResponse s : list) {
            formattedList.add(formatSession(s));
        }
        return ResponseEntity.ok(ApiResponse.success("Training sessions retrieved successfully", formattedList));
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
        if (!isManager(currentUser) && (currentUser.getEmployeeId() == null || !currentUser.getEmployeeId().equals(request.getEmployeeId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot enroll another employee.", "AUTH_002"));
        }

        try {
            TrainingEnrollmentResponse response = trainingService.enrollEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Employee enrolled successfully", formatEnrollment(response)));
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
        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (TrainingEnrollmentResponse enroll : list) {
            formattedList.add(formatEnrollment(enroll));
        }
        return ResponseEntity.ok(ApiResponse.success("My training enrollments retrieved successfully", formattedList));
    }

    @PatchMapping("/trainings/enrollments/{enrollmentId}/withdraw")
    public ResponseEntity<?> withdrawEnrollment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long enrollmentId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        // Scoping: allow manager, or the owner
        Optional<TrainingEnrollmentResponse> check = trainingService.getMyEnrollments(currentUser.getWorkEmail()).stream()
                .filter(e -> e.getId().equals(enrollmentId))
                .findFirst();

        if (!isManager(currentUser) && check.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot withdraw from this enrollment.", "AUTH_002"));
        }

        Optional<TrainingEnrollmentResponse> updated = trainingService.withdrawEnrollment(enrollmentId);
        if (updated.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Training enrollment not found with ID: " + enrollmentId, "TRN_003"));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enrollmentId", enrollmentId);
        data.put("status", "WITHDRAWN");
        return ResponseEntity.ok(ApiResponse.success("Withdrawn from training session successfully", data));
    }

    // ── 5. ATTENDANCE ────────────────────────────────────────────────────────
    @PostMapping("/trainings/sessions/{sessionId}/attendance")
    public ResponseEntity<?> submitAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long sessionId,
            @Valid @RequestBody TrainingAttendanceRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        try {
            Map<String, Object> result = trainingService.submitAttendance(sessionId, request);
            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("attendanceId", result.get("attendanceId"));
            responseData.put("employeeId", request.getEmployeeId());
            responseData.put("status", result.get("status"));
            responseData.put("progressPercent", result.get("progressPercent"));
            return ResponseEntity.ok(ApiResponse.success("Attendance submitted successfully", responseData));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_004"));
        }
    }

    // ── 6. ASSESSMENTS ───────────────────────────────────────────────────────
    @PostMapping("/trainings/assessments/{assessmentId}/submissions")
    public ResponseEntity<?> submitAssessment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long assessmentId,
            @Valid @RequestBody TrainingAssessmentRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions to grade assessments.", "AUTH_002"));

        try {
            Map<String, Object> result = trainingService.submitAssessment(assessmentId, request);
            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("submissionId", result.get("submissionId"));
            responseData.put("score", result.get("score"));
            
            String grade = (String) result.get("grade");
            if (grade != null && !"Fail".equalsIgnoreCase(grade)) {
                responseData.put("grade", grade);
            }
            
            String certNum = (String) result.get("certificateNumber");
            if (certNum != null && !"N/A".equalsIgnoreCase(certNum)) {
                responseData.put("certificateNumber", certNum);
            }
            
            return ResponseEntity.ok(ApiResponse.success("Assessment graded successfully", responseData));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_005"));
        }
    }

    // ── 7. CERTIFICATE ───────────────────────────────────────────────────────
    @GetMapping("/trainings/certificates/{enrollmentId}/download")
    public ResponseEntity<?> downloadCertificate(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long enrollmentId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<TrainingCertificateResponse> cert = trainingService.getCertificate(enrollmentId);
        if (cert.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Certificate not found for enrollment ID: " + enrollmentId, "TRN_006"));

        TrainingCertificateResponse certificate = cert.get();

        // Check permission: manager, or the owner
        if (!isManager(currentUser)) {
            Optional<Employee> empOpt = employeeRepository.findByEmail(currentUser.getWorkEmail());
            if (empOpt.isEmpty() || !empOpt.get().getFullName().equals(certificate.getEmployeeName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.error("Access Denied: You cannot download this certificate.", "AUTH_002"));
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("certificateNumber", certificate.getCertificateNumber());
        data.put("courseTitle", certificate.getCourseTitle());
        data.put("employeeName", certificate.getEmployeeName());
        data.put("issueDate", certificate.getIssueDate().toString());
        data.put("trainerName", certificate.getTrainerName());

        return ResponseEntity.ok(ApiResponse.success("Training certificate retrieved successfully", data));
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

    // ── 10. COMPLETE TRAINING (SELF-SERVICE) ─────────────────────────────────
    @PostMapping("/trainings/{id}/complete")
    public ResponseEntity<?> completeTrainingModule(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.training.complete")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.training.complete' permission.", "AUTH_002"));
        }

        TrainingEnrollment enrollment = trainingEnrollmentRepository.findById(id).orElse(null);
        if (enrollment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Training enrollment not found", "TR_001"));
        }

        Employee employee = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (employee == null || !enrollment.getEmployee().getId().equals(employee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You are not enrolled in this training.", "AUTH_002"));
        }

        // Simulate training completion via assessment submission with 100% score
        TrainingAssessmentRequest assessment = new TrainingAssessmentRequest();
        assessment.setScore(100);
        assessment.setFeedback("Completed via Self-Service");
        
        try {
            Map<String, Object> result = trainingService.submitAssessment(id, assessment);
            return ResponseEntity.ok(ApiResponse.success("Training module marked as completed", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TR_002"));
        }
    }
}
