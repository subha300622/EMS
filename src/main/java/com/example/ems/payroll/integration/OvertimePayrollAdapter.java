package com.example.ems.payroll.integration;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.payroll.dto.OvertimePeriodSummaryDto;
import com.example.ems.payroll.dto.SalaryCalculatedComponentResponse;
import com.example.ems.payroll.dto.SalaryCalculationResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Component
public class OvertimePayrollAdapter {

    private final AttendanceRepository attendanceRepository;

    public OvertimePayrollAdapter(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public OvertimePeriodSummaryDto getOvertimeSummary(Long employeeId, Long organizationId,
                                                        LocalDate periodStart, LocalDate periodEnd,
                                                        int workingDays, SalaryCalculationResponse salaryCalc) {
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(
                employeeId, periodStart, periodEnd, organizationId
        );

        double totalOtHours = 0.0;
        if (attendances != null) {
            for (Attendance att : attendances) {
                String otStr = att.getOvertime();
                if (otStr != null && !otStr.trim().isEmpty() && !"00:00".equals(otStr)) {
                    totalOtHours += parseOtHours(otStr);
                }
            }
        }

        // Round OT hours to 2 decimal places
        totalOtHours = Math.round(totalOtHours * 100.0) / 100.0;

        if (totalOtHours <= 0.0) {
            return new OvertimePeriodSummaryDto(0.0, BigDecimal.ZERO, 1.5, BigDecimal.ZERO);
        }

        // Determine Basic Salary for hourly rate calculation
        BigDecimal basicSalary = resolveBasicSalary(salaryCalc);
        int standardMonthlyHours = workingDays > 0 ? workingDays * 8 : 208;

        BigDecimal hourlyRate = basicSalary.divide(BigDecimal.valueOf(standardMonthlyHours), 2, RoundingMode.HALF_UP);
        double multiplier = 1.5;

        BigDecimal otAmount = hourlyRate
                .multiply(BigDecimal.valueOf(totalOtHours))
                .multiply(BigDecimal.valueOf(multiplier))
                .setScale(2, RoundingMode.HALF_UP);

        return new OvertimePeriodSummaryDto(totalOtHours, hourlyRate, multiplier, otAmount);
    }

    private double parseOtHours(String otStr) {
        try {
            String[] parts = otStr.split(":");
            int hours = Integer.parseInt(parts[0]);
            int mins = Integer.parseInt(parts[1]);
            return hours + (mins / 60.0);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private BigDecimal resolveBasicSalary(SalaryCalculationResponse salaryCalc) {
        if (salaryCalc != null && salaryCalc.getComponents() != null) {
            for (SalaryCalculatedComponentResponse comp : salaryCalc.getComponents()) {
                if ("BASIC".equalsIgnoreCase(comp.getComponentCode()) ||
                    "BASIC_SALARY".equalsIgnoreCase(comp.getComponentCode())) {
                    return comp.getAmount() != null ? comp.getAmount() : BigDecimal.ZERO;
                }
            }
            if (salaryCalc.getGrossPay() != null && salaryCalc.getGrossPay().compareTo(BigDecimal.ZERO) > 0) {
                return salaryCalc.getGrossPay().divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.valueOf(50000); // Default sensible base
    }
}
