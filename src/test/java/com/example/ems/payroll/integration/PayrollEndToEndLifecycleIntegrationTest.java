package com.example.ems.payroll.integration;

import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.payroll.controller.*;
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
public class PayrollEndToEndLifecycleIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private SalaryComponentController componentController;

    @Autowired
    private SalaryStructureController structureController;

    @Autowired
    private EmployeeSalaryAssignmentController assignmentController;

    @Autowired
    private SalaryCalculationController calculationController;

    @Autowired
    private PayrollRunController payrollRunController;

    @Autowired
    private PayrollEmployeeController payrollEmployeeController;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Long testOrgId;
    private Employee johnDoe;
    private Employee janeDoe;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                componentController,
                structureController,
                assignmentController,
                calculationController,
                payrollRunController,
                payrollEmployeeController
        ).setControllerAdvice(new GlobalExceptionHandler()).build();

        Organization org = new Organization();
        org.setName("TechCorp Solutions");
        org.setNormalizedName("techcorp solutions");
        org.setOrganizationCode("TCORP_2026");
        org = organizationRepository.save(org);
        testOrgId = org.getId();
        TenantContext.setCurrentTenant(testOrgId);

        johnDoe = new Employee();
        johnDoe.setEmployeeId("EMP-120");
        johnDoe.setFullName("John Doe");
        johnDoe.setEmail("john.doe@techcorp.com");
        johnDoe.setDepartment("Engineering");
        johnDoe.setDesignation("Software Engineer");
        johnDoe.setOrganization(org);
        johnDoe.setJoiningDate(LocalDate.of(2026, 1, 1));
        johnDoe = employeeRepository.save(johnDoe);
    }


    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Scenario 1 & 2: Complete Payroll Lifecycle (Components -> Structure -> Assignment -> Calculation -> Payroll Run -> Process -> Snapshots -> Finalize -> Payslip -> Structure v2 Immutability)")
    void testCompletePayrollLifecycleAndImmutability() throws Exception {
        // ====================================================================
        // STEP 1 — Create 4 Salary Components
        // ====================================================================
        Long basicId = createComponent("Basic Salary", "BASIC", SalaryComponentType.EARNING, true);
        Long housingId = createComponent("Housing Allowance", "HRA", SalaryComponentType.EARNING, true);
        Long transportId = createComponent("Transport Allowance", "TRANSPORT", SalaryComponentType.EARNING, false);
        Long insuranceId = createComponent("Insurance", "INSURANCE", SalaryComponentType.DEDUCTION, false);


        assertNotNull(basicId);
        assertNotNull(housingId);
        assertNotNull(transportId);
        assertNotNull(insuranceId);

        // ====================================================================
        // STEP 2 — Create Salary Structure (DEV_2026 v1 - DRAFT)
        // ====================================================================
        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest(
                "Developer Salary Structure",
                "DEV_2026",
                "Developer monthly salary",
                "INR",
                PayFrequency.MONTHLY,
                LocalDate.of(2026, 9, 1),
                null
        );


        MvcResult structResult = mockMvc.perform(post("/api/v1/salary-structures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(structReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.version").value(1))
                .andReturn();

        Long structureId = objectMapper.readTree(structResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // ====================================================================
        // STEP 3 — Add Structure Components
        // ====================================================================
        // 1. Basic: Fixed 50,000
        addStructureComponent(structureId, basicId, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null);

        // 2. Housing: 25% of Basic = 12,500
        addStructureComponent(structureId, housingId, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, basicId, null, BigDecimal.valueOf(25), null);

        // 3. Transport: Fixed 3,000
        addStructureComponent(structureId, transportId, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(3000), null, null);

        // 4. Insurance: 10% of Gross = 6,550
        addStructureComponent(structureId, insuranceId, CalculationType.PERCENTAGE, CalculationBaseType.GROSS, null, null, BigDecimal.valueOf(10), null);

        // ====================================================================
        // STEP 4 — Validate Structure
        // ====================================================================
        mockMvc.perform(post("/api/v1/salary-structures/{id}/validate", structureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VALIDATED"));

        // ====================================================================
        // STEP 5 — Activate Structure
        // ====================================================================
        mockMvc.perform(post("/api/v1/salary-structures/{id}/activate", structureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // ====================================================================
        // STEP 6 — Assign Structure to Employee 120 (John Doe)
        // ====================================================================
        EmployeeSalaryAssignmentCreateRequest assignReq = new EmployeeSalaryAssignmentCreateRequest(
                structureId,
                LocalDate.of(2026, 9, 1),
                null,
                "New employee salary assignment"
        );

        mockMvc.perform(post("/api/v1/employees/{empId}/salary-assignments", johnDoe.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.salaryStructureCode").value("DEV_2026"))
                .andExpect(jsonPath("$.data.salaryStructureVersion").value(1));

        // ====================================================================
        // STEP 7 — Test Salary Calculation (Preview / Current)
        // Gross = 50,000 + 12,500 + 3,000 = 65,500
        // Deduction = 10% * 65,500 = 6,550
        // Net = 65,500 - 6,550 = 58,950
        // ====================================================================
        mockMvc.perform(get("/api/v1/employees/{empId}/salary-calculations/current", johnDoe.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.grossPay").value(65500.0))
                .andExpect(jsonPath("$.data.totalDeductions").value(6550.0))
                .andExpect(jsonPath("$.data.netPay").value(58950.0));


        // ====================================================================
        // STEP 8 — Create September Payroll Run
        // ====================================================================
        PayrollRunCreateRequest septRunReq = new PayrollRunCreateRequest(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
        );

        MvcResult septRunResult = mockMvc.perform(post("/api/v1/payroll/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(septRunReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.periodStart").value("2026-09-01"))
                .andExpect(jsonPath("$.data.periodEnd").value("2026-09-30"))
                .andReturn();

        Long septRunId = objectMapper.readTree(septRunResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // ====================================================================
        // STEP 9 — Process September Payroll Run
        // ====================================================================
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", septRunId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CALCULATED"))
                .andExpect(jsonPath("$.data.totalGross").value(65500.0))
                .andExpect(jsonPath("$.data.totalDeductions").value(6550.0))
                .andExpect(jsonPath("$.data.totalNet").value(58950.0))
                .andExpect(jsonPath("$.data.processedEmployees").value(1));

        // ====================================================================
        // STEP 10 — Verify Payroll Employee Snapshot
        // ====================================================================
        MvcResult empListResult = mockMvc.perform(get("/api/v1/payroll/runs/{id}/employees", septRunId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].employeeId").value(johnDoe.getId()))
                .andExpect(jsonPath("$.data[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.data[0].grossAmount").value(65500.0))
                .andExpect(jsonPath("$.data[0].deductionsAmount").value(6550.0))
                .andExpect(jsonPath("$.data[0].netAmount").value(58950.0))
                .andExpect(jsonPath("$.data[0].status").value("CALCULATED"))
                .andReturn();

        Long septPayrollEmpId = objectMapper.readTree(empListResult.getResponse().getContentAsString())
                .path("data").get(0).path("id").asLong();

        // ====================================================================
        // STEP 11 — Verify Payroll Items Snapshot
        // ====================================================================
        mockMvc.perform(get("/api/v1/payroll/runs/{id}/employees/{peId}/items", septRunId, septPayrollEmpId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[?(@.componentCode == 'BASIC')].amount").value(50000.0))
                .andExpect(jsonPath("$.data[?(@.componentCode == 'HRA')].amount").value(12500.0))
                .andExpect(jsonPath("$.data[?(@.componentCode == 'TRANSPORT')].amount").value(3000.0))
                .andExpect(jsonPath("$.data[?(@.componentCode == 'INSURANCE')].amount").value(6550.0));

        // ====================================================================
        // STEP 12 — Finalize September Payroll Run
        // ====================================================================
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/finalize", septRunId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FINALIZED"));

        // ====================================================================
        // STEP 13 — Get Payslip for September
        // ====================================================================
        mockMvc.perform(get("/api/v1/payroll/runs/{id}/employees/{peId}/payslip", septRunId, septPayrollEmpId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.data.grossAmount").value(65500.0))
                .andExpect(jsonPath("$.data.deductionsAmount").value(6550.0))
                .andExpect(jsonPath("$.data.netAmount").value(58950.0))
                .andExpect(jsonPath("$.data.items.length()").value(4));

        // ====================================================================
        // STEP 14 — Immutability Test: Create Structure v2 with New Basic (60,000)
        // ====================================================================
        SalaryStructureCreateRequest structV2Req = new SalaryStructureCreateRequest(
                "Developer Salary Structure v2",
                "DEV_2026",
                "Updated developer salary",
                "INR",
                PayFrequency.MONTHLY,
                LocalDate.of(2026, 10, 1),
                null
        );

        MvcResult structV2Result = mockMvc.perform(post("/api/v1/salary-structures/{id}/new-version", structureId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(structV2Req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.version").value(2))
                .andReturn();

        Long structV2Id = objectMapper.readTree(structV2Result.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // Get copied components for v2 and update Basic to 60,000
        MvcResult v2CompsRes = mockMvc.perform(get("/api/v1/salary-structures/{id}/components", structV2Id))
                .andExpect(status().isOk())
                .andReturn();
        Long v2BasicStructureComponentId = objectMapper.readTree(v2CompsRes.getResponse().getContentAsString())
                .path("data").get(0).path("id").asLong();

        StructureComponentUpdateRequest updateBasicReq = new StructureComponentUpdateRequest(
                CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(60000), null, null, 1
        );
        mockMvc.perform(put("/api/v1/salary-structures/{id}/components/{scId}", structV2Id, v2BasicStructureComponentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBasicReq)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/salary-structures/{id}/validate", structV2Id)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/salary-structures/{id}/activate", structV2Id)).andExpect(status().isOk());

        // Assign structure v2 to John Doe starting from 2026-10-01
        EmployeeSalaryAssignmentCreateRequest assignV2Req = new EmployeeSalaryAssignmentCreateRequest(
                structV2Id,
                LocalDate.of(2026, 10, 1),
                null,
                "October increment to v2"
        );

        mockMvc.perform(post("/api/v1/employees/{empId}/salary-assignments", johnDoe.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignV2Req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.salaryStructureVersion").value(2));

        // ====================================================================
        // STEP 15 — Process October Payroll
        // Basic = 60,000, Housing = 15,000, Transport = 3,000 -> Gross = 78,000
        // Insurance = 10% * 78,000 = 7,800 -> Net = 70,200
        // ====================================================================
        PayrollRunCreateRequest octRunReq = new PayrollRunCreateRequest(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        MvcResult octRunResult = mockMvc.perform(post("/api/v1/payroll/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(octRunReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long octRunId = objectMapper.readTree(octRunResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", octRunId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalGross").value(78000.0))
                .andExpect(jsonPath("$.data.totalDeductions").value(7800.0))
                .andExpect(jsonPath("$.data.totalNet").value(70200.0));

        // ====================================================================
        // CRITICAL IMMUTABILITY ASSERTION:
        // September Payroll Snapshots MUST NOT Change!
        // ====================================================================
        mockMvc.perform(get("/api/v1/payroll/runs/{id}", septRunId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FINALIZED"))
                .andExpect(jsonPath("$.data.totalGross").value(65500.0))
                .andExpect(jsonPath("$.data.totalDeductions").value(6550.0))
                .andExpect(jsonPath("$.data.totalNet").value(58950.0));

        mockMvc.perform(get("/api/v1/payroll/runs/{id}/employees/{peId}/items", septRunId, septPayrollEmpId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.componentCode == 'BASIC')].amount").value(50000.0))
                .andExpect(jsonPath("$.data[?(@.componentCode == 'HRA')].amount").value(12500.0))
                .andExpect(jsonPath("$.data[?(@.componentCode == 'INSURANCE')].amount").value(6550.0));
    }

    @Test
    @DisplayName("Scenario 3: Employee-Level Component Override (Customization)")
    void testEmployeeCustomizedComponentOverride() throws Exception {
        Organization org = organizationRepository.findById(testOrgId).orElseThrow();
        janeDoe = new Employee();
        janeDoe.setEmployeeId("EMP-121");
        janeDoe.setFullName("Jane Doe");
        janeDoe.setEmail("jane.doe@techcorp.com");
        janeDoe.setDepartment("Engineering");
        janeDoe.setDesignation("Senior Software Engineer");
        janeDoe.setOrganization(org);
        janeDoe.setJoiningDate(LocalDate.of(2026, 1, 1));
        janeDoe = employeeRepository.save(janeDoe);

        Long basicId = createComponent("Basic Salary Custom", "BASIC_CUST", SalaryComponentType.EARNING, true);
        Long bonusId = createComponent("Special Allowance", "SPECIAL_ALLOW", SalaryComponentType.EARNING, false);

        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest(
                "Standard Structure", "STD_2026", "Desc", "INR", PayFrequency.MONTHLY,
                LocalDate.of(2026, 9, 1), null
        );
        MvcResult structRes = mockMvc.perform(post("/api/v1/salary-structures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(structReq))).andReturn();
        Long structId = objectMapper.readTree(structRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        addStructureComponent(structId, basicId, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null);
        addStructureComponent(structId, bonusId, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(5000), null, null);
        mockMvc.perform(post("/api/v1/salary-structures/{id}/validate", structId)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/salary-structures/{id}/activate", structId)).andExpect(status().isOk());

        // Assign to Jane Doe with an OVERRIDE: Special Allowance = 15,000 instead of 5,000
        EmployeeSalaryComponentValueRequest overrideReq = new EmployeeSalaryComponentValueRequest(
                bonusId, ComponentOverrideType.FIXED_AMOUNT, BigDecimal.valueOf(15000), null
        );

        EmployeeSalaryAssignmentCreateRequest assignReq = new EmployeeSalaryAssignmentCreateRequest(
                structId,
                LocalDate.of(2026, 9, 1),
                null,
                "Custom compensation for Jane"
        );
        assignReq.setComponentValues(List.of(overrideReq));

        mockMvc.perform(post("/api/v1/employees/{empId}/salary-assignments", janeDoe.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isCreated());

        // Jane Doe Salary: Basic(50,000) + Special(15,000) = 65,000 Gross
        mockMvc.perform(get("/api/v1/employees/{empId}/salary-calculations/current", janeDoe.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.grossPay").value(65000.0))
                .andExpect(jsonPath("$.data.netPay").value(65000.0));
    }

    @Test
    @DisplayName("Scenario 4: Negative Test Cases (Unassigned employee, Reprocess finalized, Duplicate period)")
    void testNegativePayrollScenarios() throws Exception {
        Organization org = organizationRepository.findById(testOrgId).orElseThrow();
        janeDoe = new Employee();
        janeDoe.setEmployeeId("EMP-121");
        janeDoe.setFullName("Jane Doe");
        janeDoe.setEmail("jane.doe@techcorp.com");
        janeDoe.setDepartment("Engineering");
        janeDoe.setDesignation("Senior Software Engineer");
        janeDoe.setOrganization(org);
        janeDoe.setJoiningDate(LocalDate.of(2026, 1, 1));
        janeDoe = employeeRepository.save(janeDoe);

        Long basicId = createComponent("Basic Salary Neg", "BASIC_NEG", SalaryComponentType.EARNING, true);

        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest(
                "Neg Structure", "NEG_2026", "Desc", "INR", PayFrequency.MONTHLY,
                LocalDate.of(2026, 9, 1), null
        );
        MvcResult structRes = mockMvc.perform(post("/api/v1/salary-structures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(structReq))).andReturn();
        Long structId = objectMapper.readTree(structRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        addStructureComponent(structId, basicId, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(40000), null, null);
        mockMvc.perform(post("/api/v1/salary-structures/{id}/validate", structId)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/salary-structures/{id}/activate", structId)).andExpect(status().isOk());

        // Assign John Doe, but leave Jane Doe WITHOUT an assignment
        EmployeeSalaryAssignmentCreateRequest assignReq = new EmployeeSalaryAssignmentCreateRequest(
                structId, LocalDate.of(2026, 9, 1), null, "Assign John"
        );
        mockMvc.perform(post("/api/v1/employees/{empId}/salary-assignments", johnDoe.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignReq))).andExpect(status().isCreated());

        // 1. Create Payroll Run
        PayrollRunCreateRequest runReq = new PayrollRunCreateRequest(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)
        );
        MvcResult runRes = mockMvc.perform(post("/api/v1/payroll/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(runReq)))
                .andExpect(status().isCreated())
                .andReturn();
        Long runId = objectMapper.readTree(runRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. Process: John Doe succeeds, Jane Doe (unassigned) fails gracefully -> Run status is FAILED
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.processedEmployees").value(1));

        mockMvc.perform(get("/api/v1/payroll/runs/{id}/employees", runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.employeeId == " + janeDoe.getId() + ")].status").value("FAILED"));

        // 3. Negative Case: Cannot finalize a FAILED run (400 Bad Request)
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/finalize", runId))
                .andExpect(status().isBadRequest());

        // 4. Negative Case: Cannot create duplicate payroll run for exact same period (409 Conflict)
        mockMvc.perform(post("/api/v1/payroll/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(runReq)))
                .andExpect(status().isConflict());

        // 5. Now assign Jane Doe as well and create an October run to test Finalization & Reprocessing lock
        EmployeeSalaryAssignmentCreateRequest assignJaneReq = new EmployeeSalaryAssignmentCreateRequest(
                structId, LocalDate.of(2026, 10, 1), null, "Assign Jane"
        );
        mockMvc.perform(post("/api/v1/employees/{empId}/salary-assignments", janeDoe.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignJaneReq))).andExpect(status().isCreated());

        PayrollRunCreateRequest octRunReq = new PayrollRunCreateRequest(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)
        );
        MvcResult octRunRes = mockMvc.perform(post("/api/v1/payroll/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(octRunReq)))
                .andExpect(status().isCreated())
                .andReturn();
        Long octRunId = objectMapper.readTree(octRunRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Process October (clean, CALCULATED)
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", octRunId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CALCULATED"));

        // Finalize October (FINALIZED)
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/finalize", octRunId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FINALIZED"));

        // Negative Case: Cannot re-process a FINALIZED run (409 Conflict)
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", octRunId))
                .andExpect(status().isConflict());
    }


    // ── HELPER METHODS ────────────────────────────────────────────────────────

    private Long createComponent(String name, String code, SalaryComponentType type, boolean taxable) throws Exception {
        SalaryComponentCreateRequest req = new SalaryComponentCreateRequest(
                name, code, "Description for " + name, type, taxable, true
        );
        MvcResult res = mockMvc.perform(post("/api/v1/salary-components")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    private void addStructureComponent(Long structureId, Long componentId, CalculationType calcType,
                                       CalculationBaseType baseType, Long baseComponentId,
                                       BigDecimal fixedAmount, BigDecimal percentage, String formula) throws Exception {
        StructureComponentCreateRequest req = new StructureComponentCreateRequest(
                componentId, calcType, baseType, baseComponentId, fixedAmount, percentage, formula, null
        );
        mockMvc.perform(post("/api/v1/salary-structures/{id}/components", structureId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}

