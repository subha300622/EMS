package com.example.ems.payroll.integration;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.payroll.controller.*;
import com.example.ems.payroll.dto.*;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.EmployeeSalaryComponentValueRepository;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PayrollRunIntegrationTest {

        private MockMvc salaryComponentMockMvc;
        private MockMvc salaryStructureMockMvc;
        private MockMvc salaryAssignmentMockMvc;
        private MockMvc payrollRunMockMvc;
        private MockMvc payrollEmployeeMockMvc;

        @Autowired
        private SalaryComponentController salaryComponentController;

        @Autowired
        private SalaryStructureController salaryStructureController;

        @Autowired
        private EmployeeSalaryAssignmentController employeeSalaryAssignmentController;

        @Autowired
        private PayrollRunController payrollRunController;

        @Autowired
        private PayrollEmployeeController payrollEmployeeController;

        @Autowired
        private OrganizationRepository organizationRepository;

        @Autowired
        private EmployeeRepository employeeRepository;

        @Autowired
        private EmployeeSalaryComponentValueRepository employeeSalaryComponentValueRepository;

        @Autowired
        private com.example.ems.payroll.repository.SalaryComponentRepository salaryComponentRepository;

        @Autowired
        private com.example.ems.payroll.repository.EmployeeSalaryAssignmentRepository employeeSalaryAssignmentRepository;

        @Autowired
        private ObjectMapper objectMapper;

        private Organization organization;
        private Employee employee;

        @BeforeEach
        void setUp() {
                com.example.ems.config.GlobalExceptionHandler exceptionHandler = new com.example.ems.config.GlobalExceptionHandler();
                salaryComponentMockMvc = MockMvcBuilders.standaloneSetup(salaryComponentController)
                                .setControllerAdvice(exceptionHandler).build();
                salaryStructureMockMvc = MockMvcBuilders.standaloneSetup(salaryStructureController)
                                .setControllerAdvice(exceptionHandler).build();
                salaryAssignmentMockMvc = MockMvcBuilders.standaloneSetup(employeeSalaryAssignmentController)
                                .setControllerAdvice(exceptionHandler).build();
                payrollRunMockMvc = MockMvcBuilders.standaloneSetup(payrollRunController)
                                .setControllerAdvice(exceptionHandler).build();
                payrollEmployeeMockMvc = MockMvcBuilders.standaloneSetup(payrollEmployeeController)
                                .setControllerAdvice(exceptionHandler).build();

                organization = new Organization();
                organization.setName("Tech Corp " + System.currentTimeMillis());
                organization.setOrganizationCode("TECH_" + System.currentTimeMillis());
                organization = organizationRepository.save(organization);

                employee = new Employee();
                employee.setFullName("John Doe");
                employee.setEmail("john." + System.currentTimeMillis() + "@tech.com");
                employee.setEmployeeId("EMP_" + System.currentTimeMillis());
                employee.setOrganization(organization);
                employee = employeeRepository.save(employee);
        }

        @AfterEach
        void tearDown() {
                TenantContext.clear();
        }

        private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
                assertNotNull(actual, "Actual BigDecimal is null");
                assertEquals(0, expected.compareTo(actual), "Expected " + expected + " but got " + actual);
        }

        @Test
        @DisplayName("Complete Payroll Run Lifecycle: Draft -> Process -> Calculate -> Finalize -> Immutable Payslip")
        void testFullPayrollRunLifecycle() throws Exception {
                TenantContext.setCurrentTenant(organization.getId());

                // 1. Create Components: Basic (50k), Housing (25%), Insurance (10% of Gross)
                SalaryComponentCreateRequest cBasic = new SalaryComponentCreateRequest("Basic Salary", "BASIC", "Basic",
                                SalaryComponentType.EARNING, true, true);
                Long idBasic = objectMapper
                                .readTree(salaryComponentMockMvc
                                                .perform(post("/api/v1/salary-components")
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .content(objectMapper.writeValueAsString(cBasic)))
                                                .andReturn().getResponse().getContentAsString())
                                .path("data").path("id").asLong();

                SalaryComponentCreateRequest cHousing = new SalaryComponentCreateRequest("Housing Allowance", "HOUSING",
                                "Housing", SalaryComponentType.EARNING, true, true);
                Long idHousing = objectMapper
                                .readTree(salaryComponentMockMvc
                                                .perform(post("/api/v1/salary-components")
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .content(objectMapper.writeValueAsString(cHousing)))
                                                .andReturn().getResponse().getContentAsString())
                                .path("data").path("id").asLong();

                SalaryComponentCreateRequest cInsurance = new SalaryComponentCreateRequest("Insurance Deduction",
                                "INSURANCE", "Insurance", SalaryComponentType.DEDUCTION, true, true);
                Long idInsurance = objectMapper
                                .readTree(salaryComponentMockMvc
                                                .perform(post("/api/v1/salary-components")
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .content(objectMapper.writeValueAsString(cInsurance)))
                                                .andReturn().getResponse().getContentAsString())
                                .path("data").path("id").asLong();

                // 2. Create and Activate Structure
                SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest("Standard Software Engineer",
                                "SWE_STD", "Standard", "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null);
                Long structId = objectMapper
                                .readTree(salaryStructureMockMvc
                                                .perform(post("/api/v1/salary-structures")
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .content(objectMapper.writeValueAsString(structReq)))
                                                .andReturn().getResponse().getContentAsString())
                                .path("data").path("id").asLong();

                salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new StructureComponentCreateRequest(idBasic,
                                                CalculationType.FIXED, CalculationBaseType.NONE, null,
                                                BigDecimal.valueOf(50000), null, null, 1))));
                salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new StructureComponentCreateRequest(idHousing,
                                                CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, idBasic,
                                                null, BigDecimal.valueOf(25), null, 2))));
                salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new StructureComponentCreateRequest(
                                                idInsurance, CalculationType.PERCENTAGE, CalculationBaseType.GROSS,
                                                null, null, BigDecimal.valueOf(10), null, 3))));

                salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/activate"))
                                .andExpect(status().isOk());

                // 3. Assign Structure to Employee from 2026-01-01
                salaryAssignmentMockMvc.perform(post("/api/v1/employees/" + employee.getId() + "/salary-assignments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new EmployeeSalaryAssignmentCreateRequest(
                                                structId, LocalDate.of(2026, 1, 1), null, "Initial hiring"))))
                                .andExpect(status().isCreated());

                // 4. Create Payroll Run for September 2026
                PayrollRunCreateRequest runReq = new PayrollRunCreateRequest(LocalDate.of(2026, 9, 1),
                                LocalDate.of(2026, 9, 30), "INR");
                MvcResult runRes = payrollRunMockMvc.perform(post("/api/v1/payroll/runs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(runReq)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                                .andReturn();
                Long runId = objectMapper.readTree(runRes.getResponse().getContentAsString()).path("data").path("id")
                                .asLong();

                // 5. Process Payroll Run
                MvcResult procRes = payrollRunMockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/process"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("CALCULATED"))
                                .andExpect(jsonPath("$.data.totalEmployees").value(1))
                                .andExpect(jsonPath("$.data.processedEmployees").value(1))
                                .andReturn();

                PayrollRunResponse procRun = objectMapper.treeToValue(
                                objectMapper.readTree(procRes.getResponse().getContentAsString()).path("data"),
                                PayrollRunResponse.class);
                // Gross = 50k + 12.5k = 62,500
                assertBigDecimalEquals(new BigDecimal("62500.00"), procRun.getTotalGross());
                // Deductions = 10% of 62,500 = 6,250
                assertBigDecimalEquals(new BigDecimal("6250.00"), procRun.getTotalDeductions());
                // Net = 62,500 - 6,250 = 56,250
                assertBigDecimalEquals(new BigDecimal("56250.00"), procRun.getTotalNet());

                // 6. Get Payroll Employees & Items
                MvcResult empListRes = payrollEmployeeMockMvc
                                .perform(get("/api/v1/payroll/runs/" + runId + "/employees"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.length()").value(1))
                                .andReturn();
                Long peId = objectMapper.readTree(empListRes.getResponse().getContentAsString()).path("data").get(0)
                                .path("id").asLong();

                payrollEmployeeMockMvc.perform(get("/api/v1/payroll/runs/" + runId + "/employees/" + peId + "/items"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.length()").value(3));

                // 7. Finalize Payroll Run
                payrollRunMockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/finalize"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("FINALIZED"));

                // 8. View Payslip Snapshot
                MvcResult payslipRes = payrollEmployeeMockMvc
                                .perform(get("/api/v1/payroll/runs/" + runId + "/employees/" + peId + "/payslip"))
                                .andExpect(status().isOk())
                                .andReturn();
                PayslipDetailResponse payslip = objectMapper.treeToValue(
                                objectMapper.readTree(payslipRes.getResponse().getContentAsString()).path("data"),
                                PayslipDetailResponse.class);
                assertBigDecimalEquals(new BigDecimal("62500.00"), payslip.getGrossAmount());
                assertBigDecimalEquals(new BigDecimal("6250.00"), payslip.getDeductionsAmount());
                assertBigDecimalEquals(new BigDecimal("56250.00"), payslip.getNetAmount());

                // 9. Verify Immutability: Re-processing a finalized run is REJECTED
                payrollRunMockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/process"))
                                .andExpect(status().isConflict());

                // 10. Future changes to employee salary or structure do NOT alter the finalized
                // September snapshot
                SalaryComponent basicComp = salaryComponentRepository.findById(idBasic).orElseThrow();
                EmployeeSalaryAssignment assignment = employeeSalaryAssignmentRepository
                                .findByOrganizationIdAndEmployeeIdOrderByEffectiveFromDesc(organization.getId(),
                                                employee.getId())
                                .get(0);
                EmployeeSalaryComponentValue overrideVal = new EmployeeSalaryComponentValue(
                                assignment, basicComp, BigDecimal.valueOf(100000), null,
                                ComponentOverrideType.FIXED_AMOUNT);
                employeeSalaryComponentValueRepository.save(overrideVal);

                // Payslip snapshot for September still returns ₹56,250 (NOT recalculated!)
                MvcResult payslipVerifyRes = payrollEmployeeMockMvc
                                .perform(get("/api/v1/payroll/runs/" + runId + "/employees/" + peId + "/payslip"))
                                .andExpect(status().isOk())
                                .andReturn();
                PayslipDetailResponse payslipAfterOverride = objectMapper.treeToValue(
                                objectMapper.readTree(payslipVerifyRes.getResponse().getContentAsString()).path("data"),
                                PayslipDetailResponse.class);
                assertBigDecimalEquals(new BigDecimal("56250.00"), payslipAfterOverride.getNetAmount());
        }
}
