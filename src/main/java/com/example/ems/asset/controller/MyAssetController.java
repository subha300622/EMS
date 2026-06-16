package com.example.ems.asset.controller;

import com.example.ems.asset.dto.*;
import com.example.ems.asset.entity.MyAsset;
import com.example.ems.asset.repository.MyAssetRepository;
import com.example.ems.asset.service.MyAssetService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/my-assets")
@CrossOrigin("*")
public class MyAssetController {

    @Autowired
    private MyAssetService assetService;

    @Autowired
    private MyAssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtService jwtService;

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

    private Employee resolveEmployee(User user) {
        if (user == null) return null;
        return employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
    }

    private boolean checkPermission(User user, String permission) {
        if (user == null) return false;
        return roleService.hasPermission(user.getWorkEmail(), permission)
                || roleService.hasPermission(user.getWorkEmail(), "asset.employee.manage")
                || roleService.hasPermission(user.getWorkEmail(), "asset.employee.read")
                || roleService.hasPermission(user.getWorkEmail(), "employee.asset.read")
                || roleService.isSuperAdmin(user.getWorkEmail());
    }

    private boolean isAssetOwnerOrAdmin(User currentUser, Employee employee, MyAsset asset) {
        if (asset.getAssignedTo() != null && asset.getAssignedTo().getId().equals(employee.getId())) {
            return true;
        }
        return roleService.hasPermission(currentUser.getWorkEmail(), "asset.employee.manage")
                || roleService.hasPermission(currentUser.getWorkEmail(), "asset.employee.read")
                || roleService.hasPermission(currentUser.getWorkEmail(), "asset.employee.update")
                || roleService.hasPermission(currentUser.getWorkEmail(), "employee.asset.read")
                || roleService.isSuperAdmin(currentUser.getWorkEmail());
    }

    private ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<?> forbiddenResponse(String permission) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires '" + permission + "' permission.", "AUTH_002"));
    }

    // 1. Get My Assets Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "asset.self.read")) return forbiddenResponse("asset.self.read");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        MyAssetsDashboardResponse response = assetService.getDashboard(employee);
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", response));
    }

    // 2. Get My Assigned Assets
    @GetMapping
    public ResponseEntity<?> getAssignedAssets(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String condition,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "assignedDate,desc") String sort) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "asset.self.read")) return forbiddenResponse("asset.self.read");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        String statusParam = (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) ? status.trim().toUpperCase() : null;
        String categoryParam = (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) ? category.trim().toUpperCase() : null;
        String conditionParam = (condition != null && !condition.isBlank() && !"ALL".equalsIgnoreCase(condition)) ? condition.trim().toUpperCase() : null;

        Sort sortObj = Sort.unsorted();
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String property = parts[0];
            if ("assetValue".equals(property)) {
                property = "currentValue";
            }
            Sort.Direction direction = Sort.Direction.DESC;
            if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) {
                direction = Sort.Direction.ASC;
            }
            sortObj = Sort.by(direction, property);
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<MyAssignedAssetItem> response = assetService.getAssignedAssets(employee, statusParam, categoryParam, conditionParam, pageable);
        return ResponseEntity.ok(ApiResponse.success("Assigned assets retrieved successfully", response));
    }

    // 3. Get Asset Details
    @GetMapping("/{assetId}")
    public ResponseEntity<?> getAssetDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("assetId") Long assetId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "asset.self.read")) return forbiddenResponse("asset.self.read");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        Optional<MyAsset> assetOpt = assetRepository.findById(assetId);
        if (assetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + assetId, "AST_404"));
        }

        if (!isAssetOwnerOrAdmin(currentUser, employee, assetOpt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You do not own this asset.", "AST_403"));
        }

        try {
            MyAssetDetailsResponse response = assetService.getAssetDetails(assetId, employee);
            return ResponseEntity.ok(ApiResponse.success("Asset details retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "AST_500"));
        }
    }

    // 4. Submit Asset Request
    @PostMapping("/requests")
    public ResponseEntity<?> requestAsset(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody CreateAssetRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "asset.self.request")) return forbiddenResponse("asset.self.request");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        try {
            AssetRequestResponse response = assetService.requestAsset(request, employee);
            return ResponseEntity.ok(ApiResponse.success("Asset request submitted successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "AST_500"));
        }
    }

    // 5. Get Asset Requests
    @GetMapping("/requests")
    public ResponseEntity<?> getAssetRequests(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "asset.self.read")) return forbiddenResponse("asset.self.read");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        AssetRequestsListResponse response = assetService.getAssetRequests(employee);
        return ResponseEntity.ok(ApiResponse.success("Asset requests retrieved successfully", response));
    }

    // 6. Report Issue on Asset
    @PostMapping("/{assetId}/issues")
    public ResponseEntity<?> reportIssue(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("assetId") Long assetId,
            @Valid @RequestBody ReportIssueRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "asset.self.issue.create")) return forbiddenResponse("asset.self.issue.create");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        Optional<MyAsset> assetOpt = assetRepository.findById(assetId);
        if (assetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + assetId, "AST_404"));
        }

        if (!isAssetOwnerOrAdmin(currentUser, employee, assetOpt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You do not own this asset.", "AST_403"));
        }

        try {
            ReportIssueResponse response = assetService.reportIssue(assetId, request, employee);
            return ResponseEntity.ok(ApiResponse.success("Asset issue ticket reported successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "AST_500"));
        }
    }

    // 7. Get Asset Issues
    @GetMapping("/issues")
    public ResponseEntity<?> getAssetIssues(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "asset.self.read")) return forbiddenResponse("asset.self.read");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        AssetIssuesListResponse response = assetService.getAssetIssues(employee);
        return ResponseEntity.ok(ApiResponse.success("Asset issue tickets retrieved successfully", response));
    }

    // 8. Submit Asset Return Request
    @PostMapping("/{assetId}/return-request")
    public ResponseEntity<?> submitReturnRequest(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("assetId") Long assetId,
            @Valid @RequestBody AssetReturnFormRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "asset.self.return.request")) return forbiddenResponse("asset.self.return.request");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        Optional<MyAsset> assetOpt = assetRepository.findById(assetId);
        if (assetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + assetId, "AST_404"));
        }

        if (!isAssetOwnerOrAdmin(currentUser, employee, assetOpt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You do not own this asset.", "AST_403"));
        }

        try {
            AssetReturnResponse response = assetService.submitReturnRequest(assetId, request, employee);
            return ResponseEntity.ok(ApiResponse.success("Asset return request submitted successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "AST_500"));
        }
    }

    // 9. Get Asset Timeline
    @GetMapping("/{assetId}/timeline")
    public ResponseEntity<?> getAssetTimeline(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("assetId") Long assetId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "asset.self.timeline.read")) return forbiddenResponse("asset.self.timeline.read");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        Optional<MyAsset> assetOpt = assetRepository.findById(assetId);
        if (assetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + assetId, "AST_404"));
        }

        if (!isAssetOwnerOrAdmin(currentUser, employee, assetOpt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You do not own this asset.", "AST_403"));
        }

        try {
            AssetTimelineResponse response = assetService.getAssetTimeline(assetId, employee);
            return ResponseEntity.ok(ApiResponse.success("Asset activity timeline retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "AST_500"));
        }
    }

    // 10. Get Allowed Category Configurations
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "asset.self.read")) return forbiddenResponse("asset.self.read");

        AssetCategoriesResponse response = assetService.getCategories();
        return ResponseEntity.ok(ApiResponse.success("Allowed categories retrieved successfully", response));
    }

    // 11. Get Policy Acknowledgement Details
    @GetMapping("/policies")
    public ResponseEntity<?> getPolicies(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "asset.self.read")) return forbiddenResponse("asset.self.read");

        AssetPoliciesResponse response = assetService.getPolicies();
        return ResponseEntity.ok(ApiResponse.success("Policies retrieved successfully", response));
    }
}
