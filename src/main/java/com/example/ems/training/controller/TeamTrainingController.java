package com.example.ems.training.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Team;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.TeamRepository;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.dto.TeamProgressResponse;
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
@RequestMapping("/api/v1/training/teams")
@CrossOrigin("*")
@Tag(name = "Team Training Operations")
public class TeamTrainingController {

    @Autowired
    private TrainingManagementService trainingService;

    @Autowired
    private TeamRepository teamRepository;

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
    public ResponseEntity<?> getTeamsWithSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Long orgId = trainingService.resolveOrganizationId(user);
        List<Team> teams = teamRepository.findAll().stream()
                .filter(t -> t.getOrganization() != null && t.getOrganization().getId().equals(orgId)
                        && !Boolean.TRUE.equals(t.getDeleted()))
                .toList();

        List<TeamProgressResponse> summaries = teams.stream()
                .map(t -> trainingService.getTeamProgress(t.getId(), user))
                .toList();

        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<?> getTeamDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long teamId) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            TeamProgressResponse details = trainingService.getTeamProgress(teamId, user);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_TEAM_001"));
        }
    }

    @GetMapping("/{teamId}/trainings")
    public ResponseEntity<?> getTeamTrainings(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long teamId) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            TeamProgressResponse progress = trainingService.getTeamProgress(teamId, user);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_TEAM_002"));
        }
    }

    @PostMapping("/{teamId}/trainings/{trainingId}")
    public ResponseEntity<?> assignTrainingToTeam(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long teamId,
            @PathVariable Long trainingId,
            @RequestBody(required = false) @jakarta.validation.Valid com.example.ems.training.dto.TrainingAssignmentOptionsRequest body) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            TrainingUnifiedAssignmentRequest req = new TrainingUnifiedAssignmentRequest();
            req.setAssignmentType(AssignmentTargetType.TEAM);
            req.setTargetIds(List.of(teamId.toString()));
            req.setMandatory(body != null ? body.isMandatory() : true);

            List<TrainingParticipant> assigned = trainingService.assignUnified(trainingId, req, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(assigned);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_TEAM_003"));
        }
    }

    @DeleteMapping("/{teamId}/trainings/{trainingId}")
    public ResponseEntity<?> removeTeamTrainingAssignment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long teamId,
            @PathVariable Long trainingId) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            trainingService.deleteAssignmentScope(trainingId, AssignmentTargetType.TEAM, teamId.toString(), user);
            return ResponseEntity
                    .ok(Map.of("message", "Team scope removed and employee coverage re-evaluated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_TEAM_004"));
        }
    }

    @GetMapping("/{teamId}/employees")
    public ResponseEntity<?> getTeamEmployees(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long teamId) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Long orgId = trainingService.resolveOrganizationId(user);
        List<Map<String, Object>> employees = employeeRepository.findByOrganizationId(orgId).stream()
                .filter(e -> e.getTeam() != null && e.getTeam().getId().equals(teamId))
                .map(e -> Map.of(
                        "id", (Object) e.getId(),
                        "employeeId", e.getEmployeeId() != null ? e.getEmployeeId() : "",
                        "fullName", e.getFullName() != null ? e.getFullName() : "",
                        "email", e.getEmail() != null ? e.getEmail() : ""))
                .toList();

        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{teamId}/progress")
    public ResponseEntity<?> getTeamProgress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long teamId) {
        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            TeamProgressResponse progress = trainingService.getTeamProgress(teamId, user);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "TRN_TEAM_005"));
        }
    }
}
