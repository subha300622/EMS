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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Get my assigned trainings")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyTrainingsResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<?> getMyTrainings(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        MyTrainingsResponse response = trainingService.getMyTrainings(user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get my training details")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Training details retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Training.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Training not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Accept training invitation")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Training accepted successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingParticipant.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Decline training invitation")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Training declined successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingParticipant.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
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
