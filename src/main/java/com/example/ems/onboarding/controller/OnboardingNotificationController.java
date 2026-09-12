package com.example.ems.onboarding.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.onboarding.dto.notification.OnboardingNotificationRemindRequest;
import com.example.ems.onboarding.dto.notification.OnboardingNotificationResendRequest;
import com.example.ems.onboarding.service.OnboardingNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/onboarding/{onboardingId}/notifications")
@CrossOrigin("*")
@Tag(name = "Onboarding Event-Driven Notifications")
public class OnboardingNotificationController {

    @Autowired
    private OnboardingNotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get Onboarding Notifications Log")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNotifications(
            @PathVariable Long onboardingId) {
        List<Map<String, Object>> response = notificationService.getNotifications(onboardingId);
        return ResponseEntity.ok(ApiResponse.success("Notifications log retrieved successfully", response));
    }

    @PostMapping("/remind")
    @Operation(summary = "Send Onboarding Task Reminder")
    public ResponseEntity<ApiResponse<Void>> sendReminder(
            @PathVariable Long onboardingId,
            @Valid @RequestBody OnboardingNotificationRemindRequest request) {
        notificationService.sendReminder(onboardingId, request);
        return ResponseEntity.ok(ApiResponse.success("Reminder event dispatched successfully", null));
    }

    @PostMapping("/resend")
    @Operation(summary = "Resend Onboarding Welcome / Notification")
    public ResponseEntity<ApiResponse<Void>> resendNotification(
            @PathVariable Long onboardingId,
            @RequestBody OnboardingNotificationResendRequest request) {
        notificationService.resendNotification(onboardingId, request);
        return ResponseEntity.ok(ApiResponse.success("Notification event resent successfully", null));
    }
}
