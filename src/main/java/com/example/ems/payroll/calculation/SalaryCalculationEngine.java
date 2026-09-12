package com.example.ems.payroll.calculation;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.payroll.dto.SalaryCalculatedComponentResponse;
import com.example.ems.payroll.dto.SalaryCalculationResponse;
import com.example.ems.payroll.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Component
public class SalaryCalculationEngine {

    @Autowired
    private SalaryFormulaEvaluator salaryFormulaEvaluator;

    /**
     * Executes topological DAG salary calculation across all components for an employee assignment.
     *
     * @param assignment The employee's active salary assignment
     * @param structureComponents Components attached to the structure sorted by calculation_order
     * @param effectiveOverrides Merged overrides (structure defaults -> persisted employee overrides -> preview overrides)
     * @param effectiveDate Target date for the calculation
     * @return Full itemized calculation response
     */
    public SalaryCalculationResponse calculate(
            EmployeeSalaryAssignment assignment,
            List<SalaryStructureComponent> structureComponents,
            Map<Long, EmployeeSalaryComponentValue> effectiveOverrides,
            LocalDate effectiveDate) {

        if (assignment == null) {
            throw new BadRequestException("Salary assignment cannot be null.");
        }
        if (structureComponents == null || structureComponents.isEmpty()) {
            throw new BadRequestException("Salary structure contains no components to calculate.");
        }

        SalaryCalculationResponse response = new SalaryCalculationResponse();
        response.setEmployeeId(assignment.getEmployee().getId());
        response.setEmployeeName(assignment.getEmployee().getFullName());
        response.setEmployeeCode(assignment.getEmployee().getEmployeeId());
        response.setAssignmentId(assignment.getId());

        SalaryStructure structure = assignment.getSalaryStructure();
        response.setSalaryStructureId(structure.getId());
        response.setSalaryStructureName(structure.getName());
        response.setSalaryStructureCode(structure.getCode());
        response.setSalaryStructureVersion(structure.getVersion());
        response.setCurrency(structure.getCurrency());
        response.setEffectiveDate(effectiveDate != null ? effectiveDate : LocalDate.now());

        Map<String, BigDecimal> computedValuesByCode = new HashMap<>();
        Map<Long, BigDecimal> computedValuesById = new HashMap<>();

        BigDecimal grossEarnings = BigDecimal.ZERO;
        BigDecimal totalBenefits = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;

        for (SalaryStructureComponent ssc : structureComponents) {
            SalaryComponent comp = ssc.getSalaryComponent();
            EmployeeSalaryComponentValue override = effectiveOverrides != null ? effectiveOverrides.get(comp.getId()) : null;

            BigDecimal calculatedAmount = BigDecimal.ZERO;
            BigDecimal appliedRate = null;
            boolean overrideApplied = false;

            if (override != null && override.getOverrideType() == ComponentOverrideType.FIXED_AMOUNT) {
                calculatedAmount = override.getAmount() != null ? override.getAmount() : BigDecimal.ZERO;
                appliedRate = null;
                overrideApplied = true;
            } else if (override != null && override.getOverrideType() == ComponentOverrideType.PERCENTAGE) {
                BigDecimal percentage = override.getPercentage() != null ? override.getPercentage() : BigDecimal.ZERO;
                appliedRate = percentage;
                overrideApplied = true;
                calculatedAmount = calculatePercentage(ssc, percentage, computedValuesById, grossEarnings);
            } else {
                // Structure default calculation rules
                CalculationType calcType = ssc.getCalculationType();
                if (calcType == CalculationType.FIXED) {
                    calculatedAmount = ssc.getFixedAmount() != null ? ssc.getFixedAmount() : BigDecimal.ZERO;
                    appliedRate = null;
                } else if (calcType == CalculationType.PERCENTAGE) {
                    BigDecimal percentage = ssc.getPercentage() != null ? ssc.getPercentage() : BigDecimal.ZERO;
                    appliedRate = percentage;
                    calculatedAmount = calculatePercentage(ssc, percentage, computedValuesById, grossEarnings);
                } else if (calcType == CalculationType.FORMULA) {
                    calculatedAmount = salaryFormulaEvaluator.evaluate(ssc.getFormula(), computedValuesByCode);
                    appliedRate = null;
                }
            }

            calculatedAmount = calculatedAmount.setScale(2, RoundingMode.HALF_UP);

            // Record into variable context for downstream DAG dependencies
            computedValuesByCode.put(comp.getCode().toUpperCase(), calculatedAmount);
            computedValuesById.put(comp.getId(), calculatedAmount);

            // Aggregate totals based on component type
            if (comp.getComponentType() == SalaryComponentType.EARNING) {
                grossEarnings = grossEarnings.add(calculatedAmount);
            } else if (comp.getComponentType() == SalaryComponentType.BENEFIT) {
                totalBenefits = totalBenefits.add(calculatedAmount);
            } else if (comp.getComponentType() == SalaryComponentType.DEDUCTION) {
                totalDeductions = totalDeductions.add(calculatedAmount);
            }

            SalaryCalculatedComponentResponse compResponse = new SalaryCalculatedComponentResponse(
                    comp.getId(),
                    comp.getCode(),
                    comp.getName(),
                    comp.getComponentType(),
                    ssc.getCalculationType(),
                    appliedRate,
                    calculatedAmount,
                    comp.getTaxable(),
                    overrideApplied
            );
            response.getComponents().add(compResponse);
        }

        response.setGrossPay(grossEarnings.setScale(2, RoundingMode.HALF_UP));
        response.setTotalBenefits(totalBenefits.setScale(2, RoundingMode.HALF_UP));
        response.setTotalDeductions(totalDeductions.setScale(2, RoundingMode.HALF_UP));

        BigDecimal netPay = grossEarnings.subtract(totalDeductions);
        if (netPay.compareTo(BigDecimal.ZERO) < 0) {
            netPay = BigDecimal.ZERO;
        }
        response.setNetPay(netPay.setScale(2, RoundingMode.HALF_UP));

        return response;
    }

    private BigDecimal calculatePercentage(
            SalaryStructureComponent ssc,
            BigDecimal percentage,
            Map<Long, BigDecimal> computedValuesById,
            BigDecimal grossEarnings) {

        CalculationBaseType baseType = ssc.getCalculationBaseType();

        if (baseType == CalculationBaseType.COMPONENT && ssc.getCalculationBaseComponent() != null) {
            Long baseId = ssc.getCalculationBaseComponent().getId();
            BigDecimal baseAmount = computedValuesById.getOrDefault(baseId, BigDecimal.ZERO);
            return baseAmount.multiply(percentage).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        } else if (baseType == CalculationBaseType.GROSS) {
            return grossEarnings.multiply(percentage).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
