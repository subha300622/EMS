package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.AttendanceBreakDto;
import com.example.ems.attendance.dto.AttendanceCoreResponse;
import com.example.ems.attendance.exception.ActiveBreakExistsException;
import com.example.ems.attendance.exception.AttendanceNotFoundException;
import com.example.ems.attendance.exception.DuplicateCheckInException;
import com.example.ems.attendance.exception.InvalidAttendanceStateException;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.config.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AttendanceCoreControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceCoreController attendanceCoreController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceCoreController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/attendance/check-in -> 201 Created on valid check-in")
    void testCheckIn_Returns201() throws Exception {
        AttendanceCoreResponse response = new AttendanceCoreResponse();
        response.setAttendanceId(1001L);
        response.setEmployeeId(125L);
        response.setEmployeeName("John Doe");
        response.setAttendanceDate(LocalDate.of(2026, 9, 10));
        response.setStatus("WORKING");
        response.setCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));

        when(attendanceService.checkInCore()).thenReturn(response);

        mockMvc.perform(post("/api/v1/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attendanceId").value(1001))
                .andExpect(jsonPath("$.data.status").value("WORKING"))
                .andExpect(jsonPath("$.data.checkInTime").value("2026-09-10T09:00:00Z"));
    }

    @Test
    @DisplayName("POST /api/v1/attendance/check-in -> 409 Conflict on duplicate check-in")
    void testCheckIn_Duplicate_Returns409() throws Exception {
        when(attendanceService.checkInCore()).thenThrow(new DuplicateCheckInException("Already checked in today."));

        mockMvc.perform(post("/api/v1/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ATT_002"));
    }

    @Test
    @DisplayName("POST /api/v1/attendance/break/start -> 200 OK on successful break start")
    void testStartBreak_Returns200() throws Exception {
        AttendanceCoreResponse response = new AttendanceCoreResponse();
        response.setAttendanceId(1001L);
        response.setStatus("ON_BREAK");
        response.setActiveBreak(true);
        response.setBreaks(List.of(new AttendanceBreakDto(501L, Instant.parse("2026-09-10T13:00:00Z"), null, null, true)));

        when(attendanceService.startBreakCore()).thenReturn(response);

        mockMvc.perform(post("/api/v1/attendance/break/start")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ON_BREAK"))
                .andExpect(jsonPath("$.data.activeBreak").value(true))
                .andExpect(jsonPath("$.data.breaks[0].id").value(501));
    }

    @Test
    @DisplayName("POST /api/v1/attendance/break/start -> 409 Conflict if already on active break")
    void testStartBreak_ActiveBreakExists_Returns409() throws Exception {
        when(attendanceService.startBreakCore()).thenThrow(new ActiveBreakExistsException("Employee is already on an active break."));

        mockMvc.perform(post("/api/v1/attendance/break/start")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ATT_409"));
    }

    @Test
    @DisplayName("POST /api/v1/attendance/break/end -> 200 OK on ending active break")
    void testEndBreak_Returns200() throws Exception {
        AttendanceCoreResponse response = new AttendanceCoreResponse();
        response.setAttendanceId(1001L);
        response.setStatus("WORKING");
        response.setActiveBreak(false);
        response.setTotalBreakMinutes(30);

        when(attendanceService.endBreakCore()).thenReturn(response);

        mockMvc.perform(post("/api/v1/attendance/break/end")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("WORKING"))
                .andExpect(jsonPath("$.data.totalBreakMinutes").value(30));
    }

    @Test
    @DisplayName("POST /api/v1/attendance/check-out -> 200 OK on successful check-out")
    void testCheckOut_Returns200() throws Exception {
        AttendanceCoreResponse response = new AttendanceCoreResponse();
        response.setAttendanceId(1001L);
        response.setStatus("COMPLETED");
        response.setTotalBreakMinutes(45);
        response.setTotalWorkingMinutes(495);
        response.setCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));

        when(attendanceService.checkOutCore()).thenReturn(response);

        mockMvc.perform(post("/api/v1/attendance/check-out")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.totalWorkingMinutes").value(495))
                .andExpect(jsonPath("$.data.totalBreakMinutes").value(45));
    }

    @Test
    @DisplayName("POST /api/v1/attendance/check-out -> 400 Bad Request if on active break")
    void testCheckOut_WhileOnBreak_Returns400() throws Exception {
        when(attendanceService.checkOutCore()).thenThrow(new InvalidAttendanceStateException("Employee is currently on break and must end the break before checking out."));

        mockMvc.perform(post("/api/v1/attendance/check-out")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ATT_400"));
    }

    @Test
    @DisplayName("GET /api/v1/attendance/today -> 200 OK returning NOT_CHECKED_IN state object")
    void testGetToday_ReturnsNotCheckedIn() throws Exception {
        AttendanceCoreResponse response = AttendanceCoreResponse.notCheckedIn(125L, "John Doe", "EMP-125", LocalDate.of(2026, 9, 10));

        when(attendanceService.getTodayAttendanceCore()).thenReturn(response);

        mockMvc.perform(get("/api/v1/attendance/today")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("NOT_CHECKED_IN"))
                .andExpect(jsonPath("$.data.attendanceId").doesNotExist())
                .andExpect(jsonPath("$.data.totalWorkingMinutes").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/attendance/{id} -> 404 Not Found if attendance does not belong to user")
    void testGetById_NotFound_Returns404() throws Exception {
        when(attendanceService.getAttendanceByIdCore(999L)).thenThrow(new AttendanceNotFoundException("Attendance record not found with ID: 999"));

        mockMvc.perform(get("/api/v1/attendance/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ATT_404"));
    }
}
