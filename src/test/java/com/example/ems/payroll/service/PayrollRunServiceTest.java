package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.dto.*;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.PayrollEmployeeRepository;
import com.example.ems.payroll.repository.PayrollItemRepository;
import com.example.ems.payroll.repository.PayrollRunRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PayrollRunServiceTest {

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private PayrollEmployeeRepository payrollEmployeeRepository;

    @Mock
    private PayrollItemRepository payrollItemRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryCalculationService salaryCalculationService;

    @InjectMocks
    private PayrollRunService payrollRunService;

    private final Long orgId = 1L;
    private Employee employee;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
        employee = new Employee();
        employee.setId(100L);
        employee.setFullName("John Doe");
        employee.setEmployeeId("EMP001");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Create Payroll Run - Success as DRAFT")
    void testCreatePayrollRun_Success() {
        PayrollRunCreateRequest request = new PayrollRunCreateRequest(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR"
        );

        when(payrollRunRepository.existsByOrganizationIdAndPeriodStartAndPeriodEnd(
                orgId, request.getPeriodStart(), request.getPeriodEnd())).thenReturn(false);
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(inv -> {
            PayrollRun r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        PayrollRunResponse response = payrollRunService.createPayrollRun(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(PayrollRunStatus.DRAFT, response.getStatus());
        assertEquals(LocalDate.of(2026, 9, 1), response.getPeriodStart());
        assertEquals(LocalDate.of(2026, 9, 30), response.getPeriodEnd());
    }

    @Test
    @DisplayName("Create Payroll Run - Invalid period throws BadRequestException")
    void testCreatePayrollRun_InvalidPeriod_ThrowsBadRequest() {
        PayrollRunCreateRequest request = new PayrollRunCreateRequest(
                LocalDate.of(2026, 9, 30), LocalDate.of(2026, 9, 1), "INR"
        );

        assertThrows(BadRequestException.class, () -> payrollRunService.createPayrollRun(request));
    }

    @Test
    @DisplayName("Create Payroll Run - Duplicate period throws ConflictException")
    void testCreatePayrollRun_DuplicatePeriod_ThrowsConflict() {
        PayrollRunCreateRequest request = new PayrollRunCreateRequest(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR"
        );

        when(payrollRunRepository.existsByOrganizationIdAndPeriodStartAndPeriodEnd(
                orgId, request.getPeriodStart(), request.getPeriodEnd())).thenReturn(true);

        assertThrows(ConflictException.class, () -> payrollRunService.createPayrollRun(request));
    }

    @Test
    @DisplayName("Process Payroll Run - Success calculates employees and creates items")
    void testProcessPayrollRun_Success() {
        PayrollRun run = new PayrollRun(orgId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR");
        run.setId(10L);

        SalaryCalculatedComponentResponse comp1 = new SalaryCalculatedComponentResponse(
                1L, "BASIC", "Basic Pay", SalaryComponentType.EARNING, CalculationType.FIXED, null, BigDecimal.valueOf(50000), true, false
        );
        SalaryCalculatedComponentResponse comp2 = new SalaryCalculatedComponentResponse(
                2L, "INSURANCE", "Insurance", SalaryComponentType.DEDUCTION, CalculationType.PERCENTAGE, BigDecimal.valueOf(10), BigDecimal.valueOf(5000), false, false
        );

        SalaryCalculationResponse calcResp = new SalaryCalculationResponse();
        calcResp.setEmployeeId(100L);
        calcResp.setGrossPay(BigDecimal.valueOf(50000));
        calcResp.setTotalBenefits(BigDecimal.ZERO);
        calcResp.setTotalDeductions(BigDecimal.valueOf(5000));
        calcResp.setNetPay(BigDecimal.valueOf(45000));
        calcResp.setCurrency("INR");
        calcResp.setComponents(List.of(comp1, comp2));

        when(payrollRunRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(run));
        when(employeeRepository.findByOrganizationId(orgId)).thenReturn(List.of(employee));
        when(salaryCalculationService.calculateSalaryForDate(100L, LocalDate.of(2026, 9, 30))).thenReturn(calcResp);
        when(payrollEmployeeRepository.save(any(PayrollEmployee.class))).thenAnswer(inv -> {
            PayrollEmployee pe = inv.getArgument(0);
            pe.setId(101L);
            return pe;
        });
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(inv -> inv.getArgument(0));

        PayrollRunResponse response = payrollRunService.processPayrollRun(10L);

        assertNotNull(response);
        assertEquals(PayrollRunStatus.CALCULATED, response.getStatus());
        assertEquals(1, response.getTotalEmployees());
        assertEquals(1, response.getProcessedEmployees());
        assertEquals(0, BigDecimal.valueOf(50000).compareTo(response.getTotalGross()));
        assertEquals(0, BigDecimal.valueOf(5000).compareTo(response.getTotalDeductions()));
        assertEquals(0, BigDecimal.valueOf(45000).compareTo(response.getTotalNet()));

        verify(payrollItemRepository, times(2)).save(any(PayrollItem.class));
    }

    @Test
    @DisplayName("Process Payroll Run - Employee calculation failure marks run as FAILED")
    void testProcessPayrollRun_WithFailure_MarksFailed() {
        PayrollRun run = new PayrollRun(orgId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR");
        run.setId(10L);

        when(payrollRunRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(run));
        when(employeeRepository.findByOrganizationId(orgId)).thenReturn(List.of(employee));
        when(salaryCalculationService.calculateSalaryForDate(100L, LocalDate.of(2026, 9, 30)))
                .thenThrow(new ResourceNotFoundException("No active salary assignment found"));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(inv -> inv.getArgument(0));

        PayrollRunResponse response = payrollRunService.processPayrollRun(10L);

        assertNotNull(response);
        assertEquals(PayrollRunStatus.FAILED, response.getStatus());
        assertEquals(1, response.getTotalEmployees());
        assertEquals(0, response.getProcessedEmployees());

        verify(payrollEmployeeRepository).save(argThat(pe -> pe.getStatus() == PayrollEmployeeStatus.FAILED));
    }

    @Test
    @DisplayName("Finalize Payroll Run - Success sets FINALIZED status")
    void testFinalizePayrollRun_Success() {
        PayrollRun run = new PayrollRun(orgId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR");
        run.setId(10L);
        run.setStatus(PayrollRunStatus.CALCULATED);
        run.setTotalEmployees(1);
        run.setProcessedEmployees(1);

        when(payrollRunRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(inv -> inv.getArgument(0));

        PayrollRunResponse response = payrollRunService.finalizePayrollRun(10L);

        assertNotNull(response);
        assertEquals(PayrollRunStatus.FINALIZED, response.getStatus());
        assertNotNull(response.getFinalizedAt());
    }

    @Test
    @DisplayName("Finalize Payroll Run - Uncalculated or failed run throws BadRequestException")
    void testFinalizePayrollRun_InvalidStatus_ThrowsBadRequest() {
        PayrollRun run = new PayrollRun(orgId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR");
        run.setId(10L);
        run.setStatus(PayrollRunStatus.DRAFT);

        when(payrollRunRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(run));

        assertThrows(BadRequestException.class, () -> payrollRunService.finalizePayrollRun(10L));
    }
}
