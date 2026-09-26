package com.example.ems.finance.service;

import com.example.ems.finance.dto.FinanceExpenseListItem;
import com.example.ems.finance.dto.manager.FinanceManagerDashboardResponseDto;
import com.example.ems.finance.dto.manager.ManagerPendingActionDto;
import com.example.ems.finance.dto.manager.ManagerTeamMemberDto;

import java.util.List;

public interface FinanceManagerDashboardService {

    FinanceManagerDashboardResponseDto getManagerFinanceDashboard();

    List<ManagerTeamMemberDto> getManagerTeam();

    ManagerTeamMemberDto getManagerTeamMember(Long employeeId);

    List<ManagerPendingActionDto> getPendingActions();

    List<FinanceExpenseListItem> getPendingExpenses();

    void approveExpense(Long expenseId, String remarks);

    void rejectExpense(Long expenseId, String reason);
}
