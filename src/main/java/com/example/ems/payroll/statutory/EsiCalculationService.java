package com.example.ems.payroll.statutory;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class EsiCalculationService {

    public static final BigDecimal DEFAULT_ESI_WAGE_CEILING = BigDecimal.valueOf(21000);
    public static final BigDecimal LOW_DAILY_WAGE_THRESHOLD = BigDecimal.valueOf(176); // Statutory low wage threshold (₹176/day)
    public static final double EMPLOYEE_ESI_RATE = 0.0075; // 0.75%
    public static final double EMPLOYER_ESI_RATE = 0.0325; // 3.25%

    public EsiCalculationResult calculateEsi(BigDecimal grossWages, int workingDays) {
        return calculateEsi(grossWages, workingDays, DEFAULT_ESI_WAGE_CEILING);
    }

    public EsiCalculationResult calculateEsi(BigDecimal grossWages, int workingDays, BigDecimal customCeiling) {
        if (grossWages == null || grossWages.compareTo(BigDecimal.ZERO) <= 0) {
            return new EsiCalculationResult(false, "EXEMPT_ZERO_WAGES", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false);
        }

        BigDecimal ceiling = customCeiling != null ? customCeiling : DEFAULT_ESI_WAGE_CEILING;

        // ESI coverage check: if gross exceeds the wage ceiling, ESI is not applicable
        if (grossWages.compareTo(ceiling) > 0) {
            return new EsiCalculationResult(false, "WAGES_ABOVE_CEILING", grossWages, BigDecimal.ZERO, BigDecimal.ZERO, false);
        }

        // Daily wage check for low-wage employee contribution exemption
        boolean isLowWage = false;
        if (workingDays > 0) {
            BigDecimal dailyWage = grossWages.divide(BigDecimal.valueOf(workingDays), 2, RoundingMode.HALF_UP);
            if (dailyWage.compareTo(LOW_DAILY_WAGE_THRESHOLD) <= 0) {
                isLowWage = true;
            }
        }

        // 1. Employee Contribution (0.75%, or 0 if low wage exempt)
        BigDecimal employeeContribution = isLowWage
                ? BigDecimal.ZERO
                : grossWages.multiply(BigDecimal.valueOf(EMPLOYEE_ESI_RATE)).setScale(2, RoundingMode.HALF_UP);

        // 2. Employer Contribution (3.25% always applies for covered employees)
        BigDecimal employerContribution = grossWages.multiply(BigDecimal.valueOf(EMPLOYER_ESI_RATE))
                .setScale(2, RoundingMode.HALF_UP);

        String reason = isLowWage ? "COVERED_LOW_WAGE_EMPLOYEE_EXEMPT" : "COVERED_STANDARD";

        return new EsiCalculationResult(
                true,
                reason,
                grossWages,
                employeeContribution,
                employerContribution,
                isLowWage
        );
    }
}
