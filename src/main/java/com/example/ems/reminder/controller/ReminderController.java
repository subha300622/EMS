package com.example.ems.reminder.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.reminder.dto.ReminderListResponse;
import com.example.ems.reminder.dto.ReminderRequest;
import com.example.ems.reminder.dto.ReminderResponse;
import com.example.ems.reminder.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reminders")
@CrossOrigin("*")
@Tag(name = "Reminders", description = "Endpoints for managing reminders with production-grade Redis caching")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @Operation(summary = "Get all reminders")
    @GetMapping
    public ResponseEntity<ApiResponse<ReminderListResponse>> getAllReminders() {
        ReminderListResponse reminders = reminderService.getAllReminders();
        return ResponseEntity.ok(ApiResponse.success("All reminders retrieved successfully", reminders));
    }

    @Operation(summary = "Get a reminder by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReminderResponse>> getReminder(@PathVariable Long id) {
        ReminderResponse reminder = reminderService.getReminder(id);
        return ResponseEntity.ok(ApiResponse.success("Reminder retrieved successfully", reminder));
    }

    @Operation(summary = "Get all reminders for a specific employee")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<ReminderListResponse>> getRemindersByUser(@PathVariable Long userId) {
        ReminderListResponse reminders = reminderService.getRemindersByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Reminders for user retrieved successfully", reminders));
    }

    @Operation(summary = "Create a reminder")
    @PostMapping
    public ResponseEntity<ApiResponse<ReminderResponse>> createReminder(@RequestBody ReminderRequest request) {
        ReminderResponse reminder = reminderService.createReminder(request);
        return ResponseEntity.ok(ApiResponse.success("Reminder created successfully", reminder));
    }

    @Operation(summary = "Update a reminder")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReminderResponse>> updateReminder(
            @PathVariable Long id, 
            @RequestBody ReminderRequest request) {
        ReminderResponse reminder = reminderService.updateReminder(id, request);
        return ResponseEntity.ok(ApiResponse.success("Reminder updated successfully", reminder));
    }

    @Operation(summary = "Delete a reminder")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReminder(@PathVariable Long id) {
        reminderService.deleteReminder(id);
        return ResponseEntity.ok(ApiResponse.success("Reminder deleted successfully", null));
    }
}
