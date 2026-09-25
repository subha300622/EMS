package com.example.ems.dashboard;

import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.controller.EmployeeDashboardController;
import com.example.ems.employee.dto.dashboard.*;
import com.example.ems.employee.service.EmployeeDashboardService;
import com.example.ems.security.service.PermissionCheckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmployeeDashboardControllerTest {

        private MockMvc mockMvc;

        @Mock
        private EmployeeDashboardService dashboardService;

        @Mock
        private PermissionCheckService permissionCheckService;

        @InjectMocks
        private EmployeeDashboardController controller;

        @BeforeEach
        void setUp() {
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken("emp@company.com", "password",
                                                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("employee.dashboard.view"))));
                mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
        }

        @Test
        void testGetDashboard_Returns200AndExpectedStructure() throws Exception {
                EmployeeAttendanceSummaryDto attendance = new EmployeeAttendanceSummaryDto(
                                92.0, 22, 24, 2.1, "UP");
                EmployeeLeaveBalanceSummaryDto leaveBalance = new EmployeeLeaveBalanceSummaryDto(
                                12.0, Map.of("CL", 6.0, "EL", 4.0, "SL", 2.0));
                EmployeeCompensationSummaryDto compensation = new EmployeeCompensationSummaryDto(
                                BigDecimal.valueOf(1800000), "INR", "ANNUAL", "2025-04-01");
                EmployeePerformanceSummaryDto performance = new EmployeePerformanceSummaryDto(
                                4.5, 5.0, "2025-12-01", "Excellent");

                EmployeeDashboardResponse.SummaryDto summary = new EmployeeDashboardResponse.SummaryDto(
                                attendance, leaveBalance, compensation, performance);
                EmployeeActionCenterResponseDto actions = new EmployeeActionCenterResponseDto(0,
                                Collections.emptyList());

                when(dashboardService.getDashboard()).thenReturn(new EmployeeDashboardResponse(summary, actions));

                mockMvc.perform(get("/api/v1/employee/dashboard")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.summary.attendance.percentage").value(92.0))
                                .andExpect(jsonPath("$.summary.attendance.presentDays").value(22))
                                .andExpect(jsonPath("$.summary.leaveBalance.totalAvailable").value(12.0))
                                .andExpect(jsonPath("$.summary.compensation.currentCtc").value(1800000))
                                .andExpect(jsonPath("$.summary.compensation.currency").value("INR"))
                                .andExpect(jsonPath("$.summary.performance.rating").value(4.5));
        }

        @Test
        void testGetAttendanceSummary_Returns200() throws Exception {
                EmployeeAttendanceDetailSummaryDto detail = new EmployeeAttendanceDetailSummaryDto(
                                new EmployeeAttendancePeriodDto("2026-09-01", "2026-09-24"),
                                24, 22, 92.0, 2.1, "UP");
                when(dashboardService.getAttendanceSummary()).thenReturn(detail);

                mockMvc.perform(get("/api/v1/employee/attendance/summary")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.workingDays").value(24))
                                .andExpect(jsonPath("$.presentDays").value(22))
                                .andExpect(jsonPath("$.attendancePercentage").value(92.0));
        }

        @Test
        void testGetLeaveBalance_Returns200() throws Exception {
                EmployeeLeaveBalanceDetailDto balance = new EmployeeLeaveBalanceDetailDto(
                                12.0, List.of(
                                                new EmployeeLeaveTypeItemDto("CL", "Casual Leave", 6.0),
                                                new EmployeeLeaveTypeItemDto("EL", "Earned Leave", 4.0),
                                                new EmployeeLeaveTypeItemDto("SL", "Sick Leave", 2.0)));
                when(dashboardService.getLeaveBalance()).thenReturn(balance);

                mockMvc.perform(get("/api/v1/employee/leave-balance")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalAvailable").value(12.0))
                                .andExpect(jsonPath("$.leaveTypes[0].code").value("CL"))
                                .andExpect(jsonPath("$.leaveTypes[0].available").value(6.0));
        }

        @Test
        void testGetCurrentCompensation_Returns200() throws Exception {
                EmployeeCompensationSummaryDto compensation = new EmployeeCompensationSummaryDto(
                                BigDecimal.valueOf(1800000), "INR", "ANNUAL", "2025-04-01");
                when(dashboardService.getCurrentCompensation()).thenReturn(compensation);

                mockMvc.perform(get("/api/v1/employee/compensation/current")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.currentCtc").value(1800000))
                                .andExpect(jsonPath("$.currency").value("INR"));
        }

        @Test
        void testGetPerformanceSummary_Returns200() throws Exception {
                EmployeePerformanceSummaryDto performance = new EmployeePerformanceSummaryDto(
                                4.5, 5.0, "2025-12-01", "Excellent");
                when(dashboardService.getPerformanceSummary()).thenReturn(performance);

                mockMvc.perform(get("/api/v1/employee/performance/summary")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.rating").value(4.5))
                                .andExpect(jsonPath("$.ratingLabel").value("Excellent"));
        }

        @Test
        void testGetActionCenter_Returns200() throws Exception {
                EmployeeActionCenterResponseDto actions = new EmployeeActionCenterResponseDto(
                                2,
                                List.of(
                                                new EmployeeActionDto("ACT-1", "LEAVE", "Leave Pending", "PENDING",
                                                                "NORMAL", "2026-10-01", "Desc", null),
                                                new EmployeeActionDto("ACT-2", "DOC", "Passport Missing", "OVERDUE",
                                                                "HIGH", "2026-09-20", "Desc", null)));
                when(dashboardService.getActionCenter()).thenReturn(actions);

                mockMvc.perform(get("/api/v1/employee/action-center")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.total").value(2))
                                .andExpect(jsonPath("$.items[0].id").value("ACT-1"))
                                .andExpect(jsonPath("$.items[1].id").value("ACT-2"));
        }
}
