package com.example.ems.auth.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import com.example.ems.auth.dto.MyDashboardResponse;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.asset.repository.MyAssetRepository;
import com.example.ems.performance.repository.PerformanceReviewRepository;
import com.example.ems.support.repository.MySupportTicketRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/me")
@CrossOrigin("*")
@Tag(name = "Employee Self Service - Profile")
public class MeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private MyAssetRepository assetRepository;

    @Autowired
    private PerformanceReviewRepository reviewRepository;

    @Autowired
    private MySupportTicketRepository supportTicketRepository;

    @GetMapping("/profile")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.profile.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.profile.read' permission.", "AUTH_002"));
        }

        Employee employee = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", employee));
    }

    @PutMapping("/profile")
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.profile.update")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.profile.update' permission.", "AUTH_002"));
        }

        Employee employee = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        if (body.containsKey("phoneNumber")) {
            employee.setPhone(body.get("phoneNumber"));
        } else if (body.containsKey("phone")) {
            employee.setPhone(body.get("phone"));
        }
        if (body.containsKey("address")) {
            employee.setAddress(body.get("address"));
        }
        if (body.containsKey("emergencyContact")) {
            employee.setEmergencyContact(body.get("emergencyContact"));
        }
        if (body.containsKey("profileImage")) {
            employee.setProfileImage(body.get("profileImage"));
        }

        Employee saved = employeeRepository.save(employee);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", saved));
    }

    @GetMapping("/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<MyDashboardResponse>> getMyDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.dashboard.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.dashboard.read' permission.", "AUTH_002"));
        }

        Employee employee = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        long pendingLeavesCount = leaveRepository.findByEmployeeIdAndStatus(employee.getId(), "PENDING").size();

        long pendingExpensesCount = expenseRepository.findByEmployeeId(employee.getId()).stream()
                .filter(e -> {
                    String status = e.getStatus();
                    return "PENDING".equals(status) || "SUBMITTED".equals(status)
                            || "PENDING_MANAGER_APPROVAL".equals(status) || "PENDING_FINANCE_APPROVAL".equals(status);
                })
                .count();

        long assignedAssetsCount = assetRepository.findByAssignedToId(employee.getId()).size();

        long pendingReviewsCount = reviewRepository.findByEmployeeId(employee.getId()).stream()
                .filter(r -> !"FINALIZED".equalsIgnoreCase(r.getStatus()))
                .count();

        long openTicketsCount = supportTicketRepository.findByEmployeeEmail(employee.getEmail()).stream()
                .filter(t -> "OPEN".equalsIgnoreCase(t.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(t.getStatus()))
                .count();

        MyDashboardResponse dashboardResponse = new MyDashboardResponse(
                pendingLeavesCount,
                pendingExpensesCount,
                assignedAssetsCount,
                pendingReviewsCount,
                openTicketsCount
        );

        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", dashboardResponse));
    }

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
}
