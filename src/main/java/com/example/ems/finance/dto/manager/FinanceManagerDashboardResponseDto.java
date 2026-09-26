package com.example.ems.finance.dto.manager;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;

@Schema(description = "Manager Finance Center Dashboard aggregated response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinanceManagerDashboardResponseDto implements Serializable {

    @Schema(description = "Team summary metrics")
    private ManagerTeamSummaryDto teamSummary;

    @Schema(description = "Leave summary metrics")
    private ManagerLeaveSummaryDto leaveSummary;

    @Schema(description = "Expense summary metrics")
    private ManagerExpenseSummaryDto expenseSummary;

    @Schema(description = "Payroll summary metrics (requires finance.payroll.view or finance.salary.view permission)")
    private ManagerPayrollSummaryDto payrollSummary;

    @Schema(description = "Pending actionable items for the manager")
    private List<ManagerPendingActionDto> pendingActions;

    @Schema(description = "Reporting team members workforce & finance summary")
    private List<ManagerTeamMemberDto> teamMembers;

    public FinanceManagerDashboardResponseDto() {}

    public FinanceManagerDashboardResponseDto(
            ManagerTeamSummaryDto teamSummary,
            ManagerLeaveSummaryDto leaveSummary,
            ManagerExpenseSummaryDto expenseSummary,
            ManagerPayrollSummaryDto payrollSummary,
            List<ManagerPendingActionDto> pendingActions,
            List<ManagerTeamMemberDto> teamMembers) {
        this.teamSummary = teamSummary;
        this.leaveSummary = leaveSummary;
        this.expenseSummary = expenseSummary;
        this.payrollSummary = payrollSummary;
        this.pendingActions = pendingActions;
        this.teamMembers = teamMembers;
    }

    public ManagerTeamSummaryDto getTeamSummary() {
        return teamSummary;
    }

    public void setTeamSummary(ManagerTeamSummaryDto teamSummary) {
        this.teamSummary = teamSummary;
    }

    public ManagerLeaveSummaryDto getLeaveSummary() {
        return leaveSummary;
    }

    public void setLeaveSummary(ManagerLeaveSummaryDto leaveSummary) {
        this.leaveSummary = leaveSummary;
    }

    public ManagerExpenseSummaryDto getExpenseSummary() {
        return expenseSummary;
    }

    public void setExpenseSummary(ManagerExpenseSummaryDto expenseSummary) {
        this.expenseSummary = expenseSummary;
    }

    public ManagerPayrollSummaryDto getPayrollSummary() {
        return payrollSummary;
    }

    public void setPayrollSummary(ManagerPayrollSummaryDto payrollSummary) {
        this.payrollSummary = payrollSummary;
    }

    public List<ManagerPendingActionDto> getPendingActions() {
        return pendingActions;
    }

    public void setPendingActions(List<ManagerPendingActionDto> pendingActions) {
        this.pendingActions = pendingActions;
    }

    public List<ManagerTeamMemberDto> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<ManagerTeamMemberDto> teamMembers) {
        this.teamMembers = teamMembers;
    }
}
