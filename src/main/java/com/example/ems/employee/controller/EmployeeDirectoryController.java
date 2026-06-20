package com.example.ems.employee.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.employee.dto.*;
import com.example.ems.employee.service.MyEmployeeDirectoryService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/directory")
@CrossOrigin("*")
@Tag(name = "Organization Directory")
public class EmployeeDirectoryController {

    @Autowired
    private MyEmployeeDirectoryService directoryService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

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

    private boolean checkPermission(User user, String permission) {
        if (user == null) return false;
        if (roleService.isSuperAdmin(user.getWorkEmail())) return true;
        return roleService.hasPermission(user.getWorkEmail(), permission) || 
               roleService.hasPermission(user.getWorkEmail(), "employee.directory.read");
    }

    @Operation(summary = "Get Employee Directory Dashboard", description = "Retrieves high-level counts and statistics for the employee directory dashboard.")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<EmployeeDirectoryDashboardResponse>> getDashboard(
            @RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "employee.directory.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Dashboard retrieved", directoryService.getDashboard(user.getWorkEmail())));
    }

    @Operation(summary = "Get My Team Directory", description = "Retrieves direct reports and team mates of the logged-in employee.")
    @GetMapping("/my-team")
    public ResponseEntity<ApiResponse<MyTeamResponse>> getMyTeam(
            @RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "employee.team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("My Team retrieved", directoryService.getMyTeam(user.getWorkEmail())));
    }

    @Operation(summary = "Get Employee List", description = "Retrieves a paginated list of employees with search and multi-field filters.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<EmployeeDirectoryListResponse>> getEmployeeList(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String workMode,
            @RequestParam(required = false) String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName,asc") String sort) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "employee.directory.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }

        Sort parsedSort = parseSortParam(sort);
        Pageable pageable = PageRequest.of(page, size, parsedSort);

        return ResponseEntity.ok(ApiResponse.success("Employee directory retrieved",
                directoryService.getEmployeeList(search, department, designation, status, workMode, skill, pageable)));
    }

    @Operation(summary = "Get Employee Profile", description = "Retrieves a comprehensive view of a specific employee's professional profile.")
    @GetMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> getEmployeeProfile(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long employeeId) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "employee.profile.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", directoryService.getEmployeeProfile(employeeId)));
    }

    @Operation(summary = "Quick Search Employees", description = "Provides high-performance autocomplete or keyword search for employees by keyword name/email.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<EmployeeSearchResponse>> searchEmployees(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer limit) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "employee.directory.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved", directoryService.searchEmployees(keyword, limit)));
    }

    @Operation(summary = "Get Employee Skills", description = "Retrieves technical competencies and certs recorded on the employee's profile.")
    @GetMapping("/{employeeId}/skills")
    public ResponseEntity<ApiResponse<EmployeeSkillsResponse>> getEmployeeSkills(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long employeeId) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "employee.profile.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Skills retrieved", directoryService.getEmployeeSkills(employeeId)));
    }

    @Operation(summary = "Get Employee Team Hierarchy", description = "Retrieves details of managers and direct reports nested around the employee.")
    @GetMapping("/{employeeId}/hierarchy")
    public ResponseEntity<ApiResponse<EmployeeHierarchyResponse>> getHierarchy(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long employeeId) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "employee.team.hierarchy.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Hierarchy retrieved", directoryService.getHierarchy(employeeId)));
    }

    @Operation(summary = "Get Departments Listing", description = "Retrieves department classifications and metadata for directory browsing.")
    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<DepartmentListResponse>> getDepartments(
            @RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "employee.directory.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Departments retrieved", directoryService.getDepartments()));
    }

    @Operation(summary = "Send Message to Employee", description = "Dispatches a secure message or notice to the target employee's inbox.")
    @PostMapping("/{employeeId}/messages")
    public ResponseEntity<ApiResponse<SendMessageResponse>> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long employeeId,
            @RequestBody SendMessageRequest request) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "employee.message.create")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully",
                directoryService.sendMessage(user.getWorkEmail(), employeeId, request)));
    }

    @Operation(summary = "Get Employee Availability", description = "Retrieves shift status and real-time calendar availability of the employee.")
    @GetMapping("/{employeeId}/availability")
    public ResponseEntity<ApiResponse<EmployeeAvailabilityResponse>> getAvailability(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long employeeId) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "employee.directory.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Availability retrieved", directoryService.getAvailability(employeeId)));
    }

    private Sort parseSortParam(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by("fullName").ascending();
        }
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }
}
