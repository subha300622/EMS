package com.example.ems.schedule.dto;

import java.util.List;

public class TeamScheduleResponse {
    private TeamScheduleOverviewDto overview;
    private List<EmployeeScheduleGridDto> grid;
    private List<ShiftSwapRequestDto> swaps;
    private OvertimeSummaryDto overtime;

    public TeamScheduleResponse() {}

    public TeamScheduleResponse(TeamScheduleOverviewDto overview, List<EmployeeScheduleGridDto> grid,
                                List<ShiftSwapRequestDto> swaps, OvertimeSummaryDto overtime) {
        this.overview = overview;
        this.grid = grid;
        this.swaps = swaps;
        this.overtime = overtime;
    }

    public TeamScheduleOverviewDto getOverview() {
        return overview;
    }

    public void setOverview(TeamScheduleOverviewDto overview) {
        this.overview = overview;
    }

    public List<EmployeeScheduleGridDto> getGrid() {
        return grid;
    }

    public void setGrid(List<EmployeeScheduleGridDto> grid) {
        this.grid = grid;
    }

    public List<ShiftSwapRequestDto> getSwaps() {
        return swaps;
    }

    public void setSwaps(List<ShiftSwapRequestDto> swaps) {
        this.swaps = swaps;
    }

    public OvertimeSummaryDto getOvertime() {
        return overtime;
    }

    public void setOvertime(OvertimeSummaryDto overtime) {
        this.overtime = overtime;
    }
}
