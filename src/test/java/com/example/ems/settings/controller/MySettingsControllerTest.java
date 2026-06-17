package com.example.ems.settings.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.security.service.JwtService;
import com.example.ems.settings.dto.ChangePasswordRequest;
import com.example.ems.settings.dto.SupportTicketRequest;
import com.example.ems.settings.service.MySettingsService;
import com.example.ems.support.dto.CreateTicketResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MySettingsControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private MySettingsService mySettingsService;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private RoleService roleService;

    @InjectMocks
    private MySettingsController mySettingsController;

    private User empUser;
    private final String empEmail = "employee@company.com";
    private final String token = "mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(mySettingsController).build();

        empUser = new User();
        empUser.setWorkEmail(empEmail);
        empUser.setEmployeeId("101");
    }

    private void setupAuthorized(String permission) {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
        when(roleService.hasPermission(empEmail, permission)).thenReturn(true);
    }

    private void setupForbidden(String permission) {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
        when(roleService.hasPermission(empEmail, permission)).thenReturn(false);
        when(roleService.isSuperAdmin(empEmail)).thenReturn(false);
    }

    @Test
    public void testGetDashboardSuccess() throws Exception {
        setupAuthorized("settings.self.read");

        Map<String, Object> dashboard = Map.of(
            "employeeId", 101L,
            "fullName", "John Doe",
            "email", empEmail
        );
        when(mySettingsService.getSettingsDashboard(empEmail)).thenReturn(dashboard);

        mockMvc.perform(get("/api/v1/my-settings")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(101));
    }

    @Test
    public void testGetDashboardForbidden() throws Exception {
        setupForbidden("settings.self.read");

        mockMvc.perform(get("/api/v1/my-settings")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testGetSecuritySettingsSuccess() throws Exception {
        setupAuthorized("settings.security.read");

        Map<String, Object> sec = Map.of("mfaEnabled", true, "passwordLastChanged", "2026-05-10T09:00:00Z");
        when(mySettingsService.getSecuritySettings(empEmail)).thenReturn(sec);

        mockMvc.perform(get("/api/v1/my-settings/security")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mfaEnabled").value(true));
    }

    @Test
    public void testChangePasswordSuccess() throws Exception {
        setupAuthorized("settings.security.update");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPassword123");
        request.setNewPassword("NewPassword123");
        request.setConfirmPassword("NewPassword123");

        mockMvc.perform(post("/api/v1/my-settings/security/change-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.changedAt").exists());
    }

    @Test
    public void testEnableMfaSuccess() throws Exception {
        setupAuthorized("settings.security.update");

        Map<String, Object> body = Map.of("enabled", true);

        mockMvc.perform(put("/api/v1/my-settings/security/mfa")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("MFA enabled successfully"));
    }

    @Test
    public void testGetPrivacySettingsSuccess() throws Exception {
        setupAuthorized("settings.privacy.read");

        Map<String, Object> privacy = Map.of("profileVisible", true, "showPhoneNumber", false);
        when(mySettingsService.getPrivacySettings(empEmail)).thenReturn(privacy);

        mockMvc.perform(get("/api/v1/my-settings/privacy")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.profileVisible").value(true));
    }

    @Test
    public void testUpdatePrivacySettingsSuccess() throws Exception {
        setupAuthorized("settings.privacy.update");

        Map<String, Object> body = Map.of("profileVisible", true, "showPhoneNumber", false);

        mockMvc.perform(put("/api/v1/my-settings/privacy")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Privacy settings updated successfully"));
    }

    @Test
    public void testGetNotificationPreferencesSuccess() throws Exception {
        setupAuthorized("settings.notifications.read");

        List<Map<String, Object>> prefs = List.of(Map.of("category", "LEAVE", "email", true, "push", true));
        when(mySettingsService.getNotificationPreferences(empEmail)).thenReturn(prefs);

        mockMvc.perform(get("/api/v1/my-settings/notifications")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].category").value("LEAVE"));
    }

    @Test
    public void testUpdateNotificationCategorySuccess() throws Exception {
        setupAuthorized("settings.notifications.update");

        Map<String, Object> body = Map.of("email", true, "push", false);

        mockMvc.perform(put("/api/v1/my-settings/notifications/LEAVE")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification preference for LEAVE updated successfully"));
    }

    @Test
    public void testGetNotificationTimingSuccess() throws Exception {
        setupAuthorized("settings.notifications.read");

        Map<String, Object> timing = Map.of("dailyDigestEnabled", true, "digestTime", "09:00");
        when(mySettingsService.getNotificationTiming(empEmail)).thenReturn(timing);

        mockMvc.perform(get("/api/v1/my-settings/notifications/timing")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.digestTime").value("09:00"));
    }

    @Test
    public void testUpdateNotificationTimingSuccess() throws Exception {
        setupAuthorized("settings.notifications.update");

        Map<String, Object> body = Map.of("dailyDigestEnabled", true, "digestTime", "10:00");

        mockMvc.perform(put("/api/v1/my-settings/notifications/timing")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification timing preferences updated successfully"));
    }

    @Test
    public void testGetAppearanceSuccess() throws Exception {
        setupAuthorized("settings.appearance.read");

        Map<String, Object> appearance = Map.of("theme", "DARK", "fontSize", "MEDIUM");
        when(mySettingsService.getAppearance(empEmail)).thenReturn(appearance);

        mockMvc.perform(get("/api/v1/my-settings/appearance")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.theme").value("DARK"));
    }

    @Test
    public void testUpdateAppearanceSuccess() throws Exception {
        setupAuthorized("settings.appearance.update");

        Map<String, Object> body = Map.of("theme", "LIGHT", "fontSize", "LARGE");

        mockMvc.perform(put("/api/v1/my-settings/appearance")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Appearance settings updated successfully"));
    }

    @Test
    public void testGetLanguageRegionSuccess() throws Exception {
        setupAuthorized("settings.language.read");

        Map<String, Object> lang = Map.of("language", "en", "timezone", "Asia/Kolkata");
        when(mySettingsService.getLanguageRegion(empEmail)).thenReturn(lang);

        mockMvc.perform(get("/api/v1/my-settings/language-region")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.language").value("en"));
    }

    @Test
    public void testUpdateLanguageRegionSuccess() throws Exception {
        setupAuthorized("settings.language.update");

        Map<String, Object> body = Map.of("language", "fr", "timezone", "Europe/Paris");

        mockMvc.perform(put("/api/v1/my-settings/language-region")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Language and region settings updated successfully"));
    }

    @Test
    public void testGetDevicesSuccess() throws Exception {
        setupAuthorized("settings.devices.read");

        List<Map<String, Object>> list = List.of(Map.of("deviceId", 1L, "deviceName", "Firefox Linux"));
        when(mySettingsService.getDevices(empEmail)).thenReturn(list);

        mockMvc.perform(get("/api/v1/my-settings/devices")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].deviceName").value("Firefox Linux"));
    }

    @Test
    public void testRemoveDeviceSuccess() throws Exception {
        setupAuthorized("settings.devices.remove");

        mockMvc.perform(delete("/api/v1/my-settings/devices/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Device removed successfully"));
    }

    @Test
    public void testExportDataSuccess() throws Exception {
        setupAuthorized("settings.data.export");

        Map<String, Object> mockResp = Map.of("requestId", "EXP-2026-001", "status", "PROCESSING");
        when(mySettingsService.exportData(empEmail)).thenReturn(mockResp);

        mockMvc.perform(post("/api/v1/my-settings/data/export")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requestId").value("EXP-2026-001"));
    }

    @Test
    public void testGetExportStatusSuccess() throws Exception {
        setupAuthorized("settings.data.export");

        Map<String, Object> mockResp = Map.of("requestId", "EXP-2026-001", "status", "COMPLETED", "downloadUrl", "url");
        when(mySettingsService.getExportStatus(empEmail, "EXP-2026-001")).thenReturn(mockResp);

        mockMvc.perform(get("/api/v1/my-settings/data/export/EXP-2026-001")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    public void testDownloadExportedCsvSuccess() throws Exception {
        setupAuthorized("settings.data.export");

        byte[] csv = "header,value\n".getBytes();
        when(mySettingsService.getExportedDataCsv(empEmail, "EXP-2026-001")).thenReturn(csv);

        mockMvc.perform(get("/api/v1/my-settings/data/export/EXP-2026-001/download")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    byte[] content = result.getResponse().getContentAsByteArray();
                    assert content.length > 0;
                });
    }

    @Test
    public void testGetFaqsSuccess() throws Exception {
        setupAuthorized("settings.support.read");

        List<Map<String, Object>> list = List.of(Map.of("id", 1, "question", "Q?"));
        when(mySettingsService.getFaqs()).thenReturn(list);

        mockMvc.perform(get("/api/v1/my-settings/faqs")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].question").value("Q?"));
    }

    @Test
    public void testCreateSupportTicketSuccess() throws Exception {
        setupAuthorized("settings.support.create");

        SupportTicketRequest request = new SupportTicketRequest();
        request.setCategory("ACCOUNT");
        request.setSubject("Unable to login");
        request.setDescription("Password reset link not working");

        CreateTicketResponse ticketResp = new CreateTicketResponse();
        ticketResp.setTicketNumber("SUP-2026-001");
        ticketResp.setStatus("OPEN");
        when(mySettingsService.createSupportRequest(eq(empEmail), any(SupportTicketRequest.class))).thenReturn(ticketResp);

        mockMvc.perform(post("/api/v1/my-settings/support-tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketId").value("SUP-2026-001"))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }
}
