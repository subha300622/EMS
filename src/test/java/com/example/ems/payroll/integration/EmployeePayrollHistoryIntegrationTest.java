package com.example.ems.payroll.integration;

import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.payroll.controller.EmployeePayrollHistoryController;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.*;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EmployeePayrollHistoryIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private EmployeePayrollHistoryController employeePayrollHistoryController;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRunRepository payrollRunRepository;

    @Autowired
    private PayrollEmployeeRepository payrollEmployeeRepository;

    @Autowired
    private PayrollItemRepository payrollItemRepository;

    private Long testOrgId;
    private Employee testEmployee;
    private PayrollRun testRun;
    private PayrollEmployee testPayrollEmployee;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeePayrollHistoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        long ts = System.currentTimeMillis();
        Organization org = new Organization();
        org.setName("Payroll History Test Org " + ts);
        org.setOrganizationCode("ORG_HIST_" + ts);
        org = organizationRepository.save(org);
        testOrgId = org.getId();

        TenantContext.setCurrentTenant(testOrgId);

        Employee emp = new Employee();
        emp.setFullName("John Doe History");
        emp.setEmail("johnhistory" + ts + "@acme.com");
        emp.setEmployeeId("EMP-HIST-" + ts);
        emp.setStatus("ACTIVE");
        emp.setOrganization(org);
        testEmployee = employeeRepository.save(emp);

        testRun = new PayrollRun();
        testRun.setOrganizationId(testOrgId);
        testRun.setPeriodStart(LocalDate.of(2026, 8, 1));
        testRun.setPeriodEnd(LocalDate.of(2026, 8, 31));
        testRun.setStatus(PayrollRunStatus.FINALIZED);
        testRun.setTotalEmployees(1);
        testRun.setProcessedEmployees(1);
        testRun.setTotalGross(new BigDecimal("50000.00"));
        testRun.setTotalDeductions(new BigDecimal("5000.00"));
        testRun.setTotalBenefits(BigDecimal.ZERO);
        testRun.setTotalNet(new BigDecimal("45000.00"));
        testRun.setCurrency("INR");
        testRun = payrollRunRepository.save(testRun);

        testPayrollEmployee = new PayrollEmployee();
        testPayrollEmployee.setOrganizationId(testOrgId);
        testPayrollEmployee.setPayrollRunId(testRun.getId());
        testPayrollEmployee.setEmployeeId(testEmployee.getId());
        testPayrollEmployee.setEmployeeName(testEmployee.getFullName());
        testPayrollEmployee.setEmployeeCode(testEmployee.getEmployeeId());
        testPayrollEmployee.setGrossAmount(new BigDecimal("50000.00"));
        testPayrollEmployee.setDeductionsAmount(new BigDecimal("5000.00"));
        testPayrollEmployee.setBenefitsAmount(BigDecimal.ZERO);
        testPayrollEmployee.setNetAmount(new BigDecimal("45000.00"));
        testPayrollEmployee.setCurrency("INR");
        testPayrollEmployee.setStatus(PayrollEmployeeStatus.CALCULATED);
        testPayrollEmployee.setCalculationDate(LocalDate.of(2026, 8, 31));
        testPayrollEmployee = payrollEmployeeRepository.save(testPayrollEmployee);

        PayrollItem item1 = new PayrollItem();
        item1.setOrganizationId(testOrgId);
        item1.setPayrollEmployeeId(testPayrollEmployee.getId());
        item1.setComponentCode("BASIC");
        item1.setComponentName("Basic Salary");
        item1.setComponentType("EARNING");
        item1.setCalculationType("FIXED");
        item1.setAmount(new BigDecimal("40000.00"));
        payrollItemRepository.save(item1);

        PayrollItem item2 = new PayrollItem();
        item2.setOrganizationId(testOrgId);
        item2.setPayrollEmployeeId(testPayrollEmployee.getId());
        item2.setComponentCode("PF");
        item2.setComponentName("Provident Fund");
        item2.setComponentType("DEDUCTION");
        item2.setCalculationType("FIXED");
        item2.setAmount(new BigDecimal("5000.00"));
        payrollItemRepository.save(item2);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("GET /api/v1/employees/{employeeId}/payroll-history - Success")
    void testGetEmployeePayrollHistory() throws Exception {
        mockMvc.perform(get("/api/v1/employees/{employeeId}/payroll-history", testEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].employeeId").value(testEmployee.getId()))
                .andExpect(jsonPath("$.data[0].runId").value(testRun.getId()))
                .andExpect(jsonPath("$.data[0].grossAmount").value(50000.00))
                .andExpect(jsonPath("$.data[0].deductionsAmount").value(5000.00))
                .andExpect(jsonPath("$.data[0].netAmount").value(45000.00))
                .andExpect(jsonPath("$.data[0].runStatus").value("FINALIZED"));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{employeeId}/payroll-history/{runId} - Success")
    void testGetEmployeePayrollHistoryByRun() throws Exception {
        mockMvc.perform(get("/api/v1/employees/{employeeId}/payroll-history/{runId}", testEmployee.getId(), testRun.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(testEmployee.getId()))
                .andExpect(jsonPath("$.data.payrollRunId").value(testRun.getId()))
                .andExpect(jsonPath("$.data.grossAmount").value(50000.00))
                .andExpect(jsonPath("$.data.deductionsAmount").value(5000.00))
                .andExpect(jsonPath("$.data.netAmount").value(45000.00))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{employeeId}/payroll-history - 404 for Unknown Employee")
    void testGetPayrollHistoryUnknownEmployee() throws Exception {
        mockMvc.perform(get("/api/v1/employees/{employeeId}/payroll-history", 9999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/employees/{employeeId}/payroll-history/{runId} - 404 for Unknown Run")
    void testGetPayrollHistoryUnknownRun() throws Exception {
        mockMvc.perform(get("/api/v1/employees/{employeeId}/payroll-history/{runId}", testEmployee.getId(), 9999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
