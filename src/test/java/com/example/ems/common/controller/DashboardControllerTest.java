package com.example.ems.common.controller;

import com.example.ems.appraisal.repository.IncrementRepository;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.service.ApprovalCenterService;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.offboarding.repository.OffboardingRepository;
import com.example.ems.onboarding.repository.OnboardingRepository;
import com.example.ems.onboarding.service.OnboardingService;
import com.example.ems.performance.repository.PerformanceReviewRepository;
import com.example.ems.security.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private LeaveService leaveService;

    @Mock
    private OnboardingService onboardingService;

    @Mock
    private OnboardingRepository onboardingRepository;

    @Mock
    private OffboardingRepository offboardingRepository;

    @Mock
    private IncrementRepository incrementRepository;

    @Mock
    private PerformanceReviewRepository performanceReviewRepository;

    @Mock
    private ApprovalCenterService approvalCenterService;

    @InjectMocks
    private DashboardController dashboardController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "admin@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
    }

    @Test
    public void testGetDashboardAdminSuccess() throws Exception {
        User user = new User();
        user.setWorkEmail(EMAIL);
        Role role = new Role();
        role.setName("ADMIN");
        user.setRole(role);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));

        when(userRepository.count()).thenReturn(10L);
        when(employeeRepository.count()).thenReturn(8L);

        mockMvc.perform(get("/api/v1/dashboard")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.totalUsers").value(10))
                .andExpect(jsonPath("$.data.systemStatus").value("HEALTHY"));
    }

    @Test
    public void testGetWidgetsSuccess() throws Exception {
        User user = new User();
        user.setWorkEmail(EMAIL);
        Role role = new Role();
        role.setName("ADMIN");
        user.setRole(role);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/dashboard/widgets")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0]").value("system_status"));
    }
}
