package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.adjustment.AdjustmentApprovalRequest;
import com.example.ems.attendance.dto.adjustment.AdjustmentResponseDto;
import com.example.ems.attendance.dto.adjustment.CreateAdjustmentRequest;
import com.example.ems.attendance.entity.AttendanceAdjustmentStatus;
import com.example.ems.attendance.service.AttendanceAdjustmentService;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AttendanceAdjustmentControllerIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private AttendanceAdjustmentService adjustmentService;

    @InjectMocks
    private AttendanceAdjustmentController controller;

    private AdjustmentResponseDto sampleDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleDto = new AdjustmentResponseDto();
        sampleDto.setId(501L);
        sampleDto.setAttendanceId(101L);
        sampleDto.setEmployeeId(1L);
        sampleDto.setEmployeeName("John Doe");
        sampleDto.setAttendanceDate(LocalDate.of(2026, 9, 10));
        sampleDto.setRequestedCheckInTime(Instant.parse("2026-09-10T03:30:00Z"));
        sampleDto.setRequestedCheckOutTime(Instant.parse("2026-09-10T12:30:00Z"));
        sampleDto.setReason("Adjustment reason");
        sampleDto.setStatus(AttendanceAdjustmentStatus.PENDING);
    }

    @Test
    @DisplayName("POST /{attendanceId}/adjustments: Successfully creates an adjustment request")
    void testCreateAdjustment_Success() throws Exception {
        CreateAdjustmentRequest request = new CreateAdjustmentRequest(
                Instant.parse("2026-09-10T03:30:00Z"),
                Instant.parse("2026-09-10T12:30:00Z"),
                "Adjustment reason"
        );

        when(adjustmentService.createAdjustment(eq(101L), any(CreateAdjustmentRequest.class)))
                .thenReturn(sampleDto);

        mockMvc.perform(post("/api/v1/attendance/101/adjustments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /adjustments: Returns paginated adjustment requests")
    void testGetAdjustments_Success() throws Exception {
        when(adjustmentService.getAdjustments(any(), any(), any(), any(), eq(0), eq(20)))
                .thenReturn(new PageImpl<>(List.of(sampleDto), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/attendance/adjustments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(501));
    }

    @Test
    @DisplayName("GET /adjustments/{id}: Returns single adjustment details")
    void testGetAdjustmentById_Success() throws Exception {
        when(adjustmentService.getAdjustmentById(501L)).thenReturn(sampleDto);

        mockMvc.perform(get("/api/v1/attendance/adjustments/501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501));
    }

    @Test
    @DisplayName("POST /adjustments/{id}/cancel: Successfully cancels adjustment")
    void testCancelAdjustment_Success() throws Exception {
        AdjustmentResponseDto cancelledDto = new AdjustmentResponseDto();
        cancelledDto.setId(501L);
        cancelledDto.setStatus(AttendanceAdjustmentStatus.CANCELLED);
        cancelledDto.setManagerNotes("Cancelled by requester");

        when(adjustmentService.cancelAdjustment(eq(501L), any())).thenReturn(cancelledDto);

        mockMvc.perform(post("/api/v1/attendance/adjustments/501/cancel")
                        .param("reason", "Cancelled by requester"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("POST /adjustments/{id}/approve: Successfully approves adjustment")
    void testApproveAdjustment_Success() throws Exception {
        AdjustmentResponseDto approvedDto = new AdjustmentResponseDto();
        approvedDto.setId(501L);
        approvedDto.setStatus(AttendanceAdjustmentStatus.APPROVED);
        approvedDto.setApprovedBy("MANAGER");
        approvedDto.setManagerNotes("Approved remarks");

        AdjustmentApprovalRequest request = new AdjustmentApprovalRequest("Approved remarks");

        when(adjustmentService.approveAdjustment(eq(501L), any())).thenReturn(approvedDto);

        mockMvc.perform(post("/api/v1/attendance/adjustments/501/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /adjustments/{id}/reject: Successfully rejects adjustment")
    void testRejectAdjustment_Success() throws Exception {
        AdjustmentResponseDto rejectedDto = new AdjustmentResponseDto();
        rejectedDto.setId(501L);
        rejectedDto.setStatus(AttendanceAdjustmentStatus.REJECTED);
        rejectedDto.setRejectionReason("Mismatch in timings");

        AdjustmentApprovalRequest request = new AdjustmentApprovalRequest("Mismatch in timings");

        when(adjustmentService.rejectAdjustment(eq(501L), any())).thenReturn(rejectedDto);

        mockMvc.perform(post("/api/v1/attendance/adjustments/501/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }
}
