package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.calculation.SalaryCalculationEngine;
import com.example.ems.payroll.dto.EmployeeSalaryComponentValueRequest;
import com.example.ems.payroll.dto.SalaryCalculationPreviewRequest;
import com.example.ems.payroll.dto.SalaryCalculationResponse;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.EmployeeSalaryAssignmentRepository;
import com.example.ems.payroll.repository.EmployeeSalaryComponentValueRepository;
import com.example.ems.payroll.repository.SalaryStructureComponentRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryCalculationServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeSalaryAssignmentRepository employeeSalaryAssignmentRepository;

    @Mock
    private EmployeeSalaryComponentValueRepository employeeSalaryComponentValueRepository;

    @Mock
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    @Spy
    private SalaryCalculationEngine salaryCalculationEngine = new SalaryCalculationEngine();

    @InjectMocks
    private SalaryCalculationService salaryCalculationService;

    private final Long orgId = 1L;
    private Employee employee;
    private SalaryStructure structure;
    private EmployeeSalaryAssignment assignment;
    private SalaryComponent basicComp;
    private SalaryComponent hraComp;
    private SalaryStructureComponent sscBasic;
    private SalaryStructureComponent sscHra;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);

        employee = new Employee();
        employee.setId(1001L);
        employee.setFullName("John Developer");
        employee.setEmployeeId("EMP-1001");

        structure = new SalaryStructure(orgId, "Standard Dev", "DEV_STD", null, "INR", PayFrequency.MONTHLY, null, null);
        structure.setId(200L);
        structure.setStatus(SalaryStructureStatus.ACTIVE);
        structure.setVersion(1);

        assignment = new EmployeeSalaryAssignment(orgId, employee, structure, LocalDate.of(2026, 1, 1), null, SalaryAssignmentStatus.ACTIVE, "Regular");
        assignment.setId(50L);

        basicComp = new SalaryComponent(orgId, "Basic Pay", "BASIC", null, SalaryComponentType.EARNING, true, true);
        basicComp.setId(1L);

        hraComp = new SalaryComponent(orgId, "Housing", "HRA", null, SalaryComponentType.EARNING, true, true);
        hraComp.setId(2L);

        sscBasic = new SalaryStructureComponent(structure, basicComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null, 1);
        sscHra = new SalaryStructureComponent(structure, hraComp, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, basicComp, null, BigDecimal.valueOf(25), null, 2);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Calculate Current Salary - Resolves active assignment and computes breakdown")
    void testCalculateCurrentSalary_Success() {
        LocalDate today = LocalDate.now();

        when(employeeRepository.findByIdAndOrganizationId(1001L, orgId)).thenReturn(Optional.of(employee));
        when(employeeSalaryAssignmentRepository.findActiveAssignmentsForDate(orgId, 1001L, today)).thenReturn(List.of(assignment));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(200L)).thenReturn(List.of(sscBasic, sscHra));
        when(employeeSalaryComponentValueRepository.findBySalaryAssignmentId(50L)).thenReturn(Collections.emptyList());

        SalaryCalculationResponse response = salaryCalculationService.calculateCurrentSalary(1001L);

        assertNotNull(response);
        assertEquals(1001L, response.getEmployeeId());
        assertEquals("DEV_STD", response.getSalaryStructureCode());
        assertEquals(new BigDecimal("50000.00"), response.getComponents().get(0).getAmount());
        assertEquals(new BigDecimal("12500.00"), response.getComponents().get(1).getAmount());
        assertEquals(new BigDecimal("62500.00"), response.getGrossPay());
        assertEquals(new BigDecimal("62500.00"), response.getNetPay());
    }

    @Test
    @DisplayName("Preview Salary Calculation - With ad-hoc overrides on HRA without persisting")
    void testPreviewSalaryCalculation_WithAdHocOverrides() {
        LocalDate previewDate = LocalDate.of(2026, 9, 1);
        EmployeeSalaryComponentValueRequest adHocHra = new EmployeeSalaryComponentValueRequest(
                2L, ComponentOverrideType.PERCENTAGE, null, BigDecimal.valueOf(35)
        );
        SalaryCalculationPreviewRequest previewReq = new SalaryCalculationPreviewRequest(previewDate, List.of(adHocHra));

        when(employeeRepository.findByIdAndOrganizationId(1001L, orgId)).thenReturn(Optional.of(employee));
        when(employeeSalaryAssignmentRepository.findActiveAssignmentsForDate(orgId, 1001L, previewDate)).thenReturn(List.of(assignment));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(200L)).thenReturn(List.of(sscBasic, sscHra));
        when(employeeSalaryComponentValueRepository.findBySalaryAssignmentId(50L)).thenReturn(Collections.emptyList());

        SalaryCalculationResponse response = salaryCalculationService.previewSalaryCalculation(1001L, previewReq);

        assertNotNull(response);
        // HRA with 35% of 50000 = 17500
        assertEquals(new BigDecimal("17500.00"), response.getComponents().get(1).getAmount());
        assertEquals(new BigDecimal("67500.00"), response.getGrossPay());
        assertEquals(new BigDecimal("67500.00"), response.getNetPay());
    }

    @Test
    @DisplayName("Preview Salary Calculation - Reject ad-hoc override if component not in structure")
    void testPreviewSalaryCalculation_ComponentNotInStructure_ThrowsBadRequest() {
        LocalDate previewDate = LocalDate.of(2026, 9, 1);
        EmployeeSalaryComponentValueRequest adHocInvalid = new EmployeeSalaryComponentValueRequest(
                999L, ComponentOverrideType.FIXED_AMOUNT, BigDecimal.valueOf(1000), null
        );
        SalaryCalculationPreviewRequest previewReq = new SalaryCalculationPreviewRequest(previewDate, List.of(adHocInvalid));

        when(employeeRepository.findByIdAndOrganizationId(1001L, orgId)).thenReturn(Optional.of(employee));
        when(employeeSalaryAssignmentRepository.findActiveAssignmentsForDate(orgId, 1001L, previewDate)).thenReturn(List.of(assignment));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(200L)).thenReturn(List.of(sscBasic, sscHra));

        assertThrows(BadRequestException.class, () -> salaryCalculationService.previewSalaryCalculation(1001L, previewReq));
    }

    @Test
    @DisplayName("Calculate Salary - Reject if no active assignment on date")
    void testCalculateSalary_NoAssignment_ThrowsNotFound() {
        LocalDate previewDate = LocalDate.of(2025, 1, 1);

        when(employeeRepository.findByIdAndOrganizationId(1001L, orgId)).thenReturn(Optional.of(employee));
        when(employeeSalaryAssignmentRepository.findActiveAssignmentsForDate(orgId, 1001L, previewDate)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> salaryCalculationService.previewSalaryCalculation(1001L, new SalaryCalculationPreviewRequest(previewDate)));
    }
}
