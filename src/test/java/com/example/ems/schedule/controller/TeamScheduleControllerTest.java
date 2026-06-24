package com.example.ems.schedule.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.schedule.dto.AssignShiftRequest;
import com.example.ems.schedule.dto.TeamScheduleOverviewDto;
import com.example.ems.schedule.dto.TeamScheduleResponse;
import com.example.ems.schedule.entity.ShiftType;
import com.example.ems.schedule.service.TeamScheduleService;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeamScheduleControllerTest {

        private MockMvc mockMvc;

        @Mock
        private TeamScheduleService teamScheduleService;

        @Mock
        private UserRepository userRepository;

        @Mock
        private EmployeeRepository employeeRepository;

        @Mock
        private RoleService roleService;

        @Mock
        private JwtService jwtService;

        @InjectMocks
        private TeamScheduleController teamScheduleController;

        private static final String TOKEN = "mock-token";
        private static final String AUTH_HEADER = "Bearer " + TOKEN;
        private static final String EMAIL = "manager@example.com";

        private User currentUser;

        @BeforeEach
        public void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(teamScheduleController).build();

                currentUser = new User();
                currentUser.setWorkEmail(EMAIL);

                when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
                when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
                when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(currentUser));
        }

        @Test
        public void testGetTeamScheduleSuccess() throws Exception {
                when(roleService.hasPermission(EMAIL, "employee.schedule.read")).thenReturn(true);

                TeamScheduleResponse mockResponse = new TeamScheduleResponse(
                                new TeamScheduleOverviewDto(95.0, 1, 0, 10, 90.0, 5.0, 2),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                new com.example.ems.schedule.dto.OvertimeSummaryDto(0.0, Collections.emptyList()));

                when(teamScheduleService.getTeamSchedule(any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(mockResponse);

                mockMvc.perform(get("/api/v1/team-schedule")
                                .header("Authorization", AUTH_HEADER)
                                .param("startDate", "2026-06-20")
                                .param("endDate", "2026-06-26"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.overview.coveragePercentage").value(95.0))
                                .andExpect(jsonPath("$.data.overview.totalShifts").value(10));
        }

        @Test
        public void testGetTeamScheduleForbidden() throws Exception {
                when(roleService.hasPermission(EMAIL, "employee.schedule.read")).thenReturn(false);
                when(roleService.hasPermission(EMAIL, "attendance.team.read")).thenReturn(false);
                when(roleService.hasRoleOrGreater(any(), eq("MANAGER"))).thenReturn(false);

                mockMvc.perform(get("/api/v1/team-schedule")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        public void testAssignShiftSuccess() throws Exception {
                when(roleService.hasPermission(EMAIL, "employee.schedule.write")).thenReturn(true);

                AssignShiftRequest request = new AssignShiftRequest();
                request.setEmployeeId(1L);
                request.setDate(LocalDate.of(2026, 6, 20));
                request.setShiftType(ShiftType.MORNING);

                mockMvc.perform(post("/api/v1/team-schedule/shifts")
                                .header("Authorization", AUTH_HEADER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper()
                                                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                                                .writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));

                verify(teamScheduleService, times(1)).assignShift(any());
        }

        @Test
        public void testAssignShiftInvalidType() throws Exception {
                when(roleService.hasPermission(EMAIL, "employee.schedule.write")).thenReturn(true);

                String invalidPayload = "{\"employeeId\":1,\"date\":\"2026-06-20\",\"shiftType\":\"MID_DAY\"}";

                mockMvc.perform(post("/api/v1/team-schedule/shifts")
                                .header("Authorization", AUTH_HEADER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidPayload))
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void testApproveSwapSuccess() throws Exception {
                when(roleService.hasPermission(EMAIL, "employee.schedule.write")).thenReturn(true);

                mockMvc.perform(post("/api/v1/team-schedule/swap-requests/1/approve")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));

                verify(teamScheduleService, times(1)).approveSwap(1L);
        }

        @Test
        public void testApproveSwapConflict() throws Exception {
                when(roleService.hasPermission(EMAIL, "employee.schedule.write")).thenReturn(true);

                doThrow(new IllegalStateException("Swap request has already been processed"))
                                .when(teamScheduleService).approveSwap(1L);

                mockMvc.perform(post("/api/v1/team-schedule/swap-requests/1/approve")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.errorCode").value("VAL_002"));
        }

        @Test
        public void testApproveSwapNotFound() throws Exception {
                when(roleService.hasPermission(EMAIL, "employee.schedule.write")).thenReturn(true);

                doThrow(new IllegalArgumentException("Swap request not found"))
                                .when(teamScheduleService).approveSwap(99L);

                mockMvc.perform(post("/api/v1/team-schedule/swap-requests/99/approve")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.errorCode").value("VAL_001"));
        }
}
