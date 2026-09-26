package com.example.ems.finance.controller;

import com.example.ems.finance.dto.*;
import com.example.ems.finance.service.FinanceDashboardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class FinanceDashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FinanceDashboardService dashboardService;

    @InjectMocks
    private FinanceDashboardController controller;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("emp@company.com", null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetDashboard_Unauthenticated_Returns401() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/v1/finance/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetDashboard_ReturnsAggregatedPayload() throws Exception {
        FinanceAttendanceSummaryDto attendance = new FinanceAttendanceSummaryDto(94.0, 22, 24, 2.1);
        FinanceLeaveBalanceDto leave = new FinanceLeaveBalanceDto(12, Map.of("CL", 6, "EL", 4, "SL", 2));
        FinanceCtcDto ctc = new FinanceCtcDto(new BigDecimal("1200000.00"), "₹12L", "2026-01-01", "INR");
        FinanceRatingDto rating = new FinanceRatingDto(4.3, "2025-12-01");
        List<FinancePendingActionDto> actions = List.of(
                new FinancePendingActionDto("LEAVE", "Apply for Leave", "8 days remaining", "ACTION_REQUIRED", "/leave/apply"),
                new FinancePendingActionDto("INVESTMENT_DECLARATION", "Upload Investment Declaration", "Due Apr 30", "ACTION_REQUIRED", "/finance/investments"),
                new FinancePendingActionDto("SELF_REVIEW", "Complete Self-Review", "19 days left", "ACTION_REQUIRED", "/performance/self-review"),
                new FinancePendingActionDto("EXPENSE", "Submit Pending Expense", "1 draft expense claim unpublished", "ACTION_REQUIRED", "/expenses/drafts")
        );
        FinanceScheduleDto schedule = new FinanceScheduleDto("Morning Shift", "06:00", "14:00", "HQ Office, Chennai", "09:05", true);
        List<FinanceTeamMemberDto> team = List.of(
                new FinanceTeamMemberDto(101L, "RK", "Rajan Kumar", "Finance Lead", "ONLINE"),
                new FinanceTeamMemberDto(102L, "PN", "Priya Nair", "Payroll Analyst", "AWAY"),
                new FinanceTeamMemberDto(103L, "VM", "Vikram Mehta", "Tax Specialist", "OFFLINE")
        );

        FinanceDashboardResponseDto responseDto = new FinanceDashboardResponseDto(
                attendance, leave, ctc, rating, actions, schedule, team
        );

        when(dashboardService.getFinanceDashboard()).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/finance/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendance.percentage").value(94.0))
                .andExpect(jsonPath("$.attendance.presentDays").value(22))
                .andExpect(jsonPath("$.attendance.workingDays").value(24))
                .andExpect(jsonPath("$.attendance.changePercentage").value(2.1))
                .andExpect(jsonPath("$.leaveBalance.totalRemaining").value(12))
                .andExpect(jsonPath("$.leaveBalance.balances.CL").value(6))
                .andExpect(jsonPath("$.leaveBalance.balances.EL").value(4))
                .andExpect(jsonPath("$.leaveBalance.balances.SL").value(2))
                .andExpect(jsonPath("$.ctc.annualCtc").value(1200000.00))
                .andExpect(jsonPath("$.ctc.displayValue").value("₹12L"))
                .andExpect(jsonPath("$.ctc.currency").value("INR"))
                .andExpect(jsonPath("$.rating.rating").value(4.3))
                .andExpect(jsonPath("$.rating.lastReviewDate").value("2025-12-01"))
                .andExpect(jsonPath("$.pendingActions").isArray())
                .andExpect(jsonPath("$.pendingActions[0].type").value("LEAVE"))
                .andExpect(jsonPath("$.pendingActions[1].type").value("INVESTMENT_DECLARATION"))
                .andExpect(jsonPath("$.todaySchedule.shiftName").value("Morning Shift"))
                .andExpect(jsonPath("$.todaySchedule.checkInVerified").value(true))
                .andExpect(jsonPath("$.financeTeam").isArray())
                .andExpect(jsonPath("$.financeTeam[0].name").value("Rajan Kumar"))
                .andExpect(jsonPath("$.financeTeam[0].initials").value("RK"))
                // Also verify frontend alias compatibility
                .andExpect(jsonPath("$.myAttendance.percentage").value(94.0))
                .andExpect(jsonPath("$.myCtc.displayValue").value("₹12L"))
                .andExpect(jsonPath("$.myRating.rating").value(4.3));

        verify(dashboardService, times(1)).getFinanceDashboard();
    }
}
