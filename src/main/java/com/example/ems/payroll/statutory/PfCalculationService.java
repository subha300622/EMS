package com.example.ems.payroll.statutory;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PfCalculationService {

    public static final BigDecimal DEFAULT_PF_CEILING = BigDecimal.valueOf(15000);
    public static final BigDecimal DEFAULT_EPS_CEILING = BigDecimal.valueOf(1250);
    public static final double EMPLOYEE_PF_RATE = 0.12; // 12%
    public static final double EMPLOYER_EPS_RATE = 0.0833; // 8.33%

    public PfCalculationResult calculatePf(BigDecimal basicSalary) {
        return calculatePf(basicSalary, true, DEFAULT_PF_CEILING);
    }

    public PfCalculationResult calculatePf(BigDecimal basicSalary, boolean isCapped, BigDecimal customCeiling) {
        if (basicSalary == null || basicSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return new PfCalculationResult(false, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "EXEMPT_ZERO_SALARY");
        }

        BigDecimal ceiling = customCeiling != null ? customCeiling : DEFAULT_PF_CEILING;
        BigDecimal wageBase = isCapped ? basicSalary.min(ceiling) : basicSalary;

        // 1. Employee Contribution = 12% of PF Wage
        BigDecimal employeeContribution = wageBase.multiply(BigDecimal.valueOf(EMPLOYEE_PF_RATE))
                .setScale(2, RoundingMode.HALF_UP);

        // 2. Employer EPS Contribution = Min(8.33% of PF Wage, EPS Ceiling)
        BigDecimal calculatedEps = wageBase.multiply(BigDecimal.valueOf(EMPLOYER_EPS_RATE))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal employerEps = isCapped ? calculatedEps.min(DEFAULT_EPS_CEILING) : calculatedEps;

        // 3. Employer EPF Contribution = Total 12% Employer Share - EPS Share
        BigDecimal totalEmployerContribution = wageBase.multiply(BigDecimal.valueOf(EMPLOYEE_PF_RATE))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal employerEpf = totalEmployerContribution.subtract(employerEps);
        if (employerEpf.compareTo(BigDecimal.ZERO) < 0) {
            employerEpf = BigDecimal.ZERO;
        }

        String calcType = isCapped ? "STATUTORY_CAPPED_" + ceiling.intValue() : "UNCAPPED_ACTUAL";

        return new PfCalculationResult(
                true,
                wageBase,
                employeeContribution,
                employerEpf,
                employerEps,
                totalEmployerContribution,
                calcType
        );
    }
}
