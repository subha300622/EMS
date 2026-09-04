package com.example.ems.employee.controller;

import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.dto.EmploymentStructureDtos;
import com.example.ems.employee.entity.Designation;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.EmploymentType;
import com.example.ems.employee.entity.JobLevel;
import com.example.ems.employee.repository.DesignationRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.service.EmploymentStructureService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class EmploymentStructureControllerTest {

    @Autowired
    private EmploymentStructureController employmentStructureController;

    @Autowired
    private EmploymentStructureService employmentStructureService;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Organization testOrg;
    private Designation seededDesignation;
    private JobLevel seededJobLevel;
    private EmploymentType seededEmploymentType;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(employmentStructureController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Create an Organization for the test tenant
        testOrg = new Organization();
        testOrg.setName("Test Organization");
        testOrg.setOrganizationCode("TEST_ORG");
        testOrg.setStatus(OrganizationStatus.ACTIVE);
        testOrg = organizationRepository.save(testOrg);

        // Set the TenantContext so all service calls can resolve the org
        TenantContext.setCurrentTenant(testOrg.getId());

        // Seed designation hierarchy with organization reference
        seededDesignation = new Designation();
        seededDesignation.setDesignation("Software Engineer");
        seededDesignation.setDescription("Software development position");
        seededDesignation.setStatus("ACTIVE");
        seededDesignation.setOrganization(testOrg);

        seededJobLevel = new JobLevel();
        seededJobLevel.setJobLevel("Junior");
        seededJobLevel.setDescription("Junior level");
        seededJobLevel.setStatus("ACTIVE");
        seededJobLevel.setOrganization(testOrg);

        seededEmploymentType = new EmploymentType();
        seededEmploymentType.setEmploymentType("Full Time");
        seededEmploymentType.setDescription("Permanent full-time");
        seededEmploymentType.setStatus("ACTIVE");
        seededEmploymentType.setOrganization(testOrg);

        seededJobLevel.addEmploymentType(seededEmploymentType);
        seededDesignation.addJobLevel(seededJobLevel);

        seededDesignation = designationRepository.save(seededDesignation);
        seededJobLevel = seededDesignation.getJobLevels().get(0);
        seededEmploymentType = seededJobLevel.getEmploymentTypes().get(0);
    }

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    // ── 1. Create Structure Tests ────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/employment-structures - Create Structure Success")
    public void testCreateStructure_Success() throws Exception {
        EmploymentStructureDtos.CreateEmploymentTypeRequest et = EmploymentStructureDtos.CreateEmploymentTypeRequest.builder()
                .employmentType("Contract")
                .description("Contract worker")
                .build();

        EmploymentStructureDtos.CreateJobLevelRequest jl = EmploymentStructureDtos.CreateJobLevelRequest.builder()
                .jobLevel("Senior")
                .description("Senior engineer")
                .employmentTypes(List.of(et))
                .build();

        EmploymentStructureDtos.CreateEmploymentStructureRequest req = EmploymentStructureDtos.CreateEmploymentStructureRequest.builder()
                .designation("Data Scientist")
                .description("Analytics position")
                .jobLevels(List.of(jl))
                .build();

        mockMvc.perform(post("/api/v1/employment-structures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.designationId", startsWith("DES-")))
                .andExpect(jsonPath("$.designation").value("Data Scientist"))
                .andExpect(jsonPath("$.jobLevels", hasSize(1)))
                .andExpect(jsonPath("$.jobLevels[0].jobLevelId", startsWith("JL-")))
                .andExpect(jsonPath("$.jobLevels[0].jobLevel").value("Senior"))
                .andExpect(jsonPath("$.jobLevels[0].employmentTypes", hasSize(1)))
                .andExpect(jsonPath("$.jobLevels[0].employmentTypes[0].employmentTypeId", startsWith("ET-")))
                .andExpect(jsonPath("$.jobLevels[0].employmentTypes[0].employmentType").value("Contract"));
    }

    @Test
    @DisplayName("POST /api/v1/employment-structures - Duplicate Designation Returns 400")
    public void testCreateStructure_Duplicate() throws Exception {
        EmploymentStructureDtos.CreateEmploymentStructureRequest req = EmploymentStructureDtos.CreateEmploymentStructureRequest.builder()
                .designation("software engineer") // case-insensitive duplicate
                .description("Duplicate test")
                .build();

        mockMvc.perform(post("/api/v1/employment-structures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── 2. List Structure Summary Tests ──────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/employment-structures - List Summary")
    public void testListStructures_Summary() throws Exception {
        mockMvc.perform(get("/api/v1/employment-structures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].designationId").exists())
                .andExpect(jsonPath("$.content[0].jobLevelCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[0].activeJobLevelCount").exists())
                .andExpect(jsonPath("$.content[0].employmentTypeCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[0].activeEmploymentTypeCount").exists())
                .andExpect(jsonPath("$.content[0].jobLevels", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].jobLevels[0].jobLevelId").exists())
                .andExpect(jsonPath("$.content[0].jobLevels[0].jobLevel").exists())
                .andExpect(jsonPath("$.content[0].jobLevels[0].employmentTypes", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/v1/employment-structures - Filter by Search")
    public void testListStructures_SearchFilter() throws Exception {
        mockMvc.perform(get("/api/v1/employment-structures")
                        .param("search", "software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].designation", containsStringIgnoringCase("software")));
    }

    // ── 3. Details Tests ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/employment-structures/{designationId} - Details with Formatted ID")
    public void testGetStructure_FormattedId() throws Exception {
        String desIdStr = String.format("DES-%03d", seededDesignation.getId());

        mockMvc.perform(get("/api/v1/employment-structures/" + desIdStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designationId").value(desIdStr))
                .andExpect(jsonPath("$.designation").value("Software Engineer"))
                .andExpect(jsonPath("$.jobLevels", hasSize(1)))
                .andExpect(jsonPath("$.jobLevels[0].jobLevel").value("Junior"))
                .andExpect(jsonPath("$.jobLevels[0].employmentTypes", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/v1/employment-structures/{designationId} - Details with Numeric ID")
    public void testGetStructure_NumericId() throws Exception {
        mockMvc.perform(get("/api/v1/employment-structures/" + seededDesignation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designationId").value(String.format("DES-%03d", seededDesignation.getId())))
                .andExpect(jsonPath("$.designation").value("Software Engineer"));
    }

    @Test
    @DisplayName("GET /api/v1/employment-structures/{designationId} - Nonexistent Returns 404")
    public void testGetStructure_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/employment-structures/DES-999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/employment-structures/{designationId} - Invalid ID Format Returns 400")
    public void testGetStructure_InvalidFormat() throws Exception {
        mockMvc.perform(get("/api/v1/employment-structures/DES-"))
                .andExpect(status().isBadRequest());
    }

    // ── 4. Edit Structure Tests ──────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/v1/employment-structures/{designationId} - Edit Iterative Update")
    public void testEditStructure_Success() throws Exception {
        String jlIdStr = String.format("JL-%03d", seededJobLevel.getId());
        String etIdStr = String.format("ET-%03d", seededEmploymentType.getId());

        EmploymentStructureDtos.EditEmploymentTypeRequest existingEt = EmploymentStructureDtos.EditEmploymentTypeRequest.builder()
                .employmentTypeId(etIdStr)
                .employmentType("Full Time Permanent")
                .build();

        EmploymentStructureDtos.EditEmploymentTypeRequest newEt = EmploymentStructureDtos.EditEmploymentTypeRequest.builder()
                .employmentType("Intern")
                .build();

        EmploymentStructureDtos.EditJobLevelRequest existingJl = EmploymentStructureDtos.EditJobLevelRequest.builder()
                .jobLevelId(jlIdStr)
                .jobLevel("Associate Engineer")
                .employmentTypes(List.of(existingEt, newEt))
                .build();

        EmploymentStructureDtos.EditJobLevelRequest newJl = EmploymentStructureDtos.EditJobLevelRequest.builder()
                .jobLevel("Principal")
                .employmentTypes(List.of())
                .build();

        EmploymentStructureDtos.EditEmploymentStructureRequest req = EmploymentStructureDtos.EditEmploymentStructureRequest.builder()
                .designation("Staff Software Engineer")
                .description("Updated position")
                .jobLevels(List.of(existingJl, newJl))
                .build();

        mockMvc.perform(put("/api/v1/employment-structures/" + seededDesignation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designation").value("Staff Software Engineer"))
                .andExpect(jsonPath("$.jobLevels", hasSize(2)))
                .andExpect(jsonPath("$.jobLevels[0].jobLevelId").value(jlIdStr))
                .andExpect(jsonPath("$.jobLevels[0].jobLevel").value("Associate Engineer"))
                .andExpect(jsonPath("$.jobLevels[0].employmentTypes", hasSize(2)))
                .andExpect(jsonPath("$.jobLevels[1].jobLevel").value("Principal"));
    }

    // ── 5. Status Update Tests ───────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /api/v1/employment-structures/{designationId}/status - Deactivate")
    public void testUpdateStatus_Deactivate() throws Exception {
        EmploymentStructureDtos.StatusRequest req = EmploymentStructureDtos.StatusRequest.builder()
                .status("INACTIVE")
                .build();

        mockMvc.perform(patch("/api/v1/employment-structures/" + seededDesignation.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designationId").value(String.format("DES-%03d", seededDesignation.getId())))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    // ── 6. Delete Structure Tests ────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/v1/employment-structures/{designationId} - Delete Unused Success")
    public void testDeleteStructure_Success() throws Exception {
        Designation unusedDes = new Designation();
        unusedDes.setDesignation("Unused QA Lead");
        unusedDes.setDescription("Temporary for deletion test");
        unusedDes.setStatus("ACTIVE");
        unusedDes.setOrganization(testOrg);
        unusedDes = designationRepository.save(unusedDes);

        String desIdStr = String.format("DES-%03d", unusedDes.getId());

        mockMvc.perform(delete("/api/v1/employment-structures/" + unusedDes.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employment structure deleted successfully"))
                .andExpect(jsonPath("$.designationId").value(desIdStr));

        assertThat(designationRepository.findById(unusedDes.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/employment-structures/{designationId} - Conflict on Employee Assigned")
    public void testDeleteStructure_AssignedConflict() throws Exception {
        Employee emp = new Employee();
        emp.setFirstName("John");
        emp.setLastName("Doe");
        emp.setFullName("John Doe");
        emp.setEmail("john.doe@hospital.com");
        emp.setDesignation(seededDesignation.getDesignation());
        emp.setOrganization(testOrg);
        employeeRepository.save(emp);

        mockMvc.perform(delete("/api/v1/employment-structures/" + seededDesignation.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("assigned to employees")));
    }

    // ── 7. Hierarchy Validation Utility Tests ─────────────────────────────────

    @Test
    @DisplayName("Hierarchy Validation Utility - Valid Assignment Success")
    public void testValidateAssignment_Success() {
        String desId = String.format("DES-%03d", seededDesignation.getId());
        String jlId = String.format("JL-%03d", seededJobLevel.getId());
        String etId = String.format("ET-%03d", seededEmploymentType.getId());

        employmentStructureService.validateAssignment(desId, jlId, etId);
    }

    @Test
    @DisplayName("Hierarchy Validation Utility - Inactive Designation Fails")
    public void testValidateAssignment_InactiveFails() {
        seededDesignation.setStatus("INACTIVE");
        designationRepository.save(seededDesignation);

        String desId = String.format("DES-%03d", seededDesignation.getId());
        String jlId = String.format("JL-%03d", seededJobLevel.getId());
        String etId = String.format("ET-%03d", seededEmploymentType.getId());

        assertThatThrownBy(() -> employmentStructureService.validateAssignment(desId, jlId, etId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INACTIVE");
    }
}
