package com.example.ems.payroll.service;

import com.example.ems.payroll.dto.PayrollEmployeeResponse;
import com.example.ems.payroll.dto.PayrollItemResponse;
import com.example.ems.payroll.dto.PayslipDetailResponse;
import com.example.ems.payroll.entity.PayrollEmployee;
import com.example.ems.payroll.entity.PayrollItem;
import com.example.ems.payroll.entity.PayrollRun;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PayrollDetailServiceTest {

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private PayrollEmployeeRepository payrollEmployeeRepository;

    @Mock
    private PayrollItemRepository payrollItemRepository;

    @InjectMocks
    private PayrollDetailService payrollDetailService;

    private final Long orgId = 1L;
    private PayrollRun run;
    private PayrollEmployee pe;
    private PayrollItem item;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
        run = new PayrollRun(orgId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR");
        run.setId(10L);

        pe = new PayrollEmployee();
        pe.setId(101L);
        pe.setOrganizationId(orgId);
        pe.setPayrollRunId(10L);
        pe.setEmployeeId(100L);
        pe.setEmployeeName("Alice");
        pe.setEmployeeCode("EMP001");
        pe.setGrossAmount(BigDecimal.valueOf(50000));
        pe.setNetAmount(BigDecimal.valueOf(45000));
        pe.setCurrency("INR");

        item = new PayrollItem();
        item.setId(201L);
        item.setOrganizationId(orgId);
        item.setPayrollEmployeeId(101L);
        item.setComponentCode("BASIC");
        item.setComponentName("Basic Salary");
        item.setComponentType("EARNING");
        item.setAmount(BigDecimal.valueOf(50000));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Get Payroll Employees - Success returns list")
    void testGetPayrollEmployees_Success() {
        when(payrollRunRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(run));
        when(payrollEmployeeRepository.findByPayrollRunIdAndOrganizationIdOrderByIdAsc(10L, orgId))
                .thenReturn(List.of(pe));

        List<PayrollEmployeeResponse> list = payrollDetailService.getPayrollEmployees(10L);

        assertEquals(1, list.size());
        assertEquals("Alice", list.get(0).getEmployeeName());
    }

    @Test
    @DisplayName("Get Payroll Items - Success returns item list")
    void testGetPayrollItems_Success() {
        when(payrollRunRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(run));
        when(payrollEmployeeRepository.findByIdAndOrganizationId(101L, orgId)).thenReturn(Optional.of(pe));
        when(payrollItemRepository.findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(101L, orgId))
                .thenReturn(List.of(item));

        List<PayrollItemResponse> list = payrollDetailService.getPayrollItems(10L, 101L);

        assertEquals(1, list.size());
        assertEquals("BASIC", list.get(0).getComponentCode());
    }

    @Test
    @DisplayName("Get Payslip Detail - Success returns complete payslip snapshot")
    void testGetPayslip_Success() {
        when(payrollRunRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(run));
        when(payrollEmployeeRepository.findByIdAndOrganizationId(101L, orgId)).thenReturn(Optional.of(pe));
        when(payrollItemRepository.findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(101L, orgId))
                .thenReturn(List.of(item));

        PayslipDetailResponse payslip = payrollDetailService.getPayslip(10L, 101L);

        assertNotNull(payslip);
        assertEquals(10L, payslip.getPayrollRunId());
        assertEquals(101L, payslip.getPayrollEmployeeId());
        assertEquals("Alice", payslip.getEmployeeName());
        assertEquals(1, payslip.getItems().size());
        assertEquals("BASIC", payslip.getItems().get(0).getComponentCode());
    }
}
