package com.example.ems.finance.controller;

import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.finance.dto.FinanceExpenseListItem;
import com.example.ems.finance.dto.manager.*;
import com.example.ems.finance.service.FinanceManagerDashboardService;
import com.example.ems.security.service.PermissionCheckService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class FinanceManagerDashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FinanceManagerDashboardService managerDashboardService;

    @Mock
    private PermissionCheckService permissionCheckService;

    @InjectMocks
    private FinanceManagerDashboardController controller;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("manager@company.com", null,
                        List.of(new SimpleGrantedAuthority("ROLE_MANAGER"), new SimpleGrantedAuthority("finance.team.view")));
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
        mockMvc.perform(get("/api/v1/finance/manager/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetDashboard_ReturnsAggregatedPayload() throws Exception {
        ManagerTeamSummaryDto teamSummary = new ManagerTeamSummaryDto(24, 22, 2, 91.67);
        ManagerLeaveSummaryDto leaveSummary = new ManagerLeaveSummaryDto(4L, 12L, 38.0);
        ManagerExpenseSummaryDto expenseSummary = new ManagerExpenseSummaryDto(6L, new BigDecimal("48500"), 18L, new BigDecimal("126000"));
        ManagerPayrollSummaryDto payrollSummary = new ManagerPayrollSummaryDto(24, new BigDecimal("1850000"), "PROCESSED");
        List<ManagerPendingActionDto> actions = List.of(
                new ManagerPendingActionDto("LEAVE_APPROVAL", 4L, "Leave Requests Awaiting Approval"),
                new ManagerPendingActionDto("EXPENSE_APPROVAL", 6L, "Expense Claims Awaiting Approval")
        );
        List<ManagerTeamMemberDto> teamMembers = List.of(
                new ManagerTeamMemberDto(101L, "Rajan Kumar", "Senior Accountant", 96.0, 8.0, 1L)
        );

        FinanceManagerDashboardResponseDto responseDto = new FinanceManagerDashboardResponseDto(
                teamSummary, leaveSummary, expenseSummary, payrollSummary, actions, teamMembers
        );

        when(managerDashboardService.getManagerFinanceDashboard()).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/finance/manager/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamSummary.totalEmployees").value(24))
                .andExpect(jsonPath("$.teamSummary.presentToday").value(22))
                .andExpect(jsonPath("$.teamSummary.onLeaveToday").value(2))
                .andExpect(jsonPath("$.teamSummary.attendancePercentage").value(91.67))
                .andExpect(jsonPath("$.leaveSummary.pendingRequests").value(4))
                .andExpect(jsonPath("$.leaveSummary.approvedThisMonth").value(12))
                .andExpect(jsonPath("$.leaveSummary.teamLeaveDays").value(38.0))
                .andExpect(jsonPath("$.expenseSummary.pendingClaims").value(6))
                .andExpect(jsonPath("$.expenseSummary.pendingAmount").value(48500))
                .andExpect(jsonPath("$.expenseSummary.approvedThisMonth").value(18))
                .andExpect(jsonPath("$.expenseSummary.approvedAmount").value(126000))
                .andExpect(jsonPath("$.payrollSummary.employees").value(24))
                .andExpect(jsonPath("$.payrollSummary.totalMonthlyGross").value(1850000))
                .andExpect(jsonPath("$.payrollSummary.lastPayrollStatus").value("PROCESSED"))
                .andExpect(jsonPath("$.pendingActions[0].type").value("LEAVE_APPROVAL"))
                .andExpect(jsonPath("$.pendingActions[0].count").value(4))
                .andExpect(jsonPath("$.teamMembers[0].employeeId").value(101))
                .andExpect(jsonPath("$.teamMembers[0].name").value("Rajan Kumar"))
                .andExpect(jsonPath("$.teamMembers[0].designation").value("Senior Accountant"))
                .andExpect(jsonPath("$.teamMembers[0].attendancePercentage").value(96.0))
                .andExpect(jsonPath("$.teamMembers[0].leaveBalance").value(8.0))
                .andExpect(jsonPath("$.teamMembers[0].pendingExpenses").value(1));

        verify(permissionCheckService).requirePermission(PermissionRegistry.FINANCE_TEAM_VIEW);
    }

    @Test
    void testGetTeam_ReturnsTeamList() throws Exception {
        List<ManagerTeamMemberDto> teamMembers = List.of(
                new ManagerTeamMemberDto(101L, "Rajan Kumar", "Senior Accountant", 96.0, 8.0, 1L)
        );
        when(managerDashboardService.getManagerTeam()).thenReturn(teamMembers);

        mockMvc.perform(get("/api/v1/finance/manager/team")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeId").value(101))
                .andExpect(jsonPath("$[0].name").value("Rajan Kumar"));
    }

    @Test
    void testGetTeamMember_Authorized_Returns200() throws Exception {
        ManagerTeamMemberDto member = new ManagerTeamMemberDto(101L, "Rajan Kumar", "Senior Accountant", 96.0, 8.0, 1L);
        when(managerDashboardService.getManagerTeamMember(101L)).thenReturn(member);

        mockMvc.perform(get("/api/v1/finance/manager/team/101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(101))
                .andExpect(jsonPath("$.name").value("Rajan Kumar"));
    }

    @Test
    void testGetTeamMember_UnauthorizedOutsideHierarchy_Returns403() throws Exception {
        when(managerDashboardService.getManagerTeamMember(500L))
                .thenThrow(new AccessDeniedException("Access Denied: Target employee #500 is not within your reporting hierarchy"));

        mockMvc.perform(get("/api/v1/finance/manager/team/500")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied: Target employee #500 is not within your reporting hierarchy"));
    }

    @Test
    void testGetPendingActions_ReturnsActions() throws Exception {
        List<ManagerPendingActionDto> actions = List.of(
                new ManagerPendingActionDto("LEAVE_APPROVAL", 4L, "Leave Requests Awaiting Approval")
        );
        when(managerDashboardService.getPendingActions()).thenReturn(actions);

        mockMvc.perform(get("/api/v1/finance/manager/pending-actions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("LEAVE_APPROVAL"))
                .andExpect(jsonPath("$[0].count").value(4));
    }

    @Test
    void testGetPendingExpenses_ReturnsExpenses() throws Exception {
        FinanceExpenseListItem item = new FinanceExpenseListItem(
                10L, 101L, "Rajan Kumar", "Finance", "TRAVEL", "Client visit",
                new BigDecimal("4500"), true, LocalDate.now(), "PENDING"
        );
        when(managerDashboardService.getPendingExpenses()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/finance/manager/expenses/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].expenseId").value(10))
                .andExpect(jsonPath("$[0].employeeName").value("Rajan Kumar"));
    }

    @Test
    void testApproveExpense_Success() throws Exception {
        doNothing().when(managerDashboardService).approveExpense(eq(10L), anyString());

        mockMvc.perform(post("/api/v1/finance/manager/expenses/10/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remarks\":\"Approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Expense claim approved successfully"));

        verify(managerDashboardService).approveExpense(10L, "Approved");
    }

    @Test
    void testRejectExpense_Success() throws Exception {
        doNothing().when(managerDashboardService).rejectExpense(eq(10L), anyString());

        mockMvc.perform(post("/api/v1/finance/manager/expenses/10/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Missing invoice\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Expense claim rejected successfully"));

        verify(managerDashboardService).rejectExpense(10L, "Missing invoice");
    }
}
