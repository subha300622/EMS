package com.example.ems.auth.controller;

import com.example.ems.auth.dto.OrgDashboardResponse;
import com.example.ems.auth.dto.RoleStatsResponse;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
@CrossOrigin("*")
@Tag(name = "Organization Admin - Dashboard & Analytics")
public class OrganizationDashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    private User resolveOrgAdmin(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email)
                        .filter(u -> roleService.hasPermission(email, "role.manage") || roleService.isSuperAdmin(email))
                        .orElse(null);
            }
        }
        return null;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get Organization Dashboard Summary")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        User admin = resolveOrgAdmin(authHeader);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }

        Long orgId = admin.getOrganization() != null ? admin.getOrganization().getId() : null;
        if (orgId == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("User does not belong to any organization", "AUTH_017"));
        }

        long userCount = userRepository.countByOrganizationId(orgId);
        List<Role> roles = roleRepository.findByOrganizationId(orgId);
        long roleCount = roles.size();
        long customRoleCount = roles.stream().filter(r -> !r.isSystemRole()).count();
        long totalUniquePermissions = roles.stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .distinct()
                .count();

        OrgDashboardResponse response = new OrgDashboardResponse(
                userCount,
                roleCount,
                customRoleCount,
                totalUniquePermissions
        );

        return ResponseEntity.ok(ApiResponse.success("Organization dashboard metrics retrieved successfully.", response));
    }

    @GetMapping("/role-stats")
    @Operation(summary = "Get user and permission usage metrics per role for caller's organization")
    public ResponseEntity<?> getRoleStats(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        User admin = resolveOrgAdmin(authHeader);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }

        Long orgId = admin.getOrganization() != null ? admin.getOrganization().getId() : null;
        if (orgId == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("User does not belong to any organization", "AUTH_017"));
        }

        List<RoleStatsResponse> stats = roleService.getRoleStats(orgId);
        return ResponseEntity.ok(ApiResponse.success("Role statistics retrieved successfully.", stats));
    }
}
