package com.example.ems.payroll.statutory;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class TdsCalculationService {

    public static final BigDecimal NEW_REGIME_STANDARD_DEDUCTION = BigDecimal.valueOf(75000);
    public static final BigDecimal OLD_REGIME_STANDARD_DEDUCTION = BigDecimal.valueOf(50000);
    public static final double HEALTH_AND_EDUCATION_CESS_RATE = 0.04; // 4%

    public TdsCalculationResult calculateTds(TdsCalculationContext context) {
        if (context == null) {
            return new TdsCalculationResult();
        }

        LocalDate payrollDate = context.getPayrollDate() != null ? context.getPayrollDate() : LocalDate.now();
        String financialYear = resolveFinancialYear(payrollDate);
        int remainingPeriods = calculateRemainingPeriodsInFy(payrollDate);

        String regime = context.getTaxRegime() != null ? context.getTaxRegime().toUpperCase().trim() : "NEW";

        // 1. Projected Annual Gross Income
        BigDecimal currentGross = context.getCurrentMonthGross() != null ? context.getCurrentMonthGross() : BigDecimal.ZERO;
        BigDecimal ytdIncome = context.getYtdTaxableIncome() != null ? context.getYtdTaxableIncome() : BigDecimal.ZERO;
        BigDecimal otherIncome = context.getDeclaredOtherIncome() != null ? context.getDeclaredOtherIncome() : BigDecimal.ZERO;

        // Projected remaining gross for current month + future months in FY
        BigDecimal projectedFutureGross = currentGross.multiply(BigDecimal.valueOf(remainingPeriods));
        BigDecimal projectedAnnualGross = ytdIncome.add(projectedFutureGross).add(otherIncome);

        // 2. Deductions (Standard deduction + Chapter VI-A for OLD regime)
        BigDecimal standardDeduction = "OLD".equals(regime) ? OLD_REGIME_STANDARD_DEDUCTION : NEW_REGIME_STANDARD_DEDUCTION;
        BigDecimal otherDeductions = "OLD".equals(regime) && context.getChapterViaDeductions() != null
                ? context.getChapterViaDeductions() : BigDecimal.ZERO;
        BigDecimal totalDeductions = standardDeduction.add(otherDeductions);

        // 3. Taxable Income
        BigDecimal taxableIncome = projectedAnnualGross.subtract(totalDeductions);
        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            taxableIncome = BigDecimal.ZERO;
        }

        // 4. Calculate Slab Tax & Section 87A Rebate
        BigDecimal slabTax;
        BigDecimal rebate = BigDecimal.ZERO;

        if ("OLD".equals(regime)) {
            slabTax = calculateOldRegimeTax(taxableIncome);
            if (taxableIncome.compareTo(BigDecimal.valueOf(500000)) <= 0) {
                rebate = slabTax.min(BigDecimal.valueOf(12500));
            }
        } else {
            slabTax = calculateNewRegimeTax(taxableIncome);
            // New regime Section 87A rebate for taxable income up to 12 Lakhs (rebate up to ₹60,000)
            if (taxableIncome.compareTo(BigDecimal.valueOf(1200000)) <= 0) {
                rebate = slabTax.min(BigDecimal.valueOf(60000));
            }
        }

        BigDecimal taxAfterRebate = slabTax.subtract(rebate);
        if (taxAfterRebate.compareTo(BigDecimal.ZERO) < 0) {
            taxAfterRebate = BigDecimal.ZERO;
        }

        // 5. Health & Education Cess (4%)
        BigDecimal cess = taxAfterRebate.multiply(BigDecimal.valueOf(HEALTH_AND_EDUCATION_CESS_RATE))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalProjectedAnnualTax = taxAfterRebate.add(cess);

        // 6. Remaining Tax Liability & Monthly TDS
        BigDecimal ytdTds = context.getYtdTdsDeducted() != null ? context.getYtdTdsDeducted() : BigDecimal.ZERO;
        BigDecimal remainingTaxLiability = totalProjectedAnnualTax.subtract(ytdTds);
        if (remainingTaxLiability.compareTo(BigDecimal.ZERO) < 0) {
            remainingTaxLiability = BigDecimal.ZERO;
        }

        int divisor = Math.max(1, remainingPeriods);
        BigDecimal monthlyTds = remainingTaxLiability.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);

        TdsCalculationResult result = new TdsCalculationResult();
        result.setApplicable(monthlyTds.compareTo(BigDecimal.ZERO) > 0);
        result.setFinancialYear(financialYear);
        result.setTaxRegime(regime);
        result.setMonthlyTds(monthlyTds);
        result.setProjectedAnnualGross(projectedAnnualGross);
        result.setTaxableIncome(taxableIncome);
        result.setStandardDeduction(standardDeduction);
        result.setSlabTax(slabTax);
        result.setSection87aRebate(rebate);
        result.setHealthAndEducationCess(cess);
        result.setTotalProjectedAnnualTax(totalProjectedAnnualTax);
        result.setYtdTdsDeducted(ytdTds);
        result.setRemainingPeriods(remainingPeriods);

        return result;
    }

    public static String resolveFinancialYear(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        if (month >= 4) {
            return year + "-" + String.format("%02d", (year + 1) % 100);
        } else {
            return (year - 1) + "-" + String.format("%02d", year % 100);
        }
    }

    public static int calculateRemainingPeriodsInFy(LocalDate date) {
        int month = date.getMonthValue();
        // Indian FY: April (1) -> March (12)
        int monthInFy = (month >= 4) ? (month - 3) : (month + 9);
        return Math.max(1, 12 - monthInFy + 1);
    }

    private BigDecimal calculateNewRegimeTax(BigDecimal taxableIncome) {
        double income = taxableIncome.doubleValue();
        if (income <= 400000) {
            return BigDecimal.ZERO;
        }

        double tax = 0.0;
        // ₹4,00,001 to ₹8,00,000 @ 5%
        if (income > 400000) {
            double taxableAt5 = Math.min(income - 400000, 400000);
            tax += taxableAt5 * 0.05;
        }
        // ₹8,00,001 to ₹12,00,000 @ 10%
        if (income > 800000) {
            double taxableAt10 = Math.min(income - 800000, 400000);
            tax += taxableAt10 * 0.10;
        }
        // ₹12,00,001 to ₹16,00,000 @ 15%
        if (income > 1200000) {
            double taxableAt15 = Math.min(income - 1200000, 400000);
            tax += taxableAt15 * 0.15;
        }
        // ₹16,00,001 to ₹20,00,000 @ 20%
        if (income > 1600000) {
            double taxableAt20 = Math.min(income - 1600000, 400000);
            tax += taxableAt20 * 0.20;
        }
        // ₹20,00,001 to ₹24,00,000 @ 25%
        if (income > 2000000) {
            double taxableAt25 = Math.min(income - 2000000, 400000);
            tax += taxableAt25 * 0.25;
        }
        // Above ₹24,00,000 @ 30%
        if (income > 2400000) {
            double taxableAt30 = income - 2400000;
            tax += taxableAt30 * 0.30;
        }

        return BigDecimal.valueOf(tax).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateOldRegimeTax(BigDecimal taxableIncome) {
        double income = taxableIncome.doubleValue();
        if (income <= 250000) {
            return BigDecimal.ZERO;
        }

        double tax = 0.0;
        // ₹2,50,001 to ₹5,00,000 @ 5%
        if (income > 250000) {
            double taxableAt5 = Math.min(income - 250000, 250000);
            tax += taxableAt5 * 0.05;
        }
        // ₹5,00,001 to ₹10,00,000 @ 20%
        if (income > 500000) {
            double taxableAt20 = Math.min(income - 500000, 500000);
            tax += taxableAt20 * 0.20;
        }
        // Above ₹10,00,000 @ 30%
        if (income > 1000000) {
            double taxableAt30 = income - 1000000;
            tax += taxableAt30 * 0.30;
        }

        return BigDecimal.valueOf(tax).setScale(2, RoundingMode.HALF_UP);
    }
}
