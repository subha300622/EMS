package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.CreateEmployeeAppraisalRequestDto;
import com.example.ems.appraisal.dto.WithdrawAppraisalRequestDto;
import com.example.ems.appraisal.entity.*;
import com.example.ems.appraisal.repository.*;
import com.example.ems.appraisal.service.AppraisalApprovalEventListener;
import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.event.ApprovalWorkflowRejectedEvent;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class AppraisalRequestFlowIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private AppraisalRequestController requestController;

    @Autowired
    private AppraisalApprovalEventListener approvalEventListener;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalRequestReasonRepository reasonRepository;

    @Autowired
    private AppraisalRequestRepository requestRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private AppraisalConfigurationRepository configRepository;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Organization org;
    private Employee employee;
    private User employeeUser;
    private String employeeToken;
    private AppraisalRequestReason reason;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(requestController).build();

        org = new Organization();
        org.setName("Flow Org " + System.currentTimeMillis());
        org.setOrganizationCode("FLOW-" + System.currentTimeMillis());
        org = organizationRepository.save(org);

        TenantContext.setCurrentTenant(org.getId());

        AppraisalConfiguration config = new AppraisalConfiguration();
        config.setOrganization(org);
        config.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        config.setEmployeeRequestEnabled(true);
        config.setMinServiceMonths(3);
        config.setActive(true);
        configRepository.save(config);

        reason = new AppraisalRequestReason();
        reason.setOrganization(org);
        reason.setCode("PROBATION_PASS");
        reason.setName("Probation Passed");
        reason.setActive(true);
        reason = reasonRepository.save(reason);

        Role empRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE");
                    return roleRepository.save(r);
                });

        employeeUser = new User();
        employeeUser.setUserId("EMP-USER-" + System.currentTimeMillis());
        employeeUser.setWorkEmail("emp_" + System.currentTimeMillis() + "@flow.com");
        employeeUser.setPassword("pass");
        employeeUser.setRole(empRole);
        employeeUser.setOrganization(org);
        employeeUser = userRepository.save(employeeUser);

        employee = new Employee();
        employee.setFullName("John Doe");
        employee.setEmail(employeeUser.getWorkEmail());
        employee.setOrganization(org);
        employee.setJoiningDate(LocalDate.now().minusMonths(6));
        employee = employeeRepository.save(employee);

        employeeToken = "Bearer " + jwtService.generateAccessToken(employeeUser.getUserId(), employeeUser.getWorkEmail(), "EMPLOYEE");
    }

    @AfterEach
    public void cleanup() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should create draft appraisal request and submit to approval engine")
    public void testCreateDraftAndSubmitRequest() throws Exception {
        CreateEmployeeAppraisalRequestDto dto = new CreateEmployeeAppraisalRequestDto();
        dto.setReasonId(reason.getId());
        dto.setJustification("I have completed 6 months with stellar performance.");

        // 1. Create Draft
        String draftResp = mockMvc.perform(post("/api/v1/appraisal/requests")
                        .header("Authorization", employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        Long requestId = objectMapper.readTree(draftResp).get("data").get("id").asLong();

        // 2. Submit Request
        mockMvc.perform(post("/api/v1/appraisal/requests/" + requestId + "/submit")
                        .header("Authorization", employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));

        AppraisalRequest updatedReq = requestRepository.findById(requestId).orElseThrow();
        assertEquals(AppraisalRequestStatus.UNDER_REVIEW, updatedReq.getStatus());
    }

    @Test
    @DisplayName("Should handle approval workflow completed event and automatically instantiate Appraisal record (Flow A)")
    public void testApprovalEventCreatesAppraisal() {
        AppraisalRequest req = new AppraisalRequest();
        req.setOrganization(org);
        req.setEmployee(employee);
        req.setReason(reason);
        req.setJustification("Special appraisal request");
        req.setStatus(AppraisalRequestStatus.UNDER_REVIEW);
        req = requestRepository.save(req);

        // Simulate Central Generic Approval Engine completion event
        ApprovalWorkflowCompletedEvent event = new ApprovalWorkflowCompletedEvent(
                this,
                "wf-inst-123",
                WorkflowType.APPRAISAL_REQUEST,
                "APPRAISAL_REQUEST",
                String.valueOf(req.getId()),
                org.getId(),
                ApprovalStatus.APPROVED
        );

        approvalEventListener.onApprovalWorkflowCompleted(event);

        AppraisalRequest updatedReq = requestRepository.findById(req.getId()).orElseThrow();
        assertEquals(AppraisalRequestStatus.APPRAISAL_CREATED, updatedReq.getStatus());
        assertNotNull(updatedReq.getAppraisalId());

        Optional<Appraisal> createdAppraisal = appraisalRepository.findById(updatedReq.getAppraisalId());
        assertTrue(createdAppraisal.isPresent());
        assertEquals(AppraisalStatus.CREATED, createdAppraisal.get().getStatus());
        assertEquals(employee.getId(), createdAppraisal.get().getEmployee().getId());
    }

    @Test
    @DisplayName("Should handle approval workflow rejection event")
    public void testApprovalEventRejection() {
        AppraisalRequest req = new AppraisalRequest();
        req.setOrganization(org);
        req.setEmployee(employee);
        req.setReason(reason);
        req.setJustification("Special request to be rejected");
        req.setStatus(AppraisalRequestStatus.UNDER_REVIEW);
        req = requestRepository.save(req);

        ApprovalWorkflowRejectedEvent event = new ApprovalWorkflowRejectedEvent(
                this,
                "wf-inst-999",
                WorkflowType.APPRAISAL_REQUEST,
                "APPRAISAL_REQUEST",
                String.valueOf(req.getId()),
                org.getId(),
                "Budget constraints"
        );

        approvalEventListener.onApprovalWorkflowRejected(event);

        AppraisalRequest updatedReq = requestRepository.findById(req.getId()).orElseThrow();
        assertEquals(AppraisalRequestStatus.REJECTED, updatedReq.getStatus());
    }

    @Test
    @DisplayName("Should withdraw appraisal request by employee")
    public void testWithdrawAppraisalRequest() throws Exception {
        AppraisalRequest req = new AppraisalRequest();
        req.setOrganization(org);
        req.setEmployee(employee);
        req.setReason(reason);
        req.setJustification("To be withdrawn");
        req.setStatus(AppraisalRequestStatus.DRAFT);
        req = requestRepository.save(req);

        WithdrawAppraisalRequestDto withdrawDto = new WithdrawAppraisalRequestDto();
        withdrawDto.setReason("Changed my mind");

        mockMvc.perform(post("/api/v1/appraisal/requests/" + req.getId() + "/withdraw")
                        .header("Authorization", employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("WITHDRAWN"));
    }
}
