package com.example.ems.settings.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import com.example.ems.settings.dto.ChangePasswordRequest;
import com.example.ems.settings.dto.RegenerateBackupCodesRequest;
import com.example.ems.settings.dto.SupportTicketRequest;
import com.example.ems.settings.service.MySettingsService;
import com.example.ems.support.dto.CreateTicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/my-settings")
@CrossOrigin("*")
@Tag(name = "Employee Self Service - Settings")
public class MySettingsController {

    @Autowired
    private MySettingsService mySettingsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

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
        return roleService.hasPermission(user.getWorkEmail(), permission)
                || roleService.isSuperAdmin(user.getWorkEmail());
    }

    private ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<?> forbiddenResponse(String permission) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires '" + permission + "' permission.", "AUTH_002"));
    }

    // 1. Settings Dashboard
    @Operation(summary = "Get Settings Dashboard", description = "Retrieves an overview of user preferences, security posture, and active MFA status.")
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDashboard(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.self.read")) return (ResponseEntity) forbiddenResponse("settings.self.read");

        try {
            Map<String, Object> response = mySettingsService.getSettingsDashboard(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Settings dashboard retrieved successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 2. Get Security Settings
    @Operation(summary = "Get Security Settings", description = "Retrieves details of active security policies, login history, and MFA parameters.")
    @GetMapping("/security")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getSecuritySettings(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.security.read")) return (ResponseEntity) forbiddenResponse("settings.security.read");

        try {
            Map<String, Object> response = mySettingsService.getSecuritySettings(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Security settings retrieved successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 3. Change Password
    @Operation(summary = "Change Password", description = "Updates the user account password after validating the old password.")
    @PostMapping("/security/change-password")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody ChangePasswordRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.security.update")) return (ResponseEntity) forbiddenResponse("settings.security.update");

        try {
            mySettingsService.changePassword(currentUser.getWorkEmail(), request);
            Map<String, Object> data = new HashMap<>();
            data.put("changedAt", LocalDateTime.now().format(ISO_FORMATTER));
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", data));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 4. Enable MFA
    @Operation(summary = "Enable/Disable MFA", description = "Toggles Multi-Factor Authentication (MFA) status for the employee account.")
    @PutMapping("/security/mfa")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> enableMfa(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody Map<String, Boolean> request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.security.update")) return (ResponseEntity) forbiddenResponse("settings.security.update");

        try {
            Boolean enabled = request.getOrDefault("enabled", true);
            mySettingsService.updateMfa(currentUser.getWorkEmail(), enabled);
            String message = enabled ? "MFA enabled successfully" : "MFA disabled successfully";
            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 5. Get Privacy Settings
    @Operation(summary = "Get Privacy Settings", description = "Retrieves the employee's directory visibility and communication privacy preferences.")
    @GetMapping("/privacy")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getPrivacySettings(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.privacy.read")) return (ResponseEntity) forbiddenResponse("settings.privacy.read");

        try {
            Map<String, Object> response = mySettingsService.getPrivacySettings(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Privacy settings retrieved successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 6. Update Privacy Settings
    @Operation(summary = "Update Privacy Settings", description = "Updates directory visibility and personal details sharing preferences.")
    @PutMapping("/privacy")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updatePrivacySettings(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody Map<String, Object> request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.privacy.update")) return (ResponseEntity) forbiddenResponse("settings.privacy.update");

        try {
            mySettingsService.updatePrivacySettings(currentUser.getWorkEmail(), request);
            return ResponseEntity.ok(ApiResponse.success("Privacy settings updated successfully"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 7. Get Notification Preferences
    @Operation(summary = "Get Notification Preferences", description = "Retrieves configured notification preferences across email, SMS, and push notification channels.")
    @GetMapping("/notifications")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getNotificationPreferences(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.notifications.read")) return (ResponseEntity) forbiddenResponse("settings.notifications.read");

        try {
            List<Map<String, Object>> response = mySettingsService.getNotificationPreferences(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Notification preferences retrieved successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 8. Update Notification Preferences for category
    @Operation(summary = "Update Notification Category Preference", description = "Updates delivery preferences (email/push/SMS) for a specific notification category.")
    @PutMapping("/notifications/{category}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateNotificationCategory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("category") String category,
            @RequestBody Map<String, Boolean> request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.notifications.update")) return (ResponseEntity) forbiddenResponse("settings.notifications.update");

        try {
            mySettingsService.updateNotificationCategory(currentUser.getWorkEmail(), category.toUpperCase(), request);
            return ResponseEntity.ok(ApiResponse.success("Notification preference for " + category + " updated successfully"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 9. Get Notification Timing
    @Operation(summary = "Get Notification Timing Preferences", description = "Retrieves preferred quiet hours and batching settings for alerts.")
    @GetMapping("/notifications/timing")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getNotificationTiming(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.notifications.read")) return (ResponseEntity) forbiddenResponse("settings.notifications.read");

        try {
            Map<String, Object> response = mySettingsService.getNotificationTiming(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Notification timing preferences retrieved successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 10. Update Notification Timing
    @Operation(summary = "Update Notification Timing Preferences", description = "Updates quiet hours and alert batching intervals.")
    @PutMapping("/notifications/timing")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateNotificationTiming(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody Map<String, Object> request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.notifications.update")) return (ResponseEntity) forbiddenResponse("settings.notifications.update");

        try {
            mySettingsService.updateNotificationTiming(currentUser.getWorkEmail(), request);
            return ResponseEntity.ok(ApiResponse.success("Notification timing preferences updated successfully"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 11. Get Appearance
    @Operation(summary = "Get Appearance Settings", description = "Retrieves display theme, language, and styling preferences.")
    @GetMapping("/appearance")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAppearance(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.appearance.read")) return (ResponseEntity) forbiddenResponse("settings.appearance.read");

        try {
            Map<String, Object> response = mySettingsService.getAppearance(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Appearance settings retrieved successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 12. Update Appearance
    @Operation(summary = "Update Appearance Settings", description = "Updates display theme (light/dark) and visual dashboard choices.")
    @PutMapping("/appearance")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateAppearance(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody Map<String, Object> request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.appearance.update")) return (ResponseEntity) forbiddenResponse("settings.appearance.update");

        try {
            mySettingsService.updateAppearance(currentUser.getWorkEmail(), request);
            return ResponseEntity.ok(ApiResponse.success("Appearance settings updated successfully"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 13. Get Language & Region
    @Operation(summary = "Get Language and Region Settings", description = "Retrieves current locale, language, and timezone settings.")
    @GetMapping("/language-region")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLanguageRegion(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.language.read")) return (ResponseEntity) forbiddenResponse("settings.language.read");

        try {
            Map<String, Object> response = mySettingsService.getLanguageRegion(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Language and region settings retrieved successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 14. Update Language & Region
    @Operation(summary = "Update Language and Region Settings", description = "Updates locale, preferred display language, and timezone.")
    @PutMapping("/language-region")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateLanguageRegion(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody Map<String, Object> request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.language.update")) return (ResponseEntity) forbiddenResponse("settings.language.update");

        try {
            mySettingsService.updateLanguageRegion(currentUser.getWorkEmail(), request);
            return ResponseEntity.ok(ApiResponse.success("Language and region settings updated successfully"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 15. Get Connected Devices
    @Operation(summary = "Get Connected Devices", description = "Retrieves active sessions, logins, and connected devices for security audit.")
    @GetMapping("/devices")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDevices(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.devices.read")) return (ResponseEntity) forbiddenResponse("settings.devices.read");

        try {
            List<Map<String, Object>> response = mySettingsService.getDevices(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Connected devices retrieved successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 16. Remove Connected Device
    @Operation(summary = "Remove Connected Device", description = "Revokes authentication token and logs out the selected session/device.")
    @DeleteMapping("/devices/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> removeDevice(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long deviceId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.devices.remove")) return (ResponseEntity) forbiddenResponse("settings.devices.remove");

        try {
            mySettingsService.removeDevice(currentUser.getWorkEmail(), deviceId);
            return ResponseEntity.ok(ApiResponse.success("Device removed successfully"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }



    // 20. GET FAQs
    @Operation(summary = "Get FAQs", description = "Retrieves list of frequently asked questions and troubleshooting guides.")
    @GetMapping("/faqs")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getFaqs(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.support.read")) return (ResponseEntity) forbiddenResponse("settings.support.read");

        try {
            List<Map<String, Object>> response = mySettingsService.getFaqs();
            return ResponseEntity.ok(ApiResponse.success("FAQs retrieved successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 21. Create Support Request
    @Operation(summary = "Create Support Request", description = "Creates a helpdesk support ticket linked to user settings and preferences.")
    @PostMapping("/support-tickets")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createSupportRequest(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody SupportTicketRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.support.create")) return (ResponseEntity) forbiddenResponse("settings.support.create");

        try {
            CreateTicketResponse ticketResp = mySettingsService.createSupportRequest(currentUser.getWorkEmail(), request);
            Map<String, Object> data = new HashMap<>();
            data.put("ticketId", ticketResp.getTicketNumber());
            data.put("status", ticketResp.getStatus());
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Support ticket created successfully", data));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 22. Get Backup Codes Info
    @Operation(summary = "Get Backup Codes Info", description = "Retrieves availability and count of remaining backup codes for 2FA recovery.")
    @GetMapping("/security/2fa/backup-codes")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getBackupCodesInfo(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.security.read")) return (ResponseEntity) forbiddenResponse("settings.security.read");

        try {
            Map<String, Object> response = mySettingsService.getBackupCodesInfo(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Backup codes info retrieved successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }

    // 23. Regenerate Backup Codes
    @Operation(summary = "Regenerate Backup Codes", description = "Generates a fresh set of backup/recovery codes for multi-factor authentication (MFA).")
    @PostMapping("/security/2fa/backup-codes/regenerate")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> regenerateBackupCodes(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody RegenerateBackupCodesRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "settings.security.update")) return (ResponseEntity) forbiddenResponse("settings.security.update");

        try {
            String ipAddress = httpRequest.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = httpRequest.getRemoteAddr();
            }
            Map<String, Object> response = mySettingsService.regenerateBackupCodes(currentUser.getWorkEmail(), request, ipAddress);
            return ResponseEntity.ok(ApiResponse.success("Backup codes regenerated successfully", response));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if ("INVALID_PASSWORD".equals(msg)) {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.error("Current password is incorrect", "INVALID_PASSWORD"));
            } else if ("INVALID_OTP".equals(msg)) {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.error("Invalid verification code", "INVALID_OTP"));
            }
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(msg, "SET_500"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "SET_500"));
        }
    }
}
