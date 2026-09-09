package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.CreateAppraisalCycleDto;
import com.example.ems.appraisal.dto.CycleEligibilityCriteriaDto;
import com.example.ems.appraisal.dto.ReviewStageDto;
import com.example.ems.appraisal.dto.SelfAssessmentDto;
import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalRepository;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.example.ems.appraisal.dto.ReviewStageConfigurationDto;
import com.example.ems.appraisal.dto.SaveAppraisalConfigurationRequest;
import com.example.ems.appraisal.entity.AppraisalInitiationMode;
import com.example.ems.appraisal.service.AppraisalConfigurationService;

@SpringBootTest
@Transactional
public class AppraisalCycleAndEvaluationIntegrationTest {

    private MockMvc cycleMockMvc;
    private MockMvc evalMockMvc;

    @Autowired
    private AppraisalCycleController cycleController;

    @Autowired
    private AppraisalEvaluationController evaluationController;

    @Autowired
    private AppraisalConfigurationService appraisalConfigurationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private Organization org;
    private Employee hrEmployee;
    private User hrUser;
    private String hrToken;

    private Employee appraisee;
    private User appraiseeUser;
    private String appraiseeToken;

    private Employee reviewer;
    private User reviewerUser;
    private String reviewerToken;

    @BeforeEach
    public void setup() {
        cycleMockMvc = MockMvcBuilders.standaloneSetup(cycleController).build();
        evalMockMvc = MockMvcBuilders.standaloneSetup(evaluationController).build();

        org = new Organization();
        org.setName("Cycle Org " + System.currentTimeMillis());
        org.setOrganizationCode("CYCLE-" + System.currentTimeMillis());
        org = organizationRepository.save(org);

        TenantContext.setCurrentTenant(org.getId());

        Role adminRole = roleRepository.findByName("PLATFORM_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("PLATFORM_ADMIN");
                    return roleRepository.save(r);
                });

        Role empRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE");
                    return roleRepository.save(r);
                });

        // 1. HR User
        hrUser = new User();
        hrUser.setUserId("HR-USER-" + System.currentTimeMillis());
        hrUser.setWorkEmail("hr_" + System.currentTimeMillis() + "@cycle.com");
        hrUser.setPassword("pass");
        hrUser.setRole(adminRole);
        hrUser.setOrganization(org);
        hrUser = userRepository.save(hrUser);

        hrEmployee = new Employee();
        hrEmployee.setFullName("HR Manager");
        hrEmployee.setEmail(hrUser.getWorkEmail());
        hrEmployee.setOrganization(org);
        hrEmployee = employeeRepository.save(hrEmployee);
        hrToken = "Bearer " + jwtService.generateAccessToken(hrUser.getUserId(), hrUser.getWorkEmail(), "PLATFORM_ADMIN");

        // 2. Appraisee
        appraiseeUser = new User();
        appraiseeUser.setUserId("DEV-USER-" + System.currentTimeMillis());
        appraiseeUser.setWorkEmail("dev_" + System.currentTimeMillis() + "@cycle.com");
        appraiseeUser.setPassword("pass");
        appraiseeUser.setRole(empRole);
        appraiseeUser.setOrganization(org);
        appraiseeUser = userRepository.save(appraiseeUser);

        appraisee = new Employee();
        appraisee.setFullName("Developer One");
        appraisee.setEmail(appraiseeUser.getWorkEmail());
        appraisee.setOrganization(org);
        appraisee.setDepartment("Engineering");
        appraisee.setJoiningDate(LocalDate.of(2025, 1, 1));
        appraisee = employeeRepository.save(appraisee);
        appraiseeToken = "Bearer " + jwtService.generateAccessToken(appraiseeUser.getUserId(), appraiseeUser.getWorkEmail(), "EMPLOYEE");

        // 3. Reviewer (Lead/Manager)
        reviewerUser = new User();
        reviewerUser.setUserId("LEAD-USER-" + System.currentTimeMillis());
        reviewerUser.setWorkEmail("lead_" + System.currentTimeMillis() + "@cycle.com");
        reviewerUser.setPassword("pass");
        reviewerUser.setRole(adminRole);
        reviewerUser.setOrganization(org);
        reviewerUser = userRepository.save(reviewerUser);

        reviewer = new Employee();
        reviewer.setFullName("Tech Lead");
        reviewer.setEmail(reviewerUser.getWorkEmail());
        reviewer.setOrganization(org);
        reviewer = employeeRepository.save(reviewer);
        reviewerToken = "Bearer " + jwtService.generateAccessToken(reviewerUser.getUserId(), reviewerUser.getWorkEmail(), "PLATFORM_ADMIN");
    }

    @AfterEach
    public void cleanup() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should execute Flow B: Create regular HR cycle, batch generate appraisals, evaluate, review, and publish")
    public void testFlowBEndToEndLifecycle() throws Exception {
        // Step 0: Save Custom 1-stage Appraisal Configuration
        SaveAppraisalConfigurationRequest configReq = new SaveAppraisalConfigurationRequest();
        configReq.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        configReq.setEmployeeRequestEnabled(true);
        ReviewStageConfigurationDto stage1 = new ReviewStageConfigurationDto();
        stage1.setStageOrder(1);
        stage1.setStageName("Team Lead Review");
        stage1.setRequiredPermission("APPRAISAL_REVIEW");
        stage1.setRequired(true);
        stage1.setWeightage(1.0);
        configReq.setReviewStages(List.of(stage1));
        appraisalConfigurationService.saveOrUpdateConfiguration(configReq, hrEmployee);

        // Step 1: Create Cycle
        CreateAppraisalCycleDto cycleDto = new CreateAppraisalCycleDto();
        cycleDto.setName("Annual Appraisal 2026");
        cycleDto.setType("ANNUAL");
        cycleDto.setStartDate(LocalDate.of(2026, 1, 1));
        cycleDto.setEndDate(LocalDate.of(2026, 12, 31));
        CycleEligibilityCriteriaDto criteria = new CycleEligibilityCriteriaDto();
        criteria.setMinimumServiceMonths(6);
        criteria.setDepartment("Engineering");
        cycleDto.setEligibleEmployeeCriteria(criteria);

        String createResp = cycleMockMvc.perform(post("/api/v1/appraisals/cycles")
                        .header("Authorization", hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cycleDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Annual Appraisal 2026"))
                .andReturn().getResponse().getContentAsString();

        Long cycleId = objectMapper.readTree(createResp).get("data").get("id").asLong();

        // Step 2: Activate Cycle & Generate Appraisals directly for eligible employees (Flow B)
        cycleMockMvc.perform(post("/api/v1/appraisals/cycles/" + cycleId + "/activate")
                        .header("Authorization", hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"));

        cycleMockMvc.perform(post("/api/v1/appraisals/cycles/" + cycleId + "/generate")
                        .header("Authorization", hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generatedCount").value(1));

        List<Appraisal> appraisals = appraisalRepository.findByCycleId(cycleId);
        assertEquals(1, appraisals.size());
        Appraisal appraisal = appraisals.get(0);
        assertEquals(appraisee.getId(), appraisal.getEmployee().getId());
        assertEquals(AppraisalStatus.CREATED, appraisal.getStatus());

        // Step 3: Employee Submits Self-Assessment
        SelfAssessmentDto selfDto = new SelfAssessmentDto();
        selfDto.setOverallRating(4.5);
        selfDto.setStrengths("System Architecture, Java Performance");
        selfDto.setAchievements("Delivered high-throughput payment and approval engine integration");
        selfDto.setDevelopmentAreas("Public speaking");

        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/self-assessment/submit")
                        .header("Authorization", appraiseeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(selfDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallRating").value(4.5));

        Appraisal updatedApp = appraisalRepository.findById(appraisal.getId()).orElseThrow();
        assertEquals(AppraisalStatus.STAGE_REVIEW, updatedApp.getStatus());

        // Step 4: Stage Reviewer Submits Review
        ReviewStageDto reviewDto = new ReviewStageDto();
        reviewDto.setStageOrder(1);
        reviewDto.setStageName("Team Lead Review");
        reviewDto.setRating(4.6);
        reviewDto.setComments("Exceptional architectural leadership and high code quality.");
        reviewDto.setRecommendation("PROMOTION");

        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/review")
                        .header("Authorization", reviewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(4.6))
                .andExpect(jsonPath("$.data.recommendation").value("PROMOTION"));

        // Step 5: Publish Appraisal Result
        evalMockMvc.perform(post("/api/v1/appraisals/" + appraisal.getId() + "/publish")
                        .header("Authorization", hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.finalRating").value(4.6))
                .andExpect(jsonPath("$.data.performanceCategory").value("OUTSTANDING"));

        // Step 6: Verify History Logs Trail
        evalMockMvc.perform(get("/api/v1/appraisals/" + appraisal.getId() + "/history")
                        .header("Authorization", hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }
}
