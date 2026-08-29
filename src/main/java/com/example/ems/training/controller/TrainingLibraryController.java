package com.example.ems.training.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.dto.LibraryResourceCreateRequest;
import com.example.ems.training.entity.TrainingLibraryResource;
import com.example.ems.training.service.TrainingManagementService;
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
@RequestMapping("/api/v1/training-library")
@CrossOrigin("*")
@Tag(name = "Training Library Resources")
public class TrainingLibraryController {

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

    @PostMapping
    public ResponseEntity<?> createResource(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody LibraryResourceCreateRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            TrainingLibraryResource resource = trainingService.createLibraryResource(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_025"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getResources(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String technology,
            @RequestParam(required = false) Long trainerId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        List<TrainingLibraryResource> resources = trainingService.getLibraryResources(category, technology, trainerId, user);
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{resourceId}")
    public ResponseEntity<?> updateResource(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long resourceId,
            @Valid @RequestBody LibraryResourceCreateRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            TrainingLibraryResource updated = trainingService.updateLibraryResource(resourceId, request, user);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_026"));
        }
    }

    @DeleteMapping("/{resourceId}")
    public ResponseEntity<?> deleteResource(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long resourceId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        try {
            trainingService.deleteLibraryResource(resourceId, user);
            return ResponseEntity.ok(Map.of("message", "Library resource deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_027"));
        }
    }
}
