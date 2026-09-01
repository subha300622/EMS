package com.example.ems.training.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.dto.MyTrainingsResponse;
import com.example.ems.training.dto.ParticipantResponseRequest;
import com.example.ems.training.entity.ParticipationStatus;
import com.example.ems.training.entity.Training;
import com.example.ems.training.entity.TrainingParticipant;
import com.example.ems.training.service.TrainingManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/my-training", "/api/v1/my/trainings"})
@CrossOrigin("*")
@Tag(name = "Employee Self-Service Trainings")
public class MyTrainingController {

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

    @GetMapping
    public ResponseEntity<?> getMyTrainings(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        MyTrainingsResponse response = trainingService.getMyTrainings(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{trainingId}")
    public ResponseEntity<?> getMyTrainingDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            Training training = trainingService.getTrainingById(trainingId, user);
            return ResponseEntity.ok(training);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "TRN_022"));
        }
    }

    @PostMapping("/{trainingId}/accept")
    public ResponseEntity<?> acceptTraining(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @RequestBody(required = false) ParticipantResponseRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            String note = request != null ? request.getNote() : null;
            TrainingParticipant participant = trainingService.recordParticipantResponse(trainingId, ParticipationStatus.ACCEPTED, note, user);
            return ResponseEntity.ok(participant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_023"));
        }
    }

    @PostMapping("/{trainingId}/decline")
    public ResponseEntity<?> declineTraining(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long trainingId,
            @RequestBody(required = false) ParticipantResponseRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            String note = request != null ? request.getNote() : null;
            TrainingParticipant participant = trainingService.recordParticipantResponse(trainingId, ParticipationStatus.DECLINED, note, user);
            return ResponseEntity.ok(participant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_024"));
        }
    }
}
