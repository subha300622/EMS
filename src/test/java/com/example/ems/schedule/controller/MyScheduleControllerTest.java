package com.example.ems.schedule.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.schedule.dto.*;
import com.example.ems.schedule.service.MyScheduleService;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MyScheduleControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private MyScheduleService scheduleService;
    @Mock private RoleService roleService;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    @InjectMocks
    private MyScheduleController scheduleController;

    private User empUser;
    private final String empEmail = "employee@company.com";
    private final String token = "mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController).build();

        empUser = new User();
        empUser.setWorkEmail(empEmail);
        empUser.setFullName("John Doe");

        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
    }

    private void setupMockPermissions(boolean allowed) {
        when(roleService.isSuperAdmin(empEmail)).thenReturn(false);
        when(roleService.hasPermission(eq(empEmail), any(String.class))).thenReturn(allowed);
    }

    @Test
    public void testGetDashboard() throws Exception {
        setupMockPermissions(true);
        MyScheduleDashboardResponse resp = new MyScheduleDashboardResponse();
        when(scheduleService.getDashboard(empEmail)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-schedule/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetCalendar() throws Exception {
        setupMockPermissions(true);
        MyCalendarResponse resp = new MyCalendarResponse(List.of());
        when(scheduleService.getCalendar(eq(empEmail), eq("MONTH"), eq("2026-06-01"), eq("2026-06-30"), any())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-schedule/calendar")
                .param("view", "MONTH")
                .param("startDate", "2026-06-01")
                .param("endDate", "2026-06-30")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetTodaySchedule() throws Exception {
        setupMockPermissions(true);
        TodayScheduleResponse resp = new TodayScheduleResponse();
        when(scheduleService.getTodaySchedule(empEmail)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-schedule/today")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetUpcomingSchedule() throws Exception {
        setupMockPermissions(true);
        UpcomingScheduleResponse resp = new UpcomingScheduleResponse(List.of());
        when(scheduleService.getUpcomingSchedule(eq(empEmail), eq(7), any())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-schedule/upcoming")
                .param("days", "7")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetShifts() throws Exception {
        setupMockPermissions(true);
        ShiftHistoryResponse resp = new ShiftHistoryResponse(List.of());
        when(scheduleService.getMyShiftHistory(eq(empEmail), eq("2026-06"), any())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-schedule/shifts")
                .param("month", "2026-06")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testCreateChangeRequest() throws Exception {
        setupMockPermissions(true);
        ChangeRequestPayload payload = new ChangeRequestPayload();
        payload.setCurrentShiftId(101L);
        payload.setRequestedShiftId(102L);
        payload.setRequestedDate("2026-06-20");

        ChangeRequestResponse resp = new ChangeRequestResponse();
        when(scheduleService.createChangeRequest(eq(empEmail), any(ChangeRequestPayload.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/my-schedule/change-requests")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetChangeRequests() throws Exception {
        setupMockPermissions(true);
        ChangeRequestListResponse resp = new ChangeRequestListResponse();
        when(scheduleService.getChangeRequests(eq(empEmail), any(), any())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-schedule/change-requests")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testUpdateAvailability() throws Exception {
        setupMockPermissions(true);
        AvailabilityRequest payload = new AvailabilityRequest();
        payload.setAvailability(List.of());

        AvailabilityResponse resp = new AvailabilityResponse();
        when(scheduleService.updateAvailability(eq(empEmail), any(AvailabilityRequest.class))).thenReturn(resp);

        mockMvc.perform(put("/api/v1/my-schedule/availability")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetTimeline() throws Exception {
        setupMockPermissions(true);
        ScheduleTimelineResponse resp = new ScheduleTimelineResponse(List.of());
        when(scheduleService.getTimeline(empEmail)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-schedule/timeline")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetNotifications() throws Exception {
        setupMockPermissions(true);
        ScheduleNotificationsResponse resp = new ScheduleNotificationsResponse(List.of());
        when(scheduleService.getNotifications(empEmail)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-schedule/notifications")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetPolicies() throws Exception {
        setupMockPermissions(true);
        SchedulePoliciesResponse resp = new SchedulePoliciesResponse();
        when(scheduleService.getPolicies()).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-schedule/policies")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
