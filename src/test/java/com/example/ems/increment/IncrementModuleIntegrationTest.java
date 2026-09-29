package com.example.ems.increment;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.increment.dto.*;
import com.example.ems.increment.entity.*;
import com.example.ems.increment.repository.IncrementPolicyRepository;
import com.example.ems.increment.repository.IncrementRecommendationRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.example.ems.increment.repository.IncrementCycleRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class IncrementModuleIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    @Qualifier("enterpriseIncrementPolicyRepository")
    private IncrementPolicyRepository policyRepository;

    @Autowired
    private IncrementRecommendationRepository recommendationRepository;

    private Organization organization;
    private Employee employee;
    private Appraisal appraisal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        organization = new Organization();
        organization.setName("Acme Corp");
        organization.setOrganizationCode("ORG-INC-" + UUID.randomUUID().toString().substring(0, 8));
        organization = organizationRepository.save(organization);

        TenantContext.setCurrentTenant(organization.getId());

        employee = new Employee();
        employee.setFullName("Alice Smith");
        employee.setEmail("alice@acme.com");
        employee.setEmployeeId("EMP101");
        employee.setDepartment("Engineering");
        employee.setDesignation("Software Engineer");
        employee.setStatus("ACTIVE");
        employee.setAnnualSalary(new BigDecimal("60000.00"));
        employee.setJoiningDate(LocalDate.now().minusYears(2));
        employee.setOrganization(organization);
        employee = employeeRepository.save(employee);

        appraisal = new Appraisal();
        appraisal.setOrganization(organization);
        appraisal.setEmployee(employee);
        appraisal.setStatus(AppraisalStatus.COMPLETED);
        appraisal.setFinalRating(4.2);
        appraisal.setDeliveryManagementRating(4.5);
        appraisal = appraisalRepository.save(appraisal);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testFullIncrementModuleLifecycle() throws Exception {
        // 1. Create Increment Policy
        CreateIncrementPolicyRequest policyReq = new CreateIncrementPolicyRequest();
        policyReq.setName("FY 2026-27 Increment Policy");
        policyReq.setMinimumRating(3.5);
        policyReq.setMinimumGoalAchievementPercentage(70.0);
        policyReq.setMinimumAttendancePercentage(90.0);
        policyReq.setMaximumIncrementPercentage(20.0);
        policyReq.setMinimumIncrementPercentage(3.0);
        policyReq.setEffectiveDateRule(EffectiveDateRule.FIXED_DATE);
        policyReq.setEffectiveDate(LocalDate.of(2026, 10, 1));
        policyReq.setBudgetLimit(new BigDecimal("1000000.00"));
        policyReq.setActive(true);

        List<PolicyBandDto> bands = new ArrayList<>();
        bands.add(new PolicyBandDto(4.5, 5.0, 15.0));
        bands.add(new PolicyBandDto(4.0, 4.49, 12.0));
        bands.add(new PolicyBandDto(3.5, 3.99, 10.0));
        policyReq.setBands(bands);

        String policyJson = mockMvc.perform(post("/api/v1/increment/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policyReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("FY 2026-27 Increment Policy"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andReturn().getResponse().getContentAsString();

        Long policyId = objectMapper.readTree(policyJson).path("data").path("id").asLong();

        // 2. Get Current Active Policy
        mockMvc.perform(get("/api/v1/increment/policies/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(policyId))
                .andExpect(jsonPath("$.data.minimumRating").value(3.5));

        // 3. Create Increment Cycle
        CreateIncrementCycleRequest cycleReq = new CreateIncrementCycleRequest();
        cycleReq.setName("Annual Increment Cycle 2026-27");
        cycleReq.setFinancialYear("2026-27");
        cycleReq.setStartDate(LocalDate.of(2026, 9, 1));
        cycleReq.setEndDate(LocalDate.of(2026, 9, 30));
        cycleReq.setEffectiveDate(LocalDate.of(2026, 10, 1));
        cycleReq.setPolicyId(policyId);
        cycleReq.setBudgetLimit(new BigDecimal("1000000.00"));

        String cycleJson = mockMvc.perform(post("/api/v1/increment/cycles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cycleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Annual Increment Cycle 2026-27"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        Long cycleId = objectMapper.readTree(cycleJson).path("data").path("id").asLong();

        // 4. Open Cycle
        mockMvc.perform(post("/api/v1/increment/cycles/" + cycleId + "/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"));

        // 5. Check Eligibility
        CheckEligibilityRequest eligReq = new CheckEligibilityRequest();
        eligReq.setAppraisalId(appraisal.getId());
        eligReq.setProposedPercentage(10.0);
        eligReq.setDisciplinaryClear(true);

        mockMvc.perform(post("/api/v1/increment/cycles/" + cycleId + "/employees/" + employee.getId() + "/eligibility")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eligReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eligible").value(true))
                .andExpect(jsonPath("$.data.finalRating").value(4.2));

        // 6. Create Recommendation
        CreateRecommendationRequest recReq = new CreateRecommendationRequest();
        recReq.setCycleId(cycleId);
        recReq.setEmployeeId(employee.getId());
        recReq.setAppraisalId(appraisal.getId());
        recReq.setIncrementPercentage(10.0);
        recReq.setEffectiveDate(LocalDate.of(2026, 10, 1));
        recReq.setComments("Strong performance throughout the year.");

        String recJson = mockMvc.perform(post("/api/v1/increment/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.currentSalary").value(60000.00))
                .andExpect(jsonPath("$.data.incrementPercentage").value(10.0))
                .andExpect(jsonPath("$.data.incrementAmount").value(6000.00))
                .andExpect(jsonPath("$.data.recommendedSalary").value(66000.00))
                .andExpect(jsonPath("$.data.status").value("RECOMMENDED"))
                .andReturn().getResponse().getContentAsString();

        Long recId = objectMapper.readTree(recJson).path("data").path("id").asLong();

        // 7. Submit Recommendation for Approval
        mockMvc.perform(post("/api/v1/increment/recommendations/" + recId + "/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.data.approvalRequestId").isNotEmpty());

        // 8. Simulate Approval by Generic Approval Engine
        IncrementRecommendation rec = recommendationRepository.findById(recId).orElseThrow();
        rec.setStatus(IncrementRecommendationStatus.APPROVED);
        recommendationRepository.save(rec);

        // 9. Implement Salary Revision
        mockMvc.perform(post("/api/v1/increment/recommendations/" + recId + "/implement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IMPLEMENTED"))
                .andExpect(jsonPath("$.data.recommendedSalary").value(66000.00));

        // Verify Employee Salary Updated in Database
        Employee updatedEmp = employeeRepository.findById(employee.getId()).orElseThrow();
        assertEquals(new BigDecimal("66000.00"), updatedEmp.getAnnualSalary());

        // 10. Generate Increment Letter
        mockMvc.perform(post("/api/v1/increment/recommendations/" + recId + "/letter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.letterReference").isNotEmpty())
                .andExpect(jsonPath("$.data.employeeName").value("Alice Smith"))
                .andExpect(jsonPath("$.data.content", containsString("₹66000.00")));

        // 11. Verify Employee Increment History
        mockMvc.perform(get("/api/v1/employees/" + employee.getId() + "/increment-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.history", hasSize(1)))
                .andExpect(jsonPath("$.data.history[0].previousSalary").value(60000.00))
                .andExpect(jsonPath("$.data.history[0].revisedSalary").value(66000.00))
                .andExpect(jsonPath("$.data.history[0].status").value("IMPLEMENTED"));

        // 12. Try implementing the same recommendation again -> Must be rejected (Idempotency)
        mockMvc.perform(post("/api/v1/increment/recommendations/" + recId + "/implement"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already been implemented")));

        // 13. Idempotent letter generation: Calling letter generation again returns existing letter
        mockMvc.perform(post("/api/v1/increment/recommendations/" + recId + "/letter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeName").value("Alice Smith"));
    }

    @Test
    void testFailurePaths_AppraisalNotCompleted_Ineligible_OutsidePolicy_PrematureLetter() throws Exception {
        // Setup Policy
        IncrementPolicy pol = new IncrementPolicy();
        pol.setOrganization(organization);
        pol.setName("Strict Policy");
        pol.setMinimumRating(3.5);
        pol.setMinimumIncrementPercentage(3.0);
        pol.setMaximumIncrementPercentage(15.0);
        pol.setAppraisalRequired(true);
        pol = policyRepository.save(pol);

        // Setup Cycle
        IncrementCycle cyc = new IncrementCycle();
        cyc.setOrganization(organization);
        cyc.setName("Strict Cycle");
        cyc.setFinancialYear("2026-27");
        cyc.setStartDate(LocalDate.of(2026, 9, 1));
        cyc.setEndDate(LocalDate.of(2026, 9, 30));
        cyc.setEffectiveDate(LocalDate.of(2026, 10, 1));
        cyc.setBudgetLimit(new BigDecimal("1000000.00"));
        cyc.setAllocatedBudget(BigDecimal.ZERO);
        cyc.setPolicy(pol);
        cyc.setStatus(IncrementCycleStatus.OPEN);
        cyc = ((IncrementCycleRepository) webApplicationContext.getBean(IncrementCycleRepository.class)).save(cyc);

        // Scenario 1: Appraisal Not Completed (e.g. SELF_ASSESSMENT) -> Recommendation Rejected
        Appraisal draftAppraisal = new Appraisal();
        draftAppraisal.setOrganization(organization);
        draftAppraisal.setEmployee(employee);
        draftAppraisal.setStatus(AppraisalStatus.SELF_ASSESSMENT);
        draftAppraisal = appraisalRepository.save(draftAppraisal);

        CreateRecommendationRequest recReqDraft = new CreateRecommendationRequest();
        recReqDraft.setCycleId(cyc.getId());
        recReqDraft.setEmployeeId(employee.getId());
        recReqDraft.setAppraisalId(draftAppraisal.getId());
        recReqDraft.setIncrementPercentage(8.0);

        mockMvc.perform(post("/api/v1/increment/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recReqDraft)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("allowed only for finalized appraisals")));

        // Scenario 2: Percentage Outside Policy Range (25% > 15% max) -> Recommendation Rejected
        CreateRecommendationRequest recReqExceed = new CreateRecommendationRequest();
        recReqExceed.setCycleId(cyc.getId());
        recReqExceed.setEmployeeId(employee.getId());
        recReqExceed.setAppraisalId(appraisal.getId());
        recReqExceed.setIncrementPercentage(25.0);

        mockMvc.perform(post("/api/v1/increment/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recReqExceed)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("exceeds policy maximum")));

        // Scenario 3: Valid Recommendation -> Test Premature Letter Generation & Premature Implementation
        CreateRecommendationRequest validRecReq = new CreateRecommendationRequest();
        validRecReq.setCycleId(cyc.getId());
        validRecReq.setEmployeeId(employee.getId());
        validRecReq.setAppraisalId(appraisal.getId());
        validRecReq.setIncrementPercentage(10.0);

        String validRecJson = mockMvc.perform(post("/api/v1/increment/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRecReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long validRecId = objectMapper.readTree(validRecJson).path("data").path("id").asLong();

        // Duplicate recommendation in same cycle -> Rejected
        mockMvc.perform(post("/api/v1/increment/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRecReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already exists")));

        // Cannot generate letter when status is RECOMMENDED -> Rejected
        mockMvc.perform(post("/api/v1/increment/recommendations/" + validRecId + "/letter"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("only be generated after salary implementation")));

        // Cannot implement when status is RECOMMENDED -> Rejected
        mockMvc.perform(post("/api/v1/increment/recommendations/" + validRecId + "/implement"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Only APPROVED recommendations can be implemented")));
    }

    @Test
    void testApprovalChangesRequested_SentBack_Revise_Resubmit_Lifecycle() throws Exception {
        // Setup Policy & Cycle
        IncrementPolicy pol = new IncrementPolicy();
        pol.setOrganization(organization);
        pol.setName("Revision Policy");
        pol.setMinimumRating(3.0);
        pol.setMinimumIncrementPercentage(3.0);
        pol.setMaximumIncrementPercentage(20.0);
        pol.setAppraisalRequired(false);
        pol = policyRepository.save(pol);

        IncrementCycle cyc = new IncrementCycle();
        cyc.setOrganization(organization);
        cyc.setName("Revision Cycle");
        cyc.setFinancialYear("2026-27");
        cyc.setStartDate(LocalDate.of(2026, 9, 1));
        cyc.setEndDate(LocalDate.of(2026, 9, 30));
        cyc.setEffectiveDate(LocalDate.of(2026, 10, 1));
        cyc.setBudgetLimit(new BigDecimal("1000000.00"));
        cyc.setAllocatedBudget(BigDecimal.ZERO);
        cyc.setPolicy(pol);
        cyc.setStatus(IncrementCycleStatus.OPEN);
        cyc = ((IncrementCycleRepository) webApplicationContext.getBean(IncrementCycleRepository.class)).save(cyc);

        // 1. Create Recommendation at 12%
        CreateRecommendationRequest recReq = new CreateRecommendationRequest();
        recReq.setCycleId(cyc.getId());
        recReq.setEmployeeId(employee.getId());
        recReq.setIncrementPercentage(12.0);

        String recJson = mockMvc.perform(post("/api/v1/increment/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long recId = objectMapper.readTree(recJson).path("data").path("id").asLong();

        // 2. Submit for Approval -> UNDER_REVIEW
        mockMvc.perform(post("/api/v1/increment/recommendations/" + recId + "/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));

        // 3. Simulating Changes Requested event from central approval -> SENT_BACK
        IncrementRecommendation rec = recommendationRepository.findById(recId).orElseThrow();
        rec.setStatus(IncrementRecommendationStatus.SENT_BACK);
        rec.setRejectionReason("Please reduce increment to 8%");
        recommendationRepository.save(rec);

        // 4. Revise recommendation to 8% -> REVISED
        UpdateRecommendationRequest updateReq = new UpdateRecommendationRequest();
        updateReq.setIncrementPercentage(8.0);
        updateReq.setComments("Reduced to 8% as requested");

        mockMvc.perform(put("/api/v1/increment/recommendations/" + recId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVISED"))
                .andExpect(jsonPath("$.data.incrementPercentage").value(8.0))
                .andExpect(jsonPath("$.data.recommendedSalary").value(64800.00));

        // 5. Resubmit revised recommendation -> UNDER_REVIEW
        mockMvc.perform(post("/api/v1/increment/recommendations/" + recId + "/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));

        // 6. Approval completes -> APPROVED
        rec = recommendationRepository.findById(recId).orElseThrow();
        rec.setStatus(IncrementRecommendationStatus.APPROVED);
        recommendationRepository.save(rec);

        // 7. Implement Salary -> IMPLEMENTED
        mockMvc.perform(post("/api/v1/increment/recommendations/" + recId + "/implement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IMPLEMENTED"))
                .andExpect(jsonPath("$.data.recommendedSalary").value(64800.00));

        Employee updatedEmp = employeeRepository.findById(employee.getId()).orElseThrow();
        assertEquals(new BigDecimal("64800.00"), updatedEmp.getAnnualSalary());
    }
}
