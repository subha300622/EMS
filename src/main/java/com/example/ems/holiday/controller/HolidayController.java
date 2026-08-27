package com.example.ems.holiday.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.holiday.dto.*;
import com.example.ems.holiday.service.HolidayService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/holidays")
@CrossOrigin("*")
@Tag(name = "Holiday Maintenance", description = "Organization-Wide Holiday Maintenance APIs")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

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

    @Operation(summary = "Create Holiday", description = "Creates a new organization-wide holiday.")
    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createHoliday(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody HolidayCreateRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            HolidayResponseDto dto = holidayService.createHoliday(user, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Holiday created successfully", dto));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        } catch (IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.error(e.getMessage(), "HOLIDAY_001"));
        }
    }

    @Operation(summary = "Get Holiday", description = "Retrieves holiday by holiday ID for the authenticated organization.")
    @GetMapping("/{holidayId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getHolidayById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String holidayId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            HolidayResponseDto dto = holidayService.getHolidayById(user, holidayId);
            return ResponseEntity.ok(ApiResponse.success("Holiday retrieved successfully", dto));
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "HOLIDAY_404"));
        }
    }

    @Operation(summary = "List Holidays", description = "Retrieves paginated list of organization-wide holidays with optional date filters.")
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> listHolidays(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        HolidayListResponse resp = holidayService.listHolidays(user, year, fromDate, toDate, page, size);
        return ResponseEntity.ok(ApiResponse.success("Holidays retrieved successfully", resp));
    }

    @Operation(summary = "Update Holiday", description = "Updates an existing holiday.")
    @PutMapping("/{holidayId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateHoliday(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String holidayId,
            @Valid @RequestBody HolidayUpdateRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            HolidayResponseDto dto = holidayService.updateHoliday(user, holidayId, request);
            return ResponseEntity.ok(ApiResponse.success("Holiday updated successfully", dto));
        } catch (ResourceNotFoundException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "HOLIDAY_404"));
        } catch (IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.error(e.getMessage(), "HOLIDAY_001"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "VAL_001"));
        }
    }

    @Operation(summary = "Deactivate Holiday", description = "Deactivates (soft deletes) a holiday.")
    @DeleteMapping("/{holidayId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteHoliday(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String holidayId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            HolidayResponseDto dto = holidayService.deleteHoliday(user, holidayId);
            return ResponseEntity.ok(ApiResponse.success("Holiday deactivated successfully", dto));
        } catch (ResourceNotFoundException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "HOLIDAY_404"));
        }
    }

    @Operation(summary = "Check Holiday", description = "Checks whether a given date is an active holiday for the organization.")
    @GetMapping("/check")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> checkHoliday(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        HolidayCheckResponse resp = holidayService.checkHoliday(user, date);
        return ResponseEntity.ok(ApiResponse.success("Holiday check completed", resp));
    }

    @Operation(summary = "Get Holiday Calendar", description = "Retrieves active holiday calendar for a given year.")
    @GetMapping("/calendar")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getHolidayCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Integer year) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        int queryYear = (year != null) ? year : LocalDate.now().getYear();
        HolidayCalendarResponse resp = holidayService.getHolidayCalendar(user, queryYear);
        return ResponseEntity.ok(ApiResponse.success("Holiday calendar retrieved successfully", resp));
    }
}
