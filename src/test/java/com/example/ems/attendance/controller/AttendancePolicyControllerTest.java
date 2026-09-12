package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.policy.AttendancePolicyDto;
import com.example.ems.attendance.dto.policy.CreateAttendancePolicyRequest;
import com.example.ems.attendance.dto.policy.UpdateAttendancePolicyRequest;
import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.entity.AttendancePolicyStatus;
import com.example.ems.attendance.service.AttendancePolicyService;
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

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AttendancePolicyControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private AttendancePolicyService policyService;

    @InjectMocks
    private AttendancePolicyController controller;

    private AttendancePolicyDto policyDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        AttendancePolicy policy = new AttendancePolicy();
        policy.setId(1L);
        policy.setName("General Shift");
        policy.setOfficeStartTime(LocalTime.of(9, 0));
        policy.setOfficeEndTime(LocalTime.of(18, 0));
        policy.setGracePeriodMinutes(15);
        policy.setMinimumWorkingMinutes(480);
        policy.setHalfDayThreshold(240);
        policy.setLateThreshold(15);
        policy.setEarlyCheckoutThreshold(15);
        policy.setMaximumBreakMinutes(60);
        policy.setStatus(AttendancePolicyStatus.ACTIVE);

        policyDto = AttendancePolicyDto.fromEntity(policy);
    }

    @Test
    @DisplayName("POST /policies: Successfully creates a new policy")
    void testCreatePolicy_Success() throws Exception {
        CreateAttendancePolicyRequest request = new CreateAttendancePolicyRequest();
        request.setName("General Shift");
        request.setOfficeStartTime(LocalTime.of(9, 0));
        request.setOfficeEndTime(LocalTime.of(18, 0));
        request.setGracePeriodMinutes(15);
        request.setMinimumWorkingMinutes(480);
        request.setHalfDayThreshold(240);
        request.setLateThreshold(15);
        request.setEarlyCheckoutThreshold(15);
        request.setMaximumBreakMinutes(60);

        when(policyService.createPolicy(any(CreateAttendancePolicyRequest.class))).thenReturn(policyDto);

        mockMvc.perform(post("/api/v1/attendance/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("General Shift"));
    }

    @Test
    @DisplayName("GET /policies: Returns paginated list of policies")
    void testGetPolicies_Success() throws Exception {
        when(policyService.getAllPolicies(any(), eq(0), eq(20)))
                .thenReturn(new PageImpl<>(List.of(policyDto), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/attendance/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    @DisplayName("GET /policies/{id}: Returns single policy details")
    void testGetPolicyById_Success() throws Exception {
        when(policyService.getPolicyById(1L)).thenReturn(policyDto);

        mockMvc.perform(get("/api/v1/attendance/policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("PUT /policies/{id}: Updates existing policy")
    void testUpdatePolicy_Success() throws Exception {
        UpdateAttendancePolicyRequest request = new UpdateAttendancePolicyRequest();
        request.setName("Updated Shift");
        request.setOfficeStartTime(LocalTime.of(9, 30));
        request.setOfficeEndTime(LocalTime.of(18, 30));
        request.setGracePeriodMinutes(20);

        when(policyService.updatePolicy(eq(1L), any(UpdateAttendancePolicyRequest.class))).thenReturn(policyDto);

        mockMvc.perform(put("/api/v1/attendance/policies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /policies/{id}/activate: Activates policy")
    void testActivatePolicy_Success() throws Exception {
        when(policyService.activatePolicy(1L)).thenReturn(policyDto);

        mockMvc.perform(post("/api/v1/attendance/policies/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /policies/{id}/deactivate: Deactivates policy")
    void testDeactivatePolicy_Success() throws Exception {
        AttendancePolicy policy = new AttendancePolicy();
        policy.setId(1L);
        policy.setName("General Shift");
        policy.setStatus(AttendancePolicyStatus.INACTIVE);
        AttendancePolicyDto inactiveDto = AttendancePolicyDto.fromEntity(policy);

        when(policyService.deactivatePolicy(1L)).thenReturn(inactiveDto);

        mockMvc.perform(post("/api/v1/attendance/policies/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }
}
