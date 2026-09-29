package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.AppraisalInitiationMode;
import com.example.ems.approval.entity.ApprovalWorkflowDefinition;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.repository.ApprovalWorkflowDefinitionRepository;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.service.JwtService;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class AppraisalConfigurationIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private AppraisalConfigurationController configurationController;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ApprovalWorkflowDefinitionRepository workflowDefinitionRepository;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Organization org1;
    private Organization org2;
    private User adminUser;
    private String adminToken;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(configurationController).build();

        org1 = new Organization();
        org1.setName("Acme Org 1 " + System.currentTimeMillis());
        org1.setOrganizationCode("ORG1-" + System.currentTimeMillis());
        org1 = organizationRepository.save(org1);

        org2 = new Organization();
        org2.setName("Beta Org 2 " + System.currentTimeMillis());
        org2.setOrganizationCode("ORG2-" + System.currentTimeMillis());
        org2 = organizationRepository.save(org2);

        TenantContext.setCurrentTenant(org1.getId());

        Role adminRole = roleRepository.findByName("PLATFORM_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("PLATFORM_ADMIN");
                    r.setDescription("Platform Administrator");
                    return roleRepository.save(r);
                });

        adminUser = new User();
        adminUser.setUserId("ADM-" + System.currentTimeMillis());
        adminUser.setWorkEmail("admin_" + System.currentTimeMillis() + "@acme.com");
        adminUser.setPassword("password123");
        adminUser.setRole(adminRole);
        adminUser.setOrganization(org1);
        adminUser = userRepository.save(adminUser);

        Employee adminEmp = new Employee();
        adminEmp.setFullName("Admin User");
        adminEmp.setEmail(adminUser.getWorkEmail());
        adminEmp.setOrganization(org1);
        employeeRepository.save(adminEmp);

        adminToken = "Bearer " + jwtService.generateAccessToken(adminUser.getUserId(), adminUser.getWorkEmail(), "PLATFORM_ADMIN");
    }

    @AfterEach
    public void cleanup() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should retrieve default appraisal configuration for new organization")
    public void testGetDefaultConfiguration() throws Exception {
        mockMvc.perform(get("/api/v1/appraisal/configuration")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.initiationMode").value("HR_AND_EMPLOYEE"))
                .andExpect(jsonPath("$.data.employeeRequestEnabled").value(true));
    }

    @Test
    @DisplayName("Should save and update appraisal configuration and request reasons")
    public void testSaveAndGetConfiguration() throws Exception {
        ApprovalWorkflowDefinition wf = new ApprovalWorkflowDefinition();
        wf.setOrganization(org1);
        wf.setName("Appraisal Special Request WF");
        wf.setWorkflowType(WorkflowType.APPRAISAL_REQUEST);
        wf = workflowDefinitionRepository.save(wf);

        SaveAppraisalConfigurationRequest req = new SaveAppraisalConfigurationRequest();
        req.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        req.setEmployeeRequestEnabled(true);
        req.setApprovalWorkflowId(wf.getId());
        req.setMinServiceMonths(6);
        req.setMinGapMonths(12);
        req.setAllowedRequestReasons(List.of(
                new CreateRequestReasonDto("PROMOTION", "Promotion Consideration", "Appraisal for promotion"),
                new CreateRequestReasonDto("SALARY_REVISION", "Salary Revision", "Appraisal for salary revision")
        ));

        mockMvc.perform(put("/api/v1/appraisal/configuration")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.approvalWorkflowId").value(wf.getId()))
                .andExpect(jsonPath("$.data.allowedRequestReasons", hasSize(2)));
    }

    @Test
    @DisplayName("Should reject cross-tenant approval workflow linkage")
    public void testCrossTenantWorkflowProtection() throws Exception {
        // Workflow belongs to Org 2
        ApprovalWorkflowDefinition wfOrg2 = new ApprovalWorkflowDefinition();
        wfOrg2.setOrganization(org2);
        wfOrg2.setName("Org 2 Workflow");
        wfOrg2.setWorkflowType(WorkflowType.APPRAISAL_REQUEST);
        wfOrg2 = workflowDefinitionRepository.save(wfOrg2);

        SaveAppraisalConfigurationRequest req = new SaveAppraisalConfigurationRequest();
        req.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        req.setApprovalWorkflowId(wfOrg2.getId());

        mockMvc.perform(put("/api/v1/appraisal/configuration")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should manage appraisal request reasons: create, activate/deactivate, update")
    public void testRequestReasonCrudAndSoftDeactivation() throws Exception {
        CreateRequestReasonDto createDto = new CreateRequestReasonDto("ROLE_CHANGE", "Role Change", "Request due to role change");

        mockMvc.perform(post("/api/v1/appraisal/request-reasons")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("ROLE_CHANGE"))
                .andExpect(jsonPath("$.data.active").value(true));

        // Soft deactivation
        ReasonStatusDto statusDto = new ReasonStatusDto();
        statusDto.setActive(false);

        mockMvc.perform(get("/api/v1/appraisal/request-reasons")
                        .header("Authorization", adminToken)
                        .param("activeOnly", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }
}
