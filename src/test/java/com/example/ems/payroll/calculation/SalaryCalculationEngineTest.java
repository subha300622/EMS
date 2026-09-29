package com.example.ems.payroll.calculation;

import com.example.ems.employee.entity.Employee;
import com.example.ems.payroll.dto.SalaryCalculationResponse;
import com.example.ems.payroll.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SalaryCalculationEngineTest {

    @Spy
    private SalaryFormulaEvaluator salaryFormulaEvaluator = new SalaryFormulaEvaluator();

    @InjectMocks
    private SalaryCalculationEngine calculationEngine;

    private Employee employee;
    private SalaryStructure structure;
    private EmployeeSalaryAssignment assignment;

    private SalaryComponent basicComp;
    private SalaryComponent housingComp;
    private SalaryComponent transportComp;
    private SalaryComponent insuranceComp;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1001L);
        employee.setFullName("Elsa Tester");
        employee.setEmployeeId("EMP-1001");

        structure = new SalaryStructure(1L, "Senior Developer", "SENIOR_DEV", null, "INR", PayFrequency.MONTHLY, null, null);
        structure.setId(102L);
        structure.setVersion(2);
        structure.setStatus(SalaryStructureStatus.ACTIVE);

        assignment = new EmployeeSalaryAssignment(1L, employee, structure, LocalDate.of(2026, 1, 1), null, SalaryAssignmentStatus.ACTIVE, "Standard");
        assignment.setId(500L);

        basicComp = new SalaryComponent(1L, "Basic Pay", "BASIC", null, SalaryComponentType.EARNING, true, true);
        basicComp.setId(1L);

        housingComp = new SalaryComponent(1L, "Housing Allowance", "HOUSING", null, SalaryComponentType.EARNING, true, true);
        housingComp.setId(2L);

        transportComp = new SalaryComponent(1L, "Transport Allowance", "TRANSPORT", null, SalaryComponentType.EARNING, true, true);
        transportComp.setId(3L);

        insuranceComp = new SalaryComponent(1L, "Insurance", "INSURANCE", null, SalaryComponentType.DEDUCTION, true, true);
        insuranceComp.setId(4L);
    }

    @Test
    @DisplayName("Calculate Salary - Standard flow: Basic (50k) + Housing (25% Basic) + Transport (3k) - Insurance (10% Gross)")
    void testCalculate_StandardWorkflow() {
        // Structure components
        SalaryStructureComponent ssc1 = new SalaryStructureComponent(structure, basicComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null, 1);
        SalaryStructureComponent ssc2 = new SalaryStructureComponent(structure, housingComp, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, basicComp, null, BigDecimal.valueOf(25), null, 2);
        SalaryStructureComponent ssc3 = new SalaryStructureComponent(structure, transportComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(3000), null, null, 3);
        SalaryStructureComponent ssc4 = new SalaryStructureComponent(structure, insuranceComp, CalculationType.PERCENTAGE, CalculationBaseType.GROSS, null, null, BigDecimal.valueOf(10), null, 4);

        List<SalaryStructureComponent> structureComponents = List.of(ssc1, ssc2, ssc3, ssc4);

        SalaryCalculationResponse response = calculationEngine.calculate(assignment, structureComponents, Map.of(), LocalDate.of(2026, 9, 1));

        assertNotNull(response);
        assertEquals(1001L, response.getEmployeeId());
        assertEquals("SENIOR_DEV", response.getSalaryStructureCode());
        assertEquals(4, response.getComponents().size());

        // Basic: 50,000
        assertEquals(new BigDecimal("50000.00"), response.getComponents().get(0).getAmount());
        // Housing: 25% of 50,000 = 12,500
        assertEquals(new BigDecimal("12500.00"), response.getComponents().get(1).getAmount());
        // Transport: 3,000
        assertEquals(new BigDecimal("3000.00"), response.getComponents().get(2).getAmount());
        // Insurance: 10% of Gross (65,500) = 6,550
        assertEquals(new BigDecimal("6550.00"), response.getComponents().get(3).getAmount());

        // Totals
        assertEquals(new BigDecimal("65500.00"), response.getGrossPay());
        assertEquals(new BigDecimal("6550.00"), response.getTotalDeductions());
        assertEquals(new BigDecimal("58950.00"), response.getNetPay());
    }

    @Test
    @DisplayName("Calculate Salary - With employee percentage override on Housing (30% instead of 25%)")
    void testCalculate_WithEmployeeOverride() {
        SalaryStructureComponent ssc1 = new SalaryStructureComponent(structure, basicComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null, 1);
        SalaryStructureComponent ssc2 = new SalaryStructureComponent(structure, housingComp, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, basicComp, null, BigDecimal.valueOf(25), null, 2);

        List<SalaryStructureComponent> structureComponents = List.of(ssc1, ssc2);

        // Employee override on Housing = 30%
        EmployeeSalaryComponentValue housingOverride = new EmployeeSalaryComponentValue(
                assignment, housingComp, null, BigDecimal.valueOf(30), ComponentOverrideType.PERCENTAGE
        );
        Map<Long, EmployeeSalaryComponentValue> overrides = Map.of(2L, housingOverride);

        SalaryCalculationResponse response = calculationEngine.calculate(assignment, structureComponents, overrides, LocalDate.of(2026, 9, 1));

        // Housing: 30% of 50,000 = 15,000
        assertEquals(new BigDecimal("15000.00"), response.getComponents().get(1).getAmount());
        assertTrue(response.getComponents().get(1).getOverrideApplied());
        assertEquals(new BigDecimal("65000.00"), response.getGrossPay());
        assertEquals(new BigDecimal("65000.00"), response.getNetPay());
    }

    @Test
    @DisplayName("Calculate Salary - Formula component evaluation (BASIC * 0.15 + 500)")
    void testCalculate_FormulaComponent() {
        SalaryComponent bonusComp = new SalaryComponent(1L, "Performance Bonus", "BONUS", null, SalaryComponentType.EARNING, true, true);
        bonusComp.setId(5L);

        SalaryStructureComponent ssc1 = new SalaryStructureComponent(structure, basicComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(40000), null, null, 1);
        SalaryStructureComponent ssc2 = new SalaryStructureComponent(structure, bonusComp, CalculationType.FORMULA, CalculationBaseType.NONE, null, null, null, "BASIC * 0.15 + 500", 2);

        List<SalaryStructureComponent> structureComponents = List.of(ssc1, ssc2);

        SalaryCalculationResponse response = calculationEngine.calculate(assignment, structureComponents, Map.of(), LocalDate.of(2026, 9, 1));

        // Bonus: 40000 * 0.15 + 500 = 6000 + 500 = 6500
        assertEquals(new BigDecimal("6500.00"), response.getComponents().get(1).getAmount());
        assertEquals(new BigDecimal("46500.00"), response.getGrossPay());
    }
}
