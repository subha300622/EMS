package com.example.ems.schedule.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.schedule.dto.*;
import com.example.ems.schedule.service.MyScheduleService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/my-schedule")
@CrossOrigin("*")
@Tag(name = "Employee Self Service - Schedule")
public class MyScheduleController {

    @Autowired
    private MyScheduleService scheduleService;

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
               roleService.hasPermission(user.getWorkEmail(), "employee.schedule.read");
    }



    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<MyCalendarResponse>> getCalendar(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "MONTH") String view,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String eventType) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "schedule.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Calendar retrieved", scheduleService.getCalendar(user.getWorkEmail(), view, startDate, endDate, eventType)));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayScheduleResponse>> getTodaySchedule(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "schedule.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Today's schedule retrieved", scheduleService.getTodaySchedule(user.getWorkEmail())));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<UpcomingScheduleResponse>> getUpcomingSchedule(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(required = false) String eventType) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "schedule.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Upcoming schedule retrieved", scheduleService.getUpcomingSchedule(user.getWorkEmail(), days, eventType)));
    }

    @GetMapping("/shifts")
    public ResponseEntity<ApiResponse<ShiftHistoryResponse>> getShifts(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String status) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "schedule.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Shifts retrieved", scheduleService.getMyShiftHistory(user.getWorkEmail(), month, status)));
    }

    @PostMapping("/change-requests")
    public ResponseEntity<ApiResponse<ChangeRequestResponse>> createChangeRequest(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChangeRequestPayload req) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "schedule.self.change.create")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Change request submitted", scheduleService.createChangeRequest(user.getWorkEmail(), req)));
    }

    @GetMapping("/change-requests")
    public ResponseEntity<ApiResponse<ChangeRequestListResponse>> getChangeRequests(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "schedule.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Change requests retrieved", scheduleService.getChangeRequests(user.getWorkEmail(), status, pageable)));
    }

    @PutMapping("/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> updateAvailability(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AvailabilityRequest req) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "schedule.self.availability.update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Availability updated", scheduleService.updateAvailability(user.getWorkEmail(), req)));
    }

    @GetMapping("/timeline")
    public ResponseEntity<ApiResponse<ScheduleTimelineResponse>> getTimeline(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "schedule.self.timeline.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Timeline retrieved", scheduleService.getTimeline(user.getWorkEmail())));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<ScheduleNotificationsResponse>> getNotifications(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "schedule.self.notification.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", scheduleService.getNotifications(user.getWorkEmail())));
    }

    @GetMapping("/policies")
    public ResponseEntity<ApiResponse<SchedulePoliciesResponse>> getPolicies(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (!checkPermission(user, "schedule.self.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.success("Access Denied", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Policies retrieved", scheduleService.getPolicies()));
    }
}
