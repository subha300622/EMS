package com.example.ems.training.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.dto.*;
import com.example.ems.training.entity.*;
import com.example.ems.training.service.TrainingManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trainings")
@CrossOrigin("*")
@Tag(name = "Training Operations")
public class TrainingController {

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

    // ── Training Lifecycle & CRUD ────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createTraining(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody TrainingCreateRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            Training training = trainingService.createTraining(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(training);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_001"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getTrainings(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) TrainingStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long trainerId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        List<Training> list = trainingService.getTrainingsWithFilters(status, category, trainerId, user);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{trainingId}")
    public ResponseEntity<?> getTrainingById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            Training training = trainingService.getTrainingById(trainingId, user);
            return ResponseEntity.ok(training);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "TRN_002"));
        }
    }

    @PutMapping("/{trainingId}")
    public ResponseEntity<?> updateTraining(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @Valid @RequestBody TrainingCreateRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            Training updated = trainingService.updateTraining(trainingId, request, user);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_003"));
        }
    }

    @PostMapping("/{trainingId}/submit")
    public ResponseEntity<?> submitForApproval(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            Training training = trainingService.submitForApproval(trainingId, user);
            return ResponseEntity.ok(training);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_004"));
        }
    }

    @PostMapping("/{trainingId}/approve")
    public ResponseEntity<?> approveTraining(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @RequestBody(required = false) TrainingApprovalRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            String comment = request != null ? request.getComment() : null;
            Training training = trainingService.approveTraining(trainingId, comment, user);
            return ResponseEntity.ok(training);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_005"));
        }
    }

    @PostMapping("/{trainingId}/reject")
    public ResponseEntity<?> rejectTraining(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @RequestBody(required = false) TrainingApprovalRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            String comment = request != null ? request.getComment() : null;
            Training training = trainingService.rejectTraining(trainingId, comment, user);
            return ResponseEntity.ok(training);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_006"));
        }
    }

    @PostMapping("/{trainingId}/send-back")
    public ResponseEntity<?> sendBackTraining(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @RequestBody(required = false) TrainingApprovalRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            String comment = request != null ? request.getComment() : null;
            Training training = trainingService.sendBackTraining(trainingId, comment, user);
            return ResponseEntity.ok(training);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_007"));
        }
    }

    @PostMapping("/{trainingId}/publish")
    public ResponseEntity<?> publishTraining(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            Training training = trainingService.publishTraining(trainingId, user);
            return ResponseEntity.ok(training);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_008"));
        }
    }

    @PostMapping("/{trainingId}/cancel")
    public ResponseEntity<?> cancelTraining(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @RequestBody(required = false) TrainingApprovalRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            String comment = request != null ? request.getComment() : null;
            Training training = trainingService.cancelTraining(trainingId, comment, user);
            return ResponseEntity.ok(training);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_009"));
        }
    }

    // ── Participants & Assignments ───────────────────────────────────────────
    @PostMapping("/{trainingId}/assignments")
    public ResponseEntity<?> assignUnified(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @Valid @RequestBody TrainingUnifiedAssignmentRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            List<TrainingParticipant> assigned = trainingService.assignUnified(trainingId, request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(assigned);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_010"));
        }
    }

    @PostMapping("/{trainingId}/participants")
    public ResponseEntity<?> assignParticipants(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @Valid @RequestBody ParticipantAssignRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            List<TrainingParticipant> assigned = trainingService.assignParticipants(trainingId, request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(assigned);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_010"));
        }
    }

    @GetMapping("/{trainingId}/participants")
    public ResponseEntity<?> getParticipants(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            List<TrainingParticipant> participants = trainingService.getParticipants(trainingId, user);
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_011"));
        }
    }

    @DeleteMapping("/{trainingId}/participants/{employeeId}")
    public ResponseEntity<?> removeParticipant(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @PathVariable Long employeeId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            trainingService.removeParticipant(trainingId, employeeId, user);
            return ResponseEntity.ok(Map.of("message", "Participant removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_012"));
        }
    }

    // ── Attendance ───────────────────────────────────────────────────────────
    @PostMapping("/{trainingId}/attendance")
    public ResponseEntity<?> recordAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @Valid @RequestBody AttendanceBulkMarkRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            List<TrainingAttendance> records = trainingService.bulkRecordAttendance(trainingId, request, user);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_013"));
        }
    }

    @GetMapping("/{trainingId}/attendance")
    public ResponseEntity<?> getAttendanceRecords(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            List<TrainingAttendance> records = trainingService.getAttendanceRecords(trainingId, user);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_014"));
        }
    }

    @PatchMapping("/{trainingId}/participants/{employeeId}/attendance")
    public ResponseEntity<?> updateParticipantAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @PathVariable Long employeeId,
            @RequestParam AttendanceStatus status,
            @RequestParam(required = false) String remarks) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            TrainingAttendance updated = trainingService.updateParticipantAttendance(trainingId, employeeId, status, remarks, user);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_015"));
        }
    }

    // ── Materials ────────────────────────────────────────────────────────────
    @PostMapping("/{trainingId}/materials")
    public ResponseEntity<?> addMaterial(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @Valid @RequestBody MaterialCreateRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            TrainingMaterial material = trainingService.addMaterial(trainingId, request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(material);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_016"));
        }
    }

    @GetMapping("/{trainingId}/materials")
    public ResponseEntity<?> getMaterials(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            List<TrainingMaterial> materials = trainingService.getMaterials(trainingId, user);
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_017"));
        }
    }

    @DeleteMapping("/{trainingId}/materials/{materialId}")
    public ResponseEntity<?> deleteMaterial(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @PathVariable Long materialId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            trainingService.deleteMaterial(trainingId, materialId, user);
            return ResponseEntity.ok(Map.of("message", "Material deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_018"));
        }
    }

    // ── Feedback ─────────────────────────────────────────────────────────────
    @PostMapping("/{trainingId}/feedback")
    public ResponseEntity<?> submitFeedback(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @Valid @RequestBody FeedbackSubmitRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            TrainingFeedback feedback = trainingService.submitFeedback(trainingId, request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(feedback);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_019"));
        }
    }

    @GetMapping("/{trainingId}/feedback")
    public ResponseEntity<?> getFeedbackSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            Map<String, Object> summary = trainingService.getFeedbackSummary(trainingId, user);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_020"));
        }
    }

    // ── Calendar View ────────────────────────────────────────────────────────
    @GetMapping("/calendar")
    public ResponseEntity<?> getCalendarEvents(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            List<CalendarEventResponse> events = trainingService.getCalendarEvents(startDate, endDate, user);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_021"));
        }
    }
}
