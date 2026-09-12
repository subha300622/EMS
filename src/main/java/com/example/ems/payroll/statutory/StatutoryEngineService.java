package com.example.ems.payroll.statutory;

import com.example.ems.payroll.dto.SalaryCalculatedComponentResponse;
import com.example.ems.payroll.dto.SalaryCalculationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class StatutoryEngineService {

    private final PfCalculationService pfCalculationService;
    private final EsiCalculationService esiCalculationService;
    private final ProfessionalTaxService professionalTaxService;
    private final TdsCalculationService tdsCalculationService;

    public StatutoryEngineService(PfCalculationService pfCalculationService,
                                  EsiCalculationService esiCalculationService,
                                  ProfessionalTaxService professionalTaxService,
                                  TdsCalculationService tdsCalculationService) {
        this.pfCalculationService = pfCalculationService;
        this.esiCalculationService = esiCalculationService;
        this.professionalTaxService = professionalTaxService;
        this.tdsCalculationService = tdsCalculationService;
    }

    public StatutoryCalculationResult calculateStatutory(Long employeeId, Long organizationId,
                                                         BigDecimal adjustedGross, SalaryCalculationResponse salaryCalc,
                                                         int workingDays, LocalDate payrollDate,
                                                         String workState, String taxRegime) {
        // 1. Basic Salary resolution for PF
        BigDecimal basicSalary = resolveBasicSalary(salaryCalc, adjustedGross);

        // 2. PF Calculation
        PfCalculationResult pfResult = pfCalculationService.calculatePf(basicSalary);

        // 3. ESI Calculation (Gross vs ₹21,000 threshold)
        EsiCalculationResult esiResult = esiCalculationService.calculateEsi(adjustedGross, workingDays);

        // 4. Professional Tax Calculation (State-driven)
        PtCalculationResult ptResult = professionalTaxService.calculatePt(adjustedGross, workState, payrollDate);

        // 5. TDS Calculation (Annual projection with FY & Regimes)
        TdsCalculationContext tdsContext = new TdsCalculationContext(
                employeeId, organizationId, taxRegime != null ? taxRegime : "NEW",
                payrollDate, adjustedGross, BigDecimal.ZERO, BigDecimal.ZERO
        );
        TdsCalculationResult tdsResult = tdsCalculationService.calculateTds(tdsContext);

        return new StatutoryCalculationResult(pfResult, esiResult, ptResult, tdsResult);
    }

    private BigDecimal resolveBasicSalary(SalaryCalculationResponse salaryCalc, BigDecimal fallbackGross) {
        if (salaryCalc != null && salaryCalc.getComponents() != null) {
            for (SalaryCalculatedComponentResponse comp : salaryCalc.getComponents()) {
                if ("BASIC".equalsIgnoreCase(comp.getComponentCode()) ||
                    "BASIC_SALARY".equalsIgnoreCase(comp.getComponentCode())) {
                    return comp.getAmount() != null ? comp.getAmount() : BigDecimal.ZERO;
                }
            }
        }
        if (fallbackGross != null && fallbackGross.compareTo(BigDecimal.ZERO) > 0) {
            return fallbackGross.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(50000);
    }
}
