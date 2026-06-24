package com.example.ems.onboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.onboarding.dto.OnboardingTaskResponse;
import com.example.ems.onboarding.entity.OnboardingTask;
import com.example.ems.onboarding.repository.OnboardingTaskRepository;
import com.example.ems.onboarding.service.OnboardingService;
import com.example.ems.onboarding.service.TeamOnboardingService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
@CrossOrigin("*")
@Tag(name = "Canonical Tasks Service")
public class TaskController {

    @Autowired
    private TeamOnboardingService teamOnboardingService;

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private OnboardingTaskRepository onboardingTaskRepository;

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

    @PutMapping("/{taskId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateTask(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> body) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String status = (String) body.get("status");
        if (status == null || status.isBlank()) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("status field is required", "VAL_001"));
        }

        try {
            if ("COMPLETED".equalsIgnoreCase(status)) {
                // If completedBy is not provided in body, default to 1L or resolved user's ID
                Long completedBy = 1L;
                if (body.containsKey("completedBy")) {
                    completedBy = ((Number) body.get("completedBy")).longValue();
                }
                teamOnboardingService.completeTask(taskId, completedBy);
            } else {
                onboardingService.updateTaskStatus(taskId, status);
            }

            OnboardingTask task = onboardingTaskRepository.findById(taskId).orElse(null);
            if (task == null) {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Task not found with ID: " + taskId, "ONB_004"));
            }
            return ResponseEntity.ok(ApiResponse.success("Task status updated successfully", new OnboardingTaskResponse(task)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "ONB_004"));
        }
    }
}
