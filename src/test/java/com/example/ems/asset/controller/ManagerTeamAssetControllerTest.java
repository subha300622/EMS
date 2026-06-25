package com.example.ems.asset.controller;

import com.example.ems.asset.dto.TeamAssetDtos;
import com.example.ems.asset.service.ManagerTeamAssetService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ManagerTeamAssetControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ManagerTeamAssetService teamAssetService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private ManagerTeamAssetController managerTeamAssetController;

    private static final String TOKEN = "mock-manager-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "manager@example.com";
    private Employee mockManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(managerTeamAssetController).build();

        // Security mocks
        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);

        User user = new User();
        user.setWorkEmail(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));

        mockManager = new Employee();
        mockManager.setId(10L);
        mockManager.setEmployeeId("MGR001");
        mockManager.setFullName("Manager Name");
        mockManager.setEmail(EMAIL);
        mockManager.setDepartment("Engineering");
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockManager));
    }

    private void mockPermission(boolean allowed) {
        when(roleService.hasPermission(EMAIL, "team.read")).thenReturn(allowed);
    }

    @Test
    public void testGetDashboardSuccess() throws Exception {
        mockPermission(true);

        TeamAssetDtos.DashboardResponse response = new TeamAssetDtos.DashboardResponse(
            true,
            new TeamAssetDtos.DashboardData(5, 10, BigDecimal.valueOf(1177500), "₹11.8L", 1, 2)
        );
        when(teamAssetService.getTeamAssetsDashboard(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/manager/team-assets/dashboard")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.teamMembersWithAssets").value(5))
                .andExpect(jsonPath("$.data.totalAssetValueDisplay").value("₹11.8L"));
    }

    @Test
    public void testGetDashboardForbidden() throws Exception {
        mockPermission(false);

        mockMvc.perform(get("/api/v1/manager/team-assets/dashboard")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testGetTeamAssetsSuccess() throws Exception {
        mockPermission(true);

        TeamAssetDtos.EmployeeSummary emp = new TeamAssetDtos.EmployeeSummary(142L, "EMP-0142", "Arjun Mehta", "Sr Developer");
        TeamAssetDtos.InventoryItem item = new TeamAssetDtos.InventoryItem(
            501L, 8L, "AST-008", "Dell 27\" Monitor", "MONITOR", "SN-MON-0045",
            BigDecimal.valueOf(32500), "ASSIGNED", emp, LocalDate.of(2025, 1, 15)
        );
        Page<TeamAssetDtos.InventoryItem> page = new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1);

        when(teamAssetService.getTeamAssets(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/manager/team-assets")
                .header("Authorization", AUTH_HEADER)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].assetName").value("Dell 27\" Monitor"))
                .andExpect(jsonPath("$.data.content[0].employee.employeeName").value("Arjun Mehta"));
    }

    @Test
    public void testGetTeamAssetDetailsSuccess() throws Exception {
        mockPermission(true);

        TeamAssetDtos.EmployeeSummary emp = new TeamAssetDtos.EmployeeSummary(142L, "EMP-0142", "Arjun Mehta", "Sr Developer");
        TeamAssetDtos.DetailResponse details = new TeamAssetDtos.DetailResponse(
            8L, "AST-008", "Dell 27\" Monitor", "MONITOR", "Engineering", "SN-MON-0045",
            "ASSIGNED", BigDecimal.valueOf(32500), "GOOD", "External display", LocalDate.of(2025, 1, 15), emp
        );
        when(teamAssetService.getTeamAssetDetails(any(), eq(8L))).thenReturn(details);

        mockMvc.perform(get("/api/v1/manager/team-assets/8")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assetName").value("Dell 27\" Monitor"))
                .andExpect(jsonPath("$.data.notes").value("External display"));
    }

    @Test
    public void testGetTeamAssetTimelineSuccess() throws Exception {
        mockPermission(true);

        TeamAssetDtos.TimelineEvent event = new TeamAssetDtos.TimelineEvent("ASSIGNED", LocalDate.of(2025, 1, 15), "Assigned to Arjun Mehta");
        when(teamAssetService.getTeamAssetTimeline(any(), eq(8L))).thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/manager/team-assets/8/timeline")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].eventType").value("ASSIGNED"))
                .andExpect(jsonPath("$.data[0].description").value("Assigned to Arjun Mehta"));
    }

    @Test
    public void testGetPendingAssetRequestsSuccess() throws Exception {
        mockPermission(true);

        TeamAssetDtos.RequestItem requestItem = new TeamAssetDtos.RequestItem(
            101L, 145L, "Sneha Rao", "EMP-0145", "MONITOR", "Dell 32\" Monitor",
            "Additional display for testing", LocalDate.of(2026, 6, 20), "PENDING"
        );
        Page<TeamAssetDtos.RequestItem> page = new PageImpl<>(List.of(requestItem), PageRequest.of(0, 20), 1);

        when(teamAssetService.getPendingAssetRequests(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/manager/team-assets/requests")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].assetName").value("Dell 32\" Monitor"))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));
    }

    @Test
    public void testApproveAssetRequestSuccess() throws Exception {
        mockPermission(true);

        TeamAssetDtos.ApprovalRequest body = new TeamAssetDtos.ApprovalRequest("Approved");

        mockMvc.perform(put("/api/v1/manager/team-assets/requests/101/approve")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Asset request approved"));
    }

    @Test
    public void testRejectAssetRequestSuccess() throws Exception {
        mockPermission(true);

        TeamAssetDtos.ApprovalRequest body = new TeamAssetDtos.ApprovalRequest("Budget unavailable");

        mockMvc.perform(put("/api/v1/manager/team-assets/requests/101/reject")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Asset request rejected"));
    }

    @Test
    public void testGetPendingReturnRequestsSuccess() throws Exception {
        mockPermission(true);

        TeamAssetDtos.ReturnRequestItem returnItem = new TeamAssetDtos.ReturnRequestItem(
            301L, 12L, "AST-012", "Logitech MX Keys", 150L, "Leo Martinez",
            LocalDate.of(2026, 6, 18), "Resignation", "PENDING_RETURN"
        );
        Page<TeamAssetDtos.ReturnRequestItem> page = new PageImpl<>(List.of(returnItem), PageRequest.of(0, 20), 1);

        when(teamAssetService.getPendingReturnRequests(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/manager/team-assets/returns")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].assetName").value("Logitech MX Keys"))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING_RETURN"));
    }

    @Test
    public void testApproveReturnRequestSuccess() throws Exception {
        mockPermission(true);

        TeamAssetDtos.ReturnApprovalRequest body = new TeamAssetDtos.ReturnApprovalRequest("Verified");

        mockMvc.perform(put("/api/v1/manager/team-assets/returns/301/approve")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Asset return approved"));
    }

    @Test
    public void testGetAnalyticsSuccess() throws Exception {
        mockPermission(true);

        TeamAssetDtos.CategoryCountItem categoryCount = new TeamAssetDtos.CategoryCountItem("LAPTOP", 5L);
        TeamAssetDtos.AnalyticsResponse response = new TeamAssetDtos.AnalyticsResponse(
            true,
            new TeamAssetDtos.AnalyticsData(List.of(categoryCount), 9L, 1L, BigDecimal.valueOf(1177500))
        );
        when(teamAssetService.getTeamAssetsAnalytics(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/manager/team-assets/analytics")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assignedAssets").value(9))
                .andExpect(jsonPath("$.data.assetsByCategory[0].category").value("LAPTOP"));
    }
}
