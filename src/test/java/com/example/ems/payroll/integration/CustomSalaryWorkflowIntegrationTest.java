package com.example.ems.payroll.integration;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.payroll.controller.EmployeeSalaryAssignmentController;
import com.example.ems.payroll.controller.SalaryCalculationController;
import com.example.ems.payroll.controller.SalaryComponentController;
import com.example.ems.payroll.controller.SalaryStructureController;
import com.example.ems.payroll.dto.*;
import com.example.ems.payroll.entity.*;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CustomSalaryWorkflowIntegrationTest {

    private MockMvc salaryComponentMockMvc;
    private MockMvc salaryStructureMockMvc;
    private MockMvc salaryAssignmentMockMvc;
    private MockMvc salaryCalculationMockMvc;

    @Autowired
    private SalaryComponentController salaryComponentController;

    @Autowired
    private SalaryStructureController salaryStructureController;

    @Autowired
    private EmployeeSalaryAssignmentController employeeSalaryAssignmentController;

    @Autowired
    private SalaryCalculationController salaryCalculationController;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Organization organization;
    private Employee employee;

    @BeforeEach
    void setUp() {
        salaryComponentMockMvc = MockMvcBuilders.standaloneSetup(salaryComponentController).build();
        salaryStructureMockMvc = MockMvcBuilders.standaloneSetup(salaryStructureController).build();
        salaryAssignmentMockMvc = MockMvcBuilders.standaloneSetup(employeeSalaryAssignmentController).build();
        salaryCalculationMockMvc = MockMvcBuilders.standaloneSetup(salaryCalculationController).build();

        organization = new Organization();
        organization.setName("Workflow Org");
        organization.setOrganizationCode("ORG_WF_" + System.currentTimeMillis());
        organization = organizationRepository.save(organization);

        TenantContext.setCurrentTenant(organization.getId());

        employee = new Employee();
        employee.setFullName("John Workflow");
        employee.setEmail("john.workflow." + System.currentTimeMillis() + "@test.com");
        employee.setEmployeeId("WF-EMP-" + System.currentTimeMillis());
        employee.setOrganization(organization);
        employee = employeeRepository.save(employee);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("End-to-End API Test: Batch 1 through Batch 6 Full Custom Salary Workflow")
    void testFullSalaryWorkflowE2E() throws Exception {
        // =========================================================================
        // 1. BATCH 1: Create Salary Components (Basic, Housing, Transport, Insurance)
        // =========================================================================
        SalaryComponentCreateRequest basicReq = new SalaryComponentCreateRequest("Basic Pay", "BASIC", "Basic Salary", SalaryComponentType.EARNING, true, true);
        MvcResult basicRes = salaryComponentMockMvc.perform(post("/api/v1/salary-components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basicReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("BASIC"))
                .andReturn();
        Long basicId = objectMapper.readTree(basicRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryComponentCreateRequest housingReq = new SalaryComponentCreateRequest("Housing Allowance", "HOUSING", "Housing", SalaryComponentType.EARNING, true, true);
        MvcResult housingRes = salaryComponentMockMvc.perform(post("/api/v1/salary-components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(housingReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("HOUSING"))
                .andReturn();
        Long housingId = objectMapper.readTree(housingRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryComponentCreateRequest transportReq = new SalaryComponentCreateRequest("Transport Allowance", "TRANSPORT", "Transport", SalaryComponentType.EARNING, true, true);
        MvcResult transportRes = salaryComponentMockMvc.perform(post("/api/v1/salary-components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transportReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("TRANSPORT"))
                .andReturn();
        Long transportId = objectMapper.readTree(transportRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryComponentCreateRequest insuranceReq = new SalaryComponentCreateRequest("Health Insurance", "INSURANCE", "Insurance", SalaryComponentType.DEDUCTION, true, true);
        MvcResult insuranceRes = salaryComponentMockMvc.perform(post("/api/v1/salary-components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insuranceReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("INSURANCE"))
                .andReturn();
        Long insuranceId = objectMapper.readTree(insuranceRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // List components
        salaryComponentMockMvc.perform(get("/api/v1/salary-components"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(4));

        // =========================================================================
        // 2. BATCH 2: Create Salary Structure
        // =========================================================================
        SalaryStructureCreateRequest structureReq = new SalaryStructureCreateRequest(
                "Senior Developer Structure", "SENIOR_DEV", "Standard engineering template",
                "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null
        );

        MvcResult structRes = salaryStructureMockMvc.perform(post("/api/v1/salary-structures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(structureReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("SENIOR_DEV"))
                .andExpect(jsonPath("$.data.version").value(1))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();
        Long structureId = objectMapper.readTree(structRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // =========================================================================
        // 3. BATCH 3: Attach Components & Define Calculation Rules
        // =========================================================================
        // A. Basic Pay: FIXED 50,000
        StructureComponentCreateRequest sscBasic = new StructureComponentCreateRequest(
                basicId, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null, 1
        );
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structureId + "/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sscBasic)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.componentCode").value("BASIC"));

        // B. Housing: 25% of Basic Pay
        StructureComponentCreateRequest sscHousing = new StructureComponentCreateRequest(
                housingId, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, basicId, null, BigDecimal.valueOf(25), null, 2
        );
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structureId + "/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sscHousing)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.componentCode").value("HOUSING"));

        // C. Transport: FIXED 3,000
        StructureComponentCreateRequest sscTransport = new StructureComponentCreateRequest(
                transportId, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(3000), null, null, 3
        );
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structureId + "/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sscTransport)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.componentCode").value("TRANSPORT"));

        // D. Insurance: 10% of Gross
        StructureComponentCreateRequest sscInsurance = new StructureComponentCreateRequest(
                insuranceId, CalculationType.PERCENTAGE, CalculationBaseType.GROSS, null, null, BigDecimal.valueOf(10), null, 4
        );
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structureId + "/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sscInsurance)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.componentCode").value("INSURANCE"));

        // =========================================================================
        // 4. BATCH 4: Dependency Graph Preview, DAG Validation & Activation
        // =========================================================================
        // Preview dependency graph
        salaryStructureMockMvc.perform(get("/api/v1/salary-structures/" + structureId + "/dependency-graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasCycle").value(false))
                .andExpect(jsonPath("$.data.components.length()").value(4));

        // Validate structure -> advances to VALIDATED
        MvcResult valRes = salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structureId + "/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.status").value("VALIDATED"))
                .andReturn();
        List<String> calcOrder = objectMapper.readerForListOf(String.class)
                .readValue(objectMapper.readTree(valRes.getResponse().getContentAsString()).path("data").path("calculationOrder"));
        assertTrue(calcOrder.indexOf("BASIC") < calcOrder.indexOf("HOUSING"));

        // Activate structure -> advances to ACTIVE
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structureId + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // =========================================================================
        // 5. BATCH 5: Employee Salary Assignment & Overrides
        // =========================================================================
        // Assign structure to employee from 2026-01-01
        EmployeeSalaryAssignmentCreateRequest assignReq1 = new EmployeeSalaryAssignmentCreateRequest(
                structureId, LocalDate.of(2026, 1, 1), null, "Initial hiring assignment"
        );
        MvcResult assignRes1 = salaryAssignmentMockMvc.perform(post("/api/v1/employees/" + employee.getId() + "/salary-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.effectiveFrom").value("2026-01-01"))
                .andReturn();
        Long assignId1 = objectMapper.readTree(assignRes1.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Check current assignment
        salaryAssignmentMockMvc.perform(get("/api/v1/employees/" + employee.getId() + "/salary-assignments/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(assignId1));

        // Assign revision from 2026-09-01 -> smooth timeline transition
        EmployeeSalaryAssignmentCreateRequest assignReq2 = new EmployeeSalaryAssignmentCreateRequest(
                structureId, LocalDate.of(2026, 9, 1), null, "Promotion revision"
        );
        MvcResult assignRes2 = salaryAssignmentMockMvc.perform(post("/api/v1/employees/" + employee.getId() + "/salary-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.effectiveFrom").value("2026-09-01"))
                .andReturn();
        Long assignId2 = objectMapper.readTree(assignRes2.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Add employee-specific override: Housing = 30% on assignment 2
        EmployeeSalaryComponentValueRequest overrideReq = new EmployeeSalaryComponentValueRequest(
                housingId, ComponentOverrideType.PERCENTAGE, null, BigDecimal.valueOf(30)
        );
        salaryAssignmentMockMvc.perform(post("/api/v1/employees/" + employee.getId() + "/salary-assignments/" + assignId2 + "/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overrideReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.percentage").value(30))
                .andExpect(jsonPath("$.data.overrideType").value("PERCENTAGE"));

        // =========================================================================
        // 6. BATCH 6: Dynamic Salary Calculation & Previews
        // =========================================================================
        // Calculate current salary (resolves assignment 2 as of Sept 2026)
        SalaryCalculationPreviewRequest calcReq = new SalaryCalculationPreviewRequest(LocalDate.of(2026, 9, 15));
        MvcResult calcRes = salaryCalculationMockMvc.perform(post("/api/v1/employees/" + employee.getId() + "/salary-calculations/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(calcReq)))
                .andExpect(status().isOk())
                .andReturn();

        SalaryCalculationResponse calcData = objectMapper.treeToValue(
                objectMapper.readTree(calcRes.getResponse().getContentAsString()).path("data"),
                SalaryCalculationResponse.class
        );

        assertEquals(employee.getId(), calcData.getEmployeeId());
        assertEquals("SENIOR_DEV", calcData.getSalaryStructureCode());
        assertBigDecimalEquals(new BigDecimal("68000.00"), calcData.getGrossPay());
        assertBigDecimalEquals(new BigDecimal("6800.00"), calcData.getTotalDeductions());
        assertBigDecimalEquals(new BigDecimal("61200.00"), calcData.getNetPay());

        SalaryCalculatedComponentResponse basicCalc = calcData.getComponents().stream()
                .filter(c -> "BASIC".equals(c.getComponentCode())).findFirst().orElseThrow();
        assertBigDecimalEquals(new BigDecimal("50000.00"), basicCalc.getAmount());

        SalaryCalculatedComponentResponse housingCalc = calcData.getComponents().stream()
                .filter(c -> "HOUSING".equals(c.getComponentCode())).findFirst().orElseThrow();
        assertBigDecimalEquals(new BigDecimal("15000.00"), housingCalc.getAmount());
        assertTrue(housingCalc.getOverrideApplied());

        SalaryCalculatedComponentResponse transportCalc = calcData.getComponents().stream()
                .filter(c -> "TRANSPORT".equals(c.getComponentCode())).findFirst().orElseThrow();
        assertBigDecimalEquals(new BigDecimal("3000.00"), transportCalc.getAmount());

        SalaryCalculatedComponentResponse insuranceCalc = calcData.getComponents().stream()
                .filter(c -> "INSURANCE".equals(c.getComponentCode())).findFirst().orElseThrow();
        assertBigDecimalEquals(new BigDecimal("6800.00"), insuranceCalc.getAmount());

        // Test Historical Salary Calculation (January 2026 -> uses assignment 1 with default 25% Housing)
        SalaryCalculationPreviewRequest histReq = new SalaryCalculationPreviewRequest(LocalDate.of(2026, 2, 1));
        MvcResult histRes = salaryCalculationMockMvc.perform(post("/api/v1/employees/" + employee.getId() + "/salary-calculations/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(histReq)))
                .andExpect(status().isOk())
                .andReturn();

        SalaryCalculationResponse histData = objectMapper.treeToValue(
                objectMapper.readTree(histRes.getResponse().getContentAsString()).path("data"),
                SalaryCalculationResponse.class
        );

        assertBigDecimalEquals(new BigDecimal("65500.00"), histData.getGrossPay());
        assertBigDecimalEquals(new BigDecimal("6550.00"), histData.getTotalDeductions());
        assertBigDecimalEquals(new BigDecimal("58950.00"), histData.getNetPay());

        SalaryCalculatedComponentResponse histHousing = histData.getComponents().stream()
                .filter(c -> "HOUSING".equals(c.getComponentCode())).findFirst().orElseThrow();
        assertBigDecimalEquals(new BigDecimal("12500.00"), histHousing.getAmount());
        assertFalse(histHousing.getOverrideApplied());
    }

    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertNotNull(actual, "Actual BigDecimal is null");
        assertEquals(0, expected.compareTo(actual), "Expected " + expected + " but got " + actual);
    }
}
