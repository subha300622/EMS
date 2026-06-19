package com.example.ems.settings.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.security.service.JwtService;
import com.example.ems.settings.dto.RegenerateBackupCodesRequest;
import com.example.ems.settings.service.MySettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MySettingsBackupCodesTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MySettingsService mySettingsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private MySettingsController mySettingsController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "employee@example.com";
    private User mockUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(mySettingsController).build();

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setWorkEmail(EMAIL);
        mockUser.setUserId("USR-001");
    }

    private void mockAuthAndPermission(String permission, boolean allowed) {
        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(roleService.hasPermission(EMAIL, permission)).thenReturn(allowed);
        when(roleService.isSuperAdmin(EMAIL)).thenReturn(false);
    }

    @Test
    public void testGetBackupCodesInfoSuccess() throws Exception {
        mockAuthAndPermission("settings.security.read", true);

        Map<String, Object> mockResponse = Map.of(
                "remainingCodes", 10,
                "totalCodes", 10,
                "lastGeneratedAt", "2026-06-18T10:00:00Z",
                "expiresAt", "2026-09-16T10:00:00Z"
        );
        when(mySettingsService.getBackupCodesInfo(EMAIL)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/my-settings/security/2fa/backup-codes")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Backup codes info retrieved successfully"))
                .andExpect(jsonPath("$.data.remainingCodes").value(10))
                .andExpect(jsonPath("$.data.totalCodes").value(10))
                .andExpect(jsonPath("$.data.lastGeneratedAt").value("2026-06-18T10:00:00Z"))
                .andExpect(jsonPath("$.data.expiresAt").value("2026-09-16T10:00:00Z"));
    }

    @Test
    public void testGetBackupCodesInfoForbidden() throws Exception {
        mockAuthAndPermission("settings.security.read", false);

        mockMvc.perform(get("/api/v1/my-settings/security/2fa/backup-codes")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testGetBackupCodesInfoUnauthorized() throws Exception {
        when(jwtService.validateAccessToken(TOKEN)).thenReturn(false);

        mockMvc.perform(get("/api/v1/my-settings/security/2fa/backup-codes")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testRegenerateBackupCodesSuccess() throws Exception {
        mockAuthAndPermission("settings.security.update", true);

        RegenerateBackupCodesRequest request = new RegenerateBackupCodesRequest("CurrentPassword@123", "123456");
        Map<String, Object> mockResponse = new java.util.LinkedHashMap<>();
        mockResponse.put("generatedAt", "2026-06-18T10:00:00Z");
        mockResponse.put("expiresAt", "2026-09-16T10:00:00Z");
        mockResponse.put("remainingCodes", 10);
        mockResponse.put("backupCodes", List.of("A1B2-C3D4", "E5F6-G7H8"));
        when(mySettingsService.regenerateBackupCodes(eq(EMAIL), any(RegenerateBackupCodesRequest.class), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/my-settings/security/2fa/backup-codes/regenerate")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Backup codes regenerated successfully"))
                .andExpect(jsonPath("$.data.remainingCodes").value(10))
                .andExpect(jsonPath("$.data.expiresAt").value("2026-09-16T10:00:00Z"))
                .andExpect(jsonPath("$.data.backupCodes[0]").value("A1B2-C3D4"));
    }

    @Test
    public void testRegenerateBackupCodesInvalidPassword() throws Exception {
        mockAuthAndPermission("settings.security.update", true);

        RegenerateBackupCodesRequest request = new RegenerateBackupCodesRequest("WrongPassword@123", "123456");
        when(mySettingsService.regenerateBackupCodes(eq(EMAIL), any(RegenerateBackupCodesRequest.class), any()))
                .thenThrow(new IllegalArgumentException("INVALID_PASSWORD"));

        mockMvc.perform(post("/api/v1/my-settings/security/2fa/backup-codes/regenerate")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_PASSWORD"))
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    public void testRegenerateBackupCodesInvalidOtp() throws Exception {
        mockAuthAndPermission("settings.security.update", true);

        RegenerateBackupCodesRequest request = new RegenerateBackupCodesRequest("CurrentPassword@123", "000000");
        when(mySettingsService.regenerateBackupCodes(eq(EMAIL), any(RegenerateBackupCodesRequest.class), any()))
                .thenThrow(new IllegalArgumentException("INVALID_OTP"));

        mockMvc.perform(post("/api/v1/my-settings/security/2fa/backup-codes/regenerate")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_OTP"))
                .andExpect(jsonPath("$.message").value("Invalid verification code"));
    }

    @Test
    public void testRegenerateBackupCodesForbidden() throws Exception {
        mockAuthAndPermission("settings.security.update", false);

        RegenerateBackupCodesRequest request = new RegenerateBackupCodesRequest("CurrentPassword@123", "123456");

        mockMvc.perform(post("/api/v1/my-settings/security/2fa/backup-codes/regenerate")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
