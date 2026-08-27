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
    @PostMapping("/teams")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<TeamDtos.TeamResponseDto>> createTeam(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody TeamDtos.TeamCreateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamResponseDto created = teamService.createTeam(request, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Team created successfully", created));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_001"));
        }
    }

    // 2. List Teams
    @Operation(summary = "List Teams", description = "Retrieves teams belonging to the user's organization with search, status, department filtering, and pagination.")
    @GetMapping("/teams")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Page<TeamDtos.TeamResponseDto>>> listTeams(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Page<TeamDtos.TeamResponseDto> teams = teamService.listTeams(search, status, departmentId, page, size, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Teams retrieved successfully", teams));
    }

    // 3. Get Team Details
    @Operation(summary = "Get Team Details", description = "Retrieves details of a specific team by ID.")
    @GetMapping("/teams/{teamId}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<TeamDtos.TeamResponseDto>> getTeam(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamResponseDto team = teamService.getTeam(teamId, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Team retrieved successfully", team));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_404"));
        }
    }

    // 4. Update Team
    @Operation(summary = "Update Team", description = "Updates team details, department association (or null), and team lead.")
    @PutMapping("/teams/{teamId}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<TeamDtos.TeamResponseDto>> updateTeam(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamDtos.TeamUpdateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
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
            return (ResponseEntity) ResponseEntity.badRequest().body(errBody);
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_002"));
        }
    }

    // 5. Delete Team
    @Operation(summary = "Delete Team", description = "Soft deletes a team if no active members exist. Fails with TEAM_HAS_ACTIVE_MEMBERS if active members remain.")
    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<Object> deleteTeam(
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
    @PatchMapping("/teams/{teamId}/status")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<TeamDtos.TeamResponseDto>> changeTeamStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamDtos.TeamStatusUpdateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamResponseDto updated = teamService.changeTeamStatus(teamId, request.getStatus(), currentUser);
            return ResponseEntity.ok(ApiResponse.success("Team status updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_003"));
        }
    }

    // 7. Assign Department to Team
    @Operation(summary = "Assign/Remove Department to/from Team", description = "Assigns a department (or null) to a team after checking member compatibility.")
    @PatchMapping("/teams/{teamId}/department")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<Object> assignDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamDtos.TeamDepartmentUpdateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
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
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_004"));
        }
    }

    // 8. Change Team Lead
    @Operation(summary = "Change Team Lead", description = "Promotes an existing active team member to Team Lead (or null to remove lead).")
    @PatchMapping("/teams/{teamId}/team-lead")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<TeamDtos.TeamResponseDto>> changeTeamLead(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamDtos.TeamLeadUpdateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamResponseDto updated = teamService.changeTeamLead(teamId, request.getEmployeeId(), currentUser);
            return ResponseEntity.ok(ApiResponse.success("Team lead updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_005"));
        }
    }

    // 9. Get Team Members
    @Operation(summary = "Get Team Members", description = "Retrieves active members of a specific team.")
    @GetMapping("/teams/{teamId}/members")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<TeamDtos.TeamMemberListResponseDto>> getTeamMembers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamMemberListResponseDto members = teamService.getTeamMembers(teamId, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Team members retrieved successfully", members));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_404"));
        }
    }

    // 10. Add Single Employee to Team
    @Operation(summary = "Add Employee to Team", description = "Adds an active employee to an active team.")
    @PostMapping("/teams/{teamId}/members")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<TeamDtos.MemberDto>> addMember(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamDtos.TeamMemberAddRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.MemberDto member = teamService.addMember(teamId, request.getEmployeeId(), currentUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Employee added to team successfully", member));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_006"));
        }
    }

    // 11. Bulk Add Employees to Team
    @Operation(summary = "Bulk Add Employees to Team", description = "Adds multiple employees to a team with partial success status reporting.")
    @PostMapping("/teams/{teamId}/members/bulk")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<TeamDtos.TeamMemberBulkAddResponse>> bulkAddMembers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamDtos.TeamMemberBulkAddRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            TeamDtos.TeamMemberBulkAddResponse response = teamService.bulkAddMembers(teamId, request.getEmployeeIds(), currentUser);
            return ResponseEntity.ok(ApiResponse.success("Bulk member addition processed", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_007"));
        }
    }

    // 12. Remove Employee from Team
    @Operation(summary = "Remove Employee from Team", description = "Removes an employee from a team (unless employee is current Team Lead).")
    @DeleteMapping("/teams/{teamId}/members/{employeeId}")
    public ResponseEntity<Object> removeMember(
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
    @GetMapping("/departments/{departmentId}/teams")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<List<TeamDtos.TeamResponseDto>>> getTeamsByDepartment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("departmentId") Long departmentId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            List<TeamDtos.TeamResponseDto> teams = teamService.getTeamsByDepartment(departmentId, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Department teams retrieved successfully", teams));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DEP_404"));
        }
    }

    // 14. Get Team Audit Logs
    @Operation(summary = "Get Team Audit Logs", description = "Retrieves audit history log of changes for a specific team.")
    @GetMapping("/teams/{teamId}/audit-logs")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<List<TeamAuditLog>>> getAuditLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("teamId") Long teamId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            List<TeamAuditLog> logs = teamService.getAuditLogs(teamId, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Team audit logs retrieved successfully", logs));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "TEAM_404"));
        }
    }
}
