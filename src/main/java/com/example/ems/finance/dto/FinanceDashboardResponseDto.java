package com.example.ems.finance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Aggregated response payload for the Finance Dashboard Center")
public record FinanceDashboardResponseDto(
        @JsonProperty("attendance")
        @Schema(description = "Employee attendance monthly summary")
        FinanceAttendanceSummaryDto attendance,

        @JsonProperty("leaveBalance")
        @Schema(description = "Employee remaining leave balances")
        FinanceLeaveBalanceDto leaveBalance,

        @JsonProperty("ctc")
        @Schema(description = "Employee Cost to Company compensation summary")
        FinanceCtcDto ctc,

        @JsonProperty("rating")
        @Schema(description = "Employee performance rating summary")
        FinanceRatingDto rating,

        @JsonProperty("pendingActions")
        @Schema(description = "Pending actionable items requiring employee attention")
        List<FinancePendingActionDto> pendingActions,

        @JsonProperty("todaySchedule")
        @Schema(description = "Today's shift and attendance schedule")
        FinanceScheduleDto todaySchedule,

        @JsonProperty("financeTeam")
        @Schema(description = "Finance department team members directory")
        List<FinanceTeamMemberDto> financeTeam
) {
    @JsonProperty("myAttendance")
    public FinanceAttendanceSummaryDto myAttendance() {
        return attendance;
    }

    @JsonProperty("myCtc")
    public FinanceCtcDto myCtc() {
        return ctc;
    }

    @JsonProperty("myRating")
    public FinanceRatingDto myRating() {
        return rating;
    }
}
