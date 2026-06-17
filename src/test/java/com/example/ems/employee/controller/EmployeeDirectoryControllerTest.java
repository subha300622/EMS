package com.example.ems.employee.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.dto.*;
import com.example.ems.employee.service.MyEmployeeDirectoryService;
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

public class EmployeeDirectoryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private MyEmployeeDirectoryService directoryService;
    @Mock private RoleService roleService;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    @InjectMocks
    private EmployeeDirectoryController directoryController;

    private User empUser;
    private final String empEmail = "employee@company.com";
    private final String token = "mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(directoryController).build();

        empUser = new User();
        empUser.setWorkEmail(empEmail);
        empUser.setFullName("Arjun Mehta");

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
        EmployeeDirectoryDashboardResponse resp = new EmployeeDirectoryDashboardResponse();
        when(directoryService.getDashboard(empEmail)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/employees/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetMyTeam() throws Exception {
        setupMockPermissions(true);
        MyTeamResponse resp = new MyTeamResponse();
        when(directoryService.getMyTeam(empEmail)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/employees/my-team")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetEmployeeList() throws Exception {
        setupMockPermissions(true);
        EmployeeDirectoryListResponse resp = new EmployeeDirectoryListResponse();
        when(directoryService.getEmployeeList(any(), any(), any(), any(), any(), any(), any())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/employees")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetEmployeeProfile() throws Exception {
        setupMockPermissions(true);
        EmployeeProfileResponse resp = new EmployeeProfileResponse();
        when(directoryService.getEmployeeProfile(eq(101L))).thenReturn(resp);

        mockMvc.perform(get("/api/v1/employees/101")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testSearchEmployees() throws Exception {
        setupMockPermissions(true);
        EmployeeSearchResponse resp = new EmployeeSearchResponse(List.of(), 0);
        when(directoryService.searchEmployees(eq("raj"), any())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/employees/search")
                .param("keyword", "raj")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetEmployeeSkills() throws Exception {
        setupMockPermissions(true);
        EmployeeSkillsResponse resp = new EmployeeSkillsResponse();
        when(directoryService.getEmployeeSkills(eq(101L))).thenReturn(resp);

        mockMvc.perform(get("/api/v1/employees/101/skills")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetHierarchy() throws Exception {
        setupMockPermissions(true);
        EmployeeHierarchyResponse resp = new EmployeeHierarchyResponse();
        when(directoryService.getHierarchy(eq(101L))).thenReturn(resp);

        mockMvc.perform(get("/api/v1/employees/101/hierarchy")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetDepartments() throws Exception {
        setupMockPermissions(true);
        DepartmentListResponse resp = new DepartmentListResponse(List.of());
        when(directoryService.getDepartments()).thenReturn(resp);

        mockMvc.perform(get("/api/v1/employees/departments")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testSendMessage() throws Exception {
        setupMockPermissions(true);
        SendMessageRequest req = new SendMessageRequest("Subject", "Message");
        SendMessageResponse resp = new SendMessageResponse();
        when(directoryService.sendMessage(eq(empEmail), eq(101L), any(SendMessageRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/employees/101/messages")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetAvailability() throws Exception {
        setupMockPermissions(true);
        EmployeeAvailabilityResponse resp = new EmployeeAvailabilityResponse();
        when(directoryService.getAvailability(eq(101L))).thenReturn(resp);

        mockMvc.perform(get("/api/v1/employees/101/availability")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
