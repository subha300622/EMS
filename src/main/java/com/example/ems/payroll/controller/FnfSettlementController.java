package com.example.ems.payroll.controller;
import java.util.List;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.payroll.entity.FnfSettlement;
import com.example.ems.payroll.service.FnfSettlementService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fnf-settlements")
@CrossOrigin("*")
@Tag(name = "Finance Management")
public class FnfSettlementController {

    @Autowired
    private FnfSettlementService fnfSettlementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<FnfSettlement>>> getAllSettlements(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "fnf.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'fnf.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Settlements list retrieved successfully",
                fnfSettlementService.getAllSettlements()));
    }

    @GetMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getSettlementById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "fnf.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'fnf.manage' permission.", "AUTH_002"));
        }

        FnfSettlement settlement = fnfSettlementService.getSettlementById(id).orElse(null);
        if (settlement == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Settlement not found with ID: " + id, "FNF_001"));
        }

        return ResponseEntity.ok(ApiResponse.success("Settlement details retrieved successfully", settlement));
    }

    @GetMapping("/employee/{employeeId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getSettlementByEmployeeId(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "fnf.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'fnf.manage' permission.", "AUTH_002"));
        }

        FnfSettlement settlement = fnfSettlementService.getSettlementByEmployeeId(employeeId).orElse(null);
        if (settlement == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Settlement not found for Employee: " + employeeId, "FNF_001"));
        }

        return ResponseEntity.ok(ApiResponse.success("Settlement details retrieved successfully", settlement));
    }

    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createSettlement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody FnfSettlement request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "fnf.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'fnf.manage' permission.", "AUTH_002"));
        }

        FnfSettlement created = fnfSettlementService.createSettlement(request);
        return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Settlement created successfully", created));
    }

    @PostMapping("/{id}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveSettlement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "fnf.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'fnf.manage' permission.", "AUTH_002"));
        }

        return (ResponseEntity) fnfSettlementService.approveSettlement(id)
                .<ResponseEntity<?>>map(settlement -> ResponseEntity.ok(ApiResponse.success("Settlement approved successfully", settlement)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Settlement not found with ID: " + id, "FNF_001")));
    }

    @PatchMapping("/{id}/reject")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectSettlement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "fnf.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'fnf.manage' permission.", "AUTH_002"));
        }

        return (ResponseEntity) fnfSettlementService.rejectSettlement(id)
                .<ResponseEntity<?>>map(settlement -> ResponseEntity.ok(ApiResponse.success("Settlement rejected successfully", settlement)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Settlement not found with ID: " + id, "FNF_001")));
    }

    @PostMapping("/{id}/process")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> processSettlement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "fnf.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'fnf.manage' permission.", "AUTH_002"));
        }

        return (ResponseEntity) fnfSettlementService.processSettlement(id)
                .<ResponseEntity<?>>map(settlement -> ResponseEntity.ok(ApiResponse.success("Settlement processed successfully", settlement)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Settlement not found with ID: " + id, "FNF_001")));
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
