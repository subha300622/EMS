package com.example.ems.common.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.service.HrDashboardCacheService;
import com.example.ems.security.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HrDashboardControllerTest {

        private MockMvc mockMvc;

        @Mock
        private HrDashboardCacheService hrDashboardCacheService;

        @Mock
        private UserRepository userRepository;

        @Mock
        private RoleService roleService;

        @Mock
        private JwtService jwtService;

        @InjectMocks
        private HrDashboardController hrDashboardController;

        private static final String TOKEN = "mock-token";
        private static final String AUTH_HEADER = "Bearer " + TOKEN;
        private static final String EMAIL = "hr@example.com";

        private User hrUser;

        @BeforeEach
        public void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(hrDashboardController).build();

                hrUser = new User();
                hrUser.setWorkEmail(EMAIL);
                Role role = new Role();
                role.setName("HR");
                hrUser.setRole(role);

                when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
                when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
                when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(hrUser));
                when(roleService.hasPermission(EMAIL, "recruitment.manage")).thenReturn(true);
        }

        @Test
        public void testGetDashboardSuccess() throws Exception {
                Map<String, Object> mockSummary = Map.of(
                                "totalEmployees", 1284,
                                "newHires", 24,
                                "attritionRate", 1.2);
                when(hrDashboardCacheService.getDashboardSummary()).thenReturn(mockSummary);

                mockMvc.perform(get("/api/v1/hr/dashboard")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.totalEmployees").value(1284));
        }

        @Test
        public void testGetHeadcountSuccess() throws Exception {
                Map<String, Object> mockHeadcount = Map.of(
                                "totalEmployees", 1284,
                                "activeEmployees", 1245,
                                "inactiveEmployees", 39);
                when(hrDashboardCacheService.getHeadcountStats()).thenReturn(mockHeadcount);

                mockMvc.perform(get("/api/v1/hr/dashboard/headcount")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.activeEmployees").value(1245));
        }

        @Test
        public void testGetNewHiresSuccess() throws Exception {
                Map<String, Object> mockNewHires = Map.of(
                                "count", 24,
                                "period", "Last 30 Days");
                when(hrDashboardCacheService.getNewHiresStats()).thenReturn(mockNewHires);

                mockMvc.perform(get("/api/v1/hr/dashboard/new-hires")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.count").value(24));
        }

        @Test
        public void testGetAttritionSuccess() throws Exception {
                Map<String, Object> mockAttrition = Map.of(
                                "currentMonth", 1.2,
                                "lastMonth", 1.4);
                when(hrDashboardCacheService.getAttritionStats()).thenReturn(mockAttrition);

                mockMvc.perform(get("/api/v1/hr/dashboard/attrition")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.currentMonth").value(1.2));
        }

        @Test
        public void testGetOpenPositionsSuccess() throws Exception {
                Map<String, Object> mockOpenPositions = Map.of(
                                "total", 18,
                                "highPriority", 5);
                when(hrDashboardCacheService.getOpenPositionsStats()).thenReturn(mockOpenPositions);

                mockMvc.perform(get("/api/v1/hr/dashboard/open-positions")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.total").value(18));
        }

        @Test
        public void testGetHeadcountTrendSuccess() throws Exception {
                Map<String, Object> mockTrend = Map.of(
                                "labels", List.of("Oct", "Nov"),
                                "values", List.of(1200L, 1215L));
                when(hrDashboardCacheService.getHeadcountTrend("6months")).thenReturn(mockTrend);

                mockMvc.perform(get("/api/v1/hr/dashboard/charts/headcount-trend")
                                .param("period", "6months")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.labels[0]").value("Oct"));
        }

        @Test
        public void testGetEmployeeBreakdownSuccess() throws Exception {
                Map<String, Object> mockBreakdown = Map.of(
                                "departments", List.of(Map.of("name", "Engineering", "percentage", 35)));
                when(hrDashboardCacheService.getEmployeeBreakdown()).thenReturn(mockBreakdown);

                mockMvc.perform(get("/api/v1/hr/dashboard/charts/employee-breakdown")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.departments[0].name").value("Engineering"));
        }

        @Test
        public void testGetPendingLeavesSuccess() throws Exception {
                List<Map<String, Object>> mockLeaves = List.of(Map.of("leaveId", 1L, "employeeName", "Sarah"));
                when(hrDashboardCacheService.getPendingLeaves()).thenReturn(mockLeaves);

                mockMvc.perform(get("/api/v1/hr/dashboard/pending-leaves")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].employeeName").value("Sarah"));
        }

        @Test
        public void testGetRecentHiresSuccess() throws Exception {
                List<Map<String, Object>> mockHires = List.of(Map.of("employeeId", 101L, "employeeName", "Michael"));
                when(hrDashboardCacheService.getRecentHires()).thenReturn(mockHires);

                mockMvc.perform(get("/api/v1/hr/dashboard/recent-hires")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].employeeName").value("Michael"));
        }

        @Test
        public void testGetAttendanceByDeptSuccess() throws Exception {
                List<Map<String, Object>> mockAttendance = List
                                .of(Map.of("department", "Engineering", "attendance", 94));
                when(hrDashboardCacheService.getAttendanceByDepartment()).thenReturn(mockAttendance);

                mockMvc.perform(get("/api/v1/hr/dashboard/attendance-by-department")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].department").value("Engineering"));
        }

        @Test
        public void testGetRetentionAlertsSuccess() throws Exception {
                List<Map<String, Object>> mockAlerts = List.of(Map.of("severity", "HIGH", "department", "Sales"));
                when(hrDashboardCacheService.getRetentionAlerts()).thenReturn(mockAlerts);

                mockMvc.perform(get("/api/v1/hr/dashboard/retention-alerts")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].severity").value("HIGH"));
        }

        @Test
        public void testGlobalSearchSuccess() throws Exception {
                Map<String, Object> mockResults = Map.of(
                                "employees", List.of(Map.of("fullName", "John Doe")));
                when(hrDashboardCacheService.globalSearch("john")).thenReturn(mockResults);

                mockMvc.perform(get("/api/v1/hr/dashboard/search")
                                .param("keyword", "john")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.employees[0].fullName").value("John Doe"));
        }

        @Test
        public void testGetDashboardSummaryAggregationSuccess() throws Exception {
                Map<String, Object> mockAgg = Map.of(
                                "stats", Map.of("totalEmployees", 1284));
                when(hrDashboardCacheService.getDashboardSummaryAggregation()).thenReturn(mockAgg);

                mockMvc.perform(get("/api/v1/hr/dashboard/summary")
                                .header("Authorization", AUTH_HEADER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.stats.totalEmployees").value(1284));
        }

        @Test
        public void testGetDashboardManagerSuccess() throws Exception {
                User manager = new User();
                manager.setWorkEmail("sarah.chen@example.com");
                Role role = new Role();
                role.setName("MANAGER");
                manager.setRole(role);

                when(jwtService.validateAccessToken("mgr-token")).thenReturn(true);
                when(jwtService.getEmailFromToken("mgr-token")).thenReturn("sarah.chen@example.com");
                when(userRepository.findByWorkEmail("sarah.chen@example.com")).thenReturn(Optional.of(manager));

                Map<String, Object> mockSummary = Map.of("totalEmployees", 1284);
                when(hrDashboardCacheService.getDashboardSummary()).thenReturn(mockSummary);

                mockMvc.perform(get("/api/v1/hr/dashboard")
                                .header("Authorization", "Bearer mgr-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.totalEmployees").value(1284));
        }

        @Test
        public void testGetDashboardUnauthorized() throws Exception {
                when(jwtService.validateAccessToken("invalid-token")).thenReturn(false);

                mockMvc.perform(get("/api/v1/hr/dashboard")
                                .header("Authorization", "Bearer invalid-token"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        public void testGetDashboardForbidden() throws Exception {
                User employee = new User();
                employee.setWorkEmail("emp@example.com");
                Role role = new Role();
                role.setName("EMPLOYEE");
                employee.setRole(role);

                when(jwtService.validateAccessToken("emp-token")).thenReturn(true);
                when(jwtService.getEmailFromToken("emp-token")).thenReturn("emp@example.com");
                when(userRepository.findByWorkEmail("emp@example.com")).thenReturn(Optional.of(employee));
                when(roleService.hasPermission("emp@example.com", "recruitment.manage")).thenReturn(false);
                when(roleService.hasPermission("emp@example.com", "employee.update")).thenReturn(false);
                when(roleService.hasPermission("emp@example.com", "employee.delete")).thenReturn(false);

                mockMvc.perform(get("/api/v1/hr/dashboard")
                                .header("Authorization", "Bearer emp-token"))
                                .andExpect(status().isForbidden());
        }
}
