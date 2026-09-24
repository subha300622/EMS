package com.example.ems.onboarding.controller;

import com.example.ems.onboarding.dto.notification.OnboardingNotificationRemindRequest;
import com.example.ems.onboarding.dto.notification.OnboardingNotificationResendRequest;
import com.example.ems.onboarding.service.OnboardingNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OnboardingNotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OnboardingNotificationService notificationService;

    @InjectMocks
    private OnboardingNotificationController notificationController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
    }

    @Test
    @DisplayName("GET /api/v1/onboarding/{onboardingId}/notifications returns notifications log")
    public void testGetNotificationsSuccess() throws Exception {
        Map<String, Object> logItem = Map.of(
                "id", 101L,
                "type", "WELCOME_EMAIL",
                "recipient", "newhire@example.com",
                "status", "SENT"
        );
        when(notificationService.getNotifications(1L)).thenReturn(List.of(logItem));

        mockMvc.perform(get("/api/v1/onboarding/1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notifications log retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(101))
                .andExpect(jsonPath("$.data[0].type").value("WELCOME_EMAIL"));

        verify(notificationService).getNotifications(1L);
    }

    @Test
    @DisplayName("POST /api/v1/onboarding/{onboardingId}/notifications/remind dispatches reminder event")
    public void testSendReminderSuccess() throws Exception {
        OnboardingNotificationRemindRequest request = new OnboardingNotificationRemindRequest();
        request.setTaskId(55L);
        request.setChannel("EMAIL");

        doNothing().when(notificationService).sendReminder(eq(1L), any(OnboardingNotificationRemindRequest.class));

        mockMvc.perform(post("/api/v1/onboarding/1/notifications/remind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reminder event dispatched successfully"));

        verify(notificationService).sendReminder(eq(1L), any(OnboardingNotificationRemindRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/onboarding/{onboardingId}/notifications/remind with missing taskId returns 400 Bad Request")
    public void testSendReminderValidationError() throws Exception {
        OnboardingNotificationRemindRequest request = new OnboardingNotificationRemindRequest();
        // taskId is null -> violates @NotNull

        mockMvc.perform(post("/api/v1/onboarding/1/notifications/remind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/onboarding/{onboardingId}/notifications/resend resends notification successfully")
    public void testResendNotificationSuccess() throws Exception {
        OnboardingNotificationResendRequest request = new OnboardingNotificationResendRequest();
        request.setNotificationType("ONBOARDING_WELCOME");
        request.setChannel("EMAIL");

        doNothing().when(notificationService).resendNotification(eq(1L), any(OnboardingNotificationResendRequest.class));

        mockMvc.perform(post("/api/v1/onboarding/1/notifications/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification event resent successfully"));

        verify(notificationService).resendNotification(eq(1L), any(OnboardingNotificationResendRequest.class));
    }
}
