package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.AttendanceCalendarDayDto;
import com.example.ems.attendance.dto.MonthlyAttendanceCalendarResponse;
import com.example.ems.attendance.service.AttendanceCalendarService;
import com.example.ems.config.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AttendanceCalendarControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceCalendarService attendanceCalendarService;

    @InjectMocks
    private AttendanceCalendarController attendanceCalendarController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceCalendarController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/attendance/calendar - returns monthly calendar response")
    void testGetMonthlyCalendar() throws Exception {
        MonthlyAttendanceCalendarResponse response = new MonthlyAttendanceCalendarResponse();
        response.setYear(2026);
        response.setMonth(9);
        response.setTotalDays(30);
        response.setWorkingDays(22);
        response.setPresentDays(18);
        response.setAbsentDays(1);
        response.setLeaveDays(2);
        response.setHolidayDays(1);
        response.setWeekOffDays(8);
        response.setTotalWorkingMinutes(8820);
        response.setDays(List.of(new AttendanceCalendarDayDto(LocalDate.of(2026, 9, 1), "TUESDAY", "PRESENT")));

        when(attendanceCalendarService.getMonthlyCalendar(eq(2026), eq(9)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/attendance/calendar")
                        .param("year", "2026")
                        .param("month", "9")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.month").value(9))
                .andExpect(jsonPath("$.data.totalDays").value(30))
                .andExpect(jsonPath("$.data.workingDays").value(22))
                .andExpect(jsonPath("$.data.presentDays").value(18))
                .andExpect(jsonPath("$.data.days[0].status").value("PRESENT"));
    }

    @Test
    @DisplayName("GET /api/v1/attendance/calendar - returns 400 on invalid month/year")
    void testGetMonthlyCalendar_InvalidInput_Returns400() throws Exception {
        when(attendanceCalendarService.getMonthlyCalendar(anyInt(), eq(13)))
                .thenThrow(new IllegalArgumentException("Invalid month: 13. Month must be between 1 and 12."));

        mockMvc.perform(get("/api/v1/attendance/calendar")
                        .param("year", "2026")
                        .param("month", "13")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("GET /api/v1/attendance/calendar/{date} - returns day detail")
    void testGetDayDetail() throws Exception {
        LocalDate targetDate = LocalDate.of(2026, 9, 10);
        AttendanceCalendarDayDto dayDto = new AttendanceCalendarDayDto(targetDate, "THURSDAY", "PRESENT");
        dayDto.setTotalWorkingMinutes(480);
        dayDto.setTotalBreakMinutes(45);

        when(attendanceCalendarService.getDayDetail(eq(targetDate)))
                .thenReturn(dayDto);

        mockMvc.perform(get("/api/v1/attendance/calendar/2026-09-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.date").value("2026-09-10"))
                .andExpect(jsonPath("$.data.dayOfWeek").value("THURSDAY"))
                .andExpect(jsonPath("$.data.status").value("PRESENT"))
                .andExpect(jsonPath("$.data.totalWorkingMinutes").value(480));
    }
}
