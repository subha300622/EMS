package com.example.ems.auth.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.asset.repository.MyAssetRepository;
import com.example.ems.performance.repository.PerformanceReviewRepository;
import com.example.ems.support.repository.MySupportTicketRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private MyAssetRepository assetRepository;

    @Mock
    private PerformanceReviewRepository reviewRepository;

    @Mock
    private MySupportTicketRepository supportTicketRepository;

    @InjectMocks
    private MeController meController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "employee@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(meController).build();

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
    public void testGetMyProfileSuccess() throws Exception {
        mockPermission("employee.profile.read", true);
        Employee employee = new Employee();
        employee.setId(10L);
        employee.setEmail(EMAIL);
        employee.setFullName("Me Employee");
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/v1/me/profile")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Me Employee"));
    }

    @Test
    public void testUpdateMyProfileSuccess() throws Exception {
        mockPermission("employee.profile.update", true);
        Employee employee = new Employee();
        employee.setId(10L);
        employee.setEmail(EMAIL);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        mockMvc.perform(put("/api/v1/me/profile")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(Map.of("phone", "9876543210"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetMyDashboardSuccess() throws Exception {
        mockPermission("employee.dashboard.read", true);
        Employee employee = new Employee();
        employee.setId(10L);
        employee.setEmail(EMAIL);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));

        when(leaveRepository.findByEmployeeIdAndStatus(10L, "PENDING")).thenReturn(java.util.Collections.emptyList());
        when(expenseRepository.findByEmployeeId(10L)).thenReturn(java.util.Collections.emptyList());
        when(assetRepository.findByAssignedToId(10L)).thenReturn(java.util.Collections.emptyList());
        when(reviewRepository.findByEmployeeId(10L)).thenReturn(java.util.Collections.emptyList());
        when(supportTicketRepository.findByEmployeeEmail(EMAIL)).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/v1/me/dashboard")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pendingLeaves").value(0))
                .andExpect(jsonPath("$.data.pendingExpenses").value(0))
                .andExpect(jsonPath("$.data.assignedAssets").value(0))
                .andExpect(jsonPath("$.data.pendingReviews").value(0))
                .andExpect(jsonPath("$.data.openTickets").value(0));
    }
}
