package com.example.ems.audit.controller;

import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.service.AuditLogService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.security.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuditLogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private AuditLogController auditLogController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "admin@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(auditLogController).build();

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
    public void testGetAllLogsSuccess() throws Exception {
        mockPermission("audit.read", true);
        AuditLog log = new AuditLog("EMP001", "emp@example.com", "CREATE_EMPLOYEE", "Employee", "1", "127.0.0.1",
                "Created John");
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<AuditLog> page = new org.springframework.data.domain.PageImpl<>(
                List.of(log), pageable, 1);

        when(auditLogService.getFilteredLogs(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/audit-logs")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].action").value("CREATE_EMPLOYEE"));
    }

    @Test
    public void testGetLogByIdSuccess() throws Exception {
        mockPermission("audit.read", true);
        AuditLog log = new AuditLog("EMP001", "emp@example.com", "UPDATE_EMPLOYEE", "Employee", "1", "127.0.0.1",
                "Updated John");
        when(auditLogService.getLogById(1L)).thenReturn(Optional.of(log));

        mockMvc.perform(get("/api/v1/audit-logs/1")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.action").value("UPDATE_EMPLOYEE"));
    }

    @Test
    public void testExportLogsSuccess() throws Exception {
        mockPermission("audit.export", true);
        byte[] csvData = "ID,Timestamp,User ID,Action\n1,2026-06-17T12:00:00,EMP001,LOGIN".getBytes();
        when(auditLogService.exportLogsToCsv(org.mockito.ArgumentMatchers.any())).thenReturn(csvData);

        mockMvc.perform(get("/api/v1/audit-logs/export")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetDashboardStatsSuccess() throws Exception {
        mockPermission("audit.read", true);
        when(auditLogService.getDashboardStats(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Map.of("flaggedCount", 5L));

        mockMvc.perform(get("/api/v1/audit-logs/dashboard")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flaggedCount").value(5));
    }

    @Test
    public void testReviewLogSuccess() throws Exception {
        mockPermission("audit.read", true);
        AuditLog log = new AuditLog("EMP001", "emp@example.com", "UPDATE_EMPLOYEE", "Employee", "1", "127.0.0.1",
                "Updated John");
        when(auditLogService.reviewLog(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(log);

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/audit-logs/1/review")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"remarks\":\"Clear flag\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testDismissAllFlagsSuccess() throws Exception {
        mockPermission("audit.read", true);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/v1/audit-logs/dismiss-all")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetLogsByUserSuccess() throws Exception {
        mockPermission("audit.read", true);
        AuditLog log = new AuditLog("EMP002", "emp2@example.com", "LOGIN", "User", "EMP002", "127.0.0.1", "Logged in");
        when(auditLogService.getLogsByUser("EMP002")).thenReturn(List.of(log));

        mockMvc.perform(get("/api/v1/audit-logs/user/EMP002")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetLogsByEntitySuccess() throws Exception {
        mockPermission("audit.read", true);
        AuditLog log = new AuditLog("EMP002", "emp2@example.com", "DELETE_ASSET", "Asset", "10", "127.0.0.1",
                "Deleted Laptop");
        when(auditLogService.getLogsByEntity("Asset", "10")).thenReturn(List.of(log));

        mockMvc.perform(get("/api/v1/audit-logs/entity/Asset/10")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
