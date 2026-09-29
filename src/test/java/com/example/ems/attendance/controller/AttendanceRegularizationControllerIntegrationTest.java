package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.CreateRegularizationRequest;
import com.example.ems.attendance.dto.RegularizationApprovalRequest;
import com.example.ems.attendance.dto.RegularizationResponseDto;
import com.example.ems.attendance.entity.AttendanceRegularizationStatus;
import com.example.ems.attendance.service.AttendanceRegularizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceRegularizationControllerIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private AttendanceRegularizationService regularizationService;

    @InjectMocks
    private AttendanceRegularizationController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("POST /regularizations: Successfully creates regularization request")
    void testCreateRegularization_Success() throws Exception {
        CreateRegularizationRequest request = new CreateRegularizationRequest(
                101L,
                Instant.parse("2026-09-10T09:00:00Z"),
                Instant.parse("2026-09-10T18:00:00Z"),
                "Forgot swipe out"
        );

        RegularizationResponseDto dto = new RegularizationResponseDto();
        dto.setId(501L);
        dto.setAttendanceId(101L);
        dto.setEmployeeId(125L);
        dto.setStatus(AttendanceRegularizationStatus.PENDING);

        when(regularizationService.createRegularization(any(CreateRegularizationRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/attendance/regularizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /regularizations: Retrieves paginated list")
    void testGetMyRegularizations_Success() throws Exception {
        RegularizationResponseDto dto = new RegularizationResponseDto();
        dto.setId(501L);
        dto.setStatus(AttendanceRegularizationStatus.PENDING);

        when(regularizationService.getMyRegularizations(any(), any(), any(), eq(0), eq(20)))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/attendance/regularizations")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(501));
    }

    @Test
    @DisplayName("GET /regularizations/{id}: Retrieves single regularization request")
    void testGetRegularizationById_Success() throws Exception {
        RegularizationResponseDto dto = new RegularizationResponseDto();
        dto.setId(501L);
        dto.setAttendanceId(101L);
        dto.setEmployeeId(125L);
        dto.setStatus(AttendanceRegularizationStatus.PENDING);

        when(regularizationService.getRegularizationById(501L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/attendance/regularizations/501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.employeeId").value(125))
                .andExpect(jsonPath("$.data.attendanceId").value(101))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /regularizations/{id}/approve: Successfully approves request")
    void testApproveRegularization_Success() throws Exception {
        RegularizationResponseDto dto = new RegularizationResponseDto();
        dto.setId(501L);
        dto.setStatus(AttendanceRegularizationStatus.APPROVED);

        when(regularizationService.approveRegularization(eq(501L), any(RegularizationApprovalRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/attendance/regularizations/501/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remarks\":\"Correction verified with building security log\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /regularizations/{id}/reject: Successfully rejects request")
    void testRejectRegularization_Success() throws Exception {
        RegularizationResponseDto dto = new RegularizationResponseDto();
        dto.setId(504L);
        dto.setStatus(AttendanceRegularizationStatus.REJECTED);
        dto.setRejectionReason("Correction could not be verified");

        when(regularizationService.rejectRegularization(eq(504L), any(RegularizationApprovalRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/attendance/regularizations/504/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remarks\":\"Correction could not be verified\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("POST /regularizations/{id}/cancel: Successfully cancels request")
    void testCancelRegularization_Success() throws Exception {
        RegularizationResponseDto dto = new RegularizationResponseDto();
        dto.setId(501L);
        dto.setStatus(AttendanceRegularizationStatus.CANCELLED);

        when(regularizationService.cancelRegularization(eq(501L), any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/attendance/regularizations/501/cancel")
                        .param("reason", "Cancelled by me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}
