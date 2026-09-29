package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.early.EarlyCheckoutReportDto;
import com.example.ems.attendance.dto.late.LateAttendanceReportDto;
import com.example.ems.attendance.service.AttendanceLateEarlyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AttendanceLateEarlyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceLateEarlyService lateEarlyService;

    @InjectMocks
    private AttendanceLateEarlyController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /late: Returns late attendance report")
    void testGetLateReport_Success() throws Exception {
        LateAttendanceReportDto reportDto = new LateAttendanceReportDto();
        reportDto.setAttendanceId(101L);
        reportDto.setEmployeeId(1L);
        reportDto.setEmployeeName("John Doe");
        reportDto.setDepartment("Engineering");
        reportDto.setTeam("Backend");
        reportDto.setDate(LocalDate.of(2026, 9, 10));
        reportDto.setCheckInTime(Instant.parse("2026-09-10T04:00:00Z"));
        reportDto.setExpectedStartTime(LocalTime.of(9, 0));
        reportDto.setGracePeriodMinutes(15);
        reportDto.setLateByMinutes(30);
        reportDto.setStatus("PRESENT");

        when(lateEarlyService.getLateAttendanceReport(any()))
                .thenReturn(new PageImpl<>(List.of(reportDto), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/attendance/late")
                        .param("fromDate", "2026-09-01")
                        .param("toDate", "2026-09-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].attendanceId").value(101))
                .andExpect(jsonPath("$.data.content[0].lateByMinutes").value(30));
    }

    @Test
    @DisplayName("GET /early-checkout: Returns early checkout report")
    void testGetEarlyCheckoutReport_Success() throws Exception {
        EarlyCheckoutReportDto reportDto = new EarlyCheckoutReportDto();
        reportDto.setAttendanceId(102L);
        reportDto.setEmployeeId(1L);
        reportDto.setEmployeeName("John Doe");
        reportDto.setDepartment("Engineering");
        reportDto.setTeam("Backend");
        reportDto.setDate(LocalDate.of(2026, 9, 10));
        reportDto.setCheckOutTime(Instant.parse("2026-09-10T10:30:00Z"));
        reportDto.setExpectedEndTime(LocalTime.of(18, 0));
        reportDto.setEarlyCheckoutThresholdMinutes(15);
        reportDto.setEarlyByMinutes(90);
        reportDto.setStatus("PRESENT");

        when(lateEarlyService.getEarlyCheckoutReport(any()))
                .thenReturn(new PageImpl<>(List.of(reportDto), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/attendance/early-checkout")
                        .param("fromDate", "2026-09-01")
                        .param("toDate", "2026-09-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].attendanceId").value(102))
                .andExpect(jsonPath("$.data.content[0].earlyByMinutes").value(90));
    }
}
