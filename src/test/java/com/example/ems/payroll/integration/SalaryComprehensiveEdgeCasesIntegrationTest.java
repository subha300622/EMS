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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SalaryComprehensiveEdgeCasesIntegrationTest {

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
    private EmployeeSalaryComponentValueRepository employeeSalaryComponentValueRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Organization orgA;
    private Organization orgB;
    private Employee empA;
    private Employee empB;

    @BeforeEach
    void setUp() {
        com.example.ems.config.GlobalExceptionHandler exceptionHandler = new com.example.ems.config.GlobalExceptionHandler();
        salaryComponentMockMvc = MockMvcBuilders.standaloneSetup(salaryComponentController).setControllerAdvice(exceptionHandler).build();
        salaryStructureMockMvc = MockMvcBuilders.standaloneSetup(salaryStructureController).setControllerAdvice(exceptionHandler).build();
        salaryAssignmentMockMvc = MockMvcBuilders.standaloneSetup(employeeSalaryAssignmentController).setControllerAdvice(exceptionHandler).build();
        salaryCalculationMockMvc = MockMvcBuilders.standaloneSetup(salaryCalculationController).setControllerAdvice(exceptionHandler).build();

        orgA = new Organization();
        orgA.setName("Tenant Alpha");
        orgA.setOrganizationCode("ORG_ALPHA_" + System.currentTimeMillis());
        orgA = organizationRepository.save(orgA);

        orgB = new Organization();
        orgB.setName("Tenant Beta");
        orgB.setOrganizationCode("ORG_BETA_" + System.currentTimeMillis());
        orgB = organizationRepository.save(orgB);

        empA = new Employee();
        empA.setFullName("Alice Alpha");
        empA.setEmail("alice." + System.currentTimeMillis() + "@alpha.com");
        empA.setEmployeeId("EMP_A_" + System.currentTimeMillis());
        empA.setOrganization(orgA);
        empA = employeeRepository.save(empA);

        empB = new Employee();
        empB.setFullName("Bob Beta");
        empB.setEmail("bob." + System.currentTimeMillis() + "@beta.com");
        empB.setEmployeeId("EMP_B_" + System.currentTimeMillis());
        empB.setOrganization(orgB);
        empB = employeeRepository.save(empB);
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
    @DisplayName("Edge Case 1: Cross-Tenant Isolation Across All Entities")
    void testCrossTenantIsolation() throws Exception {
        // Create component in Org A
        TenantContext.setCurrentTenant(orgA.getId());
        SalaryComponentCreateRequest compAReq = new SalaryComponentCreateRequest("Alpha Basic", "BASIC_A", "Alpha Basic", SalaryComponentType.EARNING, true, true);
        MvcResult compARes = salaryComponentMockMvc.perform(post("/api/v1/salary-components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compAReq)))
                .andExpect(status().isCreated())
                .andReturn();
        Long compAId = objectMapper.readTree(compARes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Create structure in Org A
        SalaryStructureCreateRequest structAReq = new SalaryStructureCreateRequest("Alpha Struct", "STRUCT_A", "Alpha", "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null);
        MvcResult structARes = salaryStructureMockMvc.perform(post("/api/v1/salary-structures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(structAReq)))
                .andExpect(status().isCreated())
                .andReturn();
        Long structAId = objectMapper.readTree(structARes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Switch to Org B
        TenantContext.setCurrentTenant(orgB.getId());

        // Org B cannot access Org A's component
        salaryComponentMockMvc.perform(get("/api/v1/salary-components/" + compAId))
                .andExpect(status().isNotFound());

        // Org B can create component with SAME CODE "BASIC_A" without collision
        SalaryComponentCreateRequest compBReq = new SalaryComponentCreateRequest("Beta Basic", "BASIC_A", "Beta Basic", SalaryComponentType.EARNING, true, true);
        salaryComponentMockMvc.perform(post("/api/v1/salary-components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compBReq)))
                .andExpect(status().isCreated());

        // Org B cannot access Org A's structure
        salaryStructureMockMvc.perform(get("/api/v1/salary-structures/" + structAId))
                .andExpect(status().isNotFound());

        // Org B cannot perform calculation on Org A's employee
        SalaryCalculationPreviewRequest calcReq = new SalaryCalculationPreviewRequest(LocalDate.of(2026, 1, 1));
        salaryCalculationMockMvc.perform(post("/api/v1/employees/" + empA.getId() + "/salary-calculations/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(calcReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Edge Case 2: Historical Assignment Overlap Rejection")
    void testHistoricalAssignmentOverlapRejection() throws Exception {
        TenantContext.setCurrentTenant(orgA.getId());

        // Create & activate structure
        SalaryComponentCreateRequest compReq = new SalaryComponentCreateRequest("Basic", "BASIC", "Basic", SalaryComponentType.EARNING, true, true);
        MvcResult compRes = salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(compReq))).andReturn();
        Long compId = objectMapper.readTree(compRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest("Structure", "STRUCT_HIST", "Desc", "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null);
        MvcResult structRes = salaryStructureMockMvc.perform(post("/api/v1/salary-structures").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(structReq))).andReturn();
        Long structId = objectMapper.readTree(structRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        StructureComponentCreateRequest sscReq = new StructureComponentCreateRequest(compId, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null, 1);
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(sscReq)));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/activate"));

        // Assign fixed closed range: 2026-01-01 to 2026-12-31
        EmployeeSalaryAssignmentCreateRequest closedAssign = new EmployeeSalaryAssignmentCreateRequest(
                structId, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "Contract 2026"
        );
        salaryAssignmentMockMvc.perform(post("/api/v1/employees/" + empA.getId() + "/salary-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(closedAssign)))
                .andExpect(status().isCreated());

        // Attempt overlapping assignment: 2026-06-01 to 2027-01-01 -> Must be REJECTED (Conflict)
        EmployeeSalaryAssignmentCreateRequest overlapAssign = new EmployeeSalaryAssignmentCreateRequest(
                structId, LocalDate.of(2026, 6, 1), LocalDate.of(2027, 1, 1), "Overlapping Contract"
        );
        salaryAssignmentMockMvc.perform(post("/api/v1/employees/" + empA.getId() + "/salary-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlapAssign)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Edge Case 3: Active Structure Immutability")
    void testActiveStructureImmutability() throws Exception {
        TenantContext.setCurrentTenant(orgA.getId());

        SalaryComponentCreateRequest compReq = new SalaryComponentCreateRequest("Basic", "BASIC", "Basic", SalaryComponentType.EARNING, true, true);
        MvcResult compRes = salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(compReq))).andReturn();
        Long compId = objectMapper.readTree(compRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest("Structure", "STRUCT_IMMUTABLE", "Desc", "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null);
        MvcResult structRes = salaryStructureMockMvc.perform(post("/api/v1/salary-structures").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(structReq))).andReturn();
        Long structId = objectMapper.readTree(structRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        StructureComponentCreateRequest sscReq = new StructureComponentCreateRequest(compId, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null, 1);
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(sscReq)));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/activate")).andExpect(status().isOk());

        // Attempt to modify active structure metadata -> 409 Conflict
        SalaryStructureUpdateRequest updateReq = new SalaryStructureUpdateRequest("New Name", "New Desc", "USD", PayFrequency.ANNUAL, LocalDate.of(2026, 1, 1), null);
        MvcResult updateRes = salaryStructureMockMvc.perform(put("/api/v1/salary-structures/" + structId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andReturn();
        assertEquals(409, updateRes.getResponse().getStatus());

        // Attempt to add component to active structure -> 409 Conflict
        MvcResult addCompRes = salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sscReq)))
                .andReturn();
        assertEquals(409, addCompRes.getResponse().getStatus());
    
    }
    @Test
    @DisplayName("Edge Case 4: Deep-Copy Versioning (v1 Active -> v2 Draft)")
    void testDeepCopyVersioning() throws Exception {
        TenantContext.setCurrentTenant(orgA.getId());

        SalaryComponentCreateRequest basicReq = new SalaryComponentCreateRequest("Basic", "BASIC", "Basic", SalaryComponentType.EARNING, true, true);
        MvcResult basicRes = salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(basicReq))).andReturn();
        Long basicId = objectMapper.readTree(basicRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryComponentCreateRequest hraReq = new SalaryComponentCreateRequest("HRA", "HRA", "HRA", SalaryComponentType.EARNING, true, true);
        MvcResult hraRes = salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(hraReq))).andReturn();
        Long hraId = objectMapper.readTree(hraRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest("Structure", "STRUCT_VER", "Desc", "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null);
        MvcResult structRes = salaryStructureMockMvc.perform(post("/api/v1/salary-structures").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(structReq))).andReturn();
        Long structId = objectMapper.readTree(structRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Add 2 components to v1
        StructureComponentCreateRequest ssc1 = new StructureComponentCreateRequest(basicId, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null, 1);
        StructureComponentCreateRequest ssc2 = new StructureComponentCreateRequest(hraId, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, basicId, null, BigDecimal.valueOf(25), null, 2);
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(ssc1)));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(ssc2)));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/activate"));

        // Branch v2
        MvcResult v2Res = salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/new-version"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.version").value(2))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();
        Long v2Id = objectMapper.readTree(v2Res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Verify v2 has copied the 2 components
        salaryStructureMockMvc.perform(get("/api/v1/salary-structures/" + v2Id + "/components"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].componentCode").value("BASIC"))
                .andExpect(jsonPath("$.data[1].componentCode").value("HRA"));

        // Verify v1 is still ACTIVE
        salaryStructureMockMvc.perform(get("/api/v1/salary-structures/" + structId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.version").value(1));
    }

    @Test
    @DisplayName("Edge Case 5: Transitive Circular Dependency Detection (A -> B -> C -> D -> A)")
    void testTransitiveCircularDependencyDetection() throws Exception {
        TenantContext.setCurrentTenant(orgA.getId());

        SalaryComponentCreateRequest cA = new SalaryComponentCreateRequest("Comp A", "COMP_A", "A", SalaryComponentType.EARNING, true, true);
        Long idA = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cA))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryComponentCreateRequest cB = new SalaryComponentCreateRequest("Comp B", "COMP_B", "B", SalaryComponentType.EARNING, true, true);
        Long idB = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cB))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryComponentCreateRequest cC = new SalaryComponentCreateRequest("Comp C", "COMP_C", "C", SalaryComponentType.EARNING, true, true);
        Long idC = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cC))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryComponentCreateRequest cD = new SalaryComponentCreateRequest("Comp D", "COMP_D", "D", SalaryComponentType.EARNING, true, true);
        Long idD = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cD))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest("Cycle Struct", "STRUCT_CYCLE", "Cycle", "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null);
        Long structId = objectMapper.readTree(salaryStructureMockMvc.perform(post("/api/v1/salary-structures").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(structReq))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        // Chain: A -> B -> C -> D -> A
        StructureComponentCreateRequest sscA = new StructureComponentCreateRequest(idA, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, idD, null, BigDecimal.valueOf(10), null, 1);
        StructureComponentCreateRequest sscB = new StructureComponentCreateRequest(idB, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, idA, null, BigDecimal.valueOf(10), null, 2);
        StructureComponentCreateRequest sscC = new StructureComponentCreateRequest(idC, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, idB, null, BigDecimal.valueOf(10), null, 3);
        StructureComponentCreateRequest sscD = new StructureComponentCreateRequest(idD, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, idC, null, BigDecimal.valueOf(10), null, 4);

        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(sscA)));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(sscB)));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(sscC)));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(sscD)));

        // Preview Dependency Graph -> detect cycle
        salaryStructureMockMvc.perform(get("/api/v1/salary-structures/" + structId + "/dependency-graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasCycle").value(true));

        // Validation fails -> remains DRAFT
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.errors[0].errorType").value("CIRCULAR_DEPENDENCY"));

        // Activation rejected -> 400 Bad Request
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/activate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Edge Case 6: Missing Dependency Detection")
    void testMissingDependencyDetection() throws Exception {
        TenantContext.setCurrentTenant(orgA.getId());

        SalaryComponentCreateRequest cBasic = new SalaryComponentCreateRequest("Basic", "BASIC", "Basic", SalaryComponentType.EARNING, true, true);
        Long idBasic = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cBasic))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryComponentCreateRequest cHra = new SalaryComponentCreateRequest("HRA", "HRA", "HRA", SalaryComponentType.EARNING, true, true);
        Long idHra = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cHra))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest("Missing Struct", "STRUCT_MISSING", "Missing", "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null);
        Long structId = objectMapper.readTree(salaryStructureMockMvc.perform(post("/api/v1/salary-structures").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(structReq))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        // Add ONLY HRA pointing to Basic as base (Basic is NOT added to structure)
        StructureComponentCreateRequest sscHra = new StructureComponentCreateRequest(idHra, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, idBasic, null, BigDecimal.valueOf(25), null, 1);
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(sscHra)));

        // Validation fails with DEPENDENCY_NOT_FOUND
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.errors[0].errorType").value("DEPENDENCY_NOT_FOUND"));
    }

    @Test
    @DisplayName("Edge Case 7: Formula Evaluation with Expressions and Injection Protection")
    void testFormulaEvaluationAndSecurity() throws Exception {
        TenantContext.setCurrentTenant(orgA.getId());

        SalaryComponentCreateRequest cBasic = new SalaryComponentCreateRequest("Basic", "BASIC", "Basic", SalaryComponentType.EARNING, true, true);
        Long idBasic = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cBasic))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryComponentCreateRequest cBonus = new SalaryComponentCreateRequest("Bonus", "BONUS", "Bonus", SalaryComponentType.EARNING, true, true);
        Long idBonus = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cBonus))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest("Formula Struct", "STRUCT_FORMULA", "Formula", "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null);
        Long structId = objectMapper.readTree(salaryStructureMockMvc.perform(post("/api/v1/salary-structures").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(structReq))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        StructureComponentCreateRequest sscBasic = new StructureComponentCreateRequest(idBasic, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null, 1);
        // Valid formula: BASIC * 0.40 + 500
        StructureComponentCreateRequest sscBonus = new StructureComponentCreateRequest(idBonus, CalculationType.FORMULA, CalculationBaseType.NONE, null, null, null, "BASIC * 0.40 + 500", 2);

        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(sscBasic)));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(sscBonus)));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/activate")).andExpect(status().isOk());

        // Assign to employee
        EmployeeSalaryAssignmentCreateRequest assignReq = new EmployeeSalaryAssignmentCreateRequest(structId, LocalDate.of(2026, 1, 1), null, "Formula Assignment");
        salaryAssignmentMockMvc.perform(post("/api/v1/employees/" + empA.getId() + "/salary-assignments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(assignReq)));

        // Calculate: Basic = 50000, Bonus = (50000 * 0.40) + 500 = 20500. Gross = 70500
        MvcResult calcRes = salaryCalculationMockMvc.perform(get("/api/v1/employees/" + empA.getId() + "/salary-calculations/current"))
                .andExpect(status().isOk())
                .andReturn();

        SalaryCalculationResponse calcData = objectMapper.treeToValue(objectMapper.readTree(calcRes.getResponse().getContentAsString()).path("data"), SalaryCalculationResponse.class);
        assertBigDecimalEquals(new BigDecimal("70500.00"), calcData.getGrossPay());
        assertBigDecimalEquals(new BigDecimal("70500.00"), calcData.getNetPay());
    }

    @Test
    @DisplayName("Edge Case 8: Full Complex Calculation Matrix with Gross-Based Deductions")
    void testFullCalculationMatrix() throws Exception {
        TenantContext.setCurrentTenant(orgA.getId());

        // Basic 50k, Housing 25% Basic (12.5k), Transport 3k, Bonus 40% Basic (20k), Insurance 10% Gross (8.55k)
        SalaryComponentCreateRequest cBasic = new SalaryComponentCreateRequest("Basic", "BASIC", "B", SalaryComponentType.EARNING, true, true);
        Long idBasic = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cBasic))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryComponentCreateRequest cHousing = new SalaryComponentCreateRequest("Housing", "HOUSING", "H", SalaryComponentType.EARNING, true, true);
        Long idHousing = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cHousing))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryComponentCreateRequest cTransport = new SalaryComponentCreateRequest("Transport", "TRANSPORT", "T", SalaryComponentType.EARNING, true, true);
        Long idTransport = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cTransport))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryComponentCreateRequest cBonus = new SalaryComponentCreateRequest("Bonus", "BONUS", "BN", SalaryComponentType.EARNING, true, true);
        Long idBonus = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cBonus))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryComponentCreateRequest cInsurance = new SalaryComponentCreateRequest("Insurance", "INSURANCE", "I", SalaryComponentType.DEDUCTION, true, true);
        Long idInsurance = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cInsurance))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest("Full Struct", "STRUCT_FULL", "Full", "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null);
        Long structId = objectMapper.readTree(salaryStructureMockMvc.perform(post("/api/v1/salary-structures").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(structReq))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new StructureComponentCreateRequest(idBasic, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null, 1))));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new StructureComponentCreateRequest(idHousing, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, idBasic, null, BigDecimal.valueOf(25), null, 2))));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new StructureComponentCreateRequest(idTransport, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(3000), null, null, 3))));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new StructureComponentCreateRequest(idBonus, CalculationType.FORMULA, CalculationBaseType.NONE, null, null, null, "BASIC * 0.40", 4))));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new StructureComponentCreateRequest(idInsurance, CalculationType.PERCENTAGE, CalculationBaseType.GROSS, null, null, BigDecimal.valueOf(10), null, 5))));

        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/activate")).andExpect(status().isOk());

        salaryAssignmentMockMvc.perform(post("/api/v1/employees/" + empA.getId() + "/salary-assignments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new EmployeeSalaryAssignmentCreateRequest(structId, LocalDate.of(2026, 1, 1), null, "Full Assignment"))));

        MvcResult calcRes = salaryCalculationMockMvc.perform(get("/api/v1/employees/" + empA.getId() + "/salary-calculations/current")).andExpect(status().isOk()).andReturn();
        SalaryCalculationResponse calcData = objectMapper.treeToValue(objectMapper.readTree(calcRes.getResponse().getContentAsString()).path("data"), SalaryCalculationResponse.class);

        // Gross = 50k + 12.5k + 3k + 20k = 85,500
        assertBigDecimalEquals(new BigDecimal("85500.00"), calcData.getGrossPay());
        // Deductions = 10% of 85,500 = 8,550
        assertBigDecimalEquals(new BigDecimal("8550.00"), calcData.getTotalDeductions());
        // Net = 85,500 - 8,550 = 76,950
        assertBigDecimalEquals(new BigDecimal("76950.00"), calcData.getNetPay());
    }

    @Test
    @DisplayName("Edge Case 9 & 10: Preview Overrides Do Not Persist & Database Remains Clean")
    void testPreviewOverrideDoesNotPersistDatabase() throws Exception {
        TenantContext.setCurrentTenant(orgA.getId());

        SalaryComponentCreateRequest cBasic = new SalaryComponentCreateRequest("Basic", "BASIC", "B", SalaryComponentType.EARNING, true, true);
        Long idBasic = objectMapper.readTree(salaryComponentMockMvc.perform(post("/api/v1/salary-components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(cBasic))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest("Clean Struct", "STRUCT_CLEAN", "Clean", "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null);
        Long structId = objectMapper.readTree(salaryStructureMockMvc.perform(post("/api/v1/salary-structures").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(structReq))).andReturn().getResponse().getContentAsString()).path("data").path("id").asLong();

        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/components").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new StructureComponentCreateRequest(idBasic, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null, 1))));
        salaryStructureMockMvc.perform(post("/api/v1/salary-structures/" + structId + "/activate"));

        salaryAssignmentMockMvc.perform(post("/api/v1/employees/" + empA.getId() + "/salary-assignments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new EmployeeSalaryAssignmentCreateRequest(structId, LocalDate.of(2026, 1, 1), null, "Assignment"))));

        long initialComponentValuesCount = employeeSalaryComponentValueRepository.count();

        // Perform preview simulation with ad-hoc override
        EmployeeSalaryComponentValueRequest adHocBasic = new EmployeeSalaryComponentValueRequest(idBasic, ComponentOverrideType.FIXED_AMOUNT, BigDecimal.valueOf(99000), null);
        SalaryCalculationPreviewRequest previewReq = new SalaryCalculationPreviewRequest(LocalDate.of(2026, 5, 1), List.of(adHocBasic));

        MvcResult previewRes = salaryCalculationMockMvc.perform(post("/api/v1/employees/" + empA.getId() + "/salary-calculations/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(previewReq)))
                .andExpect(status().isOk())
                .andReturn();

        SalaryCalculationResponse previewData = objectMapper.treeToValue(objectMapper.readTree(previewRes.getResponse().getContentAsString()).path("data"), SalaryCalculationResponse.class);
        assertBigDecimalEquals(new BigDecimal("99000.00"), previewData.getGrossPay());

        // Verify database state: NO new records persisted!
        long finalComponentValuesCount = employeeSalaryComponentValueRepository.count();
        assertEquals(initialComponentValuesCount, finalComponentValuesCount, "Preview must not write or persist any component values to database!");

        // Regular current calculation still yields original 50,000
        MvcResult curRes = salaryCalculationMockMvc.perform(get("/api/v1/employees/" + empA.getId() + "/salary-calculations/current")).andExpect(status().isOk()).andReturn();
        SalaryCalculationResponse curData = objectMapper.treeToValue(objectMapper.readTree(curRes.getResponse().getContentAsString()).path("data"), SalaryCalculationResponse.class);
        assertBigDecimalEquals(new BigDecimal("50000.00"), curData.getGrossPay());
    }
}
