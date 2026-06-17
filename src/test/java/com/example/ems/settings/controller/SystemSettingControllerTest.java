package com.example.ems.settings.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.security.service.JwtService;
import com.example.ems.settings.entity.SystemSetting;
import com.example.ems.settings.service.SystemSettingService;

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
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SystemSettingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SystemSettingService systemSettingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private SystemSettingController systemSettingController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "admin@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(systemSettingController).build();

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        User user = new User();
        user.setWorkEmail(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    private void mockPermission(String permission, boolean allowed) {
        when(roleService.hasPermission(EMAIL, permission)).thenReturn(allowed);
    }

    @Test
    public void testGetAllSettingsSuccess() throws Exception {
        mockPermission("settings.manage", true);
        SystemSetting setting = new SystemSetting("company.name", "Enterprise Inc.", "company");
        when(systemSettingService.getAllSettings()).thenReturn(List.of(setting));

        mockMvc.perform(get("/api/v1/settings")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].settingKey").value("company.name"));
    }

    @Test
    public void testUpdateSettingsSuccess() throws Exception {
        mockPermission("settings.manage", true);
        Map<String, String> payload = Map.of("company.name", "New Enterprise Inc.");
        SystemSetting setting = new SystemSetting("company.name", "New Enterprise Inc.", "company");
        when(systemSettingService.updateSetting("company.name", "New Enterprise Inc.", "company")).thenReturn(setting);

        mockMvc.perform(put("/api/v1/settings")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetCompanySettingsSuccess() throws Exception {
        mockPermission("settings.manage", true);
        SystemSetting setting = new SystemSetting("company.name", "Enterprise Inc.", "company");
        when(systemSettingService.getSettingsByCategory("company")).thenReturn(List.of(setting));

        mockMvc.perform(get("/api/v1/settings/company")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetPasswordPolicySettingsSuccess() throws Exception {
        mockPermission("settings.manage", true);
        SystemSetting setting = new SystemSetting("password-policy.min_length", "8", "password-policy");
        when(systemSettingService.getSettingsByCategory("password-policy")).thenReturn(List.of(setting));

        mockMvc.perform(get("/api/v1/settings/password-policy")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
