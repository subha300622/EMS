package com.example.ems.common.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.manager.*;
import com.example.ems.common.service.ManagerNotificationService;
import com.example.ems.security.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManagerNotificationService managerNotificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private NotificationController notificationController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "user@example.com";

    private User currentUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();

        currentUser = new User();
        currentUser.setId(10L);
        currentUser.setWorkEmail(EMAIL);

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(currentUser));
    }

    @Test
    public void testGetNotificationFeedSuccess() throws Exception {
        NotificationDto dto = new NotificationDto(1L, "Title", "Msg", "APPROVAL", "HIGH", false, "2026-06-22T09:30:00Z");
        Page<NotificationDto> page = new PageImpl<>(List.of(dto), org.springframework.data.domain.PageRequest.of(0, 20), 1);

        when(managerNotificationService.getNotificationFeed(eq(currentUser), eq(0), eq(20), eq("ALL"), eq("UNREAD")))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", AUTH_HEADER)
                        .param("page", "0")
                        .param("size", "20")
                        .param("type", "ALL")
                        .param("status", "UNREAD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Title"));
    }


    @Test
    public void testGetUnreadCountSuccess() throws Exception {
        when(managerNotificationService.getUnreadCount(currentUser)).thenReturn(new UnreadCountDto(5L));

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.count").value(5));
    }

    @Test
    public void testMarkAsReadSuccess() throws Exception {
        doNothing().when(managerNotificationService).markAsRead(currentUser, 1L);

        mockMvc.perform(put("/api/v1/notifications/1/read")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testMarkAllAsReadSuccess() throws Exception {
        when(managerNotificationService.markAllAsRead(currentUser)).thenReturn(5);

        mockMvc.perform(put("/api/v1/notifications/read-all")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testDeleteNotificationSuccess() throws Exception {
        doNothing().when(managerNotificationService).deleteNotification(currentUser, 1L);

        mockMvc.perform(delete("/api/v1/notifications/1")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetPreferencesSuccess() throws Exception {
        NotificationPreferenceDto dto = new NotificationPreferenceDto(true, true, true, true, true);
        when(managerNotificationService.getPreferences(currentUser)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/notifications/preferences")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.emailNotifications").value(true));
    }

    @Test
    public void testUpdatePreferencesSuccess() throws Exception {
        NotificationPreferenceDto responseDto = new NotificationPreferenceDto(true, false, true, true, true);
        when(managerNotificationService.updatePreferences(eq(currentUser), any(NotificationPreferenceDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/notifications/preferences")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailNotifications\":true,\"pushNotifications\":false,\"approvalAlerts\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pushNotifications").value(false));
    }

    @Test
    public void testGetStatsSuccess() throws Exception {
        NotificationStatsDto dto = new NotificationStatsDto(100, 5, 20, 10, 15);
        when(managerNotificationService.getStats(currentUser)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/notifications/stats")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(100))
                .andExpect(jsonPath("$.data.unread").value(5));
    }

    @Test
    public void testGetPageDataSuccess() throws Exception {
        NotificationStatsDto stats = new NotificationStatsDto(100, 5, 20, 10, 15);
        NotificationPreferenceDto prefs = new NotificationPreferenceDto(true, true, true, true, true);
        NotificationPageResponse response = new NotificationPageResponse(stats, 5, List.of(), List.of(), prefs);
        
        when(managerNotificationService.getPageData(currentUser)).thenReturn(response);

        mockMvc.perform(get("/api/v1/notifications/page-data")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(5))
                .andExpect(jsonPath("$.data.stats.total").value(100));
    }
}
