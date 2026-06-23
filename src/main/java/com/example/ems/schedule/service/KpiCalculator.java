package com.example.ems.schedule.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.schedule.dto.TeamScheduleOverviewDto;
import com.example.ems.schedule.entity.MyShift;
import com.example.ems.schedule.entity.MyScheduleChangeRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class KpiCalculator {

    public TeamScheduleOverviewDto calculateKpis(List<Employee> employees, List<MyShift> shifts, List<MyScheduleChangeRequest> changeRequests, LocalDate startDate, LocalDate endDate, double totalOvertimeHours) {
        int totalEmployees = employees.size();
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        int totalShifts = 0;
        for (MyShift shift : shifts) {
            if (shift.getTemplate() != null && !shift.getTemplate().getName().equalsIgnoreCase("NONE")) {
                totalShifts++;
            }
        }

        double coveragePercentage = 0.0;
        if (totalEmployees > 0 && totalDays > 0) {
            coveragePercentage = ((double) totalShifts / (totalEmployees * totalDays)) * 100.0;
            // Round to 1 decimal place
            coveragePercentage = Math.round(coveragePercentage * 10.0) / 10.0;
        }

        int pendingSwaps = 0;
        int totalSwaps = 0;
        for (MyScheduleChangeRequest req : changeRequests) {
            totalSwaps++;
            if ("PENDING_MANAGER_APPROVAL".equalsIgnoreCase(req.getStatus())) {
                pendingSwaps++;
            }
        }

        // System alerts: count employees with any warning (e.g. overtime hours > 8 or similar)
        int systemAlertsCount = 0;
        if (totalOvertimeHours > 0) {
            systemAlertsCount = (int) Math.ceil(totalOvertimeHours / 8.0);
        }
        if (systemAlertsCount == 0 && pendingSwaps > 0) {
            systemAlertsCount = 1; // default to 1 alert if pending swaps exist but no overtime
        }

        double coverageTargetPercentage = 90.0;

        return new TeamScheduleOverviewDto(
                coveragePercentage,
                systemAlertsCount,
                pendingSwaps,
                totalShifts,
                coverageTargetPercentage,
                totalOvertimeHours,
                totalSwaps
        );
    }
}
