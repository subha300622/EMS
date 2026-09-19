package com.example.ems.employee.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.dto.TeamDtos;
import com.example.ems.employee.entity.TeamAuditLog;
import com.example.ems.employee.service.TeamService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Team Management")
public class TeamController {

    @Autowired
    private TeamService teamService;

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

    // 1. Create Team
    @Operation(summary = "Create Team", description = "Creates a new team within the user's organization with optional department association and optional team lead.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Team created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDtos.TeamResponseDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/teams")
public ResponseEntity<?> createTeam(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody TeamDtos.TeamCreateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamResponseDto created = teamService.createTeam(request, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Team created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_001"));
        }
    }

    // 2. List Teams
    @Operation(summary = "List Teams", description = "Retrieves teams belonging to the user's organization with search, status, department filtering, and pagination.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Teams retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TeamDtos.TeamResponseDto.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/teams")
public ResponseEntity<?> listTeams(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Page<TeamDtos.TeamResponseDto> teams = teamService.listTeams(search, status, departmentId, page, size, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Teams retrieved successfully", teams));
    }

    // 3. Get Team Details
    @Operation(summary = "Get Team Details", description = "Retrieves details of a specific team by ID.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDtos.TeamResponseDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/teams/{teamId}")
public ResponseEntity<?> getTeam(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamResponseDto team = teamService.getTeam(teamId, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Team retrieved successfully", team));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_404"));
        }
    }

    // 4. Update Team
    @Operation(summary = "Update Team", description = "Updates team details, department association (or null), and team lead.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDtos.TeamResponseDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/teams/{teamId}")
public ResponseEntity<?> updateTeam(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamDtos.TeamUpdateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamResponseDto updated = teamService.updateTeam(teamId, request, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Team updated successfully", updated));
        } catch (TeamService.DepartmentMismatchException e) {
            Map<String, Object> errBody = new LinkedHashMap<>();
            errBody.put("code", e.getCode());
            errBody.put("message", e.getMessage());
            errBody.put("details", e.getDetails());
            return ResponseEntity.badRequest().body(errBody);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_002"));
        }
    }

    // 5. Delete Team
    @Operation(summary = "Delete Team", description = "Soft deletes a team if no active members exist. Fails with TEAM_HAS_ACTIVE_MEMBERS if active members remain.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found")
    })
    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<?> deleteTeam(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            teamService.deleteTeam(teamId, currentUser);
            Map<String, String> response = new LinkedHashMap<>();
            response.put("teamId", String.valueOf(teamId));
            response.put("status", "DELETED");
            return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", response));
        } catch (TeamService.ActiveMembersExistException e) {
            Map<String, String> errBody = new LinkedHashMap<>();
            errBody.put("code", e.getCode());
            errBody.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errBody);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_404"));
        }
    }

    // 6. Change Team Status
    @Operation(summary = "Change Team Status", description = "Updates team status to ACTIVE or INACTIVE.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team status updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDtos.TeamResponseDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/teams/{teamId}/status")
public ResponseEntity<?> changeTeamStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamDtos.TeamStatusUpdateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamResponseDto updated = teamService.changeTeamStatus(teamId, request.getStatus(), currentUser);
            return ResponseEntity.ok(ApiResponse.success("Team status updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_003"));
        }
    }

    // 7. Assign Department to Team
    @Operation(summary = "Assign/Remove Department to/from Team", description = "Assigns a department (or null) to a team after checking member compatibility.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department assigned to team successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDtos.TeamResponseDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/teams/{teamId}/department")
public ResponseEntity<?> assignDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamDtos.TeamDepartmentUpdateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamResponseDto updated = teamService.assignDepartment(teamId, request.getDepartmentId(), currentUser);
            return ResponseEntity.ok(ApiResponse.success("Department assigned to team successfully", updated));
        } catch (TeamService.DepartmentMismatchException e) {
            Map<String, Object> errBody = new LinkedHashMap<>();
            errBody.put("code", e.getCode());
            errBody.put("message", e.getMessage());
            errBody.put("details", e.getDetails());
            return ResponseEntity.badRequest().body(errBody);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_004"));
        }
    }

    // 8. Change Team Lead
    @Operation(summary = "Change Team Lead", description = "Promotes an existing active team member to Team Lead (or null to remove lead).")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team lead updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDtos.TeamResponseDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/teams/{teamId}/team-lead")
public ResponseEntity<?> changeTeamLead(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamDtos.TeamLeadUpdateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamResponseDto updated = teamService.changeTeamLead(teamId, request.getEmployeeId(), currentUser);
            return ResponseEntity.ok(ApiResponse.success("Team lead updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_005"));
        }
    }

    // 9. Get Team Members
    @Operation(summary = "Get Team Members", description = "Retrieves active members of a specific team.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team members retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDtos.TeamMemberListResponseDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/teams/{teamId}/members")
public ResponseEntity<?> getTeamMembers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamMemberListResponseDto members = teamService.getTeamMembers(teamId, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Team members retrieved successfully", members));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_404"));
        }
    }

    // 10. Add Single Employee to Team
    @Operation(summary = "Add Employee to Team", description = "Adds an active employee to an active team.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Employee added to team successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDtos.MemberDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/teams/{teamId}/members")
public ResponseEntity<?> addMember(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamDtos.TeamMemberAddRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.MemberDto member = teamService.addMember(teamId, request.getEmployeeId(), currentUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Employee added to team successfully", member));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_006"));
        }
    }

    // 11. Bulk Add Employees to Team
    @Operation(summary = "Bulk Add Employees to Team", description = "Adds multiple employees to a team with partial success status reporting.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bulk member addition processed",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDtos.TeamMemberBulkAddResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/teams/{teamId}/members/bulk")
public ResponseEntity<?> bulkAddMembers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamDtos.TeamMemberBulkAddRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamMemberBulkAddResponse response = teamService.bulkAddMembers(teamId, request.getEmployeeIds(), currentUser);
            return ResponseEntity.ok(ApiResponse.success("Bulk member addition processed", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_007"));
        }
    }

    // 12. Remove Employee from Team
    @Operation(summary = "Remove Employee from Team", description = "Removes an employee from a team (unless employee is current Team Lead).")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee removed from team successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/teams/{teamId}/members/{employeeId}")
    public ResponseEntity<?> removeMember(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId,
            @PathVariable("employeeId") Long employeeId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            teamService.removeMember(teamId, employeeId, currentUser);
            Map<String, String> response = new LinkedHashMap<>();
            response.put("teamId", String.valueOf(teamId));
            response.put("employeeId", String.valueOf(employeeId));
            response.put("status", "REMOVED");
            return ResponseEntity.ok(ApiResponse.success("Employee removed from team successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_008"));
        }
    }

    // 13. Get Teams by Department
    @Operation(summary = "Get Teams by Department", description = "Retrieves teams belonging to a specific department.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department teams retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TeamDtos.TeamResponseDto.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/departments/{departmentId}/teams")
public ResponseEntity<?> getTeamsByDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("departmentId") Long departmentId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            List<TeamDtos.TeamResponseDto> teams = teamService.getTeamsByDepartment(departmentId, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Department teams retrieved successfully", teams));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DEP_404"));
        }
    }

    // 14. Get Team Audit Logs
    @Operation(summary = "Get Team Audit Logs", description = "Retrieves audit history log of changes for a specific team.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team audit logs retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TeamAuditLog.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/teams/{teamId}/audit-logs")
public ResponseEntity<?> getAuditLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            List<TeamAuditLog> logs = teamService.getAuditLogs(teamId, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Team audit logs retrieved successfully", logs));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_404"));
        }
    }
}
