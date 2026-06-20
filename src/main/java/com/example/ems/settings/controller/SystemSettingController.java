package com.example.ems.settings.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import com.example.ems.settings.entity.SystemSetting;
import com.example.ems.settings.service.SystemSettingService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/settings")
@CrossOrigin("*")
@Tag(name = "System Administration")
public class SystemSettingController {

    @Autowired
    private SystemSettingService systemSettingService;

    @Autowired
    private UserRepository userRepository;

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

    private boolean checkPermission(User user) {
        if (user == null) return false;
        return roleService.hasPermission(user.getWorkEmail(), "settings.manage")
                || roleService.isSuperAdmin(user.getWorkEmail());
    }

    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<SystemSetting>>> getAllSettings(@RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires 'settings.manage' permission.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("System settings retrieved successfully", systemSettingService.getAllSettings()));
    }

    @PutMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateSettings(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> settingsMap){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires 'settings.manage' permission.", "AUTH_002"));
        }
        Map<String, SystemSetting> updatedSettings = new HashMap<>();
        for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
            SystemSetting setting = systemSettingService.updateSetting(entry.getKey(), entry.getValue(), "general");
            updatedSettings.put(entry.getKey(), setting);
        }
        return ResponseEntity.ok(ApiResponse.success("System settings updated successfully", updatedSettings));
    }

    // --- Sub-modules: Company ---
    @GetMapping("/company")
    public ResponseEntity<?> getCompanySettings(@RequestHeader(value = "Authorization", required = false) String authHeader){
        return getSettingsByCategory(authHeader, "company");
    }

    @PutMapping("/company")
    public ResponseEntity<?> updateCompanySettings(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> settingsMap){
        return updateSettingsByCategory(authHeader, settingsMap, "company");
    }

    // --- Sub-modules: Security ---
    @GetMapping("/security")
    public ResponseEntity<?> getSecuritySettings(@RequestHeader(value = "Authorization", required = false) String authHeader){
        return getSettingsByCategory(authHeader, "security");
    }

    @PutMapping("/security")
    public ResponseEntity<?> updateSecuritySettings(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> settingsMap){
        return updateSettingsByCategory(authHeader, settingsMap, "security");
    }

    // --- Sub-modules: Email ---
    @GetMapping("/email")
    public ResponseEntity<?> getEmailSettings(@RequestHeader(value = "Authorization", required = false) String authHeader){
        return getSettingsByCategory(authHeader, "email");
    }

    @PutMapping("/email")
    public ResponseEntity<?> updateEmailSettings(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> settingsMap){
        return updateSettingsByCategory(authHeader, settingsMap, "email");
    }

    // --- Sub-modules: Integrations ---
    @GetMapping("/integrations")
    public ResponseEntity<?> getIntegrationsSettings(@RequestHeader(value = "Authorization", required = false) String authHeader){
        return getSettingsByCategory(authHeader, "integrations");
    }

    @PutMapping("/integrations")
    public ResponseEntity<?> updateIntegrationsSettings(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> settingsMap){
        return updateSettingsByCategory(authHeader, settingsMap, "integrations");
    }

    // --- Sub-modules: Password Policy ---
    @GetMapping("/password-policy")
    public ResponseEntity<?> getPasswordPolicySettings(@RequestHeader(value = "Authorization", required = false) String authHeader){
        return getSettingsByCategory(authHeader, "password-policy");
    }

    @PutMapping("/password-policy")
    public ResponseEntity<?> updatePasswordPolicySettings(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> settingsMap){
        return updateSettingsByCategory(authHeader, settingsMap, "password-policy");
    }

    // --- Helper Methods ---
    private ResponseEntity<?> getSettingsByCategory(String authHeader, String category) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires 'settings.manage' permission.", "AUTH_002"));
        }
        List<SystemSetting> settings = systemSettingService.getSettingsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(category + " settings retrieved successfully", settings));
    }

    private ResponseEntity<?> updateSettingsByCategory(String authHeader, Map<String, String> settingsMap, String category) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires 'settings.manage' permission.", "AUTH_002"));
        }
        Map<String, SystemSetting> updatedSettings = new HashMap<>();
        for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
            SystemSetting setting = systemSettingService.updateSetting(entry.getKey(), entry.getValue(), category);
            updatedSettings.put(entry.getKey(), setting);
        }
        return ResponseEntity.ok(ApiResponse.success(category + " settings updated successfully", updatedSettings));
    }
}
